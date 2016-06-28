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


/** Manage impeller actor
  * Created by CAB on 15.05.2016.
  */

class Drive(pumping: ActorRef) extends BaseActor{
   //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){
    case _: Exception ⇒ Resume}
  //Definitions
  private object WorkMode extends Enumeration {val Creating, Starting, Work, Stopping = Value}
  //Variables
  private var impeller: Option[ActorRef] = None
//  private var stepMode = StepMode.None
  private var state = WorkMode.Creating
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

      //Проверка зарегестрирован ли уже Outlet, если зарегистрирован ничего не делать

      //TODO

      sender ! Right(true) //isAdded
    //Adding of Inlet
    case Msg.AddInlet(pipe) ⇒

      //TODO

      sender ! Right(true) //isAdded
    //Building
    case Msg.BuildDrive ⇒

      //TODO

      sender ! Msg.DriveBuilt
    //Starting
    case Msg.StartDrive ⇒

      //TODO

      sender ! Msg.DriveStarted


      //!!! Далее здесь:
      // 1) По Msg.AddOutlet и Msg.BuildDrive создание соответсвующей инфраструктуры (список подписчиков для выхода,
      //    очередь сообщений для входа)
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
