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

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

class InputSet(val tileSets: List[TileSet]) {
  def mergeTiles() = doMergeAll(tileSets)

  sealed private trait MergeResult

  private case class MergeMore(sets: List[TileSet]) extends MergeResult

  private case class MergeDone(sets: List[TileSet]) extends MergeResult

  @tailrec
  private def doMergeAll(sets: List[TileSet]): List[TileSet] = {
    println("* Merging, %d sets remaining... ".format(sets.size))
    doMerge(sets) match {
      case MergeDone(set) => set
      case MergeMore(set) => doMergeAll(set)
    }
  }

  private def doMerge(sets: List[TileSet]): MergeResult = {
    for {
      set1 <- sets
      set2 <- sets if set2 != set1
      (fp1, tile1) <- set1.fingerPrints
      tile2 <- set2.fingerPrints.get(fp1)
      coord1 <- set1.reverse.get(tile1)
      coord2 <- set2.reverse.get(tile2)
    } {
      val delta = coord2 - coord1
      return MergeMore(sets.filterNot(x => x == set1 || x == set2) :+ set1.merge(set2, delta))
    }
    println("* Merging Done")
    MergeDone(sets)
  }
}

object InputSet {
  def loadAsync(paths: List[File])(implicit EC: ExecutionContext) = {
    val inputs = Async("Loading Inputs") {
      (for (path <- paths) yield {
        if (!path.isDirectory || !path.canRead) {
          println("!!! Unable to read source: %s".format(path.getAbsolutePath))
          List.empty
        } else {
          val globFp = FingerPrints.from(new File(path, "fingerprints.txt"))
          path.listFiles().toList filter (x => x.canRead && x.isDirectory) map (dir => Future(TileSet.load(dir, globFp)))
        }
      }).flatten
    }
    inputs.waitUntilDone()
    new InputSet(inputs.result.flatten)
  }
}
