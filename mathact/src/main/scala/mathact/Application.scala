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
import mathact.parts.data.{Sketch, CtrlEvents}
import mathact.parts.gui.JFXApplication
import mathact.tools.Workbench
import scala.collection.mutable.{ArrayBuffer ⇒ MutList}
import scala.reflect.ClassTag
import scala.reflect._
import scalafx.application.Platform


/** Root application object and class
  * Created by CAB on 17.06.2016.
  */

private [mathact] object Application{
  //Variables
  private var environment: Option[Environment] = None
  //Methods
  /** Starting of application
    * @param sketches - List[(class of sketch, name of sketch)]
    * @param args - App arguments */
  def start(sketches: List[Sketch], args: Array[String]): Unit = {
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
    env.log.debug(s"[Application.start] Environment and JFXApplication created, starting application.")
    env.controller ! CtrlEvents.DoStart(sketches)}
  /** Get of environment
    * @return - Some(Environment) if it exist, None if some error */
  def getEnvironment: Option[Environment] = environment}


class Application {
  //Variables
  private val sketchList = MutList[SketchDsl]() //Canonical class name → SketchDsl
  //Add sketch DSL
  class SketchDsl(clazz: Class[_], sName: Option[String], sDesc: Option[String], isAutorun: Boolean) {
    //Add to list
    sketchList += this
    //Methods
    def name(n: String): SketchDsl = new SketchDsl(clazz, n match{case "" ⇒ None case _ ⇒ Some(n)}, sDesc, isAutorun)
    def description(s: String): SketchDsl = new SketchDsl(clazz, sName, s match{case "" ⇒ None case _ ⇒ Some(s)}, isAutorun)
    def autorun:  SketchDsl = new SketchDsl(clazz, sName, sDesc, true)
    private[mathact] def getData:(Class[_],Option[String],Option[String],Boolean) = (clazz,sName,sDesc,isAutorun)}
  def sketchOf[T <: Workbench : ClassTag]: SketchDsl = new SketchDsl(classTag[T].runtimeClass,None,None,false)
  //Main
  def main(arg: Array[String]):Unit = {
    //Build sketch list
    val sketches = sketchList
      .toList
      .map(_.getData)
      .foldRight(List[Sketch]()){
        case (s,l) if l.exists(_.clazz.getCanonicalName == s._1.getCanonicalName) ⇒ l
        case ((c, n, d, a),l) ⇒ Sketch(c, n, d, a) +: l}
    //Construct Application
    Application.start(sketches, arg)}}
