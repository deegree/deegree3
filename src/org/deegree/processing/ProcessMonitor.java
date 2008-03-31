package org.deegree.processing;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public interface ProcessMonitor {

	/**
	 * 
	 * @param description
	 */
	public void updateStatus(String description);

	/**
	 * 
	 * @param itemsDone
	 * @param totalItems
	 * @param itemDescription
	 */
	public void updateStatus(int itemsDone, int totalItems, String itemDescription);

	/**
	 * 
	 * @param command
	 */
	public void setCommand(Command command);

}