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
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask

import scala.reflect.ClassTag


/** Base class for testing of actors
  * Created by CAB on 12.08.2016.
  */

class ActorTestSpec extends TestKit(ActorSystem("ActorTestSpec"))
with WordSpecLike with Matchers with BeforeAndAfterAll with FutureHelpers with RandomDataGenerators{
  //Parameters
  private implicit val askTimeout = Timeout(5.seconds)
  //Stop actor sys
  override def afterAll {TestKit.shutdownActorSystem(system)}
  //Helpers definitions
  case object GetDriveState
  //Actor helpers
  implicit class ActorRefEx(actor: ActorRef){
    def askFor[R : ClassTag](msg: AnyRef): R = Await.result(ask(actor, msg).mapTo[R], askTimeout.duration)
    def askForState[R : ClassTag]: R = askFor(GetDriveState)}

  //TODO Add more

}
