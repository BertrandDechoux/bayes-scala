package bd.bayes.testutil.fluent

import dk.bayes.model.clustergraph.factor._
import dk.bayes.model.clustergraph.factor.Factor._
import dk.bayes.model.clustergraph.ClusterGraph

/**
 * Bayesian network example, borrowed from 'Adnan Darwiche. Modeling and Reasoning with Bayesian Networks, 2009' book.
 *
 */
object SprinklerBN {
  val bnb = bd.bayes.fluent.Api.buildNetwork()

  val winter = bnb.addNode('winter)(0.2, 0.8)
  val sprinkler = bnb.addNode('sprinkler, winter)(0.6, 0.4, 0.55, 0.45)
  val rain = bnb.addNode('rain, winter)(0.1, 0.9, 0.3, 0.7)
  val wetGrass = bnb.addNode('wetGrass, sprinkler, rain)(0.85, 0.15, 0.3, 0.7, 0.35, 0.65, 0.55, 0.45)
  val splippery = bnb.addNode('splippery, rain)(0.5, 0.5, 0.4, 0.6)

  val graph = bnb.createClusterGraph()
}