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
import mathact.parts.data.{WorkMode, StepMode, Sketch, Msg}
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
  object State extends Enumeration {
    val Creating, Building, Starting, Work, Stopping, Ended = Value}
  object DriveState extends Enumeration {
    val Creating, Building, Built, Starting, Started, Work, Stopping, Ended = Value}
  //Data
  case class PumpData(actor: ActorRef, name: String, image: Option[Image], state: DriveState.Value, mode: StepMode, isDone: Boolean){
    def withDriveState(newState: DriveState.Value): PumpData = PumpData(actor, name, image, newState, mode, isDone)
    def withDriveMode(newMode: StepMode): PumpData = PumpData(actor, name, image, state, newMode, isDone)
    def setIsDone(newIsDone: Boolean): PumpData = PumpData(actor, name, image, state, mode, newIsDone)}
  //Messages
  case class DriveBuildingTimeout(drive: ActorRef)
  case class DriveStartingTimeout(drive: ActorRef)
  case class DriveStoppingTimeout(drive: ActorRef)
  case class ProcessTick(startTime: Long)






//  object State extends Enumeration {val Creating, Starting, Work, Stopping = Value}
//  case object StartTimeOut




  //Variables
  var state = State.Creating
  var processTickTimeout = 1000L
  var stepMode = StepMode.None
  var workMode = WorkMode.Paused
  var expectedTickMessage: Option[ProcessTick] = None

  val drives = MutMap[ActorRef, PumpData]()



  //Functions
  def calcProcessTickTimeout(speed: Double): Long = {

    (speed * 1000).toLong    //Переписать в нелеинейное измение скорости (подумать как будут удобней)

  }
  def startTick(): Unit = expectedTickMessage match{
    case None ⇒
      val timeoutMsg = ProcessTick(System.currentTimeMillis())
      expectedTickMessage = Some(timeoutMsg)
      log.debug(s"[startTick] Timeout: $processTickTimeout millis, expected message: $timeoutMsg")
      context.system.scheduler.scheduleOnce(processTickTimeout.millis, self, timeoutMsg)
    case Some(m) ⇒
      log.error(s"[startTick] Prev tick not done, msg: $m")}
  //Messages handling
  reaction((state, stepMode, workMode)){
    //Creating of new drive actor
    case Msg.NewDrive(pump, name, image) ⇒
      //Check state
      state match{
        case State.Creating | State.Building | State.Starting | State.Work⇒
          //New actor
          val drive = context.actorOf(Props(new Drive(pump, name, self)), "DriveOf" + name)
          context.watch(drive)
          //Do init if pumping in started or in work stepMode
          state match{
            case State.Creating ⇒
              log.debug(s"[NewDrive] Creating drive, name: $name")
              drives += (drive → PumpData(drive, name, image, DriveState.Creating, stepMode, isDone = true))
              sender ! Right(drive)
            case _ ⇒
              log.debug(s"[NewDrive] Creating of drive after State.Creating step, name: $name")
              drives += (drive → PumpData(drive, name, image, DriveState.Building, stepMode, isDone = true))
              context.system.scheduler.scheduleOnce(driveBuildingTimeout, self, DriveBuildingTimeout(drive))
              drive ! Msg.BuildDrive
              sender ! Right(drive)}
        case _ ⇒
          log.error(s"[NewDrive] Creating of drive after State.Stopping step, name: $name")
          sender ! Left(new Exception("[NewDrive] Creating of drive after State.Stopping step."))}
    //Starting
    case Msg.StartPumping(initStepMode, initSpeed) if state == State.Creating ⇒
      log.info(s"[StartPumping] Start of built of sketch: $sketch, drives: $drives.")
      state = State.Building
      stepMode = initStepMode
      processTickTimeout = calcProcessTickTimeout(initSpeed)
      //Starting of building of each Drive
      drives ++= drives.values
        .map{ drive ⇒
          log.debug(s"[StartPumping] Building of: $drive")
          context.system.scheduler.scheduleOnce(driveBuildingTimeout, self, DriveBuildingTimeout(drive.actor))
          drive.actor ! Msg.BuildDrive(initStepMode)
          (drive.actor, drive.withDriveState(DriveState.Building))}
    //One drive built
    case Msg.DriveBuilt ⇒
      drives.get(sender) match{
        case Some(drive) ⇒
          //Update state
          drives += (drive.actor → drive.withDriveState(DriveState.Built))
          //If all built, do starting
          drives.values.exists(_.state != DriveState.Built) match{
            case false ⇒
              log.info(s"[DriveStarted] All drives built of sketch: $sketch, switch to starting stepMode.")
              //Switch state
              state = State.Starting
              //Do start
              drives ++= drives.values
                .map{ drive ⇒
                  log.debug(s"[DriveBuilt] Staring of: $drive")
                  context.system.scheduler.scheduleOnce(driveStartingTimeout, self, DriveStartingTimeout(drive.actor))
                  drive.actor ! Msg.StartDrive
                  (drive.actor, drive.withDriveState(DriveState.Starting))}
            case true ⇒
              log.debug(s"[DriveBuilt] Not all drives built, drives: $drives")}
        case None ⇒
          log.error(s"[DriveBuilt] Unknown drive: $sender")}
    //One drive started
    case Msg.DriveStarted ⇒
      drives.get(sender) match{
        case Some(drive) ⇒
          //Update state
          drives += (drive.actor → drive.withDriveState(DriveState.Started))
          //If all started, switch to work stepMode
          drives.values.exists(_.state != DriveState.Started) match{
            case false ⇒
              log.info(s"[DriveStarted] All drives started of sketch: $sketch, switch to work stepMode.")
              //Switch state
              state = State.Work
              drives ++= drives.values.map{ drive ⇒ (drive.actor, drive.withDriveState(DriveState.Work))}
              //Send ready msg
              controller ! Msg.PumpingStarted(stepMode)
            case true ⇒
              log.debug(s"[DriveBuilt] Not all drives built, drives: $drives")}
        case None ⇒
          log.error(s"[DriveBuilt] Unknown drive: $sender")}
      //Switch stepMode, set stepMode and send SetStepMode to all drives
    case Msg.SwitchStepMode(newStepMode) if state == State.Work ⇒
      stepMode = newStepMode
      workMode = WorkMode.Paused
      expectedTickMessage = None //Stop process cycle
      log.debug(s"[SwitchStepMode] Pumping step mode updated, update state of all drives")
      drives.values.foreach(_.actor ! Msg.SetStepMode(stepMode))
    //Step stepMode is set, update of particular actor and check if all set
    case Msg.StepModeIsSet(mode) ⇒ drives.get(sender) match{
      case Some(drive) ⇒
        drives += (drive.actor → drive.withDriveMode(mode))
        drives.values.exists(_.mode != stepMode) match{
          case false ⇒
            log.debug(s"[StepModeIsSet] All drives stepMode update to stepMode: $stepMode")
            controller ! Msg.StepModeSwitched(stepMode)
          case true ⇒
            log.debug(s"[StepModeIsSet] Not all drives stepMode update to stepMode: $stepMode")}
      case None ⇒
        log.error(s"[StepModeIsSet] Unknown drive: $sender")}
    //Updating of process cycle speed
    case Msg.SetSpeed(newSpeed) ⇒
      //Set timeout
      processTickTimeout = calcProcessTickTimeout(newSpeed)
      log.debug(s"[SetSpeed] New processTickTimeout: $processTickTimeout, workMode: $workMode")
      //Restart timer if runned
      expectedTickMessage match{
        case Some(ot) ⇒
          log.debug(s"[SetSpeed] Restarting of timer, old tick: $ot")
          expectedTickMessage = None
          startTick()
        case _ ⇒
          log.debug(s"[SetSpeed] Timer not started, do nothing.")}
    //Hit start UI button
    case Msg.HitStart if state == State.Work && workMode == WorkMode.Paused ⇒
      workMode = WorkMode.Runned
      stepMode match{
        case StepMode.HardSynchro ⇒
          log.debug(s"[HitStart] Set 'not done' for all drives and run first step.")
          //Run first step
          drives ++= drives.values.map(drive ⇒ drive.actor → drive.setIsDone(false))
          drives.values.foreach(_.actor ! Msg.DriveStep)
          //Send started to controller
          controller ! Msg.PumpingStarted
        case StepMode.SoftSynchro ⇒
          log.debug(s"[HitStart] Start process cycle.")
          startTick()
        case StepMode.Asynchro ⇒
          log.debug(s"[HitStart] Send DriveStart to all drives.")
          drives.values.foreach(_.actor ! Msg.DriveStart)}
    //Hit stop UI button
    case Msg.HitStop if state == State.Work && workMode == WorkMode.Runned ⇒
      expectedTickMessage = None
      stepMode match{
        case StepMode.HardSynchro ⇒
          log.debug(s"[HitStart] Stop process cycle, check if all drives done step.")
          drives.values.forall(_.isDone) match{
            case true ⇒
              log.debug(s"[HitStart] All drives done step, send PumpingStopped ")
              workMode = WorkMode.Paused
              controller ! Msg.PumpingStopped(stepMode)
            case false ⇒
              log.debug(s"[HitStart] Not all drives done step, wait for done.")
              workMode = WorkMode.Stopping}
        case StepMode.SoftSynchro ⇒
          log.debug(s"[HitStart] Send PumpingStopped ")
          workMode = WorkMode.Paused
          controller ! Msg.PumpingStopped(stepMode)
        case StepMode.Asynchro ⇒
          log.debug(s"[HitStart] Send DriveStart to all drives.")
          workMode = WorkMode.Stopping
          drives.values.foreach(_.actor ! Msg.DriveStop)}
    //Hit step UI button
    case Msg.HitStep if state == State.Work && workMode == WorkMode.Paused ⇒ stepMode match{
      case StepMode.HardSynchro ⇒ drives.values.forall(_.isDone) match{
        case true ⇒
          log.debug(s"[HitStep] Prev step done, reset isDone and run new step.")
          drives ++= drives.values.map(drive ⇒ drive.actor → drive.setIsDone(false))
          drives.values.foreach(_.actor ! Msg.DriveStep)
        case false ⇒
          log.debug(s"[HitStep]Prev step NOT done, do nothing.")}
      case StepMode.SoftSynchro ⇒
        log.debug(s"[HitStep] Do one step.")
        drives.values.foreach(_.actor ! Msg.DriveStep)
      case wm ⇒
        log.error(s"[HitStep] Incorrect work stepMode $wm.")}
    //Drive step done
    case Msg.DriveStepDone ⇒ drives.get(sender) match{
      case Some(drive) ⇒
        //Set is done
        log.debug(s"[DriveStepDone] Set 'is done' for the sender drive: $drive")
        drives += (drive.actor → drive.setIsDone(true))
        //If stepMode is HardSynchro and all drives done, start new tick
        (stepMode, drives.values.forall(_.isDone)) match{
          case (StepMode.HardSynchro, true) ⇒
            log.debug(s"[DriveStepDone] All drives process step.")
            //If Runned then start new task, else send PumpingStepDone
            workMode match{
              case WorkMode.Paused ⇒
                log.debug(s"[DriveStepDone] Send PumpingStepDone to controller in Paused mode.")
                controller ! Msg.PumpingStepDone
              case WorkMode.Runned ⇒
                log.debug(s"[DriveStepDone] Start next tick in Runned mode.")
                startTick()
              case WorkMode.Stopping ⇒
                log.debug(s"[DriveStepDone] All drives down, send PumpingStopped.")
                workMode = WorkMode.Paused
                controller ! Msg.PumpingStopped(stepMode)}
          case (sm, id) ⇒
           log.debug(s"[DriveStepDone] New tick not started, stepMode: $sm, allIsDone: $id.")}
      case None ⇒
        log.error(s"[DriveStepDone] Unknown drive: $sender")}
    //Process tick
    case tick: ProcessTick if workMode == WorkMode.Runned ⇒ expectedTickMessage.contains(tick) match{
      case true ⇒
        //Reset expected
        expectedTickMessage = None
        //Handling
        stepMode match{
          case StepMode.HardSynchro ⇒
            drives.values.forall(_.isDone) match{
              case true ⇒
                log.debug(s"[ProcessTick] Run next step in HardSynchro stepMode.")
                drives ++= drives.values.map(drive ⇒ drive.actor → drive.setIsDone(false))
                drives.values.foreach(_.actor ! Msg.DriveStep)
              case false ⇒
                log.error(s"[ProcessTick] All drives should be in 'done' state before new tick will started.")}
          case StepMode.SoftSynchro ⇒
            log.debug(s"[ProcessTick] Run next step in SoftSynchro stepMode, and start new tick.")
            drives.values.foreach(_.actor ! Msg.DriveStep)
            startTick()
          case StepMode.Asynchro ⇒
            log.debug(s"[ProcessTick] Do nothing in Asynchro stepMode.")
          case wm ⇒
            log.error(s"[ProcessTick] Incorrect work stepMode $wm.")}
      case false ⇒
        //Not expected tick
        log.debug(s"[ProcessTick] Not expected tick $tick, expected: $expectedTickMessage.")}
























    //!!! Далее здесь
    // 1) Завершение работы драйва (нормальное (по закрытии инструмента) и аварийно по ошибке инструмента или разущению драйва)
    //    В случае аварийного завершения драйва, остальным нужно разослать сообщение об этом чтобы они поудалали соединения с разрущеным драйвом.
    // 2) Завершение работы инициализированое контроллером (стандартное). Разослать всем драйвам сообщение о завершении,
    //    чтобы они выполениеи пользовательские функци завершения. Дождатся их разрущения (Terminated) и саморазрушится.







    //Time out on building of drive
    case DriveBuildingTimeout(drive) ⇒
      drives.get(drive) match{
        case Some(driveData) if driveData.state == DriveState.Building ⇒
          log.error(s"[DriveBuildingTimeout] Drive: $driveData, not built in $driveBuildingTimeout")

          //!!!Здесь (и в остальных таймаутах) нужно отключить все соединания и завершить работу инструмента (тем же способом как если бы
          // пользователь нажал кнопк "закрыть", но в логе скетча должна появится запись о неудачном запуске инструмента).

          ???

        case None ⇒
          log.error(s"[DriveBuildingTimeout] Unknown drive: $sender")
        case _ ⇒}
    //Time out on init of drive
    case DriveStartingTimeout(drive) ⇒
      drives.get(drive) match{
        case Some(driveData) if driveData.state == DriveState.Starting ⇒
          log.error(s"[DriveBuildingTimeout] Drive: $driveData, not started in $driveStartingTimeout")

          ???

        case None ⇒
          log.error(s"[DriveStartingTimeout] Unknown drive: $sender")
        case _ ⇒}
    //Time out on stopping of drive
    case DriveStoppingTimeout(drive) ⇒
      drives.get(drive) match{
        case Some(driveData) if driveData.state == DriveState.Stopping ⇒
          log.error(s"[DriveBuildingTimeout] Drive: $driveData, not stopped in $driveStoppingTimeout")

          ???

        case None ⇒
          log.error(s"[DriveStoppingTimeout] Unknown drive: $sender")
        case _ ⇒}




      //!!!Далее здесь
      // 1) Двух этапный, синхронный запуск всех зарегестрированых Drive (конструирование и инициализация)
      // 2) Посылка сообщения контроллеру о готовности
      // 3) Обработка завершения работы и ошибок.







//    case PlumbingInit(stepMode) ⇒
//      logMsgD("PlumbingInit", s"Init, stepMode: $stepMode, created drives: $drives", state)
//      state = State.Starting
//      controller = Some(sender)
//      //Init of pumps
//      drives.foreach{case (a, _)  ⇒ a ! Ready(stepMode)}
//      //Starting timer
//      context.system.scheduler.scheduleOnce(pumpStartingTimeout, self, StartTimeOut)
//    case Steady ⇒
//      logMsgD("Steady", s"Sender actor initialised", state)
//      //Set ready
//      drives.get(sender()).foreach(_.state = DriveState.Ready)
//      //If all ready, init is done
//      drives.values.exists(_.state != DriveState.Ready) match{
//        case false ⇒
//          logMsgD("Steady", s"All ready, change state to Work", state)
//          state = State.Work
//          controller.foreach(_ ! PlumbingStarted)
//        case true ⇒
//          logMsgD("Steady", s"Not all ready, drives: $drives", state)}




//    case StartTimeOut ⇒
//      logMsgD("StartTimeOut", s"Timeout: $pumpStartingTimeout", state)
//      state match{
//        case State.Creating | State.Starting ⇒
//          logMsgE("StartTimeOut", s"Pumping not started in: $pumpStartingTimeout", state)
//          //Send error to controller
//          controller.foreach(_ ! Msg.FatalError(s"The system not ready in $pumpStartingTimeout"))
//        case _ ⇒}
//    case Terminated(actor) ⇒
//      logMsgD("Terminated", s"Terminated actor: $actor", state)
//      //Check if in list
//      drives.contains(actor) match{
//        case true ⇒
//          logMsgE("Terminated", s"Actor suddenly terminated: $actor", state)
//          //Disconnect connections
//
//          //TODO
//
//          //Remove from list
//          drives -= actor
//        case _ ⇒}
  }}
