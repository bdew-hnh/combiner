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

class InputSet(val tileSets: List[TileSet], val fingerPrints: FingerPrintDatabase) {
  def merge(that: InputSet) = new InputSet(tileSets ++ that.tileSets, fingerPrints.merge(that.fingerPrints))

  lazy val tileFpMap = {
    val (found, missing) = tileSets.flatMap(_.tiles.values).partition(
      f => fingerPrints.hashMap.isDefinedAt(f.name)
    )

    if (found.nonEmpty)
      println(" * Found fingerprints for %d tiles".format(found.size))

    if (missing.nonEmpty)
      println(" ! Missing fingerprints for %d tiles".format(missing.size))

    found.map(t => t -> fingerPrints.hashMap(t.name)).toMap
  }

  def mergeTiles() = doMergeAll(tileSets)

  sealed private trait MergeResult

  private case class MergeMore(sets: List[TileSet]) extends MergeResult

  private case class MergeDone(sets: List[TileSet]) extends MergeResult

  @tailrec
  private def doMergeAll(sets: List[TileSet]): List[TileSet] = {
    print(" * Merging, %d sets remaining... ".format(sets.size))
    doMerge(sets) match {
      case MergeDone(set) => set
      case MergeMore(set) => doMergeAll(set)
    }
  }

  private def doMerge(sets: List[TileSet]): MergeResult = {
    for {
      set1 <- sets
      (coord1, tile1) <- set1.tiles
      fp1 <- tileFpMap.get(tile1)
      set2 <- sets if set2 != set1
      (coord2, tile2) <- set2.tiles
      fp2 <- tileFpMap.get(tile2)
      if fp1 == fp2
    } {
      val delta = coord2 - coord1
      println("Found match: %s <> %s D=(%d,%d)".format(tile1.name, tile2.name, delta.x, delta.y))
      val newSet = TileSet(set1.tiles ++ set2.tiles.map({ case (c, m) => c - delta -> m }))
      return MergeMore(sets.filterNot(x => x == set1 || x == set2) :+ newSet)
    }
    println("No more matches")
    MergeDone(sets)
  }
}

object InputSet {
  final val mapTileName = "^tile_(-?[0-9]+)_(-?[0-9]+)\\.png$".r

  def load(path: File) = {
    println(" * Reading input directory: " + path.getAbsolutePath)
    if (!path.isDirectory || !path.canRead) sys.error("Unable to read source: %s".format(path.getAbsolutePath))
    val tileSets = {
      val res = for (dir <- path.listFiles() if dir.canRead && dir.isDirectory) yield {
        val tiles = for {
          file <- dir.listFiles() if file.canRead && !file.isDirectory
          name <- mapTileName.findFirstMatchIn(file.getName)
        } yield Coord(name.group(1).toInt, name.group(2).toInt) -> MapTile(file, "%s/%s".format(dir.getName, file.getName))
        println(" * Found directory %s with %d images".format(dir.getName, tiles.length))
        TileSet(tiles.toMap)
      }
      res.filterNot(_.tiles.isEmpty).toList
    }
    val fp = FingerPrintDatabase.from(new File(path, "fingerprints.txt"))
    new InputSet(tileSets, fp)
  }
}
