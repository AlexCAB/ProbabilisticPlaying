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

import java.util.UUID

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import mathact.parts.{IdGenerator, StateActorBase}
import mathact.parts.data._
import mathact.parts.plumbing.PumpLike
import collection.mutable.{Map ⇒ MutMap}


/** Supervisor for all Pumps
  * Created by CAB on 15.05.2016.
  */

private [mathact] class Pumping(
  controller: ActorRef,
  sketchName: String,
  userLogging: ActorRef,
  visualization: ActorRef)
extends StateActorBase(ActorState.Building) with IdGenerator{ import ActorState._
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){case _: Throwable ⇒ Resume}
  //Definitions
  case class DriveData(
    drive: ActorRef,
    toolId: Int,
    var builtInfo: Option[ToolBuiltInfo] = None,
    var driveState: ActorState = Building)
  //Variables
  val drives = MutMap[ActorRef, DriveData]()
  //Functions
  def createDriveActor(toolPump: PumpLike): (ActorRef, Int) = {
    val toolId = nextIntId
    val drive = context.actorOf(
      Props(new Drive(toolId, toolPump, self, userLogging, visualization)),
      "DriveOf_" + toolPump.toolName + "_" + UUID.randomUUID)
    context.watch(drive)
    (drive, toolId)}
  def newDrive(toolPump: PumpLike, state: ActorState, actor: PumpLike⇒(ActorRef, Int))
  :Unit = state match{
    case Building ⇒
      //New drive
      val (drive, toolId) = createDriveActor(toolPump)
      log.debug(s"[newDrive] New drive created, toolName: ${toolPump.toolName}, drive: $drive")
      drives += (drive → DriveData(drive, toolId))
      //Response
      sender ! Right(drive)
    case s ⇒
      //Incorrect state
      val msg = s"[newDrive] Creating of drive not in Building state step, toolName: ${toolPump.toolName}, state: $s"
      log.error(msg)
      sender ! Left(new Exception(msg))}
  def setSenderDriveState(state: ActorState): Unit = drives.get(sender) match{
    case Some(driveData) ⇒
      log.debug(s"[setSenderDriveState] Set $state state for drive $sender.")
      driveData.driveState = state
    case None ⇒
      log.error(s"[setSenderDriveState] Unknown drive $sender, registered drives: ${drives.values}.")}
  def callIfAllDrivesInState(state: ActorState)(proc: ⇒ Unit): Unit = drives
    .values.exists(_.driveState != state) match{
      case false ⇒
        log.debug(s"[callIfAllDrivesInState] All drives in $state state, run proc.")
        proc
      case true ⇒
        log.debug(s"[callIfAllDrivesInState] Not all drives in $state state, drives: ${drives.values}")}
  def setAndSendToDrives(state: ActorState, msg: Msg): Unit = drives.values.foreach{ driveData ⇒
    log.debug(s"[setAndSendToDrives] Set $state state and send $msg to drive: $driveData")
    driveData.driveState = state
    driveData.drive ! msg}
  def allDrivesStarted(): Unit = {
    log.debug(s"[allDrivesStarted] All drives started, send Msg.PumpingStarted, drives: ${drives.values}.")
    drives.values.foreach(_.driveState = Working)
    controller ! Msg.PumpingStarted}
  def allDrivesTerminated(): Unit = {
    log.debug(s"[allDrivesTerminated] All drives started, send Msg.PumpingStarted, drives: ${drives.values}.")
    drives.values.foreach(_.driveState = Terminated)
    controller ! Msg.PumpingStopped
    self ! PoisonPill}
  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[(StateMsg, ActorState), Unit] = {
    //Switch to Starting, send BuildDrive to all drives
    case (Msg.StartPumping, Building) ⇒
      state = Building
      setAndSendToDrives(Building,  Msg.BuildDrive)
    //Switch to Stopping, send StopDrive to all drives
    case (Msg.StopPumping, Working) ⇒
      state = Stopping
      setAndSendToDrives(Stopping,  Msg.StopDrive)}
  /** Handling after reaction executed */
  def postHandling: PartialFunction[(Msg, ActorState), Unit] = {
    //Check if all drive built, if so send StartDrive to all drives
    case (Msg.DriveBuilt, Building) ⇒ callIfAllDrivesInState(Built){
      state = Starting
      setAndSendToDrives(Starting,  Msg.StartDrive)}
    //Check if all drive started, if so switch to Working and send PumpingStarted
    case (Msg.StartPumping | Msg.DriveStarted, Starting) ⇒ callIfAllDrivesInState(Started){
      state = Working
      allDrivesStarted()}
    //Check if all drive stopped, if so send TerminateDrive to all drives
    case (Msg.DriveStopped, Stopping) ⇒ callIfAllDrivesInState(Stopped){
      state = Terminating
      setAndSendToDrives(Stopped,  Msg.TerminateDrive)}
    //Check if all drive terminate, if so switch to Terminating, send PumpingStopped and terminating
    case (Msg.StopPumping | Msg.DriveTerminated, Terminating) ⇒ callIfAllDrivesInState(Terminated){
      state = Terminated
      allDrivesTerminated()}}
  /** Actor reaction on messages */
  def reaction: PartialFunction[(Msg, ActorState), Unit] = {
    //Creating of new drive for tool (ask request)
    case (Msg.NewDrive(toolPump), state) ⇒ newDrive(toolPump, state, createDriveActor)
    //Updates of driveState
    case (Msg.DriveBuilt, Building) ⇒ setSenderDriveState(Built)
    case (Msg.DriveStarted, Starting) ⇒ setSenderDriveState(Started)
    case (Msg.DriveStopped, Stopping) ⇒ setSenderDriveState(Stopped)
    case (Msg.DriveTerminated, Terminating) ⇒ setSenderDriveState(Terminated)
    //Re send SkipAllTimeoutTask to all drives
    case (Msg.SkipAllTimeoutTask, _) ⇒ drives.values.foreach(_.drive ! Msg.SkipTimeoutTask)
    case (Msg.ShowAllToolUi, _) ⇒ drives.values.foreach(_.drive ! Msg.ShowToolUi)
    case (Msg.HideAllToolUi, _) ⇒ drives.values.foreach(_.drive ! Msg.HideToolUi)}}
