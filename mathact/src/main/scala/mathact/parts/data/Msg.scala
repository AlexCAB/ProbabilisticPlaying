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
  //Application - MainController
  case class MainControllerStart(sketches: List[Sketch])
  case class NewWorkbenchContext(workbench: Workbench)
  //MainController - SketchController
  case class WorkbenchControllerInit(workbenchSender: ActorRef)
  case object WorkbenchControllerStart
  case object StopWorkbenchController
  case class SketchDone(className: String)
  case class SketchError(className: String, error: Throwable)
  //SketchController - Pumping
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
  //Object Pump - Pumping (ask)
  case class NewDrive(toolPump: Pump, toolName: String, toolImage: Option[Image])     //Name and image for display in UI
  //Object Pump - Drive (ask)
  case class AddOutlet(pipe: Outlet[_], name: Option[String])
  case class AddInlet(pipe: Inlet[_], name: Option[String])
  case class ConnectPipes(out: ()⇒Plug[_], in: ()⇒Jack[_])
  case class DisconnectPipes(out: ()⇒Plug[_], in: ()⇒Jack[_])
  case class UserData[T](outletId: Int, value: T)
  //Pumping - Drive
  case object BuildDrive //Creating of connections from pending list
  case object DriveBuilt
  case object StartDrive //Run init user code
  case object DriveStarted
  case object StopDrive  //Run sopping user code
  case object DriveStopped
  case object TerminateDrive //Disconnect all connection and terminate
  case object DriveTerminated
  //Drive - Drive
  case class AddConnection(inletId: Int, outlet: PipeData)
  case class ConnectTo(outletId: Int, inlet: PipeData)
  case class DisconnectFrom(outletId: Int, inlet: PipeData)
  case class DelConnection(inletId: Int, outlet: PipeData)
  case class UserMessage[T](outletId: Int, inletId: Int, value: T)
  case class DriveLoad(drive: ActorRef, maxQueueSize: Int)
  //Drive - Impeller
  case class RunTask[R](name: String, timeout: FiniteDuration, task: ()⇒R)
  case object SkipCurrentTask //Makes impeller to skip the current task, but not terminate it (impeller just will not wait for this more)
  case class TaskDone(name: String, execTime: FiniteDuration, taskRes: Any)
  case class TaskTimeout(name: String, timeFromStart: FiniteDuration)
  case class TaskFailed(name: String, execTime: FiniteDuration, error: Throwable)
  //User data

  //TODO Add more

}
