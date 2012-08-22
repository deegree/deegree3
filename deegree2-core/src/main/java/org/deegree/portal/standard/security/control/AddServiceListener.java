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
import static org.deegree.portal.standard.security.control.ClientHelper.TYPE_FEATURETYPE;
import static org.deegree.portal.standard.security.control.ClientHelper.TYPE_LAYER;
import static org.deegree.portal.standard.security.control.SecurityHelper.acquireTransaction;
import static org.deegree.portal.standard.security.control.SecurityHelper.checkForAdminRole;
import static org.deegree.security.drm.SecurityAccessManager.getInstance;

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
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.model.Service;
import org.xml.sax.SAXException;

/**
 * <code>AddServiceListener</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class AddServiceListener extends AbstractListener {

    private static final ILogger LOG = getLogger( AddServiceListener.class );

    private static void addAllLayers( SecurityTransaction transaction, String prefix, Layer layer,
                                      List<StringPair> objects )
                            throws GeneralSecurityException {
        if ( layer.getName() != null ) {
            objects.add( new StringPair( layer.getName(), layer.getTitle() ) );
            transaction.registerSecuredObject( TYPE_LAYER, prefix + layer.getName(), layer.getTitle() );
        }
        for ( Layer l : layer.getLayer() ) {
            addAllLayers( transaction, prefix, l, objects );
        }
    }

    @Override
    public void actionPerformed( FormEvent event ) {
        RPCParameter[] params = ( (RPCWebEvent) event ).getRPCMethodCall().getParameters();

        ServletRequest request = getRequest();

        SecurityTransaction transaction = null;
        SecurityAccessManager manager = null;

        try {
            // perform access check
            manager = getInstance();
            transaction = acquireTransaction( this );
            checkForAdminRole( transaction );

            boolean isWMS = params[1].getValue().toString().equalsIgnoreCase( "wms" );
            boolean isWFS = params[1].getValue().toString().equalsIgnoreCase( "wfs" );
            if ( !isWMS && !isWFS ) {
                // error message? client is faulty
                LOG.logError( "Unknown/unsupported service type to register: " + params[1].getValue() );
            }

            String address = params[0].getValue().toString();

            if ( address.indexOf( "?" ) != -1 ) {
                address = address.substring( 0, address.indexOf( "?" ) );
            }

            try {
                if ( transaction.getServiceByAddress( address ) != null ) {
                    request.setAttribute( "SOURCE", getClass().getSimpleName() );
                    request.setAttribute( "MESSAGE", get( "IGEO_STD_SEC_SERVICE_EXISTS", address ) );
                    setNextPage( "error.jsp" );
                    manager.abortTransaction( transaction );
                    return;
                }
            } catch ( GeneralSecurityException e ) {
                // so the lookup failed
            }

            String getCapa = address + "?request=GetCapabilities&";

            if ( isWMS ) {
                OGCCapabilities capa = getWMSCapabilitiesDocument( new URL( getCapa + "service=WMS&version=1.1.1" ) ).parseCapabilities();
                if ( capa instanceof WMSCapabilities ) {
                    WMSCapabilities cap = (WMSCapabilities) capa;
                    LinkedList<StringPair> objects = new LinkedList<StringPair>();

                    addAllLayers( transaction, "[" + address + "]:", cap.getLayer(), objects );

                    transaction.registerService( address, cap.getServiceIdentification().getTitle(), objects, "WMS" );
                }
            }

            if ( isWFS ) {
                WFSCapabilitiesDocument_1_1_0 doc = new WFSCapabilitiesDocument_1_1_0();
                doc.load( new URL( getCapa + "service=WFS&version=1.1.0" ) );
                WFSCapabilities capa = (WFSCapabilities) doc.parseCapabilities();
                LinkedList<StringPair> objects = new LinkedList<StringPair>();
                for ( WFSFeatureType ft : capa.getFeatureTypeList().getFeatureTypes() ) {
                    objects.add( new StringPair( ft.getName().getFormattedString(), ft.getTitle() ) );
                    transaction.registerSecuredObject( TYPE_FEATURETYPE, "[" + address + "]:"
                                                                         + ft.getName().getFormattedString(),
                                                       ft.getTitle() );
                }

                transaction.registerService( address, capa.getServiceIdentification().getTitle(), objects, "WFS" );
            }

            manager.commitTransaction( transaction );

            LinkedList<Service> services = transaction.getAllServices();
            request.setAttribute( "SERVICES", services );

        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_FAIL_INIT_SERVICES_EDITOR", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage(), e );
            try {
                if ( manager != null ) {
                    manager.abortTransaction( transaction );
                }
            } catch ( GeneralSecurityException e1 ) {
                LOG.logError( "Unknown error", e1 );
            }
        } catch ( SAXException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_ADD_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
            try {
                if ( manager != null ) {
                    manager.abortTransaction( transaction );
                }
            } catch ( GeneralSecurityException e1 ) {
                LOG.logError( "Unknown error", e1 );
            }
        } catch ( IOException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_ADD_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
            try {
                if ( manager != null ) {
                    manager.abortTransaction( transaction );
                }
            } catch ( GeneralSecurityException e1 ) {
                LOG.logError( "Unknown error", e1 );
            }
        } catch ( XMLParsingException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_ADD_SERVICE2" ) );
            setNextPage( "error.jsp" );
            try {
                if ( manager != null ) {
                    manager.abortTransaction( transaction );
                }
            } catch ( GeneralSecurityException e1 ) {
                LOG.logError( "Unknown error", e1 );
            }
        } catch ( InvalidCapabilitiesException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_ADD_SERVICE2" ) );
            setNextPage( "error.jsp" );
            try {
                if ( manager != null ) {
                    manager.abortTransaction( transaction );
                }
            } catch ( GeneralSecurityException e1 ) {
                LOG.logError( "Unknown error", e1 );
            }
        } catch ( Exception e ) {
            LOG.logError( get( "IGEO_STD_SEC_ERROR_UNKNOWN", stackTraceToString( e ) ) );
            try {
                if ( manager != null ) {
                    manager.abortTransaction( transaction );
                }
            } catch ( GeneralSecurityException e1 ) {
                LOG.logError( "Unknown error", e1 );
                getRequest().setAttribute( "SOURCE", this.getClass().getName() );
                getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_ADD_SERVICE", e1.getMessage() ) );
                setNextPage( "error.jsp" );
                return;

            }
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_ADD_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
        }
    }

}
