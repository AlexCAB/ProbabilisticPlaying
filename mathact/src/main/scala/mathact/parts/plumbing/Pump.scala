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
import mathact.parts.plumbing.fitting._
import mathact.parts.{WorkbenchContext, OnStart, OnStop}
import scala.concurrent.Await
import scala.concurrent.duration._
import scalafx.scene.image.Image


/** Process of tools communications
  * Created by CAB on 09.05.2016.
  */

class Pump(context: WorkbenchContext, val tool: Fitting, val toolName: String, val toolImage: Option[Image]) {
  //Parameters
  val askTimeout = Timeout(5.second)
  val pushTimeoutCoefficient = context.config.getInt("plumbing.push.timeout.coefficient")
  val startFunctionTimeout = context.config.getInt("plumbing.start.function.timeout").millis
  val messageProcessingTimeout = context.config.getInt("plumbing.message.processing.timeout").millis
  val stopFunctionTimeout = context.config.getInt("plumbing.stop.function.timeout").millis
  //Logging
  private val akkaLog = Logging.getLogger(context.system, this)
  akkaLog.info(s"[Pump.<init>] Creating of tool: $tool, name: $toolName")
  private[mathact] object log {
    def debug(msg: String): Unit = akkaLog.debug(s"[$toolName] $msg")
    def info(msg: String): Unit = akkaLog.info(s"[$toolName] $msg")
    def warning(msg: String): Unit = akkaLog.warning(s"[$toolName] $msg")
    def error(msg: String): Unit = akkaLog.error(s"[$toolName] $msg")  }
  //Actors
  private[mathact] val drive: ActorRef = Await
    .result(ask(context.pumping, Msg.NewDrive(this, toolName, toolImage))(askTimeout)
      .mapTo[Either[Throwable,ActorRef]], askTimeout.duration)
    .fold(t ⇒ throw t, d ⇒ d)
  //Functions
  private def addPipe(msg: Any): Int = Await //Return: pipe ID
    .result(
      ask(drive, msg)(askTimeout).mapTo[Either[Throwable,Int]],
      askTimeout.duration)
    .fold(
      t ⇒ {
        akkaLog.error(s"[Pump.addPipe] Error on adding of pipe, msg: $msg, error: $t")
        throw t},
      d ⇒ {
        akkaLog.debug(s"[Pump.addPipe] Pipe added, pipeId: $d")
        d})
  //Overridden methods
  override def toString: String = s"Pump(toolName: $toolName)"
  //Methods
  private[mathact] def addOutlet(pipe: OutPipe[_], name: Option[String]): Int = addPipe(Msg.AddOutlet(pipe, name))
  private[mathact] def addInlet(pipe: InPipe[_], name: Option[String]): Int = addPipe(Msg.AddInlet(pipe, name))
  private[mathact] def connect(out: ()⇒Plug[_], in: ()⇒Socket[_]): Int = Await //Return: connection ID
    .result(
      ask(drive,  Msg.ConnectPipes(out, in))(askTimeout).mapTo[Either[Throwable,Int]],
      askTimeout.duration)
    .fold(
      t ⇒ {
        akkaLog.error(s"[Pump.connect] Error on connecting of pipes: $t")
        throw t},
      d ⇒ {
        akkaLog.debug(s"[Pump.connect] Pipe added, pipeId: $d")
        d})
  private[mathact] def toolStart(): Unit = tool match{
    case os: OnStart ⇒ os.doStart()
    case _ ⇒ akkaLog.debug(s"[Pump.toolStart] Tool $toolName not have doStart method.")}
  private[mathact] def toolStop(): Unit = tool match{
    case os: OnStop ⇒  os.doStop()
    case _ ⇒ akkaLog.debug(s"[Pump.toolStop] Tool $toolName not have doStop method.")}
  private[mathact] def pushUserMessage(msg: Msg.UserData[_]): Unit = Await
    .result(
      ask(drive, msg)(askTimeout).mapTo[Either[Throwable, Option[Long]]],  //Either(error,  Option[sleep timeout])
      askTimeout.duration)
    .fold(
      error ⇒ {
        akkaLog.error(s"[Pump.pushUserMessage] Error on ask of drive, msg: $msg, error: $error")
        throw error},
      timeout ⇒ {
        akkaLog.debug(s"[Pump.pushUserMessage] Message pushed, msg: $msg, timeout, $timeout")
        timeout.foreach{ d ⇒
          try{
            Thread.sleep(d)}
          catch {case e: InterruptedException ⇒
            akkaLog.error(s"[Pump.pushUserMessage] Error on Thread.sleep, msg: $msg, error: $e")
            Thread.currentThread().interrupt()}}})














  //Нужно добавиь ещё один актор который будет следить за нагрузкой impeller, и регулировать
    //влечену очереди (подход с обратным давлением)
    //Нужно предусмотреть след режыми работы: синхронный жосткий, синхронный мягкий (без подтверждений выполения итерации)
    //и асинхронный.


}
