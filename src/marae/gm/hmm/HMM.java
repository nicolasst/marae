package marae.gm.hmm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import marae.gm.markovchain.MarkovChain;
import moorea.maths.lambda.FunctionalAlgorithms;
import moorea.maths.matrix.Matrix;
import moorea.maths.matrix.MatrixAlgorithms;
import moorea.maths.matrix.MatrixFactory;
import moorea.maths.random.ProbabilityDistribution;
import moorea.misc.Tupple2;

public class HMM {

	
	List<String> stateNames = null;
	List<String> observationNames = null;

	Matrix<Double> transitionMatrix = null;
	Matrix<Double> emissionMatrix = null;
	Matrix<Double> initialStateProbability = null;
	
	public HMM() {
	}
	
	public HMM(List<String> stateNames, List<String> observationNames) {
		this.stateNames = stateNames;
		this.observationNames = observationNames;
		transitionMatrix = new Matrix<>(Double.class, stateNames.size());
		emissionMatrix = new Matrix<>(Double.class, stateNames.size(), observationNames.size());
		initialStateProbability = new Matrix<>(Double.class, stateNames.size(),1);
	}
	
	public HMM(List<String> stateNames, List<String> observationNames, List<Double> transitionValues, List<Double> emissionValues, List<Double> initialStateValues) {
		this.stateNames = stateNames;
		this.observationNames = observationNames;
		transitionMatrix = new Matrix<>(Double.class, stateNames.size());
		emissionMatrix = new Matrix<>(Double.class, stateNames.size(), observationNames.size());
		initialStateProbability = new Matrix<>(Double.class, stateNames.size(),1);
		MatrixFactory.fillMatrix(transitionMatrix, transitionValues.iterator());
		MatrixFactory.fillMatrix(emissionMatrix, emissionValues.iterator());		
		MatrixFactory.fillMatrix(initialStateProbability, initialStateValues.iterator());
	}

	public Tupple2<List<Integer>,List<Integer>> generateSequence(int size, Random random) {
		double p = random.nextDouble();
		int initialState = ProbabilityDistribution.drawObjectIndexFromDistribution(Arrays.asList(initialStateProbability.values[0]), p);
		return generateSequence(initialState, size, random);
	}

	
	public Tupple2<List<Integer>,List<Integer>> generateSequence(int initialStateIndex, int size, Random random) {
		List<Integer> sequenceState = new ArrayList<>(size);
		List<Integer> sequenceObs = new ArrayList<>(size);
		List<Double> nextStateProb = new ArrayList<>();
		int lastStateIndex = initialStateIndex;
		//
		// get initial observation probability
		nextStateProb.clear();
		for (int i = 0; i < emissionMatrix.width; i++) {
			nextStateProb.add(emissionMatrix.values[initialStateIndex][i]);
		}
		// generate initial observation
		double p = random.nextDouble();
		int nextObservationIndex = ProbabilityDistribution.drawObjectIndexFromDistribution(nextStateProb, p);
		//
		sequenceState.add(initialStateIndex);
		sequenceObs.add(nextObservationIndex);
		//
		for (int stepi = 0; stepi < size-1; stepi++) {
			nextStateProb.clear();
			// get next state probability
			for (int i = 0; i < transitionMatrix.width; i++) {
				nextStateProb.add(transitionMatrix.values[lastStateIndex][i]);
			}
			// generate next state
			 p = random.nextDouble();
			int nextStateIndex = ProbabilityDistribution.drawObjectIndexFromDistribution(nextStateProb, p);
			//
			
			// get next observation probability
			nextStateProb.clear();
			for (int i = 0; i < emissionMatrix.width; i++) {
				nextStateProb.add(emissionMatrix.values[nextStateIndex][i]);
			}
			// generate next observation
			p = random.nextDouble();
			nextObservationIndex = ProbabilityDistribution.drawObjectIndexFromDistribution(nextStateProb, p);
			//
		
			//
			System.out.println("step "+stepi+" : "+stateNames.get(lastStateIndex)+" -> "+stateNames.get(nextStateIndex)+" ~> "+observationNames.get(nextObservationIndex));
		
			sequenceState.add(nextStateIndex);
			sequenceObs.add(nextObservationIndex);
			lastStateIndex = nextStateIndex;
		}
		return new Tupple2(sequenceState,sequenceObs);
	}
	
	public List<Integer> vitterbi(List<Integer> observations) {
		Matrix<Double> T1 = new Matrix<>(Double.class, stateNames.size(), observations.size());
		Matrix<Integer> T2 = new Matrix<>(Integer.class, stateNames.size(), observations.size());
		int size = observations.size();
		// pi: initial probability
		// A: transition matrix of size K*K
		// B: observation matrix of size K*N
		// Y: observation sequence of length size
		// X: hidden vector of states (reconstructed by algo)
		//
		// for each state si
		//   T1[i,1] = pi_i * B_i,y1 
		//   T2[i,1] = 0
		for (int i = 0; i < stateNames.size(); i++) {
			T1.values[i][0] = initialStateProbability.values[i][0] * emissionMatrix.values[i][observations.get(0)];
			T2.values[i][0] = 0;
		}
		// for i in 2 .. size
		//   for each state sj
		//     T1[j,i] = max_k(T1[k,i-1] * A_k,j * B_j,y_i
		//     T2[j,i] = arg max_k ..same as above..
		for (int oiIndex = 1; oiIndex < size; oiIndex++) {
			int oi = observations.get(oiIndex);
			//System.out.println("oi "+oi);
			for (int sj = 0; sj < stateNames.size(); sj++) {
				//System.out.println("sj "+sj);
				double maxVal = Double.MIN_VALUE;
				int indexMaxVal = -1;
				for (int sk = 0; sk < stateNames.size(); sk++) {
					//System.out.println("sk "+sk);
					double val = T1.values[sk][oiIndex-1] * transitionMatrix.values[sk][sj] * emissionMatrix.values[sj][oi];
					if(val > maxVal) {
						maxVal = val;
						indexMaxVal = sk;
					}
				}
				T1.values[sj][oiIndex] = maxVal;
				T2.values[sj][oiIndex] = indexMaxVal;
				//System.out.println("MM "+indexMaxVal);
			}
		}
		// z_size = arg max_k T1[k,size]
		// x_size = s_z_size
		ArrayList<Integer> decodedStateSequence = new ArrayList<>(size);
		double maxVal = Double.MIN_VALUE;
		int indexMaxVal = -1;
		for (int sk = 0; sk < stateNames.size(); sk++) {
			double val = T1.values[sk][size-1];
			if(val > maxVal) {
				maxVal = val;
				indexMaxVal = sk;
			}
		}
		decodedStateSequence.add(0,indexMaxVal);
		// for i in size .. 2
		//    z_{i-1} = T2|z_i,i]
		//    x_{i-1} = s_z_{i-1}
		int zi = indexMaxVal;
		for (int oiIndex = observations.size()-1; oiIndex >= 1; oiIndex--) {
			int ziminusone = T2.values[zi][oiIndex];
			int ximinusone = ziminusone;
			decodedStateSequence.add(0,ximinusone);
			zi = ziminusone;
		}
		// return x
		return decodedStateSequence;
	}
	
	
	
	
	/* Example from Wikipedia
	 */
	
	public static void main(String[] args) {
		
		HMM hamsterHMM = new HMM(
				Arrays.asList("sleep", "eat", "run"),
				Arrays.asList("silence", "short sounds", "long sounds"),
				Arrays.asList(
						0.9, 0.05, 0.05,
						0.7, 0.  , 0.3,
						0.8, 0.  , 0.2),
				Arrays.asList(
						0.9, 0.1, 0.,
						0.2, 0.7, 0.1,
						0.1, 0.2, 0.7),
				Arrays.asList(
						0.4, 0.3, 0.3)
				);
		
		HMM diseaseHMM = new HMM(
				Arrays.asList("healhy", "ill"),
				Arrays.asList("normal", "cold", "dizzy"),
				Arrays.asList(
						0.5, 0.4, 0.1,
						0.1, 0.3, 0.6),
				Arrays.asList(
						0.5, 0.4, 0.1,
						0.1, 0.3, 0.6),
				Arrays.asList(
						0.6, 0.4)
				);
		
		List<String> states = new ArrayList<>();
		
		states.add("work");
		states.add("holiday");
		
		List<Double> transitionValues = Arrays.asList(
				0.9, 0.1,
				0.9, 0.1);
	
		List<String> observations = new ArrayList<>();
		
		observations.add("relax");
		observations.add("tense");
	
		List<Double> emissionValues = Arrays.asList(
				0.9, 0.1,
				0.9, 0.1);
		
		List<Double> initialStateValues = Arrays.asList(
				0.9, 0.1);		
		
		Random random = new Random();
		
		HMM hmm = new HMM(states, observations);
		
		MatrixFactory.fillMatrix(hmm.transitionMatrix, transitionValues.iterator());

		MatrixFactory.fillMatrix(hmm.emissionMatrix, emissionValues.iterator());
		
		MatrixFactory.fillMatrix(hmm.initialStateProbability, initialStateValues.iterator());

		Tupple2<List<Integer>,List<Integer>> seq = hmm.generateSequence(50, random);
		
		System.out.println("seq obs   : "+seq.second);
		System.out.println("seq states: "+seq.first);

		List<Integer> decodedStates = hmm.vitterbi(seq.second);
		
		System.out.println("seq st dec: "+decodedStates);
		
		int countMatching = 0;
		for (int i = 0; i < decodedStates.size(); i++) {
			if(decodedStates.get(i) == seq.first.get(i)) {
				countMatching++;
			}
		}
		
		System.out.println("matching: "+countMatching+" / "+decodedStates.size());
		
	}
}
