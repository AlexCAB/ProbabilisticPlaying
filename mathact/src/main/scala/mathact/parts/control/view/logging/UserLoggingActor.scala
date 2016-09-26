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

package mathact.parts.control.view.logging

import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.scene.input.{KeyCodeCombination, KeyEvent}

import akka.actor.{ActorRef, PoisonPill}
import mathact.parts.ActorBase
import mathact.parts.gui.JFXInteraction
import mathact.parts.model.config.UserLoggingConfigLike
import mathact.parts.model.messages.M

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{Clipboard, ClipboardContent, KeyCode, KeyCombination}
import scalafx.stage.Stage
import scalafxml.core.{NoDependencyResolver, FXMLLoader}


/** Logging to user UI console
  * Created by CAB on 26.08.2016.
  */

class UserLoggingActor(
  config: UserLoggingConfigLike,
  workbenchController: ActorRef)
extends ActorBase with JFXInteraction {
  //Parameters
  val windowTitle = "MathAct - Logger"

  //Definitions
  private case class LogRow(msgType: Image, toolName: String, message: String){
    val rowImage = new ImageView{image = msgType}

  }
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




//    val table = new TableView[LogRow]{
//      //Parameters
//      columnResizePolicy = TableView.UnconstrainedResizePolicy
//      //Columns
//      val msgTypeColumn = new TableColumn[LogRow, ImageView] {
//        text = "Type"
//        prefWidth = 50
//        style = "-fx-alignment: CENTER;"
//        cellValueFactory = { d ⇒ new ObjectProperty(d.value, "type",  d.value.rowImage)}
//        cellFactory = { _ ⇒
//          new TableCell[LogRow, ImageView] {
//            item.onChange { (_, _, img) ⇒ graphic = img}}}}
//      val toolNameColumn = new TableColumn[LogRow, String] {
//        text = "Tool Name"
//        prefWidth = 200
//        style = "-fx-font-size: 12; -fx-font-weight: bold; -fx-alignment: CENTER;"
//        cellValueFactory = { d ⇒ new StringProperty(d.value, "toolName",  d.value.toolName)}}
//      val messageColumn = new TableColumn[LogRow, String] {
//        text = "Message"
//        prefWidth = 600
//        style = "-fx-font-size: 12;"
//        cellValueFactory = { d ⇒ new StringProperty(d.value, "message",  d.value.message)}}
//      columns ++= Seq(msgTypeColumn, toolNameColumn, messageColumn)
//      //Copy to clipboard
//      onKeyPressed = new EventHandler[KeyEvent]{
//        val copyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.ControlAny)
//        def handle(e: KeyEvent): Unit = if(copyCombination.`match`(e)){
//          val item = selectionModel.value.getSelectedItem
//          val clipboard = new ClipboardContent
//          val text = item.toolName + "\t|\t" + item.message
//          clipboard.putString(text)
//          Clipboard.systemClipboard.setContent(clipboard)
//          log.debug("[UserLogging] Copy to clipboard: " + text)}}}





    //TODO Далее:
    //TODO   1) Перенести всю UI логику в контроллер (из калсаа Window и удалить его), не получится вынести синхронизацию
    //TODO      так как создвать нужно тоде в потоке UI.
    //TODO   2) Здесь только создание Stage (создани Scene, иньекция этого актора в контроллер), и логика.
    //TODO
    //TODO
    //TODO
    //TODO


//    val fxmlLoader = new FXMLLoader(
//      getClass.getClassLoader.getResource("mathact/userLog/ui.fxml"),
//      NoDependencyResolver)
//    fxmlLoader.load()
//
//    val view = fxmlLoader.getRoot[Parent]
//    val controller = fxmlLoader.getController[UserLogUIControllerLike]
//
//
//
//
//
//
//
//
//    //UI
//    title = "MathAct - Workbench"
//    scene = new Scene(config.view)



//      new Scene {
//      fill = White
//      content = new BorderPane{
//        top = new HBox {
//
//          //TODO Buttons
//
//        }
//        center = table}}



    //Methods
    def setRows(rows: List[LogRow]): Unit = {



//      table.items = ObservableBuffer(rows)



    }




  }
  //Variables
  private var logRows = List[LogRow]()
  //Functions
  private def addRow(row: LogRow): Unit = {
    //Add new row
    logRows +:= row
    //Preparing rows to show
    val rowsToShow = logRows.reverse

    //TODO Здесь примеение фильтров

    //Show rows
//    runAndWait(window.setRows(rowsToShow))

  }



  //Construction
  private val (window, controller) = runNow{
    //Try to load resource
    Option(getClass.getClassLoader.getResource(config.uiFxmlPath)) match{
      case Some(conf) ⇒
        //Load FXML
        val loader = new FXMLLoader(
          getClass.getClassLoader.getResource(config.uiFxmlPath),
          NoDependencyResolver)
        loader.load()
        //Get view and controller
        val view = loader.getRoot[Parent]
        val controller = loader.getController[UserLogUIControllerLike]
        //Create stage
        val stg = new Stage {
          title = "MathAct - Logger"
          scene = new Scene(view)}
        //Set params and return
        stg.resizable = true
        stg.sizeToScene()
        (stg, controller)
      case None ⇒
        throw new IllegalArgumentException(
          s"[UserLoggingActor.<init>] Cannot load FXML by '${config.uiFxmlPath} path.'")}}








  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {
    //Show UI
    case M.ShowUserLoggingUI ⇒
      runAndWait(window.show())
      workbenchController ! M.UserLoggingUIChanged(isShow = true)
    //Hide UI
    case M.HideUserLoggingUI ⇒
      runAndWait(window.hide())
      workbenchController ! M.UserLoggingUIChanged(isShow = false)
    //Log info
    case M.LogInfo(toolId, toolName, message) ⇒
//      //Build row
//      val row = LogRow(config.infoImg, toolName, message)
//      //Add to Log
//      addRow(row)
    //Log warning
    case M.LogWarning(toolId, toolName, message) ⇒
      //Build row
//      val row = LogRow(config.warnImg, toolName, message)
//      //Add to Log
//      addRow(row)
    //Log error
    case M.LogError(toolId, toolName, error, message) ⇒
//      //Build row
//      val row = LogRow(config.errorImg, toolName, message + (error match{
//        case Some(e) ⇒
//          "\n" +
//          "Exception message: " + e.getMessage + "\n" +
//          "Stack trace: \n      " + e.getStackTrace.mkString("\n      ")
//        case None ⇒ ""}))
//      //Add to Log
//      addRow(row)
    //Terminate user logging
    case M.TerminateUserLogging ⇒
      runAndWait(window.close())
      workbenchController ! M.SketchUITerminated
      self ! PoisonPill}}
