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
import mathact.parts.model.enums.{ActorState, SketchUIElement, SketchUiElemState}
import mathact.parts.model.messages.M

import scala.concurrent.Future


/** WorkbenchController sketch building
  * Created by CAB on 04.09.2016.
  */

trait WorkbenchControllerBuilding { _: WorkbenchController ⇒

  import SketchUiElemState._, SketchUIElement._


  private var isWorkbenchContextBuilt = false


  def sketchRunBuilding(): Unit = {
    log.debug(
      s"[WorkbenchControllerBuilding.sketchRunBuilding] Try to create Workbench instance, " +
      s"sketchBuildingTimeout: ${config.sketchBuildingTimeout}")
    //Run building timeout
    context.system.scheduler.scheduleOnce(
      config.sketchBuildingTimeout,
      self,
      SketchBuiltTimeout)
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





  def sketchBuilt(result: Either[Throwable, WorkbenchLike]): Unit = {
    //Check if WorkbenchContext built
    (result, isWorkbenchContextBuilt) match{
      case (Right(workbench), true) ⇒
        log.debug(s"[WorkbenchControllerBuilding.sketchBuilt] workbench: $workbench")
        //User log
        val autorunMsg = sketchData.autorun match{
          case false ⇒ ". Auto-run is off, hit 'play' button to start sketch."
          case true ⇒ "."}
        userLogging ! M.LogInfo(None, "Workbench", s"Sketch '$sketchName' successfully built$autorunMsg")
        //Run plumping if auto run
        sketchData.autorun match{
          case true ⇒
            log.debug(s"[WorkbenchControllerBuilding.sketchBuilt] Autorun in on, try to start plumbing.")
            pumping ! M.StartPumping
          case false ⇒
            log.debug(s"[WorkbenchControllerBuilding.sketchBuilt] No autorun.")}
        //Update UI
        sketchUi !  M.UpdateSketchUIState(Map(RunBtn → (if(sketchData.autorun) ElemDisabled else ElemEnabled)))
        //Send started to main controller
        mainController ! M.SketchBuilt(sketchData.className, workbench)
      case (res, isContextBuilt) ⇒
        log.error(
          s"[WorkbenchControllerBuilding.sketchBuilt] Building failed, result: $result, " +
          s"isWorkbenchContextBuilt: $isContextBuilt")
        //Update UI
//        sketchUi !  M.UpdateSketchUIState(Map(
//          RunBtn → ElemDisabled,
//          ShowAllToolsUiBtn → ElemDisabled,
//          HideAllToolsUiBtn → ElemDisabled,
//          SkipAllTimeoutTaskBtn → ElemDisabled,
//          StopSketchBtn → ElemDisabled,
//          LogBtn → ElemDisabled,
//          VisualisationBtn → ElemDisabled))




        //TODO Логировать ошыбку в лог пользователя
        //TODO Отправка сообшения остановки скетча по ошибке, по этому сообщению контроллер скетча должен очистить
        //TODO реурсы и завершить работу (не закрывая и делая фидимым окно пользовательского лога
        //TODO и оновляя стаус скетча в заголовке окна скетча)

    }
  }


    def sketchBuiltTimeout(state: ActorState): Unit = {

      //TODO Если состояние не  Building то ошыбка (вынести в подпрограмму), иначе ничего не делать


    }











  def pumpingStarted(): Unit = {
    log.debug(s"[WorkbenchControllerBuilding.pumpingStarted] Started.")
    //Update UI
    sketchUi ! M.UpdateSketchUIState(Map(
      RunBtn → ElemDisabled,
      StopSketchBtn → ElemEnabled,
      ShowAllToolsUiBtn → ElemEnabled,
      HideAllToolsUiBtn → ElemEnabled,
      SkipAllTimeoutTaskBtn → ElemEnabled))}



//  SketchBuilt(Left(new TimeoutException(
//    s"[WorkbenchControllerBuilding.sketchRunBuilding] Sketch not build in ${config.sketchBuildingTimeout}")))


//  def sketchBuiltTimeout(): Unit = {
//
//
//
//  }

//  def sketchBuildingError(error: Throwable): Unit = {
//    log.error(
//      error,
//      s"[WorkbenchControllerBuilding.sketchBuildingError] Error on creating Workbench instance.")

  //TODO Лгирование в лог пользователя, отключение управляющих кнопок
    // Если  java.lang.NoSuchMethodException то возможно клас вложеный


//  }


}
