package bd.bayes.testutil.api

import dk.bayes.model.clustergraph.factor._
import dk.bayes.model.clustergraph.factor.Factor._
import dk.bayes.model.clustergraph.ClusterGraph

/**
 * Bayesian network example, borrowed from 'Adnan Darwiche. Modeling and Reasoning with Bayesian Networks, 2009' book.
 *
 */
object SprinklerBN {
  import bd.bayes.api.DiscreteBayesian
  import bd.bayes.api.DiscreteBayesian._
  implicit val network = DiscreteBayesian.network()

  val winter = P('winter) follows (0.2, 0.8)
  val sprinkler = P('sprinkler | winter) follows (0.6, 0.4, 0.55, 0.45)
  val rain = P('rain | winter) follows (0.1, 0.9, 0.3, 0.7)
  val wetGrass = P('wetGrass | sprinkler x rain) follows (0.85, 0.15, 0.3, 0.7, 0.35, 0.65, 0.55, 0.45)
  val splippery = P('splippery | rain) follows (0.5, 0.5, 0.4, 0.6)

  val graph = network.createClusterGraph()
}
