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

package mathact.parts.control.infrastructure

import java.util.concurrent.ExecutionException

import scala.concurrent.Future


/** WorkbenchController sketch building
  * Created by CAB on 04.09.2016.
  */

trait WorkbenchControllerBuilding { _: WorkbenchController ⇒




  def sketchRunBuilding(): Unit = {
    log.debug(
      s"[WorkbenchControllerBuilding.sketchRunBuilding] Try to create Workbench instance, " +
      s"sketchBuildingTimeout: ${config.sketchBuildingTimeout}")
    //Run building timeout
    context.system.scheduler.scheduleOnce(config.sketchBuildingTimeout, self, SketchBuiltTimeout)
    //Build sketch
    Future{sketch.clazz.newInstance()}
      .map{ _ ⇒ self ! SketchBuilt}
      .recover{
        case t: ExecutionException ⇒ self ! SketchError(t.getCause)
        case t: Throwable ⇒ self ! SketchError(t)}}



  def sketchBuilt(): Unit = {

    //TODO Проверка был ли получен контекст

  }

  def sketchBuiltTimeout(): Unit = {

    //TODO Лгирование в лог пользователя, отключение управляющих кнопок

  }

  def sketchBuildingError(error: Throwable): Unit = {
    log.error(
      error,
      s"[WorkbenchControllerBuilding.sketchBuildingError] Error on creating Workbench instance.")

  //TODO Лгирование в лог пользователя, отключение управляющих кнопок


  }


}
