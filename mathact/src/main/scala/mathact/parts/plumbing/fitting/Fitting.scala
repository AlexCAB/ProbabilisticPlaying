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


  protected object Collect{
    def apply[T](in: Flange[T]*): Flange[T] = {


      in.head
    }

  }

  protected object Convert{
    def apply[T,H](in: Flange[T])(transformer: T⇒H): Flange[H] = {     //Приобразователь типа событий

        //!!!Нужна версия для нескольких входов, чтобы можно было агрегировать с их значения
        //???Обдумать как синхронизоровать нескольхо входов (нужна проговая синхронизаия, и ваоление по каждому событию)
        //Варианты синхронизации: 1) потоковая, 2) значения по умолчнию, 3) опциональные значения.

        //Convert это статическае связание входов и выходов, нужно ещё динамическоке (чтобы можно было добвлять и удалять входы)


      ???

    }

  }



  protected val Outlet = fitting.Outlet
  protected val Inlet = fitting.Inlet




}
