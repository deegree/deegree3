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
package org.deegree.ogcwebservices.wps;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.TriggerProvider;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wps.capabilities.WPSGetCapabilities;
import org.deegree.ogcwebservices.wps.configuration.WPSConfiguration;
import org.deegree.ogcwebservices.wps.describeprocess.DescribeProcessRequest;
import org.deegree.ogcwebservices.wps.describeprocess.DescribeProcessRequestHandler;
import org.deegree.ogcwebservices.wps.execute.ExecuteRequest;
import org.deegree.ogcwebservices.wps.execute.ExecuteRequestHandler;

/**
 * WPService.java
 *
 * Created on 08.03.2006. 17:34:15h
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @version 1.0.
 * @since 2.0
 */

public class WPService implements OGCWebService {

    private static final ILogger LOG = LoggerFactory.getLogger( WPService.class );

    private static final TriggerProvider TP = TriggerProvider.create( WPService.class );

    private WPSConfiguration configuration = null;

    /**
     *
     * @param configuration
     */
    public WPService( WPSConfiguration configuration ) {
        this.configuration = configuration;
    }

    /**
     * @return Returns the configuration.
     */
    public WPSConfiguration getConfiguration() {
        return configuration;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.OGCWebService#getCapabilities()
     */
    public OGCCapabilities getCapabilities() {
        return configuration;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.OGCWebService#doService(org.deegree.ogcwebservices.OGCWebServiceRequest)
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {

        request = (OGCWebServiceRequest) TP.doPreTrigger( this, request )[0];

        Object response = null;
        if ( request instanceof WPSGetCapabilities ) {
            LOG.logDebug( "recieved a get capabilities request" );
            response = configuration;
        } else if ( request instanceof DescribeProcessRequest ) {
            LOG.logDebug( "recieved a describe process request" );
            DescribeProcessRequestHandler describeProcessRequestHandler = new DescribeProcessRequestHandler( this );
            response = describeProcessRequestHandler.handleRequest( (DescribeProcessRequest) request );
        } else if ( request instanceof ExecuteRequest ) {
            LOG.logDebug( "recieved a execute request" );
            ExecuteRequestHandler executeRequestHandler = new ExecuteRequestHandler( this );
            response = executeRequestHandler.handleRequest( (ExecuteRequest) request );
        }

        return TP.doPostTrigger( this, response )[0];
    }

}
