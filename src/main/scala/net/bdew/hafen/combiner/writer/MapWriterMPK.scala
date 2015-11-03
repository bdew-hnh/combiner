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
    val zipStream = new ZipOutputStream(new FileOutputStream(new File(path, ident + ".mpk")))
    val crc = new CRC32()
    try {
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
        Utils.fullyRead(Channels.newChannel(tile.makeInputStream()), buf)
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
    } finally {
      zipStream.close()
    }
  }))
}
