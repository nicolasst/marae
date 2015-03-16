package marae.gm.factorgraph;


import java.util.LinkedList;
import java.util.List;

import marae.gm.GMNode;
import moorea.graphs.Graph;

/**
 * Factor graph representation.
 * 
 * A factor graph contains two kinds of nodes:
 *  - variable nodes
 *  - function nodes
 * 
 * Conveniently, both can be represented by a GMNode
 * 
 * @author nicolas
 *
 * @param <C>
 */

public class FactorGraph<C extends GMNode> {

	Graph<C> graph;

	List<C> variableNodes = new LinkedList<>();
	List<C> functionNodes = new LinkedList<>();
	
	public FactorGraph(Graph<C> graph, List<C> variableNodes, List<C> functionNodes) {
		this.graph = graph;
		this.variableNodes = variableNodes;
		this.functionNodes = functionNodes;
	}
	
}
