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
      outlets: Map[Int, Outlet[_]], // (Outlet ID, Outlet)
      inlets: Map[Int, Inlet[_]],    // (Inlet ID, Outlet)
      pendingConnections: List[Msg])
    //Helpers actors
    lazy val testController = TestProbe("TestController_" + randomString())
    lazy val testPumping = TestActor("TestPumping_" + randomString())(self ⇒ {
      case Msg.NewDrive(toolPump, toolName, toolImage) ⇒
        println(s"[PumpAndDriveTest.testPumping.NewDrive] Creating of drive for tool: $toolName")
        Right{ system.actorOf(Props(
          new Drive(toolPump,  "TestTool", self){
            //Get actor state
            override def receive: PartialFunction[Any, Unit]  = {
              case GetDriveState ⇒ sender ! DriveState(
                outlets = outlets.map{ case (id, d) ⇒ (id, d.pipe) }.toMap,
                inlets = inlets.map{ case (id, d) ⇒ (id, d.pipe) }.toMap,
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
    lazy val otherTool = new Fitting{
      val pump: Pump = new Pump(testWorkbenchContext, this, "OtherTool", None){
        override val drive = otherDriver.ref}
      val testPipe = new TestPipe[Double]
      lazy val outlet = Outlet(testPipe)
      lazy val inlet = Inlet(testPipe)}
    //Drive actor
    lazy val testDrive = testTool.pump.drive}
  //Testing
  "on start and on end" should{
    "adding of Outlet and Inlet" in new TestCase {
      //Preparing
      testTool.outlet
      testTool.inlet
      val outletId = testTool.pump.addOutlet(new Outlet[Int]{}, Some("outletId1"))
      val inletId = testTool.pump.addInlet(new Inlet[Int]{protected def drain(value: Int): Unit = {}}, Some("inletId1"))
      //Testing
      val DriveState(outlets, inlets, _) = testDrive.askForState[DriveState]
      outlets should have size 2
      inlets should have size 2
      outlets.keys should contain (outletId)
      inlets.keys should contain (inletId)}
    "before BuildDrive, add new connections to pending list" in new TestCase {
      //Preparing
      val testOutlet1 = testTool.outlet
      val testInlet1 = testTool.inlet
      val otherOutlet1 = new Outlet[Double]{}
      val otherInlet1 = new Inlet[Double]{protected def drain(value: Double): Unit = {}}
      //Connecting and disconnecting
      testTool.outlet.attach(otherInlet1)
      testTool.inlet.plug(otherOutlet1)
      testTool.outlet.detach(otherInlet1)
      testTool.inlet.unplug(otherOutlet1)
      //Testing
      val pendingCon = testDrive.askForState[DriveState].pendingConnections
      pendingCon should have size 4
      pendingCon should contain (Left(Msg.ConnectPipes(()⇒testOutlet1, ()⇒otherInlet1)))
      pendingCon should contain (Left(Msg.ConnectPipes(()⇒otherOutlet1, ()⇒testInlet1)))
      pendingCon should contain (Right(Msg.DisconnectPipes(()⇒testOutlet1, ()⇒otherInlet1)))
      pendingCon should contain (Right(Msg.DisconnectPipes(()⇒otherOutlet1, ()⇒testInlet1)))}
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
    "by BuildDrive, create connections from pending list and reply with DriveBuilt (for 'attach')" in new TestCase {
      //Preparing
      val outlet = testTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
      val inlet = otherTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
      //Connecting (test tool have outlet)
      testTool.outlet.attach(otherTool.inlet)
      testTool.outlet.detach(otherTool.inlet)
      testDrive.askForState[DriveState].pendingConnections should have size 2
      //Send BuildDrive
      testPumping.send(testDrive, Msg.BuildDrive)
      //Test connecting
      val addConnection = otherDriver.expectMsgType[Msg.AddConnection]
      addConnection.initiator     shouldEqual testDrive
      addConnection.inletId       shouldEqual inlet.pipeId
      addConnection.outlet.pipeId shouldEqual outlet.pipeId
      otherDriver.send(testDrive, Msg.ConnectTo(addConnection.initiator, outlet.pipeId, inlet))
      val connectionAdded = otherDriver.expectMsgType[Msg.ConnectionAdded]
      connectionAdded.inletId  shouldEqual inlet.pipeId
      connectionAdded.outletId shouldEqual outlet.pipeId
      otherDriver.send(testDrive, Msg.PipesConnected(inlet.pipeId, outlet.pipeId))
      //Test disconnecting
      val disconnectFrom = otherDriver.expectMsgType[Msg.DisconnectFrom]
      disconnectFrom.initiator    shouldEqual testDrive
      disconnectFrom.inletId     shouldEqual inlet.pipeId
      disconnectFrom.outlet.pipeId shouldEqual outlet
      otherDriver.send(testDrive, Msg.ConnectionDeleted(inlet.pipeId, outlet.pipeId))
      //Expect DriveBuilt
      testPumping.expectMsg(Msg.DriveBuilt)
      sleep(500.millis) //Wait for processing of PipesConnected by testTool
      testDrive.askForState[DriveState].pendingConnections should have size 0}
    "by StartDrive, run user init function and reply with DriveStarted" in new TestCase {
      //Preparing
      testTool
      testPumping.send(testDrive, Msg.BuildDrive)
      testPumping.expectMsg(Msg.DriveBuilt)
      testTool.isOnStartCalled shouldEqual false
      //Test
      testPumping.send(testDrive, Msg.StartDrive)
      testPumping.expectMsg(Msg.DriveStarted)
      testTool.isOnStartCalled shouldEqual true}
    "by StopDrive, run user stopping function and reply with DriveStopped" in new TestCase {
      //Preparing
      testTool
      testPumping.send(testDrive, Msg.BuildDrive)
      testPumping.expectMsg(Msg.DriveBuilt)
      testPumping.send(testDrive, Msg.StartDrive)
      testPumping.expectMsg(Msg.DriveStarted)
      testTool.isOnStopCalled shouldEqual false
      //Test
      testPumping.send(testDrive, Msg.StopDrive)
      testPumping.expectMsg(Msg.DriveStopped)
      testTool.isOnStopCalled shouldEqual true}
    "by TerminateDrive, disconnect all connections and reply with DriveTerminated" in new TestCase {
      //Preparing
      val testOutlet = testTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
      val testInlet = testTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
      val otherOutlet = otherTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
      val otherInlet = otherTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
      testPumping.watch(testDrive)
      //Connecting
      testTool.inlet.plug(otherTool.outlet)
      testTool.outlet.attach(otherTool.inlet)
      testDrive.askForState[DriveState].pendingConnections should have size 2
      //Build
      testPumping.send(testDrive, Msg.BuildDrive)
      otherDriver.expectMsgType[Msg.ConnectTo].initiator shouldEqual testDrive
      otherDriver.send(testDrive, Msg.ConnectionAdded(testInlet.pipeId, otherOutlet.pipeId))
      otherDriver.expectMsgType[Msg.AddConnection].initiator shouldEqual testDrive
      otherDriver.send(testDrive, Msg.ConnectTo(testDrive, testOutlet.pipeId, otherInlet))
      otherDriver.expectMsgType[Msg.ConnectionAdded]
      otherDriver.send(testDrive, Msg.PipesConnected(otherInlet.pipeId, testOutlet.pipeId))
      testPumping.expectMsg(Msg.DriveBuilt)
      //Start and stop
      testPumping.send(testDrive, Msg.StartDrive)
      testPumping.expectMsg(Msg.DriveStarted)
      testPumping.send(testDrive, Msg.StopDrive)
      testPumping.expectMsg(Msg.DriveStopped)
      //Terminate
      testPumping.send(testDrive, Msg.TerminateDrive)
      //First disconnect inlets of testTool
      val delInlet = otherDriver.expectMsgType[Msg.DelConnection]
      delInlet.initiator    shouldEqual testDrive
      delInlet.outletId     shouldEqual otherOutlet.pipeId
      delInlet.inlet.pipeId shouldEqual testInlet.pipeId
      otherDriver.send(testDrive, Msg.DisconnectFrom(testDrive, testInlet.pipeId, otherOutlet))
      val inletDeleted = otherDriver.expectMsgType[Msg.ConnectionDeleted]
      inletDeleted.outletId shouldEqual otherOutlet.pipeId
      inletDeleted.inletId  shouldEqual testInlet.pipeId
      otherDriver.send(testDrive, Msg.PipesDisconnected(testInlet.pipeId, otherOutlet.pipeId))
      //Second disconnect outlets of testTool
      val disoutletFrom = otherDriver.expectMsgType[Msg.DisconnectFrom]
      disoutletFrom.initiator    shouldEqual testDrive
      disoutletFrom.inletId     shouldEqual otherInlet.pipeId
      disoutletFrom.outlet.pipeId shouldEqual testOutlet.pipeId
      otherDriver.send(testDrive, Msg.ConnectionDeleted(testOutlet.pipeId, otherInlet.pipeId))
      //Terminated
      testPumping.expectMsg(Msg.DriveTerminated)
      val t = testPumping.expectMsgType[Terminated]().actor shouldEqual testDrive}
  }
  "on connect and disconnect" should{
    "connect to another drive" in new TestCase {




     ???



    }
//    "disconnect from another drive" in new TestCase {
//
//    }
//    "not connect pipes in Stopping or Terminating mode" in new TestCase {
//
//    }
  }
//  "on user message" should{
//    "by UserData, do pour of UserMessage to all connected drives" in new TestCase {
//
//    }
//    "by UserMessage, put user message to queue and reply with DriveLoad" in new TestCase {
//
//    }
//    "dequeue user message and call user message handling function" in new TestCase {
//
//    }
//    "on Terminate after disconnect inlets, handle all usr messages before disconnect outlets" in new TestCase {
//
//    }
//    "by DriveLoad, evaluate message handling timeout" in new TestCase {
//
//    }
//  }
}
