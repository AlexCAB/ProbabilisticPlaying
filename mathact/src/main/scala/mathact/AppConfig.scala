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

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.parts.model.config._

import scala.concurrent.duration._
import scalafx.scene.image.Image


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
    val buttonsSize = 25
    val runBtnD             = new Image("run_btn_d.png", buttonsSize, buttonsSize, true, true)
    val runBtnE             = new Image("run_btn_e.png", buttonsSize, buttonsSize, true, true)
    val showAllToolsUiD     = new Image("show_all_tools_ui_d.png", buttonsSize, buttonsSize, true, true)
    val showAllToolsUiE     = new Image("show_all_tools_ui_e.png", buttonsSize, buttonsSize, true, true)
    val hideAllToolsUiBtnD  = new Image("hide_all_tools_ui_btn_d.png", buttonsSize, buttonsSize, true, true)
    val hideAllToolsUiBtnE  = new Image("hide_all_tools_ui_btn_e.png", buttonsSize, buttonsSize, true, true)
    val skipAllTimeoutTaskD = new Image("skip_all_timeout_task_d.png", buttonsSize, buttonsSize, true, true)
    val skipAllTimeoutTaskE = new Image("skip_all_timeout_task_e.png", buttonsSize, buttonsSize, true, true)
    val stopSketchBtnD      = new Image("stop_sketch_btn_d.png", buttonsSize, buttonsSize, true, true)
    val stopSketchBtnE      = new Image("stop_sketch_btn_e.png", buttonsSize, buttonsSize, true, true)
    val logBtnD             = new Image("log_btn_d.png", buttonsSize, buttonsSize, true, true)
    val logBtnS             = new Image("log_btn_s.png", buttonsSize, buttonsSize, true, true)
    val logBtnH             = new Image("log_btn_h.png", buttonsSize, buttonsSize, true, true)
    val visualisationBtnD   = new Image("visualisation_btn_d.png", buttonsSize, buttonsSize, true, true)
    val visualisationBtnS   = new Image("visualisation_btn_s.png", buttonsSize, buttonsSize, true, true)
    val visualisationBtnH   = new Image("visualisation_btn_h.png", buttonsSize, buttonsSize, true, true)}
  //Parse user logging
  val userLogging = new UserLoggingConfigLike{
    val showUIOnError = config.getBoolean("view.logging.show.ui.on.error")

  }}
