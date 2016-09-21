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

package mathact.parts.control.ui

import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.actor.{PoisonPill, ActorRef}
import akka.event.LoggingAdapter
import mathact.parts.ActorBase
import mathact.parts.gui.JFXInteraction
import mathact.parts.model.config.{SketchUIConfigLike, MainConfigLike}
import mathact.parts.model.enums.{SketchUiElemState, StepMode}
import mathact.parts.model.messages.M

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ComboBox, Slider}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.paint.Color._
import scalafx.scene.text.Text
import scalafx.stage.Stage



/** The sketch (workbench) window
  * Created by CAB on 23.05.2016.
  */

class SketchUI(
  config: SketchUIConfigLike,
  workbenchController: ActorRef)
extends ActorBase with JFXInteraction {
  //Parameters
  val initSpeed = 10.0
  val initStepMode = StepMode.HardSynchro
  val speedSliderDiapason = 100.0
  val speedSliderStep = 0.5
  val buttonsSize = 25
  val sliderWidth = 200


  //Window class
  private class Window extends Stage {
    import SketchUiElemState._
    //Definitions
    class MWButton[I](states: List[(I, Image)])(action: I⇒Unit) extends Button{
      //Variables
      private var currentStates = states.head._1  //First state should be is always 'disable'
      //Build images
      val images = states.map{case (id, img) ⇒ (id, new ImageView{image = img})}.toMap
      //Config
      graphic = images(states.head._1)
      disable = true
      prefHeight = buttonsSize
      prefWidth = buttonsSize
      onAction = handle{
        //Disable button
        graphic = images(states.head._1)
        disable = true
        //
        action(currentStates)}
      //Methods
      def setState(newState: I): Unit = {
        graphic = images(newState)
        disable = newState == states.head._1}}
    //Variables
    private var oldSliderPos = initSpeed
    //Close operation
    delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
      def handle(event: WindowEvent): Unit = {
        log.debug("[SketchUI.onCloseRequest] Close is hit, call windowClosed.")
        //        windowClosed()
        event.consume()}})
    //UI Components
    val runBtnStates = List(
      ElemDisabled → config.runBtnD,
      ElemEnabled → config.runBtnE)
    val runBtn = new MWButton[SketchUiElemState](runBtnStates)(state ⇒ {

      println(state)

    })

    val showAllToolsUiBtnStates = List(
      ElemDisabled → config.showAllToolsUiD,
      ElemEnabled → config.showAllToolsUiE)
    val showAllToolsUiBtn = new MWButton[SketchUiElemState](showAllToolsUiBtnStates)(state ⇒ {

      println(state)

    })

    val hideAllToolsUiBtnStates = List(
      ElemDisabled → config.hideAllToolsUiBtnD,
      ElemEnabled → config.hideAllToolsUiBtnE)
    val hideAllToolsUiBtn = new MWButton[SketchUiElemState](hideAllToolsUiBtnStates)(state ⇒ {

      println(state)

    })


    val skipAllTimeoutTaskBtnStates = List(
      ElemDisabled → config.skipAllTimeoutTaskD,
      ElemEnabled → config.skipAllTimeoutTaskE)
    val skipAllTimeoutTaskBtn = new MWButton[SketchUiElemState](skipAllTimeoutTaskBtnStates)(state ⇒ {

      println(state)

    })

    val stopSketchBtnStates = List(
      ElemDisabled → config.stopSketchBtnD,
      ElemEnabled → config.stopSketchBtnE)
    val stopSketchBtn = new MWButton[SketchUiElemState](stopSketchBtnStates)(state ⇒ {

      println(state)

    })

    val logBtnStates = List(
      ElemDisabled → config.logBtnD,
      ElemShow → config.logBtnS,
      ElemHide → config.logBtnH)
    val logBtn = new MWButton[SketchUiElemState](logBtnStates)(state ⇒ {

      println(state)

    })

    val visualisationBtnStates = List(
      ElemDisabled → config.visualisationBtnD,
      ElemShow → config.visualisationBtnS,
      ElemHide → config.visualisationBtnH)
    val visualisationBtn = new MWButton[SketchUiElemState](visualisationBtnStates)(state ⇒ {

      println(state)

    })











//    val startBtn: MWButton = new MWButton("start_e.png", "start_d.png")({
//      startBtn.setEnabled(false)
//      stepBtn.setEnabled(false)
//      stepMode.disable = true
//      //      hitStart()
//    })
//    val stopBtn: MWButton = new MWButton("stop_e.png", "stop_d.png")({
//      stopBtn.setEnabled(false)
//      //      hitStop()
//    })
//    val stepBtn: MWButton = new MWButton("step_e.png", "step_d.png")({
//      stepBtn.setEnabled(false)
//      //      hitStep()
//    })
//    val speedSlider = new Slider{
//      min = 0
//      max = speedSliderDiapason
//      value = initSpeed
//      showTickLabels = true
//      showTickMarks = true
//      majorTickUnit = 20
//      minorTickCount = 2
//      blockIncrement = speedSliderDiapason / 10
//      prefHeight = buttonsSize
//      prefWidth = sliderWidth
//      disable = true
//      delegate.valueProperty.addListener{ (o: ObservableValue[_ <: Number], ov: Number, newVal: Number) ⇒
//        val rVal = (newVal.doubleValue() / speedSliderStep).toInt * speedSliderStep
//        rVal != oldSliderPos match{
//          case true ⇒
//            oldSliderPos = rVal
//          //            setSpeed(rVal)
//          case false ⇒}}}
//    val stepMode = new ComboBox[String]{
//      val options = ObservableBuffer(
//        "Hard synchronization",
//        "Soft synchronization",
//        "Asynchronously")
//      prefWidth = 170
//      prefHeight = buttonsSize
//      items = options
//      disable = true
//      delegate.getSelectionModel.select(options(initStepMode.id))
//      onAction = handle{
//        disable = true
//        startBtn.setEnabled(false)
//        stopBtn.setEnabled(false)
//        stepBtn.setEnabled(false)
//        speedSlider.disable = false
//        //        switchMode(StepMode(delegate.getSelectionModel.getSelectedIndex))
//      }}
    val stateString = new Text {
      text = "Starting..."
      style = "-fx-font-size: 11pt;"}
    //UI
    title = "MathAct - Workbench"
    scene = new Scene {
      fill = White
      content = new BorderPane{
        top = new HBox {
          alignment = Pos.Center
          children = Seq(
            new HBox(2) {
              alignment = Pos.Center
              prefHeight = buttonsSize
              prefWidth = buttonsSize * 3
              padding = Insets(8.0, 4.0, 4.0, 4.0)
              children = Seq(runBtn,showAllToolsUiBtn,hideAllToolsUiBtn,skipAllTimeoutTaskBtn,stopSketchBtn,logBtn,visualisationBtn)}//,

            //TODO Раставить кнопки красиво.

//            new HBox {
//              alignment = Pos.Center
//              padding = Insets(8.0, 4.0, 4.0, 4.0)
//              children = stepMode},
//            new HBox {
//              padding = Insets(8.0, 4.0, 4.0, 4.0)
//              alignment = Pos.Center
//              children = speedSlider}

          )}
        bottom = new HBox {
          style = "-fx-border-color: #808080; -fx-border-width: 1px; -fx-border-radius: 3.0; " +
            "-fx-border-insets: 2.0 2.0 2.0 2.0;"
          prefHeight
          padding = Insets(1.0)
          children = stateString}}}}



  //Construction
  private val window = runNow{
    val stg = new Window
    stg.resizable = false
    stg.sizeToScene()
    stg}



//  //      stg.hide()
//  stg.show()

  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Show UI
    case M.ShowSketchUI ⇒
      runAndWait(window.show())
      workbenchController ! M.SketchUIChanged(isShow = true)
    //








    //Hide UI
    case M.HideSketchUI ⇒
      runAndWait(window.hide())
      workbenchController ! M.SketchUIChanged(isShow = false)
    //Terminate UI
    case M.TerminateSketchUI ⇒
      runAndWait(window.close())
      workbenchController ! M.SketchUITerminated
      self ! PoisonPill}




  //TODO Это должен быть актор
  //TODO После нажания каждая кнопка блокируется, так чтобы небыло двойного нажатия, контроллер по завершении
  //TODO должен обновить состояние UI для разблокровки



  //Callbacks
//  def hitStart(): Unit
//  def hitStop(): Unit
//  def hitStep(): Unit
//  def setSpeed(value: Double): Unit
//  def switchMode(newMode: StepMode): Unit
//  def windowClosed(): Unit
  //Definitions

  //Variables
//  private var stage: Option[MainWindowStage] = None
//  //Methods
////  def init(): Unit = {
//    //Close old is exist
//    stage.foreach(stg ⇒ runAndWait(stg.close()))
//    //Create new
//    stage = Some(runNow{
//      val stg = new MainWindowStage
//      stg.resizable = false
//      stg.sizeToScene()
////      stg.hide()
//      stg.show()
//      stg})
//
////  }
//
//
//
//  def hide(): Unit = stage.foreach{ stg ⇒
//    runAndWait(stg.close())
//    stage = None}



  def getInitSpeed: Double = initSpeed
  def getInitWorkMode: StepMode = initStepMode
//  def setRun(isRan: Boolean): Unit = runAndWait{ stage.foreach{ stg ⇒ isRan match {
//    case true ⇒
//      stg.startBtn.setEnabled(false)
//      stg.stopBtn.setEnabled(true)
//      stg.stepBtn.setEnabled(false)
//    case false ⇒
//      stg.startBtn.setEnabled(true)
//      stg.stopBtn.setEnabled(false)
//      stg.stepBtn.setEnabled(true)}}}


//  def setDisabled(): Unit = runAndWait{ stage.foreach{ stg ⇒
//    stg.speedSlider.disable = true
//    stg.stepMode.disable = true
//    stg.startBtn.setEnabled(false)
//    stg.stopBtn.setEnabled(false)
//    stg.stepBtn.setEnabled(false)}}
//  def setReady(workMode: StepMode): Unit = runAndWait{ stage.foreach{ stg ⇒ workMode match{
//    case StepMode.HardSynchro | StepMode.SoftSynchro ⇒
//      stg.speedSlider.disable = false
//      stg.stepMode.disable = false
//      stg.startBtn.setEnabled(true)
//      stg.stopBtn.setEnabled(false)
//      stg.stepBtn.setEnabled(true)
//    case StepMode.Asynchro ⇒
//      stg.speedSlider.disable = false
//      stg.stepMode.disable = false
//      stg.startBtn.setEnabled(true)
//      stg.stopBtn.setEnabled(false)
//      stg.stepBtn.setEnabled(false)}}}
//
//
//  def setStepButtonEnabled(isEnabled: Boolean): Unit = runAndWait{stage.foreach{_.stepBtn.setEnabled(isEnabled)}}
//  def setStopButtonEnabled(isEnabled: Boolean): Unit = runAndWait{stage.foreach{_.stopBtn.setEnabled(isEnabled)}}
//
//
//
//
//  //TODO Переписать в логирование
//  def setStatus(status: String): Unit = runAndWait{ stage.foreach{ stg ⇒
//    stg.stateString.text = status}}






}

