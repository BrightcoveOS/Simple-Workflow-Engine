package com.brightcove.opensource.workflowengine.record;

/**
 * <p>
 *    A generic name=value string pair
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class Property<T> {
	private String name;
	private T      value;
	
	/**
	 * <p>
	 *    Generic constructor
	 * </p>
	 */
	public Property(){
		this.name  = null;
		this.value = null;
	}
	
	/**
	 * <p>
	 *    Constructor specifying name and value
	 * </p>
	 * 
	 * @param name Name of property
	 * @param value Value of property
	 */
	public Property(String name, T value){
		this.name  = name;
		this.value = value;
	}
	
	/**
	 * <p>
	 *    Returns name of property
	 * </p>
	 * 
	 * @return Name of property
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * <p>
	 *    Sets the name of this property
	 * </p>
	 * 
	 * @param name Name of property
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * <p>
	 *    Gets the value of this property
	 * </p>
	 * 
	 * @return Value of this property
	 */
	public T getValue(){
		return value;
	}
	
	/**
	 * <p>
	 *    Sets the value of this property
	 * </p>
	 * 
	 * @param value Value of this property
	 */
	public void setValue(T value){
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "[Property name='" + name + "' value='" + value + "']";
	}
}
