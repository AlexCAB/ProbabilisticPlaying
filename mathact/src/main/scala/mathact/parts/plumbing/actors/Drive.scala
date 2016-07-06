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
import mathact.parts.data.{PipeData, Msg}
import mathact.parts.data.Msg.Connectivity
import mathact.parts.plumbing.Pump
import mathact.parts.plumbing.fitting.{Jack, Plug, Inlet, Outlet}
import scala.collection.mutable.{Map ⇒ MutMap, Queue ⇒ MutQueue}
import scala.concurrent.duration._


/** Manage tool
  * Created by CAB on 15.05.2016.
  */

class Drive(pump: Pump, toolName: String, pumping: ActorRef) extends BaseActor{
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){ case _: Exception ⇒ Resume }
  //Enums
  object WorkMode extends Enumeration {val Creating, Building, Starting, Work, Stopping = Value}
  //Definitions
  case class OutletData(id: Int, pipe: Outlet[_]){
    val subscribers = MutMap[(ActorRef, Int), PipeData]() //((subscribe tool drive, inlet ID), SubscriberData)

  }
  case class InletData(id: Int, pipe: Inlet[_]){
    val msgQueue = MutQueue[Any]()
    val publishers = MutMap[(ActorRef, Int), PipeData]() //((publishers tool drive, outlet ID), SubscriberData)


  }
  //Variables
  var state = WorkMode.Creating
//  var impeller: Option[ActorRef] = None
  var idCounter = 0
  val outlets = MutMap[Int, OutletData]()  //(Outlet ID, OutletData)
  val inlets = MutMap[Int, InletData]()    //(Inlet ID, OutletData)
  val pendingConnections = MutQueue[Connectivity]()


  //Worker actor
  val impeller = context.actorOf(Props(new Impeller(self)), "ImpellerOf" + toolName)
  context.watch(impeller)
  //Functions
  def nextId: Int = {idCounter += 1; idCounter}
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
  reaction(state){
//    //Creating of new impeller
//    case Msg.NewImpeller(componentName) ⇒
//      //Create actor
//      val impl = context.actorOf(Props(new Impeller(self)), "ImpellerOf" + componentName)
//      context.watch(impl)
//      impeller = Some(impl)
//      //Response
//      sender ! impl
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
      case WorkMode.Creating ⇒ pendingConnections += Msg.ConnectPipes(out, in)
      case WorkMode.Building | WorkMode.Starting | WorkMode.Work ⇒ doConnect(out, in)
      case s ⇒ log.error(s"[ConnectPipes] Connecting in state $s is not allowed.")}
    //Disconnecting
    case Msg.DisconnectPipes(out, in) ⇒ state match{
      case WorkMode.Creating ⇒ pendingConnections += Msg.DisconnectPipes(out, in)
      case WorkMode.Building | WorkMode.Starting | WorkMode.Work ⇒ doDisconnect(out, in)
      case s ⇒ log.error(s"[ConnectPipes] Dis connecting in state $s is not allowed.")}
    //Building
    case Msg.BuildDrive ⇒
      pendingConnections.foreach{
        case Msg.ConnectPipes(out, in) ⇒ doConnect(out, in)
        case Msg.DisconnectPipes(out, in) ⇒ doDisconnect(out, in)}
      state = WorkMode.Building
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
        outlet.subscribers += ((inlet.toolDrive, inlet.pipeId) → inlet)
        log.info(s"[ConnectTo] Connection added, from: $outlet, to: $inlet")
      case None ⇒ log.error(s"[ConnectTo] Outlet with id: $outletId, not exist.")}
    //Disconnect from given inlet
    case Msg.DisconnectFrom(outletId, inlet) ⇒ outlets.get(outletId) match{
      case Some(outlet) ⇒ outlet.subscribers.contains((inlet.toolDrive, inlet.pipeId)) match{
        case true ⇒
          outlet.subscribers -= Tuple2(inlet.toolDrive, inlet.pipeId)
          inlet.toolDrive ! Msg.DelConnection(inlet.pipeId, outlet.pipe.getPipeData)
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
      state = WorkMode.Starting
      impeller ! Msg.RunTask("Starting", ()⇒pump.toolStart())



        //!!! Здесь логика обработки пользоватльских сообщений (так же через запуск задачи в impeller)



    //Task done
    case Msg.TaskDone(name) ⇒ state match{
      case WorkMode.Starting ⇒
        //User onStart function successfully executed.
        state = WorkMode.Work
        pumping ! Msg.DriveStarted
      case WorkMode.Work ⇒

        ???

      case WorkMode.Stopping ⇒

        ???

      case _ ⇒}
    //Task failed
    case Msg.TaskFailed(name, error) ⇒ state match{
      case WorkMode.Starting ⇒
        //User out on run user onStart function




      case WorkMode.Work ⇒

        ???

      case WorkMode.Stopping ⇒

        ???

      case _ ⇒}













      //!!! Далее здесь:
      // 1) Обьмен пользовательскими сообщениями (очереди, обратное давление)
      // 2) Заврешение рабоаты скетча.











//    case Ready(initStepMode) ⇒
//      //Set values
//      stepMode = initStepMode
//      state = WorkMode.Starting
      //

      //TODO ???


      //Далее: отложениое полключение, подключение и отключение, предача значений (очреди значений),
      //алгоритм обратного давления






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
