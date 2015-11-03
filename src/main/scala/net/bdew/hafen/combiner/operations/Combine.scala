package net.bdew.hafen.combiner.operations

import java.io.File

import net.bdew.hafen.combiner._
import net.bdew.hafen.combiner.reader.TileSetReader

object Combine {
  implicit val EC = Combiner.EC
  def run(op: OpCombine, args: Args, timer: Timer): Unit = {
    timer.mark("Start")

    val in1d = new File(op.in1)
    val in2d = new File(op.in2)
    val outd = new File(op.out)
    val delta = op.coord2 - op.coord1
    println("* Input 1: " + in1d.getAbsolutePath)
    println("* Input 2: " + in2d.getAbsolutePath)
    println("* Output: " + outd.getAbsolutePath)
    println("* Delta: " + delta)

    if (!in1d.exists() || !in1d.canRead || !in1d.isDirectory) {
      println("! Input 1 does not exist or is not readable")
      sys.exit(-1)
    } else if (!in2d.exists() || !in2d.canRead || !in2d.isDirectory) {
      println("! Input 2 does not exist or is not readable")
      sys.exit(-1)
    } else if (outd.exists()) {
      println("! Output path must not exist")
      sys.exit(-1)
    }

    val t1 = TileSetReader.load(in1d, FingerPrints.nil) getOrElse {
      println("! Input 1 is empty")
      sys.exit(-1)
    }

    val t2 = TileSetReader.load(in2d, FingerPrints.nil) getOrElse {
      println("! Input 2 is empty")
      sys.exit(-1)
    }

    timer.mark("Load")

    val merged = t1.merge(t2, delta)

    Async("Saving Combined") {
      args.getMapWriter.doWriteAsync(outd.getParentFile, outd.getName, merged)
    } waitUntilDone()

    timer.mark("Copy Tiles")
  }
}
