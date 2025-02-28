package MDU


import chisel3._
import chisel3.util._

abstract class CarrySaveAdderMToN(m: Int, n: Int)(len: Int) extends Module{
  val io = IO(new Bundle() {
    val in = Input(Vec(m, UInt(len.W)))
    val out = Output(Vec(n, UInt(len.W)))
  })
}

class CSA3_2(len: Int) extends CarrySaveAdderMToN(3, 2)(len){
  val temp = Wire(Vec(len, UInt(2.W)))
  for((t, i) <- temp.zipWithIndex){
    val (a, b, cin) = (io.in(0)(i), io.in(1)(i), io.in(2)(i))
    val a_xor_b = a ^ b
    val a_and_b = a & b
    val sum = a_xor_b ^ cin
    val cout = a_and_b | (a_xor_b & cin)
    t := Cat(cout, sum)
  }
  io.out.zipWithIndex.foreach({case(x, i) => x := Cat(temp.reverse map(_(i)))})
}

object SignExt {
  def apply(sig: UInt, len: Int): UInt = {
    val signBit = sig(sig.getWidth - 1)
    if (sig.getWidth >= len) sig(len - 1, 0) else signBit.asUInt ## Fill(len - sig.getWidth, signBit) ## sig
  }
}

object ZeroExt {
  def apply(sig: UInt, len: Int): UInt = {
    if (sig.getWidth >= len) sig(len - 1, 0) else 0.U((len - sig.getWidth).W) ## sig
  }
}


class RightShifter(len: Int, lzc_width: Int) extends Module {
  val io = IO(new Bundle() {
    val shiftNum = Input(UInt(lzc_width.W))
    val in = Input(UInt(len.W))
    val msb = Input(Bool())
    val out = Output(UInt(len.W))
  })
  require(len == 64 || len == 32)
  val shift = io.shiftNum
  val msb = io.msb
  val s0 = Mux(shift(0), Cat(VecInit(Seq.fill(1)(msb)).asUInt, io.in(len - 1, 1)), io.in)
  val s1 = Mux(shift(1), Cat(VecInit(Seq.fill(2)(msb)).asUInt, s0(len - 1, 2)), s0)
  val s2 = Mux(shift(2), Cat(VecInit(Seq.fill(4)(msb)).asUInt, s1(len - 1, 4)), s1)
  val s3 = Mux(shift(3), Cat(VecInit(Seq.fill(8)(msb)).asUInt, s2(len - 1, 8)), s2)
  val s4 = Mux(shift(4), Cat(VecInit(Seq.fill(16)(msb)).asUInt, s3(len - 1, 16)), s3)
  val s5 = Wire(UInt(len.W))
  if (len == 64) {
    s5 := Mux(shift(5), Cat(VecInit(Seq.fill(32)(msb)).asUInt, s4(len - 1, 32)), s4)
  } else if (len == 32) {
    s5 := s4
  }
  io.out := s5
}