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

import java.awt.RenderingHints
import java.io.File

import net.bdew.hafen.combiner.writer.{MapWriterDirectory, MapWriterMPK}

import scala.reflect.ClassTag

sealed trait Argument

case class ArgInput(name: String) extends Argument

abstract class Operation(val imgOut: Boolean = false, val mapOut: Boolean = false, val hasInputs: Boolean = false) extends Argument

abstract class OpBasic(mapOut: Boolean) extends Operation(imgOut = true, mapOut = mapOut, hasInputs = true)

case class OpMerge(dest: String) extends OpBasic(mapOut = true)

case class OpNormal() extends OpBasic(mapOut = false)

case class OpCombine(in1: String, coord1: Coord, in2: String, coord2: Coord, out: String) extends Operation(mapOut = true)

case class OpGMap(src: String, dest: String) extends Operation(imgOut = true, hasInputs = false)

case class OpImages(path: String) extends Operation(imgOut = true, mapOut = false, hasInputs = false)

case class ArgMinZoom(level: Int) extends Argument

case class ArgTileSize(size: Int) extends Argument

abstract class ArgInterpolation(val mode: AnyRef) extends Argument

case object InterpolNearest extends ArgInterpolation(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)

case object InterpolBilinear extends ArgInterpolation(RenderingHints.VALUE_INTERPOLATION_BILINEAR)

case object InterpolBicubic extends ArgInterpolation(RenderingHints.VALUE_INTERPOLATION_BICUBIC)

sealed trait Flag extends Argument {
  def v: Boolean
}

case class FlagGrid(v: Boolean) extends Flag

case class FlagTimer(v: Boolean) extends Flag

case class FlagCoords(v: Boolean) extends Flag

case class FlagImgOut(v: Boolean) extends Flag

case class FlagMpk(v: Boolean) extends Flag

case class FlagNullTiles(v: Boolean) extends Flag

class Args(args: List[Argument]) {
  lazy val operation = findArg[Operation].getOrElse(OpNormal())
  lazy val inputs = {
    val inp = findArgs[ArgInput] map (_.name)
    if (inp.isEmpty)
      List(new File("map"))
    else
      inp map (x => new File(x))
  }

  lazy val isEnabledCoords = getFlag[FlagCoords] getOrElse false
  lazy val isEnabledGrid = getFlag[FlagGrid] getOrElse false
  lazy val isEnabledTimer = getFlag[FlagTimer] getOrElse false
  lazy val isEnabledImgOut = getFlag[FlagImgOut] getOrElse operation.imgOut
  lazy val isEnabledMpk = getFlag[FlagMpk] getOrElse operation.mapOut
  lazy val isEnabledNullTiles = getFlag[FlagNullTiles] getOrElse false

  lazy val minZoom = findArg[ArgMinZoom].map(_.level).getOrElse(0)
  lazy val tileSize = findArg[ArgTileSize].map(_.size).getOrElse(1)

  lazy val interpolationMode = findArg[ArgInterpolation].map(_.mode).getOrElse(RenderingHints.VALUE_INTERPOLATION_BILINEAR)

  def getMapWriter =
    if (isEnabledMpk)
      MapWriterMPK
    else
      MapWriterDirectory

  def verify(): Unit = {
    if (findArgs[Operation].length > 1) Args.err("Multiple operation modes specified (--merge, --combine, --images)")
    if (!operation.hasInputs && findArgs[ArgInput].nonEmpty) Args.err("Current operation mode does not take inputs")
    if (!operation.imgOut && isEnabledCoords) Args.warn("Useless flag in current mode: --coords")
    if (!operation.imgOut && isEnabledGrid) Args.warn("Useless flag in current mode: --grid")
    if (!operation.mapOut && isEnabledMpk) Args.warn("Useless flag in current mode: --nompk")
    if (!operation.mapOut && !isEnabledImgOut) Args.warn("Useless flag in current mode: --noimg")
    if (!operation.isInstanceOf[OpGMap]) {
      if (isEnabledNullTiles) Args.warn("Useless flag in current mode: --nulltiles")
      if (findArgs[ArgInterpolation].nonEmpty) Args.warn("Useless flag in current mode: --interpolation")
      if (findArg[ArgMinZoom].nonEmpty) Args.warn("Useless flag in current mode: --minzoom")
      if (findArg[ArgTileSize].nonEmpty) Args.warn("Useless flag in current mode: --tilesize")
    }
    if (findArgs[ArgInterpolation].length > 1) Args.warn("multiple --interpolation flags will be ignored")
    if (findArgs[ArgMinZoom].length > 1) Args.warn("multiple --minzoom flags will be ignored")
  }

  private def findArgs[T: ClassTag]: List[T] = {
    val c = implicitly[ClassTag[T]].runtimeClass
    args.filter(c.isInstance).asInstanceOf[List[T]]
  }

  private def findArg[T: ClassTag]: Option[T] = findArgs[T].headOption

  private def getFlag[T <: Flag : ClassTag]: Option[Boolean] = findArg[T] map (_.v)
}

object IntParam {
  def unapply(s: String): Option[Int] =
    try {
      Some(s.toInt)
    } catch {
      case e: NumberFormatException => Args.err("Invalid number: '%s'", s)
    }
}

object Args {
  def warn(msg: String, params: String*) = {
    System.err.println("Warning: " + msg.format(params: _*))
  }

  def err(msg: String, params: String*) = {
    System.err.println("Error: " + msg.format(params: _*))
    sys.exit(1)
  }

  def parse(args: Array[String]) = {
    val parsed = new Args(realParse(args.toList))
    parsed.verify()
    parsed
  }

  def realParse(args: List[String]): List[Argument] = args match {
    case "--merge" :: path :: tail => OpMerge(path) +: realParse(tail)
    case "--images" :: path :: tail => OpImages(path) +: realParse(tail)

    case "--gmap" :: in :: out :: tail => OpGMap(in, out) +: realParse(tail)

    case "--combine" :: in1 :: IntParam(x1) :: IntParam(y1) :: in2 :: IntParam(x2) :: IntParam(y2) :: out :: tail =>
      OpCombine(in1, Coord(x1, y1), in2, Coord(x2, y2), out) +: realParse(tail)

    case "--noimg" :: tail => FlagImgOut(false) +: realParse(tail)
    case "--nompk" :: tail => FlagMpk(false) +: realParse(tail)
    case "--coords" :: tail => FlagCoords(true) +: realParse(tail)
    case "--grid" :: tail => FlagGrid(true) +: realParse(tail)
    case "--time" :: tail => FlagTimer(true) +: realParse(tail)

    case "--nulltiles" :: tail => FlagNullTiles(true) +: realParse(tail)

    case "--interpolation" :: str :: tail =>
      (str match {
        case "nearest" => InterpolNearest
        case "bilinear" => InterpolBilinear
        case "bicubic" => InterpolBicubic
        case s => err("Invalid interpolation mode: '%s'", s)
      }) +: realParse(tail)

    case "--minzoom" :: IntParam(level) :: tail =>
      ArgMinZoom(level) +: realParse(tail)

    case "--tilesize" :: IntParam(size) :: tail =>
      ArgTileSize(size) +: realParse(tail)

    case str :: tail if str.startsWith("--") =>
      err("Invalid flag '%s'", str)

    case str :: tail => ArgInput(str) +: realParse(tail)

    case nil => List.empty
  }
}
