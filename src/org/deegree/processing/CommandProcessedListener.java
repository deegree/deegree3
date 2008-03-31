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
public interface CommandProcessedListener {

    /**
     * 
     * @param event
     */
    public void commandPerformed( CommandProcessedEvent event );

    /**
     * 
     * @param event
     */
    public void commandStarted( CommandProcessedEvent event );

    /**
     * 
     * @param event
     */
    public void commandStoped( CommandProcessedEvent event );

    /**
     * 
     * @param event
     */
    public void commandResumed( CommandProcessedEvent event );

    /**
     * 
     * @param event
     */
    public void commandCancelled( CommandProcessedEvent event );

}