package marae.gm;

import java.util.List;

import moorea.maths.algebra.Semiring;

/**
 * This class is used to perfrom ad-hoc initilisation of HyperMatrixGDLNode nodes.
 * 
 * @author nicolas
 *
 */

public class HyperMatrixGDLNodeInitialiser extends GMNodeInitialiser<HyperMatrixGDLNode>{

	Semiring sr;
	
	public HyperMatrixGDLNodeInitialiser(Semiring sr) {
		super(HyperMatrixGDLNode.class);
		this.sr = sr;
	}
	
	@Override
	public void configureClique(HyperMatrixGDLNode c) {
		((HyperMatrixGDLNode)c).setSemiring(sr);
	}

}
