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

package net.bdew.hafen.combiner.operations

import java.awt.image.BufferedImage
import java.awt.{Color, RenderingHints}
import java.io.File
import javax.imageio.ImageIO

import net.bdew.hafen.combiner._
import net.bdew.hafen.combiner.reader.TileSetReader

import scala.annotation.tailrec
import scala.concurrent.Future

class GMap(args: Args) {
  implicit val EC = Combiner.EC

  final val szSource = Combiner.TILE_SIZE
  final val szGen = Combiner.TILE_SIZE * args.tileSize
  final val tileSize = args.tileSize
  final val nullTile = new NullTile(szGen)

  def run(op: OpGMap, timer: Timer): Unit = {
    timer.mark("Start")

    val srcFile = new File(op.src)
    val destDir = new File(op.dest)

    if (!srcFile.exists() || !srcFile.canRead) {
      println("Input doesn't exist or is not readable, aborting!")
      sys.exit(1)
    }

    if (destDir.exists()) {
      println("Output already exists, aborting!")
      sys.exit(1)
    }

    println("* Input: " + srcFile.getAbsolutePath)
    println("* Output: " + destDir.getAbsolutePath)
    println("* Tile Size: %d x %d".format(szGen, szGen))
    println("* Loading...")

    val tiles = TileSetReader.load(srcFile, FingerPrints.nil) getOrElse {
      println("! Failed to load input")
      sys.exit(-1)
    }

    timer.mark("LOAD INPUT")

    val maxZoomLevel = (Math.log(Math.max(tiles.width / tileSize, tiles.height / tileSize)) / Math.log(2)).ceil.toInt + 1
    val size = 1 << maxZoomLevel

    println("* Max Zoom: %d (%d x %d)".format(maxZoomLevel, size, size))

    val delta = Coord(size / 2 * tileSize - tiles.width / 2, size / 2 * tileSize - tiles.height / 2)

    val baseLevelDir = new File(destDir, maxZoomLevel.toString)

    val baseLevel =
      Async("Generating base tiles") {
        generateBaseLevel(baseLevelDir, tiles, size, delta)
      } waitUntilDone()

    generateNextZoom(SimpleTileSet(baseLevel.toMap, size), destDir, maxZoomLevel - 1, timer)

    timer.mark("DONE")
  }

  private def generateBaseLevel(baseLevelDir: File, tiles: TileSet, size: Int, delta: Coord) = {
    (0 until size).flatMap { x =>
      val xDir = new File(baseLevelDir, x.toString)
      xDir.mkdirs()
      (0 until size).flatMap { y =>
        val out = new File(baseLevelDir, "%d/%d.png".format(x, y))

        // Find all existing source tiles
        val toDraw = for (xo <- 0 until tileSize; yo <- 0 until tileSize; tile <- tiles.tiles.get(new Coord(x * tileSize + xo - delta.x, y * tileSize + yo - delta.y)) if tile != nullTile) yield (xo, yo, tile)

        if (toDraw.isEmpty) {
          // If no source tiles exist skip all the drawing stuff
          if (args.isEnabledNullTiles) {
            // Generate empty tile if needed
            Some(Future {
              nullTile.copyTo(out)
              Coord(x, y) -> nullTile
            })
          } else {
            // Otherwise - nothing to do
            None
          }
        } else {
          Some(Future {
            if (tileSize == 1 && !args.isEnabledGrid && !args.isEnabledCoords) {
              // If we are generating 1x1 tiles and don't need to draw - just copy the source
              val (_, _, tile) = toDraw.head
              tile.copyTo(out)
              Coord(x, y) -> tile
            } else {
              // prepare image
              val img = new BufferedImage(szGen, szGen, BufferedImage.TYPE_INT_ARGB)
              val gr = img.getGraphics
              gr.setColor(Color.WHITE)
              for ((xo, yo, tile) <- toDraw) {
                // copy tiles into image
                gr.drawImage(tile.readImage(), xo * szSource, yo * szSource, null)

                // draw coords and grid if enabled
                if (args.isEnabledCoords) {
                  gr.drawString("(%d,%d)".format(x * tileSize - delta.x + xo, y * tileSize - delta.y + yo), xo * szSource + 3, yo * szSource + 10)
                }
                if (args.isEnabledGrid) {
                  if (tiles.tiles.isDefinedAt(Coord(x * tileSize - delta.x + xo, y * tileSize - 1 - delta.y + yo)))
                    gr.drawLine(xo * szSource, yo * szSource, (xo + 1) * szSource, yo * szSource)
                  if (tiles.tiles.isDefinedAt(Coord(x * tileSize - 1 - delta.x + xo, y * tileSize - delta.y + yo)))
                    gr.drawLine(xo * szSource, yo * szSource, xo * szSource, (yo + 1) * szSource)
                }
              }
              gr.dispose()
              // save image and return it for next layer
              ImageIO.write(img, "png", out)
              Coord(x, y) -> MapTileFile(out)
            }
          })
        }
      }
    }
  }

  @tailrec
  private def generateNextZoom(tiles: SimpleTileSet, dest: File, thisZoom: Int, timer: Timer): Unit = {
    timer.mark("GENERATE ZOOM %d".format(thisZoom + 1))
    val next = Async("Generating zoom level %d".format(thisZoom)) {
      generateTiles(tiles, dest, thisZoom)
    } waitUntilDone()

    if (thisZoom > args.minZoom)
      generateNextZoom(SimpleTileSet(next.toMap, tiles.size / 2), dest, thisZoom - 1, timer)
  }

  private def generateTiles(tiles: BaseTileSet, dest: File, zl: Int) = {
    val dir = new File(dest, zl.toString)
    (for (x <- tiles.minX / 2 until tiles.maxX / 2) yield {
      val xDir = new File(dir, x.toString)
      xDir.mkdirs()
      for (y <- tiles.minY / 2 until tiles.maxY / 2) yield {
        // Find all existing tiles from previous level
        val toDraw = for (xo <- 0 to 1; yo <- 0 to 1; tile <- tiles.tiles.get(new Coord(x * 2 + xo, y * 2 + yo)) if tile != nullTile) yield (xo, yo, tile)
        val out = new File(xDir, y + ".png")
        if (toDraw.isEmpty) {
          // No tiles were found
          if (args.isEnabledNullTiles) {
            // write null tile (if enabled) and return it for next layer
            Some(Future {
              nullTile.copyTo(out)
              Coord(x, y) -> nullTile
            })
          } else {
            // No need for null tile, nothing to do here
            None
          }
        } else Some(Future {
          // Prepare full size image
          val img = new BufferedImage(szGen * 2, szGen * 2, BufferedImage.TYPE_INT_ARGB)
          val graphics = img.getGraphics

          // Copy tiles from previous layer to full size image
          for ((xo, yo, tile) <- toDraw)
            graphics.drawImage(tile.readImage(), xo * szGen, yo * szGen, null)

          graphics.dispose()

          // prepare final image
          val scaled = new BufferedImage(szGen, szGen, BufferedImage.TYPE_INT_ARGB)
          val scaledGraphics = scaled.createGraphics()

          // resize full size image to final size
          scaledGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, args.interpolationMode)
          scaledGraphics.drawImage(img, 0, 0, szGen, szGen, null)

          // save final size image
          ImageIO.write(scaled, "png", out)
          Coord(x, y) -> MapTileFile(out)
        })
      }
    }).flatten.flatten
  }
}
