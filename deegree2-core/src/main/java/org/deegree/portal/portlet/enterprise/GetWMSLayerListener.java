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
package org.deegree.portal.portlet.enterprise;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCUtils;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.owscommon_new.DomainType;
import org.deegree.owscommon_new.Operation;
import org.deegree.owscommon_new.OperationsMetadata;
import org.deegree.portal.PortalException;
import org.deegree.portal.standard.wms.util.ClientHelper;
import org.xml.sax.SAXException;

/**
 * Lister class for accessing the layers of WMS and return it to the client.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class GetWMSLayerListener extends AbstractListener {

    private static ILogger LOG = LoggerFactory.getLogger( GetWMSLayerListener.class );

    /**
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpc = (RPCWebEvent) event;
        try {
            validate( rpc );
        } catch ( Exception e ) {
            gotoErrorPage( "Not a valid RPC for GetWMSLayer\n" + e.getMessage() );
        }

        WMSCapabilities capabilities = null;
        URL url = null;
        boolean useAuthentification = false;
        try {
            RPCMethodCall mc = rpc.getRPCMethodCall();
            RPCParameter param = mc.getParameters()[0];
            RPCStruct struct = (RPCStruct) param.getValue();
            String tmp = RPCUtils.getRpcPropertyAsString( struct, "useAuthentification" );
            useAuthentification = "true".equalsIgnoreCase( tmp );
            url = getURL( struct, useAuthentification );
            capabilities = getWMSCapabilities( url );
        } catch ( MalformedURLException ue ) {
            LOG.logError( ue.getMessage(), ue );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_INVALID_URL", url ) );
            return;
        } catch ( PortalException e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_ADDWMS_INVALID_VERSION" ) );
            return;
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_ADDWMS_NO_TARGET", url ) );
            return;
        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_INVALID_XML", url ) );
            return;
        } catch ( InvalidCapabilitiesException e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_ADDWMS_INVALID_CAPS", url ) );
            return;
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_INVALID_XML", url ) );
            return;
        }

        String s = ClientHelper.getLayersAsTree( capabilities.getLayer() );
        getRequest().setAttribute( "WMSLAYER", s );
        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter param = mc.getParameters()[0];
        RPCStruct struct = (RPCStruct) param.getValue();
        RPCMember address = struct.getMember( "WMSURL" );
        getRequest().setAttribute( "WMSURL", address );
        getRequest().setAttribute( "WMSVERSION", capabilities.getVersion() );
        getRequest().setAttribute( "USEAUTHENTICATION", useAuthentification );
        s = capabilities.getServiceIdentification().getTitle();
        s = s.replaceAll( "'", "" );
        getRequest().setAttribute( "WMSNAME", s );

        OperationsMetadata om = capabilities.getOperationMetadata();
        Operation op = om.getOperation( new QualifiedName( "GetMap" ) );
        if ( op == null )
            om.getOperation( new QualifiedName( "map" ) );
        DomainType parameter = (DomainType) op.getParameter( new QualifiedName( "Format" ) );

        // the request needs a String[], not a String
        List<TypedLiteral> values = parameter.getValues();
        String[] valueArray = new String[values.size()];
        for ( int i = 0; i < values.size(); ++i )
            valueArray[i] = values.get( i ).getValue();
        getRequest().setAttribute( "FORMATS", valueArray );
    }

    /**
     * @param rpc
     * @throws PortalException
     */
    private void validate( RPCWebEvent rpc )
                            throws PortalException {
        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter param = mc.getParameters()[0];
        RPCStruct struct = (RPCStruct) param.getValue();
        RPCMember address = struct.getMember( "WMSURL" );
        if ( address == null ) {
            throw new PortalException( "missing parameter WMSURL in RPC for GetWMSLayer" );
        }
        RPCMember version = struct.getMember( "VERSION" );
        if ( version != null
             && ( ( (String) version.getValue() ).compareTo( "1.3.0" ) > 0 || ( (String) version.getValue() ).compareTo( "1.1.0" ) < 0 ) ) {
            throw new PortalException( "VERSION must be >= 1.1.0 and < 1.3.0 but it is " + version.getValue() );
        }
    }

    /**
     *
     * @param struct
     * @return wms url
     * @throws MalformedURLException
     */
    private URL getURL( RPCStruct struct, boolean useAuthentification )
                            throws MalformedURLException {

        String address = RPCUtils.getRpcPropertyAsString( struct, "WMSURL" );

        // according to OGC WMS 1.3 Testsuite a URL to a service operation
        // via HTTPGet must end with '?' or '&'
        String href = OWSUtils.validateHTTPGetBaseURL( address );

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession();

        StringBuffer sb = new StringBuffer( href );
        sb.append( "service=WMS&request=GetCapabilities" );
        String version = RPCUtils.getRpcPropertyAsString( struct, "VERSION" );
        if ( version != null ) {
            sb.append( "&version=" ).append( version );
        }
        if ( useAuthentification ) {
            String user = ( (org.apache.jetspeed.om.security.BaseJetspeedUser) session.getAttribute( "turbine.user" ) ).getUserName();
            String password = ( (org.apache.jetspeed.om.security.BaseJetspeedUser) session.getAttribute( "turbine.user" ) ).getPassword();
            sb.append( "&user=" ).append( user );
            sb.append( "&password=" ).append( password );
        }
        LOG.logDebug( "GetCapabilities: ", sb );
        return new URL( sb.toString() );
    }

    /**
     *
     * @param url
     * @return the capabilities object
     * @throws XMLParsingException
     * @throws PortalException
     * @throws IOException
     * @throws SAXException
     * @throws InvalidCapabilitiesException
     * @throws XMLParsingException
     */
    private WMSCapabilities getWMSCapabilities( URL url )
                            throws PortalException, IOException, SAXException, InvalidCapabilitiesException,
                            XMLParsingException {

        WMSCapabilitiesDocument doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( url );
        WMSCapabilities capabilities = (WMSCapabilities) doc.parseCapabilities();

        if ( capabilities.getVersion().compareTo( "1.0.0" ) < 0 || capabilities.getVersion().compareTo( "1.3.0" ) > 0 ) {
            throw new PortalException( "VERSION must be >= 1.1.0 && < 1.3.0" );
        }

        return capabilities;
    }
}
