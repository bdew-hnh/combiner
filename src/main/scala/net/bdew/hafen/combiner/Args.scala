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

case class Args(inputs: List[String],
                merge: Option[String],
                grid: Boolean,
                timer: Boolean,
                imgOut: Boolean,
                autoMerge: Boolean,
                coords: Boolean
                 )

object Args {
  def parse(args: Array[String]) = realParse(args.toList)
  def realParse(args: List[String]): Args = args match {
    case "--merge" :: merge :: tail =>
      val rest = realParse(tail)
      if (rest.merge.isDefined) {
        println("--merge can't be used multiple times")
        sys.exit()
      }
      rest.copy(merge = Some(merge))

    case "--noimg" :: tail => realParse(tail).copy(imgOut = false)
    case "--nomerge" :: tail => realParse(tail).copy(autoMerge = false)
    case "--coords" :: tail => realParse(tail).copy(coords = true)
    case "--grid" :: tail => realParse(tail).copy(grid = true)
    case "--time" :: tail => realParse(tail).copy(timer = true)

    case str :: tail =>
      val rest = realParse(tail)
      rest.copy(inputs = str +: rest.inputs)

    case nil => Args(List.empty, merge = None, grid = false, timer = false, imgOut = true, autoMerge = true, coords = false)
  }
}
