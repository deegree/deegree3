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

import java.io.IOException;
import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wps.configuration.WPSConfiguration;
import org.deegree.ogcwebservices.wps.configuration.WPSConfigurationDocument;
import org.xml.sax.SAXException;

/**
 * WPServiceFactory.java
 *
 * Created on 08.03.2006. 17:47:52h
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public final class WPServiceFactory {

    private static WPSConfiguration CONFIG = null;

    private static final ILogger LOG = LoggerFactory.getLogger( WPServiceFactory.class );

    private WPServiceFactory() {
        //prevent instantiation
    }

    /**
     *
     * @param config
     * @return WPService
     */
    public static WPService getInstance( WPSConfiguration config ) {
        return new WPService( config );
    }

    /**
     *
     * @return WPService
     * @throws OGCWebServiceException
     */
    public static WPService getInstance()
                            throws OGCWebServiceException {
        if ( null == CONFIG ) {
            throw new OGCWebServiceException( WPServiceFactory.class.getName(),
                                              "Configuration has not been initialized" );
        }
        return new WPService( CONFIG );
    }

    /**
     *
     * @param wpsConfiguration
     */
    public static void setConfiguration( WPSConfiguration wpsConfiguration ) {
        CONFIG = wpsConfiguration;
    }

    /**
     *
     * @param serviceConfigurationUrl
     * @throws InvalidConfigurationException
     * @throws IOException
     */
    public static void setConfiguration( URL serviceConfigurationUrl )
                            throws InvalidConfigurationException, IOException {

        WPSConfigurationDocument wpsConfDoc = new WPSConfigurationDocument();
        try {
            wpsConfDoc.load( serviceConfigurationUrl );
        } catch ( SAXException e ) {
            LOG.logError( "SAXException: " + e.getMessage() );
            throw new InvalidConfigurationException( "WPServiceFactory", e.getMessage() );
        }
        WPServiceFactory.setConfiguration( wpsConfDoc.getConfiguration() );

    }

    /**
     *
     * @return WPService
     */
    public static WPService getService() {
        return WPServiceFactory.getInstance( CONFIG );
    }

}
