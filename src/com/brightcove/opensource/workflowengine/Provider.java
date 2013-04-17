package com.brightcove.opensource.workflowengine;

/**
 * <p>
 *    A type of actor that provides data to Consumer actors.
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public interface Provider {
	/**
	 * <p>
	 *    Begins execution of this provider.  If called more than once,
	 *    usually only the first call should actually do anything.
	 * </p>
	 */
	public void run();
}
