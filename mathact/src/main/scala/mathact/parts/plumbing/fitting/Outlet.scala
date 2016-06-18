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

/** Event collector
  * Created by CAB on 13.05.2016.
  */



trait Outlet[T] extends Plug[T] with Pipe[T]{
  //Variables
  private var pump: Option[Pump] = None
  //Service methods
  private[plumbing] def injectPump(pump: Pump): Unit = this.pump match{
    case Some(_) ⇒
      this.pump.foreach(_.log.warning("[Outlet.injectPump] Pump is already injected."))
    case None ⇒
      this.pump = Some(pump)}
  private[plumbing] def getPump:Option[Pump] = this.pump
  //Methods
  protected def push(value: T): Unit = {     //Вталкивание событий



    println("push" + value)




  }

}


