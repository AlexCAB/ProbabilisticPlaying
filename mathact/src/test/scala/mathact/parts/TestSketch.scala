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

package mathact.parts

import scala.concurrent.duration.Duration

/** Sketch for using in tests
  * Created by CAB on 04.09.2016.
  */

class TestSketch{
  println("[WorkbenchControllerTest.TestSketch] Creating.")
  TestSketch.instanceCreated()
  TestSketch.getProcTimeout.foreach(d ⇒ Thread.sleep(d.toMillis))
  TestSketch.getProcError.foreach(e ⇒ throw e)}


object TestSketch{
  //Variables
  @volatile private var isCreated = false
  @volatile private var procTimeout: Option[Duration] = None
  @volatile private var procError: Option[Throwable] = None
  //Static methods
  def instanceCreated(): Unit = { isCreated = true }
  def getProcTimeout: Option[Duration] = procTimeout
  def getProcError: Option[Throwable] = procError
  def isInstanceCreated: Boolean = isCreated
  def setProcTimeout(d: Duration): Unit = synchronized{ procTimeout = Some(d) }
  def setProcError(err: Option[Throwable]): Unit = synchronized{ procError = err }}
