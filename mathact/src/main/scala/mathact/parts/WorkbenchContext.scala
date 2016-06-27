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

package mathact.parts

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import mathact.parts.data.Msg
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask


/** Provide support and management of Workbench
  * Created by CAB on 20.06.2016.
  */

class WorkbenchContext(val system: ActorSystem, val controller: ActorRef) {
  //Parameters
  private implicit val askTimeout = Timeout(5.seconds)
  //Get pumping actor
  val pumping: ActorRef = Await
    .result(ask(controller, Msg.GetPumpingActor).mapTo[ActorRef], askTimeout.duration)

  println(pumping)


}
