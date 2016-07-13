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

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import mathact.parts.BaseActor
import mathact.parts.data.{StepMode, PipeData, Msg}
import mathact.parts.data.Msg.Connectivity
import mathact.parts.plumbing.Pump
import mathact.parts.plumbing.fitting.{Jack, Plug, Inlet, Outlet}
import scala.collection.mutable.{Map ⇒ MutMap, Queue ⇒ MutQueue}
import scala.concurrent.duration._


/** Manage tool
  * Inlets and outlets never removes
  * Created by CAB on 15.05.2016.
  */

class Drive(pump: Pump, toolName: String, pumping: ActorRef) extends BaseActor{
  //Parameter
  val pushTimeoutCoefficient = 10  // pushTimeout = maxQueueSize * pushTimeoutCoefficient
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){ case _: Exception ⇒ Resume }
  //Enums
  object State extends Enumeration {val Creating, Building, Starting, Work, Stopping = Value}
  //Definitions
  case class OutletData(id: Int, pipe: Outlet[_]){
    val subscribers = MutMap[(ActorRef, Int), PipeData]() //((subscribe tool drive, inlet ID), SubscriberData)

  }
  trait Task
  object Disconnect extends Task
  case class MessageProcTask(inlet: InletData, publisher: (ActorRef, Int), value: Any) extends Task{ //publisher: (drive, outletId)
    def toRunTask:Msg.RunTask = Msg.RunTask(
      id = inlet.id,
      name = s"[UserMessage] publisher: $publisher, inletId: ${inlet.id}, value: $value",
      task = ()⇒{inlet.pipe.processValue(value)})




  }
  case class InletData(id: Int, pipe: Inlet[_]){
    val taskQueue = MutQueue[Task]()
    val publishers = MutMap[(ActorRef, Int), PipeData]() //((publishers tool drive, outlet ID), SubscriberData)


  }
  case class DrivesData(drive: ActorRef){
   var driveLoad: Int = 0


  }
  //Variables
  var state = State.Creating
  var mode = StepMode.Paused
//  var impeller: Option[ActorRef] = None
  val outlets = MutMap[Int, OutletData]()  //(Outlet ID, OutletData)
  val inlets = MutMap[Int, InletData]()    //(Inlet ID, OutletData)
  val subscribedDrives = MutMap[ActorRef, DrivesData]()
  val pendingConnections = MutQueue[Connectivity]()
  var pushTimeout: Option[Long] = None   //Time out after each push (depend from current back pressure)


  //Worker actor
  val impeller = context.actorOf(Props(new Impeller(self)), "ImpellerOf" + toolName)
  context.watch(impeller)
  //Functions

  def doConnect(out: ()⇒Plug[_], in: ()⇒Jack[_]): Unit = (out(),in()) match{
    case (o: Outlet[_], i: Inlet[_]) ⇒
      val p = i.getPipeData
      p.toolDrive ! Msg.AddConnection(p.pipeId, o.getPipeData)
    case (o, i) ⇒
      log.error(s"[ConnectPipes.doConnect] Plug or Jack is not an instance of Outlet[_] or Inlet[_], out: $o, in: $i.")}
  def doDisconnect(out: ()⇒Plug[_], in: ()⇒Jack[_]): Unit = (out(),in()) match{
    case (o: Outlet[_], i: Inlet[_]) ⇒
      val p = o.getPipeData
      p.toolDrive ! Msg.DisconnectFrom(p.pipeId, i.getPipeData)
    case (o, i) ⇒
      log.error(s"[ConnectPipes.doConnect] Plug or Jack is not an instance of Outlet[_] or Inlet[_], out: $o, in: $i.")}




  //Messages handling
  reaction((state, mode)){
    //Adding of Outlet
    case Msg.AddOutlet(pipe) ⇒
      //Check if already registered
      val outletId = outlets.values.filter(_.pipe == pipe) match{
        case Nil ⇒
          //Create and add
          val id = nextId
          outlets += (id → OutletData(id, pipe))
          log.debug(s"[AddOutlet] Outlet: $pipe, added with ID: $id")
          id
        case o :: _ ⇒
          //Double creating
          log.warning(s"[AddOutlet] Outlet: $pipe, is registered more then once")
          o.id}
      sender ! Right(outletId)
    //Adding of Inlet
    case Msg.AddInlet(pipe) ⇒
      //Check if already registered
      val inletId = inlets.values.filter(_.pipe == pipe) match{
        case Nil ⇒
          //Create and add
          val id = nextId
          inlets += (id → InletData(id, pipe))
          log.debug(s"[AddInlet] Inlet: $pipe, added with ID: $id")
          id
        case o :: _ ⇒
          //Double creating
          log.warning(s"[AddInlet] Inlet: $pipe, is registered more then once")
          o.id}
      sender ! Right(inletId)
    //Connecting
    case Msg.ConnectPipes(out, in) ⇒ state match{
      case State.Creating ⇒ pendingConnections += Msg.ConnectPipes(out, in)
      case State.Building | State.Starting | State.Work ⇒ doConnect(out, in)
      case s ⇒ log.error(s"[ConnectPipes] Connecting in state $s is not allowed.")}
    //Disconnecting
    case Msg.DisconnectPipes(out, in) ⇒ state match{
      case State.Creating ⇒ pendingConnections += Msg.DisconnectPipes(out, in)
      case State.Building | State.Starting | State.Work | State.Stopping ⇒ doDisconnect(out, in)
      case s ⇒ log.error(s"[ConnectPipes] Dis connecting in state $s is not allowed.")}
    //Building
    case Msg.BuildDrive ⇒
      pendingConnections.foreach{
        case Msg.ConnectPipes(out, in) ⇒ doConnect(out, in)
        case Msg.DisconnectPipes(out, in) ⇒ doDisconnect(out, in)}
      state = State.Building
      sender ! Msg.DriveBuilt
    //Add new connection
    case Msg.AddConnection(inletId, outlet) ⇒ inlets.get(inletId) match{
      case Some(inlet) ⇒
        inlet.publishers += ((outlet.toolDrive, outlet.pipeId) → outlet)
        outlet.toolDrive ! Msg.ConnectTo(outlet.pipeId, inlet.pipe.getPipeData)
      case None ⇒ log.error(s"[AddConnection] Inlet with id: $inletId, not exist.")}
    //Connect to given inlet
    case Msg.ConnectTo(outletId, inlet) ⇒ outlets.get(outletId) match{
      case Some(outlet) ⇒
        val inDrive = inlet.toolDrive
        outlet.subscribers += ((inDrive, inlet.pipeId) → inlet)
        subscribedDrives.getOrElse(inDrive, {subscribedDrives += (inDrive → DrivesData(inDrive))})
        log.info(s"[ConnectTo] Connection added, from: $outlet, to: $inlet")
      case None ⇒ log.error(s"[ConnectTo] Outlet with id: $outletId, not exist.")}
    //Disconnect from given inlet
    case Msg.DisconnectFrom(outletId, inlet) ⇒ outlets.get(outletId) match{
      case Some(outlet) ⇒ outlet.subscribers.contains((inlet.toolDrive, inlet.pipeId)) match{
        case true ⇒
          //Remove inlet from subscribers
          outlet.subscribers -= Tuple2(inlet.toolDrive, inlet.pipeId)
          //If no more subscribe inlets with this drive, remove drive from subscribedDrives
          outlets.exists(_._2.subscribers.exists{case (_,p) ⇒ p.toolDrive == inlet.toolDrive}) match{
            case false ⇒ subscribedDrives -= inlet.toolDrive
            case _ ⇒}
          //Allow inlet to remove self outlet from publishers list
          inlet.toolDrive ! Msg.DelConnection(inlet.pipeId, outlet.pipe.getPipeData)
          log.info(
            s"[DisconnectFrom] Inlet: $inlet removed, outlet subscribers: ${outlet.subscribers}, " +
            s"subscribedDrives: $subscribedDrives ")
        case false ⇒
          log.error(s"[DisconnectFrom] Inlet not in subscribers list, inlet: $inlet")}
      case None ⇒ log.error(s"[DisconnectFrom] Outlet with id: $outletId, not exist.")}
    //Delete disconnected connection
    case Msg.DelConnection(inletId, outlet) ⇒ inlets.get(inletId) match{
      case Some(inlet) ⇒ inlet.publishers.contains((outlet.toolDrive, outlet.pipeId)) match{
        case true ⇒
          inlet.publishers -= Tuple2(outlet.toolDrive, outlet.pipeId)
          log.info(s"[DelConnection] Connection deleted, from: $outlet, to: $inlet")
        case false ⇒
          log.error(s"[DelConnection] Outlet not in publishers list, outlet: $inlet")}
      case None ⇒ log.error(s"[DelConnection] Inlet with id: $inletId, not exist.")}
    //Starting
    case Msg.StartDrive ⇒
      state = State.Starting
      impeller ! Msg.RunTask(0, "Starting", ()⇒pump.toolStart())
    //Updating of step mode
    case Msg.SetStepMode(newMode) ⇒
      mode = newMode
      sender ! Msg.StepModeIsSet
    //Run of user message processing
    case Msg.DriveGo ⇒ mode match{
      case StepMode.Stepping ⇒

        //Выбор одной задачи-сообщения из акждой очереди и ваполение, по выполении всех задачь отправка DriveDone
        //!!! Возможно можно просто отправить все в очередь импелера
        //!!! Но тогда нужно некоторое уникальное ID задачи чтобы отслежывать их выполение

      case StepMode.Walking ⇒

        //Выбор одной задачи-сообщения из каждой очереди и ваполение, БЕЗ отправка DriveDone

      case StepMode.Running ⇒

        //Отправка одной задачи в на выполение, и позавершении будет отправлена следующая, т.е. образуется
        // цикл выполения задачь


      case _ ⇒}

      //!!!!!!! Лучше всего убрать DriveGo (заменть на DriveStep) и DriveStay, Запуск цикла непрерывной обработки сообщений
      // будет запускатся сразу по получении SetStepMode(Running), и останавоиватся при смене ражыма на любой другой
      // (но нужно ещё подумаь как это сделать проще)



    //Далее здесь:
    // 1) Обдумать как лучше и реализовать DriveGo для разных режымов
    // 2) Релизовать Msg.TaskDone(id, name), действие в зависимости от режыма работа.
    // 3) Заврешение рабоаты скетча.








    //Stop of user message processing
    case Msg.DriveStay ⇒


     ???


    //!!! Здесь сообщения управления режымом работы и шагами




    //Pushed user data, send to all subscribers
    case Msg.UserData(outletId, value) ⇒ sender ! (state match{
      case State.Work ⇒ outlets.get(outletId) match{
        case Some(outlet) ⇒
          //Distribution of UserMessage
          outlet.subscribers.values.foreach{ inlet ⇒
            inlet.toolDrive ! Msg.UserMessage(outletId, inlet.pipeId, value)}
          log.debug(
            s"[UserData] Data: $value, sent from outletId: $outletId to ${outlet.subscribers.size} " +
              s"subscribers, pushTimeout: $pushTimeout")
          //Return push timeout
          Right(pushTimeout)
        case None ⇒
          Left(new IllegalArgumentException(
            s"[UserData] Outlet with id: $outletId, not exist."))}
      case _ ⇒
        Left(new IllegalStateException(
          s"[UserData] User data can be processed only in Stepping or Running state, current state: $state"))})
    //Sent user data from other drive
    case Msg.UserMessage(outletId, inletId, value) ⇒ inlets.get(inletId) match{
      case Some(inlet) ⇒
        //Reply with load message
        val maxQueueSize = inlets.values.map(_.taskQueue.size).max
        sender ! Msg.DriveLoad(self, maxQueueSize)
        //Add task to the queue
        val newRunTask = MessageProcTask(inlet, publisher = (sender, outletId), value)
        inlet.taskQueue += newRunTask
        log.debug(s"[UserMessage] Task added to the queue, task: $newRunTask, queue: ${inlet.taskQueue}")
        //If queue is empty and state is Running, send task to impeller
        (maxQueueSize, mode) match{
          case (0, StepMode.Running) ⇒
            impeller ! newRunTask.toRunTask
          case _ ⇒
            log.debug(s"[UserMessage] Task not send to impeller, maxQueueSize: $maxQueueSize, state: $state")}
      case None ⇒ log.error(s"[UserMessage] Inlet with id: $inletId, not exist.")}
    //Other drive load
    case Msg.DriveLoad(drive, maxQueueSize) ⇒
      subscribedDrives.get(drive) match{
        case Some(driveData) ⇒
          //Update drive load
          driveData.driveLoad = maxQueueSize
          //Evaluate next push timeout
          subscribedDrives.values.map(_.driveLoad).max match{
            case 0 ⇒
              pushTimeout = None
            case n ⇒
              pushTimeout = Some(n * pushTimeoutCoefficient)}
          log.debug(s"[DriveLoad] maxQueueSize: $maxQueueSize, new pushTimeout: $pushTimeout, inlet drive actor: $drive")
        case None ⇒
          log.debug(s"[DriveLoad] Drive not subscribed, actor: $drive")}

































    //???


//      sender ! Right(Some(1000L))







        //!!! Здесь логика обработки пользоватльских сообщений (так же через запуск задачи в impeller)



    //Task done
    case Msg.TaskDone(id, name) ⇒ state match{
      case State.Starting ⇒
        //User onStart function successfully executed.
        state = State.Work
        pumping ! Msg.DriveStarted
      case State.Work ⇒
        //Remove task from queue


        //If mode: Stepping → reply to Pumping, Running → run next task


        //По обработке сообщения


        //

        ???


      case State.Stopping ⇒

        //По обработке пользовательская функция останова

        ???

      case _ ⇒}
    //Task failed
    case Msg.TaskFailed(id, name, error) ⇒ state match{
      case State.Starting ⇒
        //User out on run user onStart function

        //Если пользовательская функция запуска завершилась неудачно

        ???

      case State.Work ⇒

        //Если обработка сообщения завершилась неудачно

        ???

      case State.Stopping ⇒

        //Если пользовательская функция останова завершилась неудачно

        ???

      case _ ⇒}





















//    case Ready(initStepMode) ⇒
//      //Set values
//      stepMode = initStepMode
//      state = State.Starting
      //

      //TODO ???







      //!!! Не будет возможности отследить динамическое подключение и отключение труб,
      // так как идея в том что инструмент должен быть всегда (после отработки инит функции) готов получать значеия
      // по любому из входов (не имея информации о том подключен ли он и сколько подключений там (может быть нсколько
      // подключений, в этом случае значения просто смешываются и подаются на обработчик входа)).
      //!!! Послка значений по трубами (вызовы обработчиков) насинаются сразу по завршени инит (во время инит могут быть
      // посланы сообщения но доставлены будут толко когда отработает инит адресата). Так же сообщения престают
      // отпаралятся сразу перед отработкой стоп функции.

//      Thread.sleep(1000)
//
//
//      pumping ! Steady
//
//
//
//    case Terminated(actor) ⇒

  }
}
