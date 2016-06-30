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

package mathact.parts.plumbing

import akka.actor.ActorRef
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import mathact.parts.data.Msg
import mathact.parts.plumbing.fitting.{Inlet, Outlet}
import mathact.parts.{WorkbenchContext, OnStart, OnStop}
import scala.concurrent.Await
import scala.concurrent.duration._
import scalafx.scene.image.Image


/** Process of tools communications
  * Created by CAB on 09.05.2016.
  */

class Pump(context: WorkbenchContext, tool: Fitting, toolName: String, toolImage: Option[Image]) {
  //Parameters
  private implicit val askTimeout = Timeout(5.seconds)
  //Logging
  val log = Logging.getLogger(context.system, this)
  log.info(s"[Pump.<init>] Creating of tool: $tool, name: $toolName")
//  object log {
//    def debug(msg: String): Unit = loger.debug(s"[$toolName] $msg")
//    def info(msg: String): Unit = loger.info(s"[$toolName] $msg")
//    def warning(msg: String): Unit = loger.warning(s"[$toolName] $msg")
//    def error(msg: String): Unit = loger.error(s"[$toolName] $msg")  }
//  //Actors
  private val drive: ActorRef = Await
    .result(ask(context.pumping, Msg.NewDrive(toolName, toolImage)).mapTo[Either[Throwable,ActorRef]], askTimeout.duration)
    .fold(t ⇒ throw t, d ⇒ d)

//  private val impeller: ActorRef =
//    Await.result(ask(drive, Msg.NewImpeller(toolName)).mapTo[ActorRef], askTimeout.duration)
//
//
//
//
//
//


  tool match{             //Должно выполнятся при инициализации инструмента
    case os: OnStart ⇒  os.doStart()
    case _ ⇒ println("NOT OnStart")
  }

  tool match{             //Должно выполнятся призавершении работы инструмента
    case os: OnStop ⇒  os.doStop()
    case _ ⇒ println("NOT OnStop")
  }

  //Functions
  private def addPipe(msg: Any): Int = Await
    .result(
      ask(drive, msg).mapTo[Either[Throwable,Int]],
      askTimeout.duration)
    .fold(
      t ⇒ {
        log.error(s"[Pump.addPipe] Error on adding of pipe, msg: $msg, error: $t")
        throw t},
      d ⇒ {
        log.debug(s"[Pump.addPipe] Pump added, isAdded: $d")
        d})
  //Methods
  private[mathact] def addOutlet(pipe: Outlet[_]): Int = addPipe(Msg.AddOutlet(pipe))
  private[mathact] def addInlet(pipe: Inlet[_]): Int = addPipe(Msg.AddInlet(pipe))







    //Нужно добавиь ещё один актор который будет следить за нагрузкой impeller, и регулировать
    //влечену очереди (подход с обратным давлением)
    //Нужно предусмотреть след режыми работы: синхронный жосткий, синхронный мягкий (без подтверждений выполения итерации)
    //и асинхронный.


}
