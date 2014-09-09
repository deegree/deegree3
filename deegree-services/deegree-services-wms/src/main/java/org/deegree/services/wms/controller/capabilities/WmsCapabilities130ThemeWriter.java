/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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

import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.services.wms.controller.capabilities.Capabilities130XMLAdapter.writeDimensions;
import static org.deegree.services.wms.controller.capabilities.WmsCapabilities130SpatialMetadataWriter.writeSrsAndEnvelope;
import static org.deegree.theme.Themes.getAllLayers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
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

/**
 * Writes WMS 1.3.0 Layer elements.
 * <p>
 * Data/Metadata is considered from the Theme/Layer tree as well as from the {@link OWSMetadataProvider}.
 * </p>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.3
 */
class WmsCapabilities130ThemeWriter {

    private final OWSMetadataProvider metadataProvider;

    private final Capabilities130XMLAdapter styleWriter;

    private final String mdUrlTemplate;

    /**
     * Creates a new {@link WmsCapabilities130ThemeWriter} instance.
     *
     * @param metadataProvider
     *            provider for metadata on OWS datasets, can be <code>null</code>
     * @param styleWriter
     *            writer for WMS 1.3.0 Style elements, can be <code>null</code> (styles will be skipped)
     * @param mdUrlTemplate
     *            URL template for requesting metadata records (<code>${metadataSetId}</code> will be replaced with
     *            metadata id), can be <code>null</code>
     */
    WmsCapabilities130ThemeWriter( final OWSMetadataProvider metadataProvider,
                                   final Capabilities130XMLAdapter styleWriter, final String mdUrlTemplate ) {
        this.metadataProvider = metadataProvider;
        this.styleWriter = styleWriter;
        this.mdUrlTemplate = mdUrlTemplate;
    }

    /**
     * Writes the given {@link Theme} as a WMS 1.3.0 Layer element.
     *
     * @param writer
     *            used to write the XML, must not be <code>null</code>
     * @param theme
     *            theme to be serialized, must not be <code>null</code>
     * @throws XMLStreamException
     */
    void writeTheme( final XMLStreamWriter writer, final Theme theme )
                            throws XMLStreamException {
        final ThemeMetadataMerger merger = new ThemeMetadataMerger();
        final LayerMetadata layerMetadata = merger.mergeLayerMetadata( theme );
        final DatasetMetadata providerMetadata = getDatasetMetadataFromProvider( theme );
        final DatasetMetadata datasetMetadata = merger.mergeDatasetMetadata( providerMetadata, theme, layerMetadata,
                                                                             mdUrlTemplate );
        final DoublePair scaleDenominators = merger.mergeScaleDenominators( theme );
        final Map<String, String> authorityNameToUrl = getExternalAuthorityNameToUrlMap( metadataProvider );
        writeTheme( writer, layerMetadata, datasetMetadata, authorityNameToUrl, scaleDenominators, theme.getThemes() );
    }

    private DatasetMetadata getDatasetMetadataFromProvider( final Theme theme ) {
        final String datasetName = getNameFromThemeOrFirstNamedLayer( theme );
        if ( metadataProvider != null && datasetName != null ) {
            return metadataProvider.getDatasetMetadata( new QName( datasetName ) );
        }
        return null;
    }

    private Map<String, String> getExternalAuthorityNameToUrlMap( final OWSMetadataProvider metadataProvider ) {
        if ( metadataProvider != null ) {
            return metadataProvider.getExternalMetadataAuthorities();
        }
        return null;
    }

    private String getNameFromThemeOrFirstNamedLayer( final Theme theme ) {
        if ( theme.getLayerMetadata().getName() != null ) {
            return theme.getLayerMetadata().getName();
        }
        for ( final Layer layer : getAllLayers( theme ) ) {
            if ( layer.getMetadata().getName() != null ) {
                return layer.getMetadata().getName();
            }
        }
        return null;
    }

    void writeTheme( final XMLStreamWriter writer, final LayerMetadata layerMetadata,
                     final DatasetMetadata datasetMetadata, final Map<String, String> authorityNameToUrl,
                     final DoublePair scaleDenominators, final List<Theme> subThemes )
                            throws XMLStreamException {
        writer.writeStartElement( WMSNS, "Layer" );
        // <attribute name="queryable" type="boolean" default="0"/>
        writeQueryable( writer, layerMetadata.isQueryable() && layerMetadata.getName() != null );
        // <attribute name="cascaded" type="nonNegativeInteger"/>
        writeCascaded( writer, layerMetadata.getCascaded() );
        // <element ref="wms:Name" minOccurs="0"/>
        writeName( writer, layerMetadata.getName() );
        // <element ref="wms:Title"/>
        writeTitle( writer, datasetMetadata.getTitles() );
        // <element ref="wms:Abstract" minOccurs="0"/>
        writeAbstract( writer, datasetMetadata.getAbstracts() );
        // <element ref="wms:KeywordList" minOccurs="0"/>
        writeKeywordList( writer, datasetMetadata.getKeywords() );
        // <element ref="wms:CRS" minOccurs="0" maxOccurs="unbounded"/>
        // <element ref="wms:EX_GeographicBoundingBox" minOccurs="0"/>
        // <element ref="wms:BoundingBox" minOccurs="0" maxOccurs="unbounded"/>
        writeCrsAndBoundingBoxes( writer, layerMetadata.getSpatialMetadata() );
        // <element ref="wms:Dimension" minOccurs="0" maxOccurs="unbounded"/>
        writeDimensions( writer, layerMetadata.getDimensions() );
        // <element ref="wms:AuthorityURL" minOccurs="0" maxOccurs="unbounded"/>
        writeAuthorityUrls( writer, datasetMetadata.getExternalUrls(), authorityNameToUrl );
        // <element ref="wms:Identifier" minOccurs="0" maxOccurs="unbounded"/>
        writeIdentifiers( writer, datasetMetadata.getExternalUrls() );
        // <element ref="wms:MetadataURL" minOccurs="0" maxOccurs="unbounded"/>
        writeMetadataUrl( writer, datasetMetadata.getUrl() );
        // <element ref="wms:Style" minOccurs="0" maxOccurs="unbounded"/>
        writeStyles( writer, layerMetadata.getName(), layerMetadata.getLegendStyles(), layerMetadata.getStyles() );
        // <element ref="wms:MinScaleDenominator" minOccurs="0"/>
        // <element ref="wms:MaxScaleDenominator" minOccurs="0"/>
        writeScaleDenominators( writer, scaleDenominators );
        // <element ref="wms:Layer" minOccurs="0" maxOccurs="unbounded"/>
        if ( subThemes != null ) {
            for ( final Theme subTheme : subThemes ) {
                writeTheme( writer, subTheme );
            }
        }
        writer.writeEndElement();
    }

    private void writeQueryable( final XMLStreamWriter writer, final boolean queryable )
                            throws XMLStreamException {
        if ( queryable ) {
            writer.writeAttribute( "queryable", "1" );
        }
    }

    private void writeCascaded( final XMLStreamWriter writer, final int cascaded )
                            throws XMLStreamException {
        if ( cascaded > 0 ) {
            writer.writeAttribute( "cascaded", cascaded + "" );
        }
    }

    private void writeName( final XMLStreamWriter writer, final String name )
                            throws XMLStreamException {
        if ( name != null ) {
            writeElement( writer, WMSNS, "Name", name );
        }
    }

    private void writeTitle( final XMLStreamWriter writer, final List<LanguageString> titles )
                            throws XMLStreamException {
        if ( titles != null && !titles.isEmpty() ) {
            writeElement( writer, WMSNS, "Title", titles.get( 0 ).getString() );
        }
    }

    private void writeAbstract( final XMLStreamWriter writer, final List<LanguageString> abstracts )
                            throws XMLStreamException {
        if ( abstracts != null && !abstracts.isEmpty() ) {
            writeElement( writer, WMSNS, "Abstract", abstracts.get( 0 ).getString() );
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

    private void writeCrsAndBoundingBoxes( final XMLStreamWriter writer, final SpatialMetadata smd )
                            throws XMLStreamException {
        if ( smd != null ) {
            writeSrsAndEnvelope( writer, smd.getCoordinateSystems(), smd.getEnvelope() );
        }
    }

    private void writeAuthorityUrls( final XMLStreamWriter writer, final List<StringPair> extUrls,
                                     final Map<String, String> authorityNameToUrl )
                            throws XMLStreamException {
        if ( extUrls != null && authorityNameToUrl != null ) {
            for ( final StringPair extUrl : extUrls ) {
                final String url = authorityNameToUrl.get( extUrl.first );
                if ( url != null ) {
                    writer.writeStartElement( WMSNS, "AuthorityURL" );
                    writer.writeAttribute( "name", extUrl.first );
                    writer.writeStartElement( WMSNS, "OnlineResource" );
                    writer.writeAttribute( XLNNS, "type", "simple" );
                    writer.writeAttribute( XLNNS, "href", url );
                    writer.writeEndElement();
                    writer.writeEndElement();
                }
            }
        }
    }

    private void writeIdentifiers( final XMLStreamWriter writer, final List<StringPair> extUrls )
                            throws XMLStreamException {
        for ( final StringPair extUrl : extUrls ) {
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

    private void writeStyles( final XMLStreamWriter writer, final String name, final Map<String, Style> legends,
                              final Map<String, Style> styles )
                            throws XMLStreamException {
        if ( styleWriter != null ) {
            for ( final Entry<String, Style> e : styles.entrySet() ) {
                if ( e.getKey() == null || e.getKey().isEmpty() ) {
                    continue;
                }
                Style ls = e.getValue();
                if ( legends.get( e.getKey() ) != null ) {
                    ls = legends.get( e.getKey() );
                }
                final Pair<Integer, Integer> p = new Legends().getLegendSize( ls );
                styleWriter.writeStyle( writer, e.getKey(), e.getKey(), p, name, e.getValue() );
            }
        }
    }

    private void writeScaleDenominators( final XMLStreamWriter writer, final DoublePair scaleDenominators )
                            throws XMLStreamException {
        final Double min = scaleDenominators.first;
        if ( !min.isInfinite() ) {
            writeElement( writer, WMSNS, "MinScaleDenominator", min + "" );
        }
        final Double max = scaleDenominators.second;
        if ( !max.isInfinite() ) {
            writeElement( writer, WMSNS, "MaxScaleDenominator", max + "" );
        }
    }

}
