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

package mathact.parts.control.view.sketch

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.actor.{ActorRef, PoisonPill}
import mathact.parts.ActorBase
import mathact.parts.gui.JFXInteraction
import mathact.parts.model.config.SketchUIConfigLike
import mathact.parts.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.parts.model.messages.M

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.paint.Color._
import scalafx.scene.text.Text
import scalafx.scene.{Node, Scene}
import scalafx.stage.Stage


/** The sketch (workbench) window
  * Created by CAB on 23.05.2016.
  */

class SketchUIActor(
  config: SketchUIConfigLike,
  workbenchController: ActorRef)
extends ActorBase with JFXInteraction { import SketchUIElement._
 import SketchUiElemState._
  //Window class
  private class Window extends Stage {
    //Definitions
    class MWButton[I](elem: SketchUIElement, states: List[(I, Image)], action: (SketchUIElement,I)⇒Unit) extends Button{
      //Variables
      private var currentStates = states.head._1  //First state should be is always 'disable'
      //Build images
      val images = states.map{case (id, img) ⇒ (id, new ImageView{image = img})}.toMap
      //Config
      graphic = images(states.head._1)
      disable = true
      prefHeight = config.buttonsSize
      prefWidth = config.buttonsSize
      onAction = handle{
        //Disable button
        graphic = images(states.head._1)
        disable = true
        //Run action
        action(elem,currentStates)}
      //Methods
      def setState(newState: I): Unit = {
        graphic = images(newState)
        currentStates = newState
        disable = newState == states.head._1}}
    class ButtonBox(spacing: Double, buttons: Seq[Node]) extends HBox(spacing){
      alignment = Pos.Center
      prefHeight = config.buttonsSize
      prefWidth = config.buttonsSize * 3
      padding = Insets(4.0, 4.0, 4.0, 4.0)
      children = buttons}
    //Functions
    def actionTriggered(elem: SketchUIElement, state: SketchUiElemState): Unit = {
      log.debug(s"[SketchUI.actionTriggered] Hit elem $elem in state: $state")
      workbenchController ! M.SketchUIActionTriggered(elem, state)}
    //Close operation
    delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
      def handle(event: WindowEvent): Unit = {
        log.debug("[SketchUI.onCloseRequest] Close is hit, call windowClosed.")
        workbenchController ! M.SketchUIActionTriggered(CloseBtn, Unit)
        event.consume()}})
    //UI Components
    val logBtn = new MWButton[SketchUiElemState](
      LogBtn,
      List(ElemDisabled → config.logBtnD, ElemShow → config.logBtnS, ElemHide → config.logBtnH),
      actionTriggered)
    val visualisationBtn = new MWButton[SketchUiElemState](
      VisualisationBtn,
      List(ElemDisabled → config.visualisationBtnD, ElemShow → config.visualisationBtnS, ElemHide → config.visualisationBtnH),
      actionTriggered)
    val runBtn = new MWButton[SketchUiElemState](
      RunBtn,
      List(ElemDisabled → config.runBtnD, ElemEnabled → config.runBtnE),
      actionTriggered)
    val showAllToolsUiBtn = new MWButton[SketchUiElemState](
      ShowAllToolsUiBtn,
      List(ElemDisabled → config.showAllToolsUiD, ElemEnabled → config.showAllToolsUiE),
      actionTriggered)
    val hideAllToolsUiBtn = new MWButton[SketchUiElemState](
      HideAllToolsUiBtn,
      List(ElemDisabled → config.hideAllToolsUiBtnD, ElemEnabled → config.hideAllToolsUiBtnE),
      actionTriggered)
    val skipAllTimeoutTaskBtn = new MWButton[SketchUiElemState](
      SkipAllTimeoutTaskBtn,
      List(ElemDisabled → config.skipAllTimeoutTaskD, ElemEnabled → config.skipAllTimeoutTaskE),
      actionTriggered)
    val stopSketchBtn = new MWButton[SketchUiElemState](
      StopSketchBtn,
      List(ElemDisabled → config.stopSketchBtnD, ElemEnabled → config.stopSketchBtnE),
      actionTriggered)
    val stateString = new Text {
      text = "???"
      style = "-fx-font-size: 11pt;"}
    //UI
    title = "MathAct - Workbench"
    scene = new Scene {
      fill = White
      content = new BorderPane{
        top = new HBox {
          alignment = Pos.Center
          children = Seq(
            new ButtonBox(2, Seq(logBtn,visualisationBtn)),
            new ButtonBox(0, Seq(runBtn)),
            new ButtonBox(2, Seq(showAllToolsUiBtn,hideAllToolsUiBtn)),
            new ButtonBox(2, Seq(skipAllTimeoutTaskBtn,stopSketchBtn)))}
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
  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Show UI
    case M.ShowSketchUI ⇒
      runAndWait(window.show())
      workbenchController ! M.SketchUIChanged(isShow = true)
    //Update UI state
    case M.UpdateSketchUIState(newState) ⇒ newState.foreach{
      case (LogBtn, s) if s == ElemDisabled || s == ElemShow || s == ElemHide ⇒ runAndWait{
        window.logBtn.setState(s)}
      case (VisualisationBtn, s) if s == ElemDisabled || s == ElemShow || s == ElemHide ⇒ runAndWait{
        window.visualisationBtn.setState(s)}
      case (RunBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.runBtn.setState(s)}
      case (ShowAllToolsUiBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.showAllToolsUiBtn.setState(s)}
      case (HideAllToolsUiBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.hideAllToolsUiBtn.setState(s)}
      case (SkipAllTimeoutTaskBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒ runAndWait{
        window.skipAllTimeoutTaskBtn.setState(s)}
      case (StopSketchBtn, s)  if s == ElemDisabled || s == ElemEnabled ⇒runAndWait{
        window.stopSketchBtn.setState(s)}
      case (element, state) ⇒
        log.error(s"[SketchUI] Unknown combination of element: $element and state: $state")}
    //Update status string
    case M.SetSketchUIStatusString(message, color) ⇒ runAndWait{
      window.stateString.text = message
      window.stateString.fill = color}
    //Hide UI
    case M.HideSketchUI ⇒
      runAndWait(window.hide())
      workbenchController ! M.SketchUIChanged(isShow = false)
    //Terminate UI
    case M.TerminateSketchUI ⇒
      runAndWait(window.close())
      workbenchController ! M.SketchUITerminated
      self ! PoisonPill}}

