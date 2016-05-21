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

package mathact.parts.plumbing


/** Contains definition for plumbing
  * Created by CAB on 14.05.2016.
  */

trait Fitting {

  private[mathact] val pump: Pump

  type Plug[T] = fitting.Plug[T]
  type Socket[T] = fitting.Socket[T]
  type Outlet[T] = fitting.Outlet[T]
  type Inlet[T] = fitting.Inlet[T]


//  protected object Collect{
//    def apply[T](in: Plug[T]*): Plug[T] = {
//
//
//      in.head
//    }
//
//  }
//
//  protected object Convert{
//    def apply[T,H](in: Plug[T])(transformer: T⇒H): Plug[H] = {     //Приобразователь типа событий
//
//        //!!!Нужна версия для нескольких входов, чтобы можно было агрегировать с их значения
//        //???Обдумать как синхронизоровать нескольхо входов (нужна проговая синхронизаия, и ваоление по каждому событию)
//        //Варианты синхронизации: 1) потоковая, 2) значения по умолчнию, 3) опциональные значения.
//
//        //Convert это статическае связание входов и выходов, нужно ещё динамическоке (чтобы можно было добвлять и удалять входы)
//
//
//      ???
//
//    }
//
//  }


  protected object Outlet{
    def apply[T,H](v: T with Outlet[H]): T with Plug[H] = {

      //Здесь  в Outlet должна инжектица Pump (можно росто добавить метод), который можно будет вызвать только раз
      //Чтобы не переопределяли помпу
      //И соответственно добавить медод получения помпы
      v
    }
  }

  protected object Inlet{


    def apply[T,H](v: T with Inlet[H]): T with Socket[H] = {


      //Здесь регистрируется новый Inlet


      v
    }
  }








}
