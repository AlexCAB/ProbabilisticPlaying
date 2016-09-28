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

package mathact.parts.control.infrastructure

import akka.actor.ActorRef
import mathact.parts.ActorBase
import mathact.parts.gui.JFXInteraction
import mathact.parts.model.config.UserLoggingConfigLike


/** Control of tool UI layout
  * Created by CAB on 28.09.2016.
  */

class LayoutController(
  config: UserLoggingConfigLike,
  workbenchController: ActorRef)
extends ActorBase with JFXInteraction {






  //Messages handling with logging
  def reaction: PartialFunction[Any, Unit]  = {


    case m ⇒ println("[LayoutController] message: " + m)

  }


}
