package marae.gm.factorgraph;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import marae.gm.GMGraphDisplay;
import marae.gm.GMNode;
import marae.gm.GMNodeInitialiser;
import marae.gm.HyperMatrixGDLNode;
import marae.gm.HyperMatrixGDLNodeInitialiser;
import moorea.graphs.ContainerGraph;
import moorea.graphs.ContainerNode;
import moorea.graphs.Graph;
import moorea.graphs.construction.GraphFactory;
import moorea.graphs.io.GraphDisplay;
import moorea.maths.algebra.Semiring;
import moorea.maths.hypermatrix.HyperMatrix;
import moorea.maths.hypermatrix.HyperMatrixFactory;
import moorea.maths.objects.DiscreteVariable;
import moorea.maths.objects.Function;
import moorea.maths.objects.ScopeFactory;
import moorea.maths.random.UniformRandomIntegerGenerator;

/**
 * Factor graph construction.
 * 
 * @author nicolas
 *
 */

public class FactorGraphConstruction {

	public static <C extends GMNode<V, K>, V, K> FactorGraph<C> createFactorGraph(List<Function> subFunctions, GMNodeInitialiser<C> cinit) {

		Graph<C> ct = new Graph<>(cinit.getCliqueClass());

		List<C> variableNodes = new LinkedList<>();
		List<C> functionNodes = new LinkedList<>();
		
		List<List<V>> lscopes = new LinkedList<>();
		Set<V> uniqScope = new HashSet<>();
		
		for(Function sf : subFunctions) {
			lscopes.add(sf.getScope());
			uniqScope.addAll(sf.getScope());
		}
		
		Map<V,C> mapVariableToVariableNode = new HashMap<>();
		
		for(V v : uniqScope) {
			// create node
			C sc = ct.createNewNode();

			// set clique's scope
			List<V> sfScope = new LinkedList<>();
			sfScope.add(v);
			sc.setScope(sfScope);
			cinit.configureClique(sc);

			// add node to ct
			ct.addExistingNode(sc);
			
			variableNodes.add(sc);
			
			mapVariableToVariableNode.put(v, sc);
		}
		
		for(Function sf : subFunctions) {
			// create node
			C sc = ct.createNewNode();

			// set clique's scope
			List<V> sfScope = sf.getScope();
			sc.setScope(sfScope);
			sc.getSubFunctions().add((K)sf);
			cinit.configureClique(sc);

			// add node to ct
			ct.addExistingNode(sc);
			
			functionNodes.add(sc);
			
			for(V v : (List<V>) sf.getScope()) {
				ct.createEdge(sc, mapVariableToVariableNode.get(v));
			}
		}
		
		for(C c : ct.getNodes()) {
			cinit.configureClique(c);
		}

		FactorGraph<C> fg = new FactorGraph(ct, variableNodes, functionNodes);

		return fg;
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
		
		FactorGraph<HyperMatrixGDLNode<Integer>> fg = createFactorGraph((List<Function>) (Object) subFunctions, cinit);
		
		System.out.println("fg:");
		
		GMGraphDisplay.disp(fg.graph);
	}

	public static void main(String[] args) {
		unitTest();
	}
	
}
