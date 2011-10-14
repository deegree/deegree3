//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.ows.client;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.ows.capabilities.OWSCapabilitiesAdapter;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides common base functionality for API-level client implementations that access OGC web services.
 * 
 * @param T
 *            OWSCapabilitiesAdapter type for the specific service
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractOWSClient<T extends OWSCapabilitiesAdapter> {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractOWSClient.class );

    private final HttpClient httpClient;

    protected final T capaDoc;

    private ServiceIdentification identification;

    private ServiceProvider provider;

    private OperationsMetadata metadata;

    // service endpoint derived from capabilities URL (may be null)
    private URL capaBaseUrl;

    /**
     * Creates a new {@link AbstractOWSClient} instance.
     * 
     * @param capaUrl
     *            url of a WFS capabilities document, usually this is a <code>GetCapabilities</code> request to the
     *            service, must not be <code>null</code>
     * @throws OWSExceptionReport
     *             if the server replied with a service exception report
     * @throws XMLStreamException
     * @throws IOException
     *             if a communication/network problem occured
     */
    protected AbstractOWSClient( URL capaUrl ) throws OWSExceptionReport, XMLStreamException, IOException {

        if ( capaUrl == null ) {
            throw new NullPointerException( "Capabilities URL must not be null." );
        }

        httpClient = initHttpClient();
        OWSResponse response = doGet( capaUrl, null, null );
        try {
            XMLAdapter xmlAdapter = new XMLAdapter( response.getAsXMLStream() );

            OMElement rootEl = xmlAdapter.getRootElement();
            String version = rootEl.getAttributeValue( new QName( "version" ) );
            capaDoc = getCapabilitiesAdapter( xmlAdapter.getRootElement(), version );

            try {
                identification = capaDoc.parseServiceIdentification();
            } catch ( Throwable t ) {
                LOG.warn( "Error parsing service identification section: " + t.getMessage() );
            }
            try {
                provider = capaDoc.parseServiceProvider();
            } catch ( Throwable t ) {
                LOG.warn( "Error parsing service provider section: " + t.getMessage() );
            }
            try {
                metadata = capaDoc.parseOperationsMetadata();
            } catch ( Throwable t ) {
                LOG.warn( "Error parsing metadata section: " + t.getMessage() );
            }
        } finally {
            response.close();
        }

        String baseUrl = capaUrl.toString();
        int pos = baseUrl.indexOf( '?' );
        if ( pos != -1 ) {
            baseUrl = baseUrl.substring( 0, pos );
            try {
                capaBaseUrl = new URL( baseUrl );
            } catch ( Throwable t ) {
                LOG.warn( t.getMessage() );
            }
        }
    }

    private HttpClient initHttpClient() {
        ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager();
        return new DefaultHttpClient( connManager );
    }

    /**
     * Returns an {@link OWSCapabilitiesAdapter} instance suitable for the specific service and version.
     * 
     * @param rootEl
     *            root element of the service capabilities, never <code>null</code>
     * @param version
     *            capabilities version, may be <code>null</code> (for broken capabilities responses)
     * @return capabilities adapter, must not be <code>null</code>
     * @throws IOException
     */
    protected abstract T getCapabilitiesAdapter( OMElement rootEl, String version )
                            throws IOException;

    /**
     * Returns the {@link ServiceIdentification} metadata provided by the server.
     * 
     * @return identification metadata, can be <code>null</code>
     */
    public final ServiceIdentification getIdentification() {
        return identification;
    }

    /**
     * Returns the {@link ServiceProvider} metadata provided by the server.
     * 
     * @return provider metadata, can be <code>null</code>
     */
    public final ServiceProvider getProvider() {
        return provider;
    }

    /**
     * Returns the {@link OperationsMetadata} provided by the server.
     * 
     * @return operations metadata, can be <code>null</code>
     */
    public final OperationsMetadata getOperations() {
        return metadata;
    }

    protected boolean isOperationSupported( String operationName ) {
        if ( metadata == null ) {
            return false;
        }
        return metadata.getOperation( operationName ) != null;
    }

    protected URL getGetUrl( String operationName ) {

        List<URL> getUrls = getGetUrls( operationName );
        if ( !getUrls.isEmpty() ) {
            if ( getUrls.size() > 1 ) {
                LOG.debug( "Server announces multiple HTTP-GET URLs for operation '{}'. Using first one.",
                           operationName );
            }
            return getUrls.get( 0 );
        }
        LOG.warn( "Server doesn't announce HTTP-GET URL for operation '{}'. Deriving from capabilities base URL.",
                  operationName );

        if ( capaBaseUrl == null ) {
            throw new RuntimeException( "No endpoint available." );
        }
        return capaBaseUrl;
    }

    protected URL getPostUrl( String operationName ) {

        List<URL> postUrls = getPostUrls( operationName );
        if ( !postUrls.isEmpty() ) {
            if ( postUrls.size() > 1 ) {
                LOG.debug( "Server announces multiple HTTP-POST URLs for operation '{}'. Using first one.",
                           operationName );
            }
            return postUrls.get( 0 );
        }
        LOG.warn( "Server doesn't announce HTTP-POST URL for operation '{}'. Deriving from capabilities base URL.",
                  operationName );

        if ( capaBaseUrl == null ) {
            throw new RuntimeException( "No endpoint available." );
        }
        return capaBaseUrl;
    }

    protected List<URL> getGetUrls( String operationName ) {
        if ( metadata == null ) {
            return Collections.emptyList();
        }
        return metadata.getGetUrls( operationName );
    }

    protected List<URL> getPostUrls( String operationName ) {
        if ( metadata == null ) {
            return Collections.emptyList();
        }
        return metadata.getPostUrls( operationName );
    }

    /**
     * Performs an HTTP-GET request to the service.
     * <p>
     * NOTE: The caller <b>must</b> call {@link OWSResponse#close()} on the returned object eventually, otherwise
     * underlying resources (connections) may not be freed.
     * </p>
     * 
     * @param endPoint
     * @param params
     * @param headers
     * @return
     * @throws IOException
     */
    protected OWSResponse doGet( URL endPoint, Map<String, String> params, Map<String, String> headers )
                            throws IOException {

        OWSResponse response = null;
        URI uri = null;
        try {
            StringBuilder sb = new StringBuilder( endPoint.toString() );

            boolean first = true;
            if ( params != null ) {
                if ( sb.charAt( sb.length() - 1 ) != '?' ) {
                    sb.append( '?' );
                }
                for ( Entry<String, String> param : params.entrySet() ) {
                    if ( !first ) {
                        sb.append( '&' );
                    } else {
                        first = false;
                    }
                    sb.append( URLEncoder.encode( param.getKey(), "UTF-8" ) );
                    sb.append( '=' );
                    sb.append( URLEncoder.encode( param.getValue(), "UTF-8" ) );
                }
            }

            uri = new URI( sb.toString() );
            HttpGet httpGet = new HttpGet( uri );
            LOG.debug( "Performing GET request: " + uri );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            response = new OWSResponse( uri, httpResponse );
        } catch ( Throwable e ) {
            e.printStackTrace();
            String msg = "Error performing GET request on '" + uri + "': " + e.getMessage();
            throw new IOException( msg );
        }
        return response;
    }

    /**
     * Performs an HTTP-POST request to the service.
     * <p>
     * NOTE: The caller <b>must</b> call {@link OWSResponse#close()} on the returned object eventually, otherwise
     * underlying resources (connections) may not be freed.
     * </p>
     * 
     * @param endPoint
     * @param contentType
     * @param body
     * @param headers
     * @return
     * @throws IOException
     */
    protected OWSResponse doPost( URL endPoint, String contentType, StreamBufferStore body, Map<String, String> headers )
                            throws IOException {

        OWSResponse response = null;
        try {
            HttpPost httpPost = new HttpPost( endPoint.toURI() );
            LOG.debug( "Performing POST request on " + endPoint );
            LOG.debug( "post size: " + body.size() );
            InputStreamEntity entity = new InputStreamEntity( body.getInputStream(), (long) body.size() );
            entity.setContentType( contentType );
            httpPost.setEntity( entity );
            HttpResponse httpResponse = httpClient.execute( httpPost );
            response = new OWSResponse( endPoint.toURI(), httpResponse );
        } catch ( Throwable e ) {
            String msg = "Error performing POST request on '" + endPoint + "': " + e.getMessage();
            throw new IOException( msg );
        }
        return response;
    }
}
