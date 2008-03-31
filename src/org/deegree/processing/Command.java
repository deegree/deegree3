package org.deegree.processing;

import org.deegree.model.types.Identifier;
import org.deegree.security.model.User;

/**
 * @author Administrator
 * @version 1.0
 * @created 12-Mrz-2008 13:25:54
 */
public interface Command {

    /**
     * 
     * @return unique command identifier
     */
    public Identifier getIdentifier();

    /**
     * 
     * @return command processing result
     */
    public CommandResult getResult();

    /**
     * 
     * @return current status of a command
     */
    public CommandState getStatus();

    /**
     * executes a command
     *
     */
    public void execute();
    
    /**
     * cancels command execution
     *
     */
	public void cancel();	
	
    /**
     * pauses command execution
     *
     */
	public void pause();

    /**
     * restarts command execution if it has been paused
     *
     */
	public void resume();

	/**
	 * 
	 * @param priority
	 */
	public void setPriority(int priority);

    /**
     * 
     * @return owner of a command (<code>null</code> not owned by anyone but just by deegree)
     */
	public User getOwner();

    /**
     * sets a listener to be informed im command execution has been finished or canceled
     * 
     * @param listener
     */
	public void setCommandProcessedListener(CommandProcessedListener listener);

	/**
	 * 
	 * @param processMonitor
	 */
	public void setProcessMonitor(ProcessMonitor processMonitor);

}