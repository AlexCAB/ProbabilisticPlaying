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

package mathact

import mathact.parts.Environment
import mathact.parts.control.CtrlEvents
import mathact.parts.gui.JFXApplication
import mathact.tools.Workbench
import scala.collection.mutable.{ArrayBuffer ⇒ MutList}
import scala.reflect.ClassTag
import scala.reflect._
import scalafx.application.Platform


/** Root application class and object
  * Created by CAB on 17.06.2016.
  */

private [mathact] object Application{
  //Variables
  private var environment: Option[Environment] = None
  //Methods
  /** Starting of application
    * @param sketches - List[(class of sketch, name of sketch)]
    * @param args - App arguments */
  def start(sketches: List[(Class[_], String)], args: Array[String]): Unit = {
    //Create Environment
    val env = new Environment
    environment = Some(env)
    //Java FX Application
    try{
      JFXApplication.init(args, env.log)
      Platform.implicitExit = false}
    catch{ case e: Throwable ⇒
      env.log.error(s"[Application.start] Error on start UI: $e, terminate ActorSystem.")
      env.doStop(-1)
      throw e}
    //Start
    env.log.error(s"[Application.start] Environment and JFXApplication created, starting application.")
    env.controller ! CtrlEvents.DoStart(sketches)}
  /** Register of workbench on that creating
    * @param workbench - created Workbench object
    * @return - Some(Environment) if register successfully, None if some error */
  def registerWorkbench(workbench: Workbench): Option[Environment] = {

    //Этот метод должен быть вызван в самом начале конструирования Workbench обьекта. Обьект будет конструироватся
    //каждый раз при запуске сктча.

    //Запрос передаётся актору контроллера, и тот проверяет, есть ли данный Workbench в списке и он ли был
    //запущен, и ечли да возвращает "удачно"

    //Если Workbench получил None он должен ничего не делать, или бросить исключение, которе будет отловлено
    //в дкргом Workbench если он там был создан или не будет отловлено и свя программа упадйт

    ???
  }








}

class Application {
  //Variables
  private val sketchList = MutList[(Class[_], String)]()
  //Add sketch DSL
  def sketchOf[T <: Workbench : ClassTag]: Unit = { sketchList += (classTag[T].runtimeClass → "") }
  object sketch{
    def of[T <: Workbench : ClassTag](name: String = ""): Unit = {sketchList += (classTag[T].runtimeClass → name)}}
  //Main
  def main(arg: Array[String]):Unit = {
    //Construct Application
    Application.start(sketchList.toList, arg)}}
