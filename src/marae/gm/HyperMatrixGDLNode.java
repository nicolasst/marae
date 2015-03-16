package marae.gm;

import java.util.LinkedList;
import java.util.List;

import moorea.maths.algebra.Semiring;
import moorea.maths.hypermatrix.HyperMatrix;
import moorea.maths.hypermatrix.HyperMatrixAlgorithms;
import moorea.maths.objects.DiscreteVariable;


/**
 * GMNode whose information are hyper-matrices and scope variables are discrete variables.
 * 
 * Because the type of the information contained in the hypermatrix is not specified, this
 * class can be reused to derive probabilistic table, utility table or constraint tables.
 * This information is stored in the Semiring object, and is reused directly by the
 * generic merging and summatisation operations.
 * 
 * @author nicolas
 *
 */

public class HyperMatrixGDLNode<K> extends GMNode<DiscreteVariable, HyperMatrix<DiscreteVariable, K>> {

	public Semiring<K> semiring;
	
	public HyperMatrixGDLNode(Integer id) {
		super(id);
	}

	// semiring getter/setter
	
	public Semiring<K> getSemiring() {
		return semiring;
	}

	public void setSemiring(Semiring<K> semiring) {
		this.semiring = semiring;
	}

	// summarisation and merging redefinition

	public HyperMatrix<DiscreteVariable, K> summariseInformation(List<DiscreteVariable> newScope) {
		HyperMatrix<DiscreteVariable, K> hm = HyperMatrixAlgorithms.summarise(getSemiring().generateNewDotReducer(), newScope, information);
		return hm;
	}
	
	public HyperMatrix<DiscreteVariable,K> mergeInformation() {
		List<HyperMatrix<DiscreteVariable,K>> informationToMerge = new LinkedList<>();

		informationToMerge.addAll(receivedMessages.values());
		informationToMerge.addAll(subFunctions);
		
		return HyperMatrixAlgorithms.mergeDiscreteFunctionsList(getSemiring().generateNewSumReducer(), getScope(), informationToMerge);
	}

	public String toString() {
		return "SRHMC_"+id;
	}

}
