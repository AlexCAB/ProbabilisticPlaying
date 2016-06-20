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

package mathact.parts.control.actors

import javafx.stage.{Stage => jStage}

import akka.actor.{PoisonPill, ActorRef, Actor}
import akka.event.Logging
import mathact.parts.ActorUtils
import mathact.parts.control.actors.Controller.StepMode
import mathact.parts.data.{PumpEvents, CtrlEvents}
import mathact.parts.gui.{SelectSketchWindow, SketchControlWindow}
import mathact.parts.gui.frame.Frame

import scala.util.{Failure, Success, Try}
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{LinearGradient, Stops}
import scalafx.scene.text.Text
import scalafx.stage.Stage


/** Application controller actor
  * Created by CAB on 20.06.2016.
  */

class MainController(doStop: Int⇒Unit) extends Actor with ActorUtils{
  //Objects
  val log = Logging.getLogger(context.system, this)
  //Messages
  case object DoStop                     //Normal stop
  case object DoErrorStop                //Stop by error
  case class DoTerminate(exitCode: Int)  //Free resources and terminate
  //UI definitions
  val uiSelectSketch = new SelectSketchWindow(log){
    def sketchSelected(index: Int): Unit = {

      //!!! Может быть вызван более чем один раз

      println("hitsSketchSelected")}
    def windowClosed(): Unit = {self ! DoStop}
  }
  //Functions


  //Messages handling
  def receive = {
    //Handling of starting
    case CtrlEvents.DoStart(sketches) ⇒
      logMsgD("MainController.DoStart", s"Starting, sketches: $sketches")
      //Show select sketch UI

      //!!! Если гдето утановлен флажок автозапуска, список скетчей не отображается, выполняетс я сразу запуск омеченого скетча.

      tryToRun{uiSelectSketch.show(sketches)} match{
        case Success(_) ⇒
          logMsgD("MainController.DoStart", "UI is created.")



        case Failure(_) ⇒
          self ! DoErrorStop}


      //!!! Далее здесь:
      // 1) Логика выбора и конструирования екзкмпляра Workbench.
      // 2) Конструирование WorkbenchContext по запросу NewWorkbenchContext
      // 3) Лгика завершения работы Workbench, и подготовка к запуску нового Workbench.





    //Handling of Workbench errors
    case CtrlEvents.WorkbenchError(workbench, exception) ⇒
      logMsgE("MainController.WorkbenchError", s"Fatal error of ${workbench.getClass.getName}, exception: $exception")

      // Вызывается в случае ощибки конструирования Workbench (если было сконструировано,
      // то обработка ошибок выполняется контроллером Workbench)
      // Здесь логика разрушения Workbench и очистки ресурсов, и отобрадения списка скетчей
      // !!!Можно подсветить сбойный скетч красным в списке

    //Handling of stopping
    case DoStop ⇒
      logMsgD("MainController.DoStop", "Stopping of application.")

      //Здесь логика завершения работы активного Workbench, и очистка ресурсов.\

      self ! DoTerminate(0)
    case DoErrorStop ⇒
      logMsgE("MainController.DoErrorStop", "Error of application.")

      //Здесь логика аварийной остановки

      self ! DoTerminate(-1)
    case DoTerminate(exitCode) ⇒
      logMsgD("MainController.DoTerminate", s"Terminate of application, exitCode: $exitCode.")
      //Hide UI
      tryToRun{uiSelectSketch.hide()}
      //Call stop
      doStop(exitCode)
      self ! PoisonPill
    //Unknown message
    case x ⇒
      logMsgW("MainController", "Receive unknown message: " + x)}}
