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

import akka.actor._
import akka.event.Logging
import mathact.parts.BaseActor
import mathact.parts.data.CtrlEvents.GetPumpingActor
import mathact.parts.data.{Sketch, StepMode, PumpEvents, CtrlEvents}
import mathact.parts.gui.{SelectSketchWindow, SketchControlWindow}
import mathact.parts.gui.frame.Frame
import mathact.parts.plumbing.actors.Pumping

import scala.util.{Success, Failure}
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{LinearGradient, Stops}
import scalafx.scene.text.Text
import scalafx.stage.Stage

/** Workbench application controller
  * Created by CAB on 21.05.2016.
  */

class WorkbenchController(sketch: Sketch, mainController: ActorRef) extends BaseActor{
  //Enums
  object State extends Enumeration {val Creating, Starting, Work, Stopping, Failing, Ended  = Value}
  //Messages
  case object HitWindowClose
  case class ErrorStop(error: Throwable)
  //Variables
  var state: State.Value = State.Creating

  //UI definitions
  val uiSketchControl = new SketchControlWindow(log){
    def hitStart(): Unit = {self ! CtrlEvents.HitStart}
    def hitStop(): Unit = {self ! CtrlEvents.HitStop}
    def hitStep(): Unit = {self ! CtrlEvents.HitStep}
    def setSpeed(value: Double) = {self ! CtrlEvents.SetSpeed(value)}
    def switchMode(newMode: StepMode) = {self ! CtrlEvents.SwitchMode(newMode)}
    def windowClosed(): Unit = {self ! HitWindowClose}}
  //Actors
  val pumping = context.actorOf(Props(new Pumping(self)), "Pumping_" + sketch.className)
  context.watch(pumping)
  //Messages handling
  reaction(state){
    //Return of Pumping
    case GetPumpingActor ⇒
      sender ! pumping
    //Handling of starting
    case CtrlEvents.WorkbenchControllerStart if state == State.Creating ⇒
      //Show sketch UI
      tryToRun{uiSketchControl.init()} match{
        case Success(_) ⇒
          //Starting of Pumping
          pumping ! CtrlEvents.PumpingStart
          state = State.Starting
        case Failure(e) ⇒
          //Fail on create UI
          self ! ErrorStop(e)}


       //!!!Далее здесь,
       // 1) Сообщение о готовности от Pumping
       // 2) Активизация UI.
       // 3) Если автозапуск установлен, посилка сообщения CtrlEvents.HitStart
       // 4) Обработка ошибок и завершения работы pumping-га



    //UI events passed to Pumping
    case CtrlEvents.HitStart if state == State.Work ⇒
      pumping ! CtrlEvents.HitStart
    case CtrlEvents.HitStop if state == State.Work ⇒
      pumping ! CtrlEvents.HitStop
    case CtrlEvents.HitStep if state == State.Work ⇒
      pumping ! CtrlEvents.HitStep
    case CtrlEvents.SetSpeed(v) if state == State.Work ⇒
      pumping ! CtrlEvents.SetSpeed
    case CtrlEvents.SwitchMode(v) if state == State.Work ⇒
      pumping ! CtrlEvents.SwitchMode
    //PumpingError(error: Throwable)
    case CtrlEvents.PumpingError(error) ⇒

      //!!! Здесь обработка ошибки Pumping


    //Window closing
    case HitWindowClose ⇒
      state = State.Stopping

      //!!! Здесь код для случая нормаьлного завершения

      mainController ! CtrlEvents.SketchDone(sketch.className)
    //Error stop
    case ErrorStop(error) ⇒
      state = State.Failing

      //!!! Здесь код для случая аварийного завершения

      mainController ! CtrlEvents.SketchError(sketch.className, error)

    //Stop workbench controller
    case CtrlEvents.StopWorkbenchController ⇒
      state = State.Ended

      //!!! Здесь освобожение ресурсов, в зависимости от состояния

      tryToRun{uiSketchControl.hide()}
      self ! PoisonPill



    //Terminated of Pumping
    case Terminated(actor) ⇒


//      logMsgD("CtrlEvents.Terminated", s"Terminated actor: $actor", state)
//      (pumping == actor, state) match{
//        case true ⇒ self ! ErrorStop(new Exception("[CtrlEvents.Terminated] Pumping actor "))
//
//      }
//      currentSketch.filter(_.controller.contains(actor)).foreach{ _ ⇒
//        logMsgE("MainController.SketchStartTimeout", s"Timeout: $sketchStartTimeout, currentSketch: $currentSketch")
//        setCurrentSketchState(SketchStatus.Failed)
//        currentSketch = None
//        self ! ShowUI}





      //Run pumping
//      pumping ! PumpEvents.PlumbingInit(StepMode(uiSketchControl.defaultStepMode))
//
//    case PumpEvents.PlumbingStarted ⇒
//      logMsgD("WorkbenchController.PlumbingStarted", "")
//
//      uiSketchControl.setStatus("Ready to go!")
//      uiSketchControl.setEnabled(true)
//
//
//
//
//
//      //Далее здесь должна выполнятся создание UI и иницализация инсрументов. И вынести MainWindowStage в наружу.
//      //Нужно найти способ как просто взаимодействаовать с Stage-им.
//
//      println("[WorkbenchController] Receive: MainControllerStart")

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
//      println("[WorkbenchController] Receive: DoStop")
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
//  override def postStop(): Unit = {
//    log.info(s"[WorkbenchController.postStop] Call doStop with exit code: $exitCode")
//    doStop(exitCode)}

}
