package org.deegree.processing;

import java.net.URL;

/**
 * Just a persitable command requiers informations about a target that is independ of 
 * the JVM executing a command and that shall be informed if command execution has 
 * been finished. 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public interface PersitableCommand extends Command {

	/**
	 * 
	 * @param mailAddress
	 */
	public void setResponseHandler(String mailAddress);

	/**
	 * 
	 * @param parameterName
	 * @param url
	 */
	public void setResponseHandler(String parameterName, URL url);

}