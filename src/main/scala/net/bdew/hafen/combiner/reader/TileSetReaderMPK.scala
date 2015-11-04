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

package net.bdew.hafen.combiner.reader

import java.io.File
import java.util.zip.ZipFile

import net.bdew.hafen.combiner.{Coord, FingerPrints, MapTileMPK, TileSet}

object TileSetReaderMPK extends TileSetReader {
  override def load(mpk: File, globFp: FingerPrints): Option[TileSet] = {

    if (globFp != FingerPrints.nil)
      println("Warning: MPK reader does not support global fingerprints")

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

    Some(TileSet(tiles.toMap, fps.toMap))
  }
}
