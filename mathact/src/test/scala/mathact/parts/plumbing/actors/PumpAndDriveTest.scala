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
import mathact.parts.plumbing.fitting.{Pipe, Inlet, Outlet}
import mathact.parts._
import mathact.parts.data.Msg
import mathact.parts.plumbing.{Pump, Fitting}
import org.scalatest.Suite
import scala.concurrent.duration._


/** Testing of Pump with Drive actor
  * Created by CAB on 15.08.2016.
  */

class PumpAndDriveTest extends ActorTestSpec{
  //Test data
  trait TestCase extends Suite{
    //Helpers definitions
    case class DriveState(
      outlets: Map[Int, Outlet[_]], // (Outlet ID, Outlet, taskQueue)
      inlets: Map[Int, (Inlet[_], Int)],    // (Inlet ID, Outlet, taskQueueSize)
      pendingConnections: List[Msg])
    //Helpers actors
    lazy val testController = TestProbe("TestController_" + randomString())
    lazy val testImpeller = TestProbe("TestImpeller_" + randomString())
    lazy val testPumping = TestActor("TestPumping_" + randomString())(self ⇒ {
      case Msg.NewDrive(toolPump, toolName, toolImage) ⇒
        println(s"[PumpAndDriveTest.testPumping.NewDrive] Creating of drive for tool: $toolName")
        Right{ system.actorOf(Props(
          new Drive(toolPump,  "TestTool", self, testImpeller.ref){
            //Get actor state
            override def receive: PartialFunction[Any, Unit]  = {
              case GetDriveState ⇒ sender ! DriveState(
                outlets = outlets.map{ case (id, d) ⇒ (id, d.pipe) }.toMap,
                inlets = inlets.map{ case (id, d) ⇒ (id, (d.pipe, d.taskQueue.size)) }.toMap,
                pendingConnections.toList)
              case m ⇒ super.receive.apply(m)}}),
          "Drive_" + randomString())}})
    lazy val otherDriver = TestProbe("TestOtherDriver_" + randomString())
    //Test workbench context
    lazy val testWorkbenchContext = new WorkbenchContext(system, testController.ref, testPumping.ref){
      override val pumping: ActorRef = testPumping.ref}
    //Test tools
    lazy val testTool = new Fitting with OnStart with OnStop{
      //Variable
      @volatile private var onStartCall = false
      @volatile private var onStopCall = false
      //Pump
      val pump: Pump = new Pump(testWorkbenchContext, this, "TestTool", None)
      //Pipes
      val testPipe = new TestPipe[Double]
      lazy val outlet = Outlet(testPipe)
      lazy val inlet = Inlet(testPipe)
      //On start and stop
      protected def onStart() = { onStartCall = true }
      protected def onStop() = { onStopCall = true }
      //Helpers methods
      def isOnStartCalled: Boolean = onStartCall
      def isOnStopCalled: Boolean = onStopCall}
    lazy val testDrive = testTool.pump.drive
    lazy val otherTool = new Fitting{
      //Pump
      val pump: Pump = new Pump(testWorkbenchContext, this, "OtherTool", None){
        override val drive = otherDriver.ref}
      //Pipes
      val testPipe = new TestPipe[Double]
      lazy val outlet = Outlet(testPipe)
      lazy val inlet = Inlet(testPipe)}
    lazy val startedTestTool = {
      testDrive
      testPumping.send(testDrive, Msg.BuildDrive)
      testPumping.expectMsg(Msg.DriveBuilt)
      testPumping.send(testDrive, Msg.StartDrive)
      testPumping.expectMsg(Msg.DriveStarted)
      testTool.isOnStopCalled shouldEqual false
      testTool}
    lazy val connectedTools = {
      startedTestTool
      otherTool
      //First connection
      val otherOutlet = otherTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
      val testInlet = startedTestTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
      startedTestTool.inlet.plug(otherTool.outlet)
      val connectTo = otherDriver.expectMsgType[Msg.ConnectTo]
      connectTo.outletId     shouldEqual otherOutlet.pipeId
      connectTo.inlet.pipeId shouldEqual testInlet.pipeId
      otherDriver.send(testDrive, Msg.ConnectionAdded(testInlet.pipeId, otherOutlet.pipeId))
      //Second connection
      val testOutlet = startedTestTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
      val otherInlet = otherTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
      startedTestTool.outlet.attach(otherTool.inlet)
      val addConnection = otherDriver.expectMsgType[Msg.AddConnection]
      addConnection.inletId       shouldEqual otherInlet.pipeId
      addConnection.outlet.pipeId shouldEqual testOutlet.pipeId
      otherDriver.send(testDrive, Msg.ConnectTo(addConnection.initiator, testOutlet.pipeId, otherInlet))
      val connectionAdded = otherDriver.expectMsgType[Msg.ConnectionAdded]
      connectionAdded.inletId  shouldEqual otherInlet.pipeId
      connectionAdded.outletId shouldEqual testOutlet.pipeId
      otherDriver.send(testDrive, Msg.PipesConnected(otherInlet.pipeId, testOutlet.pipeId))
      (testInlet, testOutlet, otherInlet, otherOutlet)}}
  //Testing
  "on start and on end" should{
//    "adding of Outlet and Inlet" in new TestCase {
//      //Preparing
//      testTool.outlet
//      testTool.inlet
//      val outletId = testTool.pump.addOutlet(new Outlet[Int]{}, Some("outletId1"))
//      val inletId = testTool.pump.addInlet(new Inlet[Int]{protected def drain(value: Int): Unit = {}}, Some("inletId1"))
//      //Testing
//      val DriveState(outlets, inlets, _) = testDrive.askForState[DriveState]
//      outlets should have size 2
//      inlets should have size 2
//      outlets.keys should contain (outletId)
//      inlets.keys should contain (inletId)}
//    "before BuildDrive, add new connections to pending list" in new TestCase {
//      //Preparing
//      val testOutlet1 = testTool.outlet
//      val testInlet1 = testTool.inlet
//      val otherOutlet1 = new Outlet[Double]{}
//      val otherInlet1 = new Inlet[Double]{protected def drain(value: Double): Unit = {}}
//      //Connecting and disconnecting
//      testTool.outlet.attach(otherInlet1)
//      testTool.inlet.plug(otherOutlet1)
//      //Testing
//      val pendingCon = testDrive.askForState[DriveState].pendingConnections
//      pendingCon should have size 2}
    "by BuildDrive, create connections from pending list and reply with DriveBuilt (for 'plug')" in new TestCase {
      //Preparing
      val outlet = otherTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
      val inlet = testTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
      //Connecting (test tool have inlet)
      testTool.inlet.plug(otherTool.outlet)
      testTool.inlet.unplug(otherTool.outlet)
      testDrive.askForState[DriveState].pendingConnections should have size 2
      //Send BuildDrive
      testPumping.send(testDrive, Msg.BuildDrive)
      //Test connecting
      val connectTo = otherDriver.expectMsgType[Msg.ConnectTo]
      connectTo.initiator    shouldEqual testDrive
      connectTo.outletId     shouldEqual outlet.pipeId
      connectTo.inlet.pipeId shouldEqual inlet.pipeId
      otherDriver.send(testDrive, Msg.ConnectionAdded(inlet.pipeId, outlet.pipeId))
      //Test disconnecting
      val delConnection = otherDriver.expectMsgType[Msg.DelConnection]
      delConnection.initiator    shouldEqual testDrive
      delConnection.outletId     shouldEqual outlet.pipeId
      delConnection.inlet.pipeId shouldEqual inlet.pipeId
      otherDriver.send(testDrive, Msg.DisconnectFrom(delConnection.initiator, inlet.pipeId, outlet))
      val connectionDeleted = otherDriver.expectMsgType[Msg.ConnectionDeleted]
      connectionDeleted.outletId shouldEqual outlet.pipeId
      connectionDeleted.inletId  shouldEqual inlet.pipeId
      otherDriver.send(testDrive, Msg.PipesDisconnected(inlet.pipeId, outlet.pipeId))
      //Expect DriveBuilt
      testPumping.expectMsg(Msg.DriveBuilt)
      sleep(500.millis) //Wait for processing of PipesConnected by testTool
      testDrive.askForState[DriveState].pendingConnections should have size 0}
//    "by BuildDrive, create connections from pending list and reply with DriveBuilt (for 'attach')" in new TestCase {
//      //Preparing
//      val outlet = testTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
//      val inlet = otherTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
//      //Connecting (test tool have outlet)
//      testTool.outlet.attach(otherTool.inlet)
//      testTool.outlet.detach(otherTool.inlet)
//      testDrive.askForState[DriveState].pendingConnections should have size 2
//      //Send BuildDrive
//      testPumping.send(testDrive, Msg.BuildDrive)
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
//      //Test disconnecting
//      val disconnectFrom = otherDriver.expectMsgType[Msg.DisconnectFrom]
//      disconnectFrom.initiator    shouldEqual testDrive
//      disconnectFrom.inletId     shouldEqual inlet.pipeId
//      disconnectFrom.outlet.pipeId shouldEqual outlet
//      otherDriver.send(testDrive, Msg.ConnectionDeleted(inlet.pipeId, outlet.pipeId))
//      //Expect DriveBuilt
//      testPumping.expectMsg(Msg.DriveBuilt)
//      sleep(500.millis) //Wait for processing of PipesConnected by testTool
//      testDrive.askForState[DriveState].pendingConnections should have size 0}
//    "by StartDrive, run user init function and reply with DriveStarted" in new TestCase {
//      //Preparing
//      testTool
//      testPumping.send(testDrive, Msg.BuildDrive)
//      testPumping.expectMsg(Msg.DriveBuilt)
//      testTool.isOnStartCalled shouldEqual false
//      //Test
//      testPumping.send(testDrive, Msg.StartDrive)
//      testPumping.expectMsg(Msg.DriveStarted)
//      testTool.isOnStartCalled shouldEqual true}
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
  }
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
