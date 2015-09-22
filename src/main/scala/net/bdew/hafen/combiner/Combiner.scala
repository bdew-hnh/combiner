/*
 * This file is part of bdew's H&H map stitcher.
 * Copyright (C) 2015 bdew
 *
 * Redistribution and/or modification of this file is subject to the
 * terms of the GNU Lesser General Public License, version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Other parts of this source tree adhere to other copying
 * rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 * A copy the GNU Lesser General Public License is distributed along
 * with the source tree of which this file is a part in the file
 * `doc/LPGL-3'. If it is missing for any reason, please see the Free
 * Software Foundation's website at <http://www.fsf.org/>, or write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */

package net.bdew.hafen.combiner

import java.io.File
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

object Combiner {
  final val TILE_SIZE = 100
  val pool = Executors.newFixedThreadPool(5)
  implicit val EC = ExecutionContext.fromExecutor(pool)

  def main(params: Array[String]): Unit = {
    try {
      val timer = Timer("Processing")

      val args = Args.parse(params)
      val inputs =
        if (args.inputs.isEmpty)
          List(new File("map"))
        else
          args.inputs.map(new File(_))

      timer.mark("Start")

      if (args.autoMerge) {
        val inputSet = InputSet.loadAsync(inputs)

        timer.mark("Load")

        if (inputSet.tileSets.isEmpty) {
          println("No valid inputs")
          sys.exit()
        }

        val merged = inputSet.mergeTiles()

        timer.mark("Merge")

        if (args.merge.isDefined) {
          val outDir = new File(args.merge.get)
          if (outDir.exists()) {
            println("Error: output directory %s already exists, aborting!".format(outDir.getAbsolutePath))
            sys.exit(0)
          }
          outDir.mkdir()
          writeTiles(outDir, merged)
          timer.mark("Write Sets")
          if (args.imgOut) {
            writeMergedImages(outDir, merged, args.grid, args.coords)
            timer.mark("Write Images")
          }
        } else {
          writeMergedImages(inputs.head, merged, args.grid, args.coords)
          timer.mark("Write Images")
        }
      } else {
        if (args.inputs.length != 1) {
          println("--nomerge must be used with a single input")
          sys.exit(-1)
        }

        val input = new File(args.inputs.head)
        val tileSets = InputSet.loadSingle(input)

        timer.mark("Load")

        Async("Saving Images") {
          for ((n, t) <- tileSets) yield Future {
            t.saveCombined(new File(input, n.getName + ".png"), args.grid, args.coords)
          }
        } waitUntilDone()

        timer.mark("Write Images")
      }

      if (args.timer)
        timer.print()

    } finally {
      pool.shutdown()
    }
    println("*** All done! ***")
  }

  def writeMergedImages(out: File, merged: List[TileSet], grid: Boolean, coords: Boolean): Unit = {
    Async("Saving Images") {
      for ((t, i) <- merged.zipWithIndex) yield Future {
        t.saveCombined(new File(out, "combined_%d.png".format(i)), grid, coords)
      }
    } waitUntilDone()
  }

  def writeTiles(out: File, merged: List[TileSet]): Unit = {
    val q = Async("Saving Merged") {
      (for ((t, i) <- merged.zipWithIndex) yield {
        t.saveTilesAsync(new File(out, "combined_" + i))
      }).flatten
    } waitUntilDone()
  }
}
