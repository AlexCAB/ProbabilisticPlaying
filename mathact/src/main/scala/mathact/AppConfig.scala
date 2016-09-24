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

package mathact

import javafx.scene.Parent

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.parts.control.view.user.logging.UserLogUIControllerLike
import mathact.parts.model.config._

import scala.concurrent.duration._
import scalafx.scene.image.Image
import scalafxml.core.{FXMLLoader, NoDependencyResolver, FXMLView}


/** Read and hold main commonConfig
  * Created by CAB on 03.09.2016.
  */

private [mathact] class AppConfig extends MainConfigLike{
  //Load commonConfig
  val config = ConfigFactory.load()
  //Main config
  val sketchBuildingTimeout = config.getInt("main.sketch.building.timeout").millis
  //Parse pumping config
  val pumping = new PumpingConfigLike{
    val pump = new PumpConfigLike{
      val askTimeout = Timeout(config.getInt("plumbing.pump.ask.timeout").millis)}
    val drive = new DriveConfigLike{
      val pushTimeoutCoefficient = config.getInt("plumbing.push.timeout.coefficient")
      val startFunctionTimeout = config.getInt("plumbing.start.function.timeout").millis
      val messageProcessingTimeout = config.getInt("plumbing.message.processing.timeout").millis
      val stopFunctionTimeout = config.getInt("plumbing.stop.function.timeout").millis
      val impellerMaxQueueSize = config.getInt("plumbing.impeller.max.queue.size")
      val uiOperationTimeout = config.getInt("plumbing.ui.operation.timeout").millis}}
  //Build SketchUI config
  val sketchUI = new SketchUIConfigLike{
    val buttonsSize = 30
    val runBtnD             = new Image("mathact/sketchIU/run_btn_d.png", buttonsSize, buttonsSize, true, true)
    val runBtnE             = new Image("mathact/sketchIU/run_btn_e.png", buttonsSize, buttonsSize, true, true)
    val showAllToolsUiD     = new Image("mathact/sketchIU/show_all_tools_ui_d.png", buttonsSize, buttonsSize, true, true)
    val showAllToolsUiE     = new Image("mathact/sketchIU/show_all_tools_ui_e.png", buttonsSize, buttonsSize, true, true)
    val hideAllToolsUiBtnD  = new Image("mathact/sketchIU/hide_all_tools_ui_btn_d.png", buttonsSize, buttonsSize, true, true)
    val hideAllToolsUiBtnE  = new Image("mathact/sketchIU/hide_all_tools_ui_btn_e.png", buttonsSize, buttonsSize, true, true)
    val skipAllTimeoutTaskD = new Image("mathact/sketchIU/skip_all_timeout_task_d.png", buttonsSize, buttonsSize, true, true)
    val skipAllTimeoutTaskE = new Image("mathact/sketchIU/skip_all_timeout_task_e.png", buttonsSize, buttonsSize, true, true)
    val stopSketchBtnD      = new Image("mathact/sketchIU/stop_sketch_btn_d.png", buttonsSize, buttonsSize, true, true)
    val stopSketchBtnE      = new Image("mathact/sketchIU/stop_sketch_btn_e.png", buttonsSize, buttonsSize, true, true)
    val logBtnD             = new Image("mathact/sketchIU/log_btn_d.png", buttonsSize, buttonsSize, true, true)
    val logBtnS             = new Image("mathact/sketchIU/log_btn_s.png", buttonsSize, buttonsSize, true, true)
    val logBtnH             = new Image("mathact/sketchIU/log_btn_h.png", buttonsSize, buttonsSize, true, true)
    val visualisationBtnD   = new Image("mathact/sketchIU/visualisation_btn_d.png", buttonsSize, buttonsSize, true, true)
    val visualisationBtnS   = new Image("mathact/sketchIU/visualisation_btn_s.png", buttonsSize, buttonsSize, true, true)
    val visualisationBtnH   = new Image("mathact/sketchIU/visualisation_btn_h.png", buttonsSize, buttonsSize, true, true)}
  //Parse user logging
  val userLogging = new UserLoggingConfigLike{
    //Load UI
    val fxmlLoader = new FXMLLoader(
      getClass.getClassLoader.getResource("mathact/userLog/ui.fxml"),
      NoDependencyResolver)
    fxmlLoader.load()
    //Parameters
    val showUIOnError = config.getBoolean("view.logging.show.ui.on.error")
    val view = fxmlLoader.getRoot[Parent]
    val controller = fxmlLoader.getController[UserLogUIControllerLike]
    val logImgSize = 20
    val infoImg    = new Image("mathact/userLog/info_img.png", logImgSize, logImgSize, true, true)
    val warnImg    = new Image("mathact/userLog/warn_img.png", logImgSize, logImgSize, true, true)
    val errorImg   = new Image("mathact/userLog/error_img.png", logImgSize, logImgSize, true, true)


  }}
