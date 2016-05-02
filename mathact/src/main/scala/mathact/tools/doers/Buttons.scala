package mathact.tools.doers
import mathact.utils.ui.UIParams
import mathact.utils.{ToolHelper, Environment, Tool}
import mathact.utils.clockwork.CalculationGear
import mathact.utils.ui.components.{SingleButton, FlowFrame}


/** Button panel
  * Created by CAB on 07.02.2016.
  */

abstract class Buttons (
  name:String = "",
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue)
(implicit environment:Environment)
extends Tool{
  //Definitions
  trait Button{
    def enable():Unit
    def disable():Unit}
  private class ButtonImpl(uiParams:UIParams.SingleButton, name:String, isEnabled:Boolean) extends Button{
    //Variables
    private var onClickProc:Option[()⇒Unit] = None
    //UI
    val uiButton = new SingleButton(uiParams, name){
      def onClick() = onClickProc.foreach(_())}
    uiButton.enabled = isEnabled
    //Methods
    def enable():Unit = {uiButton.enabled = true}
    def disable():Unit = {uiButton.enabled = false}
    def setProc(proc:()⇒Unit):ButtonImpl = {
      onClickProc = Some(proc)
      this}}
  protected implicit class ExButton(button:Button){
    def click(p: ⇒Unit):Button = button.asInstanceOf[ButtonImpl].setProc(()⇒p)}
  //Helpers
  private val helper = new ToolHelper(this, name, "Buttons")
  private val uiParams = environment.params.Buttons
  //Variables
  private var buttons:List[ButtonImpl] = List()
  private var uiFrame:Option[FlowFrame] =None
  //DSL Methods
  def button(name:String = "Button " + buttons.size, enanbled:Boolean = true):Button = {
    buttons :+= new ButtonImpl(uiParams, name, enanbled)
    buttons.last}
  //Methods
  def disableAll():Unit = buttons.foreach(_.disable())
  def enableAll():Unit = buttons.foreach(_.enable())
  //Gear
  private val gear:CalculationGear = new CalculationGear(environment.clockwork, updatePriority = -1){
    def start() = {
      //Create buttons
      val frame = new FlowFrame(
        environment.layout, uiParams, helper.toolName, buttons.map(_.uiButton)){
        def closing() = gear.endWork()}
      frame.show(screenX, screenY)
      uiFrame = Some(frame)}
    def update() = {}
    def stop() =
      uiFrame.foreach(_.hide())}}
