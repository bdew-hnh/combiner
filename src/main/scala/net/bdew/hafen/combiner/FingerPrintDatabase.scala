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

import java.io.{BufferedReader, File, FileReader}

class FingerPrintDatabase(dataFile: File) {
  val hashMap: Map[String, String] = if (dataFile.canRead && dataFile.isFile) {
    load()
  } else {
    println(" ! Fingerprint database does not exist or is not readable, skipping")
    Map.empty
  }

  val inverted = hashMap.map(_.swap).groupBy(_._1).mapValues(_.values.toSet)
  println(" * Loaded %d fingerprints".format(hashMap.size))

  def load(): Map[String, String] = {
    println(" * Loading fingerprints from %s ...".format(dataFile))
    val reader = new BufferedReader(new FileReader(dataFile))
    Iterator.continually(reader.readLine())
      .takeWhile(_ != null)
      .map(_.split(":"))
      .filter(_.size > 1)
      .map(x => x(0) -> x(1))
      .toMap
  }
}
