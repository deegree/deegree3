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

import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.layer.dims.Dimension.formatDimensionValueList;
import static org.deegree.services.wms.controller.capabilities.WmsCapabilities111SpatialMetadataWriter.writeSrsAndEnvelope;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;

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

    // private static final Logger LOG = getLogger( Capabilities111XMLAdapter.class );

    private final String getUrl;

    private MapService service;

    private WMSController controller;

    private final OWSMetadataProvider metadata;

    private WmsCapabilities111MetadataWriter metadataWriter;

    private WmsCapabilities111ThemeWriter themeWriter;

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
        themeWriter = new WmsCapabilities111ThemeWriter( controller, getUrl, this, metadata );
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
            WmsCapabilities111LegacyWriter lw = new WmsCapabilities111LegacyWriter( service, getUrl, metadata,
                                                                                    controller, this );
            lw.writeLayers( writer, service.getRootLayer() );
        }

        writer.writeEndElement();
    }

    private void writeThemes( XMLStreamWriter writer, List<Theme> themes )
                            throws XMLStreamException {
        if ( themes.size() == 1 ) {
            themeWriter.writeTheme( writer, themes.get( 0 ) );
        } else {
            // synthetic root layer needed
            writer.writeStartElement( "Layer" );
            writeElement( writer, "Title", "Root" );

            // TODO think about a push approach instead of a pull approach
            LayerMetadata lmd = null;
            for ( Theme t : themes ) {
                for ( org.deegree.layer.Layer l : Themes.getAllLayers( t ) ) {
                    if ( lmd == null ) {
                        lmd = l.getMetadata();
                    } else {
                        lmd.merge( l.getMetadata() );
                    }
                }
            }
            if ( lmd != null ) {
                SpatialMetadata smd = lmd.getSpatialMetadata();
                writeSrsAndEnvelope( writer, smd.getCoordinateSystems(), smd.getEnvelope() );
            }

            for ( Theme t : themes ) {
                themeWriter.writeTheme( writer, t );
            }
            writer.writeEndElement();
        }
    }

    static void writeDimensions( XMLStreamWriter writer, Map<String, Dimension<?>> dims )
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

    void writeStyle( XMLStreamWriter writer, String name, String title, Pair<Integer, Integer> legendSize,
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
