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
package org.deegree.protocol.wps.client;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles the communication with a Web Processing Service (WPS).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClient {

    // TODO build idToProcess map (using DescribeProcess-requests)

    // keys: process ids, values: processes
    private final Map<String, WPSProcess> idToProcess = new LinkedHashMap<String, WPSProcess>();

    /**
     * Creates a new {@link WPSClient} instance that connects to the specified service.
     * 
     * @param serviceUrl
     *            url of the WPS service to connect
     */
    public WPSClient( String serviceUrl ) {
        // TODO read GetCapabilities response from server and extract metadata (Title, Abstract, etc.)
        // TODO retrieve list of Processes (Identifier, Title, Abstract)
    }

    /**
     * Returns the title of the service.
     * 
     * @return title of the service
     */
    public String getTitle() {
        // TODO
        return null;
    }

    /**
     * Returns the abstract of the service.
     * 
     * @return abstract of the service
     */    
    public String getAbstract() {
        // TODO
        return null;
    }

    /**
     * Returns all processes offered by the service.
     * 
     * @return processes offered by the service, never <code>null</code>
     */
    public Collection<WPSProcess> getProcesses() {
        return idToProcess.values();
    }

    /**
     * Returns the process with the specified id.
     * 
     * @param processId
     *           id of the process
     * @return process with the specified id or <code>null</code> if no such process is offered by the service
     */
    public WPSProcess getProcesses( String processId ) {
        return idToProcess.get( processId );
    }
}
