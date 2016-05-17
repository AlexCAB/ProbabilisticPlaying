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

package mathact.tools.pots
import mathact.parts.{OnStart, OnStop, Environment, Tool}


/** Pot board tool
  * Created by CAB on 08.05.2016.
  */

abstract class PotBoard(implicit env: Environment) extends Tool(env, "PotBoard") with OnStart with OnStop{



  protected def onStart(): Unit = println("PotBoard.onStart")
  protected def onStop(): Unit = println("PotBoard.onStop")


  protected class Pot(from: Double, to: Double, in: Option[()⇒Plug[Double]]) extends Outlet[Double] with Inlet[Double]{    //Определение выхода

    protected def pours(value: Double): Unit = push(value)    //Все полученые из in значения будут нередаватся на выход



//


    in.foreach(e ⇒ Inlet(this, e))       //Эамыкание входа на выход

    in.foreach(e ⇒ new Inlet[Double]{    //Констуирование отдельного вхда

      protected def pours(value: Double): Unit = { ??? }



    })  //Эамыкание входа на выход

    def test():Unit = ???

    //Здесь реализация

    push(10)       //Посылка значения на выход

  }



  def pot(from: Double, to: Double) = Outlet(new Pot(from, from, None))   //DSL для удобного создания выхода


  def pot(in: ⇒Plug[Double]) = Outlet(new Pot(1,2, Some(()⇒in)))   //DSL для удобного создания выхода




}
