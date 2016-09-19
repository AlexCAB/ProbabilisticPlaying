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

package mathact.parts.control.infrastructure

import java.util.concurrent.ExecutionException
import javafx.stage.{Stage => jStage}

import akka.actor._
import akka.event.Logging
import com.typesafe.config.Config
import mathact.parts.control.ui.SketchUI
import mathact.parts.model.config.MainConfigLike
import mathact.parts.model.data.sketch.SketchData
import mathact.parts.model.enums.{SketchUIElement, SketchUiElemState, ActorState}
import mathact.parts.model.messages.{StateMsg, M, Msg}
import mathact.parts.bricks.WorkbenchContext
import mathact.parts.{WorkbenchLike, StateActorBase, ActorBase}
import mathact.parts.gui.SelectSketchWindow
import mathact.parts.gui.frame.Frame
import mathact.parts.plumbing.infrastructure.Pumping
import mathact.tools.Workbench

import scala.concurrent.Future
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

class WorkbenchController(
  val config: MainConfigLike,
  val sketchData: SketchData,
  val mainController: ActorRef,
  val sketchUi: ActorRef,
  val userLogging: ActorRef,
  val visualization: ActorRef,
  val pumping: ActorRef)
extends StateActorBase(ActorState.Creating) with WorkbenchControllerUIControl
with WorkbenchControllerBuilding with WorkbenchControllerUIActions
{ import ActorState._, SketchUIElement._
  //Values
  val sketchName = sketchData.sketchName.getOrElse(sketchData.className)

//  //Enums
//  object State extends Enumeration {val Creating, Starting, Work, Stopping, Failing, Ended  = Value}
  //Messages
  case class SketchBuilt(instance: WorkbenchLike) extends Msg
  case class SketchBuiltError(error: Throwable) extends Msg
  case object SketchBuiltTimeout extends Msg
  case object SketchDestructed extends Msg
 //  //Build
//  val initSketchUiState = SketchUIState(
//    isUiShown = true,
//    runBtnEnable = ! sketch.autorun,
//    showToolUiBtnEnable = true,
//    hideToolUiBtnEnable = true,
//    skipAllTimeoutProcBtnEnable = true,
//    stopBtnEnable = false,
//    logUiBtnEnable = true,
//    logUiBtnIsShow = sketch.showUserLogUi,
//    visualisationUiBtnEnable = true,
//    visualisationUiBtnIsShow = sketch.showVisualisationUi)
  //Variables
  var isStopping = false


  //TODO 1) SketchUI, бутет иметь кнопки:
  //TODO    "Скрыть UI всех тнструментов",
  //TODO    "Показать UI всех тнструментов",
  //TODO    "Пропусть функции с таймаутом",
  //TODO    "Остановить" - останавливает скетч но не закрывает окно (кнопка окна и закрывает и останавливает),
  //TODO    "Логировани" - показать/скрыть UI логирования
  //TODO    "Визуализация" - показать/скрыть UI визуализации
  //TODO 2) Сделать SketchUI (и затем остальные UI) акторами, чтобы использовать
  //TODO    обмен сообщениями вместо калбеков.
  //TODO
  //TODO
  //TODO
  //TODO


  //Functions












  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[(StateMsg, ActorState), Unit] = {
    //On WorkbenchControllerStart show all UI
    case (M.StartWorkbenchController, Creating) ⇒
      state = Creating
      showAllUi()
    //On WorkbenchControllerStart trigger shutdown process on any state
    case (M.StopWorkbenchController, _) ⇒
      isStopping = true
      state match{
        case BuildingFailed | Built ⇒
          state = Terminating
          terminateAllUi()
        case Working ⇒
          state = Stopping
          pumping ! M.StopPumping
        case _ ⇒}


   //TODO Повыносить в функции


  }
  /** Handling after reaction executed */
  def postHandling: PartialFunction[(Msg, ActorState), Unit] = {
    //Check if all UI showed, and if so switch to Building, and create Workbench instance
    case (_: M.SketchUIChanged | _: M.UserLoggingUIChanged | _: M.VisualizationUIChanged, Creating) ⇒
      isAllUiShowed match{
        case true ⇒ isStopping match{
          case false ⇒
            log.debug("[Creating] All UI showed, run sketch building.")
            state = Building
            sketchRunBuilding()
          case true ⇒
            log.debug("[Creating] All UI showed, but isStopping == true, hide UI")
            state = Terminating
            terminateAllUi()}
        case false ⇒
          log.debug(s"[Creating] Not all UI showed yet.")}
    //Sketch built, set Built state if no autorun or Starting else
    case (_: SketchBuilt, Building) ⇒ isStopping match{
      case false ⇒
        log.debug(s"[Building] Sketch built, autorun: ${sketchData.autorun}")
        sketchData.autorun match{
          case false ⇒ state = Built
          case true ⇒ state = Starting}
      case true ⇒
        log.debug(s"[Building] Sketch built, but isStopping == true, switch to Destructing")
        state = Destructing
        destructSketch()}
    //If receive SketchBuiltError or SketchBuiltTimeout in Building state, switch state to BuildingFailed
    case (_: SketchBuiltError | SketchBuiltTimeout, Building) ⇒
      state = BuildingFailed
    //If RunBtn hit switch to Starting state
    case (M.SketchUIActionTriggered(RunBtn, _), Built) ⇒
      state = Starting
    //If plumbing started set state Working
    case (M.PumpingStarted, Starting) ⇒ isStopping match{
      case false ⇒
        log.debug(s"[Starting] Sketch started.")
        state = Working
      case true ⇒
        log.debug(s"[Starting] Sketch started, but isStopping == true, stopping sketch")
        state = Stopping
        pumping ! M.StopPumping}
    //If pumping stopped, deconstruct sketch
    case (M.PumpingStopped, Stopping) ⇒
      state = Destructing
      destructSketch()
    //On SketchDestructed switch to Terminating state, hide all UI (start terminating process)
    case (SketchDestructed, Destructing) ⇒
      state = Terminating
      terminateAllUi()
    //If All UI terminated, response with WorkbenchControllerTerminated t main controller and  stop a self.
    case (M.SketchUITerminated | M.UserLoggingTerminated | M.VisualizationTerminated, Terminating) ⇒
      isAllUiTerminated match{
        case true ⇒
          log.debug(s"[Terminating] All UI terminated, stop a self.")
          mainController ! M.WorkbenchControllerTerminated
          self ! PoisonPill
        case false ⇒
          log.debug(s"[Terminating] Not all UI terminated yet.")}




    //TODO Повыносить в функции


  }
  /** Actor reaction on messages (not change the 'state' variable) */
  def reaction: PartialFunction[(Msg, ActorState), Unit] = {
    //From objects asks
    case (M.GetWorkbenchContext(sender), _) ⇒ sender ! getWorkbenchContext
    //UI showed/headed
    case (M.SketchUIChanged(isShow), _) ⇒ sketchUiChanged(isShow)
    case (M.UserLoggingUIChanged(isShow), _) ⇒ userLoggingUIChanged(isShow)
    case (M.VisualizationUIChanged(isShow), _) ⇒ visualizationUIChanged(isShow)
    //Sketch building
    case (SketchBuilt(sketchInstance), Building) ⇒ sketchBuilt(sketchInstance)
    case (SketchBuiltError(error), Building) ⇒ sketchBuiltError(error)
    case (SketchBuiltTimeout, state) ⇒ sketchBuiltTimeout(state)
    case (M.PumpingStarted, Starting) ⇒ pumpingStarted()
    //UI actions
    case (M.SketchUIActionTriggered(RunBtn, _), Built) ⇒ hitRunBtn()
    //UI terminated
    case (M.SketchUITerminated, _) ⇒ sketchUITerminated()
    case (M.UserLoggingTerminated, _) ⇒ userLoggingTerminated()
    case (M.VisualizationTerminated, _) ⇒ visualizationTerminated()







    //TODO Далее здесь обработка событий UI как: case (???, Working) ⇒ ???, в том числе закрытие окна.

    //Sketch shutdown

    //TODO Длалее обработка завершения работы







  }




















//  //UI definitions
//  val uiSketchControl = new SketchUI(log){
//    def hitStart(): Unit = ??? //{self ! M.HitStart}
//    def hitStop(): Unit =  ??? //{self ! M.HitStop}
//    def hitStep(): Unit =  ??? //{self ! M.HitStep}
//    def setSpeed(value: Double) =  ??? //{self ! M.SetSpeed(value)}
//    def switchMode(stepMode: StepMode) =  ??? //{self ! M.SwitchStepMode(stepMode)}
//    def windowClosed(): Unit = {self ! HitWindowClose}}
//  //Actors
//  val userLogging = context.actorOf(Props(new UserLogging(self)), "UserLogging_" + sketch.className)
//  context.watch(userLogging)
//  val visualization = context.actorOf(Props(new Visualization(self)), "UserLogging_" + sketch.className)
//  context.watch(visualization)
//  val pumping = context.actorOf(
//    Props(new Pumping(config.pumping, self,  ???, userLogging, visualization)),
//    "Pumping_" + sketch.className)
//  context.watch(pumping)
  //Messages handling
//  def reaction = {
//    //Init of workbench controller, creating of WorkbenchContext
////    case  M.WorkbenchControllerInit(workbenchSender) ⇒
////      workbenchSender ! Right{
////        new WorkbenchContext(context.system, mainController, pumping, config.pumping.pump, config.config)}
//    //Handling of starting
//    case M.WorkbenchControllerStart if state == State.Creating ⇒
//      //Show sketch UI
////      tryToRun{uiSketchControl.init()} match{
////        case Success(_) ⇒
////          //Starting of Pumping
////          ??? //pumping ! M.StartPumping(uiSketchControl.getInitWorkMode, uiSketchControl.getInitSpeed)
////          state = State.Starting
////        case Failure(e) ⇒
////          //Fail on create UI
////          self ! ErrorStop(e)}
//    //Pumping is ready
////    case M.PumpingStarted(workMode) if state == State.Starting ⇒
////      //Enable UI
////      tryToRun{uiSketchControl.setReady(workMode)} match{
////        case Success(_) ⇒
////          //If autorun then start Pumping
////
////          // Добавить autorun метод в Workbench DSL и получать его знаение, если аквтозапуск то посылка M.HitStart
////          // Также добавить в DSL методы ля установки InitWorkMode InitSpeed
////
////          //Update state
////          state = State.Work
////        case Failure(e) ⇒
////          self ! ErrorStop(e)}
////
////
////
////       //!!!Далее здесь,
////       // 1) Если автозапуск установлен, посилка сообщения M.HitStart
////       // 2) Обработка ошибок и завершения работы pumping-га
////       //    Нормальное завершение это саморазрушение (детектится по Terminated) в состоянии Stopping,
////       //    иначе аварийное завршение.
////       //    Аварийное завршение pumping-га приводи только к соответсвующему сообщению в UI логе. Для завершения
////       //    пользоватль всегда нажымает "закрыть" (чтобы он мог прочитаь лог).
////       // 3) Окно лога в UI должно быть многостроковым чтобы пользователь могпростматировать всю историю запуска
////       //    и выполнения скетча.
////       //    Так же нужно подсвечивать
////
////
////    //UI events passed to Pumping
////    case M.HitStart if state == State.Work ⇒
////      pumping ! M.HitStart
////    case M.HitStop if state == State.Work ⇒
////      pumping ! M.HitStop
////    case M.HitStep if state == State.Work ⇒
////      pumping ! M.HitStep
////    case M.SetSpeed(v) if state == State.Work ⇒
////      pumping ! M.SetSpeed(v)
////    case M.SwitchStepMode(v) if state == State.Work ⇒
////      pumping ! M.SwitchStepMode(v)
////    //Mode switched
////    case M.StepModeSwitched(stepMode) if state == State.Work ⇒
////      log.debug(s"[StepModeSwitched] Step mode updated to: $stepMode, enable UI.")
////      uiSketchControl.setReady(stepMode)
////    case M.PumpingStepDone if state == State.Work ⇒
////      log.debug(s"[PumpingStepDone] Enable UI step button.")
////      uiSketchControl.setStepButtonEnabled(true)
////    case M.PumpingStarted if state == State.Work ⇒
////      log.debug(s"[PumpingStepDone] Enable UI stop button.")
////      uiSketchControl.setStopButtonEnabled(true)
////    case M.PumpingStopped(stepMode) if state == State.Work ⇒
////      log.debug(s"[PumpingStopped] Enable UI run and step button, stepMode: $stepMode.")
////      uiSketchControl.setReady(stepMode)
////
////
////
////
////
////
////
////
////
////
////
////
////
////    //PumpingError(error: Throwable)
////    case M.PumpingError(error) ⇒
//
//      //!!! Здесь обработка ошибки Pumping
//
//
//    //Window closing
//    case HitWindowClose ⇒
//      state = State.Stopping
//
//      //!!! Здесь код для случая нормаьлного завершения
//
//      mainController ! M.SketchDone(sketch.className)
//    //Error stop
//    case ErrorStop(error) ⇒
//      state = State.Failing
//
//      //!!! Здесь код для случая аварийного завершения
//
//      mainController ! M.SketchError(sketch.className, error)
//
//    //Stop workbench controller
//    case M.StopWorkbenchController ⇒
//      state = State.Ended
//
//      //!!! Здесь освобожение ресурсов, в зависимости от состояния
//
////      tryToRun{uiSketchControl.hide()}
//      self ! PoisonPill
//
//
//
//    //Terminated of Pumping
//    case Terminated(actor) ⇒
//
//
////      logMD("M.Terminated", s"Terminated actor: $actor", state)
////      (pumping == actor, state) match{
////        case true ⇒ self ! ErrorStop(new Exception("[M.Terminated] Pumping actor "))
////
////      }
////      currentSketch.filter(_.controller.contains(actor)).foreach{ _ ⇒
////        logMsgE("MainController.SketchStartTimeout", s"Timeout: $sketchStartTimeout, currentSketch: $currentSketch")
////        setCurrentSketchState(SketchStatus.Failed)
////        currentSketch = None
////        self ! ShowUI}
//
//
//
//
//
//      //Run pumping
////      pumping ! PumpEvents.PlumbingInit(StepMode(uiSketchControl.defaultStepMode))
////
////    case PumpEvents.PlumbingStarted ⇒
////      logMsgD("WorkbenchController.PlumbingStarted", "")
////
////      uiSketchControl.setStatus("Ready to go!")
////      uiSketchControl.setEnabled(true)
////
////
////
////
////
////      //Далее здесь должна выполнятся создание UI и иницализация инсрументов. И вынести MainWindowStage в наружу.
////      //Нужно найти способ как просто взаимодействаовать с Stage-им.
////
////      println("[WorkbenchController] Receive: MainControllerStart")
//
////      Platform.runLater{
////
////        val w = new MainWindowStage
////
////        w.show()
////
////      }
//
//
//
//
//
////
////    case M.FatalError(message) ⇒
////
////      uiSketchControl.setStatus("Fatal error: " + message)
////
////      uiSketchControl.setEnabled(false)
//
//      //Здесь отображение сообщения и если пользователь выбрал завершение работы, то штатное заваершение,
//      // иначе ничего не даелать
//
//
//
//
////    case M.DoStop ⇒
////
////      println("[WorkbenchController] Receive: DoStop")
////
////      //Здесь остановка насосв, вызов процедур завершения инструментов и выход
////
////
////      println()
////
////
////      uiSketchControl.setEnabled(false)
////
////
////      uiSketchControl.setStatus("Stopping...")
////
////      doTerminate(exitCode = 0)
//
//
//
//
//
//
//  }




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
