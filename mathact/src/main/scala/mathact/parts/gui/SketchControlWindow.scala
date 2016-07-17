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

package mathact.parts.gui

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.event.LoggingAdapter
import mathact.parts.data.{WorkMode, StepMode}

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Pos, Insets}
import scalafx.scene.Scene
import scalafx.scene.control.{ComboBox, Slider, Button}
import scalafx.scene.image.{ImageView, Image}
import scalafx.scene.layout._
import scalafx.scene.paint.Color._
import scalafx.scene.text.Text
import scalafx.stage.Stage
import scalafx.Includes._
import javafx.beans.value.ObservableValue



/** The sketch (workbench) window
  * Created by CAB on 23.05.2016.
  */

abstract class SketchControlWindow(log: LoggingAdapter) extends JFXInteraction {
  //Parameters
  val initSpeed = 10.0
  val initStepMode = WorkMode.HardSynchro
  val speedSliderDiapason = 100.0
  val speedSliderStep = 0.5
  val buttonsSize = 25
  val sliderWidth = 200
  //Callbacks
  def hitStart(): Unit
  def hitStop(): Unit
  def hitStep(): Unit
  def setSpeed(value: Double): Unit
  def switchMode(newMode: WorkMode): Unit
  def windowClosed(): Unit
  //Definitions
  private class MainWindowStage extends Stage {
    //Definitions
    class MWButton(eImgName: String, dImgName: String)(action: ⇒Unit) extends Button{
      //Function
      def loadImg(path: String): ImageView =
        new ImageView{image =  new Image(path, buttonsSize, buttonsSize, true, true)}
      //Images
      val eImg = loadImg(eImgName)
      val dImg = loadImg(dImgName)
      //Config
      graphic = dImg
      disable = true
      prefHeight = buttonsSize
      prefWidth = buttonsSize
      onAction = handle{action}
      //Methods
      def setEnabled(isEnabled: Boolean): Unit = isEnabled match{
        case true ⇒
          graphic = eImg
          disable = false
        case false ⇒
          graphic = dImg
          disable = true}}
    //Variables
    private var oldSliderPos = initSpeed
    //Close operation
    delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
      def handle(event: WindowEvent): Unit = {
        log.debug("[SketchControlWindow.onCloseRequest] Close is hit, call windowClosed.")
        windowClosed()
        event.consume()}})
    //UI Components
    val startBtn = new MWButton("start_e.png", "start_d.png")(hitStart())
    val stopBtn = new MWButton("stop_e.png", "stop_d.png")(hitStop())
    val stepBtn = new MWButton("step_e.png", "step_d.png")(hitStep())
    val speedSlider = new Slider{
      min = 0
      max = speedSliderDiapason
      value = initSpeed
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = 20
      minorTickCount = 2
      blockIncrement = speedSliderDiapason / 10
      prefHeight = buttonsSize
      prefWidth = sliderWidth
      disable = true
      delegate.valueProperty.addListener{ (o: ObservableValue[_ <: Number], ov: Number, newVal: Number) ⇒
        val rVal = (newVal.doubleValue() / speedSliderStep).toInt * speedSliderStep
        rVal != oldSliderPos match{
          case true ⇒
            oldSliderPos = rVal
            setSpeed(rVal)
          case false ⇒}}}
    val stepMode = new ComboBox[String]{
      val options = ObservableBuffer(
        "Hard synchronization",
        "Soft synchronization",
        "Asynchronously")
      prefWidth = 170
      prefHeight = buttonsSize
      items = options
      disable = true
      delegate.getSelectionModel.select(options(initStepMode.id))
      onAction = handle{
        disable = true
        startBtn.setEnabled(false)
        stopBtn.setEnabled(false)
        stepBtn.setEnabled(false)
        switchMode(WorkMode(delegate.getSelectionModel.getSelectedIndex))}}
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
             children = Seq(startBtn, stopBtn, stepBtn)},
            new HBox {
              alignment = Pos.Center
              padding = Insets(8.0, 4.0, 4.0, 4.0)
              children = stepMode},
            new HBox {
              padding = Insets(8.0, 4.0, 4.0, 4.0)
              alignment = Pos.Center
              children = speedSlider})}
        bottom = new HBox {
          style = "-fx-border-color: #808080; -fx-border-width: 1px; -fx-border-radius: 3.0; " +
            "-fx-border-insets: 2.0 2.0 2.0 2.0;"
          prefHeight
          padding = Insets(1.0)
          children = stateString}}}}
  //Variables
  private var stage: Option[MainWindowStage] = None
  //Methods
  def init(): Unit = {
    //Close old is exist
    stage.foreach(stg ⇒ runAndWait(stg.close()))
    //Create new
    stage = Some(runNow{
      val stg = new MainWindowStage
      stg.resizable = false
      stg.sizeToScene()
      stg.show()
      stg})}
  def hide(): Unit = stage.foreach{ stg ⇒
    runAndWait(stg.close())
    stage = None}
  def getInitSpeed: Double = initSpeed
  def getInitWorkMode: WorkMode = initStepMode
//  def setRun(isRan: Boolean): Unit = runAndWait{ stage.foreach{ stg ⇒ isRan match {
//    case true ⇒
//      stg.startBtn.setEnabled(false)
//      stg.stopBtn.setEnabled(true)
//      stg.stepBtn.setEnabled(false)
//    case false ⇒
//      stg.startBtn.setEnabled(true)
//      stg.stopBtn.setEnabled(false)
//      stg.stepBtn.setEnabled(true)}}}


  def setDisabled(): Unit = runAndWait{ stage.foreach{ stg ⇒
    stg.speedSlider.disable = true
    stg.stepMode.disable = true
    stg.startBtn.setEnabled(false)
    stg.stopBtn.setEnabled(false)
    stg.stepBtn.setEnabled(false)}}
  def setReady(workMode: WorkMode): Unit = runAndWait{ stage.foreach{ stg ⇒ workMode match{
    case WorkMode.HardSynchro | WorkMode.SoftSynchro ⇒
      stg.speedSlider.disable = false
      stg.stepMode.disable = false
      stg.startBtn.setEnabled(true)
      stg.stopBtn.setEnabled(false)
      stg.stepBtn.setEnabled(true)
    case WorkMode.Asynchro ⇒
      stg.speedSlider.disable = true
      stg.stepMode.disable = false
      stg.startBtn.setEnabled(true)
      stg.stopBtn.setEnabled(false)
      stg.stepBtn.setEnabled(false)}












  }}


  //TODO Переписать в логирование
  def setStatus(status: String): Unit = runAndWait{ stage.foreach{ stg ⇒
    stg.stateString.text = status}}}

