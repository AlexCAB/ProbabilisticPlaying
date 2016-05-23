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

import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout._
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{Stops, LinearGradient}
import scalafx.scene.text.Text
import scalafx.stage.Stage
import scalafx.Includes._


/** The main (workbench) window
  * Created by CAB on 23.05.2016.
  */

abstract class MainWindow(log: LoggingAdapter, controller: ActorRef) extends JFXInteraction {
  //Definitions
  private class MainWindowStage extends Stage {
    //Close operation
    delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
      def handle(event: WindowEvent): Unit = {
        log.debug("[MainWindow.onCloseRequest] Close is hit, send CtrlEvents.DoStop.")
        controller ! CtrlEvents.DoStop
        event.consume()}})
    //UI
    title = "MathAct - Workbench"
    scene = new Scene {
      fill = White



      //Далее здесь: Workbench IU, три кнопки слайдер и перключатель режима работы, строка состояния


      content = new BorderPane{

        top = new HBox {

          children = Seq(
            new Button{
              text = "Start"


            },
            new Button{
              text = "Stop"


            },
            new Button{
              text = "Step"

            }





          )


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







}
