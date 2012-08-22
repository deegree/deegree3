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

package org.deegree.ogcwebservices.wass.wss.operation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.deegree.enterprise.WebUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilitiesDocument;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.wass.exceptions.DoServiceException;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.owscommon_new.DCP;
import org.xml.sax.SAXException;

/**
 * This base class will makes the connection to the requested service on the "hidden" machines.
 * Every Subclass must implement the handleRequest method to check the credentials. A call to
 * another service can only be made, if the requestAllowed values is true;
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public abstract class DoServiceHandler {

    private ILogger LOG = LoggerFactory.getLogger( DoServiceHandler.class );

    private boolean requestAllowed = false;

    /**
     * Each subclass must implement this method, appropriate to its needs. For example password
     * handling.
     *
     * @param request
     *            the request the client sent to the secured service
     * @throws DoServiceException
     *             if an error occured while processing the clients credentials.
     */
    public abstract void handleRequest( DoService request )
                            throws DoServiceException;

    /**
     * @return Returns the requestAllowed.
     */
    public boolean requestAllowed() {
        return requestAllowed;
    }

    /**
     * @param isAllowed
     *            The requestAllowed to set.
     */
    public void setRequestAllowed( boolean isAllowed ) {
        this.requestAllowed = isAllowed;
    }

    /**
     * This method does the actual request to the secured service. It returns the response of the
     * secured service as an inputstream. It also replace the GetCapabilities request - responses
     * with the facadeurl given by the client.
     *
     * @param request
     *            send by the client a DoService Request.
     * @param securedService
     *            the service for which this wss is proxying, must be put in the deegreeparams of
     *            the configuration file.
     * @param requestedCharset
     *            this wss uses, also read from the deegreeparams in the configuration file.
     * @param timeout
     *            how long to wait for a response. Service dependable therefor also read from the
     *            deegreeparams in the config file.
     * @param securedServiceName
     *            the name of the service for which we are proxying -> config.
     * @return the http response of the secured service as an inputstream.
     * @throws DoServiceException
     *             if an error occurs wile sending the request or treating the response. see
     *             org.deegree.ogcwebservices.csw.manager.CatalogueHarvester#getNextMetadataRecord
     */
    public DoServiceResponse sendRequest( DoService request, URL securedService, String requestedCharset, int timeout,
                                          String securedServiceName )
                            throws DoServiceException {
        if ( requestAllowed ) {

            Header[] headers = null;
            InputStream body = null;
            Header[] footers = null;
            String proxyRequest = null;
            try {
                proxyRequest = URLDecoder.decode( request.getPayload(), CharsetUtils.getSystemCharset() );
            } catch ( UnsupportedEncodingException e ) {
                LOG.logError( e.getMessage(), e );
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_INTERNAL", "WSS" ) );
            }
            LOG.logDebug( "encoded proxyrequest: " + request.getPayload() + "\ndecoded proxy: " + proxyRequest );
            String dcp = request.getDcp();
            HttpClient client = new HttpClient();
            client = WebUtils.enableProxyUsage( client, securedService );
            StringRequestEntity requestEntity = null;
            HttpClientParams params = client.getParams();
            params.setSoTimeout( timeout );
            HttpMethod requestMethod = null;
            try {
                String contentType = null;
                for ( RequestParameter param : request.getRequestParameters() ) {
                    if ( param.getId().toLowerCase().trim().contains( "mime-type" ) )
                        contentType = param.getParameter();
                }
                requestEntity = new StringRequestEntity( proxyRequest, contentType, requestedCharset );
            } catch ( UnsupportedEncodingException e1 ) {
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_ENCODING_NOT_SUPPORTED", "WSS" ) );
            }
            if ( dcp.equalsIgnoreCase( "http_post" ) ) {

                // the url to the service must be written in the deegreeparams in the configuration
                // xml
                requestMethod = new PostMethod( securedService.toExternalForm() );
                ( (PostMethod) requestMethod ).setRequestEntity( requestEntity );
            } else if ( dcp.equalsIgnoreCase( "http_get" ) ) {
                requestMethod = new GetMethod( securedService.toExternalForm() );
                requestMethod.setQueryString( proxyRequest );
            } else {
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_NOT_POST_OR_GET", "WSS" ) );
            }
            // getDataRequest
            try {
                // make header parameters of the requestParameters.
                for ( RequestParameter param : request.getRequestParameters() ) {
                    if ( !param.getId().toLowerCase().trim().contains( "mime-type" ) )// Contenttype
                        requestMethod.addRequestHeader( param.getId(), param.getParameter() );
                }
                // Call the secured service
                client.executeMethod( requestMethod );
                headers = requestMethod.getResponseHeaders();
                footers = requestMethod.getResponseFooters();
                body = requestMethod.getResponseBodyAsStream();

                if ( body == null )
                    throw new DoServiceException( Messages.getMessage( "WASS_ERROR_GOT_NO_RESPONSE", "WSS" ) );
            } catch ( HttpException e ) {
                LOG.logError( e.getMessage(), e );
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_EXCEPTION_IN_RESPONSE", "WSS" ) );
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_IN_TRANSPORT", "WSS" ) );
            }
            try {
                // Replace the given urls with the facadeurls if it is a GetCapabilities request
                if ( proxyRequest.trim().contains( "GetCapabilities" ) ) {
                    Operation[] operations = null;
                    OGCCapabilitiesDocument doc = null;
                    /*
                     * For now just check these service, others may be "secured" in the future.
                     */
                    if ( "WFS".equals( securedServiceName ) ) {
                        doc = new WFSCapabilitiesDocument();
                        doc.load( body, securedService.toExternalForm() );
                        WFSCapabilities cap = (WFSCapabilities) doc.parseCapabilities();
                        operations = cap.getOperationsMetadata().getOperations();
                        replaceFacadeURL( operations, request.getFacadeURL() );
                        doc = org.deegree.ogcwebservices.wfs.XMLFactory.export( cap );
                    } else if ( ( "WMS" ).equals( securedServiceName ) ) {
                        doc = new WMSCapabilitiesDocument();
                        doc.load( body, securedService.toExternalForm() );
                        doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( doc.getRootElement() );
                        WMSCapabilities cap = (WMSCapabilities) doc.parseCapabilities();
                        org.deegree.owscommon_new.Operation[] ops = cap.getOperationMetadata().getOperations().toArray(
                                                                                                                        new org.deegree.owscommon_new.Operation[0] );
                        replaceFacadeURL( ops, request.getFacadeURL() );
                        doc = org.deegree.ogcwebservices.wms.XMLFactory.export( cap );
                    } else if ( ( "WCS" ).equals( securedServiceName ) ) {
                        doc = new WCSCapabilitiesDocument();
                        doc.load( body, securedService.toExternalForm() );
                        WCSCapabilities cap = (WCSCapabilities) doc.parseCapabilities();
                        operations = cap.getCapabilitiy().getOperations().getOperations();
                        replaceFacadeURL( operations, request.getFacadeURL() );
                        doc = org.deegree.ogcwebservices.wcs.XMLFactory.export( cap );
                    } else if ( ( "CSW" ).equals( securedServiceName ) ) {
                        doc = new CatalogueCapabilitiesDocument();
                        doc.load( body, securedService.toExternalForm() );
                        CatalogueCapabilities cap = (CatalogueCapabilities) doc.parseCapabilities();
                        operations = cap.getOperationsMetadata().getOperations();
                        replaceFacadeURL( operations, request.getFacadeURL() );
                        doc = org.deegree.ogcwebservices.csw.XMLFactory_2_0_0.export( cap, null );
                    }

                    body = new ByteArrayInputStream( doc.getAsString().getBytes() );
                }
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_READING_BODY", "WSS" ) );
            } catch ( InvalidCapabilitiesException e ) {
                LOG.logError( e.getMessage(), e );
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_CAPABILITIES_RESPONSE", "WSS" ) );
            } catch ( SAXException e ) {
                LOG.logError( e.getMessage(), e );
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_FACADE_URL", "WSS" ) );
            } catch ( XMLParsingException e ) {
                LOG.logError( e.getMessage(), e );
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_READING_BODY", "WSS" ) );
            }
            return new DoServiceResponse( headers, body, footers );
        }

        return null;
    }

    private void replaceFacadeURL( Operation[] operations, URI facadeURI ) {

        if ( operations != null && facadeURI != null ) {
            for ( int i = 0; i < operations.length; i++ ) {
                setNewOnlineResource( operations[i], facadeURI );
            }
        }
    }

    private void replaceFacadeURL( org.deegree.owscommon_new.Operation[] operations, URI facadeURI ) {

        if ( operations != null && facadeURI != null ) {
            for ( int i = 0; i < operations.length; i++ ) {
                setNewOnlineResource( operations[i], facadeURI );
            }
        }
    }

    /**
     * Resets all the url in the response body with the facade urls
     *
     * @param op
     *            the operation which has the secured service url in it
     * @param facadeURI
     *            the url of this wss.
     */
    private void setNewOnlineResource( Operation op, URI facadeURI ) {

        if ( op.getDCPs() != null ) {
            for ( int i = 0; i < op.getDCPs().length; i++ ) {
                HTTP http = (HTTP) op.getDCPs()[i].getProtocol();
                try {
                    if ( http.getGetOnlineResources().length > 0 ) {
                        URL urls[] = new URL[http.getGetOnlineResources().length];
                        for ( int k = 0; k < http.getGetOnlineResources().length; ++k )
                            urls[k] = facadeURI.toURL();

                        http.setGetOnlineResources( urls );
                    }
                    if ( http.getPostOnlineResources().length > 0 ) {
                        URL urls[] = new URL[http.getPostOnlineResources().length];
                        for ( int k = 0; k < http.getPostOnlineResources().length; ++k ) {
                            urls[k] = facadeURI.toURL();
                        }

                        http.setPostOnlineResources( urls );
                    }
                } catch ( MalformedURLException e1 ) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private void setNewOnlineResource( org.deegree.owscommon_new.Operation op, URI facadeURI ) {

        if ( op.getDCP() != null ) {
            for ( DCP dcp : op.getDCP() ) {
                // assuming HTTP here, SOAP won't work!
                org.deegree.owscommon_new.HTTP http = (org.deegree.owscommon_new.HTTP) dcp;
                try {
                    if ( http.getGetOnlineResources().size() > 0 ) {
                        List<URL> urls = http.getGetOnlineResources();
                        List<URL> urlsnew = new ArrayList<URL>( urls.size() );
                        for ( int i = 0; i < urls.size(); ++i ) {
                            urlsnew.add( facadeURI.toURL() );
                        }

                        http.setGetOnlineResources( urlsnew );
                    }
                    if ( http.getPostOnlineResources().size() > 0 ) {
                        List<URL> urls = http.getPostOnlineResources();
                        List<URL> urlsnew = new ArrayList<URL>( urls.size() );
                        for ( int i = 0; i < urls.size(); ++i ) {
                            urlsnew.add( facadeURI.toURL() );
                        }

                        http.setPostOnlineResources( urls );
                    }
                } catch ( MalformedURLException e1 ) {
                    e1.printStackTrace();
                }
            }
        }

    }
}
