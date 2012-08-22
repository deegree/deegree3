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

package org.deegree.ogcwebservices.wcts;

import java.io.IOException;
import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wcts.configuration.WCTSConfiguration;
import org.deegree.ogcwebservices.wcts.configuration.WCTSConfigurationDocument;
import org.xml.sax.SAXException;

/**
 * <code>WCTServiceFactory</code> a convenience class to create and receive a
 * {@link WCTSConfiguration} instance.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class WCTServiceFactory {
    private static ILogger LOG = LoggerFactory.getLogger( WCTServiceFactory.class );

    private static WCTSConfiguration CONFIG;

    private WCTServiceFactory() {
        // do nottin
    }

    /**
     * @param configURL
     *            to read the configuration from.
     */
    public static synchronized void setConfiguration( URL configURL ) {
        if ( configURL != null ) {
            try {
                WCTSConfigurationDocument doc = new WCTSConfigurationDocument( configURL );
                CONFIG = doc.parseConfiguration();
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( SAXException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( InvalidCapabilitiesException e ) {
                LOG.logError( e.getMessage(), e );
            }
        }
    }

    /**
     * @return the configuration
     * @throws OGCWebServiceException
     *             if no configuration has been created yet.
     */
    public static synchronized WCTSConfiguration getConfiguration()
                            throws OGCWebServiceException {
        if ( CONFIG == null ) {
            throw new OGCWebServiceException( "Configuration has not been initialized yet",
                                              ExceptionCode.INTERNAL_SERVER_ERROR );
        }
        return CONFIG;
    }

    /**
     *
     * @return a new {@link WCTService} instance.
     * @throws OGCWebServiceException
     *             if the configuration was not initialized yet.
     */
    public static synchronized WCTService createServiceInstance()
                            throws OGCWebServiceException {
        if ( CONFIG == null ) {
            throw new OGCWebServiceException( "Configuration has not been initialized yet",
                                              ExceptionCode.INTERNAL_SERVER_ERROR );
        }
        return new WCTService( CONFIG );
    }

}
