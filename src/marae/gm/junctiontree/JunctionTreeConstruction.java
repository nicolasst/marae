package marae.gm.junctiontree;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import marae.gm.GMNode;
import marae.gm.GMNodeInitialiser;
import marae.gm.HyperMatrixGDLNode;
import marae.gm.HyperMatrixGDLNodeInitialiser;
import marae.gm.heuristics.EliminationHeuristic;
import marae.gm.heuristics.NodeComparatorMCS;
import moorea.graphs.ContainerGraph;
import moorea.graphs.ContainerNode;
import moorea.graphs.Graph;
import moorea.graphs.Node;
import moorea.graphs.SimpleNode;
import moorea.graphs.construction.GraphFactory;
import moorea.graphs.io.GraphDisplay;
import moorea.maths.algebra.Semiring;
import moorea.maths.hypermatrix.HyperMatrix;
import moorea.maths.hypermatrix.HyperMatrixFactory;
import moorea.maths.objects.DiscreteVariable;
import moorea.maths.objects.Function;
import moorea.maths.objects.ScopeFactory;
import moorea.maths.random.UniformRandomIntegerGenerator;
import moorea.misc.AdHocMapEvalComparator;
import moorea.misc.AdHocMapEvalIntComparator;
import moorea.misc.BidiMap;
import moorea.misc.Tuple2;

/**
 * Contains all the methods to build a junction tree from a list of function.
 * 
 * @author nicolas
 *
 */

public class JunctionTreeConstruction {
	
	public static boolean debug = false;
	
	/**
	 * Returns both the triangulated graph of the Markov graph g, and the particular elimination sequence used to yield the triangulated graph.
	 * Does not modify the graph in parameter, return a new graph.
	 * Uses an MCS heuristic for the elimination order.
	 * TODO parametrise the variable elimination heuristic.
	 */
	
	public static <K extends Node> Tuple2<Graph<K>,List<K>> triangulateGraph(Graph<K> g) {
		return triangulateGraph(g,  new NodeComparatorMCS());
	}

	public static <K extends Node> Tuple2<Graph<K>,List<K>> triangulateGraph(Graph<K> g, EliminationHeuristic eh) {
		//Graph<SimpleNode> gt = Tupple2.getFirst(GraphFactory.simpleGraphFactory.createGraphCopyingStructure(g));
		
		Graph gt = g;
		
		// mark nodes already eliminated
		Set<Node> marks = new HashSet<>();
		
		eh.setMarks(marks);
		
		//Map<Node,Integer> h = new HashMap<Node, Integer>();
		PriorityQueue<Node> h = new PriorityQueue<>(10, eh);
		
		for(Node n :  (List<Node>) gt.getNodes()) {
			//int hVal = heuristicEvaluationMCS(n);
			//h.put(n,hVal);
			h.add(n);
		}
		
		List<Node> eliminationOrder = new LinkedList<>();
		
		while(h.size()>0) {
			Iterator<Node> it = h.iterator();
			while(it.hasNext()) {
				Node v = it.next();
				//System.out.print(""+v+" : "+heuristicEvaluationMCS(v, marks)+"  ");
			}
			//System.out.println();
			Node n = h.poll();
			List<SimpleNode> ln = new ArrayList(n.getNeighbours());
			//System.out.println("poll : "+n);
			//System.out.println("ln : "+ln);
			marks.add(n);
			eliminationOrder.add(n);
			for(int i=0;i<ln.size()-1;i++) {
				SimpleNode nb1 = ln.get(i);
				if(marks.contains(nb1)) {
					continue;
				}
				for(int j=i+1;j<ln.size();j++) {
					SimpleNode nb2 = ln.get(j);
					if(marks.contains(nb2)) {
						continue;
					}
					if(!gt.hasEdge(nb1, nb2)) {
						gt.createEdge(nb1, nb2);
					}
				}
			}
		}
		
		Tuple2<Graph<K>,List<K>> res = new Tuple2(gt,eliminationOrder);
		
		return res;
	}

	/**
	 * Sort a list of variables according to the increasing value of their id field.
	 * This function is mostly used to make uniform functions' scopes in order for an
	 * human to quickly read them.
	 */

	
	public static void sortNodeListByIncreasingId(List<Node> l) {
		Map<Node, Integer> eval = new HashMap<>();
		for(Node v : l) {
			eval.put(v,v.id);
		}
		Collections.sort(l,new AdHocMapEvalIntComparator<Node>(eval));
	}
	
	public static <K> void sortAccordingToArbitraryOrder(List<K> l, List<K> order) {
		Map<K, Integer> eval = new HashMap<K, Integer>();
		for(K e : l) {
			eval.put(e,order.indexOf(e));
		}
		Collections.sort(l,new AdHocMapEvalIntComparator<K>(eval));
	}
	
	public static <K> void sortAccordingToArbitraryOrderMap(List<K> l, Map<K, Comparable> order) {
		Collections.sort(l,new AdHocMapEvalComparator<K>(order));
	}
	
	public static <K> void sortListAccordingToArbitraryOrderIntegerMap(List<K> l, Map<K, Integer> order) {
		Collections.sort(l,new AdHocMapEvalIntComparator<K>(order));
	}
	
	public static <K> void sortListAccordingToArbitraryOrderDoubleMap(List<K> l, Map<K, Double> order) {
		Collections.sort(l,new AdHocMapEvalIntComparator<K>(order));
	}

	public static <K extends Node> ContainerGraph<List<K>> buildJunctionTreeStructureFromEliminationOrder(JunctionTree jt, Graph<K> g, List<K> eliminationOrder) {

		
		ContainerGraph<List<K>> jtStructure = new ContainerGraph<>();
		
		// build cliques

		List<K> marks = new LinkedList<>();
		
		// both fields are required only for dedicate edge building algorithm
		BidiMap<K, ContainerNode<List<K>>> mapVarToClique = new BidiMap<>(); 
		Map<K,Integer> mapVarToOrder = new HashMap<>();
		
		if(debug) {
			GraphDisplay.disp(g);
			System.out.println(eliminationOrder);
		}
		
		int cpt=0;
		for(K v : eliminationOrder) {
			List<K> lv = new LinkedList<>(v.getNeighbours());
			lv.add(v);
			lv.removeAll(marks);
 			ContainerNode<List<K>> c = jtStructure.createNewNode(new LinkedList<K>());
 			c.setContent(lv);
			jtStructure.addExistingNode(c);
			//System.out.println("create node:"+v+" <=> clique:"+c+" = (node set)"+lv);
			marks.add(v);
			mapVarToClique.put(v,c);
			mapVarToOrder.put(v, cpt++);
		}

		// build edges (not the mst version, but the lesser known ad-hoc one)
		
		for(ContainerNode<List<K>> c : jtStructure.getNodes()) {
			List<K> lv = c.getContent();
			// sort node list according to elimition order
			Collections.sort(lv,new AdHocMapEvalIntComparator<K>(mapVarToOrder));
			// connect, if possible, following this order the first clique corresponding to
			// c's variables who has a non zero intersection with c 
			for(K v : lv) {
				ContainerNode<List<K>> cn = mapVarToClique.getAB(v);
				//System.out.println("test edge "+c+"  "+cn+" "+CollectionUtils.intersection(c.getContent(),cn.getContent()).size());
				//System.out.println("test edge "+c.getContent()+"  "+cn.getContent()+" "+CollectionUtils.intersection(c.getContent(),cn.getContent()));
				if(c != cn && CollectionUtils.intersection(c.getContent(),cn.getContent()).size() > 0) {
					//System.out.println("add edge "+c+" "+cn);
					jtStructure.createEdge(c, cn);
					break;
				}
			}
		}
		
		// re-sort node list in increasing id fashion
		for(ContainerNode<List<K>> c : jtStructure.getNodes()) {
			List<K> lv = c.getContent();
			sortNodeListByIncreasingId((List<Node>)lv);
		}
		
		// Up to now we have build an elimination tree, a tree that contains non maximal cliques an corresponds to the actual cliques used by variablem elimination.
		
		// retain maximal cliques only

		LinkedList<ContainerNode<List<K>>>  fifo = new LinkedList<>();
		fifo.addAll(jtStructure.getNodes());
		while(fifo.size() > 0) {
			ContainerNode<List<K>> c = fifo.removeFirst();
			for(ContainerNode<List<K>> cn : c.getNeighbours()) {
//				System.out.println(c+"  "+cn+" "+CollectionUtils.intersection(c.getContent(),cn.getContent()).size());
				if(CollectionUtils.intersection(c.getContent(),cn.getContent()).size() == c.getContent().size()) {
//					System.out.println("merge "+c+" in "+cn);
//					System.out.println("merge "+c.getContent()+" "+cn.getContent());
					fifo.remove(c);
					// remove node and connect neighbors to other node
					for(ContainerNode<List<K>> nb : c.getNeighbours()) {
						if(nb != c) {
							jtStructure.createEdge(nb, cn);
						}
					}
					jtStructure.removeNode(c);
					mapVarToClique.put(mapVarToClique.getBA(c), cn);
					break;
				}
			}
		}
		
		System.out.println(mapVarToClique);
		
		// TODO ok to remove?
		jt.messages.put("mapVarToClique", mapVarToClique);
		// /TODO
		
		// once a junction tree structure, or any other graphical structure whose nodes aren't the variables
		// of the original function, has been defined the scope of this junction tree can be replaced from
		// the temporary intermediary variables (with the same "name" i.e. 'id' fields) to the original
		// variables, has those won't be treated as node in a graph anymore.
		// this requires information currently outside the scope of this function, and has therefore to be performed
		// as a separate additional step.
		
		return jtStructure;
	}
	

	public static <K extends Node> ContainerGraph<List<K>> createJunctionTreeStructure(JunctionTree jt, Graph<K> g) {
		return createJunctionTreeStructure(jt, g, new NodeComparatorMCS());
	}

	
	// JT construction:
	// input: Graph<K>  i.e. any graph, whose nodes are of type K
	// output: ContainerGraph<List<K>>  i.e. a graph whose nodes are lists of nodes of type K
	//
	// intermediary steps:
	// Graph<K> -> Graph<SimpleNode> "markov graph"
	// "markov graph" -> "triangulated graph"
	// "triangulated graph" -> "jt graph"
	
	// display a warning for content already present is not an issue
	
	public static <K extends Node> ContainerGraph<List<K>> createJunctionTreeStructure(JunctionTree jt, Graph<K> markovGraph, EliminationHeuristic eh) {
		
		//
		System.out.println("triangulation");
		
		Tuple2<Graph<K>,List<K>> resTriangulation;
		
		resTriangulation = triangulateGraph(markovGraph, eh);

		Graph tmn;
		tmn = resTriangulation.first;
		
		if(debug) {
			System.out.println("elim order "+resTriangulation.second);

			System.out.println("triangulated graph:");
			GraphDisplay.disp(tmn);
		}
		
		//
		System.out.println("jt construction");
		ContainerGraph<List<K>> jtStructure = buildJunctionTreeStructureFromEliminationOrder(jt, tmn,resTriangulation.second);
		
		if(debug) {
			System.out.println("jt:");
			GraphDisplay.disp(jtStructure);
		}
		
		return jtStructure;
	}
	
	
	public static <C extends GMNode<V, K>, K, V> Graph<C> buildCliques(JunctionTree jt, GMNodeInitialiser<C> cinit, ContainerGraph<List<ContainerNode<V>>> jtStructure) {

		// create result graph
		Graph<C> ct = new Graph<>(cinit.getCliqueClass());
		
		// map nodes from jt structure graph ('jt') to the jt of clique objects ('ct')
		Map<ContainerNode<List<ContainerNode<V>>>, C> mapJTCliqueToCTClique = new HashMap<>();
		
		// create nodes of the ct
		for(ContainerNode<List<ContainerNode<V>>> c : jtStructure.getNodes()) {
			
			// create node
			C sc = ct.createNewNode();

			// set clique's scope
			List<V> scope = new ArrayList<>(c.getContent().size());
			for(ContainerNode<V> cc : c.getContent()) {
				scope.add(cc.getContent());
			}
			sc.setScope(scope);
			cinit.configureClique(sc);
			
			// add node to ct
			ct.addExistingNode(sc);
			
//			System.out.println("create srhc "+sc+" : "+scope);
			mapJTCliqueToCTClique.put(c, sc);
			
		}
		
		// TODO ok to remove?
		BidiMap<ContainerNode<Node>,Object> mapVarToClique = (BidiMap) jt.messages.get("mapVarToClique");
		BidiMap mapVarToCliqueNew = new BidiMap();
		
		for(ContainerNode<Node> key :  mapVarToClique.getMapAB().keySet()) {
			mapVarToCliqueNew.put(key.getContent(), mapJTCliqueToCTClique.get(mapVarToClique.getAB(key)));			
		}
		
		jt.messages.put("mapVarToClique", mapVarToCliqueNew);
		// /TODO
		
//		System.out.println(mapJTCliqueToCTClique);
		
		// create edges of the ct
		for(ContainerNode<List<ContainerNode<V>>> c : jtStructure.getNodes()) {
			C sc = mapJTCliqueToCTClique.get(c);
			for(ContainerNode<List<ContainerNode<DiscreteVariable>>> nb : c.getNeighbours()) {
				C nbc = mapJTCliqueToCTClique.get(nb);
				// break undirected edge symetry with node ides
				if(c.id > nb.id) {
					ct.createEdge(sc, nbc);
				}
			}
		}
		
		return ct;
	}

	public static <C extends GMNode, F extends Function> void allocateSubfunctionsToCliques(Graph<C> ct, List<F> subFunctions) {

		Iterator<F> it;
		
		// allocates hm subfunctions : try to match each clique with each unallocated subfunction
		for(C c : ct.getNodes()) {
//			System.out.println("RRRR "+c+" "+c.getScope());
			
			// iterate over sub functions to allocate with iterator to modify the list
			it = subFunctions.iterator();
			
//			System.out.println("sf remaining "+subFunctions);
			
			while(it.hasNext()) {
				F sf = it.next();
				
				//System.out.println("test "+c.getScope()+" "+sf.getScope());
				
				// if clique contains sf scope, assign sb to the clique
				if(org.apache.commons.collections4.CollectionUtils.intersection(c.getScope(), sf.getScope()).size() == sf.getScope().size()) {
					
					// allocate sub function
					c.getSubFunctions().add(sf);
//					System.out.println("assign "+sf+" to "+c);

					// remove sf from subfunctions to allocate
					it.remove();
				}
				
			}
		}
	}

	public static <C extends GMNode<V, K>, V, K> JunctionTree<C> createJunctionTree(List<Function> subFunctions, GMNodeInitialiser<C> cinit) {
		
		// it is actually a forest not a tree!
		JunctionTree jt = new JunctionTree();
		
		// build scope list
		
		List<List<V>> lscopes = new LinkedList<>();
		
		for(Function f : subFunctions) {
			lscopes.add(f.getScope());
		}
		
		// build markov graph
		
		ContainerGraph<V> mg = GraphFactory.buildContainerGraphFromObjectLists(lscopes);
		
		if(debug) {
			GraphDisplay.disp(mg);
		}
		
		// build jt structure (triangulate markov graph + variable elimination)
		
		ContainerGraph<List<ContainerNode<V>>> jtStructure = JunctionTreeConstruction.createJunctionTreeStructure(jt, mg);
		
		if(debug) {
			GraphDisplay.disp(jtStructure);
		}
		
		// build ad hoc clique tree
		
		Graph<C> ct = buildCliques(jt, cinit, jtStructure);
		
		if(debug) {
			GraphDisplay.disp(ct);
		}
		
		JunctionTreeConstruction.allocateSubfunctionsToCliques(ct, subFunctions);
	
		//List<Graph<HyperMatrixGDLNode<Integer>>> cc = (List<Graph<HyperMatrixGDLNode<Integer>>>) moorea.graphs.algorithms.GraphExtraction.getConnectedComponents(ct);
		
		//System.out.println("CC size "+cc.size());
		
		jt.setGraph(ct);
		
		return jt;
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
		
		JunctionTree<HyperMatrixGDLNode<Integer>> jt = createJunctionTree((List<Function>) (Object) subFunctions, cinit);
		
		System.out.println("jt:");
		
		GraphDisplay.disp(jt.graph);
	}
	
	public static void main(String[] args) {
		unitTest();
	}
}
