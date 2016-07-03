///* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
// * @                                                                             @ *
// *           #          # #                                 #    (c) 2016 CAB      *
// *          # #      # #                                  #  #                     *
// *         #  #    #  #           # #     # #           #     #              # #   *
// *        #   #  #   #             #       #          #        #              #    *
// *       #     #    #   # # #    # # #    #         #           #   # # #   # # #  *
// *      #          #         #   #       # # #     # # # # # # #  #    #    #      *
// *     #          #   # # # #   #       #      #  #           #  #         #       *
// *  # #          #   #     #   #    #  #      #  #           #  #         #    #   *
// *   #          #     # # # #   # #   #      #  # #         #    # # #     # #     *
// * @                                                                             @ *
//\* *  http://github.com/alexcab  * * * * * * * * * * * * * * * * * * * * * * * * * */
//
//package mathact.parts
//import akka.actor.{Props, ActorRef, ActorSystem}
//import akka.event.Logging
//import mathact.parts.control.actors.Controller
//import mathact.parts.data.PumpEvents
//import mathact.parts.gui.JFXApplication
//import mathact.parts.plumbing.actors.Pumping
//
//import scala.concurrent.Future
//import scalafx.application.Platform
//import scala.concurrent.ExecutionContext.Implicits.global
//
//
///** Contain global services
//  * Created by CAB on 13.05.2016.
//  */
//
//class Environment {
//  //Parameters
//  val beforeTerminateTimeout = 1000 //In milliseconds
//  //Actor system
//  val system = ActorSystem("MathActActorSystem")
//  val akkaLog = Logging.getLogger(system, this)
//  //Stop proc
//  def doStop(exitCode: Int): Unit = Future{
//    akkaLog.debug(s"[Environment.doStop] Stopping of program, terminate timeout: $beforeTerminateTimeout milliseconds.")
//    Thread.sleep(beforeTerminateTimeout)
//    Platform.exit()
//    system.terminate().onComplete{_ ⇒ System.exit(exitCode)}}
//  //Actors
//
//  //!!! Акторы ниже нужно создавать отдельно для каждого запуженого Workbench (можно их перенести в отдельный
//  // класс и создавть его в главном контролеере керчев)
//  //!!! Здесь должен быть только главный контроллер приложения, управляющий запуском и остановкой скетчев.
//
//  //!!! Заметки:
//  // 1) Класс с pumping и controller бужет называтся SketchContext
//  // 2) SketchContext бедет конструироватся в главном контероллере по запросу (ask) из Workbench и возвращатся ему.
//  // 3) Главный консроллет буде парамтризровать SketchContext и управлять Workbench'ем через него.
//  // 4) Последоватльность запуска скетча такая: создаётся еземпляр класса Workbench (в отдельном Future чтобы не
//  //    блокировать обработку сообщений), из которого приходит запрос на,
//  //    получение SketchContext, SketchContext конструироется (в этот мометн также отображется интефейс скетч)
//  //    и возвращается Workbench, после чего конструирование (ваполненеи конструктора) продолжжается,
//  //    создаются инструменты и их помпы.
//  //    Если при конструровании (класса скетча внутри Future) произошли ошибки, либо оно небыло завершено вовремя,
//  //    главный констроллер сообщяет об этом SketchContext, который уничтожает все ранее созданые помпы,
//  //    отображет ошибку на UI скетча. По закритии окна скетча разрушается сам, програма возврашается к отображению
//  //    окна списка скетчей.
//
//
//
//  val pumping: ActorRef = system.actorOf(Props[Pumping], "PumpingActor")
//  val controller: ActorRef = system.actorOf(Props(new Controller(pumping, doStop)), "MainControllerActor")}
