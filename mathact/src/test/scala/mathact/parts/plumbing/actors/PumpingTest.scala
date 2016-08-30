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
import mathact.parts.ActorTestSpec
import mathact.parts.data.Msg
import mathact.parts.plumbing.Pump
import org.scalatest.Suite
import scala.concurrent.duration._


/** Testing of Pumping actor
  * Created by CAB on 30.08.2016.
  */

class PumpingTest extends ActorTestSpec{
  //Test data
  trait TestCase extends Suite{
    //Test controller and logger
    lazy val testController = TestProbe("TestSketchController_" + randomString())
    lazy val testUserLogging = TestProbe("UserLogging_" + randomString())
    //Test drives
    lazy val testDrive1 = TestProbe("TestDrive1_" + randomString())
    lazy val testDrive2 = TestProbe("TestDrive2_" + randomString())
    //Pumping
    object actors{
      lazy val pumping = system.actorOf(Props(
        new Pumping(testController.ref,  "TestSketch", testUserLogging.ref){
          //Variables
          private var nextDriveIndex = -1
          //Get actor state
          override def createDriveActor(toolPump: Pump, toolName: String): ActorRef  = {
            nextDriveIndex += 1
            List(testDrive1.ref, testDrive2.ref)(nextDriveIndex)}}),
        "Pumping_" + randomString())
      lazy val pumpingWithDrives = {
        testController.send(pumping, Msg.NewDrive(null, "TestTool1", None))
        testController.expectMsgType[Either[Throwable, ActorRef]].isRight shouldEqual true
        testController.send(pumping, Msg.NewDrive(null, "TestTool2", None))
        testController.expectMsgType[Either[Throwable, ActorRef]].isRight shouldEqual true
        pumping}
      lazy val startedPumpingWithDrives = {
        pumpingWithDrives
        testController.send(pumping, Msg.StartPumping)
        testDrive1.expectMsg(Msg.BuildDrive)
        testDrive1.send(pumping, Msg.DriveBuilt)
        testDrive2.expectMsg(Msg.BuildDrive)
        testDrive2.send(pumping, Msg.DriveBuilt)
        testDrive1.expectMsg(Msg.StartDrive)
        testDrive1.send(pumping, Msg.DriveStarted)
        testDrive2.expectMsg(Msg.StartDrive)
        testDrive2.send(pumping, Msg.DriveStarted)
        testController.expectMsg(Msg.PumpingStarted)
        pumpingWithDrives}}}
  //Testing
  "Pumping actor" should{
    "by Msg.NewDrive, create and return new drive actor" in new TestCase {
      //Create first drive
      testController.send(actors.pumping, Msg.NewDrive(null, "TestTool1", None))
      val drive1 = testController.expectMsgType[Either[Throwable, ActorRef]]
      drive1.isRight shouldEqual true
      drive1.right.get shouldEqual testDrive1.ref
      //Create second drive
      testController.send(actors.pumping, Msg.NewDrive(null, "TestTool2", None))
      val drive2 = testController.expectMsgType[Either[Throwable, ActorRef]]
      drive2.isRight shouldEqual true
      drive2.right.get shouldEqual testDrive2.ref}
    "by Msg.StartPumping, build and start all drives, response Msg.PumpingStarted" in new TestCase {
      //Preparing
      actors.pumpingWithDrives
      //Start
      testController.send(actors.pumping, Msg.StartPumping)
      //Build drives
      testDrive1.expectMsg(Msg.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive1.send(actors.pumping, Msg.DriveBuilt)
      testDrive2.expectMsg(Msg.BuildDrive)
      sleep(1.second) //Imitate some time required to build drive
      testDrive2.send(actors.pumping, Msg.DriveBuilt)
      //Start drives
      testDrive1.expectMsg(Msg.StartDrive)
      sleep(1.second) //Imitate some time required to start drive
      testDrive1.send(actors.pumping, Msg.DriveStarted)
      testDrive2.expectMsg(Msg.StartDrive)
      sleep(1.second) //Imitate some time required to start drive
      testDrive2.send(actors.pumping, Msg.DriveStarted)
      //Built
      testController.expectMsg(Msg.PumpingStarted)}
    "by Msg.StopPumping, stop and terminate all drives, response Msg.PumpingStopped and terminate" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      testController.watch(actors.startedPumpingWithDrives)
      //Start
      testController.send(actors.pumping, Msg.StopPumping)
      //Build drives
      testDrive1.expectMsg(Msg.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive1.send(actors.pumping, Msg.DriveStopped)
      testDrive2.expectMsg(Msg.StopDrive)
      sleep(1.second) //Imitate some time required to stop drive
      testDrive2.send(actors.pumping, Msg.DriveStopped)
      //Start drives
      testDrive1.expectMsg(Msg.TerminateDrive)
      sleep(1.second) //Imitate some time required to termination drive
      testDrive1.send(actors.pumping, Msg.DriveTerminated)
      testDrive2.expectMsg(Msg.TerminateDrive)
      sleep(1.second) //Imitate some time required to termination drive
      testDrive2.send(actors.pumping, Msg.DriveTerminated)
      //Built
      testController.expectMsg(Msg.PumpingStopped)
      //Terminate
      testController.expectTerminated(actors.startedPumpingWithDrives)}
    "by Msg.SkipTimeoutTask, send it to all drives" in new TestCase {
      //Preparing
      actors.startedPumpingWithDrives
      //Test
      testController.send(actors.pumping, Msg.SkipTimeoutTask)
      testDrive1.expectMsg(Msg.SkipTimeoutTask)
      testDrive2.expectMsg(Msg.SkipTimeoutTask)}
  }
}
