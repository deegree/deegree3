//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wfs;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.services.controller.exception.ControllerException.NO_APPLICABLE_CODE;
import static org.deegree.services.controller.ows.OWSException.OPERATION_NOT_SUPPORTED;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link GetGmlObject} requests for the {@link WFSController}.
 * 
 * @see WFSController
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetGmlObjectHandler {

    private static final Logger LOG = LoggerFactory.getLogger( GetGmlObjectHandler.class );

    private final WFSController master;

    private final WFService service;

    private final CoordinateFormatter formatter;

    /**
     * Creates a new {@link GetGmlObjectHandler} instance that uses the given service to lookup requested
     * {@link FeatureType}s.
     * 
     * @param master
     * 
     * @param service
     *            WFS instance used to lookup the feature types
     * @param formatter
     *            coordinate formatter to use, must not be <code>null</code>
     */
    GetGmlObjectHandler( WFSController master, WFService service, CoordinateFormatter formatter ) {
        this.master = master;
        this.service = service;
        this.formatter = formatter;
    }

    /**
     * Performs the given {@link GetGmlObject} request.
     * 
     * @param request
     *            request to be handled
     * @param response
     *            response that is used to write the result
     * @throws OWSException
     *             if a WFS specific exception occurs, e.g. the requested object is not known
     * @throws IOException
     * @throws XMLStreamException
     */
    void doGetGmlObject( GetGmlObject request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {

        LOG.debug( "doGetGmlObject: " + request );

        GMLObject o = retrieveObject( request.getRequestedId() );
        GMLVersion outputFormat = determineOutputFormat( request );

        response.setContentType( outputFormat.getMimeType() );

        int traverseXLinkDepth = 0;
        if ( request.getTraverseXlinkDepth() != null ) {
            if ( "*".equals( request.getTraverseXlinkDepth() ) ) {
                traverseXLinkDepth = -1;
            } else {
                try {
                    traverseXLinkDepth = Integer.parseInt( request.getTraverseXlinkDepth() );
                } catch ( NumberFormatException e ) {
                    String msg = Messages.get( "WFS_TRAVERSEXLINKDEPTH_INVALID", request.getTraverseXlinkDepth() );
                    throw new OWSException( new InvalidParameterValueException( msg ) );
                }

            }
        }

        String schemaLocation = null;
        if ( o instanceof Feature ) {
            schemaLocation = WFSController.getSchemaLocation( request.getVersion(), outputFormat,
                                                              ( (Feature) o ).getName() );
        } else if ( o instanceof Geometry ) {
            switch ( outputFormat ) {
            case GML_2:
                schemaLocation = GMLNS + " http://schemas.opengis.net/gml/2.1.2.1/geometry.xsd";
                break;
            case GML_30:
                schemaLocation = GMLNS + " http://schemas.opengis.net/gml/3.0.1/base/geometryComplexes.xsd";
                break;
            case GML_31:
                schemaLocation = GMLNS + " http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd";
                break;
            case GML_32:
                schemaLocation = GML3_2_NS + " http://schemas.opengis.net/gml/3.2.1/geometryComplexes.xsd";
                break;
            }
        } else {
            String msg = "Error exporting GML object: only exporting of features and geometries is implemented.";
            throw new OWSException( msg, OPERATION_NOT_SUPPORTED );
        }
        XMLStreamWriter xmlStream = WFSController.getXMLResponseWriter( response, schemaLocation );
        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( outputFormat, xmlStream );
        gmlStream.setLocalXLinkTemplate( master.getObjectXlinkTemplate( request.getVersion(), outputFormat ) );
        gmlStream.setXLinkDepth( traverseXLinkDepth );
        gmlStream.setCoordinateFormatter( formatter );
        try {
            gmlStream.write( o );
        } catch ( UnknownCRSException e ) {
            String msg = "Error exporting GML object: " + e.getMessage();
            throw new OWSException( msg, NO_APPLICABLE_CODE );
        } catch ( TransformationException e ) {
            String msg = "Error exporting GML object: " + e.getMessage();
            throw new OWSException( msg, NO_APPLICABLE_CODE );
        }
    }

    private GMLObject retrieveObject( String id )
                            throws OWSException {
        GMLObject o = null;
        for ( FeatureStore fs : service.getStores() ) {
            try {
                o = fs.getObjectById( id );
            } catch ( FeatureStoreException e ) {
                throw new OWSException( e.getMessage(), NO_APPLICABLE_CODE );
            }
            if ( o != null ) {
                break;
            }
        }
        if ( o == null ) {
            String msg = Messages.getMessage( "WFS_NO_SUCH_OBJECT", id );
            throw new OWSException( new InvalidParameterValueException( msg ) );
        }
        return o;
    }

    /**
     * Determines the requested (GML) output format.
     * 
     * TODO integrate handling for custom formats
     * 
     * @param request
     *            request to be analyzed, must not be <code>null</code>
     * @return version to use for the written GML, never <code>null</code>
     * @throws OWSException
     *             if the requested format is not supported
     */
    private GMLVersion determineOutputFormat( GetGmlObject request )
                            throws OWSException {

        GMLVersion gmlVersion = master.determineFormat( request.getVersion(), request.getOutputFormat() );
        if ( gmlVersion == null ) {
            String msg = "Unsupported output format '" + request.getOutputFormat() + "'";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "outputFormat" );
        }
        return gmlVersion;
    }
}
