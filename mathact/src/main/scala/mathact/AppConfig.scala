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
import mathact.parts.model.config.{DriveConfigLike, MainConfigLike, PumpConfigLike, PumpingConfigLike}

import scala.concurrent.duration._


/** Read and hold main commonConfig
  * Created by CAB on 03.09.2016.
  */

private [mathact] class AppConfig extends MainConfigLike{
  //Load commonConfig
  val config = ConfigFactory.load()
  //Main config
  val sketchBuildingTimeout = config.getInt("main.sketch.building.timeout").millis
  //Parse commonConfig
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


}
