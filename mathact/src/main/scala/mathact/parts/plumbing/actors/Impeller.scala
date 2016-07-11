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

import akka.actor.{ActorRef, Props, Actor}
import mathact.parts.BaseActor
import mathact.parts.data.Msg
import mathact.parts.plumbing.Pump


/** User code processor
  * Created by CAB on 15.05.2016.
  */

class Impeller(drive: ActorRef) extends BaseActor{
  //Messages handling
  reaction(){
    //Run task
    case Msg.RunTask(id, name, task) ⇒
      log.debug(s"[Impeller.RunTask] Try to run task, id: $id, name: $name")
      try {
        task()
        log.debug(
          s"[Impeller.RunTask] Task done successfully, id: $id, name: $name")
        sender ! Msg.TaskDone(id, name)}
      catch{ case t: Throwable ⇒
        log.error(
          s"[Impeller.RunTask] Task failed, id: $id, name: $name, error: $t, stack: ${t.getStackTrace.mkString("\n")}")
        sender ! Msg.TaskFailed(id, name, t)}}}
