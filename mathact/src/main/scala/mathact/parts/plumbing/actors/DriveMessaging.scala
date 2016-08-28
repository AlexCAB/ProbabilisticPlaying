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
import scala.concurrent.duration.Duration


/** Handling of messages
  * Created by CAB on 22.08.2016.
  */

private [mathact] trait DriveMessaging { _: Drive ⇒
  //Functions

//  inlet.currentTask = Some(inlet.taskQueue.dequeue())
//  val task = Msg.RunTask(
//    id = inlet.inletId,
//    name = s"UserMessageTask",
//    timeout = pump.messageProcessingTimeout,
//    task = ()⇒{inlet.pipe.processValue(value)})



  private def enqueueMessageTask(inlet: InletState, value: Any): Unit = {
    val newRunTask = Msg.RunTask(
      id = inlet.inletId,
      name = s"UserMessageTaskOnStarting",
      timeout = pump.messageProcessingTimeout,
      task = ()⇒{inlet.pipe.processValue(value)})
    inlet.taskQueue.enqueue(newRunTask)
    log.debug(s"[DriveMessaging.userMessage] Task added to the queue, task: $newRunTask, queue: ${inlet.taskQueue}")}




  private def runNextMsgTask(): Unit = { //Return: Is started
    //Search for inlet with max queue size
    val maxQueueInlet = inlets.values match{
      case ins if ins.isEmpty ⇒ None
      case ins ⇒ ins.maxBy(_.taskQueue.size) match{
        case msi if msi.taskQueue.isEmpty ⇒ None
        case msi ⇒ Some(msi)}}
    //Run task
    maxQueueInlet match{
      case Some(inlet) ⇒
        val task = inlet.taskQueue.dequeue()
        inlet.currentTask = Some(task)
        log.debug(s"[DriveMessaging.runNextMsgTask] Run task: $task, from inlet: $inlet")
        impeller ! task
      case None ⇒
       log.debug(s"[DriveMessaging.runNextMsgTask] No more tasks to run.")}}







  private def runMessageTaskLoop(): Unit = { //Return: Is started
    //Check if not run already
    inlets.values.exists(_.currentTask.nonEmpty) match{
      case false ⇒
        log.debug(s"[DriveMessaging.runMessageTaskLoop] Run for first message.")
        runNextMsgTask()
      case true ⇒
        log.debug(s"[DriveMessaging.runMessageTaskLoop] Message task loop already runs.")}}



  private def cleanCurrentTask(inlet: InletState): Unit = {
    log.debug(s"[DriveMessaging.messageTaskDone] Executed task: ${inlet.currentTask}.")
    inlet.currentTask match{
      case Some(_) ⇒
        inlet.currentTask = None
      case None ⇒
        log.error(s"[DriveMessaging.messageTaskDone] Not set currentTask, inlet: $inlet.")}}










  //TODO Для yменьшения количества отправленых DriveLoad, нужно использовать что то вроде ПИ
  //TODO регулятора с мёртвой зоной (чтобы DriveLoad рассылалась не по каждому измению размера очереди).
  //TODO Сейчас отправка DriveLoad на каждое измение размера очерели инлета.
  private def sendLoadMessage(inlet: InletState, initQueueSize: Int): Unit = {
    //Check if queue size changed
    initQueueSize != inlet.taskQueue.size match{
      case true ⇒
        //Send load messages
        inlet.publishers.values.foreach{ pub ⇒
          val load = inlet.taskQueue.size
          log.debug(s"[DriveMessaging.sendLoadMessage] Send DriveLoad($load), to publisher: $pub.")
          pub.toolDrive ! Msg.DriveLoad(self, load)}
      case false ⇒
        //Queue size not changed
        log.debug(s"[DriveMessaging.sendLoadMessage] Not send, taskQueue.size is not changed.")}}



  //Methods
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
          //Add task to the queue and reply with load message
          val initQueueSize = inlet.taskQueue.size
          enqueueMessageTask(inlet, value)
          sendLoadMessage(inlet, initQueueSize)
        case ActorState.Working | ActorState.Stopping ⇒
          //Put in queue, start processing and reply with DriveLoad
          val initQueueSize = inlet.taskQueue.size
          enqueueMessageTask(inlet, value)
          runMessageTaskLoop()
          sendLoadMessage(inlet, initQueueSize)
        case s ⇒
          //Incorrect state
          log.error(s"[DriveMessaging.userMessage] Incorrect state: $s, required Starting, Working or Stopping")}
      case None ⇒
        //Incorrect inletId
        log.error(s"[DriveMessaging.userMessage] Inlet with inletId: $inletId, not exist.")}
  /** Starting of user messages processing */
  def startUserMessageProcessing(): Unit = {
    //Run for firs message
    log.error(s"[DriveMessaging.startUserMessageProcessing] Call runMessageTaskLoop().")
    runMessageTaskLoop()}
  /** Message processing done, run next task
    * @param inletId - Int
    * @param execTime - Duration */
  def messageTaskDone(inletId: Int, execTime: Duration): Unit = inlets.get(inletId) match{
    case Some(inlet) ⇒
      //Remove current, run next task and send load message
      val initQueueSize = inlet.taskQueue.size
      cleanCurrentTask(inlet)
      runNextMsgTask()
      sendLoadMessage(inlet, initQueueSize)
    case None ⇒
      //Incorrect inletId
      log.error(s"[DriveMessaging.messageTaskDone] Inlet with inletId: $inletId, not exist.")}





  def messageTaskTimeout(inletId: Int, execTime: Duration): Unit = {

    ???

  }

  def messageTaskFailed(inletId: Int, execTime: Duration, error: Throwable): Unit = {

    ???

  }






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
