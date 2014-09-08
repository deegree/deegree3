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

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.services.wms.controller.capabilities.Capabilities130XMLAdapter.writeDimensions;
import static org.deegree.services.wms.controller.capabilities.WmsCapabilities130SpatialMetadataWriter.writeSrsAndEnvelope;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringPair;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.rendering.r2d.legends.Legends;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;

/**
 * Responsible for writing out themes capabilities.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 *
 * @version $Revision: $, $Date: $
 */
class WmsCapabilities130ThemeWriter {

    private final Capabilities130XMLAdapter capWriter;

    private final String mdUrlTemplate;

    private final OWSMetadataProvider metadata;

    WmsCapabilities130ThemeWriter( final Capabilities130XMLAdapter capWriter, final OWSMetadataProvider metadata,
                                   final String mdUrlTemplate ) {
        this.capWriter = capWriter;
        this.metadata = metadata;
        this.mdUrlTemplate = mdUrlTemplate;
    }

    void writeTheme( final XMLStreamWriter writer, final Theme theme )
                            throws XMLStreamException {
        final LayerMetadataMerger metadataMerger = new LayerMetadataMerger( metadata, mdUrlTemplate );
        final LayerMetadata layerTreeMetadata = metadataMerger.getLayerTreeMetadata( theme );
        final DatasetMetadata md = metadataMerger.getDatasetMetadata( theme, layerTreeMetadata );
        writer.writeStartElement( WMSNS, "Layer" );
        // <attribute name="queryable" type="boolean" default="0"/>
        writeQueryable( writer, theme );
        // <attribute name="cascaded" type="nonNegativeInteger"/>
        writeCascaded( writer, theme.getLayerMetadata().getCascaded() );
        // <element ref="wms:Name" minOccurs="0"/>
        writeName( writer, theme );
        // <element ref="wms:Title"/>
        writeElement( writer, WMSNS, "Title", md.getTitle( null ).getString() );
        // <element ref="wms:Abstract" minOccurs="0"/>
        writeAbstract( writer, md );
        // <element ref="wms:KeywordList" minOccurs="0"/>
        writeKeywordList( writer, md.getKeywords() );
        // <element ref="wms:CRS" minOccurs="0" maxOccurs="unbounded"/>
        // <element ref="wms:EX_GeographicBoundingBox" minOccurs="0"/>
        // <element ref="wms:BoundingBox" minOccurs="0" maxOccurs="unbounded"/>
        writeCrsAndBoundingBoxes( writer, layerTreeMetadata );
        // <element ref="wms:Dimension" minOccurs="0" maxOccurs="unbounded"/>
        writeDimensions( writer, layerTreeMetadata.getDimensions() );
        // <element ref="wms:AuthorityURL" minOccurs="0" maxOccurs="unbounded"/>
        writeAuthorityUrls( writer, md );
        // <element ref="wms:Identifier" minOccurs="0" maxOccurs="unbounded"/>
        writeIdentifiers( writer, md );
        // <element ref="wms:MetadataURL" minOccurs="0" maxOccurs="unbounded"/>
        writeMetadataUrl( writer, md.getUrl() );
        // <element ref="wms:Style" minOccurs="0" maxOccurs="unbounded"/>
        writeStyles( writer, theme.getLayerMetadata() );
        // <element ref="wms:MinScaleDenominator" minOccurs="0"/>
        // <element ref="wms:MaxScaleDenominator" minOccurs="0"/>
        writeScaleDenominators( writer, theme );
        // <element ref="wms:Layer" minOccurs="0" maxOccurs="unbounded"/>
        for ( final Theme subTheme : theme.getThemes() ) {
            writeTheme( writer, subTheme );
        }
        writer.writeEndElement();
    }

    private void writeQueryable( final XMLStreamWriter writer, final Theme theme )
                            throws XMLStreamException {
        if ( theme.getLayerMetadata().isQueryable() && theme.getLayerMetadata().getName() != null ) {
            writer.writeAttribute( "queryable", "1" );
        }
    }

    private void writeCascaded( final XMLStreamWriter writer, final int cascaded )
                            throws XMLStreamException {
        if ( cascaded > 0 ) {
            writer.writeAttribute( "cascaded", cascaded + "" );
        }
    }

    private void writeName( final XMLStreamWriter writer, final Theme theme )
                            throws XMLStreamException {
        if ( theme.getLayerMetadata().getName() != null ) {
            writeElement( writer, WMSNS, "Name", theme.getLayerMetadata().getName() );
        }
    }

    private void writeAbstract( final XMLStreamWriter writer, final DatasetMetadata md )
                            throws XMLStreamException {
        final LanguageString abstractString = md.getAbstract( null );
        if ( abstractString != null ) {
            writeElement( writer, WMSNS, "Abstract", abstractString.getString() );
        }
    }

    private void writeKeywordList( final XMLStreamWriter writer,
                                   final List<Pair<List<LanguageString>, CodeType>> keywordList )
                            throws XMLStreamException {
        if ( keywordList != null && !keywordList.isEmpty() ) {
            writer.writeStartElement( WMSNS, "KeywordList" );
            // <element ref="wms:Keyword" minOccurs="0" maxOccurs="unbounded"/>
            for ( final Pair<List<LanguageString>, CodeType> kws : keywordList ) {
                String vocabulary = null;
                if ( kws.second != null ) {
                    vocabulary = kws.second.getCodeSpace();
                }
                for ( final LanguageString ls : kws.first ) {
                    writeElement( writer, WMSNS, "Keyword", ls.getString(), null, null, "vocabulary", vocabulary );
                }
            }
            writer.writeEndElement();
        }
    }

    private void writeCrsAndBoundingBoxes( final XMLStreamWriter writer, final LayerMetadata md )
                            throws XMLStreamException {
        final SpatialMetadata smd = md.getSpatialMetadata();
        if ( smd != null ) {
            writeSrsAndEnvelope( writer, smd.getCoordinateSystems(), smd.getEnvelope() );
        }
    }

    private void writeAuthorityUrls( final XMLStreamWriter writer, final DatasetMetadata md )
                            throws XMLStreamException {
        final Map<String, String> auths = metadata.getExternalMetadataAuthorities();
        for ( final StringPair extUrls : md.getExternalUrls() ) {
            final String url = auths.get( extUrls.first );
            writer.writeStartElement( WMSNS, "AuthorityURL" );
            writer.writeAttribute( "name", extUrls.first );
            writer.writeStartElement( WMSNS, "OnlineResource" );
            writer.writeAttribute( XLNNS, "type", "simple" );
            writer.writeAttribute( XLNNS, "href", url );
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private void writeIdentifiers( final XMLStreamWriter writer, final DatasetMetadata md )
                            throws XMLStreamException {
        for ( final StringPair extUrl : md.getExternalUrls() ) {
            writer.writeStartElement( WMSNS, "Identifier" );
            writer.writeAttribute( "authority", extUrl.first );
            writer.writeCharacters( extUrl.second );
            writer.writeEndElement();
        }
    }

    private void writeMetadataUrl( final XMLStreamWriter writer, final String url )
                            throws XMLStreamException {
        if ( url == null ) {
            return;
        }
        writer.writeStartElement( WMSNS, "MetadataURL" );
        writer.writeAttribute( "type", "ISO19115:2003" );
        writeElement( writer, WMSNS, "Format", "application/xml" );
        writer.writeStartElement( WMSNS, "OnlineResource" );
        writer.writeAttribute( XLNNS, "type", "simple" );
        writer.writeAttribute( XLNNS, "href", url );
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeStyles( final XMLStreamWriter writer, final LayerMetadata md )
                            throws XMLStreamException {
        Map<String, Style> legends = md.getLegendStyles();
        for ( Entry<String, Style> e : md.getStyles().entrySet() ) {
            if ( e.getKey() == null || e.getKey().isEmpty() ) {
                continue;
            }
            Style ls = e.getValue();
            if ( legends.get( e.getKey() ) != null ) {
                ls = legends.get( e.getKey() );
            }
            Pair<Integer, Integer> p = new Legends().getLegendSize( ls );
            capWriter.writeStyle( writer, e.getKey(), e.getKey(), p, md.getName(), e.getValue() );
        }
    }

    private void writeScaleDenominators( final XMLStreamWriter writer, final Theme theme )
                            throws XMLStreamException {
        Double min = POSITIVE_INFINITY;
        Double max = NEGATIVE_INFINITY;
        if ( theme.getMetadata() != null && theme.getLayerMetadata().getScaleDenominators() != null ) {
            final DoublePair themeScales = theme.getLayerMetadata().getScaleDenominators();
            if ( !themeScales.first.isInfinite() ) {
                min = themeScales.first;
            }
            if ( !themeScales.second.isInfinite() ) {
                max = themeScales.second;
            }
        }
        final List<Layer> layers = Themes.getAllLayers( theme );
        if ( layers != null ) {
            for ( final Layer layer : layers ) {
                if ( layer.getMetadata() != null ) {
                    final DoublePair layerScales = layer.getMetadata().getScaleDenominators();
                    if ( layerScales != null ) {
                        min = Math.min( min, layerScales.first );
                        max = Math.max( max, layerScales.second );
                    }
                }
            }
        }
        if ( !min.isInfinite() ) {
            writeElement( writer, WMSNS, "MinScaleDenominator", min + "" );
        }
        if ( !max.isInfinite() ) {
            writeElement( writer, WMSNS, "MaxScaleDenominator", max + "" );
        }
    }

}
