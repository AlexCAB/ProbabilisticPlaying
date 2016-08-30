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

/** Tool built info
  * Created by CAB on 30.08.2016.
  */

case class PublisherInfo(toolId: Int, outLetId: Int)

case class SubscriberInfo(toolId: Int, inletId: Int)

case class InletConnectionsInfo(
  toolId: Int,
  inletId: Int,
  inletName: Option[String],
  publishers: List[PublisherInfo])

case class OutletConnectionsInfo(
  toolId: Int,
  outletId: Int,
  outletName: Option[String],
  subscribers: List[SubscriberInfo])

case class ToolBuiltInfo(
  toolId: Int,
  toolName: Option[String],
  inlets: Map[Int, InletConnectionsInfo],
  outlets: Map[Int, OutletConnectionsInfo])
