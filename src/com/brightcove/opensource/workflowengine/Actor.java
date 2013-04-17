package com.brightcove.opensource.workflowengine;

import java.util.ArrayList;
import java.util.List;

import com.brightcove.opensource.workflowengine.record.Property;
import com.brightcove.opensource.workflowengine.record.Record;

/**
 * <p>
 *    With Workflow, forms the basis for a simple workflow engine.  An actor
 *    is a generic processor that can be chained together with other actors
 *    to form a workflow in order to process data.
 * <p>
 * 
 * <p>
 *    Each actor is both a Provider (provides data to upstream actors) and a
 *    Consumer (receives data from downstream actors).
 * </p>
 * 
 * <p>
 *    As a provider, the actor can call its downstream providers to obtain
 *    data and/or generate its own data (e.g. from reading a file), and then
 *    passes this data on to its consumers as Record objects.
 * </p>
 * 
 * <p>
 *    As a consumer, the actor receives data from its downstream providers
 *    and can do something with that data (e.g. save to a file) and/or pass
 *    the data on to its consumers as Record objects.
 * </p>
 * 
 * <p>
 *    The intention is that this class will be extended to perform specific
 *    tasks (e.g. reading a CSV file)
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class Actor implements Provider, Consumer {
	private List<Provider>         providers;
	private List<Consumer>         consumers;
	private Boolean                started;
	private List<Property<String>> properties;
	private Workflow               workflow;
	
	/**
	 * 
	 * <p>
	 *    Default constructor.
	 * </p>
	 * 
	 * @param workflow Workflow this actor is part of
	 * 
	 */
	public Actor(Workflow workflow){
		providers    = new ArrayList<Provider>();
		consumers    = new ArrayList<Consumer>();
		properties   = new ArrayList<Property<String>>();
		started      = false;
		
		this.workflow = workflow;
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Provider#run()
	 */
	public void run(){
		if(! started){
			started = true;
			
			for(Provider provider : providers){
				provider.run();
			}
		}
	}
	
	public void finalize(){
		
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Consumer#handleRecord(com.brightcove.opensource.workflowengine.Record)
	 */
	public void handleRecord(Record record){
		// ToDo - what to do when records come in before we've "started" - is that possible?
		
		for(Consumer consumer : consumers){
			consumer.handleRecord(record);
		}
	}
	
	/**
	 * <p>
	 *    Returns true if this actor has any consumers registered with it.
	 *    Actors with no consumers are considered "terminal" - meaning they
	 *    can be started from the workflow rather than from another actor.
	 * </p>
	 * 
	 * @return True if this actor has any consumers registered with it
	 */
	public Boolean hasConsumers(){
		return (! consumers.isEmpty());
	}
	
	/**
	 * <p>
	 *    Registers a provider for this actor to pull data from
	 * </p>
	 * 
	 * @param provider Provider to register
	 */
	public void addProvider(Provider provider){
		this.providers.add(provider);
	}
	
	/**
	 * <p>
	 *    Registers a consumer to provide data to
	 * </p>
	 * 
	 * @param consumer Consumer to register
	 */
	public void addConsumer(Consumer consumer){
		this.consumers.add(consumer);
	}
	
	/**
	 * <p>
	 *    Adds a configuration parameter
	 * </p>
	 * 
	 * @param property Configuration key=value pair
	 */
	public void addProperty(Property<String> property){
		this.properties.add(property);
	}
	
	/**
	 * <p>
	 *    Gets the first config property with the name specified
	 * </p>
	 * 
	 * @param name Name of property to get
	 * @return First property found, or null if none is found
	 */
	public Property<String> getFirstProperty(String name){
		for(Property<String> property : properties){
			if((property != null) && (property.getName() != null) && property.getName().equals(name)){
				return property;
			}
		}
		return null;
	}
	
	/**
	 * <p>
	 *    Gets the first config value of the first property found with the
	 *    name specified.
	 * </p>
	 * 
	 * @param name Name of property to search for
	 * @return Value of property if found, otherwise null
	 */
	public String getFirstPropertyValue(String name){
		Property<String> prop = getFirstProperty(name);
		if(prop == null){
			return null;
		}
		if(prop.getValue() == null){
			return null;
		}
		return prop.getValue();
	}
	
	/**
	 * <p>
	 *    Returns true if this actor has been started - i.e. run() has been
	 *    called from either the workflow or another actor.
	 * </p>
	 * 
	 * @return True if actor has been started
	 */
	public Boolean hasStarted(){
		return started;
	}
	
	/**
	 * <p>
	 *    Records a message via the workflow's logging mechanisms
	 * </p>
	 * 
	 * @param message Message to log
	 */
	public void log(String message){
		workflow.log(message);
	}
	
	/**
	 * <p>
	 *    Records an error message viw the workflow's logging mechanism
	 * </p>
	 * 
	 * @param message Error message to log
	 */
	public void logError(String message){
		workflow.logError(message);
	}
	
	/**
	 * <p>
	 *    Uses the workflow to log a message and then exit the virtual machine
	 *    with a non-zero exit code.
	 * </p>
	 * 
	 * @param message Message to log
	 */
	public void die(String message){
		workflow.die(message);
	}
}
