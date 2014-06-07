package bd.bayes.testutil.fluent

import dk.bayes.model.clustergraph.ClusterGraph
import dk.bayes.model.clustergraph.factor._

object TennisDBN {
  val bnb = bd.bayes.fluent.Api.buildNetwork()
  
  val priorGroup = bnb.createNodeGroup()
  val emissionGroup = bnb.createNodeGroup()
  val transitionGroup = bnb.createNodeGroup()

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
  val player1_t0 = bnb.addNode(priorGroup,'player1_t0)(priorParam:_*)
  val player1_t1 = bnb.addNode(transitionGroup,'player1_t1,player1_t0)(transitionParam:_*)
  val player1_t2 = bnb.addNode(transitionGroup,'player1_t2,player1_t1)(transitionParam:_*)

  // player 2
  val player2_t0 = bnb.addNode(priorGroup,'player2_t0)(priorParam:_*)
  val player2_t1 = bnb.addNode(transitionGroup,'player2_t1,player2_t0)(transitionParam:_*)
  val player2_t2 = bnb.addNode('player2_t2,player2_t1)(transitionParam:_*) // no group

  // player 3
  val player3_t1 = bnb.addNode(transitionGroup,'player3_t1)(transitionParam:_*)
  val player3_t2 = bnb.addNode(transitionGroup,'player3_t2, player3_t1)(transitionParam:_*)
  
  // matches
  val match1v2_t0 = bnb.addNode(emissionGroup,'match1v2_t0,player1_t0,player2_t0)(emissionParam:_*)
  
  val match1v2_t1 = bnb.addNode(emissionGroup,'match1v2_t1,player1_t1,player2_t1)(emissionParam:_*)
  val match2v3_t1 = bnb.addNode(emissionGroup,'match2v3_t1,player2_t1,player3_t1)(emissionParam:_*)
  
  val match1v2_t2 = bnb.addNode(emissionGroup,'match1v2_t2,player1_t2,player2_t2)(emissionParam:_*)
  val match1v3_t2 = bnb.addNode(emissionGroup,'match1v3_t2,player1_t2,player3_t2)(emissionParam:_*)
  val match2v3_t2 = bnb.addNode(emissionGroup,'match2v3_t2,player2_t2,player3_t2)(emissionParam:_*)

  val graph = bnb.createClusterGraph()
}