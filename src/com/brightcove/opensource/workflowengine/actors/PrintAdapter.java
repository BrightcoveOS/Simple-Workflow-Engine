package com.brightcove.opensource.workflowengine.actors;

import java.util.HashMap;
import java.util.Map;

import com.brightcove.opensource.workflowengine.Actor;
import com.brightcove.opensource.workflowengine.Workflow;
import com.brightcove.opensource.workflowengine.record.Property;
import com.brightcove.opensource.workflowengine.record.Record;

/**
 * <p>
 *    Utility actor that prints each record that passes through it to the
 *    workflow logging utility.
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class PrintAdapter extends Actor {
	Map<String, Boolean> printableFields;
	
	/**
	 * <p>
	 *    Default constructor - workflow this actor is part of must be
	 *    provided
	 * </p>
	 * 
	 * @param workflow Workflow this actor will be part of
	 */
	public PrintAdapter(Workflow workflow){
		super(workflow);
		
		printableFields = null;
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#run()
	 */
	public void run(){
		if(! hasStarted()){
			String fieldsProp = getFirstPropertyValue("fields");
			if(fieldsProp != null){
				printableFields = new HashMap<String, Boolean>();
				for(String fieldName : fieldsProp.split(",")){
					printableFields.put(fieldName, true);
				}
				log("Printable fields: '" + printableFields.keySet() + "'.");
			}
		}
		
		super.run();
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#handleRecord(com.brightcove.opensource.workflowengine.Record)
	 */
	public void handleRecord(Record record){
		log("[PrintAdapter] Record:");
		if(record == null){
			log("    null");
		}
		else{
			for(Property<?> property : record.getAllProperties()){
				if(property == null){
					log("    [null]");
				}
				else{
					if((printableFields == null) || printableFields.containsKey(property.getName())){
						log("    [" + property.getName() + "=" + property.getValue() + "]");
					}
				}
			}
		}
		super.handleRecord(record);
	}
}
