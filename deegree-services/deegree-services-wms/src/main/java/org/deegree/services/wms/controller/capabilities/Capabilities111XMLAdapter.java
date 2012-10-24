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

package org.deegree.services.wms.controller.capabilities;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.layer.dims.Dimension.formatDimensionValueList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.rendering.r2d.legends.Legends;
import org.deegree.services.jaxb.wms.LanguageStringType;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;
import org.slf4j.Logger;

/**
 * <code>Capabilities111XMLAdapter</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(warn = "logs problems with CRS when outputting 1.1.1 capabilities", trace = "logs stack traces")
public class Capabilities111XMLAdapter extends XMLAdapter {

    private static final Logger LOG = getLogger( Capabilities111XMLAdapter.class );

    private final String getUrl;

    private MapService service;

    private WMSController controller;

    private final OWSMetadataProvider metadata;

    private WmsCapabilities111MetadataWriter metadataWriter;

    /**
     * @param identification
     * @param provider
     * @param getUrl
     * @param postUrl
     * @param service
     * @param controller
     */
    public Capabilities111XMLAdapter( ServiceIdentification identification, ServiceProvider provider,
                                      OWSMetadataProvider metadata, String getUrl, String postUrl, MapService service,
                                      WMSController controller ) {
        this.metadata = metadata;
        this.getUrl = getUrl;
        this.service = service;
        this.controller = controller;
        metadataWriter = new WmsCapabilities111MetadataWriter( identification, provider, getUrl, postUrl, controller );
    }

    /**
     * Writes out a 1.1.1 style capabilities document.
     * 
     * @param writer
     * @throws XMLStreamException
     */
    public void export( XMLStreamWriter writer )
                            throws XMLStreamException {

        String dtdrequest = getUrl + "?request=DTD";

        writer.writeDTD( "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"" + dtdrequest
                         + "\" [<!ELEMENT VendorSpecificCapabilities EMPTY>]>\n" );
        writer.writeStartElement( "WMT_MS_Capabilities" );
        writer.writeAttribute( "version", "1.1.1" );
        writer.writeAttribute( "updateSequence", "" + service.updateSequence );

        metadataWriter.writeService( writer );

        writeCapability( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void writeCapability( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( "Capability" );

        metadataWriter.writeRequest( writer );
        writer.writeStartElement( "Exception" );
        writeElement( writer, "Format", "application/vnd.ogc.se_xml" );
        writeElement( writer, "Format", "application/vnd.ogc.se_inimage" );
        writeElement( writer, "Format", "application/vnd.ogc.se_blank" );
        writer.writeEndElement();

        if ( service.isNewStyle() ) {
            writeThemes( writer, service.getThemes() );
        } else {
            writeLayers( writer, service.getRootLayer() );
        }

        writer.writeEndElement();
    }

    private void writeTheme( XMLStreamWriter writer, Theme theme )
                            throws XMLStreamException {
        writer.writeStartElement( "Layer" );
        LayerMetadata md = theme.getMetadata();
        // TODO think about a push approach instead of a pull approach
        LayerMetadata lmd = null;
        for ( org.deegree.layer.Layer l : Themes.getAllLayers( theme ) ) {
            if ( lmd == null ) {
                lmd = l.getMetadata();
            } else {
                lmd.merge( l.getMetadata() );
            }
        }
        md.merge( lmd );

        if ( md.isQueryable() ) {
            writer.writeAttribute( "queryable", "1" );
        }
        if ( md.getCascaded() != 0 ) {
            writer.writeAttribute( "cascaded", md.getCascaded() + "" );
        }
        if ( md.getName() != null ) {
            writeElement( writer, "Name", md.getName() );
        }
        writeElement( writer, "Title", md.getDescription().getTitles().get( 0 ).getString() );
        List<LanguageString> abs = md.getDescription().getAbstracts();
        if ( lmd != null && ( abs == null || abs.isEmpty() ) ) {
            abs = lmd.getDescription().getAbstracts();
        }
        if ( abs != null && !abs.isEmpty() ) {
            writeElement( writer, "Abstract", abs.get( 0 ).getString() );
        }
        List<Pair<List<LanguageString>, CodeType>> kws = md.getDescription().getKeywords();
        if ( lmd != null && ( kws == null || kws.isEmpty() || kws.get( 0 ).first.isEmpty() ) ) {
            kws = lmd.getDescription().getKeywords();
        }
        if ( kws != null && !kws.isEmpty() && !kws.get( 0 ).first.isEmpty() ) {
            writer.writeStartElement( "KeywordList" );
            for ( LanguageString ls : kws.get( 0 ).first ) {
                writeElement( writer, "Keyword", ls.getString() );
            }
            writer.writeEndElement();
        }

        SpatialMetadata smd = md.getSpatialMetadata();
        writeSrsAndEnvelope( writer, smd.getCoordinateSystems(), smd.getEnvelope() );
        writeDimensions( writer, md.getDimensions() );

        mdlabel: if ( controller.getMetadataURLTemplate() != null ) {
            String id = null;

            inner: for ( org.deegree.layer.Layer l : theme.getLayers() ) {
                if ( l.getMetadata().getMetadataId() != null ) {
                    id = l.getMetadata().getMetadataId();
                    break inner;
                }
            }

            if ( id == null ) {
                break mdlabel;
            }
            String mdurlTemplate = controller.getMetadataURLTemplate();
            if ( mdurlTemplate.isEmpty() ) {
                mdurlTemplate = getUrl;
                if ( !( mdurlTemplate.endsWith( "?" ) || mdurlTemplate.endsWith( "&" ) ) ) {
                    mdurlTemplate += "?";
                }
                mdurlTemplate += "service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http%3A//www.isotc211.org/2005/gmd&elementSetName=full&id=${metadataSetId}";
            }

            writer.writeStartElement( "MetadataURL" );
            writer.writeAttribute( "type", "ISO19115:2003" );
            writeElement( writer, "Format", "application/xml" );
            writer.writeStartElement( "OnlineResource" );
            writer.writeNamespace( XLINK_PREFIX, XLNNS );
            writer.writeAttribute( XLNNS, "type", "simple" );
            writer.writeAttribute( XLNNS, "href", StringUtils.replaceAll( mdurlTemplate, "${metadataSetId}", id ) );
            writer.writeEndElement();
            writer.writeEndElement();
        }

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
            writeStyle( writer, e.getKey(), e.getKey(), p, md.getName(), e.getValue() );
        }

        DoublePair hint = md.getScaleDenominators();
        // use layers' settings only if not set for theme
        if ( hint.first.isInfinite() && hint.second.isInfinite() ) {
            hint = new DoublePair( hint.second, hint.first );
            for ( org.deegree.layer.Layer l : theme.getLayers() ) {
                hint.first = Math.min( l.getMetadata().getScaleDenominators().first, hint.first );
                hint.second = Math.max( l.getMetadata().getScaleDenominators().second, hint.second );
            }
        }
        if ( !hint.first.isInfinite() || !hint.second.isInfinite() ) {
            double fac = 0.00028;
            writer.writeStartElement( "ScaleHint" );
            writer.writeAttribute( "min", Double.toString( hint.first.isInfinite() ? MIN_VALUE : hint.first * fac ) );
            writer.writeAttribute( "max", Double.toString( hint.second.isInfinite() ? MAX_VALUE : hint.second * fac ) );
            writer.writeEndElement();
        }

        for ( Theme t : theme.getThemes() ) {
            writeTheme( writer, t );
        }
        writer.writeEndElement();
    }

    private void writeThemes( XMLStreamWriter writer, List<Theme> themes )
                            throws XMLStreamException {
        if ( themes.size() == 1 ) {
            writeTheme( writer, themes.get( 0 ) );
        } else {
            // synthetic root layer needed
            writer.writeStartElement( "Layer" );
            // TODO
            writer.writeAttribute( "queryable", "1" );
            writeElement( writer, "Title", "Root" );
            for ( Theme t : themes ) {
                writeTheme( writer, t );
            }
            writer.writeEndElement();
        }
    }

    private static void writeSrsAndEnvelope( XMLStreamWriter writer, List<ICRS> srs, Envelope layerEnv )
                            throws XMLStreamException {
        for ( ICRS crs : srs ) {
            writeElement( writer, "SRS", crs.getAlias() );
        }

        ICRS latlon;
        try {
            latlon = CRSManager.lookup( "CRS:84" );
            if ( layerEnv != null && layerEnv.getCoordinateDimension() >= 2 ) {
                Envelope bbox = new GeometryTransformer( latlon ).transform( layerEnv );
                writer.writeStartElement( "LatLonBoundingBox" );
                writer.writeAttribute( "minx", Double.toString( bbox.getMin().get0() ) );
                writer.writeAttribute( "miny", Double.toString( bbox.getMin().get1() ) );
                writer.writeAttribute( "maxx", Double.toString( bbox.getMax().get0() ) );
                writer.writeAttribute( "maxy", Double.toString( bbox.getMax().get1() ) );
                writer.writeEndElement();

                for ( ICRS crs : srs ) {
                    if ( crs.getAlias().startsWith( "AUTO" ) ) {
                        continue;
                    }
                    // try {
                    // crs
                    // } catch ( UnknownCRSException e ) {
                    // LOG.warn( "Cannot find: {}", e.getLocalizedMessage() );
                    // LOG.trace( "Stack trace:", e );
                    // continue;
                    // }
                    Envelope envelope;
                    try {
                        if ( layerEnv.getCoordinateSystem() == null ) {
                            envelope = new GeometryTransformer( crs ).transform( layerEnv, latlon );
                        } else {
                            envelope = new GeometryTransformer( crs ).transform( layerEnv );
                        }
                    } catch ( Throwable e ) {
                        LOG.warn( "Cannot transform: {}", e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                        continue;
                    }

                    writer.writeStartElement( "BoundingBox" );
                    writer.writeAttribute( "SRS", crs.getAlias() );
                    writer.writeAttribute( "minx", Double.toString( envelope.getMin().get0() ) );
                    writer.writeAttribute( "miny", Double.toString( envelope.getMin().get1() ) );
                    writer.writeAttribute( "maxx", Double.toString( envelope.getMax().get0() ) );
                    writer.writeAttribute( "maxy", Double.toString( envelope.getMax().get1() ) );
                    writer.writeEndElement();
                }
            }
        } catch ( Throwable e ) {
            LOG.warn( "Cannot transform: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
    }

    private static void writeDimensions( XMLStreamWriter writer, Map<String, Dimension<?>> dims )
                            throws XMLStreamException {
        for ( Entry<String, Dimension<?>> entry : dims.entrySet() ) {
            Dimension<?> dim = entry.getValue();
            writer.writeStartElement( "Dimension" );
            writer.writeAttribute( "name", entry.getKey() );
            writer.writeAttribute( "units", dim.getUnits() == null ? "EPSG:4979" : dim.getUnits() );
            writer.writeAttribute( "unitSymbol", dim.getUnitSymbol() == null ? "" : dim.getUnitSymbol() );
            writer.writeEndElement();
        }

        for ( Entry<String, Dimension<?>> entry : dims.entrySet() ) {
            String name = entry.getKey();
            Dimension<?> dim = entry.getValue();
            writer.writeStartElement( "Extent" );
            writer.writeAttribute( "name", name );
            if ( dim.getDefaultValue() != null ) {
                writer.writeAttribute( "default",
                                       formatDimensionValueList( dim.getDefaultValue(), "time".equals( name ) ) );
            }
            if ( dim.getNearestValue() ) {
                writer.writeAttribute( "nearestValue", "1" );
            }
            writer.writeCharacters( dim.getExtentAsString() );
            writer.writeEndElement();
        }
    }

    @Deprecated
    private void writeLayers( XMLStreamWriter writer, Layer layer )
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

        if ( layer.getAuthorityURL() != null ) {
            writer.writeStartElement( WMSNS, "AuthorityURL" );
            writer.writeAttribute( "name", "fromISORecord" );
            writer.writeStartElement( WMSNS, "OnlineResource" );
            writer.writeNamespace( XLINK_PREFIX, XLNNS );
            writer.writeAttribute( XLNNS, "href", layer.getAuthorityURL() );
            writer.writeEndElement();
            writer.writeEndElement();
        }

        if ( layer.getAuthorityIdentifier() != null ) {
            writer.writeStartElement( WMSNS, "Identifier" );
            writer.writeAttribute( "authority", "fromISORecord" );
            writer.writeCharacters( layer.getAuthorityIdentifier() );
            writer.writeEndElement();
        }

        String mdUrl = null;
        if ( layer.getName() != null && metadata != null ) {
            DatasetMetadata dsMd = metadata.getDatasetMetadata( new QName( layer.getName() ) );
            if ( dsMd != null ) {
                mdUrl = dsMd.getUrl();
            }
        }

        mdlabel: if ( mdUrl == null && controller.getMetadataURLTemplate() != null ) {
            String id = layer.getDataMetadataSetId();
            if ( id == null ) {
                break mdlabel;
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

        Style def = service.getStyles().get( layer.getName(), null );
        if ( def != null ) {
            if ( def.getName() != null && !def.getName().isEmpty() ) {
                writeStyle( writer, "default", def.getName(), service.getLegendSize( def ), layer.getName(), def ); // TODO
                // title/description/whatever
            } else {
                writeStyle( writer, "default", "default", service.getLegendSize( def ), layer.getName(), def ); // TODO
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
                writeStyle( writer, name, name, service.getLegendSize( s ), layer.getName(), s ); // TODO
                // title/description/whatever
            }
        }

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

        for ( Layer l : new LinkedList<Layer>( layer.getChildren() ) ) {
            writeLayers( writer, l );
        }

        writer.writeEndElement();
    }

    private void writeStyle( XMLStreamWriter writer, String name, String title, Pair<Integer, Integer> legendSize,
                             String layerName, Style style )
                            throws XMLStreamException {
        writer.writeStartElement( "Style" );
        writeElement( writer, "Name", name );
        writeElement( writer, "Title", title );
        if ( legendSize.first > 0 && legendSize.second > 0 ) {
            writer.writeStartElement( "LegendURL" );
            writer.writeAttribute( "width", "" + legendSize.first );
            writer.writeAttribute( "height", "" + legendSize.second );
            writeElement( writer, "Format", "image/png" );
            writer.writeStartElement( "OnlineResource" );
            writer.writeNamespace( XLINK_PREFIX, XLNNS );
            writer.writeAttribute( XLNNS, "type", "simple" );
            if ( style.getLegendURL() == null || style.prefersGetLegendGraphicUrl() ) {
                String styleName = style.getName() == null ? "" : ( "&style=" + style.getName() );
                writer.writeAttribute( XLNNS, "href", getUrl
                                                      + "?request=GetLegendGraphic&version=1.1.1&service=WMS&layer="
                                                      + layerName + styleName + "&format=image/png" );
            } else {
                writer.writeAttribute( XLNNS, "href", style.getLegendURL().toExternalForm() );
            }
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

}
