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
package org.deegree.portal.standard.security.control;

import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.util.StringTools.stackTraceToString;
import static org.deegree.i18n.Messages.get;
import static org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument;
import static org.deegree.portal.standard.security.control.ClientHelper.acquireAccess;
import static org.deegree.portal.standard.security.control.SecurityHelper.checkForAdminRole;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletRequest;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.util.StringPair;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument_1_1_0;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.model.Service;
import org.xml.sax.SAXException;

/**
 * <code>UpdateServiceListener</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class UpdateServiceListener extends AbstractListener {

    private static final ILogger LOG = getLogger( UpdateServiceListener.class );

    private static void addAllLayers( String prefix, Layer layer, List<StringPair> objects )
                            throws GeneralSecurityException {
        if ( layer.getName() != null ) {
            objects.add( new StringPair( layer.getName(), layer.getTitle() ) );
        }
        for ( Layer l : layer.getLayer() ) {
            addAllLayers( prefix, l, objects );
        }
    }

    @Override
    public void actionPerformed( FormEvent event ) {
        RPCParameter[] params = ( (RPCWebEvent) event ).getRPCMethodCall().getParameters();

        ServletRequest request = getRequest();

        SecurityAccess access = null;

        try {
            // perform access check
            access = acquireAccess( this );
            checkForAdminRole( access );

            boolean isWMS = params[1].getValue().toString().equalsIgnoreCase( "wms" );
            boolean isWFS = params[1].getValue().toString().equalsIgnoreCase( "wfs" );
            if ( !isWMS && !isWFS ) {
                // error message? client is faulty
                LOG.logError( "Unknown/unsupported service type to register: " + params[1].getValue() );
            }

            String address = params[0].getValue().toString();
            String getCapa = address + ( ( !address.endsWith( "&" ) && !address.endsWith( "?" ) ) ? "?" : "" )
                             + "request=GetCapabilities&";

            Service newService = null;

            if ( isWMS ) {
                OGCCapabilities capa = getWMSCapabilitiesDocument( new URL( getCapa + "service=WMS&version=1.1.1" ) ).parseCapabilities();
                if ( capa instanceof WMSCapabilities ) {
                    WMSCapabilities cap = (WMSCapabilities) capa;
                    LinkedList<StringPair> objects = new LinkedList<StringPair>();

                    addAllLayers( "[" + address + "]:", cap.getLayer(), objects );

                    newService = new Service( -1, address, cap.getServiceIdentification().getTitle(), objects, "WMS" );
                }
            }

            if ( isWFS ) {
                WFSCapabilitiesDocument_1_1_0 doc = new WFSCapabilitiesDocument_1_1_0();
                doc.load( new URL( getCapa + "service=WFS&version=1.1.0" ) );
                WFSCapabilities capa = (WFSCapabilities) doc.parseCapabilities();
                LinkedList<StringPair> objects = new LinkedList<StringPair>();
                for ( WFSFeatureType ft : capa.getFeatureTypeList().getFeatureTypes() ) {
                    objects.add( new StringPair( ft.getName().getFormattedString(), ft.getTitle() ) );
                }

                newService = new Service( -1, address, capa.getServiceIdentification().getTitle(), objects, "WFS" );
            }

            Service oldService = access.getServiceByAddress( address );
            request.setAttribute( "OLDSERVICE", oldService );
            request.setAttribute( "NEWSERVICE", newService );

        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_FAIL_INIT_SERVICES_EDITOR", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage(), e );
        } catch ( SAXException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_UPDATE_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
        } catch ( IOException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_UPDATE_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
        } catch ( XMLParsingException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_UPDATE_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
        } catch ( InvalidCapabilitiesException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_UPDATE_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
        } catch ( Exception e ) {
            LOG.logError( get( "IGEO_STD_SEC_ERROR_UNKNOWN", stackTraceToString( e ) ) );
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_UPDATE_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
        }
    }

}
