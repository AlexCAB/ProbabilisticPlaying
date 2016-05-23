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
import mathact.parts.control.CtrlEvents
import mathact.parts.gui.MainWindow
import mathact.parts.gui.frame.Frame
import mathact.parts.plumbing.PumpEvents

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

class Controller(pumping: ActorRef, doStop: Int⇒Unit) extends Actor{
  //Objects
  private val log = Logging.getLogger(context.system, this)
  private val frame = new MainWindow(log, self){}


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

      frame.init()



    case CtrlEvents.DoStop ⇒

      println("[Controller] Receive: DoStop")

      //Здесь остановка насосв, вызов процедур завершения инструментов и выход

      self ! PoisonPill



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
  override def postStop(): Unit = doStop(1)}  //Exit code 1
