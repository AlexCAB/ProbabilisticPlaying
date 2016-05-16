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

/** Event collector
  * Created by CAB on 13.05.2016.
  */


object Outlet{
  def apply[T,H](v: T with Outlet[H]): T with Flange[H] = {

    //Здесь  в Outlet должна инжектица Pump (можно росто добавить метод), который можно будет вызвать только раз
    //Чтобы не переопределяли помпу
    //И соответственно добавить медод получения помпы
    v
  }
}

trait Outlet[T] extends Flange[T] with Pipe[T]{

  protected def push(value: T): Unit = {     //Вталкивание событий

    ???


  }

}


