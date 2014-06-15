package bd.bayes.testutil.api

import dk.bayes.model.clustergraph.ClusterGraph
import dk.bayes.model.clustergraph.factor._
import bd.bayes.api.CPT
import bd.bayes.api.Mixture
import bd.bayes.api.Bern

object TennisDBN {
  import bd.bayes.api.DiscreteBayesian
  import bd.bayes.api.DiscreteBayesian._
  implicit val network = DiscreteBayesian.network()

  val priorGroup = network.createNodeGroup()
  val emissionGroup = network.createNodeGroup()
  val transitionGroup = network.createNodeGroup()

  val priorParam = CPT(0.2, 0.5, 0.3)
  val emissionParam = Mixture(Bern(0.5, 2d / 3, 0.75), Bern(1d / 3, 0.5, 3d / 5), Bern(1d / 4, 2d / 5, 0.5))
  val transitionParam = Mixture(CPT(0.98, 0.01, 0.01), CPT(0.01, 0.98, 0.01), CPT(0.01, 0.02, 0.97))

  // player 1
  val player1_t0 = P('player1_t0) in priorGroup is priorParam
  val player1_t1 = P('player1_t1 | player1_t0) in transitionGroup is transitionParam
  val player1_t2 = P('player1_t2 | player1_t1) in transitionGroup is transitionParam

  // player 2
  val player2_t0 = P('player2_t0) in priorGroup is priorParam
  val player2_t1 = P('player2_t1 | player2_t0) in transitionGroup is transitionParam
  val player2_t2 = P('player2_t2 | player2_t1) is transitionParam // no group

  // player 3
  val player3_t1 = P('player3_t1) in transitionGroup is priorParam
  val player3_t2 = P('player3_t2 | player3_t1) in transitionGroup is transitionParam

  // matches
  val match1v2_t0 = P('match1v2_t0 | player1_t0 x player2_t0) in emissionGroup is emissionParam

  val match1v2_t1 = P('match1v2_t1 | player1_t1 x player2_t1) in emissionGroup is emissionParam
  val match2v3_t1 = P('match2v3_t1 | player2_t1 x player3_t1) in emissionGroup is emissionParam

  val match1v2_t2 = P('match1v2_t2 | player1_t2 x player2_t2) in emissionGroup is emissionParam
  val match1v3_t2 = P('match1v3_t2 | player1_t2 x player3_t2) in emissionGroup is emissionParam
  val match2v3_t2 = P('match2v3_t2 | player2_t2 x player3_t2) in emissionGroup is emissionParam

  val graph = network.createClusterGraph()
}