//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.services.wfs.format.gml;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.protocol.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.protocol.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.services.wfs.WebFeatureService.getXMLResponseWriter;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.WebFeatureService;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class GmlGetGmlObjectHandler extends AbstractGmlRequestHandler {

    GmlGetGmlObjectHandler( GMLFormat format ) {
        super( format );
    }

    void doSingleObjectResponse( Version version, String traverseXLinkDepthStr, String id, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {

        int resolveDepth = 0;
        if ( traverseXLinkDepthStr != null ) {
            if ( "*".equals( traverseXLinkDepthStr ) ) {
                resolveDepth = -1;
            } else {
                try {
                    resolveDepth = Integer.parseInt( traverseXLinkDepthStr );
                } catch ( NumberFormatException e ) {
                    String msg = Messages.get( "WFS_TRAVERSEXLINKDEPTH_INVALID", traverseXLinkDepthStr );
                    throw new OWSException( new InvalidParameterValueException( msg ) );
                }
            }
        }

        GMLObject o = retrieveObject( id );
        GMLVersion gmlVersion = options.getGmlVersion();

        String schemaLocation = null;
        if ( o instanceof Feature ) {
            schemaLocation = WebFeatureService.getSchemaLocation( version, gmlVersion, ( (Feature) o ).getName() );
        } else if ( o instanceof Geometry ) {
            switch ( gmlVersion ) {
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

        String contentType = options.getMimeType();
        XMLStreamWriter xmlStream = getXMLResponseWriter( response, contentType, schemaLocation );
        GMLStreamWriter gmlStream = createGMLStreamWriter( gmlVersion, xmlStream );
        gmlStream.setOutputCrs( format.getMaster().getDefaultQueryCrs() );
        gmlStream.setRemoteXLinkTemplate( getObjectXlinkTemplate( version, gmlVersion ) );
        gmlStream.setXLinkDepth( resolveDepth );
        gmlStream.setCoordinateFormatter( options.getFormatter() );
        gmlStream.setNamespaceBindings( format.getMaster().getStoreManager().getPrefixToNs() );
        gmlStream.setGenerateBoundedByForFeatures( options.isGenerateBoundedByForFeatures() );
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

}
