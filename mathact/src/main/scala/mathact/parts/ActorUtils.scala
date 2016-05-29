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

package mathact.parts

import java.io.{PrintWriter, StringWriter}

import akka.actor.{ActorRef, Actor}
import akka.event.LoggingAdapter

import scala.util.Try


/** Actor helpers
  * Created by CAB on 24.05.2016.
  */

trait ActorUtils { _: Actor ⇒
  //Objects
  val log: LoggingAdapter
  implicit val execContext = context.system.dispatcher
  //Functions
  private def buildLogMessage(title: String, msg: String, state: Any): String =
    s"MESSAGE: [$title, sender: ${sender.path}${state match{case "" ⇒ ""; case s ⇒ ", state: " + s.toString}}] $msg"
  //Helpers
  /** Run block and log error
    * @param block - code to run
    * @return - Try[T] */
  def tryToRun[T](block: ⇒T): Try[T] = Try{block}
    .recover{case t: Throwable ⇒
      val sw = new StringWriter
      t.printStackTrace(new PrintWriter(sw))
      log.error(s"[ActorUtils.tryToRun] Error: ${sw.toString}")
      t.printStackTrace()
      throw t}

  /** Debug log of actor message
    * @param title - String, which will in [< msg >]
    * @param msg - Log message
    * @param state - Actor state */
  def logMsgD(title: String, msg: String = "", state: Any = ""): Unit = log.debug(buildLogMessage(title, msg, state))
  def logMsgI(title: String, msg: String = "", state: Any = ""): Unit = log.info(buildLogMessage(title, msg, state))
  def logMsgW(title: String, msg: String = "", state: Any = ""): Unit = log.warning(buildLogMessage(title, msg, state))
  def logMsgE(title: String, msg: String = "", state: Any = ""): Unit = log.error(buildLogMessage(title, msg, state))


  //TODO Add more


}
