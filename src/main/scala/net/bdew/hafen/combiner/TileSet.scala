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

import java.awt.image.BufferedImage
import java.io.{File, FileWriter}
import java.nio.file.Files
import javax.imageio.ImageIO

import scala.concurrent.{ExecutionContext, Future}

case class TileSet(tiles: Map[Coord, MapTile], fingerPrints: Map[String, MapTile]) {
  lazy val minX = tiles.keys.map(_.x).min
  lazy val maxX = tiles.keys.map(_.x).max
  lazy val minY = tiles.keys.map(_.y).min
  lazy val maxY = tiles.keys.map(_.y).max
  lazy val width = maxX - minX + 1
  lazy val height = maxY - minY + 1
  lazy val origin = Coord(minX, minY)

  lazy val reverse = tiles.map(_.swap)


  def saveCombined(output: File, grid: Boolean, coords: Boolean): Unit = {
    val result = try {
      new BufferedImage(width * Combiner.TILE_SIZE, height * Combiner.TILE_SIZE, BufferedImage.TYPE_INT_ARGB)
    } catch {
      case e: OutOfMemoryError =>
        println("Unable to draw map %s (%dx%d) - not enough memory".format(output.getName, width, height))
        return
    }
    val g = result.getGraphics
    for ((c, tile) <- tiles) {
      val ct = c - origin
      g.drawImage(tile.getImage, ct.x * Combiner.TILE_SIZE, ct.y * Combiner.TILE_SIZE, null)
      if (coords) {
        g.drawString("(%d,%d)".format(ct.x, ct.y), ct.x * Combiner.TILE_SIZE + 3, ct.y * Combiner.TILE_SIZE + 10)
      }
      if (grid) {
        if (tiles.isDefinedAt(c.copy(y = c.y - 1)))
          g.drawLine(ct.x * Combiner.TILE_SIZE, ct.y * Combiner.TILE_SIZE, (ct.x + 1) * Combiner.TILE_SIZE, ct.y * Combiner.TILE_SIZE)
        if (tiles.isDefinedAt(c.copy(x = c.x - 1)))
          g.drawLine(ct.x * Combiner.TILE_SIZE, ct.y * Combiner.TILE_SIZE, ct.x * Combiner.TILE_SIZE, (ct.y + 1) * Combiner.TILE_SIZE)
      }
    }
    ImageIO.write(result, "png", output)
  }

  def saveTilesAsync(dir: File)(implicit ec: ExecutionContext) = {
    dir.mkdirs()
    val reverseFp = fingerPrints.map(_.swap)
    val fpWriter = new FileWriter(new File(dir, "fingerprints.txt"))
    try {
      for ((coord, tile) <- tiles) yield {
        val relocated = coord - origin
        if (reverseFp.contains(tile))
          fpWriter.write("tile_%d_%d.png:%s\n".format(relocated.x, relocated.y, reverseFp(tile)))
        Future(Files.copy(tile.file.toPath, new File(dir, "tile_%d_%d.png".format(relocated.x, relocated.y)).toPath))
      }
    } finally {
      fpWriter.close()
    }
  }

  def merge(that: TileSet, delta: Coord) = {
    var tiles = this.tiles
    for ((c, t) <- that.tiles) {
      val cmod = c - delta
      if (!tiles.isDefinedAt(cmod) || t.lastModified > tiles(cmod).lastModified)
        tiles += cmod -> t
    }
    TileSet(tiles, this.fingerPrints ++ that.fingerPrints)
  }
}

object TileSet {
  private final val mapTileName = "^tile_(-?[0-9]+)_(-?[0-9]+)\\.png$".r

  val combinedCounter = Iterator.from(1)

  def load(dir: File, globFp: FingerPrints): Option[TileSet] = {
    val tiles =
      for {
        file <- dir.listFiles().toList if file.canRead && !file.isDirectory
        name <- mapTileName.findFirstMatchIn(file.getName)
      } yield Coord(name.group(1).toInt, name.group(2).toInt) -> MapTile(file)
    if (tiles.nonEmpty) {
      val lookup = globFp.mkLookup(dir.getName, FingerPrints.from(new File(dir, "fingerprints.txt")))
      val fps =
        for {
          (coord, tile) <- tiles
          fp <- lookup(tile.file.getName)
        } yield fp -> tile
      Some(TileSet(tiles.toMap, fps.toMap))
    } else {
      None
    }
  }
}