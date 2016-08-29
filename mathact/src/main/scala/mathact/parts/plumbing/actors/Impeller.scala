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
import mathact.parts.ActorBase
import mathact.parts.data.{TaskKind, Msg}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}


/** User code processor
  * Created by CAB on 15.05.2016.
  */

class Impeller(drive: ActorRef) extends ActorBase{
  //Messages
  case class TaskTimeout(taskNumber: Long, timeout: FiniteDuration)
  case class TaskSuccess(taskNumber: Long, res: Any)
  case class TaskFailure(taskNumber: Long, err: Throwable)
  //Variables
  var taskCounter = 0L
  var currentTask: Option[(Long, TaskKind, Int, Long)] = None // (task number, task kind, task ID, start time)
  //Messages handling
  def reaction = {
    //Starting of task in separate thread and start of task timeout
    case Msg.RunTask(kind, id, timeout, task) if sender == drive ⇒ currentTask match{
      case None ⇒
        val taskNumber = {taskCounter += 1; taskCounter}
        log.debug(s"[Impeller.RunTask] Try to run task, taskNumber: $taskNumber, kind: $kind, ID: $id")
        context.system.scheduler.scheduleOnce(timeout, self, TaskTimeout(taskNumber, timeout))
        currentTask = Some((taskNumber, kind, id, System.currentTimeMillis))
        Future{task()}.onComplete{
          case Success(res) ⇒ self ! TaskSuccess(taskNumber, res)
          case Failure(err) ⇒ self ! TaskFailure(taskNumber, err)}
      case Some((curNum, curKind, curId, startTime)) ⇒
        val msg =
          s"[Impeller.RunTask] Can't run new task $kind kind with ID $id, " +
          s"since current task $kind with ID $id is not done."
        log.error(msg)
        drive ! Msg.TaskFailed(kind, id, (System.currentTimeMillis - startTime).millis, new Exception(msg))}
    //Remove current task
    case Msg.SkipCurrentTask if sender == drive ⇒ currentTask match{
      case Some((curNum, curKind, curId, startTime)) ⇒
        val msg = s"[Impeller.SkipCurrentTask] Current task will skip, number: $curNum, kind: $curKind, DI: $curId"
        log.warning(msg)
        currentTask = None
        drive ! Msg.TaskFailed(curKind, curId, (System.currentTimeMillis - startTime).millis, new Exception(msg))
      case None ⇒
        log.debug("[Impeller.SkipCurrentTask] Nothing to skip.")}
    //Task timeout, send time out and restart timer
    case TaskTimeout(taskNumber, timeout) ⇒ currentTask match{
      case Some((`taskNumber`, kind, id, startTime)) ⇒
        log.debug(
          s"[Impeller.TaskTimeout] Task timeout, kind: $kind, id: $id, taskNumber: $taskNumber, after $timeout wait.")
        drive ! Msg.TaskTimeout(kind, id, (System.currentTimeMillis - startTime).millis)
        context.system.scheduler.scheduleOnce(timeout, self, TaskTimeout(taskNumber, timeout))
      case _ ⇒
        log.debug("[Impeller.TaskTimeout] Task done or skip, stop timer.")}
    //Task done, send report to driver
    case TaskSuccess(taskNumber, res) ⇒ currentTask match{
      case Some((curNum, kind, id, startTime)) ⇒
        val execTime = (System.currentTimeMillis - startTime).millis
        log.debug(
          s"[Impeller.TaskSuccess] Task done, number: $curNum, kind: $kind, id: $id, res: $res, execTime: $execTime" )
        currentTask = None
        drive ! Msg.TaskDone(kind, id, execTime, res)
      case None ⇒
        log.warning(
          s"[Impeller.TaskSuccess] Completed not a current task (probably current been skipped), " +
            s"taskNumber: $taskNumber.")}
    //Task failed, send report to driver
    case TaskFailure(taskNumber, err) ⇒ currentTask match{
      case Some((curNum, curKind, curId, startTime)) ⇒
        val execTime = (System.currentTimeMillis - startTime).millis
        log.debug(
          s"[Impeller.TaskFailure] Task fail, number: $curNum, kind: $curKind, ID: $curId, " +
          s"err: $err, execTime: $execTime" )
        currentTask = None
        drive ! Msg.TaskFailed(curKind, curId, execTime, err)
      case None ⇒
        log.warning(
          s"[Impeller.TaskFailure] Failed not a current task (probably current been skipped), " +
          s"taskNumber: $taskNumber.")}}}
