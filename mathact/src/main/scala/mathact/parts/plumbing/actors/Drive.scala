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
import mathact.parts.data.Msg
import mathact.parts.plumbing.fitting.{Inlet, Outlet}
import scala.collection.mutable.{Map ⇒ MutMap, Set ⇒ MutSet, Queue ⇒ MutQueue}


/** Manage impeller actor
  * Created by CAB on 15.05.2016.
  */

class Drive(pumping: ActorRef) extends BaseActor{
   //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){ case _: Exception ⇒ Resume }
  //Enums
  object WorkMode extends Enumeration {val Creating, Building, Starting, Work, Stopping = Value}
  //Definitions
  case class SubscriberData(drive: ActorRef,  inletId: Int, inletName: String)
  case class OutletData(id: Int, pipe: Outlet[_]){
    val subscribers = MutMap[ActorRef, SubscriberData]() //(subscribe tool drive, SubscriberData)

  }
  case class InletData(id: Int, pipe: Inlet[_]){
    val msgQueue = MutQueue[Any]()

  }
  //Variables
  var state = WorkMode.Creating
  var impeller: Option[ActorRef] = None
  var idCounter = 0
  val outlets = MutMap[Int, OutletData]()  //(Outlet ID, OutletData)
  val inlets = MutMap[Int, InletData]()    //(Inlet ID, OutletData)




  //Functions
  def nextId: Int = {idCounter += 1; idCounter}
  //Messages handling
  reaction(state){
    //Creating of new impeller
    case Msg.NewImpeller(componentName) ⇒
      //Create actor
      val impl = context.actorOf(Props(new Impeller(self)), "ImpellerOf" + componentName)
      context.watch(impl)
      impeller = Some(impl)
      //Response
      sender ! impl
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
    //Building
    case Msg.BuildDrive ⇒

      //TODO

      sender ! Msg.DriveBuilt
    //Starting
    case Msg.StartDrive ⇒

      //TODO

      sender ! Msg.DriveStarted


      //!!! Далее здесь:
      // 1) Подключения и отключения.
      // 2) При подключении до сообщения BuildDrive, добавлять подключение в список отложеных и по получении
      //    BuildDrive создавать подключение. Для подключений после BuildDrive создавть немедленно.
      //    Соответсвенно обрабатывать отключение.
      // 3) По StartDrive выпролнять пользоватльские функции инициализации (по средством импеллера).











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
    case x ⇒ println("[Drive] Unknown message " + x)

      //Если это импелер, нужно завершыть работу

        }
  }
