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

package mathact.parts.plumbing.fitting

import mathact.parts.plumbing.Pump


/** Base class for Outlet and Inlet
  * Created by CAB on 09.05.2016.
  */

trait Pipe[T]{
  //Variables
  private var pump: Option[Pump] = None
  private var pipeId: Option[Int] = None
  //Service methods
  private[plumbing] def injectPump(pump: Pump, pipeId: Int): Unit = this.pump match{
    case Some(_) ⇒
      this.pump.foreach(_.akkaLog.warning(s"[Outlet.injectPump] Pump is already injected to $this"))
    case None ⇒
      pump.akkaLog.debug(s"[Outlet.injectPump] Injected to $this.")
      this.pump = Some(pump)
      this.pipeId = Some(pipeId)}
  //Logging methods
  private[mathact] object log {
    def debug(msg: String): Unit = pump.foreach(_.akkaLog.debug(s"[$this] $msg"))
    def info(msg: String): Unit = pump.foreach(_.akkaLog.info(s"[$this] $msg"))
    def warning(msg: String): Unit = pump.foreach(_.akkaLog.warning(s"[$this] $msg"))
    def error(msg: String): Unit = pump.foreach(_.akkaLog.error(s"[$this] $msg"))}
  //Messaging methods
  private[mathact] def tellTo


//  private[plumbing] def connectPlug(plug: ⇒Plug[T]): Unit = {}
//  private[plumbing] def disconnectPlug(plug: ⇒Plug[T]): Unit = {}
//  private[plumbing] def connectJack(socket: ⇒Jack[T]): Unit = {}
//  private[plumbing] def disconnectJack(socket: ⇒Jack[T]): Unit = {}






  //  private[plumbing] def getPump:Option[Pump] = this.pump

  //!!! Здесь должны быть служебные методы для работы с Pump (которая должна остатся приватной по возможности),
  //    в частности метод отправки пользовательского сообщения по def push(v).





}
