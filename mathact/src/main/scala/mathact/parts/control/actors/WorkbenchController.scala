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
import mathact.parts.data.Msg.GetPumpingActor
import mathact.parts.data.{WorkMode, Sketch, StepMode, Msg}
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
    def hitStart(): Unit = {self ! Msg.HitStart}
    def hitStop(): Unit = {self ! Msg.HitStop}
    def hitStep(): Unit = {self ! Msg.HitStep}
    def setSpeed(value: Double) = {self ! Msg.SetSpeed(value)}
    def switchMode(stepMode: StepMode) = {self ! Msg.SwitchStepMode(stepMode)}
    def windowClosed(): Unit = {self ! HitWindowClose}}
  //Actors
  val pumping = context.actorOf(Props(new Pumping(self, sketch)), "Pumping_" + sketch.className)
  context.watch(pumping)
  //Messages handling
  reaction(state){
    //Return of Pumping
    case GetPumpingActor ⇒
      sender ! pumping
    //Handling of starting
    case Msg.WorkbenchControllerStart if state == State.Creating ⇒
      //Show sketch UI
      tryToRun{uiSketchControl.init()} match{
        case Success(_) ⇒
          //Starting of Pumping
          pumping ! Msg.StartPumping(uiSketchControl.getInitWorkMode, uiSketchControl.getInitSpeed)
          state = State.Starting
        case Failure(e) ⇒
          //Fail on create UI
          self ! ErrorStop(e)}
    //Pumping is ready
    case Msg.PumpingStarted(workMode) if state == State.Starting ⇒
      //Enable UI
      tryToRun{uiSketchControl.setReady(workMode)} match{
        case Success(_) ⇒
          //If autorun then start Pumping

          // Добавить autorun метод в Workbench DSL и получать его знаение, если аквтозапуск то посылка Msg.HitStart
          // Также добавить в DSL методы ля установки InitWorkMode InitSpeed

          //Update state
          state = State.Work
        case Failure(e) ⇒
          self ! ErrorStop(e)}



       //!!!Далее здесь,
       // 1) Если автозапуск установлен, посилка сообщения Msg.HitStart
       // 2) Обработка ошибок и завершения работы pumping-га
       //    Нормальное завершение это саморазрушение (детектится по Terminated) в состоянии Stopping,
       //    иначе аварийное завршение.
       //    Аварийное завршение pumping-га приводи только к соответсвующему сообщению в UI логе. Для завершения
       //    пользоватль всегда нажымает "закрыть" (чтобы он мог прочитаь лог).
       // 3) Окно лога в UI должно быть многостроковым чтобы пользователь могпростматировать всю историю запуска
       //    и выполнения скетча.
       //    Так же нужно подсвечивать


    //UI events passed to Pumping
    case Msg.HitStart if state == State.Work ⇒
      pumping ! Msg.HitStart
    case Msg.HitStop if state == State.Work ⇒
      pumping ! Msg.HitStop
    case Msg.HitStep if state == State.Work ⇒
      pumping ! Msg.HitStep
    case Msg.SetSpeed(v) if state == State.Work ⇒
      pumping ! Msg.SetSpeed(v)
    case Msg.SwitchStepMode(v) if state == State.Work ⇒
      pumping ! Msg.SwitchStepMode(v)
    //Mode switched
    case Msg.StepModeSwitched(stepMode) if state == State.Work ⇒
      log.debug(s"[StepModeSwitched] Step mode updated to: $stepMode, enable UI.")
      uiSketchControl.setReady(stepMode)
    case Msg.PumpingStepDone if state == State.Work ⇒
      log.debug(s"[PumpingStepDone] Enable UI step button.")
      uiSketchControl.setStepButtonEnabled(true)
    case Msg.PumpingStarted if state == State.Work ⇒
      log.debug(s"[PumpingStepDone] Enable UI stop button.")
      uiSketchControl.setStopButtonEnabled(true)
    case Msg.PumpingStopped(stepMode) if state == State.Work ⇒
      log.debug(s"[PumpingStopped] Enable UI run and step button, stepMode: $stepMode.")
      uiSketchControl.setReady(stepMode)













    //PumpingError(error: Throwable)
    case Msg.PumpingError(error) ⇒

      //!!! Здесь обработка ошибки Pumping


    //Window closing
    case HitWindowClose ⇒
      state = State.Stopping

      //!!! Здесь код для случая нормаьлного завершения

      mainController ! Msg.SketchDone(sketch.className)
    //Error stop
    case ErrorStop(error) ⇒
      state = State.Failing

      //!!! Здесь код для случая аварийного завершения

      mainController ! Msg.SketchError(sketch.className, error)

    //Stop workbench controller
    case Msg.StopWorkbenchController ⇒
      state = State.Ended

      //!!! Здесь освобожение ресурсов, в зависимости от состояния

      tryToRun{uiSketchControl.hide()}
      self ! PoisonPill



    //Terminated of Pumping
    case Terminated(actor) ⇒


//      logMsgD("Msg.Terminated", s"Terminated actor: $actor", state)
//      (pumping == actor, state) match{
//        case true ⇒ self ! ErrorStop(new Exception("[Msg.Terminated] Pumping actor "))
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
//    case Msg.FatalError(message) ⇒
//
//      uiSketchControl.setStatus("Fatal error: " + message)
//
//      uiSketchControl.setEnabled(false)

      //Здесь отображение сообщения и если пользователь выбрал завершение работы, то штатное заваершение,
      // иначе ничего не даелать




//    case Msg.DoStop ⇒
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
//    akkaLog.info(s"[WorkbenchController.postStop] Call doStop with exit code: $exitCode")
//    doStop(exitCode)}

}
