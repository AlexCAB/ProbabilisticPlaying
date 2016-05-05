package mathact.tools.calculators
import java.awt.Point
import java.io.{ByteArrayInputStream, File}
import java.nio.charset.StandardCharsets
import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale
import edu.ucla.belief.io.hugin.HuginNode
import edu.ucla.belief.{FiniteVariable, BeliefNetwork}
import edu.ucla.belief.io.{PropertySuperintendent, NetworkIO}
import mathact.utils.clockwork.{CalculationGear, ExecutionException}
import mathact.utils.dsl.SyntaxException
import mathact.utils.ui.components.{ReloadButton, SimpleGraph, ResetButton, TVFrame}
import scala.language.implicitConversions
import edu.ucla.belief.approx.{MessagePassingScheduler, PropagationEngineGenerator}
import mathact.utils.{ToolHelper, Tool, Environment}
import scala.collection.JavaConverters._
import scala.swing.Color


/**
 * Wrapper for SamIam (http://reasoning.cs.ucla.edu/samiam/index.php) bayes net inference engine.
 * Created by CAB on 12.09.2015.
 */

abstract class SamIamBayesNet(
  netPath:String,
  name:String = "",
  showUI:Boolean = true,                    //ON/OFF graphical UI
  showInference:Boolean = true,             //Shove|Hide node inference values and prob
  showCPT:Boolean = false,                  //Shove|Hide node CPT
  autoUpdate:Boolean = true,
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
  //Private definition
  private case class CPTData(node:HuginNode, linesNames:List[String], columnsIndexes:Map[List[String],Int])
  private trait Node
  private trait NoneVar extends Node{
    val nodeID:String
    var node:Option[CPTData] = None}
  private trait Prop extends Node
  private case class BinTableProb(
    nodeID:String, v:()⇒Double) extends Prop with NoneVar
  private case class TableProb(
    nodeID:String, vs:Seq[()⇒Double]) extends Prop with NoneVar
  private case class BinColumnProb(
    nodeID:String, colName:List[String], v:()⇒Double) extends Prop with NoneVar
  private case class ColumnProb(
    nodeID:String, colName:List[String], vs:Seq[()⇒Double]) extends Prop with NoneVar
  private trait Evidence extends Node
  private case class NodeEvidence(
    nodeID:String, v:()⇒String) extends Evidence with NoneVar
  private trait Inference extends Node
  private case class AllInference(
    p:(Map[String, Map[String, Double]])⇒Unit) extends Inference
  private case class AllValuesInference(
    p:(Map[String, String])⇒Unit) extends Inference
  private case class BinaryInference(
    nodeID:String, p:Double⇒Unit) extends Inference with NoneVar
  private case class AllProbInference(
    nodeID:String, p:Map[String,Double]⇒Unit) extends Inference with NoneVar
  private case class ValueInference(
    nodeID:String, p:String⇒Unit) extends Inference with NoneVar
  private case class ValueProbInference(
    nodeID:String, valueName:String, p:Double⇒Unit) extends Inference with NoneVar
  //Helpers
  private val helper = new ToolHelper(this, name + s" - [$netPath]", netPath.replace("\\","/").split("/").last)
  private val dynamator = new PropagationEngineGenerator
  private val uiParams = environment.params.SamIamBayesNet
  private val decimal = NumberFormat.getNumberInstance(Locale.ENGLISH).asInstanceOf[DecimalFormat]
  decimal.applyPattern(uiParams.numberFormat)
  //Variables
  private var probabilities = List[Prop]()
  private var evidences = List[Evidence]()
  private var inferences = List[Inference]()
  private var beliefNet:Option[BeliefNetwork] = None
  private var ui:Option[(TVFrame, SimpleGraph)] = None
  private var onLoadFun:List[()⇒Unit] = List()
  private var onReloadFun:Option[()⇒Option[String]] = None
  //Functions
  private def getAndCheckNode(net:BeliefNetwork, node:Node with NoneVar):CPTData = {
    //Get date
    val id = node.nodeID
    val vertices = net.vertices().toArray.flatMap{case n:HuginNode ⇒ Some((n.getID, n)); case _ ⇒ None}.toMap
    if(! vertices.contains(id)){
      throw new SyntaxException(s"Error: In '$netPath' not found node with ID '$id', exist: ${vertices.keySet}")}
    val vertex = vertices(id)
    val cptTable = vertex.getCPTShell.getCPT
    val linesNames = vertex.instances.asInstanceOf[java.util.List[String]].asScala.toList
    val instNames = cptTable.variables.asInstanceOf[java.util.List[FiniteVariable]].asScala.toList.dropRight(1)
      .map(_.instances.asInstanceOf[java.util.List[String]].asScala.toList)
    def buildIndexes(il:List[List[String]]):List[List[String]] = il match{
      case h :: t if t.nonEmpty ⇒  h.flatMap(e ⇒ buildIndexes(t).map(l ⇒ e +: l))
      case h :: Nil ⇒ h.map(e ⇒ List(e))
      case Nil ⇒ List()}
    val columnsIndexes = buildIndexes(instNames).zipWithIndex.toMap
    //Check definitions
    def incorrectDef(msg:String):SyntaxException =
      new SyntaxException(s"Error, for node with id ='$id': $msg.")
    node match {
      case BinTableProb(_,v) ⇒ {
        if(cptTable.getCPLength != 2){
          throw incorrectDef(s"CPT is not a binary (length(${cptTable.getCPLength}) != 2)")}}
      case TableProb( _,vs) ⇒ {
        if(cptTable.getCPLength != vs.size){
          throw incorrectDef(s"CPT size not equal to tauNCptTable size (${cptTable.getCPLength} != ${vs.size}})")}}
      case BinColumnProb( _, colName, v) ⇒ {
        if(! columnsIndexes.contains(colName)){
          throw incorrectDef(s"Unknown column name '$colName', available: ${columnsIndexes.keySet}")}
        if(linesNames.size != 2){
          throw incorrectDef(s"Column $colName not binary(${linesNames.size} != 2)")}}
      case ColumnProb(_, colName, vs) ⇒ {
        if(! columnsIndexes.contains(colName)){
          throw incorrectDef(s"Unknown column name '$colName', available: ${columnsIndexes.keySet}")}
        if(linesNames.size != vs.size){
          throw incorrectDef(s"Column '$colName' not match number of cell (${linesNames.size} != ${vs.size}})")}}
      case BinaryInference(_, p) ⇒ {
        if(linesNames.size != 2){
          throw incorrectDef(s"NodeData not binary(${linesNames.size} != 2)")}}
      case ValueProbInference(_, valueName, p) ⇒ {
        if(! linesNames.contains(valueName)){
          throw incorrectDef(s"NodeData  have no value '$valueName', available: $linesNames")}}
      case AllProbInference(_, p) ⇒
      case ValueInference(_, p) ⇒
      case NodeEvidence(_, v) ⇒
      case _:AllInference ⇒
      case _:AllValuesInference ⇒}
    //Return data
    CPTData(vertex, linesNames, columnsIndexes)}
  private def loadNet(net:Option[String]):Unit = {  //Load from string if Some, or from file if None
    //Run user proc before load
    onLoadFun.foreach(_())
    //Load net
    val bayesNet = net match{
      case Some(n) ⇒
        val is = new ByteArrayInputStream(n.getBytes(StandardCharsets.UTF_8));
        NetworkIO.readHuginNet(is)
      case _ ⇒
        val file = new File(netPath)
        if((! file.exists()) || file.isDirectory){
          throw new SyntaxException(s"Error: File '$file' not exist or is directory.")}
        NetworkIO.read(file)}
    //Setting
    val settings = PropagationEngineGenerator.getSettings(bayesNet.asInstanceOf[PropertySuperintendent])
    settings.setTimeoutMillis(engineTimeout)
    settings.setMaxIterations(engineMaxIterations)
    settings.setScheduler(engineScheduler)
    settings.setConvergenceThreshold(engineConvergenceThreshold)
    beliefNet = Some(bayesNet)
    //Map nodes to Prop probabilities, evidences, inferences
    probabilities = probabilities.map{case n:NoneVar  ⇒ {n.node = Some(getAndCheckNode(bayesNet, n)); n}; case e ⇒ e}
    evidences = evidences.map{case n:NoneVar ⇒ {n.node = Some(getAndCheckNode(bayesNet, n)); n}; case e ⇒ e}
    inferences = inferences.map{case n:NoneVar ⇒ {n.node = Some(getAndCheckNode(bayesNet, n)); n}; case e ⇒ e}}
  private def buildGraph(graph:SimpleGraph, net:BeliefNetwork):Unit = {
    //Clear old
    graph.clear()
    //Build
    val nodes = net.vertices().toArray.flatMap{
      case v:HuginNode ⇒ {
        val id = v.getID
        val probs = v.getProperties
        val List(x, y) = probs.get("position").asInstanceOf[java.util.ArrayList[Int]].asScala.toList
        val l = probs.get("label").toString
        graph.addNode(id, Some(l), None, None, Some(new Point(x, y * -1)), None)
        Some((id, v))}
      case _ ⇒ None}
    nodes.foreach{case (id, node) ⇒ {
      net.inComing(node).asInstanceOf[java.util.Set[HuginNode]].asScala.foreach(vo ⇒ {
        graph.addEdge(id + vo.getID, vo.getID, id, true, None, None, None, None)})}}}
  private def reload(net:Option[String]):Unit = {
    //Load
    loadNet(net)
    //Build UI
    (ui,beliefNet) match{
      case (Some((frame,graph)),Some(net)) ⇒ {
        buildGraph(graph,net)
        doCalc(Map())}
      case _ ⇒ }}
  private def mixColors(a:Color, b:Color, p:Double):Color = {
    val List(cr,cg,cb) = List(a.getRed, a.getGreen, a.getBlue).zip(List(b.getRed, b.getGreen, b.getBlue))
      .map{case (ac, bc) ⇒ (ac * p + bc * (1 - p)).toInt}
      .map{case c if c > 255 ⇒ 255; case c if c < 0 ⇒ 0; case c ⇒ c}
    new Color(cr,cg,cb)}
  private def splitByColumns(columnSize:Int, table:List[Double]):List[List[Double]] = {
    val sl = columnSize
    def split(ps:List[Double]):List[List[Double]] = ps match{
      case Nil ⇒ List()
      case l ⇒ ps.take(sl) +: split(ps.drop(sl))}
    split(table)}
  private def updateUI(
    net:BeliefNetwork,
    evds:List[String],
    vars:Map[String, List[(String, Double)]])
  :Unit = ui.foreach{ case(_, graph) ⇒ vars.foreach{ case (nodeID, probs) ⇒
    //Value
    val (value, prob) = probs.maxBy(_._2)
    //Color node
    val color = (nodeID, probs) match{
      case (id,ps) if evds.contains(id) ⇒ uiParams.evidenceNodeColor
      case (id,ps) if probs.size == 2 ⇒ mixColors(uiParams.activeNodeColor, uiParams.passiveNodeColor, probs.head._2)
      case (id,ps) ⇒ mixColors(
        uiParams.activeNodeColor,
        uiParams.passiveNodeColor,
        probs.indexOf((value, prob)).toDouble / probs.size)}
    //Inference
    val infVars = showInference match{
      case true ⇒ {
        (probs.map{case (v,p) ⇒ s"$v=${decimal.format(p)}"}.mkString(", "), showCPT) match{
          case (str, true) ⇒ Some(("", s"[$str]"))
          case (str, false) ⇒ Some(("", str))}}
      case _ ⇒ None}
    //CPD
    val cptVars = showCPT match{
      case true ⇒ {
        val cpt = splitByColumns(
          probs.size,
          net.forID(nodeID).asInstanceOf[HuginNode].getCPTShell.getCPT.dataclone.toList)
        Some(("", s"(${cpt.map(c ⇒ s"(${c.map(v ⇒ decimal.format(v)).mkString(", ")})").mkString(",")}})"))}
      case _ ⇒ None}
    //Update graph
    val gVars = List(infVars, cptVars).flatMap(e ⇒ e) match{case Nil ⇒ None; case l ⇒ Some(l)}
    graph.updateNode(nodeID, None, Some(color), None, None, gVars)}}
  private def doCalc(evidence:Map[String,String]):Map[String, Map[String, Double]] = beliefNet match{
    case Some(net) ⇒ {
      //Functions
      def checkOutOfBonds(p:Double, nodeID:String):Unit = if(p > 1 || p < 0){
        throw new ExecutionException(s"Error: Prob value '$p' for node '$nodeID' is out of bounds 1 >= v >= 0.")}
      def checkOutOfSun(ps:List[List[Double]], nodeID:String):Unit = ps.zipWithIndex.foreach{
        case (c,i) if (c.sum - 1.0).abs > 0.000000001 ⇒
          throw new ExecutionException(s"Error: Sum of column '$i' in node '$nodeID' not equals 1.")
        case _ ⇒}
      def normColumn(ps:List[Double]):List[Double] = ps.sum match {
        case 0.0 ⇒ ps.map(_ ⇒ 0.0)
        case s ⇒ ps.map(e ⇒ e / s)}
      def normTable(n:Int, ps:List[Double]):List[List[Double]] = { //Return columns
        splitByColumns(ps.size / n, ps).transpose.map(c ⇒ normColumn(c))}
      def updateColumn(v:HuginNode, n:Int, ci:Int, data:Array[Double]):Unit = {
        val cpd = v.getCPTShell.getCPT
        (n * ci until n * ci + data.length).zip(data).foreach{case (i, d) ⇒ cpd.setCP(i,d)}}
      //Get and set probabilities
      val vertices = net.vertices().toArray.flatMap{case v:HuginNode ⇒ Some((v.getID,v)); case _ ⇒ None}.toMap
      probabilities.foreach{
        case n:BinTableProb if n.node.nonEmpty ⇒ {
          val CPTData(v, _, _) = n.node.get
          val p = n.v()
          checkOutOfBonds(p, n.nodeID)
          v.getCPTShell.getCPT.setValues(Array(p, 1.0 - p))}
        case n:TableProb if n.node.nonEmpty ⇒ {
          val CPTData(v, ls, _) = n.node.get
          val ps = n.vs.map(_()).toList
          ps.foreach(p ⇒ checkOutOfBonds(p, n.nodeID))
          val nps = normTable(ls.size, ps)
          checkOutOfSun(nps, n.nodeID)
          v.getCPTShell.getCPT.setValues(nps.flatMap(e ⇒ e).toArray)}
        case n:BinColumnProb if n.node.nonEmpty ⇒ {
          val CPTData(v, ls, cs) = n.node.get
          val p = n.v()
          checkOutOfBonds(p, n.nodeID)
          val ci = cs(n.colName)
          updateColumn(v, ls.size, ci, Array(p, 1.0 - p))}
        case n:ColumnProb if n.node.nonEmpty ⇒ {
          val CPTData(v, ls, cs) = n.node.get
          val ps = n.vs.map(_()).toList
          ps.foreach(p ⇒ checkOutOfBonds(p, n.nodeID))
          val nps = normColumn(ps)
          checkOutOfSun(List(nps), n.nodeID)
          val ci = cs(n.colName)
          updateColumn(v, ls.size, ci, nps.toArray)}
        case _ ⇒}
      //Get evidence
      val intEvds = evidences.flatMap{
        case  n:NodeEvidence if n.node.nonEmpty ⇒ {
          val CPTData(v, ls, cs) = n.node.get
            n.v() match{
             case "none" ⇒ None
             case s if ls.contains(s) ⇒ Some((v,s))
             case s ⇒ throw new ExecutionException(s"Error: Incorrect evidence value '$s' for node '${n.nodeID}'.")}}
        case _ ⇒ None}.toMap
      val extEvds = evidence.flatMap{
        case (_, value) if value == "none" ⇒ None
        case (nodeID, value) if vertices.contains(nodeID) ⇒ {
          val n = vertices(nodeID)
          val vs = n.instances.asInstanceOf[java.util.List[String]].asScala
          value match{
            case v if n.instances.asInstanceOf[java.util.List[String]].asScala.contains(v) ⇒ Some((n, value))
            case v ⇒ throw new ExecutionException(s"Error: Incorrect evidence value '$v' for node '${nodeID}'.")}}
        case (nodeID, _) ⇒ throw new ExecutionException(s"Error: Incorrect node name '$nodeID'.")}
      //Calc
      val engine = dynamator.manufactureInferenceEngine(net)
      net.getEvidenceController.setObservations((intEvds ++ extEvds).asJava)
      val vars = vertices.map{case (nodeID,v) ⇒
        val ls = v.instances.asInstanceOf[java.util.List[String]].asScala.toList
        val ps = engine.conditional(v).dataclone().toList
        (v.getID, ls.zip(ps))}.toMap
      val varsMap = vars.map{case(k, m) ⇒ (k, m.toMap)}
      //Call inferences
      inferences.foreach{
        case BinaryInference(id, p) ⇒ p(vars(id).head._2)
        case ValueProbInference(id, valueName, p) ⇒ p(varsMap(id)(valueName))
        case AllProbInference(id, p) ⇒ p(varsMap(id))
        case ValueInference(id, p) ⇒ p(varsMap(id).maxBy(_._2)._1)
        case AllInference(p) ⇒ p(varsMap)
        case AllValuesInference(p) ⇒ p(varsMap.map{case (k, m) ⇒ (k, m.maxBy(_._2)._1)})}
      //Update UI
      updateUI(net, (intEvds ++ extEvds).map{case (n,_) ⇒ n.getID}.toList, vars)
      //Return
      varsMap}
    case _ ⇒ Map()}
  //DSL
  protected implicit def byNameToNoArg[T](v: ⇒ T):()⇒T = {() ⇒ v}
  protected def cpt:ProbNode = new ProbNode
  protected class ProbNode{
    def node(id:String):ProbCPD = new ProbCPD(id)}
  protected class ProbCPD(nodeID:String){
    def binary(cell0: ⇒Double):Unit = {
      probabilities :+= BinTableProb(nodeID, ()⇒{cell0})}
    def column(name0:String, nameN:String*):ProbColumn = new ProbColumn(nodeID, name0 +: nameN)
    def table(cell0:()⇒Double, cellN:(()⇒Double)*):Unit = {
      probabilities :+= TableProb(nodeID, cell0 +: cellN)}}
  protected class ProbColumn(nodeID:String, colName:Seq[String]){
    def binary(cell0: ⇒ Double): Unit = {
      probabilities :+= BinColumnProb(nodeID, colName.toList, ()⇒{cell0})}
    def of(cell0: () ⇒ Double, cellN: (() ⇒ Double)*): Unit = {
      probabilities :+= ColumnProb(nodeID, colName.toList, cell0 +: cellN)}}
  protected def evidence:EvdNode = new EvdNode
  protected class EvdNode{
      def node(id:String):EvdValue = new EvdValue(id)}
  protected class EvdValue(nodeID:String){
      def of(valueName: ⇒String):Unit = {evidences :+= NodeEvidence(nodeID, ()⇒{valueName})}}
  protected def inference:InfNode = new InfNode
  protected def inference(p:(Map[String, Map[String, Double]])⇒Unit):Unit = {inferences :+= AllInference(p)}
  protected class InfNode{
    def node(id:String):InfResult = new InfResult(id)
    def allValues(p:(Map[String, String])⇒Unit):Unit = {inferences :+= AllValuesInference(p)}}
  protected class InfResult(nodeID:String) {
    def binaryProb(p:Double⇒Unit):Unit = {inferences :+= BinaryInference(nodeID, p)}
    def allProb(p:Map[String,Double]⇒Unit):Unit = {inferences :+= AllProbInference(nodeID, p)}
    def value(p:String⇒Unit):Unit = {inferences :+= ValueInference(nodeID, p)}
    def valueProb(name:String):InfValue = new InfValue(nodeID, name)}
  protected class InfValue(nodeID:String, name:String){
    def prob(p:Double⇒Unit):Unit = {inferences :+= ValueProbInference(nodeID, name, p)}}
  protected def onLoad(proc: ⇒Unit):Unit = {onLoadFun :+= (()⇒{proc})}
  protected def onReload(proc: ⇒Option[String]):Unit = {onReloadFun = Some(()⇒{proc})} //proc must return *.net string, by None do nothing
  //Methods
  /**
   * Load net frome string
   */
  def loadNet(net:String):Unit = reload(Some(net))
  /**
   * Do update
   */
  def update():Unit = doCalc(Map())
  /**
   * Calc values
   * @param evidence - Map(< node name >,< value name >)
   * @return - Map(< node name >,< value name >)
   */
  def values(evidence:Map[String,String]):Map[String,String] =
    doCalc(evidence).map{case (k, m) ⇒ (k, m.maxBy(_._2)._1)}
  /**
   * Calc probabilities
   * @param evidence - Map(< node name >,< value name >)
   * @return - Map(< node name >,Map(< value name >,< probability >))
   */
  def probabilities(evidence:Map[String,String]):Map[String, Map[String, Double]] = doCalc(evidence)
  //Construction
  if(netPath == ""){
    throw new SyntaxException("Error: 'netPath' should not be empty.")}
  //UI
  ui = showUI match{
    case true ⇒ {
      val uiGraph = new SimpleGraph(uiParams, screenW, screenH, false, true)
      val uiReset = new ResetButton(uiParams){
        def reset() = {
          reload(None)}}
      val uiReload = new ReloadButton(uiParams){
        def reload() = {
          onReloadFun.foreach{net ⇒
             net().foreach(n ⇒ helper.thisTool.reload(Some(n)))}}}
      val uiFrame:TVFrame = new TVFrame(
        environment.layout, uiParams, helper.toolName, center = Some(uiGraph), bottom = List(uiReset, uiReload)){
        def closing() = {gear.endWork()}}
      Some((uiFrame, uiGraph))}
    case _ ⇒ None  }
  //Gear
  private val gear:CalculationGear = new CalculationGear(environment.clockwork, updatePriority = 1){
    def start() = {
      //Load net
      loadNet(None)
      //Build UI
      (ui,beliefNet) match{
        case (Some((frame,graph)),Some(net)) ⇒ {
          buildGraph(graph,net)
          frame.show(screenX, screenY, Int.MaxValue, Int.MaxValue)}
        case _ ⇒ }}
    def update() = if(autoUpdate){
      doCalc(Map())}
    def stop() = {
      ui.foreach{case (frame,_) ⇒ frame.hide()}}}}