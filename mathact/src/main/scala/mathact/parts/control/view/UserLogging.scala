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

package mathact.parts.control.view

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.actor.{PoisonPill, Actor, ActorRef}
import mathact.parts.ActorBase
import mathact.parts.gui.JFXInteraction
import mathact.parts.model.config.UserLoggingConfigLike
import mathact.parts.model.enums.SketchUIElement._
import mathact.parts.model.enums.SketchUiElemState._
import mathact.parts.model.enums._
import scalafx.Includes._
import mathact.parts.model.messages.M

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color._
import scalafx.scene.{Scene, Node}
import scalafx.scene.control._
import scalafx.scene.image.{ImageView, Image}
import scalafx.scene.layout.{VBox, StackPane, BorderPane, HBox}
import scalafx.scene.text.{TextFlow, Text}
import scalafx.stage.Stage


/** Logging to user UI console
  * Created by CAB on 26.08.2016.
  */

class UserLogging(
  config: UserLoggingConfigLike,
  workbenchController: ActorRef)
extends ActorBase with JFXInteraction {
  //Definitions
  case class LogRow(msgType: Image, toolName: String, message: String)
  //Window class
  private class Window extends Stage {
    //Definitions
//    class MWButton[I](elem: SketchUIElement, states: List[(I, Image)], action: (SketchUIElement,I)⇒Unit) extends Button{
//      //Variables
//      private var currentStates = states.head._1  //First state should be is always 'disable'
//      //Build images
//      val images = states.map{case (id, img) ⇒ (id, new ImageView{image = img})}.toMap
//      //Config
//      graphic = images(states.head._1)
//      disable = true
//      prefHeight = config.buttonsSize
//      prefWidth = config.buttonsSize
//      onAction = handle{
//        //Disable button
//        graphic = images(states.head._1)
//        disable = true
//        //Run action
//        action(elem,currentStates)}
//      //Methods
//      def setState(newState: I): Unit = {
//        graphic = images(newState)
//        currentStates = newState
//        disable = newState == states.head._1}}
//    class ButtonBox(spacing: Double, buttons: Seq[Node]) extends HBox(spacing){
//      alignment = Pos.Center
//      prefHeight = config.buttonsSize
//      prefWidth = config.buttonsSize * 3
//      padding = Insets(4.0, 4.0, 4.0, 4.0)
//      children = buttons}
//    //Functions
//    def actionTriggered(elem: SketchUIElement, state: SketchUiElemState): Unit = {
//      log.debug(s"[SketchUI.actionTriggered] Hit elem $elem in state: $state")
//      workbenchController ! M.SketchUIActionTriggered(elem, state)}
//    //Close operation
//    delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
//      def handle(event: WindowEvent): Unit = {
//        log.debug("[SketchUI.onCloseRequest] Close is hit, call windowClosed.")
//        workbenchController ! M.SketchUIActionTriggered(CloseBtn, Unit)
//        event.consume()}})
//    //UI Components
//    val logBtn = new MWButton[SketchUiElemState](
//      LogBtn,
//      List(ElemDisabled → config.logBtnD, ElemShow → config.logBtnS, ElemHide → config.logBtnH),
//      actionTriggered)
//    val visualisationBtn = new MWButton[SketchUiElemState](
//      VisualisationBtn,
//      List(ElemDisabled → config.visualisationBtnD, ElemShow → config.visualisationBtnS, ElemHide → config.visualisationBtnH),
//      actionTriggered)
//    val runBtn = new MWButton[SketchUiElemState](
//      RunBtn,
//      List(ElemDisabled → config.runBtnD, ElemEnabled → config.runBtnE),
//      actionTriggered)
//    val showAllToolsUiBtn = new MWButton[SketchUiElemState](
//      ShowAllToolsUiBtn,
//      List(ElemDisabled → config.showAllToolsUiD, ElemEnabled → config.showAllToolsUiE),
//      actionTriggered)
//    val hideAllToolsUiBtn = new MWButton[SketchUiElemState](
//      HideAllToolsUiBtn,
//      List(ElemDisabled → config.hideAllToolsUiBtnD, ElemEnabled → config.hideAllToolsUiBtnE),
//      actionTriggered)
//    val skipAllTimeoutTaskBtn = new MWButton[SketchUiElemState](
//      SkipAllTimeoutTaskBtn,
//      List(ElemDisabled → config.skipAllTimeoutTaskD, ElemEnabled → config.skipAllTimeoutTaskE),
//      actionTriggered)
//    val stopSketchBtn = new MWButton[SketchUiElemState](
//      StopSketchBtn,
//      List(ElemDisabled → config.stopSketchBtnD, ElemEnabled → config.stopSketchBtnE),
//      actionTriggered)
//    val stateString = new Text {
//      text = "???"
//      style = "-fx-font-size: 11pt;"}


    val textFlow = new TextFlow

    //UI
    title = "MathAct - Workbench"
    scene = new Scene {
      fill = White
      content = new BorderPane{
        top = new HBox {}

        //ObservableBuffer(sketchRows)

        center =

            new TableView[LogRow]{

              columnResizePolicy = TableView.UnconstrainedResizePolicy

              val msgTypeColumn = new TableColumn[LogRow, Image] {
                text = "Type"
                prefWidth = 50
//                style = "-fx-font-size: 13; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;"
                cellValueFactory = { d ⇒ new ObjectProperty(d.value, "type",  d.value.msgType)}}

              val toolNameColumn = new TableColumn[LogRow, String] {
                text = "Tool Name"
                prefWidth = 200
                style = "-fx-font-size: 12; -fx-alignment: CENTER-LEFT;"
                cellValueFactory = { d ⇒ new StringProperty(d.value, "toolName",  d.value.toolName)}}

              val messageColumn = new TableColumn[LogRow, String] {
                text = "Message"
                prefWidth = 600
                style = "-fx-font-size: 12; -fx-font-weight: bold; -fx-alignment: CENTER;"
                cellValueFactory = { d ⇒ new StringProperty(d.value, "message",  d.value.message)}
              }


//              val runBtnColumn = new TableColumn[SketchData, Button] {
//                text = "Run"
//                prefWidth = 42
//                style = "-fx-alignment: CENTER;"
//                cellValueFactory = { d ⇒ new ObjectProperty(d.value, "runBtn", d.value.runBtn)}
//                cellFactory = { d ⇒ new TableCell[SketchData, Button] {
//                  contentDisplay = ContentDisplay.GraphicOnly
//                  item.onChange{ (_,_,b) ⇒ graphic = b}}}}

              columns ++= Seq(msgTypeColumn, toolNameColumn, messageColumn)}


        }}}



  //Construction
  private val window = runNow{
    val stg = new Window
    stg.resizable = true
    stg.sizeToScene()
    stg}







  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Show UI
    case M.ShowUserLoggingUI ⇒

      runAndWait{

        val t1 = new Text()
        t1.setStyle("-fx-fill: #4F8A10;-fx-font-weight:bold;")
        t1.setText("Hi\n")
        window.textFlow.getChildren.add(t1)

        for (_ ← 0 to 10){
          val t2 = new Text()
          t2.setStyle("-fx-fill: #ff0000;-fx-font-weight:bold;")
          t2.setText("Bye\n")

          window.textFlow.getChildren.add(t2)

        }


      }


      runAndWait(window.show())
      workbenchController ! M.UserLoggingUIChanged(isShow = true)
    //Hide UI
    case M.HideUserLoggingUI ⇒
      runAndWait(window.hide())
      workbenchController ! M.UserLoggingUIChanged(isShow = false)
    //Log info
    case M.LogInfo(toolId, toolName, message) ⇒
      ???


    //Log warning
    case M.LogWarning(toolId, toolName, message) ⇒
      ???


    //Log error
    case M.LogError(toolId, toolName, error, message) ⇒
      ???


    //Terminate user logging
    case M.TerminateUserLogging ⇒
      runAndWait(window.close())
      workbenchController ! M.SketchUITerminated
      self ! PoisonPill}}