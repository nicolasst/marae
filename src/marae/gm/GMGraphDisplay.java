package marae.gm;

import moorea.graphs.Graph;

/**
 * Ad-hoc graph to display graphs of GMNode.
 * 
 * @author nicolas
 *
 */

public class GMGraphDisplay {

	public static <C extends GMNode> void disp(Graph<C> g) {
		System.out.println("nodes:");
		for(C n : g.getNodes()) {
			System.out.println(n+" : "+n.getScope());
		}
		System.out.println("edges:");
		for(C n : g.getNodes()) {
			System.out.println("of "+n+" : "+n.getNeighbours());
		}

	}
}
