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

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deegree.datatypes.QualifiedName;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.RemoteWFService;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wpvs.capabilities.OWSCapabilities;
import org.xml.sax.SAXException;

/**
 * This class represents a remote WFS dataSource object.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */
public class RemoteWFSDataSource extends LocalWFSDataSource {

    // private static final ILogger LOG = LoggerFactory.getLogger( RemoteWFSDataSource.class );
    private static Map<URL, WFSCapabilities> cache = new ConcurrentHashMap<URL, WFSCapabilities>();

    /**
     * Creates a new <code>RemoteWFSDataSource</code> object from the given parameters.
     *
     * @param name
     * @param owsCapabilities
     * @param validArea
     * @param minScaleDenominator
     * @param maxScaleDenominator
     * @param geometryProperty
     * @param filterCondition
     * @param maxFeatures
     */
    public RemoteWFSDataSource( QualifiedName name, OWSCapabilities owsCapabilities,
                                Surface validArea, double minScaleDenominator,
                                double maxScaleDenominator, PropertyPath geometryProperty,
                                Filter filterCondition, int maxFeatures) {

        super( name, owsCapabilities, validArea, minScaleDenominator, maxScaleDenominator,
               geometryProperty, filterCondition,  maxFeatures );
        setServiceType( REMOTE_WFS );

    }

    @Override
    public OGCWebService getOGCWebService()
                            throws OGCWebServiceException {
        WFSCapabilities wfsCapabilities = null;
        synchronized ( this ) {
            URL url = getOwsCapabilities().getOnlineResource();
            wfsCapabilities = cache.get( url );
            if ( !cache.containsKey( url ) || wfsCapabilities == null ) {
                WFSCapabilitiesDocument wfsCapsDoc = new WFSCapabilitiesDocument();
                try {
                    wfsCapsDoc.load( url );
                    wfsCapabilities = (WFSCapabilities) wfsCapsDoc.parseCapabilities();
                    cache.put( url, wfsCapabilities );
                } catch ( IOException e ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                } catch ( SAXException e ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                }
            }

        }
        return new RemoteWFService( wfsCapabilities );
    }

}
