package bd.bayes.api

import dk.bayes.model.clustergraph.factor.Var
import dk.bayes.model.clustergraph.factor.Factor
import dk.bayes.model.clustergraph.ClusterGraph

/**
 * Provide a DSL for easily building discrete Bayesion network.
 */
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
    /** Associate the parameters. */
    def is(parameters:Double*)(implicit network:NetworkBuilder) : Node = network.registerNode(this,parameters)
    def is(cpt:CPTLike)(implicit network:NetworkBuilder) : Node = is(cpt.asParameters:_*)
    /** Associate a node group. */
    def in(group:NodeGroup) = copy(group=Some(group))
  }
  
  /** The full declaration of the node with its parameters. */
  case class Node(name: Symbol, variable: Var, parents: List[Node], group: Option[NodeGroup], factor: Factor) {
	require(variable.dim >= 2, "Dimension must be at least 2")
	def id = variable.id
  }

  /** Declare a node for the current network. */
  def P(n:NodeDeclaration)(implicit network:NetworkBuilder) = network.createUnparameterizedNode(n)
  def network() = new NetworkBuilder()
}

/**
 * A mutable builder tracking the nodes which have been created.
 */
class NetworkBuilder {
  import DiscreteBayesian._
  
  private var nextNodeId = 1
  private var nextNodeGroup = 1
  private val nodes = scala.collection.mutable.LinkedHashMap[Int, Node]()
  
  /**
   * In order to identify multiple clusters (of the cluster graph) with the same id,
   * the related nodes need to be grouped within the same node group.
   * 
   * First step is the creation of that group.
   * Second step is during the creation of the node.
   */
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
  
  /**
   * Create a node template which still requires parameters.
   */
  def createUnparameterizedNode(n:NodeDeclaration) : UnparameterizedNode = {
    UnparameterizedNode(createNodeId(), n)
  }
  
  private def createFactor(parameters: Seq[Double], parents: Seq[Node], variable: Var): Factor = {
    if (parents.isEmpty) Factor(variable,parameters.toArray) else {
    	val variables = parents.map(_.variable).toArray :+ variable
    	Factor(variables, parameters.toArray)
    }
  }
  
  // XXX path dependent types could allow the compiler to check that 
  private def verifyParentsRegistration(parents: Seq[Node]): Unit = {
    val undeclaredParents = parents.filter(p => !nodes.contains(p.id))
    require(undeclaredParents.isEmpty, s"Undeclared parents : %s".format(undeclaredParents))
  }
  
  private def findDimension(parameters: Seq[Double], parents: Seq[Node]): Int = {
    if(parents.isEmpty) parameters.size else {
	    val parentDimension : Double = parents.map(_.variable.dim).reduce(_*_)
	    val dimension = parameters.size / parentDimension
	    require(dimension.isValidInt, s"Parameters error : " +
	          "total dimension %d, parent dimension %d, node dimension %d ?"
	          .format(parameters.size,parentDimension,dimension))
	    dimension.toInt
    }
  }
  
  /**
   * Register a new node for this network.
   */
  def registerNode(n:UnparameterizedNode, parameters:Seq[Double]) : Node =  {
    val parents = n.declaration.parents
    verifyParentsRegistration(parents)
    val variable = Var(n.id, findDimension(parameters, parents))
    val factor = createFactor(parameters, parents, variable)
    val node = Node(n.declaration.name, variable, parents.toList, n.group, factor)
    nodes += (node.id -> node)
    node
  }

  /**
   * Create the cluster graph representing the current Bayesian network.
   * @see page 152 "Bayesian Networks and Decision Graphs,Second Edition" Jensen Finn V., Nielsen Thomas D.
   */
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