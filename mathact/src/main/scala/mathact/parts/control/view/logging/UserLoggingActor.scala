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

import javafx.scene.Parent

import akka.actor.{ActorRef, PoisonPill}
import mathact.parts.ActorBase
import mathact.parts.gui.JFXInteraction
import mathact.parts.model.config.UserLoggingConfigLike
import mathact.parts.model.messages.M

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafxml.core.{NoDependencyResolver, FXMLLoader}


/** Logging to user UI console
  * Created by CAB on 26.08.2016.
  */

class UserLoggingActor(
  config: UserLoggingConfigLike,
  workbenchController: ActorRef)
extends ActorBase with JFXInteraction {
  //Parameters
  val windowTitle = "MathAct - Logger"
  val uiFxmlPath = "mathact/userLog/ui.fxml"
  //Variables
  private var logRows = List[LogRow]()
  //Construction
  private val (window, controller) = runNow{
    //Try to load resource
    Option(getClass.getClassLoader.getResource(uiFxmlPath)) match{
      case Some(conf) ⇒
        //Load FXML
        val loader = new FXMLLoader(
          getClass.getClassLoader.getResource(uiFxmlPath),
          NoDependencyResolver)
        loader.load()
        //Get view and controller
        val view = loader.getRoot[Parent]
        val controller = loader.getController[UserLogUIControllerLike]
        //Create stage
        val stg = new Stage {
          title = windowTitle
          scene = new Scene(view)}
        //Set params and return
        stg.resizable = true
        stg.sizeToScene()
        (stg, controller)
      case None ⇒
        throw new IllegalArgumentException(
          s"[UserLoggingActor.<init>] Cannot load FXML by '$uiFxmlPath path.'")}}
  //Functions
  private def addRow(row: LogRow): Unit = {
    //Add new row
    logRows +:= row
    //Preparing rows to show
    val rowsToShow = logRows.reverse

    //TODO Здесь примеение фильтров

    //Show rows
    runAndWait(controller.setRows(rowsToShow))

  }

  //TODO Далле:
  //TODO   1) Реализовать фильтрацию и очитску (соглсно понели инструментоы)
  //TODO   2) Здесь и в остальных окнах добавить кастомный значёк окна.
  //TODO   3) Дописать тесты
  //TODO   4) Исправить WorkbenchController, согласно заментке там.
  //TODO
  //TODO
  //TODO
  //TODO





  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Show UI
    case M.ShowUserLoggingUI ⇒
      runAndWait(window.show())
      workbenchController ! M.UserLoggingUIChanged(isShow = true)
    //Hide UI
    case M.HideUserLoggingUI ⇒
      runAndWait(window.hide())
      workbenchController ! M.UserLoggingUIChanged(isShow = false)
    //Log info
    case M.LogInfo(toolId, toolName, message) ⇒
      //Build row
      val row = LogRow(LogMsgType.Info, toolName, message)
      //Add to Log
      addRow(row)
    //Log warning
    case M.LogWarning(toolId, toolName, message) ⇒
      //Build row
      val row = LogRow(LogMsgType.Warn, toolName, message)
      //Add to Log
      addRow(row)
    //Log error
    case M.LogError(toolId, toolName, error, message) ⇒
      //Build row
      val row = LogRow(LogMsgType.Error, toolName, message + (error match{
        case Some(e) ⇒
          "\n" +
          "Exception message: " + e.getMessage + "\n" +
          "Stack trace: \n      " + e.getStackTrace.mkString("\n      ")
        case None ⇒ ""}))
      //Add to Log
      addRow(row)
    //Terminate user logging
    case M.TerminateUserLogging ⇒
      runAndWait(window.close())
      workbenchController ! M.SketchUITerminated
      self ! PoisonPill}}
