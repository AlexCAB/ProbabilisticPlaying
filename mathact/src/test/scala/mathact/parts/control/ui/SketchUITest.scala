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
import com.typesafe.config.ConfigFactory
import mathact.parts.UIActorTestSpec
import mathact.parts.model.config.{SketchUIConfigLike, MainConfigLike}
import mathact.parts.model.enums.SketchUIElement._
import mathact.parts.model.messages.M
import org.scalatest.Suite

import scala.concurrent.duration._
import scalafx.scene.image.Image


/** Testing of SketchUI actor
  * Created by CAB on 21.09.2016.
  */

class SketchUITest extends UIActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    val sketchUIConfig = new SketchUIConfigLike{
      val buttonsSize = 25
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
      //Show UI
      workbenchController.send(ui, M.ShowSketchUI)
      workbenchController.expectMsgType[M.SketchUIChanged].isShow shouldEqual true
      sleep(2.second)
      //


           sleep(5.second)




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
