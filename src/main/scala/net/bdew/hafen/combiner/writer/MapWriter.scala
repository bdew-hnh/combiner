package net.bdew.hafen.combiner.writer

import java.io.File

import net.bdew.hafen.combiner.{Combiner, TileSet}

import scala.concurrent.Future

trait MapWriter {
  implicit val EC = Combiner.EC
  def doWriteAsync(path: File, ident: String, set: TileSet): Iterable[Future[Unit]]
}
