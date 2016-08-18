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

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import mathact.parts.plumbing.fitting.{Inlet, Outlet}
import mathact.parts.{TestActor, ActorTestSpec, Tool, WorkbenchContext}
import mathact.parts.data.Msg
import mathact.parts.plumbing.{Pump, Fitting}
import org.scalatest.Suite


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
      pendingConnections: List[Either[Msg.ConnectPipes, Msg.DisconnectPipes]])
    //Helpers actors
    val testController = TestProbe("TestController_" + randomString())
    val testPumping = TestActor("TestPumping_" + randomString())(self ⇒ {
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
    val testOtherDriver = TestActor("TestOtherDriver_" + randomString())(self ⇒ {
      case Msg.AddConnection(inletId, outlet) ⇒ ???
      case Msg.ConnectTo(inletId, outlet) ⇒ ???
      case Msg.DisconnectFrom(inletId, outlet) ⇒ ???
      case Msg.DelConnection(inletId, outlet) ⇒ ???

    })

    val testImpeller =  TestProbe("TestImpeller_" + randomString())
    //Test objects
    val testWorkbenchContext = new WorkbenchContext(system, testController.ref, testPumping.ref){
      override val pumping: ActorRef = testPumping.ref}
    val testTool = new Fitting{
      //Test Pump
      val pump: Pump = new Pump(testWorkbenchContext, this, "TestTool", None)
      //Test Pipes
      object TestPipe extends Outlet[Double] with Inlet[Double]{
        //Variables
        private var receivedValues = List[Double]()
        //Receive user message
        protected def drain(value: Double): Unit = synchronized{ receivedValues +:= value }
        //Test methods
        def getReceivedValues: List[Double] = synchronized{ receivedValues }}
      //Tool Outlet and Inlet
      lazy val testOutlet = Outlet(TestPipe)
      lazy val testInlet = Inlet(TestPipe)}
    //Drive actor
    val drive = testTool.pump.drive}
  //Testing
  "on start and on end" should{
    "adding of Outlet and Inlet" in new TestCase {
      //Preparing
      testTool.testOutlet
      testTool.testInlet
      val outletId1 = testTool.pump.addOutlet(new Outlet[Int]{}, Some("outletId1"))
      val inletId1 = testTool.pump.addInlet(new Inlet[Int]{protected def drain(value: Int): Unit = {}}, Some("inletId1"))
      //Testing
      val DriveState(outlets, inlets, _) = drive.askForState[DriveState]
      outlets should have size 2
      inlets should have size 2
      outlets.keys should contain (outletId1)
      inlets.keys should contain (inletId1)}
    "before BuildDrive, add new connections to pending list" in new TestCase {
      //Preparing
      val testOutlet1 = testTool.testOutlet
      val testInlet1 = testTool.testInlet
      val otherOutlet1 = new Outlet[Double]{}
      val otherInlet1 = new Inlet[Double]{protected def drain(value: Double): Unit = {}}
      //Connecting and disconnecting
      testTool.testOutlet.connectJack(otherInlet1)
      testTool.testInlet.connectPlug(otherOutlet1)
      testTool.testOutlet.disconnectJack(otherInlet1)
      testTool.testInlet.disconnectPlug(otherOutlet1)
      //Testing
      val pendingCon = drive.askForState[DriveState].pendingConnections
      pendingCon should have size 4
      pendingCon should contain (Left(Msg.ConnectPipes(()⇒testOutlet1, ()⇒otherInlet1)))
      pendingCon should contain (Left(Msg.ConnectPipes(()⇒otherOutlet1, ()⇒testInlet1)))
      pendingCon should contain (Right(Msg.DisconnectPipes(()⇒testOutlet1, ()⇒otherInlet1)))
      pendingCon should contain (Right(Msg.DisconnectPipes(()⇒otherOutlet1, ()⇒testInlet1)))}
    "by BuildDrive, create connections from pending list and reply with DriveBuilt" in new TestCase {



     ???




    }
//    "by StartDrive, run user init function and reply with DriveStarted" in new TestCase {
//
//    }
//    "by StopDrive, run user stopping function and reply with DriveStopped" in new TestCase {
//
//    }
//    "by TerminateDrive, disconnect all connections and reply with DriveTerminated" in new TestCase {
//
//    }
  }
//  "on connect and disconnect" should{
//    "by ConnectPipes, connect to another drive " in new TestCase {
//
//    }
//    "by DisconnectPipes, disconnect from another drive " in new TestCase {
//
//    }
//  }
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
//    "by DriveLoad, evaluate message handling timeout" in new TestCase {
//
//    }
//  }
}
