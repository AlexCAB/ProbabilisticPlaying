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

package mathact.parts.model

/** Contains common enums
  * Created by CAB on 24.06.2016.
  */

package object enums {
  //SketchStatus
  object SketchStatus extends Enumeration {
    val Autorun, Ready, Ended, Failed = Value}
  type SketchStatus = SketchStatus.Value
  //StepMode
  object StepMode extends Enumeration {
    val HardSynchro, SoftSynchro, Asynchro, None = Value}
  type StepMode = StepMode.Value
  //WorkMode
  object WorkMode extends Enumeration {
    val Paused, Runned, Stopping = Value}
  type WorkMode = WorkMode.Value
  //TaskKind
  object TaskKind extends Enumeration {
    val Start, Massage, Stop, ShowUI, HideUI = Value}
  type TaskKind = TaskKind.Value
  //VisualisationLaval
  object VisualisationLaval extends Enumeration {
    val None, Basic, Load, Full = Value}
  type VisualisationLaval = VisualisationLaval.Value
  //ActorState
  object ActorState extends Enumeration {
    val Creating = Value
    val Created = Value
    val Building = Value
    val Built = Value
    val Starting = Value
    val Started = Value
    val Working = Value
    val Stopping = Value
    val Stopped = Value
    val Terminating = Value
    val Terminated = Value}
  type ActorState = ActorState.Value
  //SketchUIAction
  object SketchUIAction extends Enumeration {
    val UiShowed = Value
    val UiHided = Value
    val UiClosed = Value
    val RunBtnHit = Value
    val ShowAllToolsUiBtnHit = Value
    val HideAllToolsUiBtnHit = Value
    val SkipAllTimeoutTaskBtnHit = Value
    val StopSketchBtnHit = Value
    val LogBtnHit = Value
    val VisualisationBtnHit = Value}
  type SketchUIAction = SketchUIAction.Value

//TODO Add more

}
