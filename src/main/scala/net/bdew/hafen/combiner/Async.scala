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

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class Async[T](name: String, futures: List[Future[T]])(implicit EC: ExecutionContext) {
  lazy val length = futures.length
  def completed = futures.count(_.isCompleted)

  def waitUntilDone() = {
    var last = completed
    println("* %s... %d/%d".format(name, last, length))
    while (completed < length) {
      val now = completed
      if (now > last) {
        println("* %s... %d/%d".format(name, completed, length))
        last = now
      }
      Thread.sleep(100)
    }
    println("* %s... Done!".format(name))
    result
  }

  def result = Await.result(Future.sequence(futures), Duration.Inf)
}

object Async {
  def apply[T](name: String)(f: => List[Future[T]])(implicit EC: ExecutionContext) = {
    new Async(name, f)
  }
}