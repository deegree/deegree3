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

import java.util.List;

/**
 * Encapsulates the requested settings for a process execution response.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ResponseFormat {

    private boolean rawOutput;

    private boolean storeResponse;

    private boolean includeInputs;

    private boolean updateStatus;

    private List<OutputFormat> outputDefs;

    /**
     * Creates a new {@link ResponseFormat} instance.
     * 
     * @param rawOutput
     *            true, if a raw response is requested, false means response document
     * @param storeResponse
     *            true, if the server should store the output response (and provide a web-accessible URL for it), false
     *            otherwise
     * @param includeInputs
     *            true, if the input parameters should be repeated in the response (lineage), false otherwise
     * @param updateStatus
     *            true, if the server should provide updated response documents (asynchronous execution), false
     *            otherwise
     * @param outputDefs
     *            the requested outputs, can be empty (indicates to return all outputs), but must not be
     *            <code>null</code>
     */
    public ResponseFormat( boolean rawOutput, boolean storeResponse, boolean includeInputs, boolean updateStatus,
                           List<OutputFormat> outputDefs ) {
        this.rawOutput = rawOutput;
        this.storeResponse = storeResponse;
        this.includeInputs = includeInputs;
        this.updateStatus = updateStatus;
        this.outputDefs = outputDefs;
    }

    /**
     * Returns whether the output should be "raw" (just one output parameter) instead of a response document.
     * 
     * @return true, if a raw response is requested, false means response document
     */
    public boolean returnRawOutput() {
        return rawOutput;
    }

    /**
     * Returns whether the server should store the output response (and provide a web-accessible URL in the response
     * document).
     * 
     * @return true, if the server should store the output response, false otherwise
     */
    public boolean storeResponse() {
        return storeResponse;
    }

    /**
     * Returns whether the input parameters should be repeated in the response (lineage).
     * 
     * @return true, if the input parameters should be repeated, false otherwise
     */
    public boolean includeInputs() {
        return includeInputs;
    }

    /**
     * Returns whether the server should provide updated response documents (asynchronous execution).
     * 
     * @return true, if the server should provide updated response documents, false otherwise
     */
    public boolean updateStatus() {
        return updateStatus;
    }

    /**
     * Returns the requested outputs.
     * 
     * @return the requested outputs, can be empty (indicates to return all outputs), but never <code>null</code>
     */
    public List<OutputFormat> getOutputDefinitions() {
        return outputDefs;
    }
}
