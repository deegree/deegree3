//$HeadURL$
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

import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.post;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.client.WMSClient111;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ServiceIntegrationTestHelper {

    private Log log;

    private MavenProject project;

    public ServiceIntegrationTestHelper( MavenProject project, Log log ) {
        this.log = log;
        this.project = project;
    }

    public String getPort() {
        Object port = project.getProperties().get( "portnumber" );
        return port.toString();
    }

    public String createBaseURL() {
        Object port = project.getProperties().get( "portnumber" );
        return "http://localhost:" + port + "/" + project.getArtifactId() + "/";
    }

    public void testCapabilities( String service )
                            throws MojoFailureException {
        String address = createBaseURL() + "services/" + service.toLowerCase() + "?request=GetCapabilities&service="
                         + service;
        try {
            log.info( "Reading capabilities from " + address );
            String input = IOUtils.toString( new URL( address ).openStream(), "UTF-8" );
            XMLInputFactory fac = XMLInputFactory.newInstance();
            XMLStreamReader in = fac.createXMLStreamReader( new StringReader( input ) );
            in.next();
            if ( in.getLocalName().toLowerCase().contains( "exception" ) ) {
                log.error( "Actual response was:" );
                log.error( input );
                throw new MojoFailureException( "Retrieving capabilities from " + address + " failed." );
            }
        } catch ( Throwable e ) {
            log.debug( e );
            throw new MojoFailureException( "Retrieving capabilities for " + service + " failed: "
                                            + e.getLocalizedMessage() );
        }
    }

    public void testLayers( String service )
                            throws MojoFailureException {
        if ( !service.equals( "WMS" ) ) {
            return;
        }
        String address = createBaseURL() + "services/wms?request=GetCapabilities&version=1.1.1&service=" + service;
        String currentLayer = null;
        try {
            WMSClient111 client = new WMSClient111( new URL( address ), 360, 360 );
            for ( String layer : client.getNamedLayers() ) {
                log.info( "Retrieving map for layer " + layer );
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
                Pair<BufferedImage, String> map = client.getMap( layers, 100, 100, bbox, CRSManager.lookup( srs ),
                                                                 client.getFormats( GetMap ).getFirst(), true, false,
                                                                 -1, false, null );
                if ( map.first == null ) {
                    throw new MojoFailureException( "Retrieving map for " + layer + " failed: " + map.second );
                }
            }
        } catch ( MalformedURLException e ) {
            log.debug( e );
            throw new MojoFailureException( "Retrieving capabilities for " + service + " failed: "
                                            + e.getLocalizedMessage() );
        } catch ( IOException e ) {
            log.debug( e );
            throw new MojoFailureException( "Retrieving map for " + currentLayer + " failed: "
                                            + e.getLocalizedMessage() );
        } catch ( Throwable e ) {
            log.debug( e );
            throw new MojoFailureException( "Layer " + currentLayer + " had no bounding box." );
        }
    }

    public static double determineSimilarity( String name, InputStream in1, InputStream in2 )
                            throws IOException, MojoFailureException {
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
                throw new MojoFailureException( "Request test " + name + " resulted in a similarity of only " + sim
                                                + "!" );
            }
            return sim;
        } finally {
            closeQuietly( in1 );
            closeQuietly( in2 );
        }
    }

    public void testRequests()
                            throws MojoFailureException {
        String address = createBaseURL() + "services";

        File reqDir = new File( project.getBasedir(), "src/test/requests" );
        log.info( "---- Searching main requests directory for requests." );
        testRequestDirectories( address, reqDir );
    }

    public void testRequestDirectories( String address, File dir )
                            throws MojoFailureException {
        testRequestDirectory( address, dir );
        File[] listed = dir.listFiles();
        if ( listed != null ) {
            for ( File f : listed ) {
                if ( f.isDirectory() && !f.getName().equalsIgnoreCase( ".svn" ) ) {
                    log.info( "---- Searching request class " + f.getName() + " for requests." );
                    testRequestDirectories( address, f );
                }
            }
        }
    }

    public void testRequestDirectory( String address, File dir )
                            throws MojoFailureException {
        File[] listed = dir.listFiles( (FileFilter) new SuffixFileFilter( "kvp" ) );
        if ( listed != null ) {
            for ( File f : listed ) {
                String name = f.getName();
                name = name.substring( 0, name.length() - 4 );
                log.info( "KVP request testing " + name );
                try {
                    String req = readFileToString( f ).trim();
                    InputStream in1 = new URL( address + ( req.startsWith( "?" ) ? "" : "?" ) + req ).openStream();
                    File response = new File( f.getParentFile(), name + ".response" );
                    InputStream in2 = new FileInputStream( response );
                    double sim = determineSimilarity( name, in1, in2 );
                    if ( sim != 1 ) {
                        log.info( "Request test " + name + " resulted in similarity of " + sim );
                    }
                } catch ( IOException e ) {
                    throw new MojoFailureException( "KVP request checking of " + name + " failed: "
                                                    + e.getLocalizedMessage() );
                }
            }
        }
        listed = dir.listFiles( (FileFilter) new SuffixFileFilter( "xml" ) );
        if ( listed != null ) {
            for ( File f : listed ) {
                String name = f.getName();
                name = name.substring( 0, name.length() - 4 );
                log.info( "XML request testing " + name );
                FileInputStream reqIn = null;
                try {
                    reqIn = new FileInputStream( f );
                    InputStream in1 = post( STREAM, address, reqIn, null );
                    File response = new File( f.getParentFile(), name + ".response" );
                    InputStream in2 = new FileInputStream( response );
                    double sim = determineSimilarity( name, in1, in2 );
                    log.info( "Request test " + name + " resulted in similarity of " + sim );
                } catch ( IOException e ) {
                    throw new MojoFailureException( "KVP request checking of " + name + " failed: "
                                                    + e.getLocalizedMessage() );
                } finally {
                    closeQuietly( reqIn );
                }
            }
        }
    }

}
