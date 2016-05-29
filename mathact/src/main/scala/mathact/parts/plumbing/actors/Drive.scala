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
import akka.event.Logging
import mathact.parts.ActorUtils
import mathact.parts.control.actors.Controller.StepMode
import mathact.parts.plumbing.PumpEvents
import mathact.parts.plumbing.PumpEvents.{Steady, Ready}


/** Manage impeller actor
  * Created by CAB on 15.05.2016.
  */

class Drive(pumping: ActorRef) extends Actor with ActorUtils{
  //Objects
  val log = Logging.getLogger(context.system, this)
  //Definitions
  private object WorkMode extends Enumeration {val Creating, Starting, Work, Stopping = Value}
  //Variables
  private var impeller: Option[ActorRef] = None
  private var stepMode = StepMode.None
  private var state = WorkMode.Creating
  //Messages handling
  def receive = {
    case PumpEvents.NewImpeller(componentName) ⇒
      logMsgD("Drive.PumpEvents", s"Creating impeller for: $componentName",state)
      //Create actor
      val impl = context.actorOf(Props(new Impeller(self)), "ImpellerOf" + componentName)
      context.watch(impl)
      impeller = Some(impl)
      //Response
      sender ! impl
    case Ready(initStepMode) ⇒
      logMsgD("Drive.Ready", s"Preparing to start...",state)
      //Set values
      stepMode = initStepMode
      state = WorkMode.Starting
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

      Thread.sleep(1000)


      pumping ! Steady



    case Terminated(actor) ⇒
      logMsgD("Drive.Terminated", s"Terminated actor: $actor", state)

      //Если это импелер, нужно завершыть работу

    case x ⇒
      logMsgW("Drive", "Receive unknown message: " + x, state)}
  //Supervisor strategy
  override val supervisorStrategy = OneForOneStrategy(){
    case _: Exception ⇒ Resume}}
