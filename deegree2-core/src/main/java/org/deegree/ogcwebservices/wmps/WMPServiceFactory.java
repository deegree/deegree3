// $HeadURL:
// /cvsroot/deegree/src/org/deegree/ogcwebservices/wcs/WCSServiceFactory.java,v
// 1.4 2004/06/18 15:50:30 tf Exp $
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
package org.deegree.ogcwebservices.wmps;

import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.ogcwebservices.wcs.configuration.InvalidConfigurationException;
import org.deegree.ogcwebservices.wmps.configuration.WMPSConfiguration;
import org.deegree.ogcwebservices.wmps.configuration.WMPSConfigurationDocument;

/**
 * Service Factory class handles the WMPService Instance.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @version 2.0
 */
public final class WMPServiceFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( WMPServiceFactory.class );

    private static WMPSConfiguration CONFIG;

    /**
     * Returns a WMPService instance
     *
     * @return WMPService
     */
    public static WMPService getService() {
        return WMPServiceFactory.getInstance( CONFIG );
    }

    /**
     * Returns a WMPService instance
     *
     * @param config
     * @return WMPService
     */
    public static WMPService getInstance( WMPSConfiguration config ) {
        return new WMPService( config );
    }

    /**
     * Returns true/false depending on the initialized state of the instance.
     *
     * @return boolean
     */
    public static boolean isInitialized() {
        return CONFIG != null;
    }

    /**
     * Sets the WMPSConfiguration
     *
     * @param wmpsConfiguration
     */
    public static void setConfiguration( WMPSConfiguration wmpsConfiguration ) {
        CONFIG = wmpsConfiguration;
        /**
         * if service instance are already created destroy all instances create new service
         * instances and put in pool
         */
        LOG.logInfo( CONFIG.getServiceIdentification().getTitle() + " (" + CONFIG.getVersion()
                     + ") service pool initialized." );
    }

    /**
     * Sets the service configuration
     *
     * @param serviceConfigurationUrl
     * @throws InvalidConfigurationException
     */
    public static void setConfiguration( URL serviceConfigurationUrl )
                            throws InvalidConfigurationException {

        try {
            WMPSConfigurationDocument doc = new WMPSConfigurationDocument();
            doc.load( serviceConfigurationUrl );
            WMPSConfiguration conf = doc.parseConfiguration();
            WMPServiceFactory.setConfiguration( conf );
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new InvalidConfigurationException( "WPSServiceFactory", e.getMessage() );
        }
    }
}
