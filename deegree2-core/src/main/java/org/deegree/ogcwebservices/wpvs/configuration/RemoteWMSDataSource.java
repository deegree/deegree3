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

package org.deegree.ogcwebservices.wpvs.configuration;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wms.RemoteWMService;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wpvs.capabilities.OWSCapabilities;
import org.xml.sax.SAXException;

/**
 * This class represents a remote WPVS dataSource object.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class RemoteWMSDataSource extends LocalWMSDataSource {

    private static final ILogger LOG = LoggerFactory.getLogger( RemoteWMSDataSource.class );

    private static Map<URL, WMSCapabilities> cache = new ConcurrentHashMap<URL, WMSCapabilities>();

    /**
     * Creates a new <code>RemoteWMSDataSource</code> object from the given parameters.
     *
     * @param name
     * @param owsCapabilities
     * @param validArea
     * @param minScaleDenominator
     * @param maxScaleDenominator
     * @param filterCondition
     * @param transparentColors
     */
    public RemoteWMSDataSource( QualifiedName name, OWSCapabilities owsCapabilities, Surface validArea,
                                double minScaleDenominator, double maxScaleDenominator, GetMap filterCondition,
                                Color[] transparentColors ) {

        super( name, owsCapabilities, validArea, minScaleDenominator, maxScaleDenominator, filterCondition,
               transparentColors );
        this.setServiceType( AbstractDataSource.REMOTE_WMS );

    }

    /**
     * The <code>filterCondition</code> is a map of key-value-pairs of an incomplete WMSRequest.
     *
     * @return Returns the filterCondition as GetMap
     */
    public GetMap getFilterConditionMap() {
        return (GetMap) getFilterCondition();
    }

    /**
     * @throws OGCWebServiceException
     * @see org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource#getOGCWebService()
     */
    @Override
    public OGCWebService getOGCWebService()
                            throws OGCWebServiceException {
        WMSCapabilities wmsCapabilities = null;
        synchronized ( this ) {

            URL url = getOwsCapabilities().getOnlineResource();
            wmsCapabilities = cache.get( url );
            if ( !cache.containsKey( url ) || wmsCapabilities == null ) {
                try {
                    WMSCapabilitiesDocument wmsCapsDoc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( url );
                    wmsCapabilities = (WMSCapabilities) wmsCapsDoc.parseCapabilities();
                    cache.put( url, wmsCapabilities );
                } catch ( IOException e ) {
                    LOG.logError( e.getMessage(), e );
                    String msg = Messages.getMessage( "WPVS_DATASOURCE_CAP_ERROR", toString() );
                    throw new OGCWebServiceException( msg + e.getMessage() );
                } catch ( SAXException e ) {
                    LOG.logError( e.getMessage(), e );
                    String msg = Messages.getMessage( "WPVS_DATASOURCE_CAP_ERROR", toString() );
                    throw new OGCWebServiceException( msg + e.getMessage() );
                } catch ( InvalidCapabilitiesException e ) {
                    LOG.logError( e.getMessage(), e );
                    String msg = Messages.getMessage( "WPVS_DATASOURCE_CAP_ERROR", toString() );
                    throw new OGCWebServiceException( msg + e.getMessage() );
                } catch ( XMLParsingException e ) {
                    LOG.logError( e.getMessage(), e );
                    String msg = Messages.getMessage( "WPVS_DATASOURCE_CAP_ERROR", toString() );
                    throw new OGCWebServiceException( msg + e.getMessage() );
                }
            }

        }

        return new RemoteWMService( wmsCapabilities );
    }
}
