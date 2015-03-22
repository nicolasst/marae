package marae.gm.markovchain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import moorea.maths.matrix.Matrix;
import moorea.maths.matrix.MatrixFactory;
import moorea.maths.random.ProbabilityDistribution;

public class MarkovChain {

	List<String> stateNames = null;

	Matrix<Double> transitionMatrix = null;
	
	public MarkovChain() {
	}
	
	public MarkovChain(List<String> stateNames) {
		this.stateNames = stateNames;
		transitionMatrix = new Matrix<>(Double.class, stateNames.size());
	}
	
	public List<Integer> generateSequence(int initialStateIndex, int size, Random random) {
		List<Integer> sequence = new ArrayList<>(size);
		List<Double> nextStateProb = new ArrayList<>();
		int lastStateIndex = initialStateIndex;
		//
		for (int stepi = 0; stepi < size; stepi++) {
			nextStateProb.clear();
			// get next state probability
			for (int i = 0; i < transitionMatrix.width; i++) {
				nextStateProb.add(transitionMatrix.values[lastStateIndex][i]);
			}
			// generate next state
			double p = random.nextDouble();
			int nextStateIndex = ProbabilityDistribution.drawObjectIndexFromDistribution(nextStateProb, p);
			//
			System.out.println("step "+stepi+" : "+stateNames.get(lastStateIndex)+" -> "+stateNames.get(nextStateIndex));
			//
			sequence.add(nextStateIndex);
			lastStateIndex = nextStateIndex;
		}
		return sequence;
	}
	
	
	/* Example from Wikipedia: the Markov chain of Doudou the hamster
	
	Doudou, le hamster paresseux, ne connaît que trois endroits dans sa cage : les copeaux où il dort, la mangeoire où il mange et la roue où il fait de l'exercice. Ses journées sont assez semblables les unes aux autres, et son activité se représente aisément par une chaîne de Markov. Toutes les minutes, il peut soit changer d'activité, soit continuer celle qu'il était en train de faire. L'appellation processus sans mémoire n'est pas du tout exagérée pour parler de Doudou.

    Quand il dort, il a 9 chances sur 10 de ne pas se réveiller la minute suivante.
    Quand il se réveille, il y a 1 chance sur 2 qu'il aille manger et 1 chance sur 2 qu'il parte faire de l'exercice.
    Le repas ne dure qu'une minute, après il fait autre chose.
    Après avoir mangé, il y a 3 chances sur 10 qu'il parte courir dans sa roue, mais surtout 7 chances sur 10 qu'il retourne dormir.
    Courir est fatigant ; il y a 8 chances sur 10 qu'il retourne dormir au bout d'une minute. Sinon il continue en oubliant qu'il est déjà un peu fatigué. 
	 */
	
	public static void main(String[] args) {
		
		List<String> states = new ArrayList<>();
		
		states.add("sleep");
		states.add("eat");
		states.add("run");
		
		List<Double> values = Arrays.asList(
				0.9, 0.05, 0.05,
				0.7, 0.  , 0.3,
				0.8, 0.  , 0.2);
		
		Random random = new Random();
		
		MarkovChain mc = new MarkovChain(states);
		
		MatrixFactory.fillMatrix(mc.transitionMatrix, values.iterator());
		
		mc.generateSequence(0, 50, random);
		
	}
	
}
