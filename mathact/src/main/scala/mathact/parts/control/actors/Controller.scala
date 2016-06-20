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

import scala.util.Try
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{LinearGradient, Stops}
import scalafx.scene.text.Text
import scalafx.stage.Stage

/** Main application controller
  * Created by CAB on 21.05.2016.
  */

object Controller{
  //Enums
  object StepMode extends Enumeration {
    val Asynchronously, SoftSynchronization, HardSynchronization, None = Value}
  type StepMode = StepMode.Value}


//TODO !!! Удалить object Controlle, StepMode не нужен, управление будет выполнятся специальным сообщениями
//TODO в частности "запуск на асихронное выпоение", "остановка асихроного выполения" и "шаг" для жосткого и мягкого синхронного выполения.

class Controller(pumping: ActorRef, doStop: Int⇒Unit) extends Actor with ActorUtils{
  //Objects
  val log = Logging.getLogger(context.system, this)
  case object StartTimeOut
  //Variables
  var exitCode = 0
  //Functions
  def doTerminate(exitCode: Int): Unit = {
    this.exitCode = exitCode
    self ! PoisonPill}
  //UI definitions
//  val uiSelectSketch = new SelectSketchWindow(log){
//    def sketchSelected(index: Int): Unit = {
//
//      //!!! Может быть вызван более чем один раз
//
//      println("hitsSketchSelected")}
//    def windowClosed(): Unit = {self ! CtrlEvents.DoStop}
//  }


  val uiSketchControl = new SketchControlWindow(log){
    def windowClosed(): Unit = {println("hitWindowClosed")}
    def hitStart(): Unit = {println("hitStart")}
    def hitStop(): Unit = {println("hitStep")}
    def hitStep(): Unit = {println("hitStep")}
    def setSpeed(value: Double) = {println("setSpeed: " + value)}
    def switchMode(newMode: StepMode) = {println("newMode: " + newMode)}}
  //Run start timeout

  //Messages handling
  def receive = {
    //Handling of starting
    case CtrlEvents.DoStart(sketches) ⇒
      logMsgD("Controller.DoStart", s"Starting, sketches: $sketches")
      //Show select sketch UI

      //!!! Если гдето утановлен флажок автозапуска, список скетчей не отображается, выполняетс я сразу запуск омеченого скетча.

//      tryToRun{uiSelectSketch.show(sketches)}.getOrElse{doTerminate(exitCode = -1)}

      //Далее здесь:
      //1) Отображение списка скечей
      //2) По выбору запуск скеча (создаётся обьект класса скетчи из переданого списка)
      //3) По закрытию диалога SketchControlWindow (переименовать в SketchControlWindow) остановка скетчи, и возврат к списку
      //   где пользователь может выбрать другой скетч.







      //    println("main")
      //
      //    val clazz = sketchList.head
      //
      //    val name = clazz.getName
      //
      //    println(name)
      //
      //
      //    val const = clazz.getConstructors()(0)
      //
      //
      //    val instance = const.newInstance().asInstanceOf[Workbench]



      //Create main window
      //tryToRun{uiSketchControl.init()}.getOrElse{doTerminate(exitCode = -1)}
      //Run pumping
      pumping ! PumpEvents.PlumbingInit(StepMode(uiSketchControl.defaultStepMode))

    case PumpEvents.PlumbingStarted ⇒
      logMsgD("Controller.PlumbingStarted", "")

      uiSketchControl.setStatus("Ready to go!")
      uiSketchControl.setEnabled(true)





      //Далее здесь должна выполнятся создание UI и иницализация инсрументов. И вынести MainWindowStage в наружу.
      //Нужно найти способ как просто взаимодействаовать с Stage-им.

      println("[Controller] Receive: DoStart")

//      Platform.runLater{
//
//        val w = new MainWindowStage
//
//        w.show()
//
//      }





//
//    case CtrlEvents.FatalError(message) ⇒
//
//      uiSketchControl.setStatus("Fatal error: " + message)
//
//      uiSketchControl.setEnabled(false)

      //Здесь отображение сообщения и если пользователь выбрал завершение работы, то штатное заваершение,
      // иначе ничего не даелать




//    case CtrlEvents.DoStop ⇒
//
//      println("[Controller] Receive: DoStop")
//
//      //Здесь остановка насосв, вызов процедур завершения инструментов и выход
//
//
//      println()
//
//
//      uiSketchControl.setEnabled(false)
//
//
//      uiSketchControl.setStatus("Stopping...")
//
//      doTerminate(exitCode = 0)



    case x ⇒ println("[Controller] Receive: " + x)
  }




//  //Variables
//  private var workbenchStage: Option[WorkbenchStage] = None
//  //Methods
//  def init(primaryStage: jStage): Unit = {
//    //Create workbench stage
//    workbenchStage = Some(new WorkbenchStage(primaryStage))
//
//
//
//
//  }

  //On stop
  override def postStop(): Unit = {
    log.info(s"[Controller.postStop] Call doStop with exit code: $exitCode")
    doStop(exitCode)}}
