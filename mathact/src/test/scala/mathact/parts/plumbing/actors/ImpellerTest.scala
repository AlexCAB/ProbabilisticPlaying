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
    val testDriver = TestProbe()
    //Impeller actor
    val impeller = system.actorOf(Props(new Impeller(testDriver.ref)),"Impeller")
    //Test tasks0
    val sleepSecondTask = (req: (FiniteDuration, String))⇒()⇒{
      println(s"[ImpellerTest.sleepSecondTask] run test task with ${req._1} sleep and '${req._2}' result.")
      sleep(req._1)
      req._2}}
  //Testing
  it should "run task on RunTask and response with TaskDone on end of execution" in new TestData {
    //Start task
    val runMsg = Msg.RunTask("test task 11", 10.seconds, sleepSecondTask((4.seconds, "test task 11")))
    impeller ! runMsg
    //Wait for end
    sleep(3.seconds) //Wait for processing
    val doneMsg = testDriver.expectMsgType[Msg.TaskDone[String]]
    doneMsg.name shouldEqual runMsg.name
  }
//  it should "run task on RunTask and send TaskTimeout with timeout interval" in new TestData {
//
//
//
//
//  }
//  it should "run task on RunTask and send TaskFailed if task end with error" in new TestData {
//
//
//
//
//  }
//  it should "run task on RunTask, terminate it by TerminateCurrentTask and send TaskFailed" in new TestData {
//
//
//
//
//  }





}
