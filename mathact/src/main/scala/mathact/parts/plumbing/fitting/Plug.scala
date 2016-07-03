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


/** Source of events, must be implemented by Outlet
  * Created by CAB on 17.05.2016.
  */

trait Plug[T] extends Connector[T] {
  //Get Outlet
  private val outlet = this match{
    case out: Outlet[T] ⇒ out
    case _ ⇒ throw new Exception(
      s"[Jack] This trait must be implemented only with mathact.parts.plumbing.fitting.Outlet, " +
      s"found implementation: ${this.getClass.getName}")}
  //Methods
  /** Connecting of this Plug to given Jack
    * @param jack - Jack[T] */
  def connectJack(jack: ⇒Jack[T]): Unit = {

    ???

  }
  /** Disconnecting of this Plug to given Jack
    * @param jack - Jack[T] */
  def disconnectJack(jack: ⇒Jack[T]): Unit = {

   ???

  }


}
