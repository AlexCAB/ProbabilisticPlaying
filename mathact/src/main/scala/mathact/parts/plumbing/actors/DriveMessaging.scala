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
import mathact.parts.data.{ActorState, Msg}


/** Handling of messages
  * Created by CAB on 22.08.2016.
  */

private [mathact] trait DriveMessaging { _: Drive ⇒
  /** User data from self outlet, send to all outlet subscribers
    * @param outletId - Int, source ID
    * @param value -  Any, user data */
  def userDataAsk(outletId: Int, value: Any, state: ActorState): Either[Throwable, Option[Long]] = state match{
      case ActorState.Starting | ActorState.Working | ActorState.Stopping ⇒
        //Get of outlet
        outlets.get(outletId) match{
          case Some(outlet) ⇒
            //Distribution of UserMessage
            outlet.subscribers.values.foreach{ inlet ⇒
              inlet.toolDrive ! Msg.UserMessage(outletId, inlet.pipeId, value)}
            //Push timeout
            log.debug(
              s"[DriveMessaging.userDataAsk] Data: $value, sent from outletId: $outletId to " +
              s"all subscribers ${outlet.subscribers} , pushTimeout: ${outlet.pushTimeout}")
            //Return pour timeout
            Right(outlet.pushTimeout)
          case None ⇒
            Left(new IllegalArgumentException(
              s"[DriveMessaging.userDataAsk] Outlet with outletId: $outletId, not exist."))}
      case s ⇒
        //Incorrect state
        val msg =
          s"[DriveMessaging.userDataAsk] User data can be processed only in Starting Working, " +
          s"or Stopping state, current state: $s"
        log.error(msg)
        Left(new IllegalStateException(msg))}
  /** User message from other outlet to self inlet, set to queue
    * @param outletId - Int, source ID
    * @param inletId - Int, drain ID
    * @param value - Any, user data */
  def userMessage(outletId: Int, inletId: Int, value: Any, state: ActorState): Unit = inlets.get(inletId) match{
    case Some(inlet) ⇒
      //Check state
      state match{
        case ActorState.Starting ⇒
          //Reply with load message
          inlet.publishers.values.foreach(_.toolDrive ! Msg.DriveLoad(self, inlet.taskQueue.size))
          //Add task to the queue
          val newRunTask = MessageProcTask(inlet.inletId, publisher = (sender, outletId), value)
          inlet.taskQueue += newRunTask
          log.debug(s"[DriveMessaging.userMessage] Task added to the queue, task: $newRunTask, queue: ${inlet.taskQueue}")
        case ActorState.Working | ActorState.Stopping ⇒
          //Put in queue, start processing and reply with DriveLoad

            //TODO!!! Для Starting использован простой алгоритм рассылки DriveLoad каждому публешеру,
            //TODO    здесь-же для уменьшения количества отправленых DriveLoad, нужно использовать что то вроде ПИ
            //TODO    регулятора с мёртвой зоной (чтобы DriveLoad рассылалась не по каждому измению размера очереди).


          ???

        case s ⇒
          //Incorrect state
          log.error(s"[DriveMessaging.userMessage] Incorrect state: $s, required Starting, Working or Stopping")}
      case None ⇒
        //Incorrect inletId
        log.error(s"[DriveMessaging.userMessage] Inlet with outletId: $inletId, not exist.")}






//    ???
//
//    case Msg.UserMessage(outletId, inletId, value) ⇒
//
//
//
//
//
//    inlets.get(inletId) match{
//      case Some(inlet) ⇒
//        //Reply with load message
//        val maxQueueSize = inlets.values.map(_.taskQueue.size).max
//        sender ! Msg.DriveLoad(self, maxQueueSize)
//        //Add task to the queue
//        val newRunTask = MessageProcTask(nextLongId, inlet, publisher = (sender, outletId), value)
//        inlet.taskQueue += newRunTask
//        log.debug(s"[UserMessage] Task added to the queue, task: $newRunTask, queue: ${inlet.taskQueue}")
//        //If queue is empty and state is Running, send task to impeller
//        (maxQueueSize, workMode) match{
//          case (0, WorkMode.Runned) ⇒
//            runNextMsgTask()
//          case _ ⇒
//            log.debug(s"[UserMessage] Task not send to impeller, maxQueueSize: $maxQueueSize, workMode: $workMode")}
//      case None ⇒ log.error(s"[UserMessage] Inlet with outletId: $inletId, not exist.")}
//
















  /** Inlet drive load, update back pressure time out for given drive.
    * @param drive - ActorRef, queue of which drive is
    * @param maxQueueSize - Int */
  def driveLoad(drive: ActorRef, maxQueueSize: Int): Unit = {

    ???

  }



}
