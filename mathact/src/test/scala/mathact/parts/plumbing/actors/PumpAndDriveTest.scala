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

package mathact.parts.plumbing.actors

import akka.actor.{Terminated, ActorRef, Props}
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import mathact.parts.plumbing.fitting._
import mathact.parts._
import mathact.parts.data.Msg
import mathact.parts.plumbing.{Pump, Fitting}
import org.scalatest.Suite
import scala.concurrent.Future
import scala.concurrent.duration._


/** Testing of Pump with Drive actor
  * Created by CAB on 15.08.2016.
  */

class PumpAndDriveTest extends ActorTestSpec{
  //Test data
  trait TestCase extends Suite{
    import system.dispatcher
    //Helpers definitions
    case class DriveState(
      outlets: Map[Int, OutPipe[_]], // (Outlet ID, Outlet, taskQueue)
      inlets: Map[Int, (InPipe[_], Int)],    // (Inlet ID, Outlet, taskQueueSize)
      pendingConnections: Map[Int, Msg.ConnectPipes])
    //Helpers actors
    lazy val testController = TestProbe("TestController_" + randomString())
    lazy val userLogging = TestProbe("UserLogging_" + randomString())
    lazy val testImpeller = TestProbe("TestImpeller_" + randomString())
    lazy val testPumping = TestActor("TestPumping_" + randomString())(self ⇒ {
      case Msg.NewDrive(toolPump, toolName, toolImage) ⇒ Some{ Right{
        val drive = system.actorOf(Props(
          new Drive(toolPump,  "TestTool", self, testImpeller.ref, userLogging.ref){
            //Get actor state
            override def receive: PartialFunction[Any, Unit]  = {
              case GetDriveState ⇒ sender ! DriveState(
                outlets = outlets.map{ case (id, d) ⇒ (id, d.pipe) }.toMap,
                inlets = inlets.map{ case (id, d) ⇒ (id, (d.pipe, d.taskQueue.size)) }.toMap,
                getPendingList)
              case m ⇒ super.receive.apply(m)}}),
          "Drive_" + randomString())
        println(s"[PumpAndDriveTest.testPumping.NewDrive] Created of drive for tool: $toolName, drive: $drive")
        drive}}})
    //Test workbench context
    lazy val testWorkbenchContext =
      new WorkbenchContext(system, testController.ref, testPumping.ref, ConfigFactory.load()){
        override val pumping: ActorRef = testPumping.ref}
    //Test tools
    object tools{
      lazy val testTool = new Fitting with OnStart with OnStop{
        //Variable
        @volatile private var onStartCall = false
        @volatile private var onStopCall = false
        //Pump
        val pump: Pump = new Pump(testWorkbenchContext, this, "TestTool", None)
        //Pipes
        val testPipe = new TestIncut[Double]
        lazy val outlet = Outlet(testPipe, "testOutlet")
        lazy val inlet = Inlet(testPipe, "testInlet")
        //On start and stop
        protected def onStart() = { onStartCall = true }
        protected def onStop() = { onStopCall = true }
        //Helpers methods
        def isOnStartCalled: Boolean = onStartCall
        def isOnStopCalled: Boolean = onStopCall}
      lazy val testDrive = testTool.pump.drive
      lazy val otherDriver = TestActor("TestOtherDriver_" + randomString())(self ⇒ {
        case Msg.AddOutlet(pipe, _) ⇒ Some(Right(1))
        case Msg.AddInlet(pipe, _) ⇒  Some(Right(2))})
      lazy val otherTool = new Fitting{
        //Pump
        val pump: Pump = new Pump(testWorkbenchContext, this, "OtherTool", None){
          override val drive = otherDriver.ref}
        //Pipes
        val testIncut = new TestIncut[Double]
        lazy val outlet = Outlet(testIncut, "otherOutlet")
        lazy val inlet = Inlet(testIncut, "otherInlet")}
      lazy val builtTool = {
        testPumping.send(testTool.pump.drive, Msg.BuildDrive)
        testPumping.expectMsg(Msg.DriveBuilt)
        testTool.isOnStartCalled shouldEqual false
        testTool}
      lazy val startedTool = {
        testPumping.send(testTool.pump.drive, Msg.BuildDrive)
        testPumping.expectMsg(Msg.DriveBuilt)
        testPumping.send(testTool.pump.drive, Msg.StartDrive)
        testPumping.expectMsg(Msg.DriveStarted)
        testTool.isOnStopCalled shouldEqual false
        testTool}




      //    lazy val connectedTools = {
      //      //Tools
      //      startedTestTool
      //      otherTool
      //      //First connection
      //      val otherOutlet = otherTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
      //      val testInlet = startedTestTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
      //      startedTestTool.inlet.plug(otherTool.outlet)
      //      val connectTo = otherDriver.expectMsgType[Msg.ConnectTo]
      //      connectTo.outletId     shouldEqual otherOutlet.pipeId
      //      connectTo.inlet.pipeId shouldEqual testInlet.pipeId
      //      otherDriver.send(testDrive, Msg.ConnectionAdded(testInlet.pipeId, otherOutlet.pipeId))
      //      //Second connection
      //      val testOutlet = startedTestTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
      //      val otherInlet = otherTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
      //      startedTestTool.outlet.attach(otherTool.inlet)
      //      val addConnection = otherDriver.expectMsgType[Msg.AddConnection]
      //      addConnection.inletId       shouldEqual otherInlet.pipeId
      //      addConnection.outlet.pipeId shouldEqual testOutlet.pipeId
      //      otherDriver.send(testDrive, Msg.ConnectTo(addConnection.initiator, testOutlet.pipeId, otherInlet))
      //      val connectionAdded = otherDriver.expectMsgType[Msg.ConnectionAdded]
      //      connectionAdded.inletId  shouldEqual otherInlet.pipeId
      //      connectionAdded.outletId shouldEqual testOutlet.pipeId
      //      otherDriver.send(testDrive, Msg.PipesConnected(otherInlet.pipeId, testOutlet.pipeId))
      //      (testInlet, testOutlet, otherInlet, otherOutlet)}




    }









  }
  //Testing
//  "On starting" should{
//    "adding of Outlet and Inlet" in new TestCase {
//      //Preparing
//      val outletId = tools.testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData.pipeId
//      val inletId = tools.testTool.inlet.asInstanceOf[InPipe[Double]].pipeData.pipeId
//      tools.otherTool.outlet
//      tools.otherTool.inlet
//       //Testing
//      val DriveState(outlets, inlets, _) = tools.testDrive.askForState[DriveState]
//      outlets should have size 1
//      inlets should have size 1
//      outlets.keys should contain (outletId)
//      inlets.keys should contain (inletId)}
//    "before BuildDrive, add new connections to pending list" in new TestCase {
//      //Preparing
//      val testOutlet1 = tools.testTool.outlet
//      val testInlet1 = tools.testTool.inlet
//      val otherOutlet1 = tools.otherTool.outlet
//      val otherInlet1 =tools.otherTool.inlet
//      //Connecting and disconnecting
//      tools.testTool.outlet.attach(otherInlet1)
//      tools.testTool.inlet.plug(otherOutlet1)
//      //Testing
//      val pendingCon = tools.testDrive.askForState[DriveState].pendingConnections
//      pendingCon should have size 2}
//    "by BuildDrive, create connections from pending list and reply with DriveBuilt (for 'plug')" in new TestCase {
//      //Preparing
//      val outlet = tools.otherTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
//      val inlet = tools.testTool.inlet.asInstanceOf[InPipe[Double]].pipeData
//      //Connecting (test tool have inlet)
//      tools.testTool.inlet.plug(tools.otherTool.outlet)
//      tools.testDrive.askForState[DriveState].pendingConnections should have size 1
//      //Send BuildDrive
//      tools.otherDriver.send(tools.testDrive, Msg.BuildDrive)
//      val connectTo = tools.otherDriver.expectMsgType[Msg.ConnectTo]
//      //Test connecting
//      println(s"[PumpAndDriveTest] connectTo: $connectTo")
//      connectTo.initiator    shouldEqual tools.testDrive
//      connectTo.outletId     shouldEqual outlet.pipeId
//      connectTo.inlet.pipeId shouldEqual inlet.pipeId
//      //Send Msg.PipesConnected and expect Msg.DriveBuilt
//      testPumping.send(connectTo.initiator, Msg.PipesConnected(connectTo.connectionId, inlet.pipeId, outlet.pipeId))
//      testPumping.expectMsg(Msg.DriveBuilt)
//      //Check pending list
//      sleep(500.millis) //Wait for processing of PipesConnected by testTool
//      tools.testDrive.askForState[DriveState].pendingConnections should have size 0}
//    "by BuildDrive, create connections from pending list and reply with DriveBuilt (for 'attach')" in new TestCase {
//      //Preparing
//      val outlet = tools.testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
//      val inlet = tools.otherTool.inlet.asInstanceOf[InPipe[Double]].pipeData
//      //Connecting (test tool have outlet)
//      tools.testTool.outlet.attach(tools.otherTool.inlet)
//      tools.testDrive.askForState[DriveState].pendingConnections should have size 1
//      //Send BuildDrive
//      testPumping.send(tools.testDrive, Msg.BuildDrive)
//      //Test connecting
//      val addConnection = tools.otherDriver.expectMsgType[Msg.AddConnection]
//      addConnection.initiator     shouldEqual tools.testDrive
//      addConnection.inletId       shouldEqual inlet.pipeId
//      addConnection.outlet.pipeId shouldEqual outlet.pipeId
//      tools.otherDriver.send(tools.testDrive, Msg.ConnectTo(addConnection.connectionId, addConnection.initiator, outlet.pipeId, inlet))
//      //Expect DriveBuilt
//      testPumping.expectMsg(Msg.DriveBuilt)
//      sleep(500.millis) //Wait for processing of PipesConnected by testTool
//      tools.testDrive.askForState[DriveState].pendingConnections should have size 0}
//    "by StartDrive, run user init function and reply with DriveStarted" in new TestCase {
//      //Preparing
//      tools.builtTool
//      //Test
//      testPumping.send(tools.testDrive, Msg.StartDrive)
//      val runTask = testImpeller.expectMsgType[Msg.RunTask[_]]
//      println("[PumpAndDriveTest] runTask: " + runTask)
//      runTask.task()
//      testImpeller.send(tools.testDrive, Msg.TaskDone(runTask.name, 1.second, Unit))
//      testPumping.expectMsg(Msg.DriveStarted)
//      tools.testTool.isOnStartCalled shouldEqual true}
//    "by StartDrive, for case TaskTimeout send LogWarning to user logging actor and keep working" in new TestCase {
//      //Preparing
//      tools.builtTool
//      //Test
//      testPumping.send(tools.testDrive, Msg.StartDrive)
//      val runTask = testImpeller.expectMsgType[Msg.RunTask[_]]
//      println("[PumpAndDriveTest] runTask: " + runTask)
//      testImpeller.send(tools.testDrive, Msg.TaskTimeout(runTask.name, 5.second))
//      val logWarning = userLogging.expectMsgType[Msg.LogWarning]
//      println("[PumpAndDriveTest] logWarning: " + logWarning)
//      runTask.task()
//      testImpeller.send(tools.testDrive, Msg.TaskDone(runTask.name, 1.second, Unit))
//      testPumping.expectMsg(Msg.DriveStarted)
//      tools.testTool.isOnStartCalled shouldEqual true}
//    "by StartDrive, for case TaskTimeout send LogError to user logging actor and keep working" in new TestCase {
//      //Preparing
//      tools.builtTool
//      //Test
//      testPumping.send(tools.testDrive, Msg.StartDrive)
//      val runTask = testImpeller.expectMsgType[Msg.RunTask[_]]
//      println("[PumpAndDriveTest] runTask: " + runTask)
//      testImpeller.send(tools.testDrive, Msg.TaskFailed(runTask.name, 5.second, new Exception("Oops!!")))
//      runTask.task()
//      val logError = userLogging.expectMsgType[Msg.LogError]
//      println("[PumpAndDriveTest] logError: " + logError)
//      testPumping.expectMsg(Msg.DriveStarted)
//      tools.testTool.isOnStartCalled shouldEqual true}
//  }
  "On user message" should{




    "by call pour(value), send UserData, to all inlets of connected drives" in new TestCase {
      //Preparing
      val (testInlet, testOutlet, otherInlet, otherOutlet) = connectedTools
      val value1 = randomDouble()
      val value2 = randomDouble()
      //Call pour(value) for other tool
      otherTool.testPipe.sendValue(value1)
      val userData = otherDriver.expectMsgType[Msg.UserData[Double]]
      userData.outletId shouldEqual otherOutlet.pipeId
      userData.value    shouldEqual value1
      //Call pour(value) test tool
      testTool.testPipe.sendValue(value2)
      val userMessage = otherDriver.expectMsgType[Msg.UserMessage[Double]]
      userMessage.outletId shouldEqual testOutlet.pipeId
      userMessage.inletId  shouldEqual otherInlet.pipeId
      userMessage.value    shouldEqual value2}



//    "by UserMessage, put user message to queue and reply with DriveLoad" in new TestCase {
//      //Preparing
//      val (testInlet, testOutlet, otherInlet, otherOutlet) = connectedTools
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      //First message, reply with DriveLoad and start processing
//      otherDriver.send(testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      val driveLoad1 = otherDriver.expectMsgType[Msg.DriveLoad]
//      driveLoad1.drive        shouldEqual testDrive
//      driveLoad1.maxQueueSize shouldEqual 0
//      val runTask1 = testImpeller.expectMsgType[Msg.RunTask]
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 1
//      //Second message, reply with DriveLoad and put user message to queue
//      otherDriver.send(testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
//      val driveLoad2 = otherDriver.expectMsgType[Msg.DriveLoad]
//      driveLoad2.drive        shouldEqual testDrive
//      driveLoad2.maxQueueSize shouldEqual 1
//      testImpeller.expectNoMsg()
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 2
//      //Start of processing of second message
//      testImpeller.send(testDrive, Msg.TaskDone(runTask1.name, 1.second, Unit))
//      val runTask2 = testImpeller.expectMsgType[Msg.RunTask]
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 1
//      //Done process second task
//      testImpeller.send(testDrive, Msg.TaskDone(runTask2.name, 1.second, Unit))
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 0}
//    "dequeue user message and call user message handling function" in new TestCase {
//      //Preparing
//      val (testInlet, testOutlet, otherInlet, otherOutlet) = connectedTools
//      val value1 = randomDouble()
//      //Send message
//      otherDriver.send(testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      otherDriver.expectMsgType[Msg.DriveLoad]
//      val runTask1 = testImpeller.expectMsgType[Msg.RunTask]
//      //Run task
//      testTool.testPipe.getReceivedValues shouldBe empty
//      runTask1.task()
//      testTool.testPipe.getReceivedValues should have size 1
//      testTool.testPipe.getReceivedValues.head shouldEqual value1}
//    "on Terminate after disconnect inlets, handle all usr messages before disconnect outlets" in new TestCase {
//
//    }
//    "by DriveLoad, evaluate message handling timeout" in new TestCase {
//
//    }
  }
//  "On stopping" should{
//    "do something" in new TestCase {
//
//
//
//    }
//  }












//    "by StopDrive, run user stopping function and reply with DriveStopped" in new TestCase {
//      //Preparing
//      testTool
//      testPumping.send(testDrive, Msg.BuildDrive)
//      testPumping.expectMsg(Msg.DriveBuilt)
//      testPumping.send(testDrive, Msg.StartDrive)
//      testPumping.expectMsg(Msg.DriveStarted)
//      testTool.isOnStopCalled shouldEqual false
//      //Test
//      testPumping.send(testDrive, Msg.StopDrive)
//      testPumping.expectMsg(Msg.DriveStopped)
//      testTool.isOnStopCalled shouldEqual true}
//    "by TerminateDrive, disconnect all connections and reply with DriveTerminated" in new TestCase {
//      //Preparing
//      val testOutlet = testTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
//      val testInlet = testTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
//      val otherOutlet = otherTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
//      val otherInlet = otherTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
//      testPumping.watch(testDrive)
//      //Connecting
//      testTool.inlet.plug(otherTool.outlet)
//      testTool.outlet.attach(otherTool.inlet)
//      testDrive.askForState[DriveState].pendingConnections should have size 2
//      //Build
//      testPumping.send(testDrive, Msg.BuildDrive)
//      otherDriver.expectMsgType[Msg.ConnectTo].initiator shouldEqual testDrive
//      otherDriver.send(testDrive, Msg.ConnectionAdded(testInlet.pipeId, otherOutlet.pipeId))
//      otherDriver.expectMsgType[Msg.AddConnection].initiator shouldEqual testDrive
//      otherDriver.send(testDrive, Msg.ConnectTo(testDrive, testOutlet.pipeId, otherInlet))
//      otherDriver.expectMsgType[Msg.ConnectionAdded]
//      otherDriver.send(testDrive, Msg.PipesConnected(otherInlet.pipeId, testOutlet.pipeId))
//      testPumping.expectMsg(Msg.DriveBuilt)
//      //Start and stop
//      testPumping.send(testDrive, Msg.StartDrive)
//      testPumping.expectMsg(Msg.DriveStarted)
//      testPumping.send(testDrive, Msg.StopDrive)
//      testPumping.expectMsg(Msg.DriveStopped)
//      //Terminate
//      testPumping.send(testDrive, Msg.TerminateDrive)
//      //First disconnect inlets of testTool
//      val delInlet = otherDriver.expectMsgType[Msg.DelConnection]
//      delInlet.initiator    shouldEqual testDrive
//      delInlet.outletId     shouldEqual otherOutlet.pipeId
//      delInlet.inlet.pipeId shouldEqual testInlet.pipeId
//      otherDriver.send(testDrive, Msg.DisconnectFrom(testDrive, testInlet.pipeId, otherOutlet))
//      val inletDeleted = otherDriver.expectMsgType[Msg.ConnectionDeleted]
//      inletDeleted.outletId shouldEqual otherOutlet.pipeId
//      inletDeleted.inletId  shouldEqual testInlet.pipeId
//      otherDriver.send(testDrive, Msg.PipesDisconnected(testInlet.pipeId, otherOutlet.pipeId))
//      //Second disconnect outlets of testTool
//      val disoutletFrom = otherDriver.expectMsgType[Msg.DisconnectFrom]
//      disoutletFrom.initiator    shouldEqual testDrive
//      disoutletFrom.inletId      shouldEqual otherInlet.pipeId
//      disoutletFrom.outlet.pipeId shouldEqual testOutlet.pipeId
//      otherDriver.send(testDrive, Msg.ConnectionDeleted(testOutlet.pipeId, otherInlet.pipeId))
//      //Terminated
//      testPumping.expectMsg(Msg.DriveTerminated)
//      val t = testPumping.expectMsgType[Terminated]().actor shouldEqual testDrive}
//  }






//  "on connect and disconnect" should{
//    "connect and disconnect to another drive with 'plug' and 'unplug'" in new TestCase {
//      //Preparing
//      val outlet = otherTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
//      val inlet = startedTestTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
//      //Connecting (test tool have inlet)
//      startedTestTool.inlet.plug(otherTool.outlet)
//      //Test connecting
//      val connectTo = otherDriver.expectMsgType[Msg.ConnectTo]
//      connectTo.initiator    shouldEqual testDrive
//      connectTo.outletId     shouldEqual outlet.pipeId
//      connectTo.inlet.pipeId shouldEqual inlet.pipeId
//      otherDriver.send(testDrive, Msg.ConnectionAdded(inlet.pipeId, outlet.pipeId))
//      //Disconnecting
//      startedTestTool.inlet.unplug(otherTool.outlet)
//      //Test disconnecting
//      val delConnection = otherDriver.expectMsgType[Msg.DelConnection]
//      delConnection.initiator    shouldEqual testDrive
//      delConnection.outletId     shouldEqual outlet.pipeId
//      delConnection.inlet.pipeId shouldEqual inlet.pipeId
//      otherDriver.send(testDrive, Msg.DisconnectFrom(delConnection.initiator, inlet.pipeId, outlet))
//      val connectionDeleted = otherDriver.expectMsgType[Msg.ConnectionDeleted]
//      connectionDeleted.outletId shouldEqual outlet.pipeId
//      connectionDeleted.inletId  shouldEqual inlet.pipeId
//      otherDriver.send(testDrive, Msg.PipesDisconnected(inlet.pipeId, outlet.pipeId))}
//    "connect and disconnect to another drive with 'attach' and 'detach'" in new TestCase {
//      //Preparing
//      val outlet = startedTestTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
//      val inlet = otherTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
//      //Connecting (test tool have outlet)
//      startedTestTool.outlet.attach(otherTool.inlet)
//      //Test connecting
//      val addConnection = otherDriver.expectMsgType[Msg.AddConnection]
//      addConnection.initiator     shouldEqual testDrive
//      addConnection.inletId       shouldEqual inlet.pipeId
//      addConnection.outlet.pipeId shouldEqual outlet.pipeId
//      otherDriver.send(testDrive, Msg.ConnectTo(addConnection.initiator, outlet.pipeId, inlet))
//      val connectionAdded = otherDriver.expectMsgType[Msg.ConnectionAdded]
//      connectionAdded.inletId  shouldEqual inlet.pipeId
//      connectionAdded.outletId shouldEqual outlet.pipeId
//      otherDriver.send(testDrive, Msg.PipesConnected(inlet.pipeId, outlet.pipeId))
//      //Disconnecting
//      testTool.outlet.detach(otherTool.inlet)
//      //Test disconnecting
//      val disconnectFrom = otherDriver.expectMsgType[Msg.DisconnectFrom]
//      disconnectFrom.initiator    shouldEqual testDrive
//      disconnectFrom.inletId      shouldEqual inlet.pipeId
//      disconnectFrom.outlet.pipeId shouldEqual outlet
//      otherDriver.send(testDrive, Msg.ConnectionDeleted(inlet.pipeId, outlet.pipeId))}
//    "not connect pipes in Stopping or Terminating mode" in new TestCase {
//      //Stopping
//      testPumping.send(testDrive, Msg.StartDrive)
//      testPumping.expectMsg(Msg.DriveStarted)
//      //Try to connect
//      startedTestTool.inlet.plug(otherTool.outlet)
//      otherDriver.expectNoMsg()
//      //Terminate
//      testPumping.send(testDrive, Msg.TerminateDrive)
//      testPumping.expectMsg(Msg.DriveTerminated)
//      //Try to connect
//      startedTestTool.inlet.plug(otherTool.outlet)
//      otherDriver.expectNoMsg()}
//  }
//  "on user message" should{
//    "by call pour(value), send UserData, to all inlets of connected drives" in new TestCase {
//      //Preparing
//      val (testInlet, testOutlet, otherInlet, otherOutlet) = connectedTools
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      //Call pour(value) for other tool
//      otherTool.testPipe.sendValue(value1)
//      val userData = otherDriver.expectMsgType[Msg.UserData[Double]]
//      userData.outletId shouldEqual otherOutlet.pipeId
//      userData.value    shouldEqual value1
//      //Call pour(value) test tool
//      testTool.testPipe.sendValue(value2)
//      val userMessage = otherDriver.expectMsgType[Msg.UserMessage[Double]]
//      userMessage.outletId shouldEqual testOutlet.pipeId
//      userMessage.inletId  shouldEqual otherInlet.pipeId
//      userMessage.value    shouldEqual value2}
//    "by UserMessage, put user message to queue and reply with DriveLoad" in new TestCase {
//      //Preparing
//      val (testInlet, testOutlet, otherInlet, otherOutlet) = connectedTools
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      //First message, reply with DriveLoad and start processing
//      otherDriver.send(testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      val driveLoad1 = otherDriver.expectMsgType[Msg.DriveLoad]
//      driveLoad1.drive        shouldEqual testDrive
//      driveLoad1.maxQueueSize shouldEqual 0
//      val runTask1 = testImpeller.expectMsgType[Msg.RunTask]
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 1
//      //Second message, reply with DriveLoad and put user message to queue
//      otherDriver.send(testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
//      val driveLoad2 = otherDriver.expectMsgType[Msg.DriveLoad]
//      driveLoad2.drive        shouldEqual testDrive
//      driveLoad2.maxQueueSize shouldEqual 1
//      testImpeller.expectNoMsg()
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 2
//      //Start of processing of second message
//      testImpeller.send(testDrive, Msg.TaskDone(runTask1.name, 1.second, Unit))
//      val runTask2 = testImpeller.expectMsgType[Msg.RunTask]
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 1
//      //Done process second task
//      testImpeller.send(testDrive, Msg.TaskDone(runTask2.name, 1.second, Unit))
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 0}
//    "dequeue user message and call user message handling function" in new TestCase {
//      //Preparing
//      val (testInlet, testOutlet, otherInlet, otherOutlet) = connectedTools
//      val value1 = randomDouble()
//      //Send message
//      otherDriver.send(testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      otherDriver.expectMsgType[Msg.DriveLoad]
//      val runTask1 = testImpeller.expectMsgType[Msg.RunTask]
//      //Run task
//      testTool.testPipe.getReceivedValues shouldBe empty
//      runTask1.task()
//      testTool.testPipe.getReceivedValues should have size 1
//      testTool.testPipe.getReceivedValues.head shouldEqual value1}
//    "on Terminate after disconnect inlets, handle all usr messages before disconnect outlets" in new TestCase {
//
//    }
//    "by DriveLoad, evaluate message handling timeout" in new TestCase {
//
//    }
//  }
}
