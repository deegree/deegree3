//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.controller.capabilities;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.XMLAdapter.maybeWriteElement;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.services.wms.controller.capabilities.Capabilities111XMLAdapter.writeDimensions;
import static org.deegree.services.wms.controller.capabilities.WmsCapabilities111SpatialMetadataWriter.writeSrsAndEnvelope;

import java.util.HashSet;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.services.jaxb.wms.LanguageStringType;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.style.se.unevaluated.Style;

/**
 * Responsible for writing out old style layer capabilities.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class WmsCapabilities111LegacyWriter {

    private MapService service;

    private String getUrl;

    private OWSMetadataProvider metadata;

    private WMSController controller;

    private Capabilities111XMLAdapter capWriter;

    WmsCapabilities111LegacyWriter( MapService service, String getUrl, OWSMetadataProvider metadata,
                                    WMSController controller, Capabilities111XMLAdapter capWriter ) {
        this.service = service;
        this.getUrl = getUrl;
        this.metadata = metadata;
        this.controller = controller;
        this.capWriter = capWriter;
    }

    @Deprecated
    void writeLayers( XMLStreamWriter writer, Layer layer )
                            throws XMLStreamException {
        if ( layer.getTitle() == null || !layer.isAvailable() ) {
            for ( Layer l : new LinkedList<Layer>( layer.getChildren() ) ) {
                writeLayers( writer, l );
            }
            return;
        }

        writer.writeStartElement( "Layer" );
        if ( layer.isQueryable() ) {
            writer.writeAttribute( "queryable", "1" );
        }

        maybeWriteElement( writer, "Name", layer.getName() );
        writeElement( writer, "Title", layer.getTitle() );
        maybeWriteElement( writer, "Abstract", layer.getAbstract() );

        if ( !layer.getKeywords().isEmpty() ) {
            writer.writeStartElement( "KeywordList" );
            for ( Pair<CodeType, LanguageStringType> p : layer.getKeywords() ) {
                writeElement( writer, "Keyword", p.second.getValue() );
            }
            writer.writeEndElement();
        }

        writeSrsAndEnvelope( writer, layer.getSrs(), layer.getBbox() );

        writeDimensions( writer, layer.getDimensions() );

        writeMetadataUrls( writer, layer );
        writeStyles( writer, layer );
        writeScaleHints( writer, layer );

        for ( Layer l : new LinkedList<Layer>( layer.getChildren() ) ) {
            writeLayers( writer, l );
        }

        writer.writeEndElement();
    }

    private void writeMetadataUrls( XMLStreamWriter writer, Layer layer )
                            throws XMLStreamException {
        if ( layer.getAuthorityURL() != null ) {
            writer.writeStartElement( "AuthorityURL" );
            writer.writeAttribute( "name", "fromISORecord" );
            writer.writeStartElement( "OnlineResource" );
            writer.writeNamespace( XLINK_PREFIX, XLNNS );
            writer.writeAttribute( XLNNS, "href", layer.getAuthorityURL() );
            writer.writeEndElement();
            writer.writeEndElement();
        }

        if ( layer.getAuthorityIdentifier() != null ) {
            writer.writeStartElement( "Identifier" );
            writer.writeAttribute( "authority", "fromISORecord" );
            writer.writeCharacters( layer.getAuthorityIdentifier() );
            writer.writeEndElement();
        }

        writeMetadataUrl( writer, layer );
    }

    private void writeMetadataUrl( XMLStreamWriter writer, Layer layer )
                            throws XMLStreamException {
        String mdUrl = getMetadataUrl( layer );

        if ( mdUrl != null ) {
            writer.writeStartElement( "MetadataURL" );
            writer.writeAttribute( "type", "ISO19115:2003" );
            writeElement( writer, "Format", "application/xml" );
            writer.writeStartElement( "OnlineResource" );
            writer.writeNamespace( XLINK_PREFIX, XLNNS );
            writer.writeAttribute( XLNNS, "type", "simple" );
            writer.writeAttribute( XLNNS, "href", mdUrl );
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private String getMetadataUrl( Layer layer ) {
        String mdUrl = null;
        if ( layer.getName() != null && metadata != null ) {
            DatasetMetadata dsMd = metadata.getDatasetMetadata( new QName( layer.getName() ) );
            if ( dsMd != null ) {
                mdUrl = dsMd.getUrl();
            }
        }

        if ( mdUrl == null && controller.getMetadataURLTemplate() != null ) {
            String id = layer.getDataMetadataSetId();
            if ( id == null ) {
                return null;
            }
            String mdurlTemplate = controller.getMetadataURLTemplate();
            if ( mdurlTemplate.isEmpty() ) {
                mdurlTemplate = getUrl;
                if ( !( mdurlTemplate.endsWith( "?" ) || mdurlTemplate.endsWith( "&" ) ) ) {
                    mdurlTemplate += "?";
                }
                mdurlTemplate += "service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full&id=${metadataSetId}";
            }

            mdUrl = StringUtils.replaceAll( mdurlTemplate, "${metadataSetId}", id );
        }
        return mdUrl;
    }

    private void writeStyles( XMLStreamWriter writer, Layer layer )
                            throws XMLStreamException {
        Style def = service.getStyles().get( layer.getName(), null );
        if ( def != null ) {
            if ( def.getName() != null && !def.getName().isEmpty() ) {
                capWriter.writeStyle( writer, "default", def.getName(), service.getLegendSize( def ), layer.getName(),
                                      def ); // TODO
                // title/description/whatever
            } else {
                capWriter.writeStyle( writer, "default", "default", service.getLegendSize( def ), layer.getName(), def ); // TODO
                // title/description/whatever
            }
        }
        HashSet<Style> visited = new HashSet<Style>();
        for ( Style s : service.getStyles().getAll( layer.getName() ) ) {
            if ( visited.contains( s ) ) {
                continue;
            }
            visited.add( s );
            String name = s.getName();
            if ( name != null && !name.isEmpty() ) {
                capWriter.writeStyle( writer, name, name, service.getLegendSize( s ), layer.getName(), s ); // TODO
                // title/description/whatever
            }
        }
    }

    private void writeScaleHints( XMLStreamWriter writer, Layer layer )
                            throws XMLStreamException {
        DoublePair hint = layer.getScaleHint();
        if ( hint.first != NEGATIVE_INFINITY || hint.second != POSITIVE_INFINITY ) {
            double fac = 0.00028;
            writer.writeStartElement( "ScaleHint" );
            writer.writeAttribute( "min",
                                   Double.toString( hint.first == NEGATIVE_INFINITY ? MIN_VALUE : hint.first * fac ) );
            writer.writeAttribute( "max",
                                   Double.toString( hint.second == POSITIVE_INFINITY ? MAX_VALUE : hint.second * fac ) );
            writer.writeEndElement();
        }
    }

}
