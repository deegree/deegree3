//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wmts.controller.capabilities;

import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.geometry.Envelope;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.tile.TileLayer;
import org.deegree.protocol.ows.metadata.Description;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.protocol.ows.metadata.domain.Domain;
import org.deegree.protocol.ows.metadata.operation.DCP;
import org.deegree.protocol.ows.metadata.operation.Operation;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixMetadata;
import org.deegree.tile.TileMatrixSetMetadata;
import org.deegree.tile.persistence.TileStore;

/**
 * <code>WMTSCapabilitiesWriter</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class WMTSCapabilitiesWriter extends OWSCapabilitiesXMLAdapter {

    private static final String WMTSNS = "http://www.opengis.net/wmts/1.0";

    private static final String XSINS = "http://www.w3.org/2001/XMLSchema-instance";

    public static void export100( XMLStreamWriter writer, ServiceIdentification identification,
                                  ServiceProvider provider, List<Theme> themes )
                            throws XMLStreamException {

        writer.setDefaultNamespace( WMTSNS );
        writer.writeStartElement( WMTSNS, "Capabilities" );
        writer.setPrefix( OWS_PREFIX, OWS110_NS );
        writer.writeDefaultNamespace( WMTSNS );
        writer.writeNamespace( OWS_PREFIX, OWS110_NS );
        writer.writeNamespace( "xlink", XLN_NS );
        writer.writeNamespace( "xsi", XSINS );
        writer.writeAttribute( "service", "WPS" );
        writer.writeAttribute( "version", "1.0.0" );
        writer.writeAttribute( XSINS, "schemaLocation",
                               "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsGetCapabilities_response.xsd" );

        exportServiceIdentification( writer, identification );
        exportServiceProvider110New( writer, provider );
        exportOperationsMetadata( writer );

        exportContents( writer, themes );

        exportThemes( writer, themes );

        writer.writeEndElement(); // Capabilities
    }

    private static void exportServiceIdentification( XMLStreamWriter writer, ServiceIdentification ident )
                            throws XMLStreamException {
        writer.writeStartElement( OWS110_NS, "ServiceIdentification" );
        if ( ident == null ) {
            writeElement( writer, OWS110_NS, "Title", "deegree 3 WMTS" );
            writeElement( writer, OWS110_NS, "Abstract", "deegree 3 WMTS implementation" );
        } else {
            LanguageString title = ident.getTitle( null );
            writeElement( writer, OWS110_NS, "Title", title == null ? "deegree 3 WMTS" : title.getString() );
            LanguageString _abstract = ident.getAbstract( null );
            writeElement( writer, OWS110_NS, "Abstract", _abstract == null ? "deegree 3 WMTS implementation"
                                                                          : _abstract.getString() );
        }
        writeElement( writer, OWS110_NS, "ServiceType", "WMTS" );
        writeElement( writer, OWS110_NS, "ServiceTypeVersion", "1.0.0" );
        writer.writeEndElement();
    }

    private static void exportOperationsMetadata( XMLStreamWriter writer )
                            throws XMLStreamException {

        List<Operation> operations = new LinkedList<Operation>();

        List<DCP> dcps = null;
        try {
            DCP dcp = new DCP( new URL( OGCFrontController.getHttpGetURL() ), null );
            dcps = Collections.singletonList( dcp );
        } catch ( MalformedURLException e ) {
            // should never happen
        }

        List<Domain> params = new ArrayList<Domain>();
        List<Domain> constraints = new ArrayList<Domain>();
        List<OMElement> mdEls = new ArrayList<OMElement>();

        operations.add( new Operation( "GetCapabilities", dcps, params, constraints, mdEls ) );
        operations.add( new Operation( "GetTile", dcps, params, constraints, mdEls ) );

        OperationsMetadata operationsMd = new OperationsMetadata( operations, params, constraints, null );

        exportOperationsMetadata110( writer, operationsMd );
    }

    private static void exportThemes( XMLStreamWriter writer, List<Theme> themes )
                            throws XMLStreamException {
        if ( themes.isEmpty() ) {
            return;
        }
        writer.writeStartElement( WMTSNS, "Themes" );
        for ( Theme t : themes ) {
            writer.writeStartElement( WMTSNS, "Theme" );
            exportMetadata( writer, t.getMetadata() );

            exportThemes( writer, t.getThemes() );
            exportLayers( writer, t.getLayers() );

            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private static void exportMetadata( XMLStreamWriter writer, LayerMetadata md )
                            throws XMLStreamException {
        Description desc = md.getDescription();
        writeElement( writer, OWS110_NS, "Identifier", md.getName() );
        writeElement( writer, OWS110_NS, "Title", desc.getTitle( null ).getString() );
        LanguageString abs = desc.getAbstract( null );
        if ( abs != null ) {
            writeElement( writer, OWS110_NS, "Abstract", abs.getString() );
        }
        exportKeyWords110New( writer, desc.getKeywords() );
    }

    private static void exportLayers( XMLStreamWriter writer, List<Layer> layers )
                            throws XMLStreamException {
        for ( Layer l : layers ) {
            if ( l instanceof TileLayer ) {
                writeElement( writer, WMTSNS, "LayerRef", l.getMetadata().getName() );
            }
        }
    }

    private static void exportContents( XMLStreamWriter writer, List<Theme> themes )
                            throws XMLStreamException {
        writer.writeStartElement( WMTSNS, "Contents" );

        // layers
        for ( Theme t : themes ) {
            for ( Layer l : Themes.getAllLayers( t ) ) {
                if ( l instanceof TileLayer ) {
                    TileLayer tl = (TileLayer) l;
                    LayerMetadata md = tl.getMetadata();
                    TileStore ts = tl.getTileStore();

                    writer.writeStartElement( WMTSNS, "Layer" );

                    exportMetadata( writer, md );
                    TileMatrixSetMetadata metadata = ts.getTileMatrixSet().getMetadata();
                    writeElement( writer, WMTSNS, "Format", metadata.getFormat() );
                    writer.writeStartElement( WMTSNS, "TileMatrixSetLink" );
                    writeElement( writer, WMTSNS, "TileMatrixSet", md.getName() );
                    writer.writeEndElement();

                    writer.writeEndElement();
                }
            }
        }

        // tile matrices
        for ( Theme t : themes ) {
            for ( Layer l : Themes.getAllLayers( t ) ) {
                if ( l instanceof TileLayer ) {
                    TileLayer tl = (TileLayer) l;
                    LayerMetadata md = tl.getMetadata();
                    TileStore ts = tl.getTileStore();

                    writer.writeStartElement( WMTSNS, "TileMatrixSet" );

                    exportMetadata( writer, md );
                    TileMatrixSetMetadata metadata = ts.getTileMatrixSet().getMetadata();
                    writeElement( writer, OWS110_NS, "SupportedCRS", metadata.getCrs().getAlias() );

                    for ( TileMatrix tm : ts.getTileMatrixSet().getTileMatrices() ) {
                        TileMatrixMetadata tmmd = tm.getMetadata();
                        writer.writeStartElement( WMTSNS, "TileMatrix" );
                        double scale = tmmd.getResolution() / DEFAULT_PIXEL_SIZE;
                        writeElement( writer, OWS110_NS, "Identifier", scale + "" );
                        writeElement( writer, WMTSNS, "ScaleDenominator", scale + "" );
                        Envelope env = tmmd.getSpatialMetadata().getEnvelope();
                        // TODO verify this
                        writeElement( writer, WMTSNS, "TopLeftCorner", env.getMin().get0() + " " + env.getMax().get1() );
                        writeElement( writer, WMTSNS, "TileWidth", Integer.toString( tmmd.getTileSize().first ) );
                        writeElement( writer, WMTSNS, "TileHeight", Integer.toString( tmmd.getTileSize().second ) );
                        writeElement( writer, WMTSNS, "MatrixWidth", Integer.toString( tmmd.getNumTilesX() ) );
                        writeElement( writer, WMTSNS, "MatrixHeight", Integer.toString( tmmd.getNumTilesY() ) );

                        writer.writeEndElement();
                    }

                    writer.writeEndElement();
                }
            }
        }

        writer.writeEndElement();
    }

}
