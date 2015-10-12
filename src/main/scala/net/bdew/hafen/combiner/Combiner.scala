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

      if (params.headOption.contains("--combine")) {
        if (params.length != 8) {
          println("Usage: --combine <indir1> <x1> <y1> <indir2> <x2> <y2> <outdir>")
          sys.exit(-1)
        }
        timer.mark("Start")
        doCombine(params(1), Coord(params(2).toInt, params(3).toInt), params(4), Coord(params(5).toInt, params(6).toInt), params(7), timer)
      } else {

        val args = Args.parse(params)

        timer.mark("Start")

        val inputs =
          if (args.inputs.isEmpty)
            List(new File("map"))
          else
            args.inputs.map(new File(_))

        if (args.autoMerge) {
          doAutoMerge(inputs, timer, args)
        } else {
          doImages(inputs, timer, args)
        }

        if (args.timer)
          timer.print()
      }
    } finally {
      pool.shutdown()
    }
    println("*** All done! ***")
  }

  def doAutoMerge(inputs: List[File], timer: Timer, args: Args): Unit = {
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
      if (args.mpk) {
        writeTilesMPK(outDir, merged)
      } else {
        writeTiles(outDir, merged)
      }
      timer.mark("Write Sets")
      if (args.imgOut) {
        writeMergedImages(outDir, merged, args.grid, args.coords)
        timer.mark("Write Images")
      }
    } else {
      writeMergedImages(inputs.head, merged, args.grid, args.coords)
      timer.mark("Write Images")
    }
  }

  def doImages(inputs: List[File], timer: Timer, args: Args): Unit = {
    if (args.inputs.length != 1) {
      println("--nomerge must be used with a single input")
      sys.exit(-1)
    }

    val input = new File(args.inputs.head)
    val tileSets = InputSet.loadSingle(input)

    timer.mark("Load")

    Async("Saving Images") {
      for ((n, t) <- tileSets) yield Future {
        CombinedWriter.saveCombined(t, new File(input, n.getName + ".png"), args.grid, args.coords)
      }
    } waitUntilDone()

    timer.mark("Write Images")
  }

  def doCombine(in1: String, c1: Coord, in2: String, c2: Coord, out: String, timer: Timer): Unit = {
    val in1d = new File(in1)
    val in2d = new File(in2)
    val outd = new File(out)
    val delta = c2 - c1
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

    val t1 = TileSet.load(in1d, FingerPrints.nil) getOrElse {
      println("! Input 1 is empty")
      sys.exit(-1)
    }

    val t2 = TileSet.load(in2d, FingerPrints.nil) getOrElse {
      println("! Input 2 is empty")
      sys.exit(-1)
    }

    timer.mark("Load")

    val merged = t1.merge(t2, delta)

    Async("Saving Combined") {
      merged.saveTilesAsync(outd).toList
    } waitUntilDone()

    timer.mark("Copy Tiles")
  }

  def writeMergedImages(out: File, merged: List[TileSet], grid: Boolean, coords: Boolean): Unit = {
    Async("Saving Images") {
      for ((t, i) <- merged.zipWithIndex) yield Future {
        CombinedWriter.saveCombined(t, new File(out, "combined_%d.png".format(i)), grid, coords)
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

  def writeTilesMPK(out: File, merged: List[TileSet]): Unit = {
    val q = Async("Saving MPK Files") {
      for ((t, i) <- merged.zipWithIndex) yield Future {
        t.saveTilesMPK(new File(out, "combined_%s.mpk".format(i)))
      }
    } waitUntilDone()
  }
}
