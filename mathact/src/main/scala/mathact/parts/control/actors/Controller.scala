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

package mathact.parts.control.actors

import javafx.stage.{Stage => jStage}

import akka.actor.{PoisonPill, ActorRef, Actor}
import akka.event.Logging
import mathact.parts.control.{ControlActor, CtrlEvents}
import mathact.parts.gui.MainWindow
import mathact.parts.gui.frame.Frame
import mathact.parts.plumbing.PumpEvents

import scala.util.Try
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{LinearGradient, Stops}
import scalafx.scene.text.Text
import scalafx.stage.Stage

/** Main application controller
  * Created by CAB on 21.05.2016.
  */

class Controller(pumping: ActorRef, doStop: Int⇒Unit) extends ControlActor{
  //Objects
  val log = Logging.getLogger(context.system, this)
  val frame = new MainWindow(log){
    def doStop(): Unit = {self ! CtrlEvents.DoStop}
    def hitRun(): Unit = {???}
    def hitStop(): Unit = {???}
    def hitStep(): Unit = {???}
    def setSpeed(value: Double) = {???}
    def switchMode(newMode: Int) = {???}}
  //Variables
  var exitCode = 0
  //Functions
  def doTerminate(exitCode: Int): Unit = {
    this.exitCode = exitCode
    self ! PoisonPill}
  //Messages handling
  def receive = {

    case CtrlEvents.DoStart ⇒


      //Далее здесь должна выполнятся создание UI и иницализация инсрументов. И вынести MainWindowStage в наружу.
      //Нужно найти способ как просто взаимодействаовать с Stage-им.

      println("[Controller] Receive: DoStart")

//      Platform.runLater{
//
//        val w = new MainWindowStage
//
//        w.show()
//
//      }

      tryToRun{frame.init()}.getOrElse{doTerminate(exitCode = -1)}




    case CtrlEvents.DoStop ⇒

      println("[Controller] Receive: DoStop")

      //Здесь остановка насосв, вызов процедур завершения инструментов и выход

      doTerminate(exitCode = 0)



    case x ⇒ println("[Controller] Receive: " + x)
  }




//  //Variables
//  private var workbenchStage: Option[WorkbenchStage] = None
//  //Methods
//  def init(primaryStage: jStage): Unit = {
//    //Create workbench stage
//    workbenchStage = Some(new WorkbenchStage(primaryStage))
//
//
//
//
//  }

  //On stop
  override def postStop(): Unit = {
    log.info(s"[Controller.postStop] Call doStop with exit code: $exitCode")
    doStop(exitCode)}}
