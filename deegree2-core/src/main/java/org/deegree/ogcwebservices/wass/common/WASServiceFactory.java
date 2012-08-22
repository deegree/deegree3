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

package org.deegree.ogcwebservices.wass.common;

import java.io.IOException;
import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wass.was.WAService;
import org.deegree.ogcwebservices.wass.was.configuration.WASConfiguration;
import org.deegree.ogcwebservices.wass.was.configuration.WASConfigurationDocument;
import org.deegree.ogcwebservices.wass.wss.WSService;
import org.deegree.ogcwebservices.wass.wss.configuration.WSSConfiguration;
import org.deegree.ogcwebservices.wass.wss.configuration.WSSConfigurationDocument;
import org.xml.sax.SAXException;

/**
 * A <code>WASServiceFactory</code> class currently just creates uncached service instances.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class WASServiceFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( WASServiceFactory.class );

    private static WSSConfiguration wssConfiguration = null;

    private static WASConfiguration wasConfiguration = null;

    /**
     * Dispatches the configuration url to the appropriate methods.
     * @param url
     * @throws OGCWebServiceException
     */
    public static void setConfiguration( URL url ) throws OGCWebServiceException{
        if( url != null ){
            String service = null;
            try {
                XMLFragment doc = new XMLFragment( );
                doc.load( url );
                service = doc.getRootElement().getLocalName();
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( Messages.getMessage("WASS_ERROR_CONFIGURATION_NOT_READ","WASS"));
            } catch ( SAXException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( Messages.getMessage("WASS_ERROR_CONFIGURATION_NOT_PARSED","WASS"));
            }

            if( service != null ){
                if( service.contains("WAS") ){
                    setWASConfiguration(url);
                } else if ( service.contains("WSS") ){
                    setWSSConfiguration(url);
                }
            }
        }
    }

    /**
     * @param url
     * @throws OGCWebServiceException
     */
    private static void setWASConfiguration( URL url )
                            throws OGCWebServiceException {
        try {
            wasConfiguration = new WASConfigurationDocument().parseConfiguration( url );
        } catch ( InvalidConfigurationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( Messages.getMessage("WASS_ERROR_CONFIGURATION_NOT_PARSED","WAS"));
        } catch ( InvalidCapabilitiesException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( Messages.getMessage("WASS_ERROR_CONFIGURATION_NOT_PARSED","WAS"));
        }
    }

    /**
     * @param url
     * @throws OGCWebServiceException
     */
    private static void setWSSConfiguration( URL url )
                            throws OGCWebServiceException {
        try {
            wssConfiguration = new WSSConfigurationDocument().parseConfiguration( url );
        } catch ( InvalidConfigurationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( Messages.getMessage("WASS_ERROR_CONFIGURATION_NOT_PARSED","WSS"));
        }
    }

    /**
     * @return a new WSS service instance
     * @throws OGCWebServiceException
     */
    public static WSService getUncachedWSService()
                            throws OGCWebServiceException {
        if ( wssConfiguration == null ) {
            LOG.logError( Messages.getMessage("WASS_ERROR_CONFIGURATION_NOT_SET", "WASServiceFactory#getUncachedWSService"));
            throw new OGCWebServiceException( WASServiceFactory.class.getName(),
                                              Messages.getMessage("WASS_ERROR_CONFIGURATION_NOT_SET","WSS"));
        }
        WSService service = new WSService( wssConfiguration );
        return service;
    }

    /**
     * @return a new WAS service instance
     * @throws OGCWebServiceException
     */
    public static WAService getUncachedWAService()
                            throws OGCWebServiceException {
        if ( wasConfiguration == null ) {
            LOG.logError( Messages.getMessage("WASS_ERROR_CONFIGURATION_NOT_SET", "WASServiceFactory#getUncachedWAService"));
            throw new OGCWebServiceException( WASServiceFactory.class.getName(),
                                              Messages.getMessage("WASS_ERROR_CONFIGURATION_NOT_SET","WAS"));
        }
        return new WAService( wasConfiguration );
    }

}
