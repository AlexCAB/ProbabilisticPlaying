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

package mathact.parts.plumbing.actors

import mathact.parts.UIControl
import mathact.parts.data.{Msg, TaskKind}

/** Handling of UI control
  * Created by CAB on 31.08.2016.
  */

private [mathact] trait DriveUIControl { _: Drive ⇒


  def showToolUi(): Unit = pump.tool match{
    case task: UIControl ⇒


      log.debug("[DriveUIControl.doStopping] Try to run stopping user function.")
      impeller ! Msg.RunTask[Unit](TaskKind., -3, pump.stopFunctionTimeout, ()⇒{ task.doStop() })


    case _ ⇒
      log.debug("[DriveUIControl.showToolUi] On show UI user function not defined, nothing to do.")}



    //TODO 1) Переписать импеллет чтоб поддержывал добаление нескольких задачь одновременно (но выполнял только одну),
    //TODO    для валдения служебных задачь
    //TODO 2) Пользовательские сообщения по прежнему выполняются по одному, и обработка также начинается,
    //TODO    по завершении пользовательской функции инициализации.
    //TODO 3) Дописать управление IU (управелеьние по ShowToolUi, HideToolUi и по ShowAllToolUi, HideAllToolUi)
    //TODO
    //TODO
    //TODO
    //TODO
    //TODO
    //TODO
    //TODO
    //TODO
    //TODO










  def hideToolUi(): Unit = {

    ???

  }




}
