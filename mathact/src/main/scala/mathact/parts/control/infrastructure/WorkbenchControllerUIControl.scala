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

import mathact.parts.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.parts.model.messages.M


/** WorkbenchController UI control
  * Created by CAB on 04.09.2016.
  */

trait WorkbenchControllerUIControl { _: WorkbenchController ⇒
  import SketchUiElemState._, SketchUIElement._
  //Variables
  private var isSketchUiShowed = false
  private var isUserLogShowed = false
  private var isVisualisationShowed = false
  //Methods
  /** Show all UI, and log to user logging */
  def showAllUi(): Unit = {
    log.debug("[WorkbenchControllerUIControl.showAllUi] Try to show.")
    //Sketch UI
    sketchUi ! M.ShowSketchUI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      ShowAllToolsUiBtn → ElemDisabled,
      HideAllToolsUiBtn → ElemDisabled,
      SkipAllTimeoutTaskBtn → ElemDisabled,
      StopSketchBtn → ElemDisabled,
      LogBtn → (if(sketchData.showUserLogUi) ElemShow else ElemHide),
      VisualisationBtn → (if(sketchData.showVisualisationUi) ElemShow else ElemHide)))
    //User logging
    sketchData.showUserLogUi match{
      case true ⇒ userLogging ! M.ShowUserLoggingUI
      case false ⇒ log.debug("[WorkbenchControllerUIControl.showAllUi] User Logging UI stay hided.")}
    //Visualisation
    sketchData.showVisualisationUi match{
      case true ⇒ visualization ! M.ShowVisualizationUI
      case false ⇒ log.debug("[WorkbenchControllerUIControl.showAllUi] Visualization UI stay hided.")}}
  /** Check if all UI shown
    * @return - true if all shown */
  def isAllUiShowed: Boolean = {
    val res = isSketchUiShowed &&
      (isUserLogShowed || (! sketchData.showUserLogUi)) &&
      (isVisualisationShowed || (! sketchData.showVisualisationUi))
    log.debug(s"[WorkbenchControllerUIControl.isAllUiShowed] res: $res.")
    res}
  /** Sketch UI changed
    * @param isShow - true if shown */
  def sketchUiChanged(isShow: Boolean): Unit = {
    log.debug(s"[WorkbenchControllerUIControl.sketchUiChanged] isShow: $isShow")
    isSketchUiShowed = isShow }
  /** User logging UI changed
    * @param isShow - true if shown */
  def userLoggingUIChanged(isShow: Boolean): Unit = {
    log.debug(s"[WorkbenchControllerUIControl.userLoggingUIChanged] isShow: $isShow")
    isUserLogShowed = isShow
    sketchUi ! M.UpdateSketchUIState(Map(LogBtn → (if(isShow) ElemShow else ElemHide)))}
  /** Visualization UI changed
    * @param isShow - true if shown */
  def visualizationUIChanged(isShow: Boolean): Unit = {
    log.debug(s"[WorkbenchControllerUIControl.visualizationUIChanged] isShow: $isShow")
    isVisualisationShowed = isShow
    sketchUi ! M.UpdateSketchUIState(Map(VisualisationBtn → (if(isShow) ElemShow else ElemHide)))}}
