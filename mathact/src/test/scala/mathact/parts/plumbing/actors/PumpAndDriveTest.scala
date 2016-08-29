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

import akka.actor.{Terminated, ActorRef, Props}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mathact.parts.plumbing.fitting._
import mathact.parts._
import mathact.parts.data.Msg
import mathact.parts.plumbing.{Pump, Fitting}
import org.scalatest.Suite
import scala.concurrent.Future
import scala.concurrent.duration._


/** Testing of Pump with Drive actor
  * Created by CAB on 15.08.2016.
  */

class PumpAndDriveTest extends ActorTestSpec{
  //Test data
  trait TestCase extends Suite{
    import system.dispatcher
    //Helpers definitions
    case class DriveState(
      outlets: Map[Int, (OutPipe[_], Map[(ActorRef, Int), Int])], // (Outlet ID, (Outlet, subscribers(id, inletQueueSize))
      inlets: Map[Int, (InPipe[_], Int)],    // (Inlet ID, Outlet, taskQueueSize)
      pendingConnections: Map[Int, Msg.ConnectPipes])
    //Helpers actors
    lazy val testController = TestProbe("TestController_" + randomString())
    lazy val testUserLogging = TestProbe("UserLogging_" + randomString())
    lazy val testPumping = TestActor("TestPumping_" + randomString())(self ⇒ {
      case Msg.NewDrive(toolPump, toolName, toolImage) ⇒ Some{ Right{
        val drive = system.actorOf(Props(
          new Drive(toolPump,  "TestTool", self, testUserLogging.ref){
            //Get actor state
            override def receive: PartialFunction[Any, Unit]  = {
              case GetDriveState ⇒ sender ! DriveState(
                outlets = outlets
                  .map{ case (id, d) ⇒
                    (id, (d.pipe, d.subscribers.values.map(s ⇒ (s.id, s.inletQueueSize)).toMap))}
                  .toMap,
                inlets = inlets
                  .map{ case (id, d) ⇒
                    (id, (d.pipe, d.taskQueue.size))}
                  .toMap,
                getPendingList)
              case m ⇒ super.receive.apply(m)}}),
          "Drive_" + randomString())
        println(s"[PumpAndDriveTest.testPumping.NewDrive] Created of drive for tool: $toolName, drive: $drive")
        drive}}})
    //Test workbench context
    lazy val testWorkbenchContext =
      new WorkbenchContext(system, testController.ref, testPumping.ref, ConfigFactory.load()){
        override val pumping: ActorRef = testPumping.ref}
    //Test tools
    object tools{
      lazy val testTool = new Fitting with OnStart with OnStop{
        //Variable
        @volatile private var onStartCall = false
        @volatile private var onStopCall = false
        @volatile private var procTimeout: Option[Duration] = None
        @volatile private var procError: Option[Throwable] = None
        //Pump
        val pump: Pump = new Pump(testWorkbenchContext, this, "TestTool", None){
          override val pushTimeoutCoefficient = 10
          override val startFunctionTimeout = 4.second
          override val messageProcessingTimeout = 4.second
          override val stopFunctionTimeout = 4.second}
        //Pipes
        val testPipe = new TestIncut[Double]
        lazy val outlet = Outlet(testPipe, "testOutlet")
        lazy val inlet = Inlet(testPipe, "testInlet")
        //On start and stop
        protected def onStart() = {
          println("[PumpAndDriveTest.testTool] onStart called.")
          onStartCall = true
          procTimeout.foreach(d ⇒ sleep(d))
          procError.foreach(e ⇒ throw e)}
        protected def onStop() = {
          println("[PumpAndDriveTest.testTool] onStop called.")
          onStopCall = true
          procTimeout.foreach(d ⇒ sleep(d))
          procError.foreach(e ⇒ throw e)}
        //Helpers methods
        def setProcTimeout(d: Duration): Unit = { procTimeout = Some(d) }
        def setProcError(err: Option[Throwable]): Unit = { procError = err }
        def isOnStartCalled: Boolean = onStartCall
        def isOnStopCalled: Boolean = onStopCall}
      lazy val testDrive = testTool.pump.drive
      lazy val otherDrive = TestActor("TestOtherDriver_" + randomString())(self ⇒ {
        case Msg.AddOutlet(pipe, _) ⇒ Some(Right(1))
        case Msg.AddInlet(pipe, _) ⇒  Some(Right(2))
        case Msg.UserData(outletId, _) ⇒  Some(Right(None))})
      lazy val otherTool = new Fitting{
        //Pump
        val pump: Pump = new Pump(testWorkbenchContext, this, "OtherTool", None){
          override val drive = otherDrive.ref}
        //Pipes
        val testIncut = new TestIncut[Double]
        lazy val outlet = Outlet(testIncut, "otherOutlet")
        lazy val inlet = Inlet(testIncut, "otherInlet")}
      lazy val builtTool = {
        testPumping.send(testTool.pump.drive, Msg.BuildDrive)
        testPumping.expectMsg(Msg.DriveBuilt)
        testTool.isOnStartCalled shouldEqual false
        testTool}
      lazy val startedTool = {
        testPumping.send(testTool.pump.drive, Msg.BuildDrive)
        testPumping.expectMsg(Msg.DriveBuilt)
        testPumping.send(testTool.pump.drive, Msg.StartDrive)
        testPumping.expectMsg(Msg.DriveStarted)
        testTool.isOnStopCalled shouldEqual false
        testTool}
      lazy val connectedTools = {
        //Preparing
        val testOutlet = testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
        val testInlet = testTool.inlet.asInstanceOf[InPipe[Double]].pipeData
        val otherOutlet = otherTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
        val otherInlet = otherTool.inlet.asInstanceOf[InPipe[Double]].pipeData
        //Connecting
        testTool.inlet.plug(otherTool.outlet)
        testTool.outlet.attach(otherTool.inlet)
        //Process for other tool
        testPumping.send(testDrive, Msg.BuildDrive)
        val conMsg =  otherDrive.expectNMsg(2)
        val addConnection = conMsg.getOneWithType[Msg.AddConnection]
        val connectTo = conMsg.getOneWithType[Msg.ConnectTo]
        otherDrive.send(
          testDrive,
          Msg.ConnectTo(addConnection.connectionId, addConnection.initiator, addConnection.outlet.pipeId, otherInlet))
        otherDrive.send(
          connectTo.initiator,
          Msg.PipesConnected(connectTo.connectionId, connectTo.inlet.pipeId, connectTo.outletId))
        testPumping.expectMsg(Msg.DriveBuilt)
        //Starting
        testPumping.send(testDrive, Msg.StartDrive)
        testPumping.expectMsg(Msg.DriveStarted)    //Test drive in Working state
        //Outlets and inlets data
        (testOutlet, testInlet, otherOutlet, otherInlet)}



    }
  }
  //Testing
//  "On starting" should{
//    "adding of Outlet and Inlet" in new TestCase {
//      //Preparing
//      val outletId = tools.testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData.pipeId
//      val inletId = tools.testTool.inlet.asInstanceOf[InPipe[Double]].pipeData.pipeId
//      tools.otherTool.outlet
//      tools.otherTool.inlet
//       //Testing
//      val DriveState(outlets, inlets, _) = tools.testDrive.askForState[DriveState]
//      outlets should have size 1
//      inlets should have size 1
//      outlets.keys should contain (outletId)
//      inlets.keys should contain (inletId)}
//    "before BuildDrive, add new connections to pending list" in new TestCase {
//      //Preparing
//      val testOutlet1 = tools.testTool.outlet
//      val testInlet1 = tools.testTool.inlet
//      val otherOutlet1 = tools.otherTool.outlet
//      val otherInlet1 =tools.otherTool.inlet
//      //Connecting and disconnecting
//      tools.testTool.outlet.attach(otherInlet1)
//      tools.testTool.inlet.plug(otherOutlet1)
//      //Testing
//      val pendingCon = tools.testDrive.askForState[DriveState].pendingConnections
//      pendingCon should have size 2}
//    "by BuildDrive, create connections from pending list and reply with DriveBuilt (for 'plug')" in new TestCase {
//      //Preparing
//      val outlet = tools.otherTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
//      val inlet = tools.testTool.inlet.asInstanceOf[InPipe[Double]].pipeData
//      //Connecting (test tool have inlet)
//      tools.testTool.inlet.plug(tools.otherTool.outlet)
//      tools.testDrive.askForState[DriveState].pendingConnections should have size 1
//      //Send BuildDrive
//      testPumping.send(tools.testDrive, Msg.BuildDrive)
//      //Test connecting
//      val connectTo = tools.otherDrive.expectMsgType[Msg.ConnectTo]
//      println(s"[PumpAndDriveTest] connectTo: $connectTo")
//      connectTo.initiator    shouldEqual tools.testDrive
//      connectTo.outletId     shouldEqual outlet.pipeId
//      connectTo.inlet.pipeId shouldEqual inlet.pipeId
//      //Send Msg.PipesConnected and expect Msg.DriveBuilt
//      tools.otherDrive.send(connectTo.initiator, Msg.PipesConnected(connectTo.connectionId, inlet.pipeId, outlet.pipeId))
//      testPumping.expectMsg(Msg.DriveBuilt)
//      //Check pending list
//      sleep(500.millis) //Wait for processing of PipesConnected by testTool
//      tools.testDrive.askForState[DriveState].pendingConnections should have size 0}
//    "by BuildDrive, create connections from pending list and reply with DriveBuilt (for 'attach')" in new TestCase {
//      //Preparing
//      val outlet = tools.testTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
//      val inlet = tools.otherTool.inlet.asInstanceOf[InPipe[Double]].pipeData
//      //Connecting (test tool have outlet)
//      tools.testTool.outlet.attach(tools.otherTool.inlet)
//      tools.testDrive.askForState[DriveState].pendingConnections should have size 1
//      //Send BuildDrive
//      testPumping.send(tools.testDrive, Msg.BuildDrive)
//      //Test connecting
//      val addConnection = tools.otherDrive.expectMsgType[Msg.AddConnection]
//      addConnection.initiator     shouldEqual tools.testDrive
//      addConnection.inletId       shouldEqual inlet.pipeId
//      addConnection.outlet.pipeId shouldEqual outlet.pipeId
//      tools.otherDrive.send(
//        tools.testDrive,
//        Msg.ConnectTo(addConnection.connectionId, addConnection.initiator, outlet.pipeId, inlet))
//      //Expect DriveBuilt
//      testPumping.expectMsg(Msg.DriveBuilt)
//      sleep(500.millis) //Wait for processing of PipesConnected by testTool
//      tools.testDrive.askForState[DriveState].pendingConnections should have size 0}
//    "by StartDrive, run user init function and reply with DriveStarted" in new TestCase {
//      //Preparing
//      tools.builtTool
//      //Test
//      testPumping.send(tools.testDrive, Msg.StartDrive)
//      testPumping.expectMsg(Msg.DriveStarted)
//      tools.testTool.isOnStartCalled shouldEqual true}
//    "by StartDrive, for case TaskTimeout send LogWarning to user logging actor and keep working" in new TestCase {
//      //Preparing
//      tools.builtTool
//      tools.testTool.setProcTimeout(5.second)
//      //Test
//      testPumping.send(tools.testDrive, Msg.StartDrive)
//      sleep(3.second) //Wait for LogWarning will send
//      val logWarning = testUserLogging.expectMsgType[Msg.LogWarning]
//      println("[PumpAndDriveTest] logWarning: " + logWarning)
//      testPumping.expectMsg(Msg.DriveStarted)
//      tools.testTool.isOnStartCalled shouldEqual true}
//    "by StartDrive, for case TaskFailed send LogError to user logging actor and reply with DriveStarted" in new TestCase {
//      //Preparing
//      tools.builtTool
//      tools.testTool.setProcError(Some(new Exception("Oops!!!")))
//      //Test
//      testPumping.send(tools.testDrive, Msg.StartDrive)
//      val logError = testUserLogging.expectMsgType[Msg.LogError]
//      println("[PumpAndDriveTest] logError: " + logError)
//      testPumping.expectMsg(Msg.DriveStarted)
//      tools.testTool.isOnStartCalled shouldEqual true}
//  }
  "On user message" should{
//    "by call pour(value), send UserData, to all inlets of connected drives" in new TestCase {
//      //Preparing
//      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      //Call pour(value) for other tool
//      tools.otherTool.testIncut.sendValue(value1)
//      val userData = tools.otherDrive.getProcessedMessages.getOneWithType[Msg.UserData[Double]]
//      println("[PumpAndDriveTest] userData: " + userData)
//      userData.outletId shouldEqual otherOutlet.pipeId
//      userData.value    shouldEqual value1
//      //Call pour(value) test tool
//      tools.testTool.testPipe.sendValue(value2)
//      val userMessage = tools.otherDrive.expectMsgType[Msg.UserMessage[Double]]
//      println("[PumpAndDriveTest] userMessage: " + userData)
//      userMessage.outletId shouldEqual testOutlet.pipeId
//      userMessage.inletId  shouldEqual otherInlet.pipeId
//      userMessage.value    shouldEqual value2}
//    "by UserMessage, put user message to queue and reply with DriveLoad, but not process till Starting" in new TestCase {
//      //Preparing
//      val otherOutlet = tools.otherTool.outlet.asInstanceOf[OutPipe[Double]].pipeData
//      val testInlet = tools.testTool.inlet.asInstanceOf[InPipe[Double]].pipeData
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      //Connecting
//      tools.testTool.inlet.plug(tools.otherTool.outlet)
//      testPumping.send(tools.testDrive, Msg.BuildDrive)
//      val conTo = tools.otherDrive.expectMsgType[Msg.ConnectTo]
//      tools.otherDrive.send(conTo.initiator, Msg.PipesConnected(conTo.connectionId, conTo.inlet.pipeId, conTo.outletId))
//      testPumping.expectMsg(Msg.DriveBuilt) //Before send this tool will switch to Starting state.
//      //Send user messages
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      val driveLoad1 = tools.otherDrive.expectMsgType[Msg.DriveLoad]
//      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
//      driveLoad1.drive        shouldEqual tools.testDrive
//      driveLoad1.maxQueueSize shouldEqual 1
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
//      val driveLoad2 = tools.otherDrive.expectMsgType[Msg.DriveLoad]
//      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
//      driveLoad2.drive        shouldEqual tools.testDrive
//      driveLoad2.maxQueueSize shouldEqual 2
//      tools.testTool.testPipe.getReceivedValues.size shouldEqual 0
//      //Starting
//      testPumping.send(tools.testDrive, Msg.StartDrive)
//      val driveLoads = tools.otherDrive.expectNMsg(2).map(_.asInstanceOf[Msg.DriveLoad])
//      println("[PumpAndDriveTest] driveLoads: " + driveLoads)
//      driveLoads(0).maxQueueSize shouldEqual 1
//      driveLoads(1).maxQueueSize shouldEqual 0
//      testPumping.expectMsg(Msg.DriveStarted)
//      //Check of message processing
//      sleep(1.seconds) //Wait for messages will processed
//      tools.testTool.testPipe.getReceivedValues.size shouldEqual 2
//      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1, value2)}
//    "by UserMessage, processing of messages with no load response" in new TestCase {
//      //Preparing
//      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      tools.testTool.testPipe.setProcTimeout(1.second)
//      //Send first messages
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      tools.otherDrive.expectNoMsg(2.seconds)
//      tools.testTool.testPipe.getReceivedValues.size shouldEqual 1
//      tools.testTool.testPipe.getReceivedValues.head shouldEqual value1
//      //Send second messages
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
//      tools.otherDrive.expectNoMsg(2.seconds)
//      tools.testTool.testPipe.getReceivedValues.size shouldEqual 2
//      tools.testTool.testPipe.getReceivedValues(1) shouldEqual value2}
//    "by UserMessage, processing of messages with load response" in new TestCase {
//      //Preparing
//      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      tools.testTool.testPipe.setProcTimeout(2.second)
//      //Send message
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      tools.otherDrive.expectNoMsg(1.seconds)
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
//      //Load messages
//      val driveLoad1 = tools.otherDrive.expectMsgType[Msg.DriveLoad]
//      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
//      driveLoad1.drive        shouldEqual tools.testDrive
//      driveLoad1.maxQueueSize shouldEqual 1
//      sleep(1.second) //Wait for end processing of first message
//      val driveLoad2 = tools.otherDrive.expectMsgType[Msg.DriveLoad]
//      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
//      driveLoad2.drive        shouldEqual tools.testDrive
//      driveLoad2.maxQueueSize shouldEqual 0
//      //Check of message processing
//      sleep(3.seconds) //Wait for second messages will processed
//      tools.testTool.testPipe.getReceivedValues.size shouldEqual 2
//      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1, value2)}
//    "by UserMessage, in case message processing time out send warning to user logger" in new TestCase {
//      //Preparing
//      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
//      val value1 = randomDouble()
//      tools.testTool.testPipe.setProcTimeout(5.second)
//      //Send message
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      sleep(3.seconds) //Wait for messages long timeout
//      val logWarn = testUserLogging.expectMsgType[Msg.LogWarning]
//      println("[PumpAndDriveTest] logWarn: " + logWarn)
//      //Check of message processing
//      sleep(1.seconds) //Wait for second messages will processed
//      tools.testTool.testPipe.getReceivedValues.size shouldEqual 1
//      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1)}
//    "by UserMessage, in case message processing error send error to user logger" in new TestCase {
//      //Preparing
//      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      //Send and get error
//      tools.testTool.testPipe.setProcError(Some(new Exception("Oops!!!")))
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      val logError = testUserLogging.expectMsgType[Msg.LogError]
//      println("[PumpAndDriveTest] logError: " + logError)
//      sleep(1.seconds) //Wait for second messages will processed
//      tools.testTool.testPipe.getReceivedValues.size shouldEqual 1
//      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1)
//      //Send and not get error
//      tools.testTool.testPipe.setProcError(None)
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
//      sleep(1.seconds) //Wait for second messages will processed
//      tools.testTool.testPipe.getReceivedValues.size shouldEqual 2
//      tools.testTool.testPipe.getReceivedValues shouldEqual List(value1, value2)}
    "by DriveLoad, evaluate message handling timeout" in new TestCase {
      //Preparing
      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
      val subId = (tools.otherDrive.ref, otherInlet.pipeId)
      val queueSize = randomInt(100, 1000)
      //Test for first message
      tools.otherDrive.send(tools.testDrive, Msg.DriveLoad(subId, testOutlet.pipeId, queueSize))
      sleep(1.second) //Wait for processing
      tools.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._2(subId) shouldEqual queueSize

      ??? //TODO проверка пересчёта вемени

      //Test for second message
      tools.otherDrive.send(tools.testDrive, Msg.DriveLoad(subId, testOutlet.pipeId, 0))
      sleep(1.second) //Wait for processing
      tools.testDrive.askForState[DriveState].outlets(testOutlet.pipeId)._2(subId) shouldEqual 0

      ??? //TODO проверка пересчёта вемени

    }
  }


//TODO Добавить пропус сообщений



//  "On stopping" should{
//    "by StopDrive, run user stop function and reply with DriveStopped" in new TestCase {
//      //Preparing
//      tools.connectedTools
//      //Test
//      testPumping.send(tools.testDrive, Msg.StopDrive)
//      testPumping.expectMsg(Msg.DriveStopped)
//      tools.testTool.isOnStopCalled shouldEqual true}
//    "by StopDrive, for case TaskTimeout send LogWarning to user logging actor and keep working" in new TestCase {
//      //Preparing
//      tools.connectedTools
//      tools.testTool.setProcTimeout(5.second)
//      //Test
//      testPumping.send(tools.testDrive, Msg.StopDrive)
//      sleep(3.second) //Wait for LogWarning will send
//      val logWarning = testUserLogging.expectMsgType[Msg.LogWarning]
//      println("[PumpAndDriveTest] logWarning: " + logWarning)
//      testPumping.expectMsg(Msg.DriveStopped)
//      tools.testTool.isOnStopCalled shouldEqual true}
//    "by StopDrive, for case TaskFailed send LogError to user logging actor and reply with DriveStopped" in new TestCase {
//      //Preparing
//      tools.connectedTools
//      tools.testTool.setProcError(Some(new Exception("Oops!!!")))
//      //Test
//      testPumping.send(tools.testDrive, Msg.StopDrive)
//      val logError = testUserLogging.expectMsgType[Msg.LogError]
//      println("[PumpAndDriveTest] logError: " + logError)
//      testPumping.expectMsg(Msg.DriveStopped)
//      tools.testTool.isOnStopCalled shouldEqual true}
//    "by TerminateDrive, stop receive new user msgs, wait for empty queues, and terminate " in new TestCase {
//      //Preparing
//      val (testOutlet, testInlet, otherOutlet, otherInlet) = tools.connectedTools
//      testPumping.send(tools.testDrive, Msg.StopDrive)
//      testPumping.expectMsg(Msg.DriveStopped)
//      tools.testTool.isOnStopCalled shouldEqual true
//      tools.testTool.testPipe.setProcTimeout(3.second)
//      testPumping.watch(tools.testDrive)
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      //Send two slow messages
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
//      val driveLoad1 = tools.otherDrive.expectMsgType[Msg.DriveLoad]
//      println("[PumpAndDriveTest] driveLoad1: " + driveLoad1)
//      driveLoad1.maxQueueSize shouldEqual 1
//      //Send TerminateDrive
//      testPumping.send(tools.testDrive, Msg.TerminateDrive)
//      sleep(1.second) //Wait for TerminateDrive processed
//      //Send two messages, which will not processed
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, -1))
//      tools.otherDrive.send(tools.testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, -2))
//      tools.otherDrive.expectNoMsg(1.second)
//      //The first slow message processed
//      val driveLoad2 = tools.otherDrive.expectMsgType[Msg.DriveLoad]
//      println("[PumpAndDriveTest] driveLoad2: " + driveLoad2)
//      driveLoad2.maxQueueSize shouldEqual 0
//      //Termination
//      sleep(2.second) //Wait for second slow message will processed
//      testPumping.expectMsg(Msg.DriveTerminated)
//      testPumping.expectMsgType[Terminated].actor shouldEqual tools.testDrive}
//
//
//
//
//
//
//
//
//  }












//    "by StopDrive, run user stopping function and reply with DriveStopped" in new TestCase {
//      //Preparing
//      testTool
//      testPumping.send(testDrive, Msg.BuildDrive)
//      testPumping.expectMsg(Msg.DriveBuilt)
//      testPumping.send(testDrive, Msg.StartDrive)
//      testPumping.expectMsg(Msg.DriveStarted)
//      testTool.isOnStopCalled shouldEqual false
//      //Test
//      testPumping.send(testDrive, Msg.StopDrive)
//      testPumping.expectMsg(Msg.DriveStopped)
//      testTool.isOnStopCalled shouldEqual true}
//    "by TerminateDrive, disconnect all connections and reply with DriveTerminated" in new TestCase {
//      //Preparing
//      val testOutlet = testTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
//      val testInlet = testTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
//      val otherOutlet = otherTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
//      val otherInlet = otherTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
//      testPumping.watch(testDrive)
//      //Connecting
//      testTool.inlet.plug(otherTool.outlet)
//      testTool.outlet.attach(otherTool.inlet)
//      testDrive.askForState[DriveState].pendingConnections should have size 2
//      //Build
//      testPumping.send(testDrive, Msg.BuildDrive)
//      otherDrive.expectMsgType[Msg.ConnectTo].initiator shouldEqual testDrive
//      otherDrive.send(testDrive, Msg.ConnectionAdded(testInlet.pipeId, otherOutlet.pipeId))
//      otherDrive.expectMsgType[Msg.AddConnection].initiator shouldEqual testDrive
//      otherDrive.send(testDrive, Msg.ConnectTo(testDrive, testOutlet.pipeId, otherInlet))
//      otherDrive.expectMsgType[Msg.ConnectionAdded]
//      otherDrive.send(testDrive, Msg.PipesConnected(otherInlet.pipeId, testOutlet.pipeId))
//      testPumping.expectMsg(Msg.DriveBuilt)
//      //Start and stop
//      testPumping.send(testDrive, Msg.StartDrive)
//      testPumping.expectMsg(Msg.DriveStarted)
//      testPumping.send(testDrive, Msg.StopDrive)
//      testPumping.expectMsg(Msg.DriveStopped)
//      //Terminate
//      testPumping.send(testDrive, Msg.TerminateDrive)
//      //First disconnect inlets of testTool
//      val delInlet = otherDrive.expectMsgType[Msg.DelConnection]
//      delInlet.initiator    shouldEqual testDrive
//      delInlet.outletId     shouldEqual otherOutlet.pipeId
//      delInlet.inlet.pipeId shouldEqual testInlet.pipeId
//      otherDrive.send(testDrive, Msg.DisconnectFrom(testDrive, testInlet.pipeId, otherOutlet))
//      val inletDeleted = otherDrive.expectMsgType[Msg.ConnectionDeleted]
//      inletDeleted.outletId shouldEqual otherOutlet.pipeId
//      inletDeleted.inletId  shouldEqual testInlet.pipeId
//      otherDrive.send(testDrive, Msg.PipesDisconnected(testInlet.pipeId, otherOutlet.pipeId))
//      //Second disconnect outlets of testTool
//      val disoutletFrom = otherDrive.expectMsgType[Msg.DisconnectFrom]
//      disoutletFrom.initiator    shouldEqual testDrive
//      disoutletFrom.inletId      shouldEqual otherInlet.pipeId
//      disoutletFrom.outlet.pipeId shouldEqual testOutlet.pipeId
//      otherDrive.send(testDrive, Msg.ConnectionDeleted(testOutlet.pipeId, otherInlet.pipeId))
//      //Terminated
//      testPumping.expectMsg(Msg.DriveTerminated)
//      val t = testPumping.expectMsgType[Terminated]().actor shouldEqual testDrive}
//  }






//  "on connect and disconnect" should{
//    "connect and disconnect to another drive with 'plug' and 'unplug'" in new TestCase {
//      //Preparing
//      val outlet = otherTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
//      val inlet = startedTestTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
//      //Connecting (test tool have inlet)
//      startedTestTool.inlet.plug(otherTool.outlet)
//      //Test connecting
//      val connectTo = otherDrive.expectMsgType[Msg.ConnectTo]
//      connectTo.initiator    shouldEqual testDrive
//      connectTo.outletId     shouldEqual outlet.pipeId
//      connectTo.inlet.pipeId shouldEqual inlet.pipeId
//      otherDrive.send(testDrive, Msg.ConnectionAdded(inlet.pipeId, outlet.pipeId))
//      //Disconnecting
//      startedTestTool.inlet.unplug(otherTool.outlet)
//      //Test disconnecting
//      val delConnection = otherDrive.expectMsgType[Msg.DelConnection]
//      delConnection.initiator    shouldEqual testDrive
//      delConnection.outletId     shouldEqual outlet.pipeId
//      delConnection.inlet.pipeId shouldEqual inlet.pipeId
//      otherDrive.send(testDrive, Msg.DisconnectFrom(delConnection.initiator, inlet.pipeId, outlet))
//      val connectionDeleted = otherDrive.expectMsgType[Msg.ConnectionDeleted]
//      connectionDeleted.outletId shouldEqual outlet.pipeId
//      connectionDeleted.inletId  shouldEqual inlet.pipeId
//      otherDrive.send(testDrive, Msg.PipesDisconnected(inlet.pipeId, outlet.pipeId))}
//    "connect and disconnect to another drive with 'attach' and 'detach'" in new TestCase {
//      //Preparing
//      val outlet = startedTestTool.outlet.asInstanceOf[Pipe[Double]].getPipeData
//      val inlet = otherTool.inlet.asInstanceOf[Pipe[Double]].getPipeData
//      //Connecting (test tool have outlet)
//      startedTestTool.outlet.attach(otherTool.inlet)
//      //Test connecting
//      val addConnection = otherDrive.expectMsgType[Msg.AddConnection]
//      addConnection.initiator     shouldEqual testDrive
//      addConnection.inletId       shouldEqual inlet.pipeId
//      addConnection.outlet.pipeId shouldEqual outlet.pipeId
//      otherDrive.send(testDrive, Msg.ConnectTo(addConnection.initiator, outlet.pipeId, inlet))
//      val connectionAdded = otherDrive.expectMsgType[Msg.ConnectionAdded]
//      connectionAdded.inletId  shouldEqual inlet.pipeId
//      connectionAdded.outletId shouldEqual outlet.pipeId
//      otherDrive.send(testDrive, Msg.PipesConnected(inlet.pipeId, outlet.pipeId))
//      //Disconnecting
//      testTool.outlet.detach(otherTool.inlet)
//      //Test disconnecting
//      val disconnectFrom = otherDrive.expectMsgType[Msg.DisconnectFrom]
//      disconnectFrom.initiator    shouldEqual testDrive
//      disconnectFrom.inletId      shouldEqual inlet.pipeId
//      disconnectFrom.outlet.pipeId shouldEqual outlet
//      otherDrive.send(testDrive, Msg.ConnectionDeleted(inlet.pipeId, outlet.pipeId))}
//    "not connect pipes in Stopping or Terminating mode" in new TestCase {
//      //Stopping
//      testPumping.send(testDrive, Msg.StartDrive)
//      testPumping.expectMsg(Msg.DriveStarted)
//      //Try to connect
//      startedTestTool.inlet.plug(otherTool.outlet)
//      otherDrive.expectNoMsg()
//      //Terminate
//      testPumping.send(testDrive, Msg.TerminateDrive)
//      testPumping.expectMsg(Msg.DriveTerminated)
//      //Try to connect
//      startedTestTool.inlet.plug(otherTool.outlet)
//      otherDrive.expectNoMsg()}
//  }
//  "on user message" should{
//    "by call pour(value), send UserData, to all inlets of connected drives" in new TestCase {
//      //Preparing
//      val (testInlet, testOutlet, otherInlet, otherOutlet) = connectedTools
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      //Call pour(value) for other tool
//      otherTool.testPipe.sendValue(value1)
//      val userData = otherDrive.expectMsgType[Msg.UserData[Double]]
//      userData.outletId shouldEqual otherOutlet.pipeId
//      userData.value    shouldEqual value1
//      //Call pour(value) test tool
//      testTool.testPipe.sendValue(value2)
//      val userMessage = otherDrive.expectMsgType[Msg.UserMessage[Double]]
//      userMessage.outletId shouldEqual testOutlet.pipeId
//      userMessage.inletId  shouldEqual otherInlet.pipeId
//      userMessage.value    shouldEqual value2}
//    "by UserMessage, put user message to queue and reply with DriveLoad" in new TestCase {
//      //Preparing
//      val (testInlet, testOutlet, otherInlet, otherOutlet) = connectedTools
//      val value1 = randomDouble()
//      val value2 = randomDouble()
//      //First message, reply with DriveLoad and start processing
//      otherDrive.send(testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      val driveLoad1 = otherDrive.expectMsgType[Msg.DriveLoad]
//      driveLoad1.drive        shouldEqual testDrive
//      driveLoad1.maxQueueSize shouldEqual 0
//      val runTask1 = testImpeller.expectMsgType[Msg.RunTask]
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 1
//      //Second message, reply with DriveLoad and put user message to queue
//      otherDrive.send(testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value2))
//      val driveLoad2 = otherDrive.expectMsgType[Msg.DriveLoad]
//      driveLoad2.drive        shouldEqual testDrive
//      driveLoad2.maxQueueSize shouldEqual 1
//      testImpeller.expectNoMsg()
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 2
//      //Start of processing of second message
//      testImpeller.send(testDrive, Msg.TaskDone(runTask1.name, 1.second, Unit))
//      val runTask2 = testImpeller.expectMsgType[Msg.RunTask]
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 1
//      //Done process second task
//      testImpeller.send(testDrive, Msg.TaskDone(runTask2.name, 1.second, Unit))
//      testDrive.askForState[DriveState].inlets(testInlet.pipeId)._2 shouldEqual 0}
//    "dequeue user message and call user message handling function" in new TestCase {
//      //Preparing
//      val (testInlet, testOutlet, otherInlet, otherOutlet) = connectedTools
//      val value1 = randomDouble()
//      //Send message
//      otherDrive.send(testDrive, Msg.UserMessage(otherOutlet.pipeId, testInlet.pipeId, value1))
//      otherDrive.expectMsgType[Msg.DriveLoad]
//      val runTask1 = testImpeller.expectMsgType[Msg.RunTask]
//      //Run task
//      testTool.testPipe.getReceivedValues shouldBe empty
//      runTask1.task()
//      testTool.testPipe.getReceivedValues should have size 1
//      testTool.testPipe.getReceivedValues.head shouldEqual value1}
//    "on Terminate after disconnect inlets, handle all usr messages before disconnect outlets" in new TestCase {
//
//    }
//    "by DriveLoad, evaluate message handling timeout" in new TestCase {
//
//    }
//  }
}
