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
import akka.actor.{Props, ActorRef, ActorSystem}
import akka.event.Logging
import mathact.parts.control.actors.Controller
import mathact.parts.gui.JFXApplication
import mathact.parts.plumbing.Events
import mathact.parts.plumbing.actors.Pumping

import scalafx.application.Platform
import scala.concurrent.ExecutionContext.Implicits.global


/** Contain global services
  * Created by CAB on 13.05.2016.
  */

class Environment {
  //Actor system
  implicit val system = ActorSystem("MathActActorSystem")
  private val log = Logging.getLogger(system, this)
  //Actors
  val pumping: ActorRef = system.actorOf(Props[Pumping], "PumpingActor")
  val controller: ActorRef = system.actorOf(Props(new Controller(pumping)), "MainControllerActor")
  //Methods
  def start(args: Array[String]): Unit = {
    //Start UI
    try{
      JFXApplication.init(args, log)}
    catch{ case e: Throwable ⇒
      log.error(s"[Environment.start] Error on start UI: $e, terminate ActorSystem.")
      system.terminate().onComplete(_ ⇒ Platform.exit())
      throw e}
    //Start Main window
    controller ! Events.DoStart}}
