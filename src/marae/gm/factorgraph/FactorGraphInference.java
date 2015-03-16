package marae.gm.factorgraph;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import marae.gm.GMGraphDisplay;
import marae.gm.GMNode;
import marae.gm.GMNodeInitialiser;
import marae.gm.HyperMatrixGDLNode;
import marae.gm.HyperMatrixGDLNodeInitialiser;
import moorea.maths.algebra.Semiring;
import moorea.maths.hypermatrix.HyperMatrix;
import moorea.maths.hypermatrix.HyperMatrixFactory;
import moorea.maths.objects.DiscreteVariable;
import moorea.maths.objects.Function;
import moorea.maths.objects.ScopeFactory;
import moorea.maths.random.UniformRandomIntegerGenerator;

/**
 * Inference over factor graphs.
 * 
 * @author nicolas
 *
 */

public class FactorGraphInference {

	public static <C extends GMNode<V, K>, V, K> void updateNTimeCyclicaly(FactorGraph<C> fg, int nbTimes) {
		
		//
		Set<GMNode<V, K>> updatedCliques = new HashSet<>();

		for(C c : fg.graph.getNodes()) {
			c.setInformation(c.mergeInformation());
		}
		
		for (int i = 0; i < nbTimes; i++) {
			// compute from function nodes to variable nodes
			for(C c : fg.functionNodes) {
				for(GMNode<V, K> nb : c.getNeighbours()) {
					System.out.println("summarise "+c+" for "+nb);
					K message = c.summariseInformation(nb);
					nb.updateMessage(message, c);
				}
			}
			
			// update variable nodes
			for(C c : fg.variableNodes) {
				c.setInformation(c.mergeInformation());
			}
			
			// compute from variable nodes to function nodes
			for(C c : fg.variableNodes) {
				for(GMNode<V, K> nb : c.getNeighbours()) {
					System.out.println("summarise "+c+" for "+nb);
					K message = c.summariseInformation(nb);
					nb.updateMessage(message, c);
				}
			}
			
			// update function nodes
			for(C c : fg.functionNodes) {
				c.setInformation(c.mergeInformation());
			}
			
		}
		
	}
	
	public static void unitTest() {
		////// sub function generation

		// ad hoc semiring
		
		Semiring<Integer> semiring = Semiring.constraintOptimisationSR;
		
		// generate sub function scopes
		
		ScopeFactory<DiscreteVariable> sf = new ScopeFactory<>(DiscreteVariable.class);
		sf.configureRandomness(new Random());
		
		sf.createDiscreteVariables(4, 2);
		
		List<DiscreteVariable> scope1 = sf.sampleScopeUniformRandom(2);
		List<DiscreteVariable> scope2 = sf.sampleScopeUniformRandom(2);
		
		List<List<DiscreteVariable>> lscopes = new LinkedList<>();
		lscopes.add(scope1);
		lscopes.add(scope2);
		
		// generate ad hoc sub functions values
		
		HyperMatrixFactory<DiscreteVariable, Integer> hmf = new HyperMatrixFactory<>(DiscreteVariable.class, semiring.getSetClass());
		hmf.setPRNG(new UniformRandomIntegerGenerator(new Random(), 0, 100));
		
		List<HyperMatrix<DiscreteVariable, Integer>> subFunctions =	hmf.generateRandomHyperMatrixListFromScopeList(lscopes);
		
		////// construction

		GMNodeInitialiser cinit = new HyperMatrixGDLNodeInitialiser(semiring);
		
		FactorGraph<HyperMatrixGDLNode<Integer>> fg = FactorGraphConstruction.createFactorGraph((List<Function>) (Object) subFunctions, cinit);
		
		System.out.println("fg:");
		
		GMGraphDisplay.disp(fg.graph);
		
		/////// Inference
		
		updateNTimeCyclicaly(fg, 10);
	}
	
	public static void main(String[] args) {
		unitTest();
	}
}
