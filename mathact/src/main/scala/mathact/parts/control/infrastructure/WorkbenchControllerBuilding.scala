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

import java.util.concurrent.{TimeoutException, ExecutionException}

import mathact.parts.WorkbenchLike
import mathact.parts.bricks.WorkbenchContext

import scala.concurrent.Future


/** WorkbenchController sketch building
  * Created by CAB on 04.09.2016.
  */

trait WorkbenchControllerBuilding { _: WorkbenchController ⇒



  private var isWorkbenchContextBuilt = false


  def sketchRunBuilding(): Unit = {
    log.debug(
      s"[WorkbenchControllerBuilding.sketchRunBuilding] Try to create Workbench instance, " +
      s"sketchBuildingTimeout: ${config.sketchBuildingTimeout}")
    //Run building timeout
    context.system.scheduler.scheduleOnce(
      config.sketchBuildingTimeout,
      self,
      SketchBuilt(Left(new TimeoutException(
        s"[WorkbenchControllerBuilding.sketchRunBuilding] Sketch not build in ${config.sketchBuildingTimeout}"))))
    //Build sketch
    Future{sketchData.clazz.newInstance()}
      .map{ s ⇒ self ! SketchBuilt(Right(s.asInstanceOf[WorkbenchLike]))}
      .recover{
        case t: ExecutionException ⇒ self ! SketchBuilt(Left(t.getCause))
        case t: Throwable ⇒ self ! SketchBuilt(Left(t))}}


  def getWorkbenchContext: Either[Exception, WorkbenchContext] = isWorkbenchContextBuilt match{
    case false ⇒
      log.debug(s"[WorkbenchControllerBuilding.getWorkbenchContext] Build WorkbenchContext")
      val response = Right{ new WorkbenchContext(
        context.system,
        mainController,
        pumping,
        config.pumping.pump,
        config.config)}
      isWorkbenchContextBuilt = true
      response
    case true⇒
      val err = new IllegalStateException(s"[WorkbenchControllerBuilding.getWorkbenchContext] Context already created.")
      log.error(err, s"[WorkbenchControllerBuilding.getWorkbenchContext] Error on creating.")
      Left(err)}



  //TODO Убрать sketchBuiltTimeout (вместо неё вызывать sketchBuildingError), перенести про

  def sketchBuilt(result: Either[Throwable, WorkbenchLike]): Unit = {
    //Check if WorkbenchContext built
    (result, isWorkbenchContextBuilt) match{
      case (Right(sketch), true) ⇒

        //TODO Если автозапус то дапуск движка и отправка сообщеия набнавления UI, если не автозапуск то только обновление UI.

      case _ ⇒
        //TODO Логировать ошыбку в лог пользователя
        //TODO Отправка сообшения остановки скетча по ошибке, по этому сообщению контроллер скетча должен очистить
        //TODO реурсы и завершить работу (не закрывая и делая фидимым окно пользовательского лога
        //TODO и оновляя стаус скетча в заголовке окна скетча)

    }









  }

  def sketchBuiltTimeout(): Unit = {



  }

  def sketchBuildingError(error: Throwable): Unit = {
    log.error(
      error,
      s"[WorkbenchControllerBuilding.sketchBuildingError] Error on creating Workbench instance.")

  //TODO Лгирование в лог пользователя, отключение управляющих кнопок
    // Если  java.lang.NoSuchMethodException то возможно клас вложеный


  }


}
