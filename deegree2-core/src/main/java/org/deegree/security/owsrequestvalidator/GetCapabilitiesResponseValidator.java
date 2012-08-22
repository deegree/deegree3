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
package org.deegree.security.owsrequestvalidator;

import static org.deegree.framework.util.CharsetUtils.getSystemCharset;
import static org.deegree.security.drm.model.RightType.GETFEATURE;
import static org.deegree.security.drm.model.RightType.GETMAP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.metadata.iso19115.Linkage;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilitiesDocument;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wms.XMLFactory;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.LegendURL;
import org.deegree.ogcwebservices.wms.capabilities.Style;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.owscommon_new.DCP;
import org.deegree.owscommon_new.HTTP;
import org.deegree.owscommon_new.Operation;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.RightSet;
import org.deegree.security.drm.model.SecuredObject;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsproxy.Request;
import org.deegree.security.owsrequestvalidator.wms.GetMapRequestValidator;
import org.w3c.dom.Document;

/**
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetCapabilitiesResponseValidator extends ResponseValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( GetCapabilitiesResponseValidator.class );

    private static final String INVALIDSERVICE = Messages.getString( "GetCapabilitiesResponseValidator.INVALIDSERVICE" );

    private String proxyURL = null;

    private String proxiedURL;

    /**
     * @param policy
     * @param proxyURL
     */
    public GetCapabilitiesResponseValidator( Policy policy, String proxyURL ) {
        super( policy );
        this.proxyURL = proxyURL;
        this.proxiedURL = this.getPolicy().getSecurityConfig().getProxiedUrl();
    }

    /**
     * validates the passed object as a response to a OWS request. The validity of the response may is assigned to
     * specific user rights. If the passed user is <>null this will be evaluated. <br>
     * the reponse may contain three valid kinds of objects:
     * <ul>
     * <li>a serialized image
     * <li>a xml encoded exception
     * <li>a svg-encoded vector image
     * </ul>
     * Each of these types can be identified by the mime-type of the response that is also passed to the method. <br>
     * If something basic went wrong it is possible that not further specified kind of object is passed as response. In
     * this case the method will throw an <tt>InvalidParameterValueException</tt> to avoid sending bad responses to the
     * client.
     * 
     * @param service
     *            service which produced the response (WMS, WFS ...)
     * @param response
     * @param mime
     *            mime-type of the response
     * @param user
     * @return the new response array
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     * @see GetMapRequestValidator#validateRequest(OGCWebServiceRequest, User)
     */
    @Override
    public byte[] validateResponse( String service, byte[] response, String mime, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        Request req = policy.getRequest( service, "GetCapabilities" );
        if ( req == null ) {
            throw new InvalidParameterValueException( INVALIDSERVICE + service );
        }
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPostConditions().isAny() ) {
            return response;
        }

        if ( MimeTypeMapper.isKnownOGCType( mime ) ) {
            // if the mime-type is a known OGC mime-type it must be an XML
            // document. probably it is a capabilities document but it also
            // could be an
            response = validateXML( service, response, user );
        } else if ( mime.equals( "text/xml" ) ) {
            // if the mime-type isn't an image type but 'text/xml'
            // it could be an exception
            response = validateXML( service, response, user );
        } else {
            throw new InvalidParameterValueException( UNKNOWNMIMETYPE + mime );
        }

        return response;
    }

    /**
     * splits document string into 'core' capabilities document and xml header
     * 
     * @param xml
     * @return the splitted document
     * @throws InvalidParameterValueException
     */
    private String[] clearCapabilities( byte[] xml )
                            throws InvalidParameterValueException {
        InputStreamReader isr = new InputStreamReader( new ByteArrayInputStream( xml ) );
        StringBuffer sb = new StringBuffer( 50000 );
        int c = 0;
        try {
            while ( ( c = isr.read() ) > -1 ) {
                sb.append( (char) c );
            }
            isr.close();
        } catch ( IOException e ) {
            String s = Messages.format( "GetCapabilitiesResponseValidator.CAPAREAD", e.getMessage() );
            throw new InvalidParameterValueException( s );
        }
        // WMS <= 1.1.1
        int pos = sb.indexOf( "<WMT_MS_Capabilities" );
        // WMS 1.3
        if ( pos < 0 ) {
            pos = sb.indexOf( "WMS_Capabilities" );
        }
        // WFS 1.1.0
        if ( pos < 0 ) {
            pos = sb.indexOf( "WFS_Capabilities" );
        }
        // CSW 2.0.0
        if ( pos < 0 ) {
            pos = sb.indexOf( "Capabilities" );
        }
        // WCS 1.0.0
        if ( pos < 0 ) {
            pos = sb.indexOf( "WCS_Capabilities" );
        }

        // just if pos is > -1 it makes sense to find the starting
        // index of the root element
        if ( pos > -1 ) {
            pos = pos + 4;
            char ch = '$';
            // find starting index of the root element
            while ( ch != '<' && pos > 0 ) {
                pos--;
                ch = sb.charAt( pos );
            }
            // if the least char read does not equal '<' the parsed document
            // is not an XML document
            if ( ch != '<' ) {
                pos = -1;
            }
        }
        String[] o = new String[2];
        if ( pos > 0 ) {
            // XML header / doctype
            o[0] = sb.substring( 0, pos );
        } else {
            o[0] = "";
        }
        if ( pos > -1 ) {
            // xml document starting at the root element
            o[1] = sb.substring( pos );
        } else {
            // no XML document
            o[0] = "ERROR";
            o[1] = sb.toString();
        }

        return o;
    }

    /**
     * validates the passed xml to be valid against the policy
     * 
     * @param service
     *            service which produced the response (WMS, WFS ...)
     * @param xml
     * @param user
     * @throws InvalidParameterValueException
     */
    private byte[] validateXML( String service, byte[] xml, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        String[] st = clearCapabilities( xml );
        if ( st[0].equals( "ERROR" ) ) {
            LOG.logError( st[1] );
            String s = Messages.format( "GetCapabilitiesResponseValidator.NOCAPADOC", st[1] );
            throw new InvalidParameterValueException( s );
        }
        Document doc = null;
        try {
            XMLFragment frag = new XMLFragment();
            frag.load( new StringReader( st[1] ), XMLFragment.DEFAULT_URL );
            doc = frag.getRootElement().getOwnerDocument();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String s = Messages.getString( "GetCapabilitiesResponseValidator.ALLCAPAPARSE" );
            throw new InvalidParameterValueException( s );
        }
        String root = doc.getDocumentElement().getNodeName();
        if ( root.equalsIgnoreCase( "Exception" ) ) {
            // if the xml contains a exception the reponse is valid!
        } else if ( "WMS".equals( service ) ) {
            try {
                xml = validateWMSCapabilities( doc, user );
            } catch ( XMLParsingException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( "invalid WMS capabilities" );
            }
        } else if ( "WFS".equals( service ) ) {
            xml = validateWFSCapabilities( doc, user );
        } else if ( "WCS".equals( service ) ) {
            xml = validateWCSCapabilities( doc, user );
        } else if ( "CSW".equals( service ) ) {
            xml = validateCSWCapabilities( doc );
        }

        StringBuffer sb = new StringBuffer( xml.length + st[0].length() );
        sb.append( st[0] );
        String s = new String( xml );
        int p = s.indexOf( "?>" );
        if ( p > -1 ) {
            sb.append( s.substring( p + 2, s.length() ) );
        } else {
            sb.append( s );
        }
        s = sb.toString();
        if ( s.indexOf( "<?xml version" ) > 1 ) {
            s = StringTools.replace( s, "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>", "", false );
            s = StringTools.replace( s, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "", false );
        }

        // TODO
        // regular expression
        // s = sb.toString().replaceAll( "<?...?>", "" );

        return s.getBytes();
    }

    /**
     * 
     * @param doc
     * @param user
     * @return nothing, an exception is thrown
     */
    private byte[] validateWCSCapabilities( Document doc, User user ) {
        // TODO
        // implement support for WCS
        throw new UnsupportedOperationException();
    }

    /**
     * validates the passed xml to be valid against the policy
     * 
     * @param user
     * @throws InvalidParameterValueException
     * @throws XMLParsingException
     */
    private byte[] validateWMSCapabilities( Document doc, User user )
                            throws InvalidParameterValueException, UnauthorizedException, XMLParsingException {

        WMSCapabilitiesDocument cdoc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( doc.getDocumentElement() );
        WMSCapabilities capa = null;
        try {
            capa = (WMSCapabilities) cdoc.parseCapabilities();
        } catch ( InvalidCapabilitiesException e ) {
            LOG.logError( e.getMessage(), e );
            String s = Messages.format( "GetCapabilitiesResponseValidator.WMSCAPAPARSE", e.getMessage() );
            throw new InvalidParameterValueException( s );
        }
        capa = filterWMSLayers( capa, user );

        List<Operation> ops = capa.getOperationMetadata().getOperations();
        for ( Operation operation : ops ) {
            setNewOnlineResource( operation );
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream( 50000 );
        byte[] b;
        try {
            cdoc = XMLFactory.export( capa );
            Properties properties = new Properties();
            // setting this to system charset is no problem, as later on it will be converted to a different encoding
            // again anyway
            // not using byte arrays might solve the problems here...
            properties.setProperty( OutputKeys.ENCODING, getSystemCharset() );
            cdoc.write( bos, properties );
            b = bos.toByteArray();
            bos.close();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String s = Messages.format( "GetCapabilitiesResponseValidator.WMSCAPAEXPORT", e.getMessage() );
            throw new InvalidParameterValueException( s );
        }

        return b;

    }

    /**
     * 
     * @param op
     */
    private void setNewOnlineResource( Operation op ) {
        if ( op.getDCP() != null ) {
            List<DCP> dcps = op.getDCP();
            for ( DCP dcp : dcps ) {
                HTTP http = (HTTP) dcp;
                List<OnlineResource> links = http.getLinks();
                try {
                    int size = links.size();
                    links.clear();
                    OnlineResource proxy = new OnlineResource( new Linkage( new URL( proxyURL ) ) );
                    for ( int i = 0; i < size; ++i )
                        links.add( proxy );
                } catch ( MalformedURLException e1 ) {
                    LOG.logError( e1.getLocalizedMessage(), e1 );
                }
            }
        }
    }

    /**
     * Sets the proxy online resource in the old owscommon Operation class. To be removed soon!
     * 
     * @param op
     */
    private void setNewOnlineResourceInOldOperation( org.deegree.ogcwebservices.getcapabilities.Operation op ) {
        if ( op.getDCPs() != null ) {
            for ( int i = 0; i < op.getDCPs().length; i++ ) {
                org.deegree.ogcwebservices.getcapabilities.HTTP http = (org.deegree.ogcwebservices.getcapabilities.HTTP) op.getDCPs()[i].getProtocol();
                try {
                    if ( http.getGetOnlineResources().length > 0 ) {
                        URL urls[] = new URL[http.getGetOnlineResources().length];
                        for ( int k = 0; k < http.getGetOnlineResources().length; ++k )
                            urls[k] = new URL( proxyURL );
                        http.setGetOnlineResources( urls );
                    }
                    if ( http.getPostOnlineResources().length > 0 ) {
                        URL urls[] = new URL[http.getPostOnlineResources().length];
                        for ( int k = 0; k < http.getPostOnlineResources().length; ++k )
                            urls[k] = new URL( proxyURL );
                        http.setPostOnlineResources( urls );
                    }
                } catch ( MalformedURLException e1 ) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * validates the passed xml to be valid against the policy
     * 
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    private byte[] validateWFSCapabilities( Document doc, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        WFSCapabilities capa = null;
        try {
            WFSCapabilitiesDocument capaDoc = new WFSCapabilitiesDocument();
            capaDoc.setRootElement( doc.getDocumentElement() );
            capa = (WFSCapabilities) capaDoc.parseCapabilities();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String s = Messages.format( "GetCapabilitiesResponseValidator.INVALIDWFSCAPA", e.getMessage() );
            throw new InvalidParameterValueException( s );
        }

        capa = filterWFSFeatureType( capa, user );

        org.deegree.ogcwebservices.getcapabilities.Operation[] ops = capa.getOperationsMetadata().getOperations();
        for ( int i = 0; i < ops.length; i++ ) {
            setNewOnlineResourceInOldOperation( ops[i] );
        }

        WFSCapabilitiesDocument capaDoc = null;
        try {
            capaDoc = org.deegree.ogcwebservices.wfs.XMLFactory.export( capa );
        } catch ( Exception e ) {
            throw new InvalidParameterValueException( e );
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream( 20000 );
        PrintWriter pr = new PrintWriter( bos );
        capaDoc.write( pr );
        return bos.toByteArray();

    }

    /**
     * validates the passed xml to be valid against the policy
     * 
     * @param doc
     * @return the new response
     * @throws InvalidParameterValueException
     */
    private byte[] validateCSWCapabilities( Document doc )
                            throws InvalidParameterValueException {
        CatalogueCapabilities capa = null;
        try {
            CatalogueCapabilitiesDocument capaDoc = new CatalogueCapabilitiesDocument();
            capaDoc.setRootElement( doc.getDocumentElement() );
            capa = (CatalogueCapabilities) capaDoc.parseCapabilities();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException(
                                                      Messages.format( "GetCapabilitiesResponseValidator.INVALIDWFSCAPA",
                                                                       e.getMessage() ) );
        }

        org.deegree.ogcwebservices.getcapabilities.Operation[] ops = capa.getOperationsMetadata().getOperations();
        for ( int i = 0; i < ops.length; i++ ) {
            setNewOnlineResourceInOldOperation( ops[i] );
        }

        CatalogueCapabilitiesDocument capaDoc = null;
        try {
            capaDoc = org.deegree.ogcwebservices.csw.XMLFactory_2_0_0.export( capa, null );
        } catch ( Exception e ) {
            throw new InvalidParameterValueException( e );
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream( 20000 );
        PrintWriter pr = new PrintWriter( bos );
        capaDoc.write( pr );
        return bos.toByteArray();
    }

    /**
     * filters the wms capabilities to rturn just the valid layers
     * 
     * @param capa
     * @param user
     */
    private WMSCapabilities filterWMSLayers( WMSCapabilities capa, User user )
                            throws UnauthorizedException {

        Request req = policy.getRequest( "WMS", "GetCapabilities" );
        Condition con = req.getPostConditions();
        OperationParameter op = con.getOperationParameter( "layers" );
        if ( op.isAny() )
            return capa;

        Layer layer = capa.getLayer();
        if ( op.isUserCoupled() && user != null ) {
            try {
                SecurityAccessManager sam = SecurityAccessManager.getInstance();
                SecurityAccess access = sam.acquireAccess( user );
                // call recursive method to remove all 'named' layers not
                // included in the list from the capabilities
                layer = removeWMSLayer( layer, user, access );
                updateLegendURLs( layer );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                throw new UnauthorizedException( Messages.format( "GetCapabilitiesResponseValidator.INVALIDUSER", user ) );
            }
        } else {
            // get list of valid wms layers
            List<?> list = op.getValues();
            // call recursive method to remove all 'named' layers not
            // included in the list from the capabilities
            layer = removeWMSLayer( layer, list );
        }
        capa.setLayer( layer );
        return capa;

    }

    private void updateLegendURLs( Layer layer ) {
        if ( proxiedURL != null && layer.getStyles() != null ) {
            for ( Style style : layer.getStyles() ) {
                if ( style.getLegendURL() != null ) {
                    for ( LegendURL url : style.getLegendURL() ) {
                        URL link = url.getOnlineResource();
                        try {
                            String externalForm = link.toExternalForm();
                            if ( externalForm.startsWith( proxiedURL ) ) {
                                url.setOnlineResource( new URL( externalForm.replace( proxiedURL, proxyURL ) ) );
                            } else if ( externalForm.contains( "GetLegendGraphic" ) ) {
                                String[] parts = externalForm.split( "\\?" );
                                url.setOnlineResource( new URL( proxyURL + "?" + parts[1] ) );
                            }
                        } catch ( MalformedURLException e ) {
                            LOG.logDebug( "A modified legend URL could not be created." );
                        }
                    }
                }
            }
        }
        for ( Layer l : layer.getLayer() ) {
            updateLegendURLs( l );
        }
    }

    /**
     * recursive method that removes all 'named' layers (layers that has a name in addtion to a title) from the layer
     * tree thats root node (layer) is passed to the method and that not present in the passed <tt>List</tt>
     * 
     * @param layer
     * @param validLayers
     */
    private Layer removeWMSLayer( Layer layer, List<?> validLayers ) {
        Layer[] layers = layer.getLayer();
        for ( int i = 0; i < layers.length; i++ ) {
            if ( layers[i].getName() != null && !validLayers.contains( layers[i].getName() ) ) {
                layer.removeLayer( layers[i].getName() );
            } else {
                removeWMSLayer( layers[i], validLayers );
                if ( layers[i].getLayer().length == 0 && layers[i].getName() == null ) {
                    layer.removeLayerByTitle( layers[i].getTitle() );
                }
            }
        }
        return layer;
    }

    /**
     * recursive method that removes all 'named' layers (layers that has a name in addition to a title) from the layer
     * tree thats root node (layer) is passed to the method and the passed user doesn't have a GetMap right on.
     * 
     * @param layer
     *            layer to validate
     * @param user
     *            user whose rights are considered
     * @param access
     *            object to access DRM registry
     * 
     */
    private Layer removeWMSLayer( Layer layer, User user, SecurityAccess access )
                            throws GeneralSecurityException {
        Layer[] layers = layer.getLayer();
        for ( int i = 0; i < layers.length; i++ ) {
            if ( layers[i].getName() != null ) {
                SecuredObject secObj = null;
                try {
                    // must be in try-catch block because an exception will be thrown
                    // if no SecuredObject with the passed layer exists
                    if ( policy.getSecurityConfig().getProxiedUrl() == null ) {
                        secObj = access.getSecuredObjectByName( layers[i].getName(), "Layer" );
                    } else {
                        secObj = access.getSecuredObjectByName( "[" + policy.getSecurityConfig().getProxiedUrl() + "]:"
                                                                + layers[i].getName(), "Layer" );
                    }
                } catch ( Exception e ) {
                    LOG.logDebug( "Lookup failed? ", e.getLocalizedMessage() );
                }
                RightSet rights = user.getRights( access, secObj, GETMAP );
                if ( secObj == null || rights == null ) {
                    // remove the layer from the capabilities if it's not known
                    // by the DRM registry or if the user doesn't have a GetMap
                    // right on it
                    layer.removeLayer( layers[i].getName() );
                }
            } else {
                removeWMSLayer( layers[i], user, access );
                if ( layers[i].getLayer().length == 0 && layers[i].getName() == null ) {
                    layer.removeLayerByTitle( layers[i].getTitle() );
                }
            }
        }
        return layer;
    }

    /**
     * @param capa
     * @param user
     * @return the new capabilities
     * @throws UnauthorizedException
     */
    private WFSCapabilities filterWFSFeatureType( WFSCapabilities capa, User user )
                            throws UnauthorizedException {

        Request req = policy.getRequest( "WFS", "GetCapabilities" );
        Condition con = req.getPostConditions();
        OperationParameter op = con.getOperationParameter( "featureTypes" );
        if ( op.isAny() )
            return capa;

        if ( op.isUserCoupled() && user != null ) {
            try {
                SecurityAccessManager sam = SecurityAccessManager.getInstance();
                SecurityAccess access = sam.acquireAccess( user );
                FeatureTypeList ftl = capa.getFeatureTypeList();
                WFSFeatureType[] ft = ftl.getFeatureTypes();
                StringBuffer sb = new StringBuffer( 200 );
                for ( int i = 0; i < ft.length; i++ ) {
                    SecuredObject secObj = null;
                    try {
                        // must be in try-catch block because an exception will be thrown
                        // if no SecuredObject with the passed layer exists
                        sb.delete( 0, sb.length() );
                        sb.append( '{' ).append( ft[i].getName().getNamespace().toASCIIString() );
                        sb.append( "}:" ).append( ft[i].getName().getLocalName() );
                        if ( policy.getSecurityConfig().getProxiedUrl() == null ) {
                            secObj = access.getSecuredObjectByName( sb.toString(), "Featuretype" );
                        } else {
                            secObj = access.getSecuredObjectByName( "[" + policy.getSecurityConfig().getProxiedUrl()
                                                                    + "]:" + sb, "Featuretype" );
                        }
                    } catch ( Exception e ) {
                        LOG.logDebug( "Lookup failed? ", e.getLocalizedMessage() );
                    }
                    if ( secObj == null || user.getRights( access, secObj, GETFEATURE ) == null ) {
                        ftl.removeFeatureType( ft[i] );
                    }
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                throw new UnauthorizedException( Messages.format( "GetCapabilitiesResponseValidator.INVALIDUSER", user ) );
            }
        } else {
            // get list of valid wms layers
            List<String> list = op.getValues();
            FeatureTypeList ftl = capa.getFeatureTypeList();
            WFSFeatureType[] ft = ftl.getFeatureTypes();
            StringBuffer sb = new StringBuffer( 200 );
            for ( int i = 0; i < ft.length; i++ ) {
                sb.delete( 0, sb.length() );
                sb.append( '{' ).append( ft[i].getName().getNamespace().toASCIIString() );
                sb.append( "}:" ).append( ft[i].getName().getLocalName() );
                if ( !list.contains( sb.toString() ) ) {
                    ftl.removeFeatureType( ft[i] );
                }
            }
        }

        return capa;
    }
}
