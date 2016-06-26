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

package mathact.parts.control.actors

import java.util.concurrent.ExecutionException

import akka.actor._
import mathact.parts.{WorkbenchContext, BaseActor}
import mathact.parts.data.{SketchStatus, Sketch, CtrlEvents}
import mathact.parts.gui.SelectSketchWindow
import mathact.tools.Workbench

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._


/** Application controller actor
  * Created by CAB on 20.06.2016.
  */

class MainController(doStop: Int⇒Unit) extends BaseActor{
  //Parameters
  val sketchStartTimeout = 5.seconds
  //Messages
  case object ShowUI
  case class RunSketch(className: String)
  case class SketchStarted(className: String)
  case class SketchStartTimeout(className: String)
  case object DoStop                     //Normal stop
  case object DoErrorStop                //Stop by error
  case class DoTerminate(exitCode: Int)  //Free resources and terminate
  //Holders
  case class CurrentSketch(sketch: Sketch, isWorking: Boolean, controller: Option[ActorRef]){
    def started(): CurrentSketch = CurrentSketch(sketch, isWorking = true, controller)
    def withController(controller: ActorRef): CurrentSketch = CurrentSketch(sketch, isWorking, Some(controller))}
  //UI definitions
  val uiSelectSketch = new SelectSketchWindow(log){
    def sketchSelected(sketchClassName: String): Unit = {self ! RunSketch(sketchClassName)}
    def windowClosed(): Unit = {self ! DoStop}}
  //Variables
  var sketches = List[Sketch]()
  var currentSketch: Option[CurrentSketch] = None
  //Functions
  def setCurrentSketchState(newStat: SketchStatus): Unit = currentSketch.foreach{ cs ⇒
    sketches = sketches.map{
      case s if s.className == cs.sketch.className ⇒ s.withStatus(newStat)
      case s ⇒ s}}
  def cleanCurrentSketch(): Unit = {
    currentSketch.foreach(_.controller.foreach(_ ! CtrlEvents.StopWorkbenchController))
    currentSketch = None}
  //Messages handling
  reaction(){
    //Handling of starting
    case CtrlEvents.MainControllerStart(sketchList) ⇒
      sketches = sketchList
      //Check if there is autoruned
      sketchList.find(_.status == SketchStatus.Autorun) match{
        case Some(sketch) ⇒
          self ! RunSketch(sketch.className)
        case None ⇒
          self ! ShowUI}
    //Display UI
    case ShowUI ⇒
      tryToRun{uiSelectSketch.show(sketches)} match{
        case Success(_) ⇒
          log.debug("[MainController.MainControllerStart] UI is created.")
        case Failure(_) ⇒
          self ! DoErrorStop}
    //Run selected sketch
    case RunSketch(className) ⇒
      (currentSketch, sketches.find(_.className == className)) match{
        case (None, Some(sketch)) ⇒
          currentSketch = Some(CurrentSketch(sketch, isWorking = false, None))
          //Start creating timeout
          context.system.scheduler.scheduleOnce(sketchStartTimeout, self, SketchStartTimeout(className))
          //Hid UI
          tryToRun{uiSelectSketch.hide()}
          //Create Workbench instance
          Future{sketch.clazz.newInstance()}
            .map{ _ ⇒ self ! SketchStarted(className)}
            .recover{
              case t: ExecutionException ⇒ self ! CtrlEvents.SketchError(className, t.getCause)
              case t: Throwable ⇒ self ! CtrlEvents.SketchError(className, t)}
        case (Some(curSketch), _) if curSketch.sketch.className != className ⇒
          log.warning(s"[MainController.RunSketch] Current sketch $curSketch not ended.")
        case (_, None) ⇒
          log.error(s"[MainController.RunSketch] Not found sketch for className: $className")
        case _ ⇒}
    //Creating of new WorkbenchContext instance, return Either[Exception,WorkbenchContext]
    case CtrlEvents.NewWorkbenchContext(workbench: Workbench) ⇒
      (currentSketch, Option(workbench.getClass.getCanonicalName)) match {
        case (Some(s), Some(cn)) if s.sketch.className == cn ⇒
          //Create WorkbenchContext
          val controller = context.actorOf(
            Props(new WorkbenchController(s.sketch, self)),
            "WorkbenchControllerActor_" + s.sketch.className)
          context.watch(controller)
          currentSketch = currentSketch.map(_.withController(controller))
          //Return
          sender ! Right(new WorkbenchContext(context.system, controller))
        case (_, cn) ⇒ Left(new Exception(
          s"[MainController.NewWorkbenchContext] Workbench class $cn not match a current sketch: $currentSketch"))}
    //Sketch started
    case SketchStarted(className) ⇒
      currentSketch.filter(_.sketch.className == className).foreach{
        case s if s.controller.nonEmpty ⇒
          s.controller.foreach(_ ! CtrlEvents.WorkbenchControllerStart)
          currentSketch = currentSketch.map(_.started())
        case s ⇒
          self ! CtrlEvents.SketchError(className, new Exception(
            s"[MainController.SketchStarted] Workbench controller not created, current sketch: $currentSketch"))}
    //Normal end of sketch
    case CtrlEvents.SketchDone(className) ⇒
      currentSketch.filter(_.sketch.className == className).foreach{ _ ⇒
        log.info(s"[MainController.SketchDone] Current sketch: $currentSketch")
        setCurrentSketchState(SketchStatus.Ended)
        cleanCurrentSketch()
        self ! ShowUI}
    //Failure end of sketch
    case CtrlEvents.SketchError(className, error) ⇒
      currentSketch.filter(_.sketch.className == className).foreach{ _ ⇒
        log.error(
          s"[MainController.SketchError] Error: $error currentSketch: $currentSketch, " +
          s"StackTrace: \n ${error.getStackTrace.mkString("\n")}")
        setCurrentSketchState(SketchStatus.Failed)
        cleanCurrentSketch()
        self ! ShowUI}
    //Sketch start timeout
    case SketchStartTimeout(className) ⇒
      currentSketch.filter(cs ⇒ cs.sketch.className == className && (! cs.isWorking)).foreach{ s ⇒
        log.error(s"[MainController.SketchStartTimeout] Timeout: $sketchStartTimeout, currentSketch: $currentSketch")
        setCurrentSketchState(SketchStatus.Failed)
        cleanCurrentSketch()
        self ! ShowUI}
    //Terminated of current sketch
    case Terminated(actor) ⇒
      currentSketch.filter(_.controller.contains(actor)).foreach{ _ ⇒
        log.error(s"[MainController.Terminated] Actor: $actor, currentSketch: $currentSketch")
        setCurrentSketchState(SketchStatus.Failed)
        currentSketch = None
        self ! ShowUI}
    //Self normal stopping
    case DoStop ⇒
      cleanCurrentSketch()
      self ! DoTerminate(0)
    //Error normal stopping
    case DoErrorStop ⇒
      cleanCurrentSketch()
      self ! DoTerminate(-1)
    case DoTerminate(exitCode) ⇒
      //Hide UI
      tryToRun{uiSelectSketch.hide()}
      //Call stop
      doStop(exitCode)
      self ! PoisonPill}}
