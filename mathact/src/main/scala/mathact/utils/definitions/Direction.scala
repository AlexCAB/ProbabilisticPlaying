package mathact.utils.definitions


/**
 * Direction
 * Created by CAB on 21.09.2015.
 */

trait Direction


object Direction {
  //Definitions
  case object Up extends Direction
  case object Down extends Direction
  case object Left extends Direction
  case object Right extends Direction
  case object None extends Direction
  //Methods
  def fromString(s:String):Direction = s match{
    case "Up" ⇒ Direction.Up
    case "Down" ⇒ Direction.Down
    case "Left" ⇒ Direction.Left
    case "Right" ⇒ Direction.Right
    case "None" ⇒ Direction.None
    case _ ⇒ throw new Exception(s"Incorrect direction name '$s'")}}
