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

package mathact.parts.model.data.sketch


/** Sketch UI state
  * Created by CAB on 03.09.2016.
  */

case class SketchUIState(
  isUiShown: Boolean,
  runBtnEnable: Boolean,
  showToolUiBtnEnable: Boolean,
  hideToolUiBtnEnable: Boolean,
  skipAllTimeoutProcBtnEnable: Boolean,
  stopBtnEnable: Boolean,
  logUiBtnEnable: Boolean,
  logUiBtnIsShow: Boolean, //false - hide button
  visualisationUiBtnEnable: Boolean,
  visualisationUiBtnIsShow: Boolean) //false - hide button



