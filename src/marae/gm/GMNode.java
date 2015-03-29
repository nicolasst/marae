package marae.gm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import moorea.graphs.SimpleNode;


/**
 * This class provide a generic node for a graphical model. No hypothesis
 * are made on the type of information processed and exchanged by nodes. These
 * could be discrete probability function, discrete utility table, or other
 * data types.
 * 
 * Any instantiation must:
 * - specify the type of data processed
 * - specify the type of the element of the scope
 * - redefine the data combination and sumarisation operators
 * 
 * Because this class is abstract, any instanciation must also provide
 * an ad-hoc initialiszer.
 * 
 * @author nicolas
 *
 * @param <K>
 */

public abstract class GMNode<V, K> extends SimpleNode<GMNode<V, K>> {

	// scope over which information is defined
	protected List<V> scope;
	
	// current state
	protected K information;
	
	// messages
	protected Map<GMNode<V, K>, K> receivedMessages;

	// pre-allocated functions
	protected List<K> subFunctions;
	
	public GMNode(Integer id) {
		super(id);
		subFunctions = new LinkedList<>();
		//inMessages = new LinkedList<>();
		receivedMessages = new HashMap<>();
	}

	public List<V> getIntersectedScope(GMNode c) {
		return getIntersectedScope(c.getScope());
	}
	
	public List<V> getIntersectedScope(List<V> otherScope) {
		return new LinkedList<>(org.apache.commons.collections4.CollectionUtils.intersection(scope, otherScope));
	}
	
	public K summariseInformation(GMNode c) {
		return summariseInformation(getIntersectedScope(c.getScope()));
	}
	
	public abstract K summariseInformation(List<V> newScope);
	
	public abstract K mergeInformation();
	
	public void updateMessage(K message, GMNode<V, K> sender) {
		receivedMessages.put(sender, message);
	}
	
	//	

	public List<V> getScope() {
		return scope;
	}

	public void setScope(List<V> scope) {
		this.scope = scope;
	}

	public List<K> getSubFunctions() {
		return subFunctions;
	}

	public void setSubFunctions(List<K> subFunctions) {
		this.subFunctions = subFunctions;
	}
	
	public K getInformation() {
		return information;
	}

	public void setInformation(K information) {
		this.information = information;
	}
	
	public Map<GMNode<V, K>, K> getReceivedMessages() {
		return receivedMessages;
	}
	
	public String toString() {
		return "JTC_"+id;
	}
	

}
