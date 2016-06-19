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
import mathact.parts.data.PumpEvents
import mathact.parts.gui.JFXApplication
import mathact.parts.plumbing.actors.Pumping

import scala.concurrent.Future
import scalafx.application.Platform
import scala.concurrent.ExecutionContext.Implicits.global


/** Contain global services
  * Created by CAB on 13.05.2016.
  */

class Environment {
  //Parameters
  val beforeTerminateTimeout = 1000 //In milliseconds
  //Actor system
  val system = ActorSystem("MathActActorSystem")
  val log = Logging.getLogger(system, this)
  //Stop proc
  def doStop(exitCode: Int): Unit = Future{
    log.debug(s"[Environment.doStop] Stopping of program, terminate timeout: $beforeTerminateTimeout milliseconds.")
    Thread.sleep(beforeTerminateTimeout)
    Platform.exit()
    system.terminate().onComplete{_ ⇒ System.exit(exitCode)}}
  //Actors
  val pumping: ActorRef = system.actorOf(Props[Pumping], "PumpingActor")
  val controller: ActorRef = system.actorOf(Props(new Controller(pumping, doStop)), "MainControllerActor")}
