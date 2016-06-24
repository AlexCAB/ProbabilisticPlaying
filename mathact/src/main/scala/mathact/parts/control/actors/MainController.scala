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
import mathact.parts.data.{SketchStatus, Sketch, PumpEvents, CtrlEvents}
import mathact.parts.gui.{SelectSketchWindow, SketchControlWindow}
import mathact.parts.gui.frame.Frame

import scala.util.{Failure, Success, Try}
import scala.collection.mutable.{ListBuffer ⇒ MutList}
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{LinearGradient, Stops}
import scalafx.scene.text.Text
import scalafx.stage.Stage
import scala.concurrent.duration._


/** Application controller actor
  * Created by CAB on 20.06.2016.
  */

class MainController(doStop: Int⇒Unit) extends Actor with ActorUtils{
  //Parameters
  val sketchStartTimeout = 5.seconds
  //Objects
  val log = Logging.getLogger(context.system, this)
  //Messages
  case object ShowUI
  case class RunSketch(className: String)
  case object SketchStartTimeout
  case object DoStop                     //Normal stop
  case object DoErrorStop                //Stop by error
  case class DoTerminate(exitCode: Int)  //Free resources and terminate
  //UI definitions
  val uiSelectSketch = new SelectSketchWindow(log){
    def sketchSelected(sketchClassName: String): Unit = {self ! RunSketch(sketchClassName)}
    def windowClosed(): Unit = {self ! DoStop}}
  //Variables
  var sketches = List[Sketch]()
  var currentSketch: Option[(Sketch, Boolean)] = None  //(Sketch, isStarted)
  //Functions


  //Messages handling
  def receive = {
    //Handling of starting
    case CtrlEvents.DoStart(sketchList) ⇒
      logMsgD("MainController.DoStart", s"Starting, sketchList: $sketchList")
      sketches = sketchList
      //Check if there is autoruned
      sketchList.find(_.status == SketchStatus.Autorun) match{
        case Some(sketch) ⇒
          self ! RunSketch(sketch.clazz.getCanonicalName)
        case None ⇒
          self ! ShowUI}
    //Display UI
    case ShowUI ⇒
      logMsgD("MainController.ShowUI", s"Sketches: $sketches")
      tryToRun{uiSelectSketch.show(sketches)} match{
        case Success(_) ⇒
          logMsgD("MainController.DoStart", "UI is created.")
        case Failure(_) ⇒
          self ! DoErrorStop}
    //Run selected sketch
    case RunSketch(className) ⇒
      logMsgD("MainController.RunSketch", s"className: $className, currentSketch: $currentSketch")
      (currentSketch, sketches.find(_.clazz.getCanonicalName == className)) match{
        case (None, Some(sketch)) ⇒
          currentSketch = Some((sketch, false))
          //Start creating timeout
          context.system.scheduler.scheduleOnce(sketchStartTimeout, self, SketchStartTimeout)
          //Hid UI
          tryToRun{uiSelectSketch.hide()}
          //

          //!!! Далее здесь:
          // 1) Конструирование скетча в отдельном Future
          // 2) По завершении конструирования, currentSketch. isStarted == true
          // 3) Конструироване WorkbenchContext по запросу
          // 4) В случае ошибки констрирования сообщение об ошибке, котрое должно очистить ресурсы.
          // 5) В случае таймаута или ошибки WorkbenchContext должен быть разрушен и тображено




        case (Some((curSketch,_)), _) if curSketch.clazz.getCanonicalName != className ⇒
          logMsgW("MainController.RunSketch", s"Current sketch $curSketch not ended.")
        case (_, None) ⇒
          logMsgE("MainController.RunSketch", s"Not found sketch for className: $className")
        case _ ⇒}
    //Sketch start timeout
    case SketchStartTimeout ⇒
      logMsgD("MainController.SketchStartTimeout", s"Timeout: $sketchStartTimeout, currentSketch: $currentSketch")
      currentSketch match{
        case Some((sketch, false)) ⇒
          logMsgE("MainController.SketchStartTimeout", s"Sketch $sketch not started in $sketchStartTimeout")

          //!!! Перенести код ниже в отдельное сообщение, будет отрабатывть для всех случаев завершения работы скетча.

          //Set Failed status and clear current sketch
          sketches = sketches.map{
            case `sketch` ⇒ sketch.withStatus(SketchStatus.Failed)
            case s ⇒ s}

          //??? Здесь посылка сообщения о разрушении WorkbenchContext'у

          currentSketch = None
          //Show UI
          self ! ShowUI

        case _ ⇒}













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
