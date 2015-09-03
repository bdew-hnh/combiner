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
import java.io.File
import javax.imageio.ImageIO

case class TileSet(tiles: Map[Coord, MapTile]) {
  lazy val minX = tiles.keys.map(_.x).min
  lazy val maxX = tiles.keys.map(_.x).max
  lazy val minY = tiles.keys.map(_.y).min
  lazy val maxY = tiles.keys.map(_.y).max
  lazy val width = maxX - minX + 1
  lazy val height = maxY - minY + 1
  lazy val origin = Coord(minX, minY)

  def saveCombined(output: File, grid: Boolean): Unit = {
    val result = new BufferedImage(width * Combiner.TILE_SIZE, height * Combiner.TILE_SIZE, BufferedImage.TYPE_INT_ARGB)
    val g = result.getGraphics
    for ((c, tile) <- tiles) {
      val ct = c - origin
      g.drawImage(tile.getImage, ct.x * Combiner.TILE_SIZE, ct.y * Combiner.TILE_SIZE, null)
      if (grid && (ct.x > 0 || ct.y > 0)) {
        g.drawLine(ct.x * Combiner.TILE_SIZE, ct.y * Combiner.TILE_SIZE, (ct.x + 1) * Combiner.TILE_SIZE, ct.y * Combiner.TILE_SIZE)
        g.drawLine(ct.x * Combiner.TILE_SIZE, ct.y * Combiner.TILE_SIZE, ct.x * Combiner.TILE_SIZE, (ct.y + 1) * Combiner.TILE_SIZE)
      }
    }
    ImageIO.write(result, "png", output)
  }
}
