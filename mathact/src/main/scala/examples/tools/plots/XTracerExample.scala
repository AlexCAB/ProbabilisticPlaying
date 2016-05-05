package examples.tools.plots

import mathact.tools.Workbench
import mathact.tools.plots.XTracer
import mathact.tools.pots.PotBoard
import math._


/**
 * Example of using XTracerExample tool.
 * Created by CAB on 17.03.2015.
 */

object XTracerExample extends Workbench{
  //Creating PotBoard with variables μ and σ
  val variables = new PotBoard{
    val μ = init(0) in(-2,2)
    val σ = init(1) in(-5,5)
  }
  //Creating XTrace
  new XTracer(a = -5, b = 5, autoRange = true){
    import variables._
    trace(name = "μ", color = green) of {_ ⇒ μ}
    trace(name = "σ", color = blue) of {_ ⇒  σ}
    trace(name = "Gaussian", color = red) of {x ⇒ exp(-(pow(x - μ, 2) / (2 * pow(σ, 2)))) / (σ * sqrt(2 * Pi))}
  }
}