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
  case class StartPumping(workMode: WorkMode, speed: Double)
  case class PumpingStarted(workMode: WorkMode)
  case object HitStart
  case object HitStop
  case object HitStep
  case class SetSpeed(value: Double)
  case class SwitchWorkMode(newMode: WorkMode)
  case class StepModeSwitched(workMode: WorkMode, stepMode: StepMode)
  case class PumpingError(error: Throwable)
  //Pumping - Drive
  case class NewDrive(toolPump: Pump, toolName: String, toolImage: Option[Image])     //Mane and image for display in UI
  case class NewImpeller(name: String)
  case class AddOutlet(pipe: Outlet[_])
  case class AddInlet(pipe: Inlet[_])
  trait Connectivity
  case class ConnectPipes(out: ()⇒Plug[_], in: ()⇒Jack[_]) extends Connectivity
  case class AddConnection(inletId: Int, outlet: PipeData)
  case class ConnectTo(outletId: Int, inlet: PipeData)
  case class DisconnectPipes(out: ()⇒Plug[_], in: ()⇒Jack[_]) extends Connectivity
  case class DisconnectFrom(outletId: Int, inlet: PipeData)
  case class DelConnection(inletId: Int, outlet: PipeData)
  case object BuildDrive
  case object DriveBuilt
  case object StartDrive
  case object DriveStarted
  case class SetStepMode(mode: StepMode)
  case class StepModeIsSet(mode: StepMode)
  case object DriveStep
  case object DriveDone







  //Drive-Impeller
  case class RunTask(id: Long, name: String, task: ()⇒Unit)
  case class TaskDone(id: Long, name: String)
  case class TaskFailed(id: Long, name: String, error: Throwable)
  //User data
  case class UserData[T](outletId: Int, value: T)
  case class UserMessage[T](outletId: Int, inletId: Int, value: T)
  case class DriveLoad(drive: ActorRef, maxQueueSize: Int)


}
