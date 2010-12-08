//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wps.client.process.execute;

import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wps.WPSConstants.ExecutionState;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecutionStatus {

    private ExecutionState state;

    private String statusMsg;

    private Integer percent;

    private String creationTime;

    private OWSException exceptionReport;

    /**
     * @param state
     * @param statusMsg
     * @param percent
     * @param creationTime
     * @param exceptionReport
     */
    public ExecutionStatus( ExecutionState state, String statusMsg, Integer percent, String creationTime,
                            OWSException exceptionReport ) {
        this.state = state;
        this.statusMsg = statusMsg;
        this.percent = percent;
        this.creationTime = creationTime;
        this.exceptionReport = exceptionReport;
    }

    /**
     * Returns the current state of the execution.
     * 
     * @return state of the execution, never <code>null</code>
     */
    public ExecutionState getState() {
        return state;
    }

    /**
     * Returns the status message.
     * 
     * @return status message, may be <code>null</code> (no status message available)
     */
    public String getStatusMessage() {
        return statusMsg;
    }

    /**
     * Returns the percentage of the process that has been completed.
     * 
     * @return the completed percentage of the process, may be <code>null</code> (no completion percentage available)
     */
    public Integer getPercentCompleted() {
        return percent;
    }

    /**
     * @return creation time of the process execution, can be <code>null</code>
     */
    public String getCreationTime() {
        return creationTime;
    }

    /**
     * Returns the exception report.
     * <p>
     * NOTE: An exception report is only available if state is {@link ExecutionState#FAILED}.
     * </p>
     * 
     * @return an exception message in case the execution failed, <code>null</code> otherwise
     */
    public OWSException getExceptionReport() {
        return exceptionReport;
    }
}
