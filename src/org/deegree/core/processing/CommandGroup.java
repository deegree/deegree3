package org.deegree.core.processing;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public interface CommandGroup extends Command {

	/**
	 * 
	 * @param command
	 */
	public void addCommand(Command command);

	/**
	 * 
	 * @param command
	 */
	public void removeCommand(Command command);

	/**
	 * 
	 * @param executionPlan
	 */
	public void setExecutionPlan(ExecutionPlan executionPlan);

}