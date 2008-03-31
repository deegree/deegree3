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
public interface CommandResult {

    /**
     * 
     * @return command state description
     */
	public CommandState getState();

    /**
     * 
     * @return result value or <code>null</code> if a command does not have a result
     */
	public Object getValue();

}