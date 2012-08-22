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

package org.deegree.portal.standard.wfs.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.WebUtils;
import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.portal.Constants;
import org.deegree.portal.standard.wfs.WFSClientException;
import org.deegree.portal.standard.wfs.configuration.DigitizerClientConfiguration;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This Listener is used to perform a wfs transaction (e.g. write a geometry that was digitized in
 * the client to a WFS). The featureType must be passed as first parameter of the RPC request, the
 * second parameter must contain a struct, whose members are replaced in the transaction template.
 *
 * WFS address and transaction template are both taken from the module configuration.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DigitizeListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( DigitizeListener.class );

    protected static final String DIGITIZER_CLIENT_CONFIGURATION = "DIGITIZER_CLIENT_CONFIGURATION";

    protected static final String GEOMETRY = "GEOMETRY";

    protected static final String CRS = "CRS";

    protected DigitizerClientConfiguration config = null;

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        // GET MODULE CONFIGURATION
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        config = (DigitizerClientConfiguration) session.getAttribute( DIGITIZER_CLIENT_CONFIGURATION );

        // VALIDATE RPC REQUEST
        RPCWebEvent rpcEvent = (RPCWebEvent) event;
        try {
            validateRequest( rpcEvent );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_INVALID_RPC", e.getLocalizedMessage() ) );
            return;
        }

        // GET WFS ADDRESS FOR QUERY
        String address = null;
        QualifiedName qualiName = null;
        try {
            qualiName = extractFeatureTypeAsQualifiedName( rpcEvent );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_INVALID_RPC", e.getLocalizedMessage() ) );
            return;
        }
        address = config.getFeatureTypeAddress( qualiName );

        // CREATE WFS REQUEST
        String query = null;
        try {
            query = createWFSTransactionRequest( rpcEvent, "INSERT" );
            // special replacements handled individually - only needed in derived Listeners
            // query = replaceSpecialTemplateVars( query, rpcEvent );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_WFS_CREATE_REQ_FAILED", e.getLocalizedMessage() ) );
            return;
        }

        if( LOG.getLevel() == ILogger.LOG_DEBUG ){
            String msg = StringTools.concat( 300, "WFSTransactionRequest = ", query );
            LOG.logDebug( msg );
        }

        // PERFORM QUERY (WFS TRANSACTION)
        XMLFragment response = null;
        try {
            response = performRequest( query, address );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_PERFORM_REQ_FAILED", query, e.getLocalizedMessage() ) );
            return;
        }

        if( LOG.getLevel() == ILogger.LOG_DEBUG ){
            String msg = StringTools.concat( 300, "WFSTransactionResponse = ", response.getAsPrettyString() );
            LOG.logDebug( msg );
        }

        // HANLDE RESPONSE
        Object[] obj = null;
        try {
            obj = handleResponse( response );
            if ( obj != null && obj.length > 0 ) { /* handle obj in derived listeners */
            }
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_WFS_RESPONSE_ERROR", e.getLocalizedMessage() ) );
            return;
        }

    }

    /**
     * Validate the RPC request: number of RPCParameter must be 2 or more.
     *
     * The first param must contain the "FEATURE_TYPE". The second param must contain a struct with
     * all the parameters that shall be replaced in the WFS request template.
     *
     * If the struct contains a member "GEOM", then one more member is mandatory: "CRS".
     *
     * Further params will be ignored in this Listener. They might want to be used to handle
     * specific behaviour in derived Listeners.
     *
     * @param rpcEvent
     * @throws RPCException
     * @throws GeometryException
     * @throws UnknownCRSException
     */
    protected void validateRequest( RPCWebEvent rpcEvent )
                            throws RPCException, GeometryException, UnknownCRSException {

        RPCParameter[] params = rpcEvent.getRPCMethodCall().getParameters();

        // validity check for number of parameters in RPCMethodCall
        if ( params.length < 2 ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_WFS_WRONG_RPC_PARAMS_NUM", "2" ) );
        }

        String featureType = (String) params[0].getValue();
        if ( featureType == null || "".equals( featureType ) ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_MISSING_RPC_PARAM", "featureType", "1." ) );
        }

        RPCStruct rpcStruct = (RPCStruct) params[1].getValue();

        // check geometry-string represents a valid GEOMETRY-object
        RPCMember mem = rpcStruct.getMember( GEOMETRY );

        if ( mem != null ) {
            String geom = (String) mem.getValue();
            RPCMember crsMem = rpcStruct.getMember( CRS );
            if ( crsMem == null ) {
                throw new RPCException(
                                        Messages.getMessage( "IGEO_STD_MISSING_RPC_PARAM_DEPENDENCY", "GEOMETRY", "CRS" ) );
            }
            String crsName = (String) crsMem.getValue();
            CoordinateSystem crs = CRSFactory.create( crsName );

            Geometry g = WKTAdapter.wrap( geom, crs );

            if ( !( g instanceof Point ) && !( g instanceof Curve ) && !( g instanceof Surface ) ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_WFS_UNSERVED_GEOM", g.getClass().getName() ) );
            }
        }
    }

    /**
     * The FEATURE_TYPE passed in the rpc must be given with namespace and featuretype name in the
     * form: {http://some.address.com}:featureTypeName. This string gets extracted from the rpc and
     * transformed into a <code>QualifiedName</code>.
     *
     * @param rpcEvent
     * @return the QualifiedNames for the featureType in the passed rpcEvent
     * @throws WFSClientException
     *             if featureType cannot be transformed to a <code>QualifiedName</code>.
     */
    protected QualifiedName extractFeatureTypeAsQualifiedName( RPCWebEvent rpcEvent )
                            throws WFSClientException {

        RPCParameter[] params = rpcEvent.getRPCMethodCall().getParameters();
        String featureType = (String) params[0].getValue();

        String ns = featureType.substring( ( 1 + featureType.indexOf( "{" ) ), featureType.indexOf( "}:" ) );
        String ftName = featureType.substring( 2 + featureType.indexOf( "}:" ) );

        try {
            return new QualifiedName( null, ftName, new URI( ns ) );
        } catch ( URISyntaxException e ) {
            LOG.logError( e.getLocalizedMessage() );
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_INVALID_NS", featureType, ns ) );
        }
    }

    /**
     * Create a WFS request (transaction) from the params given in the rpc event and the module
     * configuration.
     *
     * @param rpcEvent
     * @param transactionType
     *            the type of WFS transaction (might be "INSERT", "UPDATE", "DELETE")
     * @return the request as string
     * @throws WFSClientException
     *             if featureType in rpcEvent cannot be transformed to a <code>QualifiedName</code>
     *             or if the query template could not be read.
     */
    protected String createWFSTransactionRequest( RPCWebEvent rpcEvent, String transactionType )
                            throws WFSClientException {

        String request = null;
        QualifiedName qualiName = extractFeatureTypeAsQualifiedName( rpcEvent );
        String wfsTemplate = null;

        if ( "INSERT".equals( transactionType ) ) {
            wfsTemplate = config.getFeatureTypeInsertTemplate( qualiName );
        } else if ( "UPDATE".equals( transactionType ) ) {
            wfsTemplate = config.getFeatureTypeUpdateTemplate( qualiName );
        } else if ( "DELETE".equals( transactionType ) ) {
            wfsTemplate = config.getFeatureTypeDeleteTemplate( qualiName );
        }

        if ( !new File( wfsTemplate ).isAbsolute() ) {
            wfsTemplate = getHomePath() + wfsTemplate;
        }

        StringBuffer template = new StringBuffer( 10000 );
        try {
            // XMLFragment transactionFrag = new XMLFragment();
            // transactionFrag.load( new URL( wfsTemplate ) );
            // template.append( transactionFrag.toString() );

            BufferedReader br = new BufferedReader( new FileReader( wfsTemplate ) );
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                template.append( line );
            }
            br.close();
        } catch ( IOException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_WRONG_TEMPLATE", wfsTemplate ) );
        }
        request = template.toString();

        // SUBSTITUTE VARIABLES IN TEMPLATE
        request = replaceTemplateVars( request, rpcEvent );

        return request;
    }

    /**
     * Send the request (e.g. wfs:Transaction, wfs:GetFeature) to the WFS of the passed address and
     * return the received WFS response as XMLFragment.
     *
     * @param request
     *            the request for the WFS
     * @param wfsAddress
     *            the address of the WFS
     * @return the response by the WFS as XMLFragment.
     * @throws WFSClientException
     *             if request agains wfsAddress failed, or if wfs response could not be loaded as
     *             XMLFragment
     */
    protected XMLFragment performRequest( String request, String wfsAddress )
                            throws WFSClientException {

        if( LOG.getLevel() == ILogger.LOG_DEBUG ){
            String msg = StringTools.concat( 300, "performRequest \n", request, "\nto wfsAddress ", wfsAddress );
            LOG.logDebug( msg );
        }

        XMLFragment xmlFrag = null;

        StringRequestEntity re = new StringRequestEntity( request );
        PostMethod post = new PostMethod( wfsAddress );
        post.setRequestEntity( re );
        post.setRequestHeader( "Content-type", "text/xml;charset=" + CharsetUtils.getSystemCharset() );
        InputStream is = null;
        try {
            HttpClient client = new HttpClient();
            client = WebUtils.enableProxyUsage( client, new URL( wfsAddress ) );
            client.executeMethod( post );
            is = post.getResponseBodyAsStream();
        } catch ( IOException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_QUERY_FAILED", wfsAddress ) );
        }

        xmlFrag = new XMLFragment();
        try {
            InputStreamReader isr = new InputStreamReader( is, CharsetUtils.getSystemCharset() );
            xmlFrag.load( isr, wfsAddress );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_LOAD_XMLFRAG_FAILED" ) );
        }

        return xmlFrag;
    }

    /**
     * Handle the WFS transaction response.
     *
     * A positive response may be overwritten in method handlePositiveResponse(). A negative
     * response may be overwritten in method handleNegativeResponse().
     *
     * @param response
     * @return any objects that are returned from handlePositiveResponse() and that might be needed
     *         in derived listeners. Here, it returns null.
     * @throws WFSClientException
     *             if the transaction response does not contain a root element of
     *             "wfs:TransactionResponse"
     */
    protected Object[] handleResponse( XMLFragment response )
                            throws WFSClientException {

        Object[] obj = null;

        Element rootElem = response.getRootElement();
        String root = rootElem.getNodeName();

        if( LOG.getLevel() == ILogger.LOG_DEBUG ){
            String msg = StringTools.concat( 100, "Response root name: " + root );
            LOG.logDebug( msg );
        }
        // root.contains("TransactionResponse")
        if ( "wfs:TransactionResponse".equals( root ) ) {
            obj = handlePositiveResponse( response );
        } else if ( "ServiceExceptionReport".equals( root ) ) {
            handleNegativeResponse( response );
        } else {
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_UNKNOWN_TRANSACTION_RESPONSE", root ) );
        }

        return obj;
    }

    /**
     * A positive response by the WFS needs to be handled individually in each derived Listener.
     * Override the method, if needed.
     *
     * In this basic implementation, it sets the request attribute MESSAGE with the value of
     * i18n.IGEO_STD_WFS_TRANSACTION_SUCCESS
     *
     * @param response
     * @return any objects that might be needed. Here, it returns null.
     */
    protected Object[] handlePositiveResponse( XMLFragment response ) {
        this.getRequest().setAttribute( Constants.MESSAGE, Messages.getMessage( "IGEO_STD_WFS_TRANSACTION_SUCCESS" ) );
        return null;
    }

    /**
     * A negative response by the WFS needs to be handled individually in each derived Listener.
     * Override the method, if needed.
     *
     * In this basic implementation, it throws a WFSClientException IGEO_STD_WFS_TRANSACTION_FAILED,
     * containing the message of the ServiceException element of the response.
     *
     * @param response
     * @throws WFSClientException
     *             in any case. always.
     */
    protected void handleNegativeResponse( XMLFragment response )
                            throws WFSClientException {
        Element rootElement = response.getRootElement();
        NodeList errors = rootElement.getElementsByTagName( "ServiceException" );
        throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_TRANSACTION_FAILED",
                                                           errors.item( 0 ).getTextContent() ) );
    }

    /**
     * This method is used to replace all placeholders in the template with values given in the
     * struct of the second rpc parameter only. For the replacement to work, the placeholders in the
     * template need to have the same name ($XYZ) as the struct members (XYZ) in the rpc.
     *
     * @param request
     * @param rpcEvent
     * @return the passed request after replacing the templates variables
     */
    private String replaceTemplateVars( String request, RPCWebEvent rpcEvent ) {

        // standard replacements
        RPCParameter[] params = rpcEvent.getRPCMethodCall().getParameters();
        RPCStruct rpcStruct = (RPCStruct) params[1].getValue();
        RPCMember[] members = rpcStruct.getMembers();
        for ( int i = 0; i < members.length; i++ ) {

            if ( "GEOMETRY".equals( members[i].getName() ) ) {
                String geom = (String) members[i].getValue();
                // geom = "POLYGON(( 12,34 13,35 ))";
                geom = geom.substring( geom.lastIndexOf( '(' ) + 1, geom.indexOf( ')' ) ).trim();
                request = StringTools.replace( request, "$GEOMETRY", geom, true );
            } else {
                request = StringTools.replace( request, "$" + members[i].getName(), (String) members[i].getValue(),
                                               true );
            }
        }
        return request;
    }

    /**
     * this method will be overwritten in derived listeners
     *
     * @param request
     * @param args
     *            any number of Objects needed in this method
     * @return the request, with all special variables replaced. In this implementation, it returns
     *         the original request.
     * @throws WFSClientException
     */
    protected String replaceSpecialTemplateVars( String request, Object... args )
                            throws WFSClientException {
        // special replacements handled individually - only needed in derived Listeners
        return request;
    }

    /**
     * this method will be overwritten in derived listeners
     *
     * @param request
     * @param args
     *            any number of Objects needed in this method
     * @return the request, with all update variables replaced. In this implementation, it returns
     *         the original request.
     * @throws WFSClientException
     */
    protected String replaceUpdateTemplateVars( String request, Object... args )
                            throws WFSClientException {
        // special replacements handled individually - only needed in derived Listeners
        return request;
    }

    /**
     * this method will be overwritten in derived listeners
     *
     * @param request
     * @param args
     *            any number of Objects needed in this method
     * @return the request, with all delete variables replaced. In this implementation, it returns
     *         the original request.
     * @throws WFSClientException
     */
    protected String replaceDeleteTemplateVars( String request, Object... args )
                            throws WFSClientException {
        // special replacements handled individually - only needed in derived Listeners
        return request;
    }

}
