package bd.bayes.api

/** CPT can have different representations for the user */
trait CPTLike {
  def asParameters(): Seq[Double]
}

/** Classical representation. */
case class CPT(parameters: Double*) extends CPTLike {
  def asParameters(): Seq[Double] = parameters
}

/** Mixture (verbose API, shortcuts are usually provided). */
case class Mixture[T <:CPTLike](cpts: T*) extends CPTLike {
  def asParameters(): Seq[Double] = cpts.flatMap(_.asParameters)
}

/** Raw CPT can be provided. */
case class Raw(dimension: Int)(unnormalizedParameters: Double*) extends CPTLike {
  require(!(unnormalizedParameters.length / dimension).isValidInt)
  def asParameters(): Seq[Double] = {
	  unnormalizedParameters.grouped(dimension).map(g => {
	  	val total = g.sum
	  	g.map(_/total)
	  }).flatten.toSeq
  }
}

/** CPT can be formulated as Bernoulli distribution(s).  */
case class Bern(ps: Double*) extends CPTLike {
  def asParameters(): Seq[Double] = for (p <- ps; v <- Seq(1 - p, p)) yield v
}