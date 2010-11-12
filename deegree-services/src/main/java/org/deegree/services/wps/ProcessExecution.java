//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/svn_classfile_header_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.services.wps;

import java.net.URL;
import java.util.Date;
import java.util.List;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.protocol.wps.WPSConstants.ExecutionState;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.wps.execute.ExecuteRequest;
import org.deegree.services.wps.execute.ExecuteResponse;
import org.deegree.services.wps.execute.RequestedOutput;
import org.deegree.services.wps.storage.StorageLocation;

/**
 * Encapsulates the status of a {@link WPSProcess} execution.
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 */
public class ProcessExecution implements ProcessletExecutionInfo {

    private ExecuteRequest request;

    private ExecutionState processState;

    private String acceptedMessage;

    private String succeededMessage;

    private String startedMessage;

    private String pausedMessage;

    private OWSException failedException;

    private long startTime = -1;

    private long finishTime = -1;

    private int percentCompleted;

    private StorageLocation responseStorage;

    private URL serviceInstance;

    private List<RequestedOutput> outputParams;

    private ProcessletOutputs outputs;

    /**
     * Creates a new {@link ProcessExecution} for a {@link Processlet} that has been accepted for execution.
     * <p>
     * Processing state is {@link ExecutionState#ACCEPTED}.
     * </p>
     * 
     * @param request
     * @param responseStorage
     * @param serviceInstance
     * @param outputParams
     * @param outputs
     */
    public ProcessExecution( ExecuteRequest request, StorageLocation responseStorage, URL serviceInstance,
                             List<RequestedOutput> outputParams, ProcessletOutputs outputs ) {
        this.request = request;
        this.responseStorage = responseStorage;
        this.serviceInstance = serviceInstance;
        this.outputParams = outputParams;
        this.outputs = outputs;
        this.processState = ExecutionState.ACCEPTED;
    }

    /**
     * Returns the current processing state.
     * 
     * @return the current processing state
     */
    public ExecutionState getExecutionState() {
        return processState;
    }

    /**
     * Returns the percentage of process that has been completed, where 0 means the process has just started, and 99
     * means the process is almost complete. This value is expected to be accurate to within ten percent.
     * 
     * @return the percentCompleted, a number between 0 and 99
     */
    public int getPercentCompleted() {
        return percentCompleted;
    }

    /**
     * Returns optional additional human-readable text associated with the acceptance of the process execution.
     * 
     * @return optional additional human-readable text, null if it is not available
     */
    public String getAcceptedMessage() {
        return acceptedMessage;
    }

    /**
     * Returns optional additional human-readable text associated with the starting of the process execution.
     * 
     * @return optional additional human-readable text, null if it is not available
     */
    public String getStartMessage() {
        if ( startedMessage == null ) {
            return "Process execution started@" + DateUtils.formatISO8601Date( new Date( startTime ) );
        }
        return startedMessage;
    }

    /**
     * Returns the time when the process execution has been started.
     * 
     * @return the time when the process execution has been started (in milliseconds) or -1 if the process has not been
     *         started yet
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the time that the process execution has been finished.
     * 
     * @return the time that the process execution has been finished (in milliseconds) or -1 if the process has not been
     *         finished yet
     */
    public long getFinishTime() {
        return finishTime;
    }

    /**
     * Returns the "Identifier" for this process.
     * 
     * @return Returns the CodeType or "Identifier" for this process.
     */
    public CodeType getProcessId() {
        return request.getProcessId();
    }

    /**
     * Returns optional additional human-readable text associated with the pausing of the process execution.
     * 
     * @return optional additional human-readable text, null if it is not available
     */
    public String getPauseMessage() {
        return pausedMessage;
    }

    /**
     * Returns optional additional human-readable text associated with the successful finishing of the process
     * execution.
     * 
     * @return optional additional human-readable text, null if it is not available
     */
    public String getSucceededMessage() {
        if ( succeededMessage == null && finishTime >= 0 ) {
            return "Process execution succeeded@" + DateUtils.formatISO8601Date( new Date( finishTime ) );
        }
        return succeededMessage;
    }

    /**
     * Returns the exception that describes the reason for the failure of the process execution.
     * 
     * @return the exception that describes the reason for the failure of the process execution.
     */
    public OWSException getFailedException() {
        return failedException;
    }

    /**
     * Sets the processing state to {@link ExecutionState#STARTED}.
     */
    void setStarted() {
        this.processState = ExecutionState.STARTED;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Sets the processing state to {@link ExecutionState#SUCCEEDED}.
     * 
     * @param msg
     *            additional human-readable client information, may be null
     */
    void setSucceeded( String msg ) {
        this.processState = ExecutionState.SUCCEEDED;
        this.finishTime = System.currentTimeMillis();
        this.succeededMessage = msg;
    }

    /**
     * Sets the processing state to {@link ExecutionState#PAUSED}.
     * 
     * @param msg
     *            additional human-readable client information, may be null
     */
    void setPaused( String msg ) {
        this.processState = ExecutionState.PAUSED;
        this.pausedMessage = msg;
    }

    /**
     * Sets the processing state to {@link ExecutionState#FAILED}.
     * 
     * @param e
     *            exception that describes the reason for the failure
     */
    void setFailed( OWSException e ) {
        this.processState = ExecutionState.FAILED;
        this.failedException = e;
    }

    /**
     * Creates an {@link ExecuteResponse} that reflects the current execution state.
     * 
     * @return an {@link ExecuteResponse} that reflects the current execution state
     */
    public ExecuteResponse createExecuteResponse() {
        return new ExecuteResponse( responseStorage, serviceInstance, this, outputParams, outputs, request );
    }

    // -----------------------------------------------------------------------
    // methods from ProcessletExecutionInfo
    // -----------------------------------------------------------------------

    @Override
    public void setStartedMessage( String msg ) {
        this.startedMessage = msg;
    }

    @Override
    public void setSucceededMessage( String msg ) {
        this.succeededMessage = msg;
    }

    @Override
    public void setPercentCompleted( int percentCompleted ) {
        this.percentCompleted = percentCompleted;
    }
}
