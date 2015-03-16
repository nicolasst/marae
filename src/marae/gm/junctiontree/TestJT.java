package marae.gm.junctiontree;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;


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
import moorea.maths.hypermatrix.HyperMatrixAlgorithms;
import moorea.maths.hypermatrix.HyperMatrixFactory;
import moorea.maths.objects.DiscreteVariable;
import moorea.maths.objects.Function;
import moorea.maths.objects.ScopeFactory;
import moorea.maths.random.UniformRandomIntegerGenerator;

/**
 * Unit test for JunctionTree.
 * 
 * Uses a constraint optimisation example.
 * 
 * @author nicolas
 *
 */

public class TestJT {

	public static <K> Graph<HyperMatrixGDLNode<K>> buildSRHMJT(Semiring<K> sr, ContainerGraph<List<ContainerNode<DiscreteVariable>>> jt) {

		// create result graph
		Graph<HyperMatrixGDLNode<K>> ct = new Graph<>(HyperMatrixGDLNode.class);
		
		// map nodes from jt structure graph ('jt') to the jt of clique objects ('ct')
		Map<ContainerNode<List<ContainerNode<DiscreteVariable>>>, HyperMatrixGDLNode<K>> mapJTCliqueToCTClique = new HashMap<>();
		
		// create nodes of the ct
		for(ContainerNode<List<ContainerNode<DiscreteVariable>>> c : jt.getNodes()) {
			
			// create node
			HyperMatrixGDLNode<K> sc = ct.createNewNode();

			// set clique's scope
			List<DiscreteVariable> scope = new LinkedList<>();
			for(ContainerNode<DiscreteVariable> cc : c.getContent()) {
				scope.add(cc.getContent());
			}
			sc.setScope(scope);
			sc.setSemiring(sr);
			
			// add node to ct
			ct.addExistingNode(sc);
			
			System.out.println("create srhc "+sc+" : "+scope);
			mapJTCliqueToCTClique.put(c, sc);
		}
		
		System.out.println(mapJTCliqueToCTClique);
		
		// create edges of the ct
		for(ContainerNode<List<ContainerNode<DiscreteVariable>>> c : jt.getNodes()) {
			HyperMatrixGDLNode<K> sc = mapJTCliqueToCTClique.get(c);
			for(ContainerNode<List<ContainerNode<DiscreteVariable>>> nb : c.getNeighbours()) {
				HyperMatrixGDLNode<K> nbc = mapJTCliqueToCTClique.get(nb);
				// break undirected edge symetry with node ides
				if(c.id > nb.id) {
					ct.createEdge(sc, nbc);
				}
			}
		}
		
		return ct;
	}
	
	
	public static void unitTest() {
	
		JunctionTree jt = new JunctionTree();
		
		// sub function generation

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

		// build markov graph
		
		ContainerGraph<DiscreteVariable> mg = GraphFactory.buildContainerGraphFromObjectLists(lscopes);
		
		GraphDisplay.disp(mg);
		
		// build jt structure (triangulate markov graph + variable elimination)
		
		ContainerGraph<List<ContainerNode<DiscreteVariable>>> jtStructure = JunctionTreeConstruction.createJunctionTreeStructure(jt, mg);
		
		GraphDisplay.disp(jtStructure);
		
		// build ad hoc Clique Tree
			
		GMNodeInitialiser cinit = new HyperMatrixGDLNodeInitialiser(semiring);
		
		Graph<HyperMatrixGDLNode<Integer>> ct = JunctionTreeConstruction.buildCliques(jt, cinit, jtStructure);
		
		
		GraphDisplay.disp(ct);
		
		JunctionTreeConstruction.allocateSubfunctionsToCliques(ct, subFunctions);
		
	
		List<Graph<HyperMatrixGDLNode<Integer>>> cc = (List<Graph<HyperMatrixGDLNode<Integer>>>) moorea.graphs.algorithms.GraphExtraction.getConnectedComponents(ct);
		
		System.out.println("CC size "+cc.size());
		
		
		// select the two cliques (sometime may be only one)
		
		HyperMatrixGDLNode<Integer> c1 = ct.getNodes().get(0);

		System.out.println("c1: "+c1);
		
		HyperMatrixGDLNode<Integer> c2 = ct.getNodes().get(1);

		System.out.println("c2: "+c2);
		
		// test clique information merging
		
		HyperMatrix<DiscreteVariable, Integer> hm = c1.mergeInformation();

		HyperMatrixAlgorithms.disp(hm);
		
		// test clique information summarisation
		
		c1.setInformation(hm);
		
		hm = c1.summariseInformation(c2);
		
		HyperMatrixAlgorithms.disp(hm);
		
		jt.setGraph(ct);
		
		List<HyperMatrixGDLNode<Integer>> schedule = JunctionTreeInference.getUpdateScheduleRecursivelyFromNode(jt.graph,ct.getNodes().get(0));
		
		System.out.println("update sequence:");
		System.out.println(schedule);
		
		JunctionTreeInference.performSequentialUpdateSchedule(schedule);
		
		HyperMatrixAlgorithms.disp(c1.getInformation());
		
		HyperMatrixAlgorithms.disp(c2.getInformation());
		
		System.out.println("messages to "+c1);
		for(HyperMatrix m : c1.getReceivedMessages().values()) {
			HyperMatrixAlgorithms.disp(m);
		}
		/*
		System.out.println("subf "+c1);
		for(HyperMatrix m : c1.subFunctions) {
			HyperMatrixAlgorithms.disp(m);
		}
		*/
		
		System.out.println("messages to "+c2);
		for(HyperMatrix m : c2.getReceivedMessages().values()) {
			HyperMatrixAlgorithms.disp(m);
		}
		/*
		System.out.println("subf "+c2);
		for(HyperMatrix m : c2.subFunctions) {
			HyperMatrixAlgorithms.disp(m);
		}
		*/
		
		//// construction : jt structure
		
		// generate scope list, ok
		
		// merge scopes, ok
		
		// build graph from scope list, ok
		
		// build jt from graph, ok
		
		//// construction : jt state initialisation
		
		// build clique graph from jt structure, ok
		
		// generate hypermatrix according to scope, ok
		
		// assign hypermatrix to clique graph, ok
		
		//// inference
		
		// handle several ccs, todo
		
		// schedule clique updates, ok
	}
	
	public static void main(String[] args) {
		unitTest();
	}
}
