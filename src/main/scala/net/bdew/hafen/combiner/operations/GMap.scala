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

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO

import net.bdew.hafen.combiner._
import net.bdew.hafen.combiner.reader.TileSetReader

import scala.annotation.tailrec
import scala.concurrent.Future

object GMap {
  implicit val EC = Combiner.EC
  def run(op: OpGMap, args: Args, timer: Timer): Unit = {
    timer.mark("Start")

    val srcFile = new File(op.src)
    val destDir = new File(op.dest)

    if (!srcFile.exists() || !srcFile.canRead) {
      println("Input doesn't exist or is not readable, aborting!")
      sys.exit(1)
    }

    if (destDir.exists()) {
      println("Output already exists, aborting!")
      sys.exit(1)
    }

    println("* Input: " + srcFile.getAbsolutePath)
    println("* Output: " + destDir.getAbsolutePath)
    println("* Loading...")

    val tiles = TileSetReader.load(srcFile, FingerPrints.nil) getOrElse {
      println("! Failed to load input")
      sys.exit(-1)
    }

    timer.mark("LOAD INPUT")

    val maxZoomLevel = (Math.log(Math.max(tiles.width, tiles.height)) / Math.log(2)).ceil.toInt
    val size = 1 << maxZoomLevel

    println("* Max Zoom: %d (%d x %d)".format(maxZoomLevel, size, size))

    val delta = Coord(size / 2 - tiles.width / 2, size / 2 - tiles.height / 2)

    val baseLevelDir = new File(destDir, maxZoomLevel.toString)

    val baseLevel =
      Async("Copy base tiles") {
        (for (x <- 0 until size) yield {
          val xDir = new File(baseLevelDir, x.toString)
          xDir.mkdirs()
          for (y <- 0 until size if args.isEnabledNullTiles || tiles.tiles.isDefinedAt(Coord(x, y) - delta)) yield Future {
            val tile = tiles.tiles.getOrElse(Coord(x, y) - delta, NullTile)
            val out = new File(baseLevelDir, "%d/%d.png".format(x, y))
            Files.copy(tile.makeInputStream(), out.toPath)
            Coord(x, y) -> tile
          }
        }).flatten
      } waitUntilDone()

    generateNextZoom(SimpleTileSet(baseLevel.toMap, size), destDir, maxZoomLevel - 1, op.minZoom, timer, args.isEnabledNullTiles)

    timer.mark("DONE")
  }

  @tailrec
  def generateNextZoom(tiles: SimpleTileSet, dest: File, thisZoom: Int, minZoom: Int, timer: Timer, nullTiles: Boolean): Unit = {
    timer.mark("GENERATE ZOOM %d".format(thisZoom + 1))
    val next = Async("Generating zoom level %d".format(thisZoom)) {
      generateTiles(tiles, dest, thisZoom, nullTiles)
    } waitUntilDone()

    if (thisZoom > minZoom)
      generateNextZoom(SimpleTileSet(next.toMap, tiles.size / 2), dest, thisZoom - 1, minZoom, timer, nullTiles)
  }

  def generateTiles(tiles: BaseTileSet, dest: File, zl: Int, nullTiles: Boolean) = {
    val dir = new File(dest, zl.toString)
    for {
      x <- tiles.minX / 2 until tiles.maxX / 2
      y <- tiles.minY / 2 until tiles.maxY / 2
    } yield Future {
      val toDraw = for (xo <- 0 to 1; yo <- 0 to 1; tile <- tiles.tiles.get(new Coord(x * 2 + xo, y * 2 + yo)) if tile != NullTile) yield (xo, yo, tile)
      val out = new File(dir, "%d/%d.png".format(x, y))
      out.getParentFile.mkdirs()
      if (toDraw.isEmpty) {
        if (nullTiles)
          NullTile.write(out)
        Coord(x, y) -> NullTile
      } else {
        val img = new BufferedImage(Combiner.TILE_SIZE * 2, Combiner.TILE_SIZE * 2, BufferedImage.TYPE_INT_ARGB)
        val graphics = img.getGraphics
        for ((xo, yo, tile) <- toDraw)
          graphics.drawImage(tile.readImage(), xo * Combiner.TILE_SIZE, yo * Combiner.TILE_SIZE, null)
        graphics.dispose()
        val scaled = new BufferedImage(Combiner.TILE_SIZE, Combiner.TILE_SIZE, BufferedImage.TYPE_INT_ARGB)
        val scaledGraphics = scaled.createGraphics()
        scaledGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        scaledGraphics.drawImage(img, 0, 0, Combiner.TILE_SIZE, Combiner.TILE_SIZE, null)
        ImageIO.write(scaled, "png", out)
        Coord(x, y) -> MapTileFile(out)
      }
    }
  }
}
