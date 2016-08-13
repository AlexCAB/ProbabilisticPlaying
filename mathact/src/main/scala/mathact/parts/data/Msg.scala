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

private[mathact] object Msg {
  //MainController - SketchController
  case class MainControllerStart(sketches: List[Sketch])
  case class NewWorkbenchContext(workbench: Workbench)
  case object WorkbenchControllerStart
  case object StopWorkbenchController
  case class SketchDone(className: String)
  case class SketchError(className: String, error: Throwable)
  //SketchController - Pumping
  case object GetPumpingActor
  case class StartPumping(stepMode: StepMode, speed: Double)
  case class PumpingStarted(stepMode: StepMode)
  case class SwitchStepMode(stepMode: StepMode)
  case class StepModeSwitched(stepMode: StepMode)
  case class SetSpeed(value: Double)
  case object HitStart
  case object PumpingStarted
  case object HitStop
  case class PumpingStopped(stepMode: StepMode)
  case object HitStep
  case object PumpingStepDone
  case class PumpingError(error: Throwable)
  //Pumping - Drive
  case class NewDrive(toolPump: Pump, toolName: String, toolImage: Option[Image])     //Mane and image for display in UI
  case class AddOutlet(pipe: Outlet[_])
  case class AddInlet(pipe: Inlet[_])
  trait Connectivity
  case class ConnectPipes(out: ()⇒Plug[_], in: ()⇒Jack[_]) extends Connectivity
  case class AddConnection(inletId: Int, outlet: PipeData)
  case class ConnectTo(outletId: Int, inlet: PipeData)
  case class DisconnectPipes(out: ()⇒Plug[_], in: ()⇒Jack[_]) extends Connectivity
  case class DisconnectFrom(outletId: Int, inlet: PipeData)
  case class DelConnection(inletId: Int, outlet: PipeData)
  case object BuildDrive //Creating of connections from pending list
  case object DriveBuilt
  case object StartDrive //Run init user code
  case object DriveStarted
  case object StopDrive  //Run sopping user code
  case object DriveStopped
  case object TerminateDrive //Disconnect all connection and terminate
  case object DriveTerminated
  //Drive-Impeller
  case class RunTask[R](name: String, timeout: FiniteDuration, task: ()⇒R)
  case object SkipCurrentTask //Makes impeller to skip the current task, but not terminate it (impeller just will not wait for this more)
  case class TaskDone(name: String, execTime: FiniteDuration, taskRes: Any)
  case class TaskTimeout(name: String, timeFromStart: FiniteDuration)
  case class TaskFailed(name: String, execTime: FiniteDuration, error: Throwable)
  //User data
  case class UserData[T](outletId: Int, value: T)
  case class UserMessage[T](outletId: Int, inletId: Int, value: T)
  case class DriveLoad(drive: ActorRef, maxQueueSize: Int)

  //TODO Add more

}
