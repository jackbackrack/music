package Music

import Chisel._

object Main {
  def main(args: Array[String]): Unit = { 
    val rArgs  = args.slice(1, args.length) 
    val cArgs = rArgs ++ Array("--backend", "c", "--targetDir", "../emulator")
    val tArgs = rArgs ++ Array("--backend", "c", "--targetDir", "../emulator", "--compile", "--genHarness")
    val vArgs = rArgs ++ Array("--backend", "v", "--targetDir", "../verilog")
    val fArgs = rArgs ++ Array("--backend", "flo", "--targetDir", "../emulator")
    val res = 
    args(0) match {
      case "music" => // FOR FPGA or ASIC one TILE with 8 queues in torus
        chiselMain(cArgs, () => new Music())
    }
    // chiselMainTest(args, () => { Component.isReportDims = false; new Fabric(2) }){
    //   c => new FabricTests(c)}
  }
}

