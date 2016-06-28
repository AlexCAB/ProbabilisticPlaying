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
  //Service methods
  private[plumbing] def injectPump(pump: Pump): Unit = this.pump match{
    case Some(_) ⇒
      this.pump.foreach(_.log.warning(s"[Outlet.injectPump] Pump is already injected to $this"))
    case None ⇒
      pump.log.debug(s"[Outlet.injectPump] Injected to $this.")
      this.pump = Some(pump)}
  private[plumbing] def getPump:Option[Pump] = this.pump





}
