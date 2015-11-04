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
