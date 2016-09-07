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

import mathact.parts.WorkbenchLike
import mathact.parts.bricks.WorkbenchContext
import mathact.parts.model.enums.{ActorState, SketchUIElement, SketchUiElemState}
import mathact.parts.model.messages.M

import scala.concurrent.Future


/** WorkbenchController sketch building
  * Created by CAB on 04.09.2016.
  */

trait WorkbenchControllerBuilding { _: WorkbenchController ⇒

  import SketchUiElemState._, SketchUIElement._


  private var isWorkbenchContextBuilt = false


  //Functions
  private def buildingError(error: Throwable): Unit = {
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


    def sketchBuiltTimeout(state: ActorState): Unit = state match{
      case ActorState.Building ⇒
        log.error(
          s"[WorkbenchControllerBuilding.sketchBuiltTimeout] Building failed, sketch not built " +
          s"in ${config.sketchBuildingTimeout}.")
        buildingError(new TimeoutException(
          s"[WorkbenchControllerBuilding.sketchBuiltTimeout] Sketch not built in ${config.sketchBuildingTimeout}"))
      case st ⇒
        log.debug(s"[WorkbenchControllerBuilding.sketchBuiltTimeout] Not a Building state do nothing, state: $st")}


  def sketchBuiltError(error: Throwable): Unit = {
    log.error(
      error,
      s"[WorkbenchControllerBuilding.sketchBuildingError] Error on creating Workbench instance.")
     buildingError(error)}



  def pumpingStarted(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.pumpingStarted] Started.")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      StopSketchBtn → ElemEnabled,
      ShowAllToolsUiBtn → ElemEnabled,
      HideAllToolsUiBtn → ElemEnabled,
      SkipAllTimeoutTaskBtn → ElemEnabled))
    //User log
    userLogging ! M.LogInfo(None, "Workbench", s"Pumping started.")}





}
