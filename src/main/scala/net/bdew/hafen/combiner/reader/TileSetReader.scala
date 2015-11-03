package net.bdew.hafen.combiner.reader

import java.io.File

import net.bdew.hafen.combiner.{Coord, FingerPrints, MapTile, TileSet}

trait TileSetReader {
  final val mapTileName = "^tile_(-?[0-9]+)_(-?[0-9]+)\\.png$".r

  def load(file: File, globFp: FingerPrints): Option[TileSet]

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
}

object TileSetReader {
  def load(file: File, globFp: FingerPrints) = {
    if (file.isDirectory)
      TileSetReaderFolder.load(file, globFp)
    else if (file.getName.endsWith(".mpk"))
      TileSetReaderMPK.load(file, globFp)
    else
      None
  }
}
