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

import java.io.{File, FileWriter}
import java.nio.file.Files

object Combiner {
  final val TILE_SIZE = 100

  def main(params: Array[String]): Unit = {
    val args = Args.parse(params)
    val inputs =
      if (args.inputs.isEmpty)
        List(new File("map"))
      else
        args.inputs.map(new File(_))

    val inputSets = inputs map InputSet.load

    val inputSet =
      if (inputSets.isEmpty) {
        println("No valid inputs")
        sys.exit()
      } else if (inputSets.size > 1) {
        inputSets.tail.foldRight(inputSets.head)(_.merge(_))
      } else {
        inputSets.head
      }

    val merged = inputSet.mergeTiles()
    if (args.merge.isDefined) {
      val outDir = new File(args.merge.get)
      if (outDir.exists()) {
        println("Error: output directory %s already exists, aborting!".format(outDir.getAbsolutePath))
        sys.exit(0)
      }
      outDir.mkdir()
      writeTiles(outDir, merged, inputSet.tileFpMap)
      writeMergedImages(outDir, merged, args.grid)
    } else {
      writeMergedImages(inputs.head, merged, args.grid)
    }
  }

  def writeMergedImages(out: File, merged: List[TileSet], grid: Boolean): Unit = {
    for ((t, i) <- merged.zipWithIndex) {
      val file = new File(out, "combined_%d.png".format(i))
      println(" + Writing set #%d with %d tiles to %s".format(i, t.tiles.size, file.getAbsolutePath))
      t.saveCombined(file, grid)
    }
  }

  def writeTiles(out: File, merged: List[TileSet], fpMap: Map[MapTile, String]): Unit = {
    val fpWriter = new FileWriter(new File(out, "fingerprints.txt"))
    for ((tileSet, i) <- merged.zipWithIndex) {
      val dir = new File(out, "combined_%d".format(i))
      dir.mkdirs()
      for ((coord, tile) <- tileSet.tiles) {
        val relocated = coord - tileSet.origin
        val out = new File(dir, "tile_%d_%d.png".format(relocated.x, relocated.y))
        println(" + Copy %s -> %s".format(tile.file.getAbsolutePath, out.getAbsolutePath))
        Files.copy(tile.file.toPath, out.toPath)
        if (fpMap.contains(tile))
          fpWriter.write("combined_%d/tile_%d_%d.png:%s\n".format(i, relocated.x, relocated.y, fpMap(tile)))
      }
    }
    fpWriter.close()
  }
}
