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

package mathact.parts.model.messages

import akka.actor.ActorRef
import mathact.parts.WorkbenchLike
import mathact.parts.model.data.pipes.{InletData, OutletData}
import mathact.parts.model.data.sketch.{SketchUIState, SketchData}
import mathact.parts.model.data.visualisation.ToolBuiltInfo
import mathact.parts.model.enums._
import mathact.parts.plumbing.PumpLike
import mathact.parts.plumbing.fitting.{Plug, Socket, InPipe, OutPipe}
import mathact.tools.Workbench

import scala.concurrent.duration.FiniteDuration


/** Set of actor messages
  * Created by CAB on 23.05.2016.
  */

private [mathact] object M {
  //Application - MainController
  case class MainControllerStart(sketches: List[SketchData]) extends Msg
  case class NewWorkbenchContext(workbench: WorkbenchLike) extends Msg
  //MainController - SketchController
  case object WorkbenchControllerStart extends StateMsg
  case class GetWorkbenchContext(sender: ActorRef) extends Msg
  case object StopWorkbenchController extends StateMsg
  case class SketchDone(className: String) extends Msg
  case class SketchError(className: String, error: Throwable) extends Msg
  //SketchController - SketchUI
  case class SetSketchUIState(state: SketchUIState) extends Msg
  case class SketchUIActionTriggered(action: SketchUIAction, state: SketchUIState) extends Msg
  //SketchController - UserLogging
  case object ShowUserLoggingUI extends Msg
  case object UserLoggingUIShowed extends Msg
  case object HideUserLoggingUI extends Msg
  case object UserLoggingUIHided extends Msg
  //SketchController - Visualization
  case object ShowVisualizationUI extends Msg
  case object VisualizationUIShowed extends Msg
  case object HideVisualizationUI extends Msg
  case object VisualizationUIHided extends Msg
  //SketchController - Pumping
  case object StartPumping extends StateMsg
  case object PumpingStarted extends Msg
  case object StopPumping extends StateMsg
  case object PumpingStopped extends Msg
  case object SkipAllTimeoutTask extends Msg
  case object ShowAllToolUi extends Msg
  case object HideAllToolUi extends Msg
  //Object Pump - Pumping (ask)
  case class NewDrive(toolPump: PumpLike) extends Msg     //Name and image for display in UI
  //Object Pump - Drive (ask)
  case class AddOutlet(pipe: OutPipe[_], name: Option[String]) extends Msg
  case class AddInlet(pipe: InPipe[_], name: Option[String]) extends Msg
  case class ConnectPipes(out: ()⇒Plug[_], in: ()⇒Socket[_]) extends Msg
  case class UserData[T](outletId: Int, value: T) extends Msg
  //Pumping - Drive
  case object BuildDrive extends StateMsg //Creating of connections from pending list
  case object DriveBuilt extends Msg
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
  case class LogWarning(toolId: Int, toolName: String, message: String) extends Msg
  case class LogError(toolId: Int, toolName: String, error: Option[Throwable], message: String) extends Msg
  //Visualization - Drive
  case class ToolBuilt(builtInfo: ToolBuiltInfo) extends Msg   //Send to Visualization from Drive after tool built
  case class SetVisualisationLaval(laval: VisualisationLaval) extends Msg //Send to Drive from Visualization
  case object SkipTimeoutTask extends Msg
  case object ShowToolUi extends Msg  //Send to Drive from Visualization to show it's UI
  case object HideToolUi extends Msg  //Send to Drive from Visualization to hide it's UI

  //TODO Add more

}