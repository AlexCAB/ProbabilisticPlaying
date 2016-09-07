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

import mathact.parts.model.enums.{SketchUIElement, SketchUiElemState}
import mathact.parts.model.messages.M

/** WorkbenchController UI actions processing
  * Created by CAB on 07.09.2016.
  */

trait WorkbenchControllerUIActions { _: WorkbenchController ⇒

  import SketchUiElemState._, SketchUIElement._


  def hitRunBtn() = {
    log.debug(s"[WorkbenchControllerUIActions.hitRunBtn] Try to run plumbing.")
    //Update UI
    sketchUi !  M.UpdateSketchUIState(Map(RunBtn → ElemDisabled))
    //Send StartPumping
    pumping ! M.StartPumping}



}
