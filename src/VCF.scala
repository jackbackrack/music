package Music

import Chisel._

// Moog VCF from CSound source code, Stilson/Smith CCRMA paper

object VCF {
  def apply(cutoff: Dbl, res: Dbl, input: Dbl): Dbl = {
    val s = new VCF(); s.io.cutoff := cutoff; s.io.res := res; s.io.input := input; s.io.output
  }
}
class VCF extends Component {
  val io = new Bundle {
    val cutoff = Dbl(INPUT);
    val res    = Dbl(INPUT);
    val input  = Dbl(INPUT);
    val output = Dbl(OUTPUT);
  };
  val e      = 2.714;
  val y1     = Reg(resetVal = Dbl(0.0));
  val y2     = Reg(resetVal = Dbl(0.0));
  val y3     = Reg(resetVal = Dbl(0.0));
  val y4     = Reg(resetVal = Dbl(0.0));
  val f      = Dbl(2.0) * io.cutoff / Dbl(44100.0);
  val k      = Dbl(3.6) * f - Dbl(1.6)*f*f - Dbl(1.0);
  val p      = (k + Dbl(1.0)) * Dbl(0.5);
  val scale  = Pow(Dbl(e), (Dbl(1.0)-p)*Dbl(1.386249));
  val r      = io.res * scale
  val x      = io.input - r * y4
  val oldx   = Reg(x)
  val oldy1  = Reg(y1)
  val oldy2  = Reg(y2)
  val oldy3  = Reg(y3)
  y1        := x*p  + oldx*p  - k*y1;
  y2        := y1*p + oldy1*p - k*y2;
  y3        := y2*p + oldy2*p - k*y3;
  val y4t    = y3*p + oldy3*p - k*y4;
  y4        := y4t - Pow(y4t, Dbl(3.0)) / Dbl(6.0);
  io.output := y4
}
