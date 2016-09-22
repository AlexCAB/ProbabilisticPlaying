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

package mathact.parts.control.ui

import akka.actor.Props
import akka.testkit.TestProbe
import mathact.parts.UIActorTestSpec
import mathact.parts.model.config.SketchUIConfigLike
import mathact.parts.model.enums.{SketchUiElemState, SketchUIElement}
import mathact.parts.model.messages.M
import org.scalatest.Suite

import scala.concurrent.duration._
import scalafx.scene.image.Image
import scalafx.scene.paint.Color


/** Testing of SketchUI actor
  * Created by CAB on 21.09.2016.
  */

class SketchUITest extends UIActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    val sketchUIConfig = new SketchUIConfigLike{
      val buttonsSize = 30
      val runBtnD             = new Image("run_btn_d.png", buttonsSize, buttonsSize, true, true)
      val runBtnE             = new Image("run_btn_e.png", buttonsSize, buttonsSize, true, true)
      val showAllToolsUiD     = new Image("show_all_tools_ui_d.png", buttonsSize, buttonsSize, true, true)
      val showAllToolsUiE     = new Image("show_all_tools_ui_e.png", buttonsSize, buttonsSize, true, true)
      val hideAllToolsUiBtnD  = new Image("hide_all_tools_ui_btn_d.png", buttonsSize, buttonsSize, true, true)
      val hideAllToolsUiBtnE  = new Image("hide_all_tools_ui_btn_e.png", buttonsSize, buttonsSize, true, true)
      val skipAllTimeoutTaskD = new Image("skip_all_timeout_task_d.png", buttonsSize, buttonsSize, true, true)
      val skipAllTimeoutTaskE = new Image("skip_all_timeout_task_e.png", buttonsSize, buttonsSize, true, true)
      val stopSketchBtnD      = new Image("stop_sketch_btn_d.png", buttonsSize, buttonsSize, true, true)
      val stopSketchBtnE      = new Image("stop_sketch_btn_e.png", buttonsSize, buttonsSize, true, true)
      val logBtnD             = new Image("log_btn_d.png", buttonsSize, buttonsSize, true, true)
      val logBtnS             = new Image("log_btn_s.png", buttonsSize, buttonsSize, true, true)
      val logBtnH             = new Image("log_btn_h.png", buttonsSize, buttonsSize, true, true)
      val visualisationBtnD   = new Image("visualisation_btn_d.png", buttonsSize, buttonsSize, true, true)
      val visualisationBtnS   = new Image("visualisation_btn_s.png", buttonsSize, buttonsSize, true, true)
      val visualisationBtnH   = new Image("visualisation_btn_h.png", buttonsSize, buttonsSize, true, true)}
    //Helpers actors
    val workbenchController = TestProbe("TestWorkbenchController_" + randomString())
    //UI Actor
    val ui = system.actorOf(Props(new SketchUI(sketchUIConfig, workbenchController.ref)), "SketchUI_" + randomString())
    workbenchController.watch(ui)}
  //Testing
  "SketchUI on start" should{
    "change UI view" in new TestCase {
      //Preparing
      import SketchUIElement._, SketchUiElemState._
      //Show UI
      workbenchController.send(ui, M.ShowSketchUI)
      workbenchController.expectMsgType[M.SketchUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Buttons test
      workbenchController.send(ui, M.SetSketchUIStatusString("Do hit active button...", Color.Red))
      //Buttons test: LogBtn show
      workbenchController.send(ui, M.UpdateSketchUIState(Map(LogBtn → ElemShow)))
      val logActS = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      logActS.element shouldEqual LogBtn
      logActS.action shouldEqual ElemShow
      //Buttons test: LogBtn hide
      workbenchController.send(ui, M.UpdateSketchUIState(Map(LogBtn → ElemHide)))
      val logActH = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      logActH.element shouldEqual LogBtn
      logActH.action shouldEqual ElemHide
      //Buttons test: VisualisationBtn show
      workbenchController.send(ui, M.UpdateSketchUIState(Map(VisualisationBtn → ElemShow)))
      val visualisationBtnS = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      visualisationBtnS.element shouldEqual VisualisationBtn
      visualisationBtnS.action shouldEqual ElemShow
      //Buttons test: VisualisationBtn hide
      workbenchController.send(ui, M.UpdateSketchUIState(Map(VisualisationBtn → ElemHide)))
      val visualisationBtnH = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      visualisationBtnH.element shouldEqual VisualisationBtn
      visualisationBtnH.action shouldEqual ElemHide
      //Buttons test: RunBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(RunBtn → ElemEnabled)))
      val runBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      runBtn.element shouldEqual RunBtn
      runBtn.action shouldEqual ElemEnabled
      //Buttons test: ShowAllToolsUiBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(ShowAllToolsUiBtn → ElemEnabled)))
      val showAllToolsUiBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      showAllToolsUiBtn.element shouldEqual ShowAllToolsUiBtn
      showAllToolsUiBtn.action shouldEqual ElemEnabled
      //Buttons test: HideAllToolsUiBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(HideAllToolsUiBtn → ElemEnabled)))
      val hideAllToolsUiBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      hideAllToolsUiBtn.element shouldEqual HideAllToolsUiBtn
      hideAllToolsUiBtn.action shouldEqual ElemEnabled
      //Buttons test: SkipAllTimeoutTaskBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(SkipAllTimeoutTaskBtn → ElemEnabled)))
      val skipAllTimeoutTaskBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      skipAllTimeoutTaskBtn.element shouldEqual SkipAllTimeoutTaskBtn
      skipAllTimeoutTaskBtn.action shouldEqual ElemEnabled
      //Buttons test: StopSketchBtn
      workbenchController.send(ui, M.UpdateSketchUIState(Map(StopSketchBtn → ElemEnabled)))
      val stopSketchBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      stopSketchBtn.element shouldEqual StopSketchBtn
      stopSketchBtn.action shouldEqual ElemEnabled
      //Close button
      workbenchController.send(ui, M.SetSketchUIStatusString("Do hit close (X) button...", Color.Red))
      val closeBtn = workbenchController.expectMsgType[M.SketchUIActionTriggered](10.seconds)
      closeBtn.element shouldEqual CloseBtn
      closeBtn.action shouldEqual Unit
      //Buttons test done
      workbenchController.send(ui, M.SetSketchUIStatusString("Buttons test done.", Color.Green))
      sleep(2.second)
      //Hide UI
      workbenchController.send(ui, M.HideSketchUI)
      workbenchController.expectMsgType[M.SketchUIChanged].isShow shouldEqual false
      sleep(2.second)
      //Terminate UI
      workbenchController.send(ui, M.TerminateSketchUI)
      workbenchController.expectMsg(M.SketchUITerminated)
      workbenchController.expectTerminated(ui)}
  }
}
