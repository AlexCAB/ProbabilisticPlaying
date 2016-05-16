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
    //Если нужно несколько обработчиков для разных типов, можно внутири Line создать несколько Inlet

    protected def pours(v: Double): Unit = {


      println("Handle: " + v)

    }


    def of(in: ⇒Flange[Double]): Unit = {


      val s = Inlet(this, () ⇒ in)



//      connect(() ⇒ in)      //!!! Этот метод должен возвращать что-то из чего можно будет получить последнее значений


      //!!! Метод должен возврящать как раз Inlet, из которого можно забрать последнее (опциональное) занчение,
      //либо параметризорованый функцией-обработчиком
      //Варианты создания входа: 1) без обработчика (значение забирается в ручьную), 2) С функций обработчиком
      // 3) С обьектом обработчиком (реализающим интерфейс Handler).




    }

  }

  protected class Line2(name: String) extends Inlet[(Double, String)]{ //Пример с обработчиком с двумя значениями разного типа


    protected def pours(v: (Double, String)): Unit = {


      println("Handle: " + v)

    }


    def of(in1: ⇒Flange[Double], in2: ⇒Flange[String]): Unit = {


      val s = Inlet(this, () ⇒ in1, () ⇒ in2)




    }

  }








  def line(name: String) = new Line(name)    //DSL для более простого подключниея входя и создания подинстумента


  //!!!Нужно API для получения последнего значения входа, для агрегирования значений с нескольких входов.





  //??? Далее о том как реализовать внутреннюю мехнику передачи сообщений








}
