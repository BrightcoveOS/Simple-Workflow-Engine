package com.brightcove.opensource.workflowengine.record;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *    Generic data transport object.  A record consists of an arbitrary list
 *    of name=value string pairs (represented as Property objects)
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class Record {
	private List<Property<?>> properties;
	
	/**
	 * <p>
	 *    Generic constructor
	 * </p>
	 */
	public Record(){
		properties = new ArrayList<Property<?>>();
	}
	
	/**
	 * <p>
	 *    Gets all properties with the specified name
	 * </p>
	 * 
	 * @param name Name of property to find
	 * @return List of properties matching name provided
	 */
	public List<Property<?>> getProperty(String name){
		List<Property<?>> ret = new ArrayList<Property<?>>();
		
		if(name == null){
			return ret;
		}
		
		for(Property<?> property : properties){
			if(name.equals(property.getName())){
				ret.add(property);
			}
		}
		
		return ret;
	}
	
	/**
	 * <p>
	 *    Returns the first property with a name matching the provided string
	 * </p>
	 * 
	 * @param name Name of property to search for
	 * @return First property found
	 */
	public Property<?> getFirstProperty(String name){
		if(name == null){
			return null;
		}
		
		for(Property<?> property : properties){
			if(name.equals(property.getName())){
				return property;
			}
		}
		
		return null;
	}
	
	/**
	 * <p>
	 *    Adds a property to this record
	 * </p>
	 * 
	 * @param property Property to add
	 */
	public void addProperty(Property<?> property){
		properties.add(property);
	}
	
	/**
	 * <p>
	 *    Removes all properties with the specified name
	 * </p>
	 * 
	 * @param name Name of property to remove
	 */
	public void removeProperty(String name){
		if(name == null){
			return;
		}
		
		for(Property<?> current : properties){
			if((current.getName() != null) && current.getName().equals(name)){
				properties.remove(current);
			}
		}
	}
	
	/**
	 * <p>
	 *    Removes a specific property
	 * </p>
	 * 
	 * @param property Property to remove
	 */
	public void removeProperty(Property<?> property){
		if((property == null) || (property.getName() == null)){
			return;
		}
		
		properties.remove(property);
	}
	
	/**
	 * <p>
	 *    Returns a list of all properties added to this record
	 * </p>
	 * 
	 * @return All properties added to this record
	 */
	public List<Property<?>> getAllProperties(){
		return properties;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuffer buffer = new StringBuffer("[Record ");
		for(Property<?> property : properties){
			buffer.append(""+property);
		}
		buffer.append("]");
		
		return buffer.toString();
	}
}
