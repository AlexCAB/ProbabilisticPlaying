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

package mathact.parts.plumbing.actors

import akka.actor.ActorRef
import mathact.parts.OnStart
import mathact.parts.data.Msg
import mathact.parts.plumbing.Pump

import scala.concurrent.duration.FiniteDuration


/** Handling of staring and stopping
  * Created by CAB on 26.08.2016.
  */

private [mathact] trait DriveStartStop { _: Drive ⇒
  //Parameters


  //Variables
  private var started = false
  private var stopped = false

  //Methods
  /** Rus staring task if defined */
  def doStarting(): Unit = {
    pump.tool match{
      case task: OnStart ⇒
        log.debug("[DriveStartStop.doStarting] Try to run starting user function.")
        impeller ! Msg.RunTask[Unit]("DoStaring", pump.startFunctionTimeout, ()⇒{ task.doStart() })
      case _ ⇒
        log.debug("[DriveStartStop.doStarting] Starting user function not defined, nothing to do.")
        started = true}}
  /** Starting task done, set of started
    * @param name - String, "DoStaring"
    * @param execTime - FiniteDuration */
  def startingTaskDone(name: String, execTime: FiniteDuration): Unit = {
    log.debug(s"[DriveStartStop.startingTaskDone] name: $name, execTime: $execTime.")
    started = true}
  /** Starting task timeout, log to user console
    * @param name -  String, "DoStaring"
    * @param execTime - FiniteDuration */
  def startingTaskTimeout(name: String, execTime: FiniteDuration): Unit = {
    log.warning(s"[DriveStartStop.startingTaskTimeout] name: $name, execTime: $execTime.")
    userLogging ! Msg.LogWarning(toolName, s"Starting function timeout on $execTime, keep waiting.")}
  /** Starting task failed, set of started, log to user console
    * @param name - String, "DoStaring"
    * @param execTime - FiniteDuration
    * @param error - Throwable */
  def startingTaskFailed(name: String, execTime: FiniteDuration, error: Throwable): Unit = {
    log.error(s"[DriveStartStop.startingTaskTimeout] name: $name, execTime: $execTime, error: $error.")
    started = true
    userLogging ! Msg.LogError(toolName, Some(error), s"Starting function failed on $execTime.")}



   //TODO Здесь обработка функции завершкния




  /** Check if starting user function is executed
    * @return - true if started */
  def isStarted: Boolean = started
  /** Check if stopping user function is executed
    * @return - true if stopped*/
  def isStopped: Boolean = started

  //TODO Add more

}
