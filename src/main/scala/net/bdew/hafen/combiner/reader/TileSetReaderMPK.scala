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
