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

import java.io.File

object Combiner {
  final val TILE_SIZE = 100

  def main(params: Array[String]): Unit = {
    val args = Args.parse(params)
    val inputs =
      if (args.inputs.isEmpty)
        List(new File("map"))
      else
        args.inputs.map(new File(_))

    val inputSets = inputs map InputSet.load

    val inputSet =
      if (inputSets.isEmpty) {
        println("No valid inputs")
        sys.exit()
      } else if (inputSets.size > 1) {
        inputSets.tail.foldRight(inputSets.head)(_.merge(_))
      } else {
        inputSets.head
      }

    if (args.merge.isDefined) {
      sys.error("Not implemented")
    } else {
      val merged = inputSet.mergeTiles

      for ((t, i) <- merged.zipWithIndex) {
        println(" + Writing set #%d with %d images to combined_%d.png".format(i, t.tiles.size, i))
        t.saveCombined(new File(inputs.head, "combined_%d.png".format(i)))
      }
    }
  }
}
