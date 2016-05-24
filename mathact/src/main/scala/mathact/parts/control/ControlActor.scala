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

package mathact.parts.control

import java.io.{PrintWriter, StringWriter}

import akka.actor.Actor
import akka.event.LoggingAdapter

import scala.util.Try


/** Base class for control actors
  * Created by CAB on 24.05.2016.
  */

abstract class ControlActor extends Actor{
  //Objects
  val log: LoggingAdapter
  //Helpers
  /** Run block and log error
    * @param block - code to run
    * @return - Try[T] */
  def tryToRun[T](block: ⇒T): Try[T] = Try{block}
    .recover{case t: Throwable ⇒
      val sw = new StringWriter
      t.printStackTrace(new PrintWriter(sw))
      log.error(s"[ControlActor.tryToRun] Error: ${sw.toString}")
      t.printStackTrace()
      throw t}


  //TODO Add more


}
