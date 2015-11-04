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

import java.io.File

import net.bdew.hafen.combiner._
import net.bdew.hafen.combiner.writer.CombinedWriter

import scala.concurrent.Future

object Images {
  implicit val EC = Combiner.EC
  def run(op: OpImages, args: Args, timer: Timer): Unit = {
    val input = new File(op.path)
    val tileSets = InputSet.loadSingle(input)

    timer.mark("Load")

    Async("Saving Images") {
      for ((n, t) <- tileSets) yield Future {
        CombinedWriter.saveCombined(t, new File(input, n.getName + ".png"), args.isEnabledGrid, args.isEnabledCoords)
      }
    } waitUntilDone()

    timer.mark("Write Images")
  }
}
