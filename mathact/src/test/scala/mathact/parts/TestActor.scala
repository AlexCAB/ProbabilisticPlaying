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
import scala.concurrent.duration._
import scala.reflect._


/** Test actors factory
  * Created by CAB on 17.08.2016.
  */

class TestActor(name: String, customReceive: ActorRef⇒PartialFunction[Any, Option[Any]], system: ActorSystem){
  //Parameters
  val expectMsgTimeout: FiniteDuration = 3.seconds
  val waitMsgTimeout: FiniteDuration = 500.millis
  //Messages
  private case class SendTo(to: ActorRef, msg: Any)
  private case class WatchFor(to: ActorRef)
  //Variables
  @volatile private var lastMessage: Option[Any] = None
  //Actor
  val ref: ActorRef = system.actorOf(
    Props(new Actor{
      def receive = {
        case WatchFor(actor) ⇒
          context.watch(actor)
        case SendTo(to, msg) ⇒
          to ! msg
        case msg ⇒
          println("[TestActor] Receive message: " + msg)
          lastMessage = None
          customReceive(self)
            .applyOrElse[Any, Option[Any]](msg, _ ⇒ {
              lastMessage = Some(msg)
              None})
            .foreach(m ⇒ sender ! m)}}),
    name)
  //Classes
  class Response(data: Option[Any]){
    def expectResponseType[T : ClassTag]: T = {
      val clazz = classTag[T]
      assert(data.nonEmpty, s"timeout during request while waiting for type $clazz")
      val msg = data.get
      assert(data.get.getClass == clazz.runtimeClass, s"expected $clazz, found ${msg.getClass} ($msg)")
      msg.asInstanceOf[T]}
    def expectResponseMsg(msg: Any): Any = {
      assert(data.nonEmpty, s"timeout during request while waiting for $msg")
      assert(data.get == msg, s"expected $msg, found ${data.get}")
      data.get}}
  //Methods
  /** Sending of any message to given actor
    * @param to - ActorRef, target actor
    * @param msg - Any, message */
  def send(to: ActorRef, msg: Any): Unit = {
    lastMessage = None
    ref ! SendTo(to, msg)
    println(s"[TestActor] Send message: $msg, to: $to")}
  /** Expectation of receiving of any message
    * @param duration - FiniteDuration, wait timeout
    * @return - Option[Any], None if timeout, Some(message) otherwise */
  def expectAnyMsg(implicit duration: FiniteDuration = expectMsgTimeout): Option[Any] = {
    var counter = duration.toMillis / 10
    var msg = lastMessage
    while (msg.isEmpty && counter > 0){
      Thread.sleep(10)
      msg = lastMessage
      counter -= 1}
    lastMessage = None
    msg}
  /** Expectation of receiving of given message
    * @param msg - Any, to check
    * @param duration - FiniteDuration, wait timeout
    * @return - Any, received message of throw AssertionError */
  def expectMsg(msg: Any)(implicit duration: FiniteDuration = expectMsgTimeout): Any = {
    val opMsg = expectAnyMsg(duration)
    assert(opMsg.nonEmpty, s"timeout ($duration) during expectMsg while waiting for $msg")
    assert(opMsg.get == msg, s"expected $msg, found ${opMsg.get}")
    opMsg.get}
  /** Expectation of receiving of message with given type
    * @param duration - FiniteDuration, wait timeout
    * @tparam T - expected type
    * @return - message */
  def expectMsgType[T : ClassTag](implicit duration: FiniteDuration = expectMsgTimeout): T = {
    val opMsg = expectAnyMsg(duration)
    val clazz = classTag[T]
    assert(opMsg.nonEmpty, s"timeout ($duration) during expectMsg while waiting for type $clazz")
    val msg = opMsg.get
    assert(opMsg.get.getClass == clazz.runtimeClass, s"expected $clazz, found ${msg.getClass} ($msg)")
    msg.asInstanceOf[T]}
  /** Watch for given actor
    * @param actor - ActorRef */
  def watch(actor: ActorRef): Unit = ref ! WatchFor(actor)



    //TODO Add more

}

object TestActor {
  def apply(name: String)(receive: ActorRef⇒PartialFunction[Any, Option[Any]])(implicit system: ActorSystem)
  :TestActor =
    new TestActor(name, receive, system)}
