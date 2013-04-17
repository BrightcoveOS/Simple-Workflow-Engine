package com.brightcove.opensource.workflowengine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.brightcove.commons.misc.logging.LogUtils;
import com.brightcove.commons.xml.XalanUtils;
import com.brightcove.opensource.workflowengine.record.Property;

/**
 * <p>
 *    This forms the base of a simple workflow engine.  Actors are stringed
 *    together as providers and consumers, each producing Record objects to
 *    pass to the next actor in the workflow.
 * </p>
 * 
 * <p>
 *    The workflow will start any actor that does not have any consumers (i.e.
 *    a terminal actor), which in turn will call all of their providers to
 *    get records.  Each provider can call their providers for data or
 *    generate their own to pass back to all of its consumers (including
 *    the one that called it).
 * </p>
 * 
 * <p>
 *    The primary use case is a single threaded workflow for now.  I.e. while
 *    the structure of the workflow allows a provider to feed multiple
 *    consumers, the behavior of this isn't yet well tested (e.g. what happens
 *    when a provider feeds a consumer before that consumer is "started").
 *    That use case is at your own risk for now.
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class Workflow {
	private List<Actor> actors;
	private Logger      logger;
	
	/**
	 * <p>
	 *    Basic constructor
	 * </p>
	 * 
	 */
	public Workflow(){
		init();
	}
	
	private void init(){
		actors = new ArrayList<Actor>();
		logger = LogUtils.getLogger(this.getClass().getCanonicalName());
	}
	
	/**
	 * <p>
	 *    Adds an actor to the workflow
	 * </p>
	 * 
	 * @param actor Actor to be added
	 */
	public void addActor(Actor actor){
		this.actors.add(actor);
	}
	
	/**
	 * <p>
	 *    Starts the workflow.  All of the terminal actors will be run, which
	 *    should in turn call all of their producers.
	 * </p>
	 */
	public void run(){
		// Start all the end points, and let them start their providers
		for(Actor actor : actors){
			if(! actor.hasConsumers()){
				actor.run();
			}
		}
		
		for(Actor actor : actors){
			actor.finalize();
		}
	}
	
	/**
	 * <p>
	 *    Creates a new workflow from an XML configuration file.
	 * <p>
	 * 
	 * @param xmlFile XML configuration file
	 * @return Workflow object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Workflow fromXML(File xmlFile) throws ParserConfigurationException, SAXException, IOException, TransformerException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		init();
		
		Document   doc        = XalanUtils.parseXml(xmlFile, false);
		Element    root       = doc.getDocumentElement();
		List<Node> actorNodes = XalanUtils.getNodesFromXPath(root, "/workflow/actor");
		
		Map<String, Actor> actorsMap = new HashMap<String, Actor>();
		for(Node actorNode : actorNodes){
			String         actorClassName = XalanUtils.getStringFromXPath(actorNode, "@class");
			String         actorName      = XalanUtils.getStringFromXPath(actorNode, "@name");
			Constructor<?> constructor    = Class.forName(actorClassName).getConstructor(Workflow.class);			
			Actor          actor          = null;
			
			try{
				actor = (Actor)constructor.newInstance(this);
			}
			catch(InvocationTargetException ite){
				die("Couldn't create actor '" + actorClassName + "' (" + actorClassName + ").  InvocationTargetException caught: '" + ite + "' (" + ite.getCause() + ").");
			}
			
			// Class<?> actorClass     = Class.forName(actorClassName);
			// Actor    actor          = (Actor)actorClass.newInstance(this);
			
			actorsMap.put(actorName, actor);
			
			List<Node> propertyNodes = XalanUtils.getNodesFromXPath(actorNode, "property");
			for(Node propertyNode : propertyNodes){
				String           propName = XalanUtils.getStringFromXPath(propertyNode, "@name");
				String           propVal  = XalanUtils.getStringFromXPath(propertyNode, ".");
				Property<String> prop     = new Property<String>(propName, propVal);
				actor.addProperty(prop);
			}
		}
		
		for(Node actorNode : actorNodes){
			String     actorName = XalanUtils.getStringFromXPath(actorNode, "@name");
			List<Node> providers = XalanUtils.getNodesFromXPath(actorNode, "provider");
			List<Node> consumers = XalanUtils.getNodesFromXPath(actorNode, "consumer");
			Actor      actor     = actorsMap.get(actorName);
			
			for(Node providerNode : providers){
				String providerName = XalanUtils.getStringFromXPath(providerNode, "@name");
				Actor  provider     = actorsMap.get(providerName);
				actor.addProvider(provider);
			}
			
			for(Node consumerNode : consumers){
				String consumerName = XalanUtils.getStringFromXPath(consumerNode, "@name");
				Actor  consumer     = actorsMap.get(consumerName);
				actor.addConsumer(consumer);
			}
		}
		
		for(String actorName : actorsMap.keySet()){
			addActor(actorsMap.get(actorName));
		}
		
		return this;
	}
	
	public void log(String message){
		logger.info(message);
	}
	
	public void logError(String message){
		logger.severe(message);
	}
	
	public void die(String message){
		logError(message);
		System.exit(1);
	}
}
