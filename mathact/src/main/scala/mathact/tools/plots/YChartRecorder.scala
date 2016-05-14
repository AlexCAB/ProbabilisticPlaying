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

package mathact.tools.plots
import mathact.parts.{Environment, Tool}
import mathact.parts.plumbing._


/** Chart recorder by Y tool
  * Created by CAB on 08.05.2016.
  */

abstract class YChartRecorder(implicit env: Environment) extends Tool(env, "YChartRecorder"){



  protected class Line(name: String) extends Inlet[Double]{   //В этом случае имеется один обработчик, к которому может быть подключено несколько флянцев
    //Если нужно несколько обработчиков для разных типов, можно внутири Line создать несколько Sink

    protected def handler(v: Double): Unit = {println("Handle: " + v)}


    def of(in: ⇒Flange[Double]): Unit = {

      connect(() ⇒ in)





    }

  }

  def line(name: String) = new Line(name)    //DSL для более простого подключниея входя и создания подинстумента





  //??? Далее о том как реализовать внутреннюю мехнику передачи сообщений







}
