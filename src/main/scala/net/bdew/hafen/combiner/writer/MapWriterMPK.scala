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

import java.io.{ByteArrayOutputStream, File, FileOutputStream, OutputStreamWriter}
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.attribute.FileTime
import java.util.zip.{CRC32, ZipEntry, ZipOutputStream}

import net.bdew.hafen.combiner.{TileSet, Utils}

import scala.concurrent.Future

object MapWriterMPK extends MapWriter {
  override def doWriteAsync(path: File, ident: String, set: TileSet) = List(Future({
    val crc = new CRC32()
    Utils.withResource(new ZipOutputStream(new FileOutputStream(new File(path, ident + ".mpk")))) { zipStream =>
      val reverseFp = set.fingerPrints.map(_.swap)
      val fpBytes = new ByteArrayOutputStream()
      val fpWriter = new OutputStreamWriter(fpBytes)
      val zipChan = Channels.newChannel(zipStream)
      zipStream.setMethod(ZipOutputStream.STORED)
      zipStream.setLevel(0)
      for ((coord, tile) <- set.tiles) yield {
        val relocated = coord - set.origin
        if (reverseFp.contains(coord))
          fpWriter.write("tile_%d_%d.png:%s\n".format(relocated.x, relocated.y, reverseFp(coord)))
        val buf = ByteBuffer.allocate(tile.size)
        Utils.withResource(tile.makeInputStream()) { input =>
          Utils.fullyRead(Channels.newChannel(input), buf)
        }
        buf.flip()
        crc.reset()
        crc.update(buf)
        buf.rewind()
        val zipEntry = new ZipEntry("tile_%d_%d.png".format(relocated.x, relocated.y))
        zipEntry.setCrc(crc.getValue)
        zipEntry.setSize(tile.size)
        zipEntry.setCompressedSize(tile.size)
        zipEntry.setLastModifiedTime(FileTime.fromMillis(tile.lastModified))
        zipStream.putNextEntry(zipEntry)
        Utils.fullyWrite(zipChan, buf)
        zipStream.closeEntry()
      }
      fpWriter.flush()
      zipStream.setMethod(ZipOutputStream.DEFLATED)
      zipStream.setLevel(9)
      zipStream.putNextEntry(new ZipEntry("fingerprints.txt"))
      zipStream.write(fpBytes.toByteArray)
      zipStream.closeEntry()
    }
  }))
}
