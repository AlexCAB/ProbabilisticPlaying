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
import mathact.parts.data.{Msg, StateMsg, ActorState}

import scala.util.Try


/** Base class for actors with state
  * Created by CAB on 26.08.2016.
  */

abstract class StateActorBase(initState: ActorState) extends Actor{
  //Objects
  val log: LoggingAdapter = Logging.getLogger(context.system, this)
  implicit val execContext = context.system.dispatcher
  //Variables
  private var currentState: ActorState = initState
  //Getters and setters
  def state = currentState
  def state_= (s: ActorState):Unit = {currentState = s}
  //Receives
  /** Reaction on StateMsg'es */
  def onStateMsg: PartialFunction[StateMsg, Unit]
  /** Handling after reaction executed */
  def reaction: PartialFunction[(Msg, ActorState), Unit]
  /** Actor reaction on messages */
  def postHandling: PartialFunction[(Msg, ActorState), Unit]
  //Receive
  def receive: PartialFunction[Any, Unit] = {
    case message: StateMsg ⇒
      log.debug(s"STATE MESSAGE: $message, FROM: $sender, CURRENT STATE: $currentState")
      onStateMsg.applyOrElse[StateMsg, Unit](
        message,
        _ ⇒ log.error(s"LAST STATE MESSAGE NOT HANDLED: $message"))
      postHandling.applyOrElse[(Msg, ActorState), Unit](
        (message, currentState),
        _ ⇒ Unit)
    case message: Msg ⇒
      log.debug(s"MESSAGE: $message, FROM: $sender, STATE: $currentState")
      reaction.applyOrElse[(Msg, ActorState), Unit](
        (message, currentState),
        _ ⇒ log.error(s"LAST MESSAGE NOT HANDLED: $message"))
      postHandling.applyOrElse[(Msg, ActorState), Unit](
        (message, currentState),
        _ ⇒ Unit)
    case message: Any ⇒
      log.error(s"Receive not a Msg type: $message")}


  //TODO Add more


}