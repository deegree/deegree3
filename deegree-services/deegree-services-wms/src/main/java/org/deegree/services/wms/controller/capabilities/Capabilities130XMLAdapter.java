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

import static org.deegree.commons.xml.CommonNamespaces.SLDNS;
import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.layer.dims.Dimension.formatDimensionValueList;
import static org.deegree.services.wms.controller.capabilities.WmsCapabilities130SpatialMetadataWriter.writeSrsAndEnvelope;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;
import org.slf4j.Logger;

/**
 * <code>Capabilities130XMLAdapter</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(warn = "logs problems with CRS when outputting 1.3.0 capabilities", trace = "logs stack traces")
public class Capabilities130XMLAdapter {

    private static final Logger LOG = getLogger( Capabilities130XMLAdapter.class );

    private final String getUrl;

    private MapService service;

    private final WMSController controller;

    private OWSMetadataProvider metadata;

    private WmsCapabilities130MetadataWriter metadataWriter;

    private WmsCapabilities130ThemeWriter themeWriter;

    /**
     * @param identification
     * @param provider
     * @param getUrl
     * @param postUrl
     * @param service
     * @param controller
     */
    public Capabilities130XMLAdapter( ServiceIdentification identification, ServiceProvider provider,
                                      OWSMetadataProvider metadata, String getUrl, String postUrl, MapService service,
                                      WMSController controller ) {
        this.metadata = metadata;
        this.getUrl = getUrl;
        this.service = service;
        this.controller = controller;
        metadataWriter = new WmsCapabilities130MetadataWriter( identification, provider, getUrl, postUrl, controller );
        themeWriter = new WmsCapabilities130ThemeWriter( controller, this, getUrl, metadata );
    }

    /**
     * Writes out a 1.3.0 style capabilities document.
     * 
     * @param writer
     * @throws XMLStreamException
     */
    public void export( XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.setDefaultNamespace( WMSNS );
        writer.writeStartElement( WMSNS, "WMS_Capabilities" );
        writer.writeAttribute( "version", "1.3.0" );
        writer.writeAttribute( "updateSequence", "" + service.updateSequence );
        writer.writeDefaultNamespace( WMSNS );
        writer.writeNamespace( "xsi", XSINS );
        writer.writeNamespace( "xlink", XLNNS );
        writer.writeNamespace( "sld", SLDNS );

        writer.writeAttribute( XSINS,
                               "schemaLocation",
                               "http://www.opengis.net/wms http://schemas.opengis.net/wms/1.3.0/capabilities_1_3_0.xsd "
                                                       + "http://www.opengis.net/sld http://schemas.opengis.net/sld/1.1.0/sld_capabilities.xsd" );

        metadataWriter.writeService( writer );

        writeCapability( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void writeExtendedCapabilities( XMLStreamWriter writer ) {
        List<OMElement> caps = controller.getExtendedCapabilities( "1.3.0" );
        if ( caps != null ) {
            for ( OMElement c : caps ) {
                try {
                    XMLStreamReader reader = c.getXMLStreamReader();
                    XMLStreamUtils.skipStartDocument( reader );
                    writeElement( writer, reader );
                } catch ( Throwable e ) {
                    LOG.warn( "Could not export extended capabilities snippet" );
                    LOG.trace( "Stack trace", e );
                }
            }
        }
    }

    private void writeCapability( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( WMSNS, "Capability" );

        metadataWriter.writeRequest( writer );
        writer.writeStartElement( WMSNS, "Exception" );
        writeElement( writer, "Format", "XML" );
        writeElement( writer, "Format", "INIMAGE" );
        writeElement( writer, "Format", "BLANK" );
        writer.writeEndElement();

        writeExtendedCapabilities( writer );

        if ( service.isNewStyle() ) {
            writeThemes( writer, service.getThemes() );
        } else {
            WmsCapabilities130LegacyWriter lw = new WmsCapabilities130LegacyWriter( service, getUrl, metadata,
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
            writer.writeStartElement( WMSNS, "Layer" );
            writeElement( writer, WMSNS, "Title", "Root" );

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
            writer.writeStartElement( WMSNS, "Dimension" );
            Dimension<?> dim = entry.getValue();
            writer.writeAttribute( "name", entry.getKey() );
            writer.writeAttribute( "units", dim.getUnits() == null ? "CRS:88" : dim.getUnits() );
            writer.writeAttribute( "unitSymbol", dim.getUnitSymbol() == null ? "" : dim.getUnitSymbol() );
            if ( dim.getDefaultValue() != null ) {
                writer.writeAttribute( "default",
                                       formatDimensionValueList( dim.getDefaultValue(), "time".equals( entry.getKey() ) ) );
            }
            if ( dim.getNearestValue() ) {
                writer.writeAttribute( "nearestValue", "1" );
            }
            if ( dim.getMultipleValues() ) {
                writer.writeAttribute( "multipleValues", "1" );
            }
            if ( dim.getCurrent() ) {
                writer.writeAttribute( "current", "1" );
            }
            writer.writeCharacters( dim.getExtentAsString() );
            writer.writeEndElement();
        }
    }

    void writeStyle( XMLStreamWriter writer, String name, String title, Pair<Integer, Integer> legendSize,
                     String layerName, Style style )
                            throws XMLStreamException {
        writer.writeStartElement( WMSNS, "Style" );
        writeElement( writer, WMSNS, "Name", name );
        writeElement( writer, WMSNS, "Title", title );
        if ( legendSize.first > 0 && legendSize.second > 0 ) {
            writer.writeStartElement( WMSNS, "LegendURL" );
            writer.writeAttribute( "width", "" + legendSize.first );
            writer.writeAttribute( "height", "" + legendSize.second );
            writeElement( writer, WMSNS, "Format", "image/png" );
            writer.writeStartElement( WMSNS, "OnlineResource" );
            writer.writeAttribute( XLNNS, "type", "simple" );
            if ( style.getLegendURL() == null || style.prefersGetLegendGraphicUrl() ) {
                String styleName = style.getName() == null ? "" : ( "&style=" + style.getName() );
                writer.writeAttribute( XLNNS, "href", getUrl
                                                      + "?request=GetLegendGraphic&version=1.3.0&service=WMS&layer="
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
