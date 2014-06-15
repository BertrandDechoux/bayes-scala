package bd.bayes.testutil.api

import dk.bayes.model.clustergraph.factor._
import dk.bayes.model.clustergraph.factor.Factor._
import dk.bayes.model.clustergraph.ClusterGraph
import bd.bayes.api.Bern
import bd.bayes.api.Raw

/**
 * Bayesian network example, borrowed from 'Daphne Koller, Nir Friedman. Probabilistic Graphical Models, Principles and Techniques, 2009' book.
 *
 */
object StudentBN {
  import bd.bayes.api.DiscreteBayesian
  import bd.bayes.api.DiscreteBayesian._
  implicit val network = DiscreteBayesian.network()

  val difficulty = P('difficulty) is Bern(0.4)
  val intelligence = P('intelligence) is Bern(0.3)
  val grade = P('grade | intelligence x difficulty) is Raw(3)(0.3, 0.4, 0.3, 0.05, 0.25, 0.7, 0.9, 0.08, 0.02, 0.5, 0.3, 0.2)
  val sat = P('sat | intelligence) is Bern(0.05, 0.8)
  val letter = P('letter | grade) is Bern(0.9, 0.6, 0.01)

  val graph = network.createClusterGraph()
}