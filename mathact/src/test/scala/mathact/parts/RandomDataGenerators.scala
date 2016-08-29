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

import java.util.UUID

import mathact.parts.data.TaskKind


/** Random data generator methods
  * Created by CAB on 13.08.2016.
  */

trait RandomDataGenerators {
  def randomBoolean(): Boolean = math.random > .5
  def randomOpt[T](v: T): Option[T] = randomBoolean() match{case true ⇒ Some(v); case _ ⇒ None}
  def randomInt(from:Int = 0, to:Int = 1000): Int = from + (math.random * (to - from + 1)).toInt
  def randomDouble(from:Double = 0, to:Double = 100): Double = from + math.random * (to - from)
  def randomString(n:Int = 32): String = UUID.randomUUID().toString.reverse.take(n)
  def randomTaskKind(): TaskKind = TaskKind(randomInt(0, TaskKind.maxId - 1))

  //TODO Add more

}
