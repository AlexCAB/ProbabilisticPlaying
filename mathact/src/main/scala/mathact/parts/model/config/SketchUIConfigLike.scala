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


package mathact.parts.model.config

import scalafx.scene.image.Image


/** SketchUI config
  * Created by CAB on 21.09.2016.
  */

trait SketchUIConfigLike {
  val buttonsSize: Int
  val runBtnD: Image
  val runBtnE: Image
  val showAllToolsUiD: Image
  val showAllToolsUiE: Image
  val hideAllToolsUiBtnD: Image
  val hideAllToolsUiBtnE: Image
  val skipAllTimeoutTaskD: Image
  val skipAllTimeoutTaskE: Image
  val stopSketchBtnD: Image
  val stopSketchBtnE: Image
  val logBtnD: Image
  val logBtnS: Image
  val logBtnH: Image
  val visualisationBtnD: Image
  val visualisationBtnS: Image
  val visualisationBtnH: Image}
