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

package mathact.parts.data

import akka.actor.ActorRef
import mathact.parts.plumbing.Pump
import mathact.parts.plumbing.fitting._
import mathact.tools.Workbench

import scala.concurrent.duration.FiniteDuration
import scalafx.scene.image.Image


/** Set of actor messages
  * Created by CAB on 23.05.2016.
  */

private[mathact] trait Msg

private[mathact] object Msg {
  //Application - MainController
  case class MainControllerStart(sketches: List[Sketch]) extends Msg
  case class NewWorkbenchContext(workbench: Workbench) extends Msg
  //MainController - SketchController
  case class WorkbenchControllerInit(workbenchSender: ActorRef) extends Msg
  case object WorkbenchControllerStart extends Msg
  case object StopWorkbenchController extends Msg
  case class SketchDone(className: String) extends Msg
  case class SketchError(className: String, error: Throwable) extends Msg
  //SketchController - Pumping
  case class StartPumping(stepMode: StepMode, speed: Double) extends Msg
  case class PumpingStarted(stepMode: StepMode) extends Msg
  case class SwitchStepMode(stepMode: StepMode) extends Msg
  case class StepModeSwitched(stepMode: StepMode) extends Msg
  case class SetSpeed(value: Double) extends Msg
  case object HitStart extends Msg
  case object PumpingStarted extends Msg
  case object HitStop extends Msg
  case class PumpingStopped(stepMode: StepMode) extends Msg
  case object HitStep extends Msg
  case object PumpingStepDone extends Msg
  case class PumpingError(error: Throwable) extends Msg
  //Object Pump - Pumping (ask)
  case class NewDrive(toolPump: Pump, toolName: String, toolImage: Option[Image]) extends Msg     //Name and image for display in UI
  //Object Pump - Drive (ask)
  case class AddOutlet(pipe: Outlet[_], name: Option[String]) extends Msg
  case class AddInlet(pipe: Inlet[_], name: Option[String]) extends Msg
  case class ConnectPipes(out: ()⇒Plug[_], in: ()⇒Socket[_]) extends Msg
  case class DisconnectPipes(out: ()⇒Plug[_], in: ()⇒Socket[_]) extends Msg
  case class UserData[T](outletId: Int, value: T) extends Msg
  //Pumping - Drive
  case object BuildDrive extends Msg //Creating of connections from pending list
  case object DriveBuilt extends Msg
  case object StartDrive extends Msg //Run init user code
  case object DriveStarted extends Msg
  case object StopDrive extends Msg  //Run sopping user code
  case object DriveStopped extends Msg
  case object TerminateDrive extends Msg //Disconnect all connection and terminate
  case object DriveTerminated extends Msg
  //Drive - Drive
  case class AddConnection(initiator: ActorRef, inletId: Int, outlet: PipeData) extends Msg
  case class ConnectTo(initiator: ActorRef, outletId: Int, inlet: PipeData) extends Msg
  case class ConnectionAdded(inletId: Int, outletId: Int) extends Msg
  case class PipesConnected(inletId: Int, outletId: Int) extends Msg
  case class DelConnection(initiator: ActorRef, outletId: Int, inlet: PipeData) extends Msg
  case class DisconnectFrom(initiator: ActorRef, inletId: Int, outlet: PipeData) extends Msg
  case class ConnectionDeleted(inletId: Int, outletId: Int) extends Msg
  case class PipesDisconnected(inletId: Int, outletId: Int) extends Msg
  case class UserMessage[T](outletId: Int, inletId: Int, value: T) extends Msg
  case class DriveLoad(drive: ActorRef, maxQueueSize: Int) extends Msg
  //Drive - Impeller
  case class RunTask[R](name: String, timeout: FiniteDuration, task: ()⇒R) extends Msg
  case object SkipCurrentTask  extends Msg //Makes impeller to skip the current task, but not terminate it (impeller just will not wait for this more)
  case class TaskDone(name: String, execTime: FiniteDuration, taskRes: Any) extends Msg
  case class TaskTimeout(name: String, timeFromStart: FiniteDuration) extends Msg
  case class TaskFailed(name: String, execTime: FiniteDuration, error: Throwable) extends Msg
  //User data

  //TODO Add more

}
