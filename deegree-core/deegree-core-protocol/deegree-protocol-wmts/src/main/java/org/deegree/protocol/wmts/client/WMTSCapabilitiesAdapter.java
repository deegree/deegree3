//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wmts.client;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getElementTextAsDouble;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getElementTextAsInteger;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.requireStartElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.protocol.wmts.WMTSConstants.WMTS_100_NS;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.protocol.ows.capabilities.OWSCapabilitiesAdapter;
import org.deegree.protocol.ows.capabilities.OWSCommon110CapabilitiesAdapter;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;

/**
 * {@link OWSCapabilitiesAdapter} for Web Map Tile Service (WMTS) 1.0.0 capabilities documents.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class WMTSCapabilitiesAdapter extends OWSCommon110CapabilitiesAdapter {

    private static final QName IDENTIFIER = new QName( OWS_11_NS, "Identifier" );

    private static final QName LAYER = new QName( WMTS_100_NS, "Layer" );

    private static final QName TILE_MATRIX_SET = new QName( WMTS_100_NS, "TileMatrixSet" );

    private static final QName TILE_MATRIX = new QName( WMTS_100_NS, "TileMatrix" );

    private static final QName SCALE_DENOMINATOR = new QName( WMTS_100_NS, "ScaleDenominator" );

    private static final QName TOP_LEFT_CORNER = new QName( WMTS_100_NS, "TopLeftCorner" );

    private static final QName TILE_WIDTH = new QName( WMTS_100_NS, "TileWidth" );

    private static final QName TILE_HEIGHT = new QName( WMTS_100_NS, "TileHeight" );

    private static final QName MATRIX_WIDTH = new QName( WMTS_100_NS, "MatrixWidth" );

    private static final QName MATRIX_HEIGHT = new QName( WMTS_100_NS, "MatrixHeight" );

    /**
     * Creates a new {@link WMTSCapabilitiesAdapter} instance.
     */
    public WMTSCapabilitiesAdapter() {
        nsContext.addNamespace( "wmts", WMTS_100_NS );
    }

    public List<Layer> parseLayers()
                            throws XMLStreamException {
        OMElement contentsElement = getElement( getRootElement(), new XPath( "wmts:Contents", nsContext ) );
        XMLStreamReader xmlStream = contentsElement.getXMLStreamReader();
        skipStartDocument( xmlStream );
        while ( !xmlStream.isStartElement() || !LAYER.equals( xmlStream.getName() ) ) {
            if ( xmlStream.getEventType() == END_DOCUMENT ) {
                return emptyList();
            }
            xmlStream.next();
        }
        return parseLayers( xmlStream );
    }

    private List<Layer> parseLayers( XMLStreamReader xmlStream )
                            throws NoSuchElementException, XMLStreamException {
        List<Layer> layers = new ArrayList<Layer>();
        while ( xmlStream.isStartElement() && LAYER.equals( xmlStream.getName() ) ) {
            Layer layer = parseLayer( xmlStream );
            layers.add( layer );
            nextElement( xmlStream );
        }
        return layers;
    }

    private Layer parseLayer( XMLStreamReader xmlStream ) {

        // <element ref="ows:WGS84BoundingBox" minOccurs="0" maxOccurs="unbounded">
        // <element name="Identifier" type="ows:CodeType">
        // <element ref="ows:BoundingBox" minOccurs="0" maxOccurs="unbounded">
        // <element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded">
        // <element ref="ows:DatasetDescriptionSummary" minOccurs="0" maxOccurs="unbounded">
        // <element ref="wmts:Style" maxOccurs="unbounded">
        // <element name="Format" type="ows:MimeType" maxOccurs="unbounded">
        // <element name="InfoFormat" type="ows:MimeType" minOccurs="0" maxOccurs="unbounded">
        // <element ref="wmts:Dimension" minOccurs="0" maxOccurs="unbounded">
        // <element ref="wmts:TileMatrixSetLink" maxOccurs="unbounded">
        // <element name="ResourceURL" type="wmts:URLTemplateType" minOccurs="0" maxOccurs="unbounded">

        return new Layer();
    }

    public List<TileMatrixSet> parseTileMatrixSets()
                            throws XMLStreamException {
        OMElement contentsElement = getElement( getRootElement(), new XPath( "wmts:Contents", nsContext ) );
        XMLStreamReader xmlStream = contentsElement.getXMLStreamReader();
        skipStartDocument( xmlStream );
        parseLayers( xmlStream );
        while ( !xmlStream.isStartElement() || !TILE_MATRIX_SET.equals( xmlStream.getName() ) ) {
            if ( xmlStream.getEventType() == END_DOCUMENT ) {
                return emptyList();
            }
            xmlStream.next();
        }
        return parseTileMatrixSets( xmlStream );
    }

    private List<TileMatrixSet> parseTileMatrixSets( XMLStreamReader xmlStream )
                            throws NoSuchElementException, XMLStreamException {
        List<TileMatrixSet> tileMatrixSets = new ArrayList<TileMatrixSet>();
        while ( xmlStream.isStartElement() && TILE_MATRIX_SET.equals( xmlStream.getName() ) ) {
            TileMatrixSet tileMatrixSet = parseTileMatrixSet( xmlStream );
            tileMatrixSets.add( tileMatrixSet );
            nextElement( xmlStream );
        }
        return tileMatrixSets;
    }

    private TileMatrixSet parseTileMatrixSet( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        nextElement( xmlStream );

        // <element ref="ows:Identifier">
        requireStartElement( xmlStream, singletonList( IDENTIFIER ) );
        String identifier = xmlStream.getElementText().trim();
        nextElement( xmlStream );

        // <element ref="ows:BoundingBox" minOccurs="0">
        // <element ref="ows:SupportedCRS">
        // <element name="WellKnownScaleSet" type="anyURI" minOccurs="0">

        while ( !xmlStream.isStartElement() || !TILE_MATRIX.equals( xmlStream.getName() ) ) {
            xmlStream.next();
        }

        // <element ref="wmts:TileMatrix" maxOccurs="unbounded">
        requireStartElement( xmlStream, singletonList( TILE_MATRIX ) );
        List<TileMatrix> matrices = new ArrayList<TileMatrix>();
        while ( xmlStream.isStartElement() && TILE_MATRIX.equals( xmlStream.getName() ) ) {
            matrices.add( parseTileMatrix( xmlStream ) );
            XMLStreamUtils.nextElement( xmlStream );
        }

        SpatialMetadata spatialMetadata = null;
        return new TileMatrixSet( identifier, matrices, spatialMetadata );
    }

    private TileMatrix parseTileMatrix( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        nextElement( xmlStream );

        // <element ref="ows:Identifier">
        requireStartElement( xmlStream, singletonList( IDENTIFIER ) );
        String identifier = xmlStream.getElementText().trim();
        nextElement( xmlStream );

        // <element name="ScaleDenominator" type="double">
        requireStartElement( xmlStream, singletonList( SCALE_DENOMINATOR ) );
        double resolution = getElementTextAsDouble( xmlStream );
        nextElement( xmlStream );

        // <element name="TopLeftCorner" type="ows:PositionType">
        requireStartElement( xmlStream, singletonList( TOP_LEFT_CORNER ) );
        SpatialMetadata spatialMetadata = null;
        skipElement( xmlStream );
        nextElement( xmlStream );

        // <element name="TileWidth" type="positiveInteger">
        requireStartElement( xmlStream, singletonList( TILE_WIDTH ) );
        int tileSizeX = getElementTextAsInteger( xmlStream );
        nextElement( xmlStream );

        // <element name="TileHeight" type="positiveInteger">
        requireStartElement( xmlStream, singletonList( TILE_HEIGHT ) );
        int tileSizeY = getElementTextAsInteger( xmlStream );
        nextElement( xmlStream );

        // <element name="MatrixWidth" type="positiveInteger">
        requireStartElement( xmlStream, singletonList( MATRIX_WIDTH ) );
        int numTilesX = getElementTextAsInteger( xmlStream );
        nextElement( xmlStream );

        // <element name="MatrixHeight" type="positiveInteger">
        requireStartElement( xmlStream, singletonList( MATRIX_HEIGHT ) );
        int numTilesY = getElementTextAsInteger( xmlStream );
        nextElement( xmlStream );

        return new TileMatrix( identifier, spatialMetadata, tileSizeX, tileSizeY, resolution, numTilesX, numTilesY );
    }
}
