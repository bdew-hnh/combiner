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
import java.io._
import java.nio.file.Files
import java.util.zip.{ZipEntry, ZipFile}
import javax.imageio.ImageIO

trait MapTile {
  def name: String
  def lastModified: Long
  def size: Int
  def makeInputStream(): InputStream
  def readImage(): BufferedImage
  def copyTo(dest: File): Unit
}

class NullTile(sz: Int) extends MapTile {
  lazy val nullImg = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB)
  lazy val nullBytes = {
    val bos = new ByteArrayOutputStream()
    ImageIO.write(nullImg, "png", bos)
    bos.toByteArray
  }

  override def name = "<null>"
  override def readImage() = nullImg
  override def makeInputStream() = new ByteArrayInputStream(nullBytes)
  override def lastModified = Long.MinValue
  override def size = nullBytes.length

  override def copyTo(out: File) =
    Utils.withResource(new FileOutputStream(out)) { fs =>
      fs.write(nullBytes)
    }
}

case class MapTileFile(file: File) extends MapTile {
  override lazy val name = file.getAbsolutePath
  override lazy val lastModified = file.lastModified()
  override lazy val size = file.length().toInt
  override def makeInputStream() = new FileInputStream(file)
  override def readImage() = ImageIO.read(file)

  override def copyTo(dest: File) = {
    Utils.withResource(makeInputStream()) { input =>
      Files.copy(input, dest.toPath)
    }
  }
}

case class MapTileMPK(zipFile: ZipFile, ent: ZipEntry) extends MapTile {
  override lazy val name = ent.getName
  override lazy val lastModified = ent.getLastModifiedTime.toMillis
  override lazy val size = ent.getSize.toInt
  override def makeInputStream() = zipFile.getInputStream(ent)
  override def readImage() =
    Utils.withResource(makeInputStream()) {
      stream => ImageIO.read(stream)
    }

  override def copyTo(dest: File) = {
    Utils.withResource(makeInputStream()) { input =>
      Files.copy(input, dest.toPath)
    }
  }
}


