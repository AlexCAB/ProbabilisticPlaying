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

import mathact.parts.data.ActorState
import mathact.parts.plumbing.fitting.{InPipe, OutPipe}


/** Drive construction
  * Created by CAB on 22.08.2016.
  */

private [mathact] trait DriveBuilding { _: Drive ⇒
  /** Adding of new outlet, called from object
    * @param pipe - Outlet[_]
    * @param name - Option[String]
    * @return - Either[Throwable, pipeId] */
  def addOutletAsk(pipe: OutPipe[_], name: Option[String], state: ActorState): Either[Throwable,Int] = state match {
    case ActorState.Building ⇒
      //Check of already added
      outlets.values.filter(_.pipe == pipe) match{
        case Nil ⇒
          //Create and add
          val id = nextIntId
          outlets += (id → OutletState(id, name, pipe))
          log.debug(s"[DriveBuilding.addOutletAsk] Outlet: $pipe, added with ID: $id")
          Right(id)
        case o :: _ ⇒
          //Double creating
          val msg = s"[DriveBuilding.addOutletAsk] Outlet: $pipe, is registered more then once"
          log.error(msg)
          Left(new IllegalArgumentException(msg))}
    case s ⇒
      //Incorrect state
      val msg = s"[DriveBuilding.addOutletAsk] Incorrect state $s, required Building"
      log.error(msg)
      Left(new IllegalStateException(msg))}
  /** Adding of new inlet, called from object
    * @param pipe - Inlet[_]
    * @param name - Option[String]
    * @return - Either[Throwable, pipeId] */
  def addInletAsk(pipe: InPipe[_], name: Option[String], state: ActorState): Either[Throwable,Int] = state match {
    case ActorState.Building ⇒
      //Check if pipe already added
      inlets.values.filter(_.pipe == pipe) match{
        case Nil ⇒
          //Create and add
          val id = nextIntId
          inlets += (id → InletState(id, name, pipe))
          log.debug(s"[DriveBuilding.addInletAsk] Inlet: $pipe, added with ID: $id")
          Right(id)
        case o :: _ ⇒
          //Double creating
          val msg = s"[DriveBuilding.addInletAsk] Inlet: $pipe, is registered more then once"
          log.error(msg)
          Left(new IllegalArgumentException(msg))}
    case s ⇒
      //Incorrect state
      val msg = s"[DriveBuilding.addInletAsk] Incorrect state $s, required Building"
      log.error(msg)
      Left(new IllegalStateException(msg))}


  /** Terminating of this drive, currently here only logging */
  def doTerminating(): Unit = {
    log.debug(s"[DriveBuilding.doTerminating] Start of terminating of drive.")}










}
