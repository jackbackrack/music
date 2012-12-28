package Music

import Chisel._

class MusicIO extends Bundle {
  val t   = Dbl(INPUT);
  val in  = Vec(2){ Dbl(INPUT) };
  val out = Vec(2){ Dbl(OUTPUT) };
}

class Music extends Component {
  val io = new MusicIO();
  io.out(0) := Sin(Dbl(1500.0) + Dbl( 800.0) * Sin(Dbl(3.0) * io.t));
  io.out(1) := Sin(Dbl(2000.0) + Dbl(1000.0) * Sin(Dbl(4.0) * io.t));
  // io.out(0) := Sin(Flo(1000.0) * io.t);
  // io.out(1) := Sin(Flo(2000.0) * io.t);
}
