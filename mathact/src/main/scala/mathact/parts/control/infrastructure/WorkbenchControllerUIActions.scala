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

/** WorkbenchController UI actions processing
  * Created by CAB on 07.09.2016.
  */

trait WorkbenchControllerUIActions { _: WorkbenchController ⇒
  import SketchUiElemState._, SketchUIElement._
  //Methods
  /** Hit run button */
  def hitRunBtn() = {
    log.debug(s"[WorkbenchControllerUIActions.hitRunBtn] Try to run plumbing.")
    //Update UI
    sketchUi !  M.UpdateSketchUIState(Map(RunBtn → ElemDisabled))
    //Send StartPumping
    pumping ! M.StartPumping}
  /** Show all tools UI btn hit */
  def showAllToolsUiBtnHit(): Unit = {
    log.debug(s"[WorkbenchControllerUIActions.showAllToolsUiBtn] Send ShowAllToolUi.")
    pumping ! M.ShowAllToolUi}
  /** Hide all tools UI btn hit */
  def hideAllToolsUiBtnHit(): Unit = {
    log.debug(s"[WorkbenchControllerUIActions.hideAllToolsUiBtn] Send HideAllToolUi.")
    pumping ! M.HideAllToolUi}
  /** Skip all timeout task btn hit */
  def skipAllTimeoutTaskBtnHit(): Unit = {
    log.debug(s"[WorkbenchControllerUIActions.skipAllTimeoutTaskBtn] Send SkipAllTimeoutTask.")
    pumping ! M.SkipAllTimeoutTask}
  /** Change user logging state
    * @param act - SketchUiElemState */
  def logBtnHit(act: SketchUiElemState): Unit = {
    log.debug(s"[WorkbenchControllerUIActions.logBtnHit] act: $act ")
    act match{
      case SketchUiElemState.ElemShow ⇒
        userLogging ! M.ShowUserLoggingUI
      case SketchUiElemState.ElemHide ⇒
        userLogging ! M.HideUserLoggingUI}}
  /** Change visualisation
    * @param act - SketchUiElemState */
  def visualisationBtnHit(act: SketchUiElemState): Unit = {
    log.debug(s"[WorkbenchControllerUIActions.logBtnHit] act: $act ")
    act match{
      case SketchUiElemState.ElemShow ⇒
        visualization ! M.ShowVisualizationUI
      case SketchUiElemState.ElemHide ⇒
        visualization ! M.HideVisualizationUI}}}
