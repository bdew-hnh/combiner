package net.bdew.hafen.combiner.operations

import java.io.File

import net.bdew.hafen.combiner._
import net.bdew.hafen.combiner.writer.CombinedWriter

import scala.concurrent.Future

object Normal {
  implicit val EC = Combiner.EC
  def run(op: OpBasic, args: Args, timer: Timer): Unit = {
    timer.mark("Start")

    val inputSet = InputSet.loadAsync(args.inputs)

    timer.mark("Load")

    if (inputSet.tileSets.isEmpty) {
      println("Error: No valid inputs")
      sys.exit()
    }

    val outDir = op match {
      case OpMerge(dest) =>
        val dir = new File(dest)
        if (dir.exists()) {
          println("Error: output directory %s already exists, aborting!".format(dir.getAbsolutePath))
          sys.exit(0)
        }
        dir.mkdirs()
        dir
      case OpNormal() =>
        args.inputs.head
    }

    val merged = inputSet.mergeTiles()
    timer.mark("Merge")

    if (op.isInstanceOf[OpMerge]) {
      val writer = args.getMapWriter
      Async("Saving Merged Maps") {
        merged.zipWithIndex.flatMap { case (t, i) => writer.doWriteAsync(outDir, "combined_" + i, t) }
      } waitUntilDone()
      timer.mark("Save Maps")
    }

    if (args.isEnabledImgOut) {
      Async("Saving Images") {
        for ((t, i) <- merged.zipWithIndex) yield Future {
          CombinedWriter.saveCombined(t, new File(outDir, "combined_%d.png".format(i)), args.isEnabledGrid, args.isEnabledCoords)
        }
      } waitUntilDone()
      timer.mark("Generate Images")
    }
  }
}
