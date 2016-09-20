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


/** WorkbenchController sketch building
  * Created by CAB on 04.09.2016.
  */

trait WorkbenchControllerLife { _: WorkbenchController ⇒
  import SketchUiElemState._, SketchUIElement._
  //Variables
  private var isWorkbenchContextBuilt = false
  private var isSketchUiTerminated = false
  private var isUserLogTerminated = false
  private var isVisualisationTerminated = false
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
    mainController ! M.SketchError(sketchData.className, error)}
  //Methods
  /** Sketch run building, called after all UI shown */
  def sketchRunBuilding(): Unit = {
    log.debug(
      s"[WorkbenchControllerBuilding.sketchRunBuilding] Try to create Workbench instance, " +
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
        case t: Throwable ⇒ self ! SketchBuiltError(t)}}
  /** Get workbench context, create and return of WorkbenchContext
    * @return - Either[Exception, WorkbenchContext] */
  def getWorkbenchContext: Either[Exception, WorkbenchContext] = isWorkbenchContextBuilt match{
    case false ⇒
      log.debug(s"[WorkbenchControllerBuilding.getWorkbenchContext] Build WorkbenchContext")
      val response = Right{ new WorkbenchContext(
        context.system,
        mainController,
        pumping,
        config.pumping.pump,
        config.config)}
      isWorkbenchContextBuilt = true
      response
    case true⇒
      val err = new IllegalStateException(s"[WorkbenchControllerBuilding.getWorkbenchContext] Context already created.")
      log.error(err, s"[WorkbenchControllerBuilding.getWorkbenchContext] Error on creating.")
      Left(err)}
  /** Sketch successfully built
    * @param workbench - WorkbenchLike */
  def sketchBuilt(workbench: WorkbenchLike): Unit = {
    //Check if WorkbenchContext built
    isWorkbenchContextBuilt match{
      case true ⇒
        log.debug(s"[WorkbenchControllerBuilding.sketchBuilt] workbench: $workbench")
        //User log
        val autorunMsg = sketchData.autorun match{
          case false ⇒ ". Auto-run is off, hit 'play' button to start sketch."
          case true ⇒ "."}
        userLogging ! M.LogInfo(None, "Workbench", s"Sketch '$sketchName' successfully built$autorunMsg")
        //Run plumping if auto run
        sketchData.autorun match{
          case true ⇒
            log.debug(s"[WorkbenchControllerBuilding.sketchBuilt] Autorun in on, try to start plumbing.")
            pumping ! M.StartPumping
          case false ⇒
            log.debug(s"[WorkbenchControllerBuilding.sketchBuilt] No autorun.")}
        //Update UI
        sketchUi !  M.UpdateSketchUIState(Map(RunBtn → (if(sketchData.autorun) ElemDisabled else ElemEnabled)))
        //Send started to main controller
        mainController ! M.SketchBuilt(sketchData.className, workbench)
      case false ⇒
        log.error(s"[WorkbenchControllerBuilding.sketchBuilt] Building failed, WorkbenchContext is not built.")
        buildingError(new IllegalStateException(
          "[WorkbenchControllerBuilding.sketchBuilt] WorkbenchContext is not built."))}}
  /** Error during sketch building
    * @param error - Throwable */
  def sketchBuiltError(error: Throwable): Unit = {
    log.error(
      error,
      s"[WorkbenchControllerBuilding.sketchBuildingError] Error on creating Sketch extends Workbench instance.")
     buildingError(error)}
  /** Sketch not build in required time.
    * @param state - ActorState */
  def sketchBuiltTimeout(state: ActorState): Unit = state match{
    case ActorState.Building ⇒
      log.error(
        s"[WorkbenchControllerBuilding.sketchBuiltTimeout] Building failed, sketch not built " +
          s"in ${config.sketchBuildingTimeout}.")
      buildingError(new TimeoutException(
        s"[WorkbenchControllerBuilding.sketchBuiltTimeout] Sketch not built in ${config.sketchBuildingTimeout}"))
    case st ⇒
      log.debug(s"[WorkbenchControllerBuilding.sketchBuiltTimeout] Not a Building state do nothing, state: $st")}
  /** Pumping started, update UI and log to user log */
  def pumpingStarted(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.pumpingStarted] Started.")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      StopSketchBtn → ElemEnabled,
      ShowAllToolsUiBtn → ElemEnabled,
      HideAllToolsUiBtn → ElemEnabled,
      SkipAllTimeoutTaskBtn → ElemEnabled))
    //User log
    userLogging ! M.LogInfo(None, "Workbench", s"Pumping started.")}
  /** Try to stop Pumping, send StopPumping */
  def stopPumping(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.stopPumping] Try to stop Pumping.")
    pumping ! M.StopPumping}
  /** Pumping stopped, log to user logger */
  def pumpingStopped(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.pumpingStopped] Stopped.")
    //Log to user log
    userLogging ! M.LogInfo(None, "Workbench", s"Pumping stopped.")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      ShowAllToolsUiBtn → ElemDisabled,
      HideAllToolsUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled))}
  /** Sketch built, but SketchBuiltTimeout received earlier */
  def lateSketchBuilt(): Unit = {
    log.debug(
      s"[Building] SketchBuilt receive but state BuildingFailed (probably SketchBuiltTimeout received earlier).")}
  /** Starting of destruct sketch */
  def destructSketch(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.destructSketch] Starting of destruct sketch.")
    mainController ! M.SketchDone(sketchData.className)
    self ! SketchDestructed}
  /** Shutdown workbench controller */
  def shutdownWorkbenchController(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.shutdownWorkbenchController] Shutdown.")
    userLogging ! M.LogInfo(None, "Workbench", "The Shutdown signal received, sketch will terminated.")}
  /** Terminate all UI and */
  def terminateAllUi(): Unit = {
    log.debug("[WorkbenchControllerUIControl.terminateAllUi] Send Terminate... messages to all UI.")
    //Set all disable
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      ShowAllToolsUiBtn → ElemDisabled,
      HideAllToolsUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled,
      LogBtn → ElemDisabled,
      VisualisationBtn → ElemDisabled))
    //Terminate
    visualization ! M.TerminateVisualization
    userLogging ! M.TerminateUserLogging
    sketchUi ! M.TerminateSketchUI}
  /** Sketch UI terminated */
  def sketchUITerminated(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.sketchUITerminated] Terminated.")
    isSketchUiTerminated = true}
  /** User logging terminated */
  def userLoggingTerminated(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.userLoggingTerminated] Terminated.")
    isUserLogTerminated = true}
  /** Visualization terminated */
  def visualizationTerminated(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.visualizationTerminated] Terminated.")
    isVisualisationTerminated = true}
  /** Check if all UI terminated
    * @return - true if all terminated */
  def isAllUiTerminated: Boolean = {
    val res = isSketchUiTerminated && isUserLogTerminated && isVisualisationTerminated
    log.debug(s"[WorkbenchControllerBuilding.isAllUiTerminated] res: $res.")
    res}
  /** Terminate self */
  def terminateSelf(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.terminateSelf] Send WorkbenchControllerTerminated and terminate.")
    mainController ! M.WorkbenchControllerTerminated
    self ! PoisonPill}}
