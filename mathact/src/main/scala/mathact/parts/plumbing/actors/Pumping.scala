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
import akka.event.Logging
import mathact.parts.ActorUtils
import mathact.parts.data.{PumpEvents, CtrlEvents}
import PumpEvents._
import collection.mutable.{Map ⇒ MutMap}
import scalafx.scene.image.Image
import scala.concurrent.duration._


/** Supervisor for all Pump actors
  * Created by CAB on 15.05.2016.
  */

class Pumping extends Actor with ActorUtils{
  //Parameters
  val pumpStartingTimeout = 10.second
  //Objects
  val log = Logging.getLogger(context.system, this)
  //Definitions
  private object WorkMode extends Enumeration {val Creating, Starting, Work, Stopping = Value}
  private object PumpState extends Enumeration {val Created, Ready, Destroyed = Value}
  private case class PumpData(actor: ActorRef, name: String, image: Option[Image], var state: PumpState.Value)
  private case object StartTimeOut

  //Variables
  private var state = WorkMode.Creating
  private var controller: Option[ActorRef] = None
  private val drives = MutMap[ActorRef, PumpData]()







  //Messages handling
  def receive = {





//    case NewDrive(name, image) ⇒
//      logMsgD("Pumping.NewDrive", s"New drive with name: $name, image: $image", state)
//      //New actor
//      val drive = context.actorOf(Props(new Drive(self)), "DriveOf" + name)
//      context.watch(drive)
//      drives += (drive → PumpData(drive, name, image, PumpState.Created))
//      //Do init if pumping in started or in work mode
//
//      //TODO
//
//      //Response
//      sender ! drive
//    case PlumbingInit(stepMode) ⇒
//      logMsgD("Pumping.PlumbingInit", s"Init, stepMode: $stepMode, created drives: $drives", state)
//      state = WorkMode.Starting
//      controller = Some(sender)
//      //Init of pumps
//      drives.foreach{case (a, _)  ⇒ a ! Ready(stepMode)}
//      //Start timer
//      context.system.scheduler.scheduleOnce(pumpStartingTimeout, self, StartTimeOut)
//    case Steady ⇒
//      logMsgD("Pumping.Steady", s"Sender actor initialised", state)
//      //Set ready
//      drives.get(sender()).foreach(_.state = PumpState.Ready)
//      //If all ready, init is done
//      drives.values.exists(_.state != PumpState.Ready) match{
//        case false ⇒
//          logMsgD("Pumping.Steady", s"All ready, change state to Work", state)
//          state = WorkMode.Work
//          controller.foreach(_ ! PlumbingStarted)
//        case true ⇒
//          logMsgD("Pumping.Steady", s"Not all ready, drives: $drives", state)}




//    case StartTimeOut ⇒
//      logMsgD("Pumping.StartTimeOut", s"Timeout: $pumpStartingTimeout", state)
//      state match{
//        case WorkMode.Creating | WorkMode.Starting ⇒
//          logMsgE("Pumping.StartTimeOut", s"Pumping not started in: $pumpStartingTimeout", state)
//          //Send error to controller
//          controller.foreach(_ ! CtrlEvents.FatalError(s"The system not ready in $pumpStartingTimeout"))
//        case _ ⇒}
//    case Terminated(actor) ⇒
//      logMsgD("Pumping.Terminated", s"Terminated actor: $actor", state)
//      //Check if in list
//      drives.contains(actor) match{
//        case true ⇒
//          logMsgE("Pumping.Terminated", s"Actor suddenly terminated: $actor", state)
//          //Disconnect connections
//
//          //TODO
//
//          //Remove from list
//          drives -= actor
//        case _ ⇒}
    case x ⇒
      logMsgW("Pumping", "Receive unknown message: " + x, state)}
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){
    case _: Exception ⇒ Resume}}
