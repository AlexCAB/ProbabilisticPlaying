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
import javafx.scene.input.{KeyCodeCombination, KeyEvent}

import scalafx.beans.property.{StringProperty, ObjectProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{Clipboard, ClipboardContent, KeyCombination, KeyCode}
import scalafxml.core.macros.sfxml
import scalafx.Includes._


/** User log UI presenter
  * Created by CAB on 24.09.2016.
  */

@sfxml
class UserLogUIController(
  private val searchText: TextField,
  private val logLevelChoice: ChoiceBox[String],
  private val logAmountChoice: ChoiceBox[String],
  private val cleanBtn: Button,
  private val tableView: TableView[LogRow])
extends UserLogUIControllerLike{
   //Params
  val buttonsImageSize = 30
  val logImageSize = 20
  val toolNameColumnPrefWidth = 150
  val runBtnDPath = "mathact/userLog/clean_btn_d.png"
  val runBtnEPath = "mathact/userLog/clean_btn_e.png"
  val infoImgPath = "mathact/userLog/info_img.png"
  val warnImgPath = "mathact/userLog/warn_img.png"
  val errorImgPath = "mathact/userLog/error_img.png"
  val logLevelChoiceDefault = "Show all"
  val logAmountChoiceDefault = "Last 100"
  //Load resources
  val runBtnDImg = new ImageView{image = new Image(runBtnDPath, buttonsImageSize, buttonsImageSize, true, true)}
  val runBtnEImg = new ImageView{image = new Image(runBtnEPath, buttonsImageSize, buttonsImageSize, true, true)}
  val logImages = Map(LogMsgType.Info → infoImgPath, LogMsgType.Warn → warnImgPath, LogMsgType.Error → errorImgPath)
    .map{case (id, path) ⇒ (id, new ImageView{image = new Image(path, logImageSize, logImageSize, true, true)})}
  //Listeners
  def searchTextAction(): Unit = {

    println("searchTextAction")

  }
  def logLevelChoiceAction(): Unit = {

    println("logLevelChoiceAction")

  }
  def logAmountChoiceAction(): Unit = {

    println("logAmountChoiceAction")

  }
  def cleanBtnAction(): Unit = {

    println("cleanBtnAction")

  }
  //Preparing tools
  logLevelChoice.delegate.getSelectionModel.select(logLevelChoiceDefault)
  logAmountChoice.delegate.getSelectionModel.select(logAmountChoiceDefault)
  cleanBtn.graphic = runBtnEImg
  cleanBtn.disable = false
  searchText.textProperty.onChange(searchTextAction())
  logLevelChoice.delegate.getSelectionModel.selectedItemProperty.onChange(logLevelChoiceAction())
  logAmountChoice.delegate.getSelectionModel.selectedItemProperty.onChange(logAmountChoiceAction())
  //Preparing table
  val msgTypeColumn = new TableColumn[LogRow, ImageView] {
    text = "T"
    prefWidth = logImageSize + 4
    style = "-fx-alignment: CENTER;"
    cellValueFactory = { d ⇒ new ObjectProperty(d.value, "type",  logImages(d.value.msgType))}
    cellFactory = { _ ⇒
      new TableCell[LogRow, ImageView] {
        item.onChange { (_, _, img) ⇒ graphic = img}}}}
  val toolNameColumn = new TableColumn[LogRow, String] {
    text = "Tool Name"
    prefWidth = toolNameColumnPrefWidth
    style = "-fx-font-size: 12; -fx-font-weight: bold; -fx-alignment: CENTER;"
    cellValueFactory = { d ⇒ new StringProperty(d.value, "toolName",  d.value.toolName)}}
  val messageColumn = new TableColumn[LogRow, String] {
    text = "Message"
    style = "-fx-font-size: 12;"
    cellValueFactory = { d ⇒ new StringProperty(d.value, "message",  d.value.message)}}
  messageColumn.prefWidthProperty.bind(tableView.width - (msgTypeColumn.width + toolNameColumn.width) - 20)
  tableView.columns ++= Seq(msgTypeColumn, toolNameColumn, messageColumn)
  tableView.onKeyPressed = new EventHandler[KeyEvent]{
    val copyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.ControlAny)
    def handle(e: KeyEvent): Unit = if(copyCombination.`match`(e)){
      val item = tableView.selectionModel.value.getSelectedItem
      val clipboard = new ClipboardContent
      val text = item.toolName + "\t|\t" + item.message
      clipboard.putString(text)
      Clipboard.systemClipboard.setContent(clipboard)}}
  //Methods
  def setRows(rows: List[LogRow]): Unit = { tableView.items = ObservableBuffer(rows) }










}