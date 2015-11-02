package net.bdew.hafen.combiner.operations

import java.io.File

import net.bdew.hafen.combiner._
import net.bdew.hafen.combiner.writer.CombinedWriter

import scala.concurrent.Future

object Images {
  implicit val EC = Combiner.EC
  def run(op: OpImages, args: Args, timer: Timer): Unit = {
    val input = new File(op.path)
    val tileSets = InputSet.loadSingle(input)

    timer.mark("Load")

    Async("Saving Images") {
      for ((n, t) <- tileSets) yield Future {
        CombinedWriter.saveCombined(t, new File(input, n.getName + ".png"), args.isEnabledGrid, args.isEnabledCoords)
      }
    } waitUntilDone()

    timer.mark("Write Images")
  }
}
