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

import mathact.parts.{OnStart, OnStop, Fitting, Environment}

/** Process of tools communications
  * Created by CAB on 09.05.2016.
  */

class Pump(env: Environment, tool: Fitting) { import env._   //Импорт сисемы акторов и прочего



  tool match{             //Должно выполнятся при инициализации инструмента
    case os: OnStart ⇒  os.doStart()
    case _ ⇒ println("NOT OnStart")
  }

  tool match{             //Должно выполнятся призавершении работы инструмента
    case os: OnStop ⇒  os.doStop()
    case _ ⇒ println("NOT OnStop")
  }




//  val impeller: ActorRef = ???

    //Нужно добавиь ещё один актор который будет следить за нагрузкой impeller, и регулировать
    //влечену очереди (подход с обратным давлением)
    //Нужно предусмотреть след режыми работы: синхронный жосткий, синхронный мягкий (без подтверждений выполения итерации)
    //и асинхронный.


}
