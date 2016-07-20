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


/** Contains common enums
  * Created by CAB on 24.06.2016.
  */

package object data {
  //SketchStatus
  object SketchStatus extends Enumeration {val Autorun, Ready, Ended, Failed = Value}
  type SketchStatus = SketchStatus.Value
  //StepMode
  object StepMode extends Enumeration {val HardSynchro, SoftSynchro, Asynchro, None = Value}
  type StepMode = StepMode.Value
  //WorkMode
  object WorkMode extends Enumeration {val Paused, Runned, Stopping = Value}
  type WorkMode = WorkMode.Value

//TODO Add more

}
