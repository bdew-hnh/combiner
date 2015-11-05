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

    if (!in1d.exists() || !in1d.canRead) {
      println("! Input 1 does not exist or is not readable")
      sys.exit(-1)
    } else if (!in2d.exists() || !in2d.canRead) {
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
