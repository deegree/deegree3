package org.deegree.processing;

import java.util.Calendar;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public interface CommandState {

    public enum STATE {
        finished, cancelled, paused, processing
    };

    /**
     * 
     * @return current command execution state
     */
    public STATE getState();

    /**
     * 
     * @return detailed descrition of the current state (should return an empts string instead of
     *         <code>null</code> if no description is available
     */
    public String getDescription();

    /**
     * 
     * @return timestamp of commiting a {@link Command} to the {@link CommandProcessor}
     */
    public Calendar getIncomingOrderTimestamp();

    /**
     * 
     * @return timestamp of starting a {@link Command}
     */
    public Calendar getStartExecutionTimestamp();

    /**
     * 
     * @return timestamp when execution of a {@link Command} has been finished
     */
    public Calendar getExecutionFinishedTimestamp();

    /**
     * This method returns the real duration required for processing a {@link Command}. This is the
     * time between starting timestamp and finishing timestamp less pause times.
     * 
     * @return duration (millis) of a {@link Command} execution.
     */
    public long getRealExecutionDuration();

}