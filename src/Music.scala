package Music

import Chisel._

class DemoIO extends Bundle {
  val t   = Dbl(INPUT);
  val in  = Vec(2){ Dbl(INPUT) };
  val out = Vec(2){ Dbl(OUTPUT) };
}

class Demo extends Component {
  val io = new DemoIO();
  io.out(0) := Sin(Dbl(1500.0) + Dbl( 800.0) * Sin(Dbl(3.0) * io.t));
  io.out(1) := Sin(Dbl(2000.0) + Dbl(1000.0) * Sin(Dbl(4.0) * io.t));
  // io.out(0) := Sin(Flo(1000.0) * io.t);
  // io.out(1) := Sin(Flo(2000.0) * io.t);
}

class MusicIO extends Bundle {
  val t   = Dbl(INPUT);
  val in  = Vec(2){ Dbl(INPUT) };
  val out = Vec(2){ Dbl(OUTPUT) };
}

object Interpolate {
  def apply(a: Dbl, x: Dbl, y: Dbl): Dbl = 
    (a * y) + ((Dbl(1.0)-a)*x)
}

class Sliders extends BlackBox {
  val io = new Bundle{ val sliders = Vec(8){ Dbl(OUTPUT) } }
  def apply(idx: Int, min: Dbl, max: Dbl, pow: Dbl = null) = {
    if (pow == null)
      Interpolate(io.sliders(idx), min, max)
    else
      Pow(Interpolate(io.sliders(idx), Log(min, pow), Log(max, pow)), pow)
  }
  def apply(idx: Int) = io.sliders(idx);
}

class Time extends BlackBox {
  val io = new Bundle{ val time = Dbl(OUTPUT) }
}

class Speakers extends BlackBox {
  val io = new Bundle{ val channels = Vec(2){ Dbl(INPUT) } }
}

class Mic extends BlackBox {
  val io = new Bundle{ val channels = Vec(2){ Dbl(OUTPUT) } }
}

object Phase {
  def apply(f: Dbl): Dbl = {
    val s = new Phase(); s.io.f := f; s.io.o
  }
}
class Phase extends Component {
  val io = new Bundle{ 
    val f = Dbl(INPUT);
    val o = Dbl(OUTPUT);
  }
  val p = Reg(resetVal = Dbl(0.0));
  p := p + io.f * Dbl(1.0 / 44100.0);
  io.o := Sin(p);
}

object SinWave {
  def apply(f: Dbl): Dbl = {
    val s = new SinWave(); s.io.f := f; s.io.o
  }
}
class SinWave extends Component {
  val io = new Bundle{ 
    val f = Dbl(INPUT);
    val o = Dbl(OUTPUT);
  }
  io.o := Sin(Phase(io.f));
}

object SawWave {
  def apply(f: Dbl): Dbl = {
    val s = new SawWave();  s.io.f := f; s.io.o
  }
}
class SawWave extends Component {
  val io = new Bundle{ 
    val f = Dbl(INPUT);
    val o = Dbl(OUTPUT);
  }
  val e = Reg(resetVal = Dbl(0.0));
  val i = Dbl(1.0 / 44100.0);
  val p = Dbl(1.0) / io.f;
  val s = io.f;
  when (e >= p) {
    e := e - p + i;
  } .otherwise {
    e := e + i;
  }
  io.o := io.f * e * Dbl(2.0) - Dbl(1.0);
}

/*
class Music extends Component {
  val io = new Bundle{ 
    val o = Vec(2){ Dbl(OUTPUT) };
  }
  val s = new Sliders();
  val o = new Speakers();

  val x = SinWave(Interpolate(s.io.sliders(0), Dbl(1000.0), Dbl(2000.0)));
  o.io.channels(0) := x;
  io.o(0) := x;
  // w.io.t := Dbl(0.0);
  // w.io.f := Dbl(0.0);
}
*/

class Music extends Component {
  val io = new Bundle{ 
    val o = Vec(2){ Dbl(OUTPUT) };
  }
  val s   = new Sliders();
  val o   = new Speakers();
  val f   = s(0, Dbl(10.0), Dbl(10000.0), Dbl(10.0));
  val lfo = s(1, Dbl(0.0), Dbl(10.0)) * SawWave(s(2, Dbl(0.5), Dbl(10.0), Dbl(10.0)));
  val vco = SawWave(f + lfo)
  // val vcf = VCF(s(3, Dbl(2.0), Dbl(4.0), Dbl(10.0)), s(4), vco);
  val vcf = VCF(s(3, Dbl(400.0), Dbl(10000.0), Dbl(10.0)), s(4), vco);
  val out = vcf
  io.o(0) := out;
  io.o(1) := out;
  o.io.channels(0) := out;
  o.io.channels(1) := out;
}
