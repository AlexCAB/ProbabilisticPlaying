package mathact.utils.ui.components
import java.awt.Dimension
import mathact.utils.ui.UIParams
import scala.swing.Button
import scala.swing.event.ButtonClicked


/**
 * Reload button component.
 * Created by CAB on 30.10.2015.
 */
abstract class ReloadButton(uiParams:UIParams.ReloadButton) extends Button with UIComponent{
  //Constructions
  preferredSize = new Dimension(
    uiParams.reloadButtonSize,
    uiParams.reloadButtonSize)
  icon = uiParams.reloadButtonIcon
  reactions += {case ButtonClicked(_) ⇒ reload()}
  //Abstract methods
  def reload()}