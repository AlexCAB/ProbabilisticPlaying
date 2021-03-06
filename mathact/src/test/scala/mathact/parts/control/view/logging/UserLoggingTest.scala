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

package mathact.parts.control.view.logging

import akka.actor.Props
import akka.testkit.TestProbe
import mathact.parts.UIActorTestSpec
import mathact.parts.model.config.UserLoggingConfigLike
import mathact.parts.model.messages.M
import org.scalatest.Suite

import scala.concurrent.duration._


/** Testing of UserLogging actor
  * Created by CAB on 23.09.2016.
  */

class UserLoggingTest extends UIActorTestSpec {
  //Test model
  trait TestCase extends Suite{
    //Test config
    def newConfig(showOnErr: Boolean) = new UserLoggingConfigLike{
      val uiFxmlPath = "mathact/userLog/ui.fxml"
      val showUIOnError = showOnErr}
    //Helpers actors
    val workbenchController = TestProbe("TestWorkbenchController_" + randomString())
    //UI Actor
    def newUserLog(config: UserLoggingConfigLike) = system.actorOf(
      Props(new UserLoggingActor(config, workbenchController.ref)),
      "UserLogging_" + randomString())}
  //Testing
  "UserLogging" should{
    "log events" in new TestCase {
      //Preparing
      val userLog = newUserLog(newConfig(showOnErr = false))
      workbenchController.watch(userLog)
      //Show UI
      workbenchController.send(userLog, M.ShowUserLoggingUI)
      workbenchController.expectMsgType[M.UserLoggingUIChanged].isShow shouldEqual true
      sleep(2.second)
      //Log events
      workbenchController.send(userLog, M.LogInfo(
        toolId = Some(1001),
        toolName = "Tool 1",
        message = "Info message."))
      sleep(2.second)
      workbenchController.send(userLog, M.LogInfo(
        toolId = Some(1001),
        toolName = "Tool 1",
        message = "Long info message: \n" +
          "A dream written down with a date becomes a Goal. \n" +
          "A goal broken down into steps becomes a plan. \n" +
          "A plan backed by action makes your dreams come true. \n"))
      sleep(2.second)
      workbenchController.send(userLog, M.LogWarning(
        toolId = Some(1002),
        toolName = "Tool 2",
        message = "Warning message."))
      sleep(2.second)
      workbenchController.send(userLog, M.LogError(
        toolId = Some(1003),
        toolName = "Tool 3",
        error = Some(new Exception("Oops!!! But not worries, this is just a test :)")),
        message = "!!!Error message.!!!"))
      sleep(2.second)
      //Test close button
      workbenchController.send(userLog, M.LogInfo(None, "TESTING", "Click close button (X)."))
      sleep(2.second)
      workbenchController.expectMsgType[M.UserLoggingUIChanged].isShow shouldEqual false
      workbenchController.send(userLog, M.ShowUserLoggingUI)
      workbenchController.expectMsgType[M.UserLoggingUIChanged].isShow shouldEqual true
      //Test done
      workbenchController.send(userLog, M.LogInfo(None, "TESTING", "Test done, you can play with UI next 30 second"))
      sleep(30.second)  //Time for playing with UI
      //Terminate UI
      workbenchController.send(userLog, M.TerminateUserLogging)
      workbenchController.expectMsg(M.UserLoggingTerminated)
      workbenchController.expectTerminated(userLog)}
  }
}
