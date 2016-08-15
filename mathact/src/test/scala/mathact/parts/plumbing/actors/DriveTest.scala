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
import mathact.ActorTestSpec
import mathact.parts.{Tool, WorkbenchContext}
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
    //Helpers actors
    val testController = TestProbe("TestController_" + randomString())
    val testPumping = TestProbe("TestPumping_" + randomString())
    val testOtherDriver = TestProbe("TestOtherDriver_" + randomString())
    val testImpeller =  TestProbe("TestImpeller_" + randomString())
    //Test objects
    val testWorkbenchContext = new WorkbenchContext(system, testController.ref){
      override val pumping: ActorRef = testPumping.ref}
    val testTool = new Tool(testWorkbenchContext, "TestTool"){
      override private[mathact] val pump: Pump = new Pump(testWorkbenchContext, this, "TestTool", None){
        override private[mathact] val drive: ActorRef = system.actorOf(Props(
          new Drive(this,  "TestTool", testPumping.ref){



            //Здесь добавить доступ к состоянию актора


          }),
          "Drive_" + randomString())}}
    //Drive actor
    val drive = testTool.pump.drive



  }
  //Testing
  "on start and on end" should{
    "before BuildDrive, add new connections to pending list" in new TestCase {

    }
    "by BuildDrive, create connections from pending list and reply with DriveBuilt" in new TestCase {

    }
    "by StartDrive, run user init function and reply with DriveStarted" in new TestCase {

    }
    "by StopDrive, run user stopping function and reply with DriveStopped" in new TestCase {

    }
    "by TerminateDrive, disconnect all connections and reply with DriveTerminated" in new TestCase {

    }
  }
  "on connect and disconnect" should{
    "by ConnectPipes, connect to another drive " in new TestCase {

    }
    "by DisconnectPipes, disconnect from another drive " in new TestCase {

    }
  }
  "on user message" should{
    "by UserData, do push of UserMessage to all connected drives" in new TestCase {

    }
    "by UserMessage, put user message to queue and reply with DriveLoad" in new TestCase {

    }
    "dequeue user message and call user message handling function" in new TestCase {

    }
    "by DriveLoad, evaluate message handling timeout" in new TestCase {

    }
  }
}
