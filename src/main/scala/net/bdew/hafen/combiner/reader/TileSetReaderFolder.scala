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

import net.bdew.hafen.combiner.{Coord, FingerPrints, MapTileFile, TileSet}

object TileSetReaderFolder extends TileSetReader {
  override def load(dir: File, globFp: FingerPrints): Option[TileSet] = {
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
