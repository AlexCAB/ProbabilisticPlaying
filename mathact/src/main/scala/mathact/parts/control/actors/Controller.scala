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

package mathact.parts.control.actors

import javafx.stage.{Stage => jStage}

import akka.actor.{ActorRef, Actor}
import mathact.parts.plumbing.Events

import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{LinearGradient, Stops}
import scalafx.scene.text.Text
import scalafx.stage.Stage

/** Main application controller
  * Created by CAB on 21.05.2016.
  */

class Controller(pumping: ActorRef) extends Actor{
  //Definitions
  private class MainWindowStage extends Stage {
    title = "ScalaFX Hello World"
    scene = new Scene {
      fill = Black
      content = new HBox {
        padding = Insets(20)
        children = Seq(
          new Text {
            text = "Hello "
            style = "-fx-font-size: 48pt"
            fill = new LinearGradient(
              endX = 0,
              stops = Stops(PaleGreen, SeaGreen))
          },
          new Text {
            text = "World!!!"
            style = "-fx-font-size: 48pt"
            fill = new LinearGradient(
              endX = 0,
              stops = Stops(Cyan, DodgerBlue)
            )
            effect = new DropShadow {
              color = DodgerBlue
              radius = 25
              spread = 0.25
            }
          }
        )
      }
    }
  }


  def receive = {

    case Events.DoStart ⇒


      //Далее здесь должна выполнятся создание UI и иницализация инсрументов.

      println("[Controller] Receive: DoStart")

      Platform.runLater{

        val w = new MainWindowStage

        w.show()

      }





    case x ⇒ println("[Controller] Receive: " + x)
  }




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




}
