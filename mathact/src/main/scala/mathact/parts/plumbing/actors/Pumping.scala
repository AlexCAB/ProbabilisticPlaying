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
import mathact.parts.data.{Sketch, Msg}
import collection.mutable.{Map ⇒ MutMap}
import scalafx.scene.image.Image
import scala.concurrent.duration._


/** Supervisor for all Pumps
  * Created by CAB on 15.05.2016.
  */

class Pumping(controller: ActorRef, sketch: Sketch) extends BaseActor{
  //Parameters
  val driveBuildingTimeout = 5.second
  val driveStartingTimeout = 10.second
  val driveStoppingTimeout = 10.second
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){case _: Exception ⇒ Resume}
  //Enums
  object WorkMode extends Enumeration {val Creating, Building, Starting, Work, Stopping, Ended = Value}
  object DriveState extends Enumeration {val Creating, Building, Built, Starting, Started, Work, Stopping, Ended = Value}
  //Data
  case class PumpData(actor: ActorRef, name: String, image: Option[Image], state: DriveState.Value){
    def withState(newState: DriveState.Value): PumpData = PumpData(actor, name, image, newState)}
  //Messages
  case class DriveBuildingTimeout(drive: ActorRef)
  case class DriveStartingTimeout(drive: ActorRef)
  case class DriveStoppingTimeout(drive: ActorRef)


//  object WorkMode extends Enumeration {val Creating, Starting, Work, Stopping = Value}
//
//
//  case object StartTimeOut

  //Variables
  var state = WorkMode.Creating
//  var controller: Option[ActorRef] = None
  val drives = MutMap[ActorRef, PumpData]()







  //Messages handling
  reaction(state){
    //Creating of new drive actor
    case Msg.NewDrive(name, image) ⇒
      //Check state
      state match{
        case WorkMode.Creating | WorkMode.Building | WorkMode.Starting | WorkMode.Work⇒
          //New actor
          val drive = context.actorOf(Props(new Drive(self)), "DriveOf" + name)
          context.watch(drive)
          //Do init if pumping in started or in work mode
          state match{
            case WorkMode.Creating ⇒
              log.debug(s"[Pumping.NewDrive] Creating drive, name: $name")
              drives += (drive → PumpData(drive, name, image, DriveState.Creating))
              sender ! Right(drive)
            case _ ⇒
              log.debug(s"[Pumping.NewDrive] Creating of drive after WorkMode.Creating step, name: $name")
              drives += (drive → PumpData(drive, name, image, DriveState.Building))
              context.system.scheduler.scheduleOnce(driveBuildingTimeout, self, DriveBuildingTimeout(drive))
              drive ! Msg.BuildDrive
              sender ! Right(drive)}
        case _ ⇒
          log.error(s"[Pumping.NewDrive] Creating of drive after WorkMode.Stopping step, name: $name")
          sender ! Left(new Exception("[Pumping.NewDrive] Creating of drive after WorkMode.Stopping step."))}
    //Starting
    case Msg.StartPumping(initSpeed, initStepMode) if state == WorkMode.Creating ⇒
      log.info(s"[Pumping.StartPumping] Start of built of sketch: $sketch, drives: $drives.")
      state = WorkMode.Building
      //Starting of building of each Drive
      drives ++= drives.values
        .map{ drive ⇒
          log.debug(s"[Pumping.StartPumping] Building of: $drive")
          context.system.scheduler.scheduleOnce(driveBuildingTimeout, self, DriveBuildingTimeout(drive.actor))
          drive.actor ! Msg.BuildDrive
          (drive.actor, drive.withState(DriveState.Building))}
    //One drive built
    case Msg.DriveBuilt ⇒
      drives.get(sender) match{
        case Some(drive) ⇒
          //Update state
          drives += (drive.actor → drive.withState(DriveState.Built))
          //If all built, do starting
          drives.values.exists(_.state != DriveState.Built) match{
            case false ⇒
              log.info(s"[Pumping.DriveStarted] All drives built of sketch: $sketch, switch to starting mode.")
              //Switch state
              state = WorkMode.Starting
              //Do start
              drives ++= drives.values
                .map{ drive ⇒
                  log.debug(s"[Pumping.DriveBuilt] Staring of: $drive")
                  context.system.scheduler.scheduleOnce(driveStartingTimeout, self, DriveStartingTimeout(drive.actor))
                  drive.actor ! Msg.StartDrive
                  (drive.actor, drive.withState(DriveState.Starting))}
            case true ⇒
              log.debug(s"[Pumping.DriveBuilt] Not all drives built, drives: $drives")}
        case None ⇒
          log.error(s"[Pumping.DriveBuilt] Unknown drive: $sender")}
    //One drive started
    case Msg.DriveStarted ⇒
      drives.get(sender) match{
        case Some(drive) ⇒
          //Update state
          drives += (drive.actor → drive.withState(DriveState.Started))
          //If all started, switch to work mode
          drives.values.exists(_.state != DriveState.Started) match{
            case false ⇒
              log.info(s"[Pumping.DriveStarted] All drives started of sketch: $sketch, switch to work mode.")
              //Switch state
              state = WorkMode.Work
              drives ++= drives.values.map{ drive ⇒ (drive.actor, drive.withState(DriveState.Work))}
              //Send ready msg
              controller ! Msg.PumpingStarted
            case true ⇒
              log.debug(s"[Pumping.DriveBuilt] Not all drives built, drives: $drives")}
        case None ⇒
          log.error(s"[Pumping.DriveBuilt] Unknown drive: $sender")}





    //!!! Далее здесь
    // 1) Завершение работы драйва (нормальное (по закрытии инструмента) и аварийно по ошибке инструмента или разущению драйва)
    //    В случае аварийного завершения драйва, остальным нужно разослать сообщение об этом чтобы они поудалали соединения с разрущеным драйвом.
    // 2) Завершение работы инициализированое контроллером (стандартное). Разослать всем драйвам сообщение о завершении,
    //    чтобы они выполениеи пользовательские функци завершения. Дождатся их разрущения (Terminated) и саморазрушится.







    //Time out on building of drive
    case DriveBuildingTimeout(drive) ⇒
      drives.get(drive) match{
        case Some(driveData) if driveData.state == DriveState.Building ⇒
          log.error(s"[Pumping.DriveBuildingTimeout] Drive: $driveData, not built in $driveBuildingTimeout")

          ???

        case None ⇒
          log.error(s"[Pumping.DriveBuildingTimeout] Unknown drive: $sender")
        case _ ⇒}
    //Time out on init of drive
    case DriveStartingTimeout(drive) ⇒
      drives.get(drive) match{
        case Some(driveData) if driveData.state == DriveState.Starting ⇒
          log.error(s"[Pumping.DriveBuildingTimeout] Drive: $driveData, not started in $driveStartingTimeout")

          ???

        case None ⇒
          log.error(s"[Pumping.DriveStartingTimeout] Unknown drive: $sender")
        case _ ⇒}
    //Time out on stopping of drive
    case DriveStoppingTimeout(drive) ⇒
      drives.get(drive) match{
        case Some(driveData) if driveData.state == DriveState.Stopping ⇒
          log.error(s"[Pumping.DriveBuildingTimeout] Drive: $driveData, not stopped in $driveStoppingTimeout")

          ???

        case None ⇒
          log.error(s"[Pumping.DriveStoppingTimeout] Unknown drive: $sender")
        case _ ⇒}




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
//      //Starting timer
//      context.system.scheduler.scheduleOnce(pumpStartingTimeout, self, StartTimeOut)
//    case Steady ⇒
//      logMsgD("Pumping.Steady", s"Sender actor initialised", state)
//      //Set ready
//      drives.get(sender()).foreach(_.state = DriveState.Ready)
//      //If all ready, init is done
//      drives.values.exists(_.state != DriveState.Ready) match{
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
//          controller.foreach(_ ! Msg.FatalError(s"The system not ready in $pumpStartingTimeout"))
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
