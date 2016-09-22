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

package mathact.parts.control.infrastructure

import java.util.concurrent.{TimeoutException, ExecutionException}

import akka.actor.PoisonPill
import mathact.parts.WorkbenchLike
import mathact.parts.bricks.WorkbenchContext
import mathact.parts.model.enums.{ActorState, SketchUIElement, SketchUiElemState}
import mathact.parts.model.messages.M

import scala.concurrent.Future
import scalafx.scene.paint.Color


/** WorkbenchController sketch building
  * Created by CAB on 04.09.2016.
  */

trait WorkbenchControllerLife { _: WorkbenchController ⇒
  import SketchUiElemState._, SketchUIElement._
  //Variables
  private var isWorkbenchContextBuilt = false
  //Functions
  private def buildingError(error: Throwable): Unit = {  //Called only until plumbing run
    //Build message
    val msg = error match{
      case err: NoSuchMethodException ⇒ s"NoSuchMethodException, check if sketch class is not inner."
      case err ⇒ s"Exception on building of sketch."}
    //Log to user logging
    userLogging ! M.LogError(None, "Workbench", Some(error), msg)
    //Update UI
    sketchUi !  M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      ShowAllToolsUiBtn → ElemDisabled,
      HideAllToolsUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled))
    //Inform MainController
    mainController ! M.SketchError(sketchData.className, error)
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Building error! Check logs.", Color.Red)}
  //Methods
  /** Start workbench controller */
  def startWorkbenchController(): Unit = {
    log.debug(s"[WorkbenchControllerLife.startWorkbenchController] Start creating.")
    sketchUi ! M.SetSketchUIStatusString("Creating...", Color.Black)}
  /** Sketch run building, called after all UI shown */
  def sketchRunBuilding(): Unit = {
    log.debug(
      s"[WorkbenchControllerLife.sketchRunBuilding] Try to create Workbench instance, " +
      s"sketchBuildingTimeout: ${config.sketchBuildingTimeout}")
    //Run building timeout
    context.system.scheduler.scheduleOnce(
      config.sketchBuildingTimeout,
      self,
      SketchBuiltTimeout)
    //Build sketch
    Future{sketchData.clazz.newInstance()}
      .map{ s ⇒ self ! SketchBuilt(s.asInstanceOf[WorkbenchLike])}
      .recover{
        case t: ExecutionException ⇒ self ! SketchBuiltError(t.getCause)
        case t: Throwable ⇒ self ! SketchBuiltError(t)}
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Building...", Color.Black)}
  /** Get workbench context, create and return of WorkbenchContext
    * @return - Either[Exception, WorkbenchContext] */
  def getWorkbenchContext: Either[Exception, WorkbenchContext] = isWorkbenchContextBuilt match{
    case false ⇒
      log.debug(s"[WorkbenchControllerLife.getWorkbenchContext] Build WorkbenchContext")
      val response = Right{ new WorkbenchContext(
        context.system,
        mainController,
        pumping,
        config.pumping.pump,
        config.config)}
      isWorkbenchContextBuilt = true
      response
    case true⇒
      val err = new IllegalStateException(s"[WorkbenchControllerLife.getWorkbenchContext] Context already created.")
      log.error(err, s"[WorkbenchControllerLife.getWorkbenchContext] Error on creating.")
      Left(err)}
  /** Sketch successfully built
    * @param workbench - WorkbenchLike */
  def sketchBuilt(workbench: WorkbenchLike): Unit = {
    //Check if WorkbenchContext built
    isWorkbenchContextBuilt match{
      case true ⇒
        log.debug(s"[WorkbenchControllerLife.sketchBuilt] workbench: $workbench")
        //User log
        val autorunMsg = sketchData.autorun match{
          case false ⇒ ". Auto-run is off, hit 'play' button to start sketch."
          case true ⇒ "."}
        userLogging ! M.LogInfo(None, "Workbench", s"Sketch '$sketchName' successfully built$autorunMsg")
        //Update UI
        sketchUi !  M.UpdateSketchUIState(Map(RunBtn → (if(sketchData.autorun) ElemDisabled else ElemEnabled)))
        //Send started to main controller
        mainController ! M.SketchBuilt(sketchData.className, workbench)
        //Update status string
        sketchUi ! M.SetSketchUIStatusString(
          s"Sketch built. ${if(sketchData.autorun) "" else "Wait for start."}",
          if(sketchData.autorun) Color.Black else Color.Green)
      case false ⇒
        log.error(s"[WorkbenchControllerLife.sketchBuilt] Building failed, WorkbenchContext is not built.")
        buildingError(new IllegalStateException(
          "[WorkbenchControllerLife.sketchBuilt] WorkbenchContext is not built."))}}
  /** Error during sketch building
    * @param error - Throwable */
  def sketchBuiltError(error: Throwable): Unit = {
    log.error(
      error,
      s"[WorkbenchControllerLife.sketchBuildingError] Error on creating Sketch extends Workbench instance.")
     buildingError(error)}
  /** Sketch not build in required time.
    * @param state - ActorState */
  def sketchBuiltTimeout(state: ActorState): Unit = state match{
    case ActorState.Building ⇒
      log.error(
        s"[WorkbenchControllerLife.sketchBuiltTimeout] Building failed, sketch not built " +
          s"in ${config.sketchBuildingTimeout}.")
      buildingError(new TimeoutException(
        s"[WorkbenchControllerLife.sketchBuiltTimeout] Sketch not built in ${config.sketchBuildingTimeout}"))
    case st ⇒
      log.debug(s"[WorkbenchControllerLife.sketchBuiltTimeout] Not a Building state do nothing, state: $st")}
  /** Start pumping */
  def startPumping(): Unit = {
    log.debug(s"[WorkbenchControllerLife.startPumping] Send StartPumping")
    pumping ! M.StartPumping
    sketchUi ! M.SetSketchUIStatusString("Starting of pumping...", Color.Black)}
  /** Pumping started, update UI and log to user log */
  def pumpingStarted(): Unit = {
    log.debug(s"[WorkbenchControllerLife.pumpingStarted] Started.")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      StopSketchBtn → ElemEnabled,
      ShowAllToolsUiBtn → ElemEnabled,
      HideAllToolsUiBtn → ElemEnabled,
      SkipAllTimeoutTaskBtn → ElemEnabled))
    //User log
    userLogging ! M.LogInfo(None, "Workbench", s"Pumping started.")
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Pumping started. Working...", Color.Green)}
  /** Try to stop Pumping, send StopPumping */
  def stopPumping(): Unit = {
    log.debug(s"[WorkbenchControllerLife.stopPumping] Try to stop Pumping.")
    pumping ! M.StopPumping
    sketchUi ! M.SetSketchUIStatusString("Stopping of pumping...", Color.Black)}
  /** Pumping stopped, log to user logger */
  def pumpingStopped(): Unit = {
    log.debug(s"[WorkbenchControllerLife.pumpingStopped] Stopped.")
    //Log to user log
    userLogging ! M.LogInfo(None, "Workbench", s"Pumping stopped.")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      ShowAllToolsUiBtn → ElemDisabled,
      HideAllToolsUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled))
    //Update status string
    sketchUi ! M.SetSketchUIStatusString("Pumping stopped.", Color.Black)}
  /** Sketch built, but SketchBuiltTimeout received earlier */
  def lateSketchBuilt(): Unit = {
    log.debug(
      s"[Building] SketchBuilt receive but state BuildingFailed (probably SketchBuiltTimeout received earlier).")}
  /** Starting of destruct sketch */
  def destructSketch(): Unit = {
    log.debug(s"[WorkbenchControllerLife.destructSketch] Starting of destruct sketch.")
    mainController ! M.SketchDone(sketchData.className)
    self ! SketchDestructed
    sketchUi ! M.SetSketchUIStatusString("Destructing...", Color.Black)}
  /** Shutdown workbench controller */
  def shutdownWorkbenchController(): Unit = {
    log.debug(s"[WorkbenchControllerLife.shutdownWorkbenchController] Shutdown.")
    userLogging ! M.LogInfo(None, "Workbench", "The Shutdown signal received, sketch will terminated.")
    sketchUi ! M.SetSketchUIStatusString("Shutdown signal received.", Color.Black)}
  /** Terminate self */
  def terminateSelf(): Unit = {
    log.debug(s"[WorkbenchControllerLife.terminateSelf] Send WorkbenchControllerTerminated and terminate.")
    mainController ! M.WorkbenchControllerTerminated
    self ! PoisonPill}}
