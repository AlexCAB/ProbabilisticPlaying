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

package mathact.parts.gui

import scalafx.application.Platform


/** Set of helpers to interact with Java FX app.
  * Created by CAB on 22.05.2016.
  */

trait JFXInteraction {
  //Parameters
  val runBlockTimeout: Int = 10000  //In milliseconds
  //Methods
  /** Run block and return without wait
    * @param block - block with code to run in Java FX thread. */
  def runLater(block: ⇒ Unit): Unit =
    Platform.runLater{block}
  /** Run block and wait for execution
    * @param block - block with code to run in Java FX thread. */
  def runAndWait(block: ⇒ Unit): Unit = {
    //Variables
    @volatile var isDome = false
    var count = runBlockTimeout
    //Run block
    Platform.runLater{
      block
      isDome = true}
    //Wait for result
    while (! isDome && count > 0){
      Thread.sleep(1)
      count -= 1}
    //Check of result
    isDome match{
      case false ⇒ throw new IllegalStateException(
        s"[JFXInteraction.runAndWait] Block not executed in $runBlockTimeout milliseconds.")
      case true ⇒}}
  /** Run block, wait end return result
    * @param block - block with code to run in Java FX thread.
    * @tparam T - type of lock return value
    * @return - value of type T returned from block */
  def runNow[T](block: ⇒ T): T = {
    //Variables
    @volatile var result: Option[T] = None
    var count = runBlockTimeout
    //Run block
    Platform.runLater{ result = Some(block) }
    //Wait for result
    while (result.isEmpty && count > 0){
      Thread.sleep(1)
      count -= 1}
    //Check of result
    result.isEmpty match{
      case true ⇒ throw new IllegalStateException(
        s"[JFXInteraction.runNow] Block not executed in $runBlockTimeout milliseconds.")
      case false ⇒ result.get}}}

