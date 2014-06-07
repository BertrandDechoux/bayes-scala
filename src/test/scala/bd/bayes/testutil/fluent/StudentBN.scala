package bd.bayes.testutil.fluent

import dk.bayes.model.clustergraph.factor._
import dk.bayes.model.clustergraph.factor.Factor._
import dk.bayes.model.clustergraph.ClusterGraph

/**
 * Bayesian network example, borrowed from 'Daphne Koller, Nir Friedman. Probabilistic Graphical Models, Principles and Techniques, 2009' book.
 *
 */
object StudentBN {
  val bnb = bd.bayes.fluent.Api.buildNetwork()

  val difficulty = bnb.addNode('difficulty)(0.6, 0.4)
  val intelligence = bnb.addNode('intelligence)(0.7, 0.3)
  val grade = bnb.addNode('grade, intelligence, difficulty)(FactorUtil.normalise(Array(0.3, 0.4, 0.3, 0.05, 0.25, 0.7, 0.9, 0.08, 0.02, 0.5, 0.3, 0.2)):_*)
  val sat = bnb.addNode('sat, intelligence)(0.95, 0.05, 0.2, 0.8)
  val letter = bnb.addNode('letter, grade)(0.1, 0.9, 0.4, 0.6, 0.99, 0.01)

  val graph = bnb.createClusterGraph()
}