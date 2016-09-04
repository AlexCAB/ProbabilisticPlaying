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

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.Application
import mathact.parts.{TestSketch, ActorTestSpec}
import mathact.parts.model.config.{MainConfigLike, DriveConfigLike, PumpConfigLike, PumpingConfigLike}
import mathact.parts.model.data.sketch.{SketchUIState, SketchData}
import mathact.parts.model.enums._
import mathact.parts.model.messages.M
import mathact.tools.Workbench
import org.scalatest.Suite

import scala.concurrent.duration._


/** Workbench controller test
  * Created by CAB on 02.09.2016.
  */

class WorkbenchControllerTest extends ActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    val testMainConfig = new MainConfigLike{
      val config = ConfigFactory.load()
      val sketchBuildingTimeout = 5.second
      val pumping = new PumpingConfigLike{
        val pump = new PumpConfigLike{
          val askTimeout = Timeout(1.second) }
        val drive = new DriveConfigLike{
          val pushTimeoutCoefficient = 0
          val startFunctionTimeout = 1.second
          val messageProcessingTimeout = 1.second
          val stopFunctionTimeout = 1.second
          val impellerMaxQueueSize = 0
          val uiOperationTimeout = 1.second}}}
    //Test SketchData
    def newTestSketchData(
      autorun: Boolean,
      showUserLogUi: Boolean,
      showVisualisationUi: Boolean)
    :SketchData = SketchData(
        clazz = classOf[TestSketch],
        className = "this.TestSketch",
        sketchName = Some("TestSketch1"),
        sketchDescription = Some("Testing sketch 1"),
        autorun,
        showUserLogUi,
        showVisualisationUi)
    //Helpers actors
    lazy val testMainController = TestProbe("TestMainController" + randomString())
    lazy val testSketchUi = TestProbe("TestSketchUi" + randomString())
    lazy val testUserLogging = TestProbe("TestUserLogging" + randomString())
    lazy val testVisualization = TestProbe("Visualization" + randomString())
    lazy val testPumping = TestProbe("TestPumping" + randomString())
    //WorkbenchController
    def newWorkbenchController(sketch: SketchData): ActorRef = system.actorOf(Props(
      new WorkbenchController(
        testMainConfig,
        sketch,
        testMainController.ref,
        testSketchUi.ref,
        testUserLogging.ref,
        testVisualization.ref,
        testPumping.ref)),
      "WorkbenchController_" + randomString())}
  //Testing
  "WorkbenchController" should{
    "by WorkbenchControllerStart, create sketch instance show UI, start pumping" in new TestCase {
      //Preparing
      val controller = newWorkbenchController( newTestSketchData(
        autorun = true,
        showUserLogUi = true,
        showVisualisationUi = true))
      //Send start
      testMainController.send(controller, M.WorkbenchControllerStart)
      //Show sketch UI
      val sketchUIState1 = testSketchUi.expectMsgType[M.SetSketchUIState].state
      sketchUIState1 shouldEqual SketchUIState(
        isUiShown = true,
        runBtnEnable = false,
        showToolUiBtnEnable = true,
        hideToolUiBtnEnable = true,
        skipAllTimeoutProcBtnEnable = true,
        stopBtnEnable = false,
        logUiBtnEnable = true,
        logUiBtnIsShow = true,
        visualisationUiBtnEnable = true,
        visualisationUiBtnIsShow = true)
      testSketchUi.send(controller, M.SketchUIActionTriggered(SketchUIAction.UiShowed, sketchUIState1))
      //Show user logging UI
      testUserLogging.expectMsg(M.ShowUserLoggingUI)
      testUserLogging.send(controller, M.UserLoggingUIShowed)
      //Show visualization UI
      testVisualization.expectMsg(M.ShowVisualizationUI)
      testVisualization.send(controller, M.VisualizationUIShowed)
      //Run plumbing
      testPumping.expectMsg(M.StartPumping)
      testPumping.send(controller, M.PumpingStarted)
      //Check if Sketch build
      TestSketch.isInstanceCreated shouldEqual true}




    //TODO Разные варианты запуска (стразными отображениями и ошибками скетчей)

//
//
//    "by GetWorkbenchContext, create and return WorkbenchContext" in new TestCase {
//
//
//
//
//    }







  }

}
