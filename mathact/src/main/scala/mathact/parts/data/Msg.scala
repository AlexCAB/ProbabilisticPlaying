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
private[mathact] trait StateMsg extends Msg

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
  case object StartPumping extends StateMsg
//    case class PumpingStarted(stepMode: StepMode) extends Msg
//    case class SwitchStepMode(stepMode: StepMode) extends Msg
//    case class StepModeSwitched(stepMode: StepMode) extends Msg
//    case class SetSpeed(value: Double) extends Msg
//    case object HitStart extends Msg
  case object PumpingStarted extends Msg
  case object StopPumping extends StateMsg
  case object PumpingStopped extends Msg
//  case object HitStep extends Msg
//  case object PumpingStepDone extends Msg
//  case class PumpingError(error: Throwable) extends Msg
  //Object Pump - Pumping (ask)
  case class NewDrive(toolPump: Pump, toolName: String, toolImage: Option[Image]) extends Msg     //Name and image for display in UI
  //Object Pump - Drive (ask)
  case class AddOutlet(pipe: OutPipe[_], name: Option[String]) extends Msg
  case class AddInlet(pipe: InPipe[_], name: Option[String]) extends Msg
  case class ConnectPipes(out: ()⇒Plug[_], in: ()⇒Socket[_]) extends Msg
  case class UserData[T](outletId: Int, value: T) extends Msg
  //Pumping - Drive
  case object BuildDrive extends StateMsg //Creating of connections from pending list
  case class DriveBuilt(builtInfo: ToolBuiltInfo) extends Msg
  case object StartDrive extends StateMsg //Run init user code
  case object DriveStarted extends Msg
  case object StopDrive extends StateMsg  //Run sopping user code
  case object DriveStopped extends Msg
  case object TerminateDrive extends StateMsg //Disconnect all connection and terminate
  case object DriveTerminated extends Msg
  //Drive - Drive
  case class AddConnection(connectionId: Int, initiator: ActorRef, inletId: Int, outlet: OutletData) extends Msg
  case class ConnectTo(connectionId: Int, initiator: ActorRef, outletId: Int, inlet: InletData) extends Msg
  case class PipesConnected(connectionId: Int, outletId: Int, inletId: Int) extends Msg
  case class UserMessage[T](outletId: Int, inletId: Int, value: T) extends Msg
  case class DriveLoad(subscriberId: (ActorRef, Int), outletId: Int, inletQueueSize: Int) extends Msg //subscriberId: (drive, inletId)
  //Drive - Impeller
  case class RunTask[R](kind: TaskKind, id: Int, timeout: FiniteDuration, task: ()⇒R) extends Msg
  case object SkipCurrentTask extends Msg //Makes impeller to skip the current task, but not terminate it (impeller just will not wait for this more)
  case class TaskDone(kind: TaskKind, id: Int, execTime: FiniteDuration, taskRes: Any) extends Msg
  case class TaskTimeout(kind: TaskKind, id: Int, timeFromStart: FiniteDuration) extends Msg
  case class TaskFailed(kind: TaskKind, id: Int, execTime: FiniteDuration, error: Throwable) extends Msg
  //User logging
  case class LogWarning(toolName: String, message: String) extends Msg
  case class LogError(toolName: String, error: Option[Throwable], message: String) extends Msg
  //Common messages
  case class SkipTimeoutTask(toolId: Int) extends Msg
  case class ShowToolUi(toolId: Int) extends Msg

  //TODO Add more

}
