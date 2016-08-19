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

/** Base trite for Plug and Socket.
  * Created by CAB on 13.05.2016.
  */

trait Connector[T]{

  //!!! Аргубенты должны быть как socket: ⇒Socket[T], для поддержки перехрёстного связания инструментов
//
//  def connect(connector: ⇒Connector[T]): Unit = {
//
////    (this, connector) match{
////      case (s: Socket[T], p: Plug[T]) ⇒
////
////
////
////      case (p: Plug[T], s: Socket[T]) ⇒
////
////
////      case (c1,c2) ⇒ log.error(
////        s"[Connector.connect] Only Socket-Plug and Socket-Plug connecting acceptable, " +
////        s"currently connector 1: $c1, and connector 2: $c2")}
//
//
//  }
//
//
//
//
//
//
//
//
//  def disconnect(connector: ⇒Connector[T]): Unit = {
//
//
//
//
//
//
//
//  }




}