package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.event.ButtonClicked
import scala.swing.{Button, GridPanel}

/** Button with name
  * Created by CAB on 07.02.2016.
  */

abstract class SingleButton (uiParams:UIParams.SingleButton, label:String, width:Option[Int] = None)
extends Button with UIComponent{
  //Constructions
  preferredSize = new Dimension(
  width.getOrElse(calcStringWidth(label, uiParams.singleButtonFont)) + 30,
  uiParams.singleButtonHeight)
  peer.setText(label)
  reactions += {case ButtonClicked(_) ⇒ onClick()}
  //Abstract methods
  def onClick()}