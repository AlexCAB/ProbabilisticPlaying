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

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.Application
import mathact.parts.bricks.WorkbenchContext
import mathact.parts.dummies.{TestSketchWithSmallTimeout, TestSketchEmpty}
import mathact.parts.{WorkbenchLike, ActorTestSpec}
import mathact.parts.model.config.{MainConfigLike, DriveConfigLike, PumpConfigLike, PumpingConfigLike}
import mathact.parts.model.data.sketch.{SketchUIState, SketchData}
import mathact.parts.model.enums._
import mathact.parts.model.messages.M
import mathact.parts.WorkbenchLike
import org.scalatest.Suite

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.pattern.ask


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
      clazz: Class[_] = classOf[TestSketchEmpty],
      autorun: Boolean,
      showUserLogUi: Boolean,
      showVisualisationUi: Boolean)
    :SketchData = SketchData(
        clazz,
        className = "this.TestSketch",
        sketchName = Some("TestSketch1"),
        sketchDescription = Some("Testing sketch 1"),
        autorun,
        showUserLogUi,
        showVisualisationUi)
    //Helpers actors
    def testAskMainController(workbenchController: ActorRef) = system.actorOf(Props(
      new Actor{
        def receive = {
          case M.NewWorkbenchContext(workbench) ⇒
            println(
              s"[WorkbenchControllerTest.testAskMainController] Send GetWorkbenchContext, " +
              s"sender: $sender, workbench: $workbench")
            workbenchController ! M.GetWorkbenchContext(sender)
          case m ⇒
            println(s"[WorkbenchControllerTest.testAskMainController] Unknown msg: $m")}}),
      "TestAskMainController_" + randomString())
    lazy val testActor = TestProbe("testActor_" + randomString())
    lazy val testMainController = TestProbe("TestMainController_" + randomString())
    lazy val testSketchUi = TestProbe("TestSketchUi_" + randomString())
    lazy val testUserLogging = TestProbe("TestUserLogging_" + randomString())
    lazy val testVisualization = TestProbe("Visualization_" + randomString())
    lazy val testPumping = TestProbe("TestPumping_" + randomString())
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
      "WorkbenchController_" + randomString())
    def newStartedWorkbenchController(): ActorRef = {
      val controller = newWorkbenchController( newTestSketchData(
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      testMainController.send(controller, M.WorkbenchControllerStart)
      val sketchUIState = testSketchUi.expectMsgType[M.SetSketchUIState].state
      testSketchUi.send(controller, M.SketchUIActionTriggered(SketchUIAction.UiShowed, sketchUIState))
      controller}



  }
  //Testing
  "WorkbenchController" should{
    "by WorkbenchControllerStart, create sketch instance show UI, start pumping" in new TestCase {
      //Preparing
      val controller = newWorkbenchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout],
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
      //Get context
      testActor.send(controller, M.GetWorkbenchContext(testActor.ref))
      testActor.expectMsgType[Either[Exception, WorkbenchContext]]
      //Run plumbing
      testPumping.expectMsg(M.StartPumping)
      testPumping.send(controller, M.PumpingStarted)


      println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT")
      //Check if Sketch build
//      TestSketch.isInstanceCreated shouldEqual true





    }




    //TODO Разные варианты запуска (стразными отображениями и ошибками скетчей)



//    "by GetWorkbenchContext, create and return WorkbenchContext" in new TestCase {
//      //Preparing
//      val controller = newStartedWorkbenchController()
//      val askMainController = testAskMainController(controller)
//      val askTimeout = 1.second
//      //Construct Workbench and do ask
//      val workbench = new WorkbenchLike{
//        val res: Either[Exception,WorkbenchContext]  = Await.result(
//          ask(askMainController, M.NewWorkbenchContext(this))(askTimeout).mapTo[Either[Exception,WorkbenchContext]],
//          askTimeout)
//        println("[WorkbenchControllerTest.workbench] res: " + res)
//        res.isRight shouldEqual true
//        protected implicit val context: WorkbenchContext = res.right.get}}







  }

}
