package com.brightcove.opensource.workflowengine.actors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.brightcove.opensource.workflowengine.Actor;
import com.brightcove.opensource.workflowengine.Workflow;
import com.brightcove.opensource.workflowengine.record.Property;
import com.brightcove.opensource.workflowengine.record.Record;

/**
 * <p>
 *    Generic output adapter - writes all records to a file
 * </p>
 * 
 * <p>
 *    This actor will write all records it receives from downstream providers
 *    to a file on disk.
 * </p>
 * 
 * <p>
 *    The following configuration properties will be honored:<ul>
 *        <li>output-file - Location of file to write</li>
 *        <li>character-set - Character set to use for writing ("UTF-8" if omitted)</li>
 *    </ul>
 * </p>
 * 
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class OutputAdapter extends Actor {
	Writer writer;
	
	/**
	 * <p>
	 *    Default constructor - workflow this actor is part of must be
	 *    provided
	 * </p>
	 * 
	 * @param workflow Workflow this actor will be part of
	 */
	public OutputAdapter(Workflow workflow){
		super(workflow);
		
		writer = null;
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#run()
	 */
	public void run(){
		if(! hasStarted()){
			Property<String> outputFileProp = getFirstProperty("output-file");
			if((outputFileProp == null) || (outputFileProp.getValue() == null)){
				die("OutputAdapter requires an output-file property.");
			}
			File outputFile = new File(outputFileProp.getValue());
			
			String           characterSet = "UTF-8";
			Property<String> charSetProp  = getFirstProperty("character-set");
			if((charSetProp != null) && (charSetProp.getValue() != null)){
				characterSet = charSetProp.getValue();
			}
			
			try {
				writer = new OutputStreamWriter(new FileOutputStream(outputFile), characterSet);
			}
			catch (UnsupportedEncodingException uee) {
				die("OutputAdapter given invalid character encoding.  Exception caught: '" + uee + "'.");
			}
			catch (FileNotFoundException fnfe) {
				die("OutputAdapter can't find file to write.  Exception caught: '" + fnfe + "'.");
			}
		}
		
		super.run();
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#handleRecord(com.brightcove.opensource.workflowengine.Record)
	 */
	public void handleRecord(Record record){
		try {
			writer.write("{---------- RECORD ----------}\n" + record + "\n");
		}
		catch (IOException ioe) {
			die("Couldn't write record to file.  Exception caught: '" + ioe + "'.");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#finalize()
	 */
	public void finalize(){
		try {
			writer.close();
		}
		catch (IOException ioe) {
			die("Couldn't close file.  Exception caught: '" + ioe + "'.");
		}
	}
}
