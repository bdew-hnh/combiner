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

package net.bdew.hafen.combiner.writer

import java.io.{File, FileWriter}
import java.nio.file.Files

import net.bdew.hafen.combiner.TileSet

import scala.concurrent.Future

object MapWriterDirectory extends MapWriter {
  override def doWriteAsync(path: File, ident: String, set: TileSet) = {
    val dir = new File(path, ident)
    dir.mkdirs()
    val reverseFp = set.fingerPrints.map(_.swap)
    val fpWriter = new FileWriter(new File(dir, "fingerprints.txt"))
    try {
      for ((coord, tile) <- set.tiles) yield {
        val relocated = coord - set.origin
        if (reverseFp.contains(coord))
          fpWriter.write("tile_%d_%d.png:%s\n".format(relocated.x, relocated.y, reverseFp(coord)))
        val file = new File(dir, "tile_%d_%d.png".format(relocated.x, relocated.y))
        Future[Unit] {
          Files.copy(tile.makeInputStream(), file.toPath)
          file.setLastModified(tile.lastModified)
        }
      }
    } finally {
      fpWriter.close()
    }
  }
}
