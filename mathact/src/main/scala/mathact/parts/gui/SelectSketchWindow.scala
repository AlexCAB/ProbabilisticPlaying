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

import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.event.LoggingAdapter
import mathact.parts.control.actors.Controller._
import mathact.parts.data.Sketch

import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, BorderPane}
import scalafx.scene.paint.Color._
import scalafx.scene.text.Text
import scalafx.stage.Stage


/** Select sketch window, show list of sketches
  * Created by CAB on 19.06.2016.
  */

abstract class SelectSketchWindow(log: LoggingAdapter) extends JFXInteraction {
  //Parameters

  //Callbacks
  def sketchSelected(index: Int): Unit
  def windowClosed(): Unit


  //Definitions
  private class MainWindowStage(sketches: List[Sketch]) extends Stage {
    //Definitions


    //UI
    title = "MathAct - Sketches"
    scene = new Scene {
      fill = White
      content = new BorderPane{
        top = new Label("Hello, select an of next sketches to run:")
        center = sketches match{
          case Nil ⇒
            new Label("No sketches found.")
          case sks ⇒ new TableView[Sketch](ObservableBuffer(sks)){
            //Columns
            val nameColumn = new TableColumn[Sketch, String] {
              text = "Name"
              prefWidth = 150
              cellValueFactory = { d ⇒
                new StringProperty(d.value, "name",  d.value.sName.getOrElse(d.value.clazz.getSimpleName))}}
            val descriptionColumn = new TableColumn[Sketch, String] {
              text = "Description"
              prefWidth = 250
              cellValueFactory = { d ⇒
                new StringProperty(d.value, "description",  d.value.sDesc.getOrElse("---"))}}
            val autorunColumn = new TableColumn[Sketch, String] {
              text = "Autorun"
              prefWidth = 60
              cellValueFactory = { d ⇒
                new StringProperty(d.value, "autorun",  d.value.isAutorun match{case true ⇒ "YES" case _ ⇒ "NO"})}}

            val runBtnColumn = new TableColumn[Sketch, Button] {
              text = "Run"
              prefWidth = 60


              cellFactory = { _ ⇒

                new TableCell[Sketch, Button] {
                  graphic = new Button("Test")

                }


                //Далее здесь:
                //1) Кнопка должна быть в виде картинки '>'
                //2) Исправить ресайзинг (таблица должна изменятся с окном) и исправить вид (размтку, шрифты т.п.)
                //   так же проверить для случая когда нет ни одноо скетча
                //3) Добавть Button вызов sketchSelected
                //4) По нажатию одного Button остальные должны бить сделаны не активными, но нажатый должен остатся активным,
                //   и повторное нажатие должно слать слать повторное сообжение актору, на всякий случай.



              }


              }

            columns ++= Seq(nameColumn, descriptionColumn, autorunColumn, runBtnColumn)
            //On selected



          }
        }






      }

    }


    //Close operation
    delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
      def handle(event: WindowEvent): Unit = {
        log.debug("[SelectSketchWindow.onCloseRequest] Close is hit, call windowClosed.")
        windowClosed()
        event.consume()}})}
  //Variables
  private var stage: Option[MainWindowStage] = None
  //Methods
  def show(sketches: List[Sketch]): Unit = {
    stage = Some(runNow{
      val stg = new MainWindowStage(sketches)
      stg.resizable = true
      stg.sizeToScene()
      stg.show()
      stg})}
  def hide(): Unit = stage.foreach{ stg ⇒
    runAndWait(stg.close())
    stage = None}









}
