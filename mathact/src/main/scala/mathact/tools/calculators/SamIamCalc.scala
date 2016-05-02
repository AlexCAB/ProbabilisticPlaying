package mathact.tools.calculators
import java.awt.{Color, Point}
import java.io.{PrintWriter, StringWriter, ByteArrayInputStream}
import java.nio.charset.StandardCharsets
import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale
import edu.ucla.belief.io.{PropertySuperintendent, NetworkIO}
import edu.ucla.belief.BeliefNetwork
import edu.ucla.belief.approx.{PropagationEngineGenerator, MessagePassingScheduler}
import edu.ucla.belief.io.hugin.HuginNode
import mathact.utils.clockwork.{CalculationGear, ExecutionException}
import mathact.utils.ui.components.{ReloadButton, SimpleGraph, TVFrame}
import mathact.utils.{ToolHelper, Tool, Environment}
import scala.collection.JavaConverters._


/** SamIam bayes net calculation and visualization tool
  * Created by CAB on 24.01.2016.
  */

abstract class SamIamCalc(
  name:String = "",
  showUI:Boolean = true,                    //ON/OFF graphical UI
  showInference:Boolean = true,             //Shove|Hide node inference values and prob
  showCPT:Boolean = false,                  //Shove|Hide node CPT
  engineTimeout:Long = 10000,
  engineMaxIterations:Int = 100,
  engineScheduler:MessagePassingScheduler = edu.ucla.belief.approx.MessagePassingScheduler.TOPDOWNBOTTUMUP,
  engineConvergenceThreshold:Double = 1.0E-7,
  screenX:Int = Int.MaxValue,
  screenY:Int = Int.MaxValue,
  screenW:Int = 800,
  screenH:Int = 300)
(implicit environment:Environment)
extends Tool{
  //Parameters
  private val thisTool = this
  //Definitions
  private class CommonDefinition{
    /** Split raw CPT to lines and columns.
      * @param columnSize - Int
      * @param table - raw CPT
      * @return - CPT Columns(Lines(Double)) */
    def splitByColumns(columnSize:Int, table:List[Double]):List[List[Double]] = {
      val sl = columnSize
      def split(ps:List[Double]):List[List[Double]] = ps match{
        case Nil ⇒ List()
        case l ⇒ ps.take(sl) +: split(ps.drop(sl))}
      split(table)}}
  /** BayesNetwork wrapper */
  private class BayesNetwork(
    net: String, //Bayes ne as String
    timeout: Long,
    maxIterations: Int,
    scheduler: MessagePassingScheduler)
  extends CommonDefinition{
    //Construction
    private val bayesNet = NetworkIO.readHuginNet(
      new ByteArrayInputStream(net.getBytes(StandardCharsets.UTF_8)))
    private val dynamator = new PropagationEngineGenerator
    //Setting
    private val settings = PropagationEngineGenerator.getSettings(bayesNet.asInstanceOf[PropertySuperintendent])
    settings.setTimeoutMillis(timeout)
    settings.setMaxIterations(maxIterations)
    settings.setScheduler(scheduler)
    settings.setConvergenceThreshold(engineConvergenceThreshold)
    //Build vertices map
    private val vertices = bayesNet
      .vertices()
      .asScala
      .flatMap{
        case v:HuginNode ⇒ Some((v.getID,v))
        case _ ⇒ None}
      .toMap
    //Methods
    def beliefNet:BeliefNetwork = bayesNet
    def getBayesNet:String = {
      val stringWriter = new StringWriter()
      val writer = new PrintWriter(stringWriter)
      NetworkIO.writeNetwork(bayesNet, writer)
      stringWriter.toString}
    /** Extract list of node ID presented in the network
      * @return - List(node ID) */
    def getIds:List[String] = vertices.keys.toList
    /** Lade CPTs to BayesNetwork
      * @param cptMap - Map(node ID, Prop tauNCptTable in format Lines(Columns(Value))) */
    def setCPTs(cptMap: Map[String, List[List[Double]]]): Unit = {
      cptMap.foreach{ case (nodeId, table) ⇒
        vertices.get(nodeId) match{
          case Some(vertex) ⇒
            //Set CPT tauNCptTable to vertex
            val array = table.flatMap(e ⇒ e).toArray
            vertex.getCPTShell.getCPT.setValues(array)
          case _ ⇒
            //Error
            throw new Exception(s"[BayesNetwork.replaceCPTs] No node in bayes net with ID: $nodeId")}}}
    /** Get CPT for particulat node
      * @param nodeId - String
      * @return - CPT Columns(Lines(Double)) */
    def getCPT(nodeId: String): List[List[Double]] = {
      vertices.get(nodeId) match{
        case Some(vertex) ⇒
          //Get CPT tauNCptTable from vertex
          splitByColumns(
            vertex.instances().size(),
            vertex.getCPTShell.getCPT.dataclone().toList)
        case _ ⇒
          //Error
          throw new Exception(s"[BayesNetwork.getCPT] No node in bayes net with ID: $nodeId")}}
    /** Set evidences
      * @param evidences - Map(node ID, value name */
    def setEvidences(evidences: Map[String,String]): Unit = {
      //Check of evidences
      val prepEvidence  = evidences.flatMap{
        case (_, value) if value == "none" ⇒ None
        case (nodeID, value) if vertices.contains(nodeID) ⇒
          val n = vertices(nodeID)
          val vs = n.instances.asInstanceOf[java.util.List[String]].asScala
          value match{
            case v if n.instances.asInstanceOf[java.util.List[String]].asScala.contains(v) ⇒ Some((n, value))
            case v ⇒ throw new ExecutionException(
              s"[BayesNetwork.setEvidences] Error: Incorrect evidence value '$v' for node '$nodeID'.")}
        case (nodeID, _) ⇒ throw new ExecutionException(
          s"[BayesNetwork.setEvidences] Error: Incorrect node name '$nodeID'.")}
      //Set evidences
      bayesNet.getEvidenceController.setObservations(prepEvidence.asJava)}
    /** Evaluate of bayes net
      * @return - inferences: Map(node ID, Map(value name, probability)) */
    def evaluate(): Map[String, Map[String, Double]] = {
      val engine = dynamator.manufactureInferenceEngine(bayesNet)
      val variables = vertices.map{case (nodeID,v) ⇒
        val ls = v.instances.asInstanceOf[java.util.List[String]].asScala.toList
        val ps = engine.conditional(v).dataclone().toList
        (v.getID, ls.zip(ps))}
      variables.map{case(k, m) ⇒ (k, m.toMap)}}}
  /** Contain of component UI */
  private class GraphUI (onReloadFun:()⇒Unit, onClosingFun:()⇒Unit) extends CommonDefinition {
    //Helpers
    private val helper = new ToolHelper(thisTool, name, "SamIamCalc")
    private val uiParams = environment.params.SamIamCalc
    private val decimal = NumberFormat.getNumberInstance(Locale.ENGLISH).asInstanceOf[DecimalFormat]
    decimal.applyPattern(uiParams.numberFormat)
    //UI components
    private val uiGraph = new SimpleGraph(uiParams, screenW, screenH, false, true)
    private val uiReload = new ReloadButton(uiParams){
      def reload() = {onReloadFun()}}
    private val uiFrame:TVFrame = new TVFrame(
      environment.layout, uiParams, helper.toolName, center = Some(uiGraph), bottom = List(uiReload)){
      def closing() = {onClosingFun()}}
    //Show frame
    uiFrame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)
    //Functions
    private def mixColors(a:Color, b:Color, p:Double):Color = {
      val List(cr,cg,cb) = List(a.getRed, a.getGreen, a.getBlue).zip(List(b.getRed, b.getGreen, b.getBlue))
        .map{case (ac, bc) ⇒ (ac * p + bc * (1 - p)).toInt}
        .map{case c if c > 255 ⇒ 255; case c if c < 0 ⇒ 0; case c ⇒ c}
      new Color(cr,cg,cb)}
    //Methods
    /** Redraw network graph
      * @param net - BayesNetwork
      * @param evidences - List(node ID)
      * @param inference - Map(node ID, Map(value name, probability)) */
    def updateGraph(net:BayesNetwork, evidences:List[String], inference:Map[String, Map[String, Double]]):Unit = {
      //Clear old
      uiGraph.clear()
      //Build
      val nodes = net.beliefNet.vertices().toArray.flatMap{
        case huginNode:HuginNode ⇒
          //Get node data
          val nodeID = huginNode.getID
          val verProbs = huginNode.getProperties
          val List(x, y) = verProbs.get("position").asInstanceOf[java.util.ArrayList[Int]].asScala.toList
          val label = verProbs.get("label").toString
          //Node color
          val color = evidences.contains(nodeID) match{
            case true ⇒
              uiParams.evidenceNodeColor
            case false ⇒
              inference.get(nodeID) match{
                case Some(probs) ⇒
                  val (value, prob) = probs.maxBy(_._2)
                  (nodeID, probs) match{
                    case (id,ps) if probs.size == 2 ⇒
                      mixColors(uiParams.activeNodeColor, uiParams.passiveNodeColor, probs.head._2)
                    case (id,ps) ⇒ mixColors(
                      uiParams.activeNodeColor,
                      uiParams.passiveNodeColor,
                      probs.toList.indexOf((value, prob)).toDouble / probs.size)}
                case _ ⇒
                  uiParams.defaultNodeColor}}
          //Inference
          val infVars = showInference match{
            case true ⇒ inference.get(nodeID).flatMap{ case probs ⇒
              (probs.map{case (v,p) ⇒ s"$v=${decimal.format(p)}"}.mkString(", "), showCPT) match{
                case (str, true) ⇒ Some(("", s"[$str]"))
                case (str, false) ⇒ Some(("", str))}}
            case _ ⇒ None}
          //CPD
          val cptVars = showCPT match{
            case true ⇒ inference.get(nodeID).flatMap{ case probs ⇒
              val cpt = splitByColumns(
                probs.size,
                net.beliefNet.forID(nodeID).asInstanceOf[HuginNode].getCPTShell.getCPT.dataclone.toList)
              Some(("", s"(${cpt.map(c ⇒ s"(${c.map(v ⇒ decimal.format(v)).mkString(", ")})").mkString(",")}})"))}
            case _ ⇒ None}
          //Update graph
          val gVars = List(infVars, cptVars).flatMap(e ⇒ e) match{case Nil ⇒ None; case l ⇒ Some(l)}
          uiGraph.addNode(
            nodeID,
            Some(label),
            Some(color),
            None,
            Some(new Point(x, y * -1)),
            gVars)
          Some((nodeID, huginNode))
        case _ ⇒ None}
      nodes.foreach{case (nodeID, node) ⇒
        net.beliefNet.inComing(node).asInstanceOf[java.util.Set[HuginNode]].asScala.foreach(vo ⇒ {
          uiGraph.addEdge(
            nodeID + vo.getID,
            vo.getID, nodeID,
            idDirected = true,
            None,
            Some(uiParams.defaultNodeColor),
            None,
            None)})}}
    /** Hide UI */
    def hide() = {
      uiFrame.hide()}}
  //Variables
  private var beliefNet: Option[BayesNetwork] = None
  private var cptMap: Map[String, List[List[Double]]] = Map()       //Map(node ID, Prop tauNCptTable in format Lines(Columns(Value)))
  private var evidences:  Map[String,String]  = Map()               //Map(node ID, value name)
  private var inferences: Map[String, Map[String, Double]] = Map()  //Map(node ID, Map(value name, probability))
  private var graphUI: Option[GraphUI] = None
  private var onReloadProc:Option[()⇒Unit] = None                     //Calling by hit on reload button
  //DSL
  protected def onReload(proc: ⇒Unit): Unit = {onReloadProc = Some(()⇒proc)}
  //Methods
  /** Clear network data */
  def clear(): Unit = {
    beliefNet = None
    cptMap = Map()
    evidences = Map()
    inferences = Map()}
  /** Set bayes net
    * @param stringNet - *.net file as String */
  def setNet(stringNet: String): Unit = {
    val bayesNet = new BayesNetwork(stringNet, engineTimeout, engineMaxIterations, engineScheduler)
    bayesNet.setCPTs(cptMap)
    bayesNet.setEvidences(evidences)
    this.beliefNet = Some(bayesNet)
    inferences = Map()}
  /** Get of SamIam .net file as String
    * @return - converted SamIam net as String*/
  def getSamIamNet:String = beliefNet match{
    case Some(net) ⇒
      net.getBayesNet
    case _ ⇒
      throw new Exception("[SamIamCalc.getSamIamNet] No bayes net setup.")}
  /** Set of net CPTs (replace CPT from net)
    * @param cptMap - Map(node ID, Prop tauNCptTable in format Lines(Columns(Value))) */
  def replaceCPTs(cptMap: Map[String, List[List[Double]]]): Unit = {
    this.cptMap = cptMap
    beliefNet.foreach(net ⇒ net.setCPTs(cptMap))}
  /** Get current CPTs
    * @return - Map(node ID, Prop tauNCptTable in format Lines(Columns(Value))) */
  def getReplacedCPTs :Map[String, List[List[Double]]] = this.cptMap
  /** Get of CPT of particular node.
    * @return - List() */
  def getListOfNodeIds: List[String] = {
    beliefNet match{
      case Some(net) ⇒
        net.getIds
      case _ ⇒
        throw new Exception("[SamIamCalc.getNodeCPT] No bayes net setup.")}}
  /** Get of CPT of particular node.
    * @param nodeId - String
    * @return - CPT */
  def getNodeCPT(nodeId: String): List[List[Double]] = {
    beliefNet match{
      case Some(net) ⇒
        net.getCPT(nodeId)
      case _ ⇒
        throw new Exception("[SamIamCalc.getNodeCPT] No bayes net setup.")}}
  /** Replace of particular node CPT.
    * @param nodeId - String
    * @param cpt - List(List(Double)) */
  def replaceNodeCPT(nodeId :String, cpt: List[List[Double]]): Unit = {
    this.cptMap += (nodeId → cpt)
    beliefNet.foreach(net ⇒ net.setCPTs(cptMap))}
  /** Set of net evidence
    * @param evidences - Map(node ID, value name) */
  def setEvidences(evidences: Map[String,String]): Unit = {
    this.evidences = evidences
    beliefNet.foreach(net ⇒ net.setEvidences(evidences))}
  /** Get current evidences
    * @return - Map(node ID, value name) */
  def getEvidences:Map[String,String] = this.evidences
  /** Adding given evidences
    * @param evidences - Map(node ID, value name) */
  def addEvidences(evidences: Map[String,String]):Unit = {
    this.evidences ++= evidences
    beliefNet.foreach(net ⇒ net.setEvidences(this.evidences))}
  /** Subtract evidences for given nodes set.
    * @param nodeIds - Set(node id) */
  def subEvidences(nodeIds: Set[String]):Unit = {
    this.evidences = this.evidences.filterKeys(k ⇒ ! nodeIds.contains(k))
    beliefNet.foreach(net ⇒ net.setEvidences(this.evidences))}
  /** Evaluate of net
    * @return - inferences: Map(node ID, Map(value name, probability)) */
  def evaluate(): Map[String, Map[String, Double]] = {
    inferences = beliefNet.map(net ⇒ net.evaluate()).getOrElse(Map())
    inferences}
  /** Update network state in UI if showUI == true */
  def show(): Unit = {
    graphUI.foreach(gu ⇒ beliefNet.foreach(bn ⇒ gu.updateGraph(bn, evidences.keys.toList, inferences)))}
  //Gear
  private val gear:CalculationGear = new CalculationGear(environment.clockwork, updatePriority = 1){
    def start() = {
      //Build UI
      graphUI = showUI match{
        case true ⇒
          Some(new GraphUI(
            onReloadFun = () ⇒ {
              onReloadProc.foreach(_ ())},
            onClosingFun = () ⇒ {
              gear.endWork()}))
      case _ ⇒
        None}}
    def update() = {}
    def stop() = {
      graphUI.foreach{_.hide()}}}}