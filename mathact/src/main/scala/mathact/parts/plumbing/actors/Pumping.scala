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
import mathact.parts.BaseActor
import mathact.parts.data.{PumpEvents, CtrlEvents}
import collection.mutable.{Map ⇒ MutMap}
import scalafx.scene.image.Image
import scala.concurrent.duration._


/** Supervisor for all Pump actors
  * Created by CAB on 15.05.2016.
  */

class Pumping(controller: ActorRef) extends BaseActor{
  //Parameters
  val pumpStartingTimeout = 10.second
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){case _: Exception ⇒ Resume}
  //Objects
//  val log = Logging.getLogger(context.system, this)
  //Enums
  object WorkMode extends Enumeration {val Creating, Starting, Work, Stopping = Value}


//  object WorkMode extends Enumeration {val Creating, Starting, Work, Stopping = Value}
//  object PumpState extends Enumeration {val Created, Ready, Destroyed = Value}
//  case class PumpData(actor: ActorRef, name: String, image: Option[Image], var state: PumpState.Value)
//  case object StartTimeOut

  //Variables
  var state = WorkMode.Creating
//  var controller: Option[ActorRef] = None
//  val drives = MutMap[ActorRef, PumpData]()







  //Messages handling
  reaction(state){
    //Creating of new drive actor
    case PumpEvents.NewDrive(name, image) ⇒
      //New actor
      val drive = context.actorOf(Props(new Drive(self)), "DriveOf" + name)
      context.watch(drive)
//      drives += (drive → PumpData(drive, name, image, PumpState.Created))
//      //Do init if pumping in started or in work mode

      //TODO

      //Response
      sender ! drive
    //Starting
    case CtrlEvents.PumpingStart(initSpeed, initStepMode) ⇒


      //!!!Далее здесь
      // 1) Двух этапный, синхронный запуск всех зарегестрированых Drive (конструирование и инициализация)
      // 2) Посылка сообщения контроллеру о готовности
      // 3) Обработка завершения работы и ошибок.







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
  }}
