//$HeadURL$
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
package org.deegree.ogcwebservices.wps.describeprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Code;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wps.WPService;
import org.deegree.ogcwebservices.wps.configuration.WPSConfiguration;
import org.deegree.ogcwebservices.wps.execute.Process;

/**
 * DescribeProcessRequestHandler.java
 *
 * Created on 10.03.2006. 12:06:58h
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 *
 * @version 1.0.
 *
 * @since 2.0
 */

public class DescribeProcessRequestHandler {

    private static ILogger LOG = LoggerFactory.getLogger( DescribeProcessRequestHandler.class );

    private WPService wpService = null;

    /**
     *
     * @param wpService
     */
    public DescribeProcessRequestHandler( WPService wpService ) {
        this.wpService = wpService;
    }

    /**
     *
     * @param describeProcessRequest
     * @return the describtion of the process.
     * @throws OGCWebServiceException
     */
    public ProcessDescriptions handleRequest( DescribeProcessRequest describeProcessRequest )
                            throws OGCWebServiceException {

        // Get configuration from current wps instance
        WPSConfiguration wpsConfiguration = wpService.getConfiguration();

        // Get the map of registered Processes from wps configuration
        Map<String, Process> processesMap = wpsConfiguration.getRegisteredProcesses();

        // Get list of requested process descriptions from
        // wpsDescribeProcessRequest
        List<Code> requestedProcessesList = describeProcessRequest.getIdentifier();

        // Prepare list of process description to return
        List<ProcessDescription> processDescriptionList = new ArrayList<ProcessDescription>();

        // If the requested process is registered in current wps instance add it to processDescriptions, otherwise throw
        // exception
        int requestedProcessesSize = requestedProcessesList.size();
        for ( int i = 0; i < requestedProcessesSize; i++ ) {

            Code requestedProcess = requestedProcessesList.get( i );

            String identifier = requestedProcess.getCode().toUpperCase();

            if ( processesMap.containsKey( identifier ) ) {

                Process process = processesMap.get( identifier );

                ProcessDescription processDescription = process.getProcessDescription();
                processDescriptionList.add( i, processDescription );

            } else {
                String msg = "Process '" + identifier + "' is not supported by this WPS.";
                LOG.logError( msg );
                throw new InvalidParameterValueException( "Identifier", msg );
            }
        }
        return new ProcessDescriptions( processDescriptionList );
    }

}
