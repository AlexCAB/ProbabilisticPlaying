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
import akka.event.{Logging, LoggingAdapter}

import scala.util.Try


/** Base actor
  * Created by CAB on 24.05.2016.
  */

abstract class BaseActor extends Actor{
  //Objects
  val log: LoggingAdapter = Logging.getLogger(context.system, this)
  implicit val execContext = context.system.dispatcher
  //Variables
  private var msgHandler: Option[(Option[()⇒Any], PartialFunction[Any, Unit])] = None
  private var intIdCounter = 0
  private var longIdCounter = 0L
  //Helpers
  /** Run block and akkaLog error
    * @param block - code to run
    * @return - Try[T] */
  def tryToRun[T](block: ⇒T): Try[T] = Try{block}
    .recover{case t: Throwable ⇒
      val sw = new StringWriter
      t.printStackTrace(new PrintWriter(sw))
      log.error(s"[BaseActor.tryToRun] Error: ${sw.toString}")
      t.printStackTrace()
      throw t}
  /** Generate next integer ID
    * @return - Int ID */
  def nextIntId: Int = {intIdCounter += 1; intIdCounter}
  def nextLongId: Long = {longIdCounter += 1; longIdCounter}
  //Messages handling with logging
  def reaction(state: ⇒ Any)(handling: PartialFunction[Any, Unit]): Unit = {
    msgHandler = Some(Tuple2(Some(()⇒state), handling))}
  def reaction()(handling: PartialFunction[Any, Unit]): Unit = {
    msgHandler = Some(Tuple2(None, handling))}
  final def receive: PartialFunction[Any, Unit] = {
    case m ⇒ msgHandler match{
      case Some((state, handling)) ⇒
        log.debug(state.map(s ⇒ s"STATE: ${s()}, ").getOrElse("") + "MESSAGE: " + m)
        handling.applyOrElse[Any, Unit](m, _ ⇒ log.warning("NOT HANDLED MESSAGE: " + m))
      case None ⇒  log.error(s"Message handler not setup, message: $m")}}


  //TODO Add more


}
