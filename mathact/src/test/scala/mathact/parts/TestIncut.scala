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

import mathact.parts.plumbing.fitting.{Inlet, Outlet}


/** Pipe used in tests
  * Created by CAB on 19.08.2016.
  */

class TestIncut[T] extends Outlet[T] with Inlet[T]{
  //Variables
  private var receivedValues = List[T]()
  //Receive user message
  protected def drain(value: T): Unit = synchronized{ receivedValues +:= value }
  //Test methods
  override def toString = s"TestIncut(receivedValues.size: ${receivedValues.size})"
  def getReceivedValues: List[T] = synchronized{ receivedValues }
  def sendValue(value: T): Unit = pour(value)}