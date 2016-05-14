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

/** Contains event handler.
  * Created by CAB on 13.05.2016.
  */

trait Inlet[T] extends Pipe[T]{

  protected def handler(value: T): Unit

  def connect(in:()⇒Flange[T]): Unit = {    //Значение должно считыватся только после полного конструирования (чтобы небыло NPE)


//    println(in())

//    in match{
//      case o: Outlet[T] ⇒ //Подключнение
//      case _ ⇒   //Ошибка


}





//}
  def disconnect(in:()⇒Flange[Double]): Unit = {

  ???

}


}