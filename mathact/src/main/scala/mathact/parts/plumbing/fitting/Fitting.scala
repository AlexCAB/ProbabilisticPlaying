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
import mathact.parts.plumbing.{Pump, fitting}


/** Contains definition for plumbing
  * Created by CAB on 14.05.2016.
  */

trait Fitting {

  private[mathact] val pump: Pump

  type Flange[T] = fitting.Flange[T]
  type Outlet[T] = fitting.Outlet[T]
  type Inlet[T] = fitting.Inlet[T]


  protected object Collector{
    def apply[T](in: Flange[T]*): Flange[T] = {


      in.head
    }

  }


  protected object Outlet{
    def apply[T,H](v: T with Outlet[H]): T with Flange[H] = {

      //Здесь  в Outlet должна инжектица Pump (можно росто добавить метод), который можно будет вызвать только раз
      //Чтобы не переопределяли помпу
      //И соответственно добавить медод получения помпы
      v
    }
  }





}
