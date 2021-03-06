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

import java.io._

class FingerPrints(val hashMap: Map[String, String]) {
  def mkLookup(dirName: String, local: FingerPrints) =
    (name: String) => local.hashMap.get(name) orElse this.hashMap.get("%s/%s".format(dirName, name))
}

object FingerPrints {
  val nil = new FingerPrints(Map.empty)

  def from(dataFile: File): FingerPrints =
    if (dataFile.canRead && dataFile.isFile)
      Utils.withResource(new FileInputStream(dataFile)) { stream =>
        from(stream)
      }
    else nil

  def from(stream: InputStream): FingerPrints = {
    Utils.withResource(new BufferedReader(new InputStreamReader(stream))) { reader =>
      val data = Iterator.continually(reader.readLine())
        .takeWhile(_ != null)
        .map(_.split(":"))
        .filter(_.size > 1)
        .filterNot(x => BadHashes.bad.contains(x(1)))
        .map(x => x(0) -> x(1))
        .toMap
      new FingerPrints(data)
    }
  }
}


