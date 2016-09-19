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
import mathact.parts.dummies.{TestSketchWithError, TestSketchWithBigTimeout, TestSketchWithSmallTimeout}
import mathact.parts.model.enums.SketchUIElement._
import mathact.parts.model.enums.SketchUiElemState._
import mathact.parts.{WorkbenchLike, ActorTestSpec}
import mathact.parts.model.config.{MainConfigLike, DriveConfigLike, PumpConfigLike, PumpingConfigLike}
import mathact.parts.model.data.sketch.SketchData
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
      clazz: Class[_] = classOf[TestSketchWithSmallTimeout],
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
    def newBuiltWorkbenchController(): ActorRef = {
      val controller = newWorkbenchController( newTestSketchData(
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      testMainController.send(controller, M.StartWorkbenchController)
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      testActor.send(controller, M.GetWorkbenchContext(testActor.ref))
      testActor.expectMsgType[Either[Exception, WorkbenchContext]]
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testUserLogging.expectMsgType[M.LogInfo]
      testMainController.expectMsgType[M.SketchBuilt]
      controller}



  }
  //Testing
//  "WorkbenchController on start" should{
//    "by WorkbenchControllerStart, create sketch instance show UI, start pumping with autorun on" in new TestCase {
//      //Preparing
//      val controller = newWorkbenchController( newTestSketchData(
//        clazz = classOf[TestSketchWithSmallTimeout],
//        autorun = true,
//        showUserLogUi = true,
//        showVisualisationUi = true))
//      //Send start
//      testMainController.send(controller, M.WorkbenchControllerStart)
//      //Show sketch UI
//      testSketchUi.expectMsg(M.ShowSketchUI)
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
//        RunBtn → ElemDisabled,
//        ShowAllToolsUiBtn → ElemDisabled,
//        HideAllToolsUiBtn → ElemDisabled,
//        SkipAllTimeoutTaskBtn → ElemDisabled,
//        StopSketchBtn → ElemDisabled,
//        LogBtn →  ElemShow,
//        VisualisationBtn → ElemShow)
//      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
//      //Show user logging UI
//      testUserLogging.expectMsg(M.ShowUserLoggingUI)
//      testUserLogging.send(controller, M.UserLoggingUIChanged(isShow = true))
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(LogBtn → ElemShow)
//      //Show visualization UI
//      testVisualization.expectMsg(M.ShowVisualizationUI)
//      testVisualization.send(controller, M.VisualizationUIChanged(isShow = true))
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(VisualisationBtn → ElemShow)
//      //Get context
//      testActor.send(controller, M.GetWorkbenchContext(testActor.ref))
//      testActor.expectMsgType[Either[Exception, WorkbenchContext]]
//      //Update user UI
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(RunBtn → ElemDisabled)
//      //Run plumbing
//      testPumping.expectMsg(M.StartPumping)
//      testPumping.send(controller, M.PumpingStarted)
//      //Update user UI
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
//        RunBtn → ElemDisabled,
//        ShowAllToolsUiBtn → ElemEnabled,
//        HideAllToolsUiBtn → ElemEnabled,
//        SkipAllTimeoutTaskBtn → ElemEnabled,
//        StopSketchBtn → ElemEnabled)
//      //Log info
//      val info1 = testUserLogging.expectMsgType[M.LogInfo]
//      println("[WorkbenchController] info1: " + info1)
//      //Sketch built
//      testMainController.expectMsgType[M.SketchBuilt].workbench.asInstanceOf[TestSketchWithSmallTimeout]
//      //Run plumbing
//      testMainController.expectNoMsg(1.second)
//      testSketchUi.expectNoMsg(1.second)
//      testUserLogging.expectNoMsg(1.second)
//      testVisualization.expectNoMsg(1.second)
//      testPumping.expectNoMsg(1.second)}
//    "by WorkbenchControllerStart, create sketch instance show UI, with autorun off" in new TestCase {
//      //Preparing
//      val controller = newWorkbenchController( newTestSketchData(
//        clazz = classOf[TestSketchWithSmallTimeout],
//        autorun = false,
//        showUserLogUi = false,
//        showVisualisationUi = false))
//      //Send start
//      testMainController.send(controller, M.WorkbenchControllerStart)
//      //Show sketch UI
//      testSketchUi.expectMsg(M.ShowSketchUI)
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
//        RunBtn → ElemDisabled,
//        ShowAllToolsUiBtn → ElemDisabled,
//        HideAllToolsUiBtn → ElemDisabled,
//        SkipAllTimeoutTaskBtn → ElemDisabled,
//        StopSketchBtn → ElemDisabled,
//        LogBtn →  ElemHide,
//        VisualisationBtn → ElemHide)
//      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
//      //Get context
//      testActor.send(controller, M.GetWorkbenchContext(testActor.ref))
//      testActor.expectMsgType[Either[Exception, WorkbenchContext]]
//      //Update user UI
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(RunBtn → ElemEnabled)
//      //Log info
//      val info1 = testUserLogging.expectMsgType[M.LogInfo]
//      println("[WorkbenchController] info1: " + info1)
//      //Sketch built
//      testMainController.expectMsgType[M.SketchBuilt].workbench.asInstanceOf[TestSketchWithSmallTimeout]
//      //Run plumbing
//      testMainController.expectNoMsg(1.second)
//      testSketchUi.expectNoMsg(1.second)
//      testUserLogging.expectNoMsg(1.second)
//      testVisualization.expectNoMsg(1.second)
//      testPumping.expectNoMsg(1.second)}
//    "by WorkbenchControllerStart, terminate sketch if not build in time" in new TestCase {
//      //Preparing
//      val controller = newWorkbenchController( newTestSketchData(
//        clazz = classOf[TestSketchWithBigTimeout],
//        autorun = false,
//        showUserLogUi = false,
//        showVisualisationUi = false))
//      //Send start
//      testMainController.send(controller, M.WorkbenchControllerStart)
//      //Show sketch UI
//      testSketchUi.expectMsg(M.ShowSketchUI)
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
//        RunBtn → ElemDisabled,
//        ShowAllToolsUiBtn → ElemDisabled,
//        HideAllToolsUiBtn → ElemDisabled,
//        SkipAllTimeoutTaskBtn → ElemDisabled,
//        StopSketchBtn → ElemDisabled,
//        LogBtn →  ElemHide,
//        VisualisationBtn → ElemHide)
//      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
//      //Wait for time out
//      sleep(5.second)
//      //Error log
//      val error1 = testUserLogging.expectMsgType[M.LogError]
//      println("[WorkbenchController] error1: " + error1)
//      //Sketch UI update
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
//        RunBtn → ElemDisabled,
//        ShowAllToolsUiBtn → ElemDisabled,
//        HideAllToolsUiBtn → ElemDisabled,
//        SkipAllTimeoutTaskBtn → ElemDisabled,
//        StopSketchBtn → ElemDisabled)
//      //Sketch error
//      testMainController.expectMsgType[M.SketchError]}
//    "by WorkbenchControllerStart, terminate sketch if error on build" in new TestCase {
//      //Preparing
//      val controller = newWorkbenchController( newTestSketchData(
//        clazz = classOf[TestSketchWithError],
//        autorun = false,
//        showUserLogUi = false,
//        showVisualisationUi = false))
//      //Send start
//      testMainController.send(controller, M.WorkbenchControllerStart)
//      //Show sketch UI
//      testSketchUi.expectMsg(M.ShowSketchUI)
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
//        RunBtn → ElemDisabled,
//        ShowAllToolsUiBtn → ElemDisabled,
//        HideAllToolsUiBtn → ElemDisabled,
//        SkipAllTimeoutTaskBtn → ElemDisabled,
//        StopSketchBtn → ElemDisabled,
//        LogBtn →  ElemHide,
//        VisualisationBtn → ElemHide)
//      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
//      //Error log
//      val error1 = testUserLogging.expectMsgType[M.LogError]
//      println("[WorkbenchController] error1: " + error1)
//      //Sketch UI update
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(
//        RunBtn → ElemDisabled,
//        ShowAllToolsUiBtn → ElemDisabled,
//        HideAllToolsUiBtn → ElemDisabled,
//        SkipAllTimeoutTaskBtn → ElemDisabled,
//        StopSketchBtn → ElemDisabled)
//      //Sketch error
//      testMainController.expectMsgType[M.SketchError]}
//    "by GetWorkbenchContext, create and return WorkbenchContext" in new TestCase {
//      //Preparing
//      val controller = newWorkbenchController( newTestSketchData(
//        clazz = classOf[TestSketchWithSmallTimeout],
//        autorun = false,
//        showUserLogUi = false,
//        showVisualisationUi = false))
//      val askMainController = testAskMainController(controller)
//      val askTimeout = 1.second
//      //Start
//      testMainController.send(controller, M.WorkbenchControllerStart)
//      testSketchUi.expectMsg(M.ShowSketchUI)
//      testSketchUi.expectMsgType[M.UpdateSketchUIState]
//      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
//      //Construct Workbench and do ask
//      val workbench = new WorkbenchLike{
//        val res: Either[Exception,WorkbenchContext]  = Await.result(
//          ask(askMainController, M.NewWorkbenchContext(this))(askTimeout).mapTo[Either[Exception,WorkbenchContext]],
//          askTimeout)
//        println("[WorkbenchControllerTest.workbench] res: " + res)
//        res.isRight shouldEqual true
//        protected implicit val context: WorkbenchContext = res.right.get}
//      //UI update, log and built
//      testSketchUi.expectMsgType[M.UpdateSketchUIState]
//      testUserLogging.expectMsgType[M.LogInfo]
//      testMainController.expectMsgType[M.SketchBuilt]}
//  }

//  "WorkbenchController in work" should{
//    "by RunBtn hit, run sketch" in new TestCase {
//      //Preparing
//      val controller = newBuiltWorkbenchController()
//      //Send start
//      testSketchUi.send(controller, M.SketchUIActionTriggered(RunBtn, Unit))
//      //Run Pumping
//      testPumping.expectMsg(M.StartPumping)
//      testPumping.send(controller, M.PumpingStarted)
//      //UI update
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(RunBtn → ElemDisabled) //On StartPumping
//      testSketchUi.expectMsgType[M.UpdateSketchUIState].state shouldEqual Map(    //On PumpingStarted
//        ShowAllToolsUiBtn → ElemEnabled,
//        HideAllToolsUiBtn → ElemEnabled,
//        SkipAllTimeoutTaskBtn → ElemEnabled,
//        StopSketchBtn → ElemEnabled)
//      //User log
//      val info1 = testUserLogging.expectMsgType[M.LogInfo]
//      println("[WorkbenchController] info1: " + info1)
//      //Run plumbing
//      testMainController.expectNoMsg(1.second)
//      testSketchUi.expectNoMsg(1.second)
//      testUserLogging.expectNoMsg(1.second)
//      testVisualization.expectNoMsg(1.second)
//      testPumping.expectNoMsg(1.second)}
//
//
//     //TODO Далее здесь остальные UI действия, за исключением закрытия окна
//
//  }





  "WorkbenchController on shutdown" should{
//    "stop in Creating state" in new TestCase {
//      //Preparing
//      val controller = newWorkbenchController( newTestSketchData(
//        clazz = classOf[TestSketchWithSmallTimeout],
//        autorun = false,
//        showUserLogUi = true,
//        showVisualisationUi = true))
//      testMainController.watch(controller)
//      //Send start
//      testMainController.send(controller, M.StartWorkbenchController)
//      //Show sketch UI
//      testSketchUi.expectMsg(M.ShowSketchUI)
//      testSketchUi.expectMsgType[M.UpdateSketchUIState]
//      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
//      //Send stop
//      testMainController.send(controller, M.StopWorkbenchController)
//      //Show user logging UI
//      testUserLogging.expectMsg(M.ShowUserLoggingUI)
//      testUserLogging.send(controller, M.UserLoggingUIChanged(isShow = true))
//      testSketchUi.expectMsgType[M.UpdateSketchUIState]
//      //Show visualization UI
//      testVisualization.expectMsg(M.ShowVisualizationUI)
//      testVisualization.send(controller, M.VisualizationUIChanged(isShow = true))
//      testSketchUi.expectMsgType[M.UpdateSketchUIState]
//      //Terminate visualization UI
//      testVisualization.expectMsg(M.TerminateVisualization)
//      testVisualization.send(controller, M.VisualizationTerminated)
//      //Terminate user logging UI
//      testUserLogging.expectMsg(M.TerminateUserLogging)
//      testUserLogging.send(controller, M.UserLoggingTerminated)
//      //Terminate sketch UI
//      testSketchUi.expectMsg(M.TerminateSketchUI)
//      testSketchUi.send(controller, M.SketchUITerminated)
//      //Terminating of controller
//      testMainController.expectMsg(M.WorkbenchControllerTerminated)
//      testMainController.expectTerminated(controller)}
    "stop in Building state" in new TestCase {
      //Preparing
      val controller = newWorkbenchController( newTestSketchData(
        clazz = classOf[TestSketchWithSmallTimeout],
        autorun = false,
        showUserLogUi = false,
        showVisualisationUi = false))
      testMainController.watch(controller)
      //Send start
      testMainController.send(controller, M.StartWorkbenchController)
      //Show sketch UI
      testSketchUi.expectMsg(M.ShowSketchUI)
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      testSketchUi.send(controller, M.SketchUIChanged(isShow = true))
      //Get context
      testActor.send(controller, M.GetWorkbenchContext(testActor.ref))
      testActor.expectMsgType[Either[Exception, WorkbenchContext]]
      //Wait for controller switch to Building state
      sleep(1.second)
      //Send stop
      testMainController.send(controller, M.StopWorkbenchController)
      //Sketch built
      testUserLogging.expectMsgType[M.LogInfo]
      testMainController.expectMsgType[M.SketchBuilt]
      testSketchUi.expectMsgType[M.UpdateSketchUIState]
      //Terminate visualization UI
      testVisualization.expectMsg(M.TerminateVisualization)
      testVisualization.send(controller, M.VisualizationTerminated)
      //Terminate user logging UI
      testUserLogging.expectMsg(M.TerminateUserLogging)
      testUserLogging.send(controller, M.UserLoggingTerminated)
      //Terminate sketch UI
      testSketchUi.expectMsg(M.TerminateSketchUI)
      testSketchUi.send(controller, M.SketchUITerminated)
      //Terminating of controller
      testMainController.expectMsg(M.WorkbenchControllerTerminated)
      testMainController.expectTerminated(controller)}



    "stop in Built state" in new TestCase {
      //Preparing
      val controller = newBuiltWorkbenchController()
      //Send stop
      testMainController.send(controller, M.StopWorkbenchController)
      //








    }
//    "stop in BuildingFailed state" in new TestCase {}
//    "stop in Starting state" in new TestCase {}
//    "stop in Working state" in new TestCase {}








  }

}
