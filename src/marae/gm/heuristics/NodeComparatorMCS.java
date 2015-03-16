package marae.gm.heuristics;


import java.util.Set;

import moorea.graphs.Node;

/**
 * Maximal cardinality elimination heuristic.
 * 
 * @author nicolas
 *
 */

public class NodeComparatorMCS extends EliminationHeuristic {

	public NodeComparatorMCS() {
	}
	
	public NodeComparatorMCS(Set marks) {
		super(marks);
	}
	
	public int compare (Node n1, Node n2) {
		return heuristicEvaluationMCS(n1, marks) - heuristicEvaluationMCS(n2, marks);
	}
	
	// heuristic evaluation functions
	
	/**
	 * MCS heuristic evaluation of Node n
	 * For use with algorithms that actualy modify the graph they are running on.
	 */
	
	public static int heuristicEvaluationMCS(Node n) {
		int v=0;
		v = n.getNeighbours().size();
		return v;
	}

	/**
	 * MCS heuristic evaluation of Node n, given the set 'marks' of already eliminated neighbours.
	 * For use with algorithms that do not modify the graph they are running on.
	 */
	
	public static int heuristicEvaluationMCS(Node n, Set marks) {
		int v=0;
		//v = n.getNeighbours().size();
		for(Object nb : n.getNeighbours()) {
			if(!marks.contains(nb)) {
				v++;
			}
		}
		return v;
	}
}
