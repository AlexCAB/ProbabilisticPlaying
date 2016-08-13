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

import akka.actor.Props
import akka.testkit.TestProbe
import mathact.ActorTestSpec
import mathact.parts.data.Msg
import org.scalatest.Suite

import scala.concurrent.duration._


/** Testing of Impeller actor
  * Created by CAB on 11.08.2016.
  */

class ImpellerTest extends ActorTestSpec {
  //Test data
  trait TestData extends Suite{
    //Helpers actors
    val testDriver = TestProbe("TestDrive_" + randomString())
    //Impeller actor
    val impeller = system.actorOf(Props(new Impeller(testDriver.ref)), "Impeller_" + randomString())
    //Test tasks0
    val sleepTask = (req: (FiniteDuration, String))⇒()⇒{
      println(s"[ImpellerTest.sleepTask] Run test task with ${req._1} sleep and '${req._2}' result.")
      sleep(req._1)
      req._2}
    val errorTask = (req: (FiniteDuration, Throwable))⇒()⇒{
      println(s"[ImpellerTest.errorTask] Error after ${req._1} sleep.")
      sleep({req._1})
      throw req._2}}
  //Testing
  it should "run task on RunTask and response with TaskDone on end of execution" in new TestData {
    //Run 3 tasks
    (1 to 3).foreach{ i ⇒
      //Start task
      val result = randomString()
      val runMsg = Msg.RunTask(s"test task 1 number: $i", 10.seconds, sleepTask((2.seconds, result)))
      testDriver.send(impeller, runMsg)
      //Wait for end
      sleep(1.seconds) //Wait for processing
      val doneMsg = testDriver.expectMsgType[Msg.TaskDone]
      println(s"[ImpellerTest] doneMsg: $doneMsg")
      doneMsg.name shouldEqual runMsg.name
      doneMsg.taskRes shouldEqual result}}
  it should "run only one task at the time" in new TestData {
    //Start first task
    val firstTask = Msg.RunTask(randomString(), 10.seconds, sleepTask((5.seconds, randomString())))
    testDriver.send(impeller, firstTask)
    //Start second task
    sleep(1.seconds)
    val secondTask = Msg.RunTask(randomString(), 10.seconds, sleepTask((5.seconds, randomString())))
    testDriver.send(impeller,  secondTask)
    //Error of second task
    val failedMsg = testDriver.expectMsgType[Msg.TaskFailed]
    failedMsg.name shouldEqual secondTask.name
    println(s"[ImpellerTest] failedMsg: $failedMsg")
    //Done of second task
    sleep(3.seconds)
    testDriver.expectMsgType[Msg.TaskDone].name shouldEqual firstTask.name}
  it should "run task on RunTask and send TaskTimeout with timeout interval" in new TestData {
    //Start task
    val result = randomString()
    val runMsg = Msg.RunTask(randomString(), 4.seconds, sleepTask((10.seconds, result)))
    testDriver.send(impeller, runMsg)
    //Test first timeout message
    sleep(4.seconds) //Wait for processing
    val timeoutMsg1 = testDriver.expectMsgType[Msg.TaskTimeout]
    timeoutMsg1.name shouldEqual runMsg.name
    timeoutMsg1.timeFromStart >= 4.seconds shouldEqual true
    //Test second timeout message
    sleep(4.seconds) //Wait for processing
    val timeoutMsg2 = testDriver.expectMsgType[Msg.TaskTimeout]
    timeoutMsg2.name shouldEqual runMsg.name
    timeoutMsg2.timeFromStart >= 8.seconds shouldEqual true
    //Test done message
    sleep(2.seconds) //Wait for processing
    val doneMsg = testDriver.expectMsgType[Msg.TaskDone]
    doneMsg.name shouldEqual runMsg.name
    doneMsg.taskRes shouldEqual result}
  it should "run task on RunTask and send TaskFailed if task end with error" in new TestData {
    //Start task
    val error = new Exception("[ImpellerTest] Ooops!")
    val runMsg = Msg.RunTask(randomString(), 10.seconds, errorTask((4.seconds, error)))
    testDriver.send(impeller, runMsg)
    //Test done message
    sleep(3.seconds) //Wait for processing
    val failedMsg = testDriver.expectMsgType[Msg.TaskFailed]
    failedMsg.name shouldEqual runMsg.name
    failedMsg.error shouldEqual error}
  it should "run task on RunTask, terminate it by SkipCurrentTask and send TaskFailed" in new TestData {
    //Start to terminate task
    val toTermTaskMsg = Msg.RunTask(randomString(), 10.seconds, sleepTask((20.seconds, randomString())))
    testDriver.send(impeller, toTermTaskMsg)
    //Terminate
    testDriver.send(impeller, Msg.SkipCurrentTask)
    val failedMsg = testDriver.expectMsgType[Msg.TaskFailed]
    failedMsg.name shouldEqual toTermTaskMsg.name
    println(s"[ImpellerTest] failedMsg: $failedMsg")
    //Start new task
    val newTaskMsg = Msg.RunTask(randomString(), 10.seconds, sleepTask((4.seconds, randomString())))
    testDriver.send(impeller, newTaskMsg)
    //Normal compilation
    sleep(3.seconds) //Wait for processing
    val doneMsg = testDriver.expectMsgType[Msg.TaskDone]
    doneMsg.name shouldEqual newTaskMsg.name}
}
