/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * @                                                                             @ *
 *           #          # #                                 #    (c) 2016 CAB      *
 *          # #      # #                                  #  #                     *
 *         #  #    #  #           # #     # #           #     #              # #   *
 *        #   #  #   #             #       #          #        #              #    *
 *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
 *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
 *     #          #   # # # #   #       #      #  #           #  #         #       *
 *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
 *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
 * @                                                                             @ *
\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */

package mathact.parts.plumbing.actors

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import mathact.parts.{IdGenerator, StateActorBase}
import mathact.parts.data._
import mathact.parts.plumbing.PumpLike
import mathact.parts.plumbing.fitting._
import scala.collection.mutable.{Map ⇒ MutMap, Queue ⇒ MutQueue}


/** Manage tool
  * Inlets and outlets never removes
  * Created by CAB on 15.05.2016.
  */

private [mathact] class Drive(
  val toolId: Int,
  val pump: PumpLike,
  val pumping: ActorRef,
  val userLogging: ActorRef,
  val visualization: ActorRef)
extends StateActorBase(ActorState.Building) with IdGenerator with DriveBuilding with DriveConnectivity
with DriveStartStop with DriveMessaging with DriveUIControl{ import ActorState._, TaskKind._
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){ case _: Throwable ⇒ Resume }
  //Definitions
  case class SubscriberData(
    id: (ActorRef, Int),
    inlet: InletData,
    var inletQueueSize: Int = 0)
  case class OutletState(
    outletId: Int,
    name: Option[String],
    pipe: OutPipe[_],
    subscribers: MutMap[(ActorRef, Int), SubscriberData] = MutMap(),  //((subscribe tool drive, inlet ID), SubscriberData)
    var pushTimeout: Option[Long] = None)
  case class InletState(
    inletId: Int,
    name: Option[String],
    pipe: InPipe[_],
    taskQueue: MutQueue[Msg.RunTask[_]] = MutQueue(),
    publishers: MutMap[(ActorRef, Int), OutletData] = MutMap(),  // ((publishers tool drive, outlet ID), SubscriberData)
    var currentTask: Option[Msg.RunTask[_]] = None)
  //Variables
  val outlets = MutMap[Int, OutletState]()  //(Outlet ID, OutletData)
  val inlets = MutMap[Int, InletState]()    //(Inlet ID, OutletData)
  var visualisationLaval: VisualisationLaval = VisualisationLaval.None
  //On start
  val impeller = context.actorOf(Props(new Impeller(self)), "ImpellerOf_" + pump.toolName)
  context.watch(impeller)
  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[(StateMsg, ActorState), Unit] = {
    case (Msg.BuildDrive, Building) ⇒
      doConnectivity()
    case (Msg.StartDrive, Starting) ⇒
      doStarting()
    case (Msg.StopDrive, Working) ⇒
      state = Stopping
      doStopping()
    case (Msg.TerminateDrive, Stopping) ⇒
      state = Terminating
      doTerminating()}
  /** Handling after reaction executed */
  def postHandling: PartialFunction[(Msg, ActorState), Unit] = {
    //Check if all pipes connected in Building state, if so switch to Starting, send DriveBuilt and ToolBuilt
    case (_: Msg.PipesConnected | Msg.BuildDrive, Building) ⇒ isAllConnected match{
      case true ⇒
        log.debug(
          s"[Drive.postHandling @ Building] All pipes connected, send Msg.DriveBuilt, and switch to Working mode.")
        state = Starting
        pumping ! Msg.DriveBuilt
        buildAndSendToolBuiltInfo()
      case false ⇒
        log.debug(s"[Drive.postHandling @ Building] Not all pipes connected.")}
    //Check if user start function executed in Starting state
    case (Msg.StartDrive | _: Msg.TaskDone | _: Msg.TaskFailed, Starting) ⇒ isStarted match{
      case true ⇒
        log.debug(
          s"[Drive.postHandling @ Starting] Started, send Msg.DriveStarted, " +
          s"run message processing and switch to Working mode.")
        state = Working
        startUserMessageProcessing()
        pumping ! Msg.DriveStarted
      case false ⇒
        log.debug(s"[Drive.postHandling @ Starting] Not started yet.")}
    //Check if user stop function executed in Stopping state
    case (Msg.StopDrive | _: Msg.TaskDone | _: Msg.TaskFailed, Stopping) ⇒ isStopped match{
      case true ⇒
        log.debug(s"[Drive.postHandling @ Stopping] Stopped, send Msg.DriveStopped")
        pumping ! Msg.DriveStopped
      case false ⇒
        log.debug(s"[Drive.postHandling @ Stopping] Not stopped yet.")}
    //Check if all message queues is empty in Terminating, and if so do terminating
    case (Msg.TerminateDrive | _: Msg.TaskDone | _: Msg.TaskFailed, Terminating) ⇒ isAllMsgProcessed match{
      case true ⇒
        log.debug(s"[Drive.postHandling @ Terminating] Terminated, send Msg.DriveTerminated, and PoisonPill")
        pumping ! Msg.DriveTerminated
        self ! PoisonPill
      case false ⇒
        log.debug(s"[Drive.postHandling @ Terminating] Not terminated yet.")}}
  /** Actor reaction on messages */
  def reaction: PartialFunction[(Msg, ActorState), Unit] = {
    //Construction, adding pipes, ask from object
    case (Msg.AddOutlet(pipe, name), state) ⇒ sender ! addOutletAsk(pipe, name, state)
    case (Msg.AddInlet(pipe, name), state) ⇒ sender ! addInletAsk(pipe, name, state)
    //Connectivity, ask from object
    case (message: Msg.ConnectPipes, state) ⇒ sender ! connectPipesAsk(message, state)
    //Connectivity, internal
    case (Msg.AddConnection(id, initiator, inletId, outlet), Building) ⇒ addConnection(id, initiator, inletId, outlet)
    case (Msg.ConnectTo(id, initiator, outletId, inlet), Building) ⇒ connectTo(id, initiator, outletId, inlet)
    case (Msg.PipesConnected(id, inletId, outletId), Building) ⇒ pipesConnected(id, inletId, outletId)
    //Starting
    case (Msg.TaskDone(Start, _, time, _), Starting) ⇒ startingTaskDone(time)
    case (Msg.TaskTimeout(Start, _, time), Starting) ⇒ startingTaskTimeout(time)
    case (Msg.TaskFailed(Start, _, time, error), Starting) ⇒ startingTaskFailed(time, error)
    //Messaging, ask from object
    case (Msg.UserData(outletId, value), state) ⇒ sender ! userDataAsk(outletId, value, state)
    //Messaging
    case (Msg.UserMessage(outletId, inletId, value), state) ⇒ userMessage(outletId, inletId, value, state)
    case (Msg.DriveLoad(sub, outId, queueSize), Starting | Working | Stopping) ⇒ driveLoad(sub, outId, queueSize)
    case (Msg.TaskDone(Massage, inletId, time, _), Working | Stopping | Terminating) ⇒ messageTaskDone(inletId, time)
    case (Msg.TaskTimeout(Massage, inId, time), Working | Stopping | Terminating) ⇒ messageTaskTimeout(inId, time)
    case (Msg.TaskFailed(Massage, inId, t, err), Working | Stopping | Terminating) ⇒ messageTaskFailed(inId, t, err)
    //Stopping
    case (Msg.TaskDone(Stop, _, time, _), Stopping) ⇒ stoppingTaskDone(time)
    case (Msg.TaskTimeout(Stop, _, time), Stopping) ⇒ stoppingTaskTimeout(time)
    case (Msg.TaskFailed(Stop, _, time, error), Stopping) ⇒ stoppingTaskFailed(time, error)
    //Managing
    case (Msg.SkipAllTimeoutTask, _) ⇒ impeller ! Msg.SkipAllTimeoutTask
    case (Msg.SetVisualisationLaval(laval), _) ⇒ visualisationLaval = laval
    case (Msg.ShowToolUi, Starting | Working | Stopping) ⇒ showToolUi()
    case (Msg.HideToolUi, Starting | Working | Stopping) ⇒ hideToolUi()}}
