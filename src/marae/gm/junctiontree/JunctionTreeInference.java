package marae.gm.junctiontree;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marae.gm.GMNode;
import moorea.graphs.Graph;
import moorea.graphs.traversal.GraphNodeIterator;
import moorea.graphs.traversal.GraphNodeIteratorBFS;
import moorea.maths.algebra.Semiring;
import moorea.maths.lambda.FunctionalAlgorithms;
import moorea.maths.lambda.functions.IdentityMapper;
import moorea.maths.matrix.Matrix;
import moorea.maths.matrix.MatrixAlgorithms;


/**
 * Contains the methods used to schedule inference over a junction tree.
 * 
 * @author nicolas
 *
 */

public class JunctionTreeInference {

	public static <C extends GMNode<V, K>, V, K> void performParallelUpdateSchedule(List<C> schedule) {
		
	}
	
	public static boolean debugInference = false;

	
	public static <C extends GMNode<V, K>, V, K> void performSequentialUpdateSchedule(List<C> schedule) {

		//
		Set<GMNode<V, K>> updatedCliques = new HashSet<>();
		
		//
		for(GMNode<V, K> c : schedule) {
			
			// merge info of a clique
			if(debugInference) {
				System.out.println("merge info in "+c);
			}
			c.setInformation(c.mergeInformation());
			if(debugInference) {
				System.out.println("new information:");
				MatrixAlgorithms.display((Matrix)c.getInformation());
			}
			updatedCliques.add(c);
			
			// build list of clique to compute messages to (not the already processed ones)
			List<GMNode<V, K>> neighboursToProcess = new LinkedList<>();
			neighboursToProcess.addAll(c.getNeighbours());
			neighboursToProcess.removeAll(updatedCliques);
			neighboursToProcess.addAll(
					org.apache.commons.collections4.CollectionUtils.intersection(
							c.getNeighbours(), updatedCliques));
			
			// compute message to each neighbour to process
			for(GMNode<V, K> nb : neighboursToProcess) {
				if(debugInference) {
					System.out.println("summarise info of "+c+" for "+nb);
				}
				K message = c.summariseInformation(nb);
				if(debugInference) {	
					System.out.println("message:");
					MatrixAlgorithms.display((Matrix)message);
				}
				nb.updateMessage(message, c);
			}
		}
	}
	
	public static <C extends GMNode> List<C> getUpdateScheduleRecursivelyFromNode(Graph<C> g, C n) {
		GraphNodeIterator<C> it = new GraphNodeIteratorBFS<C>(g, n);
		List<C> updateSequence = FunctionalAlgorithms.map(it, new IdentityMapper<C>());
		//System.out.println(updateSequence);
		return updateSequence;
	}
	
	/*
	 * What:
	 * - update a node asynchronously
	 * 
	 * How:
	 * - if a node reveives any new message ir recomputes its messages for all its neighbours
	 * - it send the message if they differ from the former
	 * 
	 */
	
	
	public static void asynchronousUpdate(GMNode c) {

	}
	
	/*
	 * What:
	 * - update a node synchronously
	 * 
	 * How:
	 * - perform update only if all predecesor have been update and computer their message to this node
	 * - otherwise, waits or return
	 * 
	 * order: hashmap that encodes Node -> successor Node (parent)
	 */
	
	public static void synchronousUpdate(GMNode c, Map<GMNode, GMNode> order) {
		
	}

}
