package marae.gm.junctiontree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marae.gm.GMNode;
import moorea.graphs.Graph;
import moorea.maths.lambda.FunctionalAlgorithms;
import moorea.maths.lambda.Mapper;
import moorea.maths.lambda.functions.lists.ListSizeMapper;
import moorea.misc.ArrayMisc;
import moorea.misc.HelperClass;

/**
 * Represent a junction forest.
 * 
 * Contains a graph of cliques, and a se of messages between cliques.
 * 
 * 
 * @author nicolas
 *
 * @param <C>
 */

public class JunctionTree<C extends GMNode> {

	public Graph<C> graph;
	
	public static HashMap messages = new HashMap<>();

	public JunctionTree() {
	}
	
	public void setGraph(Graph<C> graph) {
		this.graph = graph;
	}
	
	public void displayStatistics() {
		List<Integer> csizes = new LinkedList<>();
		
		for(GMNode c : (List<GMNode>) graph.getNodes()) {
			csizes.add(c.getScope().size());
		}
		
		int[] histSizes = HelperClass.histogram(csizes);
		
		System.out.println("== JT statistics");
		
		System.out.println("nb cliques: "+csizes.size());
		
		System.out.println("clique sizes histogram:");
		ArrayMisc.dispArrayWithIndexes(histSizes);
	}
}
