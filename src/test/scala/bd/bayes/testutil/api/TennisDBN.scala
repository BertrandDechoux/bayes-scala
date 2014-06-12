package bd.bayes.testutil.api

import dk.bayes.model.clustergraph.ClusterGraph
import dk.bayes.model.clustergraph.factor._

object TennisDBN {
  import bd.bayes.api.DiscreteBayesian
  import bd.bayes.api.DiscreteBayesian._
  implicit val network = DiscreteBayesian.network()

  val priorGroup = network.createNodeGroup()
  val emissionGroup = network.createNodeGroup()
  val transitionGroup = network.createNodeGroup()

  val priorParam = Array(0.2, 0.5, 0.3)
  val emissionParam = Array(
    0.5, 0.5,
    1d / 3, 2d / 3,
    0.25, 0.75,
    2d / 3, 1d / 3,
    0.5, 0.5,
    2d / 5, 3d / 5,
    3d / 4, 1d / 4,
    3d / 5, 2d / 5,
    0.5, 0.5)
  val transitionParam = Array(0.98, 0.01, 0.01, 0.01, 0.98, 0.01, 0.01, 0.02, 0.97)

  // player 1
  val player1_t0 = P('player1_t0) in priorGroup follows (priorParam:_*)
  val player1_t1 = P('player1_t1 | player1_t0) in transitionGroup follows (transitionParam:_*)
  val player1_t2 = P('player1_t2 | player1_t1) in transitionGroup follows (transitionParam:_*)

  // player 2
  val player2_t0 = P('player2_t0) in priorGroup follows (priorParam:_*)
  val player2_t1 = P('player2_t1 | player2_t0) in transitionGroup follows (transitionParam:_*)
  val player2_t2 = P('player2_t2 | player2_t1) follows (transitionParam:_*) // no group

  // player 3
  val player3_t1 = P('player3_t1) in transitionGroup follows (priorParam:_*)
  val player3_t2 = P('player3_t2 | player3_t1) in transitionGroup follows (transitionParam:_*)

  // matches
  val match1v2_t0 = P('match1v2_t0 | player1_t0 x player2_t0) in emissionGroup follows (emissionParam:_*)

  val match1v2_t1 = P('match1v2_t1 | player1_t1 x player2_t1) in emissionGroup follows (emissionParam:_*)
  val match2v3_t1 = P('match2v3_t1 | player2_t1 x player3_t1) in emissionGroup follows (emissionParam:_*)

  val match1v2_t2 = P('match1v2_t2 | player1_t2 x player2_t2) in emissionGroup follows (emissionParam:_*)
  val match1v3_t2 = P('match1v3_t2 | player1_t2 x player3_t2) in emissionGroup follows (emissionParam:_*)
  val match2v3_t2 = P('match2v3_t2 | player2_t2 x player3_t2) in emissionGroup follows (emissionParam:_*)

  val graph = network.createClusterGraph()
}