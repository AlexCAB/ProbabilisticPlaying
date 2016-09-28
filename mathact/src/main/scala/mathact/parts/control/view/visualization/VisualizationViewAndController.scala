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

package mathact.parts.control.view.visualization

import mathact.parts.model.config.VisualizationConfigLike
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import akka.actor.ActorRef
import akka.event.LoggingAdapter
import mathact.parts.model.config.SketchUIConfigLike
import mathact.parts.model.enums.SketchUIElement._
import mathact.parts.model.enums.SketchUiElemState._
import mathact.parts.model.enums._
import mathact.parts.model.messages.M

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color._
import scalafx.scene.{Scene, Node}
import scalafx.scene.control.Button
import scalafx.scene.image.{ImageView, Image}
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.text.Text
import scalafx.stage.Stage


/** Visualization view and controller
  * Created by CAB on 28.09.2016.
  */

class VisualizationViewAndController(
  config: VisualizationConfigLike,
  visualizationActor: ActorRef,
  log: LoggingAdapter)
extends Stage { import Visualization._
  //Params
  val windowTitle = "MathAct - Visualization"




  //Close operation
  delegate.setOnCloseRequest(new EventHandler[WindowEvent]{
    def handle(event: WindowEvent): Unit = {
      log.debug("[VisualizationViewAndController.onCloseRequest] Close is hit, send DoClose.")
      visualizationActor ! DoClose
      event.consume()}})
  //UI Components

  //TODO


  //UI
  title = windowTitle
  scene = new Scene {
    fill = White
    content = new BorderPane{}





  }






}
