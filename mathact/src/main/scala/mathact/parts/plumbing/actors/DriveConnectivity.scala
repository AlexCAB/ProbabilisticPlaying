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

import mathact.parts.data.Msg
import mathact.parts.data.Msg._
import mathact.parts.plumbing.fitting.{Inlet, Outlet, Socket, Plug}
import scala.collection.mutable.{Map ⇒ MutMap, Queue ⇒ MutQueue}


/** Handling of connections and disconnections
  * Created by CAB on 22.08.2016.
  */

trait DriveConnectivity { _: Drive ⇒


  //Variables
  val pendingConnections = MutMap[Int, Msg.ConnectPipes]()

  //Methods
  /** Adding of ConnectPipes to pending connections
    * @param message = ConnectPipes */
  def connectPipes(message: ConnectPipes): Unit = state match{
    case State.Creating ⇒
      //On create store to pending connections
      pendingConnections += (nextIntId → message)
    case st ⇒
      //Error in case state not Creating
      log.error(s"[DriveConnectivity.connectPipes] Incorrect state $st, can be called only it State.Creating.")}

  def doConnectivity(): Unit = pendingConnections.foreach{ case (id, Msg.ConnectPipes(out, in)) ⇒ (out(),in()) match{
    case (outlet: Outlet[_], inlet: Inlet[_]) ⇒
      val pipe = outlet.getPipeData
      pipe.toolDrive ! Msg.AddConnection(id, self, pipe.pipeId, inlet.getPipeData)
    case (o, i) ⇒
      log.error(
        s"[DriveConnectivity.doConnectivity] Plug or Socket is not an instance of Outlet[_] or Inlet[_], out: $o, in: $i.")}





  }

  def isAllConnected: Boolean = {

  }







}
