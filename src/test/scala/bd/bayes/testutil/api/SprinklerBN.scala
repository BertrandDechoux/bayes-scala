package bd.bayes.testutil.api

import dk.bayes.model.clustergraph.factor._
import dk.bayes.model.clustergraph.factor.Factor._
import dk.bayes.model.clustergraph.ClusterGraph
import bd.bayes.api.Bern

/**
 * Bayesian network example, borrowed from 'Adnan Darwiche. Modeling and Reasoning with Bayesian Networks, 2009' book.
 *
 */
object SprinklerBN {
  import bd.bayes.api.DiscreteBayesian
  import bd.bayes.api.DiscreteBayesian._
  implicit val network = DiscreteBayesian.network()

  val winter = P('winter) is Bern(0.8)
  val sprinkler = P('sprinkler | winter) is Bern(0.4, 0.45)
  val rain = P('rain | winter) is Bern(0.9, 0.7)
  val wetGrass = P('wetGrass | sprinkler x rain) is Bern(0.15, 0.7, 0.65, 0.45)
  val splippery = P('splippery | rain) is Bern(0.5, 0.6)

  val graph = network.createClusterGraph()
}
