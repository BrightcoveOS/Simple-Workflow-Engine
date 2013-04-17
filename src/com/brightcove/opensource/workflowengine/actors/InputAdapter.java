package com.brightcove.opensource.workflowengine.actors;

import com.brightcove.opensource.workflowengine.Actor;
import com.brightcove.opensource.workflowengine.Workflow;
import com.brightcove.opensource.workflowengine.record.Property;
import com.brightcove.opensource.workflowengine.record.Record;

/**
 * <p>
 *    Generic input adapter - for demonstration purposes only.
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class InputAdapter extends Actor {
	
	/**
	 * <p>
	 *    Default constructor - workflow this actor is part of must be
	 *    provided
	 * </p>
	 * 
	 * @param workflow Workflow this actor will be part of
	 */
	public InputAdapter(Workflow workflow){
		super(workflow);
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#run()
	 */
	public void run(){
		if(! hasStarted()){
			Record           test = new Record();
			Property<String> id   = new Property<String>("ID", "FOO");
			test.addProperty(id);
			handleRecord(test);
		}
	}
}
