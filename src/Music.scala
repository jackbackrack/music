package Music

import Chisel._
import scala.math._

//
// Interfacing
//

class DemoIO extends Bundle {
  val t   = Dbl(INPUT);
  val in  = Vec.fill(2){ Dbl(INPUT) };
  val out = Vec.fill(2){ Dbl(OUTPUT) };
}

class Demo extends Module {
  val io = new DemoIO();
}

class MusicIO extends Bundle {
  val t   = Dbl(INPUT);
  val in  = Vec.fill(2){ Dbl(INPUT) };
  val out = Vec.fill(2){ Dbl(OUTPUT) };
}

class Sliders extends BlackBox {
  val io = new Bundle{ val sliders = Vec.fill(256){ Dbl(OUTPUT) } }
	
  // pow_curve: (== 1) is linear, (> 1) emphasizes bottom of range, (< 1) emphasizes top of range
  def apply(idx: Int, min: Dbl = Dbl(0), max: Dbl = Dbl(1), pow_curve: Dbl = Dbl(1)) = {
    val slider_output = io.sliders(idx);
    Smooth(Interpolate(Pow(pow_curve, slider_output), min, max), Dbl(0.995));
  }
}

class Time extends BlackBox {
  val io = new Bundle{ val time = Dbl(OUTPUT) }
}

class Speakers extends BlackBox {
  val io = new Bundle{ val channels = Vec.fill(2){ Dbl(INPUT) } }
}

class Mic extends BlackBox {
  val io = new Bundle{ val channels = Vec.fill(2){ Dbl(OUTPUT) } }
}

//
// Math
//

object Pi { def apply(): Dbl = { Dbl(Math.PI); } }
object TwoPi { def apply(): Dbl = { Dbl(2*Math.PI); } }

object Min { def apply(a: Dbl, b: Dbl): Dbl = { Mux(a < b, a, b); } }
object Max { def apply(a: Dbl, b: Dbl): Dbl = { Mux(a > b, a, b); } }
object Clip { def apply(a: Dbl, low: Dbl, high: Dbl) = { Min(Max(a, low), high); } }

object Log2 { def apply(n: Dbl): Dbl = { Log(n, Dbl(2)); } }
object Log10 { def apply(n: Dbl): Dbl = { Log(n, Dbl(10)); } }
object Pow2 { def apply(n: Dbl): Dbl = { Pow(Dbl(2), n); } }
object Pow10 { def apply(n: Dbl): Dbl = { Pow(Dbl(10), n); } }

object Interpolate {
  def apply(a: Dbl, x: Dbl, y: Dbl): Dbl = {
    (a * y) + ((Dbl(1.0)-a)*x);
  }
}

//
// Counters
//


object Counter {
  def apply(max: Dbl) : Dbl = {
    val count = Reg(init = Dbl(0));
    val next_count = count + Dbl(1);
    count := Mux(next_count >= max, Dbl(0), next_count);
    count
  }
}

object SampleHold {
  def apply(x: Dbl, trigger: Bool) : Dbl = {
    val output = Reg(init = Dbl(0));
    output := Mux(trigger, x, output);
    output
  }
}

//
// Musical Unit Conversion
//

object PitchToFreq { def apply(pitch : Dbl): Dbl = { Pow2((pitch - Dbl(69)) / Dbl(12)); } }
object FreqToPitch { def apply(freq : Dbl): Dbl = { Dbl(12) * Log2(freq / Dbl(440)) + Dbl(69); } }

object DbToAmp { def apply(db : Dbl): Dbl = { Pow10(db / Dbl(20)); } }
object AmpToDb { def apply(amp : Dbl): Dbl = { Log10(amp) * Dbl(20); } }

object SampsPerSec { def apply(): Dbl = { Dbl(44100); } }
object SampToSec { def apply(samps: Dbl): Dbl = { samps / SampsPerSec(); } }
object SecToSamp { def apply(secs: Dbl): Dbl = { secs * SampsPerSec(); } }

//
// Generators
//

object SinWave {
  def apply(f: Dbl): Dbl = {
    val phase = Reg(init = Dbl(0.0));
    phase := phase + (f * TwoPi() / SampsPerSec());
    Sin(phase)
  }
}

object PhasorWave {
  def apply(f: Dbl): Dbl = {
    val phase = Reg(init = Dbl(0.0));
    val next_phase = phase + (f / SampsPerSec());
    phase := Mux(next_phase >= Dbl(1.0), next_phase - Dbl(1.0), next_phase);
    phase
  }
}

object SawWave {
  def apply(f: Dbl): Dbl = {
    val phasor = PhasorWave(f);
    (phasor * Dbl(2.0)) - Dbl(1.0);
  }
}

object TriangleWave {
  def apply(f: Dbl): Dbl = {
    val phasor = PhasorWave(f);
    (Dbl(4.0) * (Mux(phasor <= Dbl(0.5), phasor, Dbl(1.0) - phasor))) - Dbl(1.0);
  }
}

object PulseTrain {
  def apply(freq : Dbl): Dbl = {
    val period_samps = SecToSamp(Dbl(1.0) / freq); //fixme: add rounding
    val counter = Counter(period_samps);
    Mux(counter <= Dbl(0), Dbl(1), Dbl(0));
  }
}

object PulseWidthMod {
  def apply(total_period : Dbl, duty_ratio : Dbl): Dbl = {
    val samps_total = SecToSamp(total_period);  //fixme: add rounding
    val samps_duty = duty_ratio * SecToSamp(total_period);
    val counter = Counter(samps_total);
    Mux(counter <= samps_duty, Dbl(1), Dbl(0));
  }
}

//
// Modulation
//

object FreqMod {
  def apply(fc: Dbl, fmr: Dbl, im: Dbl): Dbl = {
    val fm = fc * fmr;
    SinWave(fc + (fm*im*SinWave(fm)));
  }
}
 
object AmpMod {
  def apply(carrier: Dbl, fm: Dbl, index: Dbl) = {
    carrier * (((SinWave(fm) + Dbl(1)) * index / Dbl(2)) + (Dbl(1) - index));
  }
}

object RingMod {
  def apply(carrier: Dbl, fm: Dbl, index: Dbl): Dbl = {
    ((Dbl(1)-index) * carrier) + (index * carrier * SinWave(fm));
  }
}

//
// Filters and Delays
//

object Smooth {
  def apply(x: Dbl, a: Dbl): Dbl = {
    val sum = Reg(init = Dbl(0.0));
    sum := ((Dbl(1.0)-a) * x) + (a*sum)
    sum
  }
}

/*
object CombFilterFixed {
  def apply(input: Dbl, period: Double, feedback: Dbl): Dbl = {
    val period_samps : Int = floor(period * 44100.0).toInt;
    val inner_reg = Reg(init = Dbl(0.0));
    val delayed = ShiftRegister(period_samps-1, inner_reg);
    inner_reg := (delayed * feedback) + input;
    inner_reg;
  }
}

object VCF {
	def apply(cutoff: Dbl, res: Dbl, input: Dbl): Dbl = {
		val e      = 2.714;
		val y1     = Reg(init = Dbl(0.0));
		val y2     = Reg(init = Dbl(0.0));
		val y3     = Reg(init = Dbl(0.0));
		val y4     = Reg(init = Dbl(0.0));
		val f      = Dbl(2.0) * cutoff / Dbl(44100.0);
		val k      = Dbl(3.6) * f - Dbl(1.6)*f*f - Dbl(1.0);
		val p      = (k + Dbl(1.0)) * Dbl(0.5);
		val scale  = Pow(Dbl(e), (Dbl(1.0)-p)*Dbl(1.386249));
		val r      = res * scale
		val x      = input - r * y4
		y1        := x*p  + Reg(x)*p  - k*y1;
		y2        := y1*p + Reg(y1)*p - k*y2;
		y3        := y2*p + Reg(y2)*p - k*y3;
		val y4t    = y3*p + Reg(y3)*p - k*y4;
		y4        := y4t - Pow(y4t, Dbl(3.0)) / Dbl(6.0);
		y4
	}
}
*/

//*****************************************//
// Application
//*****************************************//

class Monotron extends Module {
  val io = new Bundle {
    val swof = Dbl(INPUT);
    val lfof = Dbl(INPUT);
    val lfoi = Dbl(INPUT);
    val lfos = Dbl(INPUT);
    val vcfc = Dbl(INPUT);
    val vcfq = Dbl(INPUT);
    val out  = Dbl(OUTPUT);
    val lfo  = Dbl(OUTPUT);
  }
  val lfo_saw = (Dbl(1)-io.lfos)*SawWave(io.lfof);
  val lfo_sin = (Dbl(-1)*io.lfos*SinWave(io.lfof));
  val lfo = io.lfoi * (lfo_saw + lfo_sin);
  val lfo_scaled = Dbl(0.8)*io.swof*lfo;
  val vco = SawWave(io.swof + lfo_scaled)
  val vcf = VCF(io.vcfc, io.vcfq, vco)
  io.out := vcf
  io.lfo := lfo
}

class Music extends Module {
  val io = new Bundle{ 
    val o    = Vec.fill(2){ Dbl(OUTPUT) };
    val lfos = Vec.fill(2){ Dbl(OUTPUT) };
  }
  val s   = Module(new Sliders())
  val o   = Module(new Speakers())

  val m1  = Module(new Monotron())
  val kb1 = 1;
  val sb1 = 81;
  val gain1 = s(sb1+0, Dbl(0.0), Dbl(1.0), Dbl(3.0));
  m1.io.lfos := s(kb1+1, Dbl(0.0), Dbl(1.0), Dbl(1.0));
  m1.io.swof := s(sb1+1, Dbl(40.0), Dbl(1000.0), Dbl(2.5));
  m1.io.lfoi := s(kb1+2, Dbl(0.0), Dbl(1.0), Dbl(2.0));
  m1.io.lfof := s(sb1+2, Dbl(1.0), Dbl(50.0), Dbl(2.0));
  m1.io.vcfc := s(sb1+3, Dbl(100.0), Dbl(10000.0), Dbl(2.0));
  m1.io.vcfq := s(kb1+3);

  val m2  = Module(new Monotron())
  val kb2 = 5;
  val sb2 = 85;
  val gain2 = s(sb2+0, Dbl(0.0), Dbl(1.0), Dbl(3.0));
  m2.io.lfos := s(kb2+1, Dbl(0.0), Dbl(1.0), Dbl(1.0));
  m2.io.swof := s(sb2+1, Dbl(40.0), Dbl(1000.0), Dbl(2.5));
  m2.io.lfoi := s(kb2+2, Dbl(0.0), Dbl(1.0), Dbl(2.0));
  m2.io.lfof := s(sb2+2, Dbl(1.0), Dbl(50.0), Dbl(2.0));
  m2.io.vcfc := s(sb2+3, Dbl(100.0), Dbl(10000.0), Dbl(2.0));
  m2.io.vcfq := s(kb2+3);

  // val out = SinWave(Dbl(440)); // gain1 * m1.io.out + gain2 * m2.io.out
  val out = gain1 * m1.io.out + gain2 * m2.io.out
  io.o(0) := out;
  io.o(1) := out;
  io.lfos(0) := m1.io.lfo;
  io.lfos(1) := m2.io.lfo;
  o.io.channels(0) := out;
  o.io.channels(1) := out;
}

//
// Problematic: To be completed...
//

object Remainder { 
  def apply(a: Dbl, b: Dbl): Dbl = {
    val quotient = a / b;
    quotient - Floor(quotient);
  }
}

object Mod { def apply(a: Dbl, b: Dbl) = { Remainder(a,b) * b; } }

// object Random { def apply() : Dbl = { ??? } // need Modulo arithmetic

object SimpleCounter {
  def apply(max: Int, initial: Int = 0) : UInt = {
    val count = Reg(init = UInt(initial, log2Up(max)));
    count := Mux(count === UInt(max-1), UInt(0), count + UInt(1))
    count
  }
}

object MemBasedDelay { // currently outputs noise
  def apply(max_delay: Int, current_delay: Dbl, input: Dbl): Dbl = {
    val write_pos = SimpleCounter(max_delay, 0);
    // val read_pos_raw = write_pos - current_delay.toSInt;
    // val read_pos = Mux(read_pos_raw < SInt(0), read_pos_raw + Fix(max_delay), read_pos_raw);
    val delay = current_delay.toUInt;
    val read_pos = Mux(write_pos < delay, write_pos + (UInt(max_delay) - delay), write_pos - delay);
    val memory = Mem(Dbl(), max_delay)
    val output = memory(read_pos);
    memory(write_pos) := input;
    output
  }
}
