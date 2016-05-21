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

import mathact.parts.plumbing.{Fitting, Pump}
import mathact.parts.Environment
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

abstract class Workbench extends Fitting{


  //Environment должен констрироватся до того как будетсоздан хоть один инструмент (т.е. самый первый при старте программы),
  //так как Environment содержыт все служебные обьекты и сервисы (как например ActorSystem)

  protected implicit val environment = new Environment

  private[mathact] val pump: Pump = new Pump(environment, this, "WorkbenchPump")









//  stage = new PrimaryStage {
//    title = "ScalaFX Hello World"
//    scene = new Scene {
//      fill = Black
//      content = new HBox {
//        padding = Insets(20)
//        children = Seq(
//          new Text {
//            text = "Hello "
//            style = "-fx-font-size: 48pt"
//            fill = new LinearGradient(
//              endX = 0,
//              stops = Stops(PaleGreen, SeaGreen))
//          },
//          new Text {
//            text = "World!!!"
//            style = "-fx-font-size: 48pt"
//            fill = new LinearGradient(
//              endX = 0,
//              stops = Stops(Cyan, DodgerBlue)
//            )
//            effect = new DropShadow {
//              color = DodgerBlue
//              radius = 25
//              spread = 0.25
//            }
//          }
//        )
//      }
//    }
//  }








  def main(arg:Array[String]):Unit = {


    //До вызова этого метода акторы могут обмениватся только конструкционными сообщениями (NewDrive, NewImpeller)
    environment.start(arg)


  }


    //Далее: работа над UI Workbench (запуск, и пошаговое выполение приложения)

    //Нету готового решение для конкурентного интерфейса, прийдётся делать что-то своё.

    //Полезный метод: Swing.onEDT  --> http://stackoverflow.com/questions/32355872/gui-for-akka-application


    //Scala-swing заброшена, придётся использовать scalafx, нужно разобратся как создать несколько окон и интегрировать
    //с AKKA.


}