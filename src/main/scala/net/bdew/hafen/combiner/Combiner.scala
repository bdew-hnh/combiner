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

object Combiner {
  final val TILE_SIZE = 100

  def main(args: Array[String]): Unit = {
    val workDir =
      if (args.length >= 1)
        new File(args.head)
      else
        new File(System.getProperty("user.dir"), "map")

    println(" * Source Directory: " + workDir.getAbsolutePath)

    if (!workDir.isDirectory || !workDir.canRead) sys.error("Unable to read source directory")

    val tileSets = findInputSets(workDir)
    val fpDatabase = new FingerPrintDatabase(new File(workDir, "fingerprints.txt"))

    val (found, missing) = tileSets.flatMap(_.tiles.values).partition(f => fpDatabase.hashMap.isDefinedAt(f.name))

    if (found.nonEmpty)
      println(" * Found fingerprints for %d tiles".format(found.size))

    if (missing.nonEmpty)
      println(" ! Missing fingerprints for %d tiles".format(missing.size))

    val tileFpMap = found.map(t => t -> fpDatabase.hashMap(t.name)).toMap

    val merged = doMergeAll(tileSets, tileFpMap)

    for ((t, i) <- merged.zipWithIndex) {
      println(" + Writing set #%d with %d images to combined_%d.png".format(i, t.tiles.size, i))
      t.saveCombined(new File(workDir, "combined_%d.png".format(i)))
    }
  }

  @tailrec
  def doMergeAll(sets: List[TileSet], fpMap: Map[MapTile, String]): List[TileSet] = {
    print(" * Merging, %d sets remaining... ".format(sets.size))
    val (res, more) = doMerge(sets, fpMap)
    if (!more || res.size <= 1)
      res
    else
      doMergeAll(res, fpMap)
  }

  def doMerge(sets: List[TileSet], fpMap: Map[MapTile, String]): (List[TileSet], Boolean) = {
    for {
      set1 <- sets
      (coord1, tile1) <- set1.tiles
      fp1 <- fpMap.get(tile1)
      set2 <- sets if set2 != set1
      (coord2, tile2) <- set2.tiles
      fp2 <- fpMap.get(tile2)
      if fp1 == fp2
    } {
      val delta = coord2 - coord1
      println("Found match: %s <> %s D=(%d,%d)".format(tile1.name, tile2.name, delta.x, delta.y))
      val newSet = TileSet(set1.tiles ++ set2.tiles.map({ case (c, m) => c - delta -> m }))
      return (sets.filterNot(x => x == set1 || x == set2) :+ newSet, true)
    }
    println("No more matches")
    (sets, false)
  }

  var mapTileName = "^tile_(-?[0-9]+)_(-?[0-9]+)\\.png$".r

  def findInputSets(base: File): List[TileSet] = {
    val res = for (dir <- base.listFiles() if dir.canRead && dir.isDirectory) yield {
      val tiles = for {
        file <- dir.listFiles() if file.canRead && !file.isDirectory
        name <- mapTileName.findFirstMatchIn(file.getName)
      } yield Coord(name.group(1).toInt, name.group(2).toInt) -> MapTile(file, "%s/%s".format(dir.getName, file.getName))
      println(" * Found directory %s with %d images".format(dir.getName, tiles.length))
      TileSet(tiles.toMap)
    }
    res.filterNot(_.tiles.isEmpty).toList
  }
}
