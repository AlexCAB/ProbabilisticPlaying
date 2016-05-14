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

package mathact.parts

import mathact.parts.plumbing.Pump


/** Contains definition for plumbing
  * Created by CAB on 14.05.2016.
  */

trait Fitting {

  private[mathact] val pump: Pump

  type Flange[T] = plumbing.Flange[T]
  type Outlet[T] = plumbing.Outlet[T]
  type Inlet[T] = plumbing.Inlet[T]


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
