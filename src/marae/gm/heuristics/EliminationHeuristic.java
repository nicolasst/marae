package marae.gm.heuristics;


import java.util.Comparator;
import java.util.Set;

import moorea.graphs.Node;

/**
 * 
 * @author nicolas
 *
 */

public abstract class EliminationHeuristic implements Comparator<Node> {
	
	Set marks;

	public EliminationHeuristic() {
	}
	
	public EliminationHeuristic(Set marks) {
		this.marks = marks;
	}

	public Set getMarks() {
		return marks;
	}

	public void setMarks(Set marks) {
		this.marks = marks;
	}
	
	public abstract int compare (Node n1, Node n2);

}
