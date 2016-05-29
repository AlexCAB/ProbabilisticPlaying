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

package mathact.parts.plumbing

import mathact.parts.control.actors.Controller.StepMode

import scalafx.scene.image.Image


/** Set of akka messages, used for internal interaction
  * Created by CAB on 09.05.2016.
  */

private[mathact] object PumpEvents {
  //Constructing
  case class NewDrive(name: String, image: Option[Image])     //Mane and image for display in UI
  case class NewImpeller(name: String)
  case class PlumbingInit(stepMode: StepMode)
  case object PlumbingStarted
  //Pumps init
  case class Ready(initStepMode: StepMode)                    //Sends to component for they init
  case object Steady                                          //Response on Ready from components on end of init
  //Work
  case object Go                                              //Runt in asynchronous
  case object Halt                                            //Stop in asynchronous
  case object Tick                                            //Process one step (can be send after Go), sends to component
  case object Tack                                            //Process one stem (can be send after Co), confirming of executing of one step
  //Pumps stop
  case object StopDrive


}
