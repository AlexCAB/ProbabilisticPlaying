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

import akka.actor.ActorRef
import akka.event.LoggingAdapter
import mathact.parts.control.CtrlEvents

import scalafx.beans.property.ObjectProperty
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{ImageView, Image}
import scalafx.scene.layout._
import scalafx.Includes._
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{Stops, LinearGradient}
import scalafx.scene.text.Text
import scalafx.stage.Stage
import scalafx.Includes._


/** The main (workbench) window
  * Created by CAB on 23.05.2016.
  */

abstract class MainWindow(log: LoggingAdapter) extends JFXInteraction {
  //Callbacks
  def doStop(): Unit
  def hitRun(): Unit
  def hitStop(): Unit
  def hitStep(): Unit
  def setSpeed(value: Double): Unit
  def switchMode(newMode: Int): Unit
  //Definitions
  private class MainWindowStage extends Stage {
    //Definitions
    class MWButton(eImgName: String, dImgName: String)(action: ⇒Unit) extends Button{
      //Images
      val eImg = new ImageView{image = new Image(eImgName)}
      val dImg = new ImageView{image = new Image(dImgName)}
      //Config
      graphic = dImg
      disable = true
      onAction = handle{action}
      //Methods
      def setEnabled(isEnabled: Boolean): Unit = isEnabled match{
        case true ⇒
          graphic = eImg
          disable = false
        case false ⇒
          graphic = dImg
          disable = true}}
    //Close operation
    delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
      def handle(event: WindowEvent): Unit = {
        log.debug("[MainWindow.onCloseRequest] Close is hit, call doStop.")
        doStop()
        event.consume()}})
    //UI Components
    private val startBtn = new MWButton("start_e.png", "start_d.png")(println("$$$$$ startBtn"))
    private val stopBtn = new MWButton("stop_e.png", "stop_d.png")(println("$$$$$ stopBtn"))
    private val stepBtn = new MWButton("step_e.png", "step_d.png")(println("$$$$$ stepBtn"))


     //Далее здесь: Workbench IU, три кнопки слайдер и перключатель режима работы, строка состояния

    //UI
    title = "MathAct - Workbench"
    height = 140
    width = 300





      scene = new Scene {
      fill = White






      content = new BorderPane{




        top = new HBox {

          children = Seq(startBtn, stopBtn, stepBtn)


        }

        bottom = new Text {

          text = "Starting..."




        }




      }

//      content = new HBox {
//        padding = Insets(20)
//        children = Seq(
//          new Text {
//            text = "Hello "
//            style = "-fx-font-size: 48pt"
//            fill = new LinearGradient(
//              endX = 0,
//              stops = Stops(PaleGreen, SeaGreen))
//          },
//          new Text {
//            text = "World!!!"
//            style = "-fx-font-size: 48pt"
//            fill = new LinearGradient(
//              endX = 0,
//              stops = Stops(Cyan, DodgerBlue)
//            )
//            effect = new DropShadow {
//              color = DodgerBlue
//              radius = 25
//              spread = 0.25
//            }
//          }
//        )
//      }




    }
  }
  //Variables
  private var stage: Option[MainWindowStage] = None
  //Methods
  def init(): Unit = {

    stage = Some(runNow{
      val stg = new MainWindowStage
      stg.show()
      stg})



  }
  def setRan(isRan: Boolean): Unit = {    //При true делает стоп активным остальные те активными, при false стоп не активен остальные активны.

    ???

  }

  def setEnabled(isEnabled: Boolean): Unit = {    //При true разрешает интерфейс

    ???

  }

  def setStatus(status: String): Unit = {

    ???

  }






}
