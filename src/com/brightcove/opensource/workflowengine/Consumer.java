package com.brightcove.opensource.workflowengine;

import com.brightcove.opensource.workflowengine.record.Record;

/**
 * <p>
 *    A type of actor that receives data from a Provider actor and acts on it
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public interface Consumer {
	/**
	 * <p>
	 *    Called by a Provider to pass data to this Consumer
	 * </p>
	 * 
	 * @param record Data passed by provider
	 */
	public void handleRecord(Record record);
}
