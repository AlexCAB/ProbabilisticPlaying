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

import scalafx.scene.image.Image

/** Tool built info
  * Created by CAB on 30.08.2016.
  */

case class PublisherInfo(
  toolId: Int,
  outletId: Int)
{
  override def toString = s"PublisherInfo(toolId: $toolId, outletId: $outletId)"}

case class SubscriberInfo(
  toolId: Int,
  inletId: Int)
{
  override def toString = s"PublisherInfo(toolId: $toolId, inletId: $inletId)"}

case class InletConnectionsInfo(
  toolId: Int,
  inletId: Int,
  inletName: Option[String],
  publishers: List[PublisherInfo])
{
  override def toString =
    s"InletConnectionsInfo(toolId: $toolId, inletId: $inletId, inletName: $inletName, publishers: $publishers)"}


case class OutletConnectionsInfo(
  toolId: Int,
  outletId: Int,
  outletName: Option[String],
  subscribers: List[SubscriberInfo])
{
  override def toString =
    s"InletConnectionsInfo(toolId: $toolId, outletId: $outletId, outletName: $outletName, subscribers: $subscribers)"}

case class ToolBuiltInfo(
  toolId: Int,
  toolName: String,
  toolImage: Option[Image],
  inlets: Map[Int, InletConnectionsInfo],
  outlets: Map[Int, OutletConnectionsInfo])
{
  override def toString =
    s"InletConnectionsInfo(toolId: $toolId, toolName: $toolName, toolImage: $toolImage, " +
    s"inlets: $inlets, outlets: $outlets)"}
