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

import mathact.parts.model.data.sketch.SketchUIState
import mathact.parts.model.messages.M


/** WorkbenchController UI control
  * Created by CAB on 04.09.2016.
  */

trait WorkbenchControllerUIControl { _: WorkbenchController ⇒



  private var sketchUiState: Option[SketchUIState] = None  //None - UI is hide
  private var isUserLogShowed = false
  private var isVisualisationShowed = false




  def showAllUi(): Unit = {
    //Sketch UI
    sketchUi ! M.SetSketchUIState(SketchUIState(
      isUiShown = true,
      runBtnEnable = ! sketch.autorun,
      showToolUiBtnEnable = true,
      hideToolUiBtnEnable = true,
      skipAllTimeoutProcBtnEnable = true,
      stopBtnEnable = false,
      logUiBtnEnable = true,
      logUiBtnIsShow = sketch.showUserLogUi,
      visualisationUiBtnEnable = true,
      visualisationUiBtnIsShow = sketch.showVisualisationUi))
    //User logging
    sketch.showUserLogUi match{
      case true ⇒ userLogging ! M.ShowUserLoggingUI
      case false ⇒ log.debug("[WorkbenchControllerUIControl.showAllUi] User Logging UI will hided.")}
    //Visualisation
    sketch.showVisualisationUi match{
      case true ⇒ visualization ! M.ShowVisualizationUI
      case false ⇒ log.debug("[WorkbenchControllerUIControl.showAllUi] Visualization UI will hided..")}}


  def isAllUiShowed: Boolean = {
    sketchUiState.nonEmpty && sketchUiState.get.isUiShown &&
      (isUserLogShowed || (! sketch.showUserLogUi)) &&
      (isVisualisationShowed || (! sketch.showVisualisationUi))}


  def sketchUiShowed(state: SketchUIState): Unit = { sketchUiState = Some(state) }


  def sketchUiHided(state: SketchUIState): Unit = { sketchUiState = Some(state) }


  def userLoggingUIShowed(): Unit = { isUserLogShowed = true }


  def  userLoggingUIHided(): Unit = { isUserLogShowed = false }


  def visualizationUIShowed(): Unit = { isVisualisationShowed = true }


  def  visualizationUIHided(): Unit = { isVisualisationShowed = false }




}
