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

import javafx.application.Application

import akka.event.LoggingAdapter

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import javafx.stage.Stage


/** FX Application class and helper object
  * Created by CAB on 21.05.2016.
  */


private [mathact] object JFXApplication{
  //Parameters
  val appRunTimeout: Int = 5000  //In milliseconds
  //Variables
  @volatile private var primaryStage: Option[Stage] = None
  //Methods
  /** Do run of Java FX
    * @param args - Array[String], application arguments from def main()
    * @param log - LoggingAdapter, logging. */
  def init(args: Array[String], log: LoggingAdapter): Unit = {
    //Starting app
    log.debug("[JFXApplication.init] Try to start Java FX.")
    Future{
      Application.launch(classOf[JFXApplication], args: _*)}
    //Wait for started
    var count = appRunTimeout
    while (primaryStage.isEmpty && count > 0){
      Thread.sleep(1)
      count -= 1}
    //Check if run
    primaryStage.isEmpty match{
      case true ⇒ throw new IllegalStateException("[JFXApplication.init] Java FX not started.")
      case false ⇒ log.info("[JFXApplication.init] Java FX started.")}}}


private [mathact] class JFXApplication extends Application{
  //Methods
  def start(stage: Stage) = { JFXApplication.primaryStage = Some(stage) }}
