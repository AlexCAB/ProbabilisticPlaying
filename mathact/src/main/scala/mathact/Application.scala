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

import akka.actor.{PoisonPill, Props, ActorRef, ActorSystem}
import akka.event.Logging
import akka.util.Timeout
import mathact.parts.WorkbenchContext
import mathact.parts.control.actors.MainController
import mathact.parts.data.{Sketch, CtrlEvents}
import mathact.parts.gui.JFXApplication
import mathact.tools.Workbench
import scala.collection.mutable.{ArrayBuffer ⇒ MutList}
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag
import scala.reflect._
import scalafx.application.Platform
import akka.pattern.ask
import scala.concurrent.duration._


/** Root application object and class
  * Created by CAB on 17.06.2016.
  */

private [mathact] object Application{
  //Parameters
  private val beforeTerminateTimeout = 1.seconds
  private val creatingWorkbenchContextTimeout = 5.seconds
  //Enums
  object State extends Enumeration { val Starting, Work, Stopping = Value }
  //Variables
  private var state: State.Value = State.Starting
  //Actor system
  private val system = ActorSystem("MathActActorSystem")
  private implicit val execContext = system.dispatcher
  private val log = Logging.getLogger(system, this)
  log.info(s"[Application] Starting of program...")
  //Main controller
  private val mainController: ActorRef = system.actorOf(Props(new MainController(doStop)), "MainControllerActor")
  //Stop proc
  private def doStop(exitCode: Int): Unit = Future{
    state = State.Stopping
    log.debug(s"[Application.doStop] Stopping of program, terminate timeout: $beforeTerminateTimeout milliseconds.")
    Thread.sleep(beforeTerminateTimeout.toMillis)
    Platform.exit()
    system.terminate().onComplete{_ ⇒ System.exit(exitCode)}}
  private def doTerminate(): Unit = {
    log.error(s"[Application.doStop] Application, terminated.")
    mainController ! PoisonPill
    doStop(-1)}
  //Methods
  /** Starting of application
    * @param sketches - List[(class of sketch, name of sketch)]
    * @param args - App arguments */
  def start(sketches: List[Sketch], args: Array[String]): Unit =
    try{
      //Check state
      state match{
        case State.Starting ⇒
          //Run Java FX Application
          JFXApplication.init(args, log)
          Platform.implicitExit = false
          log.debug(s"[Application.start] JFXApplication created, starting application.")
          mainController ! CtrlEvents.DoStart(sketches)
        case st ⇒
          throw new IllegalStateException(
            s"[Application.start] This method can be called only if App in Starting state, current state: $st")}}
    catch { case e: Throwable ⇒
      log.error(s"[Application.start] Error on start: $e, terminate ActorSystem.")
      doTerminate()
      throw e}
  /** Get of WorkbenchContext for new Workbench
    * @param workbench - Workbench
    * @return - MainController ActorRef or thrown exception */
  def getWorkbenchContext(workbench: Workbench): WorkbenchContext = state match{
    case State.Work ⇒
      log.debug(s"[Application.getWorkbenchContext] Try to create WorkbenchContext for ${workbench.getClass.getName}.")
      //Ask for new context
      val askTimeout = Timeout(creatingWorkbenchContextTimeout).duration
      Await
        .result(
          ask(mainController, CtrlEvents.NewWorkbenchContext(workbench))(askTimeout).mapTo[Either[Exception,WorkbenchContext]],
          askTimeout)
        .fold(
          e ⇒ {
            log.debug(s"[Application.getWorkbenchContext] Error on ask for ${workbench.getClass.getName}, err: $e.")
            throw e},
          wc ⇒ {
            log.debug(s"[Application.getWorkbenchContext] WorkbenchContext created for ${workbench.getClass.getName}.")
            wc})
    case st ⇒
      throw new IllegalStateException(
        s"[Application.getWorkbenchContext] This method can be called only if App in Work state, current state: $st")}}


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
