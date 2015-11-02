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
import java.util.zip.ZipFile

case class TileSet(tiles: Map[Coord, MapTile], fingerPrints: Map[String, Coord]) {
  lazy val minX = tiles.keys.map(_.x).min
  lazy val maxX = tiles.keys.map(_.x).max
  lazy val minY = tiles.keys.map(_.y).min
  lazy val maxY = tiles.keys.map(_.y).max
  lazy val width = maxX - minX + 1
  lazy val height = maxY - minY + 1
  lazy val origin = Coord(minX, minY)

  lazy val reverse = tiles.map(_.swap)

  def merge(that: TileSet, delta: Coord) = {
    var tiles = this.tiles
    for ((c, t) <- that.tiles) {
      val cmod = c - delta
      if (!tiles.isDefinedAt(cmod) || t.lastModified > tiles(cmod).lastModified)
        tiles += cmod -> t
    }
    TileSet(tiles, this.fingerPrints ++ that.fingerPrints.map(x => x._1 -> (x._2 - delta)))
  }
}

object TileSet {
  private final val mapTileName = "^tile_(-?[0-9]+)_(-?[0-9]+)\\.png$".r

  def checkFarTiles(tiles: List[(Coord, MapTile)], source: String) = {
    val coords = tiles.map(_._1)
    for (c1 <- coords) {
      val md = coords.filterNot(_ == c1).map(_.distance(c1)).min
      if (md > 5) {
        println("Tile %s in %s is %.0f tiles away from other tiles! This is probably bad data.".format(c1, source, md))
        sys.exit(-1)
      }
    }
  }

  def loadMPK(mpk: File): TileSet = {
    import scala.collection.JavaConversions._
    val zf = new ZipFile(mpk)
    val tiles = for {
      ent <- zf.entries().toList
      name <- mapTileName.findFirstMatchIn(ent.getName)
    } yield Coord(name.group(1).toInt, name.group(2).toInt) -> MapTileMPK(zf, ent)

    checkFarTiles(tiles, mpk.getAbsolutePath)

    val fpe = Option(zf.getEntry("fingerprints.txt"))
      .map(ent => FingerPrints.from(zf.getInputStream(ent)))
      .getOrElse(FingerPrints.nil)

    val fps =
      for {
        (coord, tile) <- tiles
        fp <- fpe.hashMap.get(tile.name)
      } yield fp -> coord

    TileSet(tiles.toMap, fps.toMap)
  }

  def load(dir: File, globFp: FingerPrints): Option[TileSet] = {
    val tiles =
      for {
        file <- dir.listFiles().toList if file.canRead && !file.isDirectory
        name <- mapTileName.findFirstMatchIn(file.getName)
      } yield Coord(name.group(1).toInt, name.group(2).toInt) -> MapTileFile(file)

    checkFarTiles(tiles, dir.getAbsolutePath)

    if (tiles.nonEmpty) {
      val lookup = globFp.mkLookup(dir.getName, FingerPrints.from(new File(dir, "fingerprints.txt")))
      val fps =
        for {
          (coord, tile) <- tiles
          fp <- lookup(tile.file.getName)
        } yield fp -> coord
      Some(TileSet(tiles.toMap, fps.toMap))
    } else {
      None
    }
  }
}