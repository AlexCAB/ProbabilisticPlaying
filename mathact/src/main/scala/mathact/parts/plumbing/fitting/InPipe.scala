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

import mathact.parts.data.InletData
import mathact.parts.plumbing.Pump


/** Wrapper fot Inlet
  * Created by CAB on 24.08.2016.
  */

private [mathact] class InPipe[H] (
  in: Inlet[H],
  protected val pipeName: Option[String],
  protected val pump: Pump)
extends Pipe[H] with Socket[H]{
  //Construction
  protected val pipeId: Int = pump.addInlet(this, pipeName)
  //Fields
  lazy val pipeData = InletData(pump.drive, pump.toolName, pipeId, pipeName)
  //Methods
  override def toString: String = s"InPipe(in: $in, pipeName: $pipeName, pump: $pump)"








}
