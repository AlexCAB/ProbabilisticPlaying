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

package mathact.parts.plumbing.actors

import akka.actor.ActorRef
import mathact.parts.IdGenerator
import mathact.parts.data.{ActorState, InletData, OutletData, Msg}
import mathact.parts.plumbing.fitting._
import scala.collection.mutable.{Map ⇒ MutMap, Queue ⇒ MutQueue}


/** Handling of connections and disconnections
  * Created by CAB on 22.08.2016.
  */

private [mathact] trait DriveConnectivity { _: Drive ⇒


  //Variables
  private val pendingConnections = MutMap[Int, Msg.ConnectPipes]()

  //Methods
  /** Adding of ConnectPipes to pending connections
    * @param message = ConnectPipes */
  def connectPipesAsk(message: Msg.ConnectPipes, state: ActorState): Either[Throwable,Int] = state match{
    case ActorState.Building ⇒
      //On create store to pending connections
      val connectionId = nextIntId
      pendingConnections += (connectionId → message)
      log.debug(s"[DriveConnectivity.connectPipes] Connection added to pending list, connectionId: $connectionId")
      Right(connectionId)
    case s ⇒
      //Incorrect state
      val msg = s"[DriveConnectivity.connectPipes] Incorrect state $s, required Building"
      log.error(msg)
      Left(new IllegalStateException(msg))}
  /** Connecting of pipes on build of drive
    * Sends Msg.AddConnection to all inlets drive from pendingConnections list */
  def doConnectivity(): Unit = pendingConnections.foreach{
    case (connectionId, Msg.ConnectPipes(out, in)) ⇒ (out(),in()) match{
      case (outlet: OutPipe[_], inlet: InPipe[_]) ⇒
        inlet.pipeData.toolDrive ! Msg.AddConnection(connectionId, self, inlet.pipeData.pipeId, outlet.pipeData)
      case (o, i) ⇒
        log.error(
          s"[DriveConnectivity.doConnectivity] Plug or Socket is not an instance of Outlet[_] or Inlet[_], out: $o, in: $i.")}}
  /** Adding of connection to given inlet (inletId), send  Msg.ConnectTo to outlet
    * @param connectionId - Int
    * @param initiator - ActorRef
    * @param outletId - Int
    * @param outlet - OutletData */
  def addConnection(connectionId: Int, initiator: ActorRef, outletId: Int, outlet: OutletData): Unit = inlets
    .get(outletId) match{
      case Some(inState) ⇒
        inState.publishers += ((outlet.toolDrive, outlet.pipeId) → outlet)
        outlet.toolDrive ! Msg.ConnectTo(connectionId, initiator, outlet.pipeId, inState.pipe.pipeData)
        log.debug(s"[DriveConnectivity.addConnection] Connection added from $outlet to $inState.")
      case None ⇒
        log.error(s"[DriveConnectivity.addConnection] Inlet with ID $outletId, not in inlets list.")}
  /** Adding of connection to given outlet (outletId), and send Msg.PipesConnected
    * @param connectionId - Int
    * @param initiator - ActorRef
    * @param outletId - Int
    * @param inlet - PipeData */
  def connectTo(connectionId: Int, initiator: ActorRef, outletId: Int, inlet: InletData): Unit = outlets
    .get(outletId) match{
      case Some(outlet) ⇒
        val inDrive = inlet.toolDrive
        outlet.subscribers += ((inDrive, inlet.pipeId) → SubscriberData((inDrive, inlet.pipeId), inlet))
//        subscribedDrives.getOrElse(inDrive, {subscribedDrives += (inDrive → DrivesData(inDrive))})
        initiator ! Msg.PipesConnected(connectionId, outletId, inlet.pipeId)
        log.debug(s"[ConnectTo] Connection added, from: $outlet, to: $inlet")
      case None ⇒
        log.error(s"[DriveConnectivity.connectTo] Outlet with outletId: $outletId, not exist.")}
  /** Remove connected connection from pendingConnections list
    * @param connectionId - Int
    * @param inletId - Int
    * @param outletId - Int */
  def pipesConnected(connectionId: Int, inletId: Int, outletId: Int): Unit = pendingConnections
    .contains(connectionId) match{
      case true ⇒
        log.debug(s"[DriveConnectivity.pipesConnected] Connected, connectionId: $connectionId.")
        pendingConnections -= connectionId
      case false ⇒
        log.error(s"[DriveConnectivity.pipesConnected] Unknown connection with connectionId: $connectionId.")}
  /** Check if all connections connected
    * @return - true if all connected */
  def isAllConnected: Boolean = pendingConnections.isEmpty


  def getPendingList: Map[Int, Msg.ConnectPipes] = pendingConnections.toMap







}
