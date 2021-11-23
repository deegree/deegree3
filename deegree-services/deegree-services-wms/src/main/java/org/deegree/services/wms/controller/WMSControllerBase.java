//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.wms.controller;

import static java.lang.Integer.parseInt;
import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;
import static org.deegree.services.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.xml.stream.XMLOutputFactory;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.WMSController.Controller;
import org.deegree.services.wms.controller.exceptions.ExceptionsManager;
import org.deegree.services.wms.controller.exceptions.SerializingException;
import org.slf4j.Logger;

/**
 * <code>WMSControllerBase</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class WMSControllerBase implements Controller {

    private static final Logger LOG = getLogger( WMSControllerBase.class );

    protected String EXCEPTION_DEFAULT, EXCEPTION_INIMAGE, EXCEPTION_BLANK;

    protected XMLExceptionSerializer exceptionSerializer;

    protected String EXCEPTION_MIME = "text/xml";

    private final ExceptionsManager exceptionsManager;

    public WMSControllerBase( ExceptionsManager exceptionsManager ) {
        this.exceptionsManager = exceptionsManager;
    }

    @Override
    public void handleException( Map<String, String> map, WMSRequestType req, OWSException e,
                                 HttpResponseBuffer response, WMSController controller )
                            throws ServletException {
        String exceptionFormat = detectExceptionsParameter( map, req );

        writeException( map, e, response, exceptionFormat, controller );
    }

    @Override
    public void getCapabilities( String getUrl, String postUrl, String updateSequence, MapService service,
                                 HttpResponseBuffer response, ServiceIdentification identification,
                                 ServiceProvider provider, Map<String, String> customParameters,
                                 WMSController controller, OWSMetadataProvider metadata )
                            throws OWSException, IOException {
        getUrl = getUrl.substring( 0, getUrl.length() - 1 );
        if ( updateSequence != null && updateSequence.trim().length() > 0 ) {
            try {
                int seq = parseInt( updateSequence );
                if ( seq > service.getCurrentUpdateSequence() ) {
                    throw new OWSException( get( "WMS.INVALID_UPDATE_SEQUENCE", updateSequence ),
                                            OWSException.INVALID_UPDATE_SEQUENCE );
                }
                if ( seq == service.getCurrentUpdateSequence() ) {
                    throw new OWSException( get( "WMS.CURRENT_UPDATE_SEQUENCE" ), OWSException.CURRENT_UPDATE_SEQUENCE );
                }
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.INVALID_UPDATE_SEQUENCE", updateSequence ),
                                        OWSException.INVALID_UPDATE_SEQUENCE );
            }
        }

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( IS_REPAIRING_NAMESPACES, true );
        exportCapas( getUrl, postUrl, service, response, identification, provider, customParameters, controller,
                     metadata );
    }

    protected abstract void exportCapas( String getUrl, String postUrl, MapService service,
                                         HttpResponseBuffer response, ServiceIdentification identification,
                                         ServiceProvider provider, Map<String, String> customParameters,
                                         WMSController controller, OWSMetadataProvider metadata )
                            throws IOException, OWSException;

    protected abstract Version getVersion();

    private String detectExceptionsParameter( Map<String, String> map, WMSRequestType req ) {
        String exceptionFormatParameter = map.get( "EXCEPTIONS" );
        String notNullExceptionFormat = exceptionFormatParameter == null ? EXCEPTION_DEFAULT : exceptionFormatParameter;
        if ( isSupportedFormatForRequestType( req, notNullExceptionFormat ) )
            return notNullExceptionFormat;
        return EXCEPTION_DEFAULT;
    }

    private void writeException( Map<String, String> map, OWSException e, HttpResponseBuffer response,
                                 String exceptionsFormat, WMSController controller )
                            throws ServletException {
        try {
            writeExceptionCatchExceptions( map, e, response, exceptionsFormat, controller );
        } catch ( ServletException ignored ) {
            sendException( e, response, controller );
        }
    }

    private void writeExceptionCatchExceptions( Map<String, String> map, OWSException e, HttpResponseBuffer response,
                                                String exceptionsFormat, WMSController controller )
                            throws ServletException {
        try {
            exceptionsManager.serialize( getVersion(), exceptionsFormat, response, e, exceptionSerializer, map );
        } catch ( SerializingException se ) {
            LOG.info( "An exception occured during serializing the exception, default serializer is used. Exception: {}",
                      se.getMessage() );
            controller.sendException( null, exceptionSerializer, e, response );
        }
    }

    private boolean isSupportedFormatForRequestType( WMSRequestType req, String exceptions ) {
        if ( req == null ) {
            if ( EXCEPTION_BLANK.equals( exceptions ) || EXCEPTION_INIMAGE.equals( exceptions ) )
                return false;
            return true;
        }
        switch ( req ) {
        case map:
        case GetMap:
        case GetLegendGraphic:
            return true;
        default:
            if ( EXCEPTION_BLANK.equals( exceptions ) || EXCEPTION_INIMAGE.equals( exceptions ) )
                return false;
            return true;
        }
    }

}
