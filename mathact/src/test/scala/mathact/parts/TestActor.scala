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

import akka.actor.{Props, Actor, ActorSystem, ActorRef}


/** Test actors factory
  * Created by CAB on 17.08.2016.
  */

class TestActor(name: String, customReceive: ActorRef⇒PartialFunction[Any, Any], system: ActorSystem){
  //Actor
  val ref: ActorRef = system.actorOf(
    Props(new Actor{
      def receive = { case m ⇒
        sender ! customReceive(self).apply(m)}}),
    name)
  //Methods

    //TODO

}

object TestActor {
  def apply(name: String)(receive: ActorRef⇒PartialFunction[Any, Any])(implicit system: ActorSystem): TestActor =
    new TestActor(name, receive, system)}
