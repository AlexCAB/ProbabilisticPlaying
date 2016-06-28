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

import mathact.parts.plumbing.fitting.{Inlet, Outlet, Pipe}
import mathact.tools.Workbench

import scalafx.scene.image.Image


/** Set of actor messages
  * Created by CAB on 23.05.2016.
  */

private[mathact]  object Msg {
  //MainController - SketchController
  case class MainControllerStart(sketches: List[Sketch])
  case class NewWorkbenchContext(workbench: Workbench)
  case object WorkbenchControllerStart
  case object StopWorkbenchController
  case class SketchDone(className: String)
  case class SketchError(className: String, error: Throwable)
  //SketchController - Pumping
  case object GetPumpingActor
  case class StartPumping(speed: Double, stepMode: StepMode)
  case object PumpingStarted
  case object HitStart
  case object HitStop
  case object HitStep
  case class SetSpeed(value: Double)
  case class SwitchMode(newMode: StepMode)
  case class PumpingError(error: Throwable)
  //Pumping - Drive
  case class NewDrive(name: String, image: Option[Image])     //Mane and image for display in UI
  case class NewImpeller(name: String)
  case class AddOutlet(pipe: Outlet[_])
  case class AddInlet(pipe: Inlet[_])
  case object BuildDrive
  case object DriveBuilt
  case object StartDrive
  case object DriveStarted



}
