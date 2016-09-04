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

package mathact.parts.plumbing.fitting

import mathact.parts.model.data.pipes.OutletData
import mathact.parts.plumbing.Pump


/** Wrapper for Outlet
  * Created by CAB on 24.08.2016.
  */

private [mathact] class OutPipe[H](
  out: Outlet[H],
  protected val pipeName: Option[String],
  protected val pump: Pump)
extends Pipe[H] with Plug[H]{
  //Construction
  protected val (toolId, pipeId) = pump.addOutlet(this, pipeName)
  out.injectOutPipe(this)
  //Fields
  lazy val pipeData = OutletData(toolId, pump.drive, pump.toolName, pipeId, pipeName)
  //Methods
  override def toString: String = s"OutPipe(in: $out, pipeName: $pipeName, pump: $pump)"





}
