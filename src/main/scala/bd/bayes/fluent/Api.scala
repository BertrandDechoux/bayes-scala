package bd.bayes.fluent

import dk.bayes.model.clustergraph.factor.Var
import dk.bayes.model.clustergraph.factor.Factor
import dk.bayes.model.clustergraph.ClusterGraph

object Api {
  def buildNetwork() = new NetworkBuilder()
}

class NetworkBuilder {
  type NodeGroup = Int
  case class Node(name: Symbol, variable: Var, parents: List[Node], group: Option[NodeGroup], factor: Factor) {
	if (variable.dim < 2) {
      throw new IllegalArgumentException("Dimension must be at least 2")
    }
	def id = variable.id
  }
  
  private var nextNodeId = 1
  private var nextNodeGroup = 1
  private val nodes = scala.collection.mutable.LinkedHashMap[Int, Node]()
  
  private def register(name:Symbol, dimension:Int, parents:List[Node], group: Option[NodeGroup], parameters:List[Double]): Node = {
    val variable = Var(nextNodeId, dimension)
    val variables = parents.map(_.variable).toArray :+ variable
    val node = Node(name, variable, parents, group, Factor(variables, parameters.toArray))
    nodes += (node.id -> node)
    nextNodeId += 1
    node
  }
  
  private def createNode(name: Symbol, parents: List[Node], group: Option[NodeGroup], parameters: List[Double]): Node = {
    if (parents.isEmpty) {
      register(name, parameters.size, parents, group, parameters)
    } else {
	    val undeclaredParents = parents.filter(p => !nodes.contains(p.id))
	    if (!undeclaredParents.isEmpty) {
	      throw new IllegalArgumentException(s"Undeclared parents : %s".format(undeclaredParents))
	    }
	    val parentDimension : Double = parents.map(_.variable.dim).reduce(_*_)
	    val dimension = parameters.size / parentDimension
	    if(!dimension.isValidInt) {
	      throw new IllegalArgumentException(s"Parameters error : " +
	          "total dimension %d, parent dimension %d, node dimension %d ?"
	          .format(parameters.size,parentDimension,dimension))
	    }
      register(name, dimension.toInt, parents, group, parameters)
    }
  }
  
  def createNodeGroup(): NodeGroup = {
    val nodeGroup = nextNodeGroup
    nextNodeGroup += 1
    nodeGroup
  }
  
  def addNode(name:Symbol, parents:Node*)(parameters: Double*): Node = {
    createNode(name, parents.toList, None, parameters.toList)
  }
  
  def addNode(group: NodeGroup, name:Symbol, parents:Node*)(parameters: Double*): Node = {
    createNode(name, parents.toList, Some(group), parameters.toList)
  }
  
  def createClusterGraph(): ClusterGraph = {
    val graph = ClusterGraph()
    nodes.foreach(e => graph.addCluster(e._1, e._2.factor))
    val edges = (for (e <- nodes.toSeq; p <- e._2.parents) yield (p.id,e._1)).sorted
    if (!edges.isEmpty) {
    	graph.addEdges(edges.head,edges.tail:_*)
    }
    graph
  }
}
