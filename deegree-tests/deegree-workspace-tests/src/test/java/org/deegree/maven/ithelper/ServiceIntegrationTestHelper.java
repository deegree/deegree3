/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.maven.ithelper;

import org.apache.commons.io.IOUtils;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.protocol.wms.ops.GetMap;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServiceIntegrationTestHelper {

    private static final Logger LOG = getLogger( ServiceIntegrationTestHelper.class );

    private TestEnvironment environment;

    public ServiceIntegrationTestHelper( TestEnvironment environment ) {
        this.environment = environment;
    }

    public String createBaseURL() {
        String port = environment.getPort();
        String context = environment.getContext();
        return "http://localhost:" + port + "/" + context + "/";
    }

    public void testCapabilities( String service ) throws Exception {
        String address = createBaseURL() + "services/" + service.toLowerCase() + "?request=GetCapabilities&service="
                         + service;
        try {
            LOG.info( "Reading capabilities from " + address );
            String input = IOUtils.toString( new URL( address ).openStream(), "UTF-8" );
            XMLInputFactory fac = XMLInputFactory.newInstance();
            XMLStreamReader in = fac.createXMLStreamReader( new StringReader( input ) );
            while (!in.isStartElement()) {
                in.next();
            }
            if ( in.getLocalName().toLowerCase().contains( "exception" ) ) {
                LOG.error( "Actual response was:" );
                LOG.error( input );
                throw new Exception( "Retrieving capabilities from " + address + " failed." );
            }
        } catch ( Throwable e ) {
            LOG.debug( "Failed to retrieve capabilities.", e );
            throw new Exception( "Retrieving capabilities for " + service + " failed: "
                                            + e.getLocalizedMessage(), e );
        }
    }

    public void testLayers( String service )
                            throws Exception {
        if ( !service.equals( "WMS" ) ) {
            return;
        }
        String address = createBaseURL() + "services/wms?request=GetCapabilities&version=1.1.1&service=" + service;
        String currentLayer = null;
        try {
            WMSClient client = new WMSClient( new URL( address ), 360, 360 );
            for ( String layer : client.getNamedLayers() ) {
                LOG.info( "Retrieving map for layer " + layer );
                currentLayer = layer;
                List<String> layers = singletonList( layer );
                String srs = client.getCoordinateSystems( layer ).getFirst();
                Envelope bbox = client.getBoundingBox( srs, layer );
                if ( bbox == null ) {
                    bbox = client.getLatLonBoundingBox( layer );
                    if ( bbox == null ) {
                        bbox = new GeometryFactory().createEnvelope( -180, -90, 180, 90, CRSUtils.EPSG_4326 );
                    }
                    bbox = new GeometryTransformer( CRSManager.lookup( srs ) ).transform( bbox );
                }
                GetMap gm = new GetMap( layers, 100, 100, bbox, CRSManager.lookup( srs ),
                                        client.getFormats( GetMap ).getFirst(), true );
                Object img = ImageIO.read( client.getMap( gm ) );
                if ( img == null ) {
                    throw new Exception( "Retrieving map for " + layer + " failed." );
                }
            }
        } catch ( MalformedURLException e ) {
            LOG.error(e.getLocalizedMessage(), e );
            throw new Exception( "Retrieving capabilities for " + service + " failed: "
                                            + e.getLocalizedMessage(), e );
        } catch ( IOException e ) {
            LOG.error(e.getLocalizedMessage(), e );
            throw new Exception( "Retrieving map for " + currentLayer + " failed: "
                                            + e.getLocalizedMessage(), e );
        } catch ( Throwable e ) {
            LOG.error(e.getLocalizedMessage(), e );
            throw new Exception( "Layer " + currentLayer + " had no bounding box.", e );
        }
    }

}
