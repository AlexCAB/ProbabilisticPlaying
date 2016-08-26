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

import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}

import scala.util.Try


/** Base actor
  * Created by CAB on 24.05.2016.
  */

abstract class ActorBase extends Actor{
  //Objects
  val log: LoggingAdapter = Logging.getLogger(context.system, this)
  implicit val execContext = context.system.dispatcher
  //Variables
  private var stateToLogFun: Option[()⇒Any] = None
  //Helpers
  /** Run block and akkaLog error
    * @param block - code to run
    * @return - Try[T] */
  def tryToRun[T](block: ⇒T): Try[T] = Try{block}
    .recover{case t: Throwable ⇒
      val sw = new StringWriter
      t.printStackTrace(new PrintWriter(sw))
      log.error(s"[ActorBase.tryToRun] Error: ${sw.toString}")
      t.printStackTrace()
      throw t}
//  //Extra methods
//  implicit class AnyEx(value: Any){
//    def handle(handler: PartialFunction[Any, Unit]): Unit = {
//      log.debug(s"[ActorBase.AnyEx.handle] Handle for value: $value")
//      handler.applyOrElse[Any, Unit](value, _ ⇒ {
//        log.error(
//          s"[ActorBase.AnyEx.handle] Not handled value: $value, " +
//          s"stack: \n ${Thread.currentThread().getStackTrace.mkString("\n")}")})}
//    def apply(handler: PartialFunction[Any, Unit]): Unit = handler.applyOrElse[Any, Unit](value, _ ⇒ Unit)}
  //Messages handling with logging
  def stateToLog(state: ⇒Any): Unit = { stateToLogFun = Some(()⇒state) }
  def reaction: PartialFunction[Any, Unit]
  //Receive
  def receive: PartialFunction[Any, Unit] = { case m ⇒
    val stateText = stateToLogFun.map(s ⇒ s", STATE: ${s()}").getOrElse("")
    log.debug(s"FROM: $sender$stateText, MESSAGE: $m")
    reaction.applyOrElse[Any, Unit](m, _ ⇒ log.warning(s"LAST MESSAGE NOT HANDLED: $m"))}


  //TODO Add more


}
