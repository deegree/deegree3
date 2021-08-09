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
import org.apache.commons.io.filefilter.SuffixFileFilter;
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
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.post;
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

    public static double determineSimilarity( String name, InputStream in1, InputStream in2 )
                            throws IOException, Exception {
        try {
            byte[] buf1 = toByteArray( in1 );
            byte[] buf2 = toByteArray( in2 );
            long equal = 0;
            for ( int i = 0; i < buf1.length; ++i ) {
                if ( i < buf2.length && buf1[i] == buf2[i] ) {
                    ++equal;
                }
            }
            double sim = (double) equal / (double) buf1.length;
            if ( sim < 0.99 ) {
                throw new Exception( "Request test " + name + " resulted in a similarity of only " + sim
                                                + "!" );
            }
            return sim;
        } finally {
            closeQuietly( in1 );
            closeQuietly( in2 );
        }
    }

    public void testRequests()
                            throws Exception {
        String address = createBaseURL() + "services";

        File reqDir = new File( environment.getBasedir(), "src/test/requests" );
        LOG.info( "---- Searching main requests directory for requests." );
        testRequestDirectories( address, reqDir );
    }

    public void testRequestDirectories( String address, File dir )
                            throws Exception {
        testRequestDirectory( address, dir );
        File[] listed = dir.listFiles();
        if ( listed != null ) {
            for ( File f : listed ) {
                if ( f.isDirectory() && !f.getName().equalsIgnoreCase( ".svn" ) ) {
                    LOG.info( "---- Searching request class " + f.getName() + " for requests." );
                    testRequestDirectories( address, f );
                }
            }
        }
    }

    public void testRequestDirectory( String address, File dir )
                            throws Exception {
        File[] listed = dir.listFiles( (FileFilter) new SuffixFileFilter( "kvp" ) );
        if ( listed != null ) {
            for ( File f : listed ) {
                String name = f.getName();
                name = name.substring( 0, name.length() - 4 );
                LOG.info( "KVP request testing " + name );
                try {
                    String req = readFileToString( f ).trim();
                    InputStream in1 = new URL( address + ( req.startsWith( "?" ) ? "" : "?" ) + req ).openStream();
                    File response = new File( f.getParentFile(), name + ".response" );
                    InputStream in2 = new FileInputStream( response );
                    double sim = determineSimilarity( name, in1, in2 );
                    if ( sim != 1 ) {
                        LOG.info( "Request test " + name + " resulted in similarity of " + sim );
                    }
                } catch ( IOException e ) {
                    throw new Exception( "KVP request checking of " + name + " failed: "
                                                    + e.getLocalizedMessage(), e );
                }
            }
        }
        listed = dir.listFiles( (FileFilter) new SuffixFileFilter( "xml" ) );
        if ( listed != null ) {
            for ( File f : listed ) {
                String name = f.getName();
                name = name.substring( 0, name.length() - 4 );
                LOG.info( "XML request testing " + name );
                FileInputStream reqIn = null;
                try {
                    reqIn = new FileInputStream( f );
                    InputStream in1 = post( STREAM, address, reqIn, null );
                    File response = new File( f.getParentFile(), name + ".response" );
                    InputStream in2 = new FileInputStream( response );
                    double sim = determineSimilarity( name, in1, in2 );
                    LOG.info( "Request test " + name + " resulted in similarity of " + sim );
                } catch ( IOException e ) {
                    throw new Exception( "KVP request checking of " + name + " failed: "
                                                    + e.getLocalizedMessage(), e );
                } finally {
                    closeQuietly( reqIn );
                }
            }
        }
    }

}
