//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

import java.net.URL;

import org.deegree.protocol.wps.WPSConstants.ExecutionState;
import org.deegree.protocol.wps.client.output.ExecutionOutput;

/**
 * Encapsulates the results from a process execution.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecutionResponse {

    private final URL statusLocation;

    private final ExecutionStatus status;

    private final ExecutionOutputs outputs;

    /**
     * @param statusLocation
     *            may be <code>null</code>
     * @param status
     *            may be <code>null</code>
     * @param outputs
     *            never <code>null</code>
     */
    public ExecutionResponse( URL statusLocation, ExecutionStatus status, ExecutionOutput[] outputs ) {
        this.statusLocation = statusLocation;
        if ( status == null ) {
            this.status = new ExecutionStatus( ExecutionState.SUCCEEDED, null, null, null, null );
        } else {
            this.status = status;
        }
        this.outputs = new ExecutionOutputs( outputs );
    }

    /**
     * Returns the status location for fetching updated response documents.
     * 
     * @return the status location, may be <code>null</code> (in synchronous mode)
     */
    public URL getStatusLocation() {
        return statusLocation;
    }

    /**
     * Returns the current status of the process execution.
     * 
     * @return the current status of the process execution, never <code>null</code>
     */
    public ExecutionStatus getStatus() {
        return status;
    }

    /**
     * Returns the output parameters from the process execution.
     * <p>
     * NOTE: This method may only be called when the process is in state {@link ExecutionState#SUCCEEDED}.
     * </p>
     * 
     * @return the output parameters, never <code>null</code>
     */
    public ExecutionOutputs getOutputs() {
        return outputs;
    }
}
