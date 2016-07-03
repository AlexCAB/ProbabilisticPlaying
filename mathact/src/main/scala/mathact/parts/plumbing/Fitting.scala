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
  type Socket[T] = fitting.Jack[T]
  type Outlet[T] = fitting.Outlet[T]
  type Inlet[T] = fitting.Inlet[T]




  //!!! Не забывать о том что инструменты могут динамически создаватся и разрушатся
  //Нужно добавить метод разрушения в Pump и сюда (метод регистрации не нужен, так как Pump регистрируется при создании).


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

  //Registration if Outlet
  protected object Outlet{
    def apply[T,H](out: T with Outlet[H]): T with Plug[H] = { //If pump set, inject it to Outlet and register Outlet
      Option(pump).foreach(p ⇒ out.injectPump(p, p.addOutlet(out)))
      out}}
  //Registration if Inlet
  protected object Inlet{
    def apply[T,H](in: T with Inlet[H]): T with Socket[H] = {
      Option(pump).foreach(p ⇒ in.injectPump(p, p.addInlet(in)))
      in}}








}
