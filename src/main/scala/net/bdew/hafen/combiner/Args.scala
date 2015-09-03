package net.bdew.hafen.combiner

case class Args(inputs: List[String], merge: Option[String])

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

    case str :: tail =>
      val rest = realParse(tail)
      rest.copy(inputs = rest.inputs :+ str)

    case nil => Args(List.empty, None)
  }
}
