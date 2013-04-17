package com.brightcove.opensource.workflowengine;

import java.io.File;

import com.brightcove.commons.system.commandLine.CommandLineProgram;
import com.brightcove.opensource.workflowengine.Workflow;

/**
 * <p>
 *    A command line program to load a Workflow from an XML configuration file
 *    and then begin execution.
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class ExecWorkflow extends CommandLineProgram {
	
	/**
	 * <p>
	 *    Command line execution
	 * </p>
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String[] args){
		ExecWorkflow test = new ExecWorkflow();
		
		test.allowNormalArgument("workflow-file", "--workflow-file <path>", "--workflow-file <path>: Path to workflow XML config file", true);
		
		test.parseArguments(args);
		test.run(args);
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.commons.system.commandLine.CommandLineProgram#run(java.lang.String[])
	 */
	public void run(String[] args){
		File configFile = new File(getNormalArgument("workflow-file"));
		try {
			Workflow workflow = new Workflow().fromXML(configFile);
			workflow.run();
		}
		catch (Exception e) {
			usage(e);
		}
	}
}
