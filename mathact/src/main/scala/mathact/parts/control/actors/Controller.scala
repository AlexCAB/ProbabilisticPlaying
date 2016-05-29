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
import mathact.parts.control.CtrlEvents
import mathact.parts.gui.MainWindow
import mathact.parts.gui.frame.Frame
import mathact.parts.plumbing.PumpEvents

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


class Controller(pumping: ActorRef, doStop: Int⇒Unit) extends Actor with ActorUtils{
  //Objects
  val log = Logging.getLogger(context.system, this)
  val uiFrame = new MainWindow(log){
    def doStop(): Unit = {self ! CtrlEvents.DoStop}
    def hitStart(): Unit = {println("hitStart")}
    def hitStop(): Unit = {println("hitStep")}
    def hitStep(): Unit = {println("hitStep")}
    def setSpeed(value: Double) = {println("setSpeed: " + value)}
    def switchMode(newMode: StepMode) = {println("newMode: " + newMode)}}
  //Variables
  var exitCode = 0
  //Functions
  def doTerminate(exitCode: Int): Unit = {
    this.exitCode = exitCode
    self ! PoisonPill}
  //Messages handling
  def receive = {

    case CtrlEvents.DoStart ⇒
      logMsgD("Controller.DoStart", "Starting...")
      //Create main window
      tryToRun{uiFrame.init()}.getOrElse{doTerminate(exitCode = -1)}
      //Run pumping
      pumping ! PumpEvents.PlumbingInit(StepMode(uiFrame.defaultStepMode))

    case PumpEvents.PlumbingStarted ⇒
      logMsgD("Controller.PlumbingStarted", "")

      uiFrame.setStatus("Ready to go!")
      uiFrame.setEnabled(true)





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






    case CtrlEvents.FatalError(message) ⇒

      uiFrame.setStatus("Fatal error: " + message)

      uiFrame.setEnabled(false)

      //Здесь отображение сообщения и если пользователь выбрал завершение работы, то штатное заваершение,
      // иначе ничего не даелать




    case CtrlEvents.DoStop ⇒

      println("[Controller] Receive: DoStop")

      //Здесь остановка насосв, вызов процедур завершения инструментов и выход


      println()


      uiFrame.setEnabled(false)


      uiFrame.setStatus("Stopping...")

      doTerminate(exitCode = 0)



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
