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
import org.scalatest.{Matchers, WordSpecLike, Suite}

import scala.concurrent.duration._


/** Testing of Drive actor
  * Created by CAB on 15.08.2016.
  */

class DriveTest extends ActorTestSpec{
  //Test data
  trait TestCase extends Suite{
    //Helpers definitions
    case object GetDriveState
    case class DriveState(
      outlets: Map[Int, Outlet[_]], // (Outlet ID, Outlet)
      inlets: Map[Int, Inlet[_]])   // (Inlet ID, Outlet)
    //Helpers actors
    val testController = TestProbe("TestController_" + randomString())
    val testPumping = TestActor("TestPumping_" + randomString())(self ⇒ {
      case Msg.NewDrive(toolPump, toolName, toolImage) ⇒
        println(s"[DriveTest.testPumping.NewDrive] Creating of drive for tool: $toolName")
        Right{
          system.actorOf(Props(
           new Drive(toolPump,  "TestTool", self){
             //Get actor state
             override def receive: PartialFunction[Any, Unit]  = {
               case GetDriveState ⇒ println("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR")

                 //Далее здесь, получение состояния драйва.

               case m ⇒ super.receive}}),
           "Drive_" + randomString())


      }})



    val testOtherDriver = TestProbe("TestOtherDriver_" + randomString())
    val testImpeller =  TestProbe("TestImpeller_" + randomString())
    //Test objects
    val testWorkbenchContext = new WorkbenchContext(system, testController.ref, testPumping.ref){
      override val pumping: ActorRef = testPumping.ref}
    val testTool = new Fitting{
      //Test Pump
      private[mathact] val pump: Pump = new Pump(testWorkbenchContext, this, "TestTool", None)
      //Test Pipes
      object TestPipe extends Outlet[Double] with Inlet[Double]{
        //Variables
        private var receivedValues = List[Double]()
        //Receive user message
        protected def pours(value: Double): Unit = synchronized{ receivedValues +:= value }
        //Test methods
        def getReceivedValues: List[Double] = synchronized{ receivedValues }}
      //Tool Outlet and Inlet
      lazy val testOutlet = Outlet(TestPipe)
      lazy val testInlet = Inlet(TestPipe)}
    //Drive actor
    val drive = testTool.pump.drive



  }
  //Testing
  "on start and on end" should{
    "adding of Outlet and Inlet" in new TestCase {
      //Preparing

      drive ! GetDriveState






    }




//    "before BuildDrive, add new connections to pending list" in new TestCase {
//
//
//
//
////      testPumping.send(drive, Msg.BuildDrive)
//
//
//
//
//
//    }
//    "by BuildDrive, create connections from pending list and reply with DriveBuilt" in new TestCase {
//       testPumping.send(drive, Msg.BuildDrive)
//    }
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
//    "by UserData, do push of UserMessage to all connected drives" in new TestCase {
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
