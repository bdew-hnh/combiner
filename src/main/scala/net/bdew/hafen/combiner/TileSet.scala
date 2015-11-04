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

trait BaseTileSet {
  val tiles: Map[Coord, MapTile]
  def minX: Int
  def maxX: Int
  def minY: Int
  def maxY: Int
}

case class SimpleTileSet(tiles: Map[Coord, MapTile], size: Int) extends BaseTileSet {
  override def minX = 0
  override def minY = 0
  override def maxX = size
  override def maxY = size
}

case class TileSet(tiles: Map[Coord, MapTile], fingerPrints: Map[String, Coord]) extends BaseTileSet {
  override lazy val minX = tiles.keys.map(_.x).min
  override lazy val maxX = tiles.keys.map(_.x).max
  override lazy val minY = tiles.keys.map(_.y).min
  override lazy val maxY = tiles.keys.map(_.y).max
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
