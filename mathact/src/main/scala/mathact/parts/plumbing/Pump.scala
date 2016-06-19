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
import mathact.parts.data.PumpEvents
import mathact.parts.{OnStart, OnStop, Environment}
import scala.concurrent.Await
import scala.concurrent.duration._


/** Process of tools communications
  * Created by CAB on 09.05.2016.
  */

class Pump(env: Environment, tool: Fitting, toolName: String) {
  //Parameters
  private implicit val askTimeout = Timeout(5.seconds)
  //Logging
  private val loger = Logging.getLogger(env.system, this)
  object log {
    def debug(msg: String): Unit = loger.debug(s"[$toolName] $msg")
    def info(msg: String): Unit = loger.info(s"[$toolName] $msg")
    def warning(msg: String): Unit = loger.warning(s"[$toolName] $msg")
    def error(msg: String): Unit = loger.error(s"[$toolName] $msg")  }
  //Actors
  private val drive: ActorRef =
    Await.result(ask(env.pumping, PumpEvents.NewDrive(toolName, None)).mapTo[ActorRef], askTimeout.duration)
  private val impeller: ActorRef =
    Await.result(ask(drive, PumpEvents.NewImpeller(toolName)).mapTo[ActorRef], askTimeout.duration)








  tool match{             //Должно выполнятся при инициализации инструмента
    case os: OnStart ⇒  os.doStart()
    case _ ⇒ println("NOT OnStart")
  }

  tool match{             //Должно выполнятся призавершении работы инструмента
    case os: OnStop ⇒  os.doStop()
    case _ ⇒ println("NOT OnStop")
  }



    //Нужно добавиь ещё один актор который будет следить за нагрузкой impeller, и регулировать
    //влечену очереди (подход с обратным давлением)
    //Нужно предусмотреть след режыми работы: синхронный жосткий, синхронный мягкий (без подтверждений выполения итерации)
    //и асинхронный.


}
