package net.bdew.hafen.combiner.writer

import java.awt.Color
import java.io.File
import java.util

import ar.com.hjg.pngj.{ImageInfo, PngWriter}
import net.bdew.hafen.combiner.{Combiner, Coord, TileSet}

object CombinedWriter {
  def saveCombined(tiles: TileSet, output: File, grid: Boolean, coords: Boolean): Unit = {
    val imageInfo = new ImageInfo(tiles.width * Combiner.TILE_SIZE, tiles.height * Combiner.TILE_SIZE, 8, true)
    val writer = new PngWriter(output, imageInfo, true)
    writer.getPixelsWriter.setDeflaterCompLevel(1)
    val data = new Array[Int](tiles.width * Combiner.TILE_SIZE * 4)
    val rgb = new Array[Int](Combiner.TILE_SIZE)

    for (y <- tiles.minY to tiles.maxY) {
      val images = (
        (tiles.minX to tiles.maxX) map { x =>
          x -> tiles.tiles.get(new Coord(x, y))
        } collect { case (x, Some(tile)) =>
          val img = tile.readImage()
          if (grid || coords) {
            val gr = img.getGraphics
            if (coords) {
              gr.drawString("(%d,%d)".format(x - tiles.minX, y - tiles.minY), 3, 10)
            }
            if (grid) {
              gr.setColor(Color.WHITE)
              if (tiles.tiles.isDefinedAt(new Coord(x, y - 1)))
                gr.drawLine(0, 0, Combiner.TILE_SIZE, 0)
              if (tiles.tiles.isDefinedAt(new Coord(x - 1, y)))
                gr.drawLine(0, 0, 0, Combiner.TILE_SIZE)
            }
            gr.dispose()
          }
          x -> img
        }).toMap

      for (iy <- 0 until Combiner.TILE_SIZE) {
        util.Arrays.fill(data, 0)
        for (x <- tiles.minX to tiles.maxX; image <- images.get(x) if image.getHeight > iy) {
          if (image.getWidth >= Combiner.TILE_SIZE)
            image.getRGB(0, iy, Combiner.TILE_SIZE, 1, rgb, 0, 0)
          else
            image.getRGB(0, iy, image.getWidth, 1, rgb, 0, 0)
          for (ix <- 0 until Combiner.TILE_SIZE) {
            val idx = ((x - tiles.minX) * Combiner.TILE_SIZE + ix) * 4
            val sample = rgb(ix)
            data(idx) = (sample >> 16) & 0xFF
            data(idx + 1) = (sample >> 8) & 0xFF
            data(idx + 2) = sample & 0xFF
            data(idx + 3) = (sample >> 24) & 0xFF
          }
        }
        writer.writeRowInt(data)
      }
    }
    writer.close()
  }
}
