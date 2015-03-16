package marae.gm;

import java.util.List;

/**
 * This class must be extend to perform initialisation of specific GMNode instance.
 * 
 * @author nicolas
 *
 * @param <C>
 */

public abstract class GMNodeInitialiser<C extends GMNode> {
	
	Class cliqueClass;
	
	public GMNodeInitialiser(Class ck) {
		cliqueClass = ck;
	}
	
	public abstract void configureClique(C c);
	
	public Class getCliqueClass() {
		return cliqueClass;
	}
	
}
