package net.bdew.hafen.combiner

import java.io.File

import scala.annotation.tailrec

class InputSet(val tileSets: List[TileSet], val fingerPrints: FingerPrintDatabase) {
  def merge(that: InputSet) = new InputSet(tileSets ++ that.tileSets, fingerPrints.merge(that.fingerPrints))

  lazy val tileFpMap = {
    val (found, missing) = tileSets.flatMap(_.tiles.values).partition(
      f => fingerPrints.hashMap.isDefinedAt(f.name)
    )

    if (found.nonEmpty)
      println(" * Found fingerprints for %d tiles".format(found.size))

    if (missing.nonEmpty)
      println(" ! Missing fingerprints for %d tiles".format(missing.size))

    found.map(t => t -> fingerPrints.hashMap(t.name)).toMap
  }

  def mergeTiles = doMergeAll(tileSets)

  sealed trait MergeResult

  case class MergeMore(sets: List[TileSet]) extends MergeResult

  case class MergeDone(sets: List[TileSet]) extends MergeResult

  @tailrec
  private def doMergeAll(sets: List[TileSet]): List[TileSet] = {
    print(" * Merging, %d sets remaining... ".format(sets.size))
    doMerge(sets) match {
      case MergeDone(set) => set
      case MergeMore(set) => doMergeAll(set)
    }
  }

  private def doMerge(sets: List[TileSet]): MergeResult = {
    for {
      set1 <- sets
      (coord1, tile1) <- set1.tiles
      fp1 <- tileFpMap.get(tile1)
      set2 <- sets if set2 != set1
      (coord2, tile2) <- set2.tiles
      fp2 <- tileFpMap.get(tile2)
      if fp1 == fp2
    } {
      val delta = coord2 - coord1
      println("Found match: %s <> %s D=(%d,%d)".format(tile1.name, tile2.name, delta.x, delta.y))
      val newSet = TileSet(set1.tiles ++ set2.tiles.map({ case (c, m) => c - delta -> m }))
      return MergeMore(sets.filterNot(x => x == set1 || x == set2) :+ newSet)
    }
    println("No more matches")
    MergeDone(sets)
  }
}

object InputSet {
  final val mapTileName = "^tile_(-?[0-9]+)_(-?[0-9]+)\\.png$".r

  def load(path: File) = {
    println(" * Reading input directory: " + path.getAbsolutePath)
    if (!path.isDirectory || !path.canRead) sys.error("Unable to read source: %s".format(path.getAbsolutePath))
    val tileSets = {
      val res = for (dir <- path.listFiles() if dir.canRead && dir.isDirectory) yield {
        val tiles = for {
          file <- dir.listFiles() if file.canRead && !file.isDirectory
          name <- mapTileName.findFirstMatchIn(file.getName)
        } yield Coord(name.group(1).toInt, name.group(2).toInt) -> MapTile(file, "%s/%s".format(dir.getName, file.getName))
        println(" * Found directory %s with %d images".format(dir.getName, tiles.length))
        TileSet(tiles.toMap)
      }
      res.filterNot(_.tiles.isEmpty).toList
    }
    val fp = FingerPrintDatabase.from(new File(path, "fingerprints.txt"))
    new InputSet(tileSets, fp)
  }
}
