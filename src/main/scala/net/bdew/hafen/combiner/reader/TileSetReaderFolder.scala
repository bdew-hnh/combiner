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
