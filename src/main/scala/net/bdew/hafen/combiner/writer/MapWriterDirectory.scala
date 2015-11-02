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
