package org.deegree.core.processing;

import java.util.Calendar;

import org.deegree.model.types.Identifier;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public interface CommandProcessor {

    /**
     * executes a {@link Command} asynchronoulsy 
     * 
     * @param command
     *            command to perform; if command is an instance of {@link PersitableCommand} it
     *            will be persited into the configured backend before executing
     * @param timeout
     *            timeout (millis) after processing will be canceled
     * @param executionStart
     *            date/time when execution shall start or <code>null</code> if it shall start
     *            directly
     * @param cyclic
     *            true if a command shall be executed in a cycle
     * @param interval
     *            time length of the cycle (millis)
     * @param repeatOnFailture
     *            nummber of tries to execute a command if execution failed. If repeatOnFailture > 0
     *            the interval will be used to determine the time lags between tries.
     */
    public void executeAsynchronously( Command command, long timeout, Calendar executionStart, boolean cyclic,
                                       long interval, int repeatOnFailture );

   
    /**
     * Executes a {@link Command} synchronously. If the pass {@link Command} can not be processed in
     * passed timeout an {@link CommandProcessingException} will be thrown
     * 
     * @param command
     * @param timeout
     */
    public CommandResult executeSychronously( Command command, long timeout )
                            throws CommandProcessingException;

    /**
     * 
     * @param identifier
     */
    public void cancelCommand( Identifier identifier );

    /**
     * 
     * @param identifier
     */
    public void pauseCommand( Identifier identifier );

    /**
     * 
     * @param idenditfier
     */
    public void restartCommand( Identifier idenditfier );

    /**
     * 
     * @param identifier
     */
    public CommandState getStatus( Identifier identifier );

    /**
     * registers listeners that will be informed if processing of a command has been finished.
     * 
     * @param listener
     */
    public void addCommandProcessedListener( CommandProcessedListener listener );

    /**
     * 
     * @param listener
     */
    public void removeCommandProcessedListener( CommandProcessedListener listener );

}