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

package mathact.tools

import mathact.Application
import mathact.parts.WorkbenchLike
import mathact.parts.model.messages.Msg
import mathact.parts.plumbing.{Fitting, Pump}
import mathact.parts.bricks.Tool
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{Stops, LinearGradient}
import scalafx.scene.text.Text


/** Box class for placing of tools
  * Created by CAB on 09.05.2016.
  */

abstract class Workbench extends WorkbenchLike{ //extends Fitting{
  //Get of WorkbenchContext
  protected implicit val context = Application.getWorkbenchContext(this)


  //!!! Workbench должен зарегистировать себя и получить свой собсвенный controller и pumping





  //Здесь нижно получить environment из сервиса Applicati, если уже зарегестироват, логировать оштбку




}


//
//  extends Fitting{
//
//
//  //Environment должен констрироватся до того как будетсоздан хоть один инструмент (т.е. самый первый при старте программы),
//  //так как Environment содержыт все служебные обьекты и сервисы (как например ActorSystem)
//
////  protected implicit val environment = new Environment
////
////  private[mathact] val pump: Pump = new Pump(environment, this, "WorkbenchPump")
////
////
////
////  def terminate(): Unit = ???      //Разрушает инструмент или соединетель, предварительно отключив все трубы
////
////
////
////
////
////
////
////
////  def main(arg:Array[String]):Unit = {
////    //Starting of main controller
////    environment.controller ! M.MainControllerStart
////
////
////     //До вызова этого метода акторы могут обмениватся только конструкционными сообщениями (NewDrive, NewImpeller)
////
////
////
////  }
//
//
//    //Далее: работа над UI Workbench (запуск, и пошаговое выполение приложения)
//
//    //Нету готового решение для конкурентного интерфейса, прийдётся делать что-то своё.
//
//    //Полезный метод: Swing.onEDT  --> http://stackoverflow.com/questions/32355872/gui-for-akka-application
//
//
//    //Scala-swing заброшена, придётся использовать scalafx, нужно разобратся как создать несколько окон и интегрировать
//    //с AKKA.
//
//
//}