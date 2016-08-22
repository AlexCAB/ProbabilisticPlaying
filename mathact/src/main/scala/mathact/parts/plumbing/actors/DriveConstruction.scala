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

import mathact.parts.plumbing.fitting.{Inlet, Outlet}


/** Drive construction
  * Created by CAB on 22.08.2016.
  */

trait DriveConstruction { _: Drive ⇒
  /** Adding of new outlet, called from object
    * @param pipe - Outlet[_]
    * @param name - Option[String]
    * @return - Either[Throwable, pipeId] */
  def addOutlet(pipe: Outlet[_], name: Option[String]): Either[Throwable, Int] = {
    //Check of already added
    outlets.values.filter(_.pipe == pipe) match{
      case Nil ⇒
        //Create and add
        val id = nextIntId
        outlets += (id → OutletData(id, name, pipe))
        log.debug(s"[DriveConstruction.addOutlet] Outlet: $pipe, added with ID: $id")
        Right(id)
      case o :: _ ⇒
        //Double creating
        log.warning(s"[DriveConstruction.addOutlet] Outlet: $pipe, is registered more then once")
        Right(o.id)}}
  /** Adding of new inlet, called from object
    * @param pipe - Inlet[_]
    * @param name - Option[String]
    * @return - Either[Throwable, pipeId] */
  def addInlet(pipe: Inlet[_], name: Option[String]): Either[Throwable, Int] = {
    inlets.values.filter(_.pipe == pipe) match{
      case Nil ⇒
        //Create and add
        val id = nextIntId
        inlets += (id → InletData(id, name, pipe))
        log.debug(s"[DriveConstruction.addInlet] Inlet: $pipe, added with ID: $id")
        Right(id)
      case o :: _ ⇒
        //Double creating
        log.warning(s"[DriveConstruction.addInlet] Inlet: $pipe, is registered more then once")
        Right(o.id)}}}
