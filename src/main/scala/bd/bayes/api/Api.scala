package bd.bayes.api

import dk.bayes.model.clustergraph.factor.Var
import dk.bayes.model.clustergraph.factor.Factor
import dk.bayes.model.clustergraph.ClusterGraph

object DiscreteBayesian {
  type NodeGroup = Int
  
  /** A node with its dependencies but without its parameters. */
  trait NodeDeclaration {
    def name: Symbol
    def parents: Seq[Node]
  }
  
  /** A NodeDeclaration with potential parents. */
  case class AdoptedNodeDeclaration(name:Symbol,parents:Vector[Node]) extends NodeDeclaration {
    def x(parent:Node) = AdoptedNodeDeclaration(name,parents :+ parent)
  }
  
  /** A NodeDeclaration without parent. */
  case class OrphanNodeDeclaration(name:Symbol) extends NodeDeclaration {
    val parents = Vector()
    def |(parent:Node) = AdoptedNodeDeclaration(name,Vector(parent))
  }
  
  /** Allow a concise declaration like ('myVar | parent1 x parent2). */
  implicit val declarationStarter = (s:Symbol) => OrphanNodeDeclaration(s)
  
  /** The full declaration of the node without its parameters. */
  case class UnparameterizedNode(id:Int,declaration:NodeDeclaration,group:Option[NodeGroup]=None) {
    def follows(parameters:Double*)(implicit network:NetworkBuilder) = network.registerNode(this,parameters)
    def in(group:NodeGroup) = copy(group=Some(group))
  }
  
  /** The full declaration of the node with its parameters. */
  case class Node(name: Symbol, variable: Var, parents: List[Node], group: Option[NodeGroup], factor: Factor) {
	if (variable.dim < 2) {
      throw new IllegalArgumentException("Dimension must be at least 2")
    }
	def id = variable.id
  }

  /** Declare a node for the current network. */
  def P(n:NodeDeclaration)(implicit network:NetworkBuilder) = network.createParameterlessNode(n)
  def network() = new NetworkBuilder()
}

class NetworkBuilder {
  import DiscreteBayesian._
  
  private var nextNodeId = 1
  private var nextNodeGroup = 1
  private val nodes = scala.collection.mutable.LinkedHashMap[Int, Node]()
  
  def createNodeGroup(): NodeGroup = {
    val nodeGroup = nextNodeGroup
    nextNodeGroup += 1
    nodeGroup
  }
  
  private def createNodeId() = {
    val nodeId = nextNodeId
    nextNodeId += 1
    nodeId
  }
  
  def createParameterlessNode(n:NodeDeclaration) : UnparameterizedNode = {
    UnparameterizedNode(createNodeId(), n)
  }
  
  private def register(n:UnparameterizedNode, dimension:Int, parameters:Seq[Double]): Node = {
    val parents = n.declaration.parents
    val variable = Var(nextNodeId, dimension)
    val factor = if (parents.isEmpty) Factor(variable,parameters.toArray) else {
    	val variables = parents.map(_.variable).toArray :+ variable
    	Factor(variables, parameters.toArray)
    }
    val node = Node(n.declaration.name, variable, parents.toList, n.group, factor)
    nodes += (node.id -> node)
    nextNodeId += 1
    node
  }
  
  def registerNode(n:UnparameterizedNode,parameters:Seq[Double]) : Node =  {
    val parents = n.declaration.parents
    if (parents.isEmpty) {
      register(n, parameters.size, parameters)
    } else {
    	// path dependent types could allow the compiler to check that 
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
      register(n, dimension.toInt, parameters)
    }
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