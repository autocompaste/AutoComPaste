package acp.beans;

import java.util.List;

/**
 * This class represent operation/method invoking request initiated by the network 
 * clients (eg. Mozilla Firefox Plugin, Microsoft Add-in). This allows the clients 
 * to denote the method to invoked, and the parameters to pass into the invoking method. 
 * The parameters type is set to Object so that the array can contain a combination of 
 * object type.
 * 
 * @author Loke Yan Hao
 */
public class Operation {
	private String name;
	private List<Object> parameters;
	
	/**
	 * Basic constructor to create an instance of Operation object.
	 * @param name			the name of the invoking method.
	 * @param parameters	the list of parameters to pass to the invoking method. 
	 */
	public Operation(String name, List<Object> parameters) {
		super();
		this.name = name;
		this.parameters = parameters;
	}
	
	/**
	 * Return the name of the invoking method.
	 * 
	 * @return	the method name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the invoking method name.
	 * @param name	the method name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Return the list of parameters to pass to the invoking method.
	 * 
	 * @return		the list of parameters to pass to the invoking method.
	 */
	public List<Object> getParameters() {
		return parameters;
	}
	
	/**
	 * Set the list of parameters to pass to the invoking method.
	 * 
	 * @param parameters	the list of parameters to pass to the invoking method.
	 */
	public void setParameters(List<Object> parameters) {
		this.parameters = parameters;
	}
}
