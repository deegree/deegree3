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
import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValueAsBoolean;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getElementTextAsBigInteger;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getElementTextAsDouble;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.requireStartElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipToElementOnSameLevel;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipToRequiredElementOnSameLevel;
import static org.deegree.cs.CRSUtils.calcResolution;
import static org.deegree.protocol.wmts.WMTSConstants.WMTS_100_NS;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.protocol.ows.capabilities.OWSCommon110CapabilitiesAdapter;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;

/**
 * {@link org.deegree.protocol.ows.capabilities.OWSCapabilitiesAdapter} for Web Map Tile
 * Service (WMTS) 1.0.0 capabilities documents.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class WMTSCapabilitiesAdapter extends OWSCommon110CapabilitiesAdapter {

	private static final QName IDENTIFIER = new QName(OWS_11_NS, "Identifier");

	private static final QName BOUNDING_BOX = new QName(OWS_11_NS, "BoundingBox");

	private static final QName SUPPORTED_CRS = new QName(OWS_11_NS, "SupportedCRS");

	private static final QName LAYER = new QName(WMTS_100_NS, "Layer");

	private static final QName STYLE = new QName(WMTS_100_NS, "Style");

	private static final QName FORMAT = new QName(WMTS_100_NS, "Format");

	private static final QName INFO_FORMAT = new QName(WMTS_100_NS, "InfoFormat");

	private static final QName TILE_MATRIX_SET_LINK = new QName(WMTS_100_NS, "TileMatrixSetLink");

	private static final QName TILE_MATRIX_SET_LIMITS = new QName(WMTS_100_NS, "TileMatrixSetLimits");

	private static final QName TILE_MATRIX_SET = new QName(WMTS_100_NS, "TileMatrixSet");

	private static final QName WELL_KNOWN_SCALE_SET = new QName(WMTS_100_NS, "WellKnownScaleSet");

	private static final QName TILE_MATRIX = new QName(WMTS_100_NS, "TileMatrix");

	private static final QName SCALE_DENOMINATOR = new QName(WMTS_100_NS, "ScaleDenominator");

	private static final QName TOP_LEFT_CORNER = new QName(WMTS_100_NS, "TopLeftCorner");

	private static final QName TILE_WIDTH = new QName(WMTS_100_NS, "TileWidth");

	private static final QName TILE_HEIGHT = new QName(WMTS_100_NS, "TileHeight");

	private static final QName MATRIX_WIDTH = new QName(WMTS_100_NS, "MatrixWidth");

	private static final QName MATRIX_HEIGHT = new QName(WMTS_100_NS, "MatrixHeight");

	/**
	 * Creates a new {@link WMTSCapabilitiesAdapter} instance.
	 */
	public WMTSCapabilitiesAdapter() {
		nsContext.addNamespace("wmts", WMTS_100_NS);
	}

	/**
	 * Returns the {@link Layers}s defined in this document.
	 * @return layers, can be empty, but never <code>null</code>
	 * @throws XMLStreamException
	 */
	public List<Layer> parseLayers() throws XMLStreamException {
		OMElement contentsElement = getElement(getRootElement(), new XPath("wmts:Contents", nsContext));
		XMLStreamReader xmlStream = contentsElement.getXMLStreamReader();
		skipStartDocument(xmlStream);
		nextElement(xmlStream);
		List<Layer> layers = emptyList();
		if (skipToElementOnSameLevel(xmlStream, LAYER)) {
			layers = parseLayers(xmlStream);
		}
		return layers;
	}

	private List<Layer> parseLayers(XMLStreamReader xmlStream) throws XMLStreamException {
		List<Layer> layers = new ArrayList<Layer>();
		while (xmlStream.isStartElement() && LAYER.equals(xmlStream.getName())) {
			Layer layer = parseLayer(xmlStream);
			layers.add(layer);
			nextElement(xmlStream);
		}
		return layers;
	}

	/**
	 * Consumes a <code>wmts:Layer</code> element from the given XML stream.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wmts:Layer&gt;)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/wmts:Layer&gt;)</li>
	 * </ul>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wmts:Layer&gt;), points at the corresponding <code>END_ELEMENT</code> event
	 * (&lt;/wmts:Layer&gt;) afterwards
	 * @return corresponding {@link Layer} representation, never <code>null</code>
	 * @throws org.deegree.commons.xml.XMLParsingException if the element can not be
	 * parsed as a "wmts:Layer" element
	 * @throws XMLStreamException
	 */
	Layer parseLayer(XMLStreamReader xmlStream) throws XMLStreamException {

		nextElement(xmlStream);

		// <element ref="ows:Title" minOccurs="0" maxOccurs="unbounded"/>
		// <element ref="ows:Abstract" minOccurs="0" maxOccurs="unbounded"/>
		// <element ref="ows:Keywords" minOccurs="0" maxOccurs="unbounded"/>

		// <element ref="ows:Identifier">
		skipToRequiredElementOnSameLevel(xmlStream, IDENTIFIER);
		String identifier = xmlStream.getElementText().trim();
		nextElement(xmlStream);

		// <element ref="wmts:Style" maxOccurs="unbounded">
		skipToRequiredElementOnSameLevel(xmlStream, STYLE);
		List<Style> styles = new ArrayList<Style>();
		while (xmlStream.isStartElement() && STYLE.equals(xmlStream.getName())) {
			styles.add(parseStyle(xmlStream));
			nextElement(xmlStream);
		}

		// <element name="Format" type="ows:MimeType" maxOccurs="unbounded">
		skipToRequiredElementOnSameLevel(xmlStream, FORMAT);
		List<String> formats = new ArrayList<String>();
		while (xmlStream.isStartElement() && FORMAT.equals(xmlStream.getName())) {
			formats.add(xmlStream.getElementText().trim());
			nextElement(xmlStream);
		}

		// <element name="InfoFormat" type="ows:MimeType" minOccurs="0"
		// maxOccurs="unbounded">
		List<String> infoFormats = new ArrayList<String>();
		while (xmlStream.isStartElement() && INFO_FORMAT.equals(xmlStream.getName())) {
			infoFormats.add(xmlStream.getElementText().trim());
			nextElement(xmlStream);
		}

		// <element ref="wmts:Dimension" minOccurs="0" maxOccurs="unbounded">

		// <element ref="wmts:TileMatrixSetLink" maxOccurs="unbounded">
		skipToRequiredElementOnSameLevel(xmlStream, TILE_MATRIX_SET_LINK);
		List<String> tileMatrixSets = new ArrayList<String>();
		while (xmlStream.isStartElement() && TILE_MATRIX_SET_LINK.equals(xmlStream.getName())) {
			tileMatrixSets.add(parseTileMatrixSetLink(xmlStream));
			nextElement(xmlStream);
		}

		// <element name="ResourceURL" type="wmts:URLTemplateType" minOccurs="0"
		// maxOccurs="unbounded">
		while (!xmlStream.isEndElement() || !LAYER.equals(xmlStream.getName())) {
			xmlStream.next();
		}

		return new Layer(identifier, styles, formats, infoFormats, tileMatrixSets);
	}

	private Style parseStyle(XMLStreamReader xmlStream) throws XMLStreamException {

		// <attribute name="isDefault" type="boolean">
		boolean isDefault = getAttributeValueAsBoolean(xmlStream, null, "isDefault", true);
		nextElement(xmlStream);

		// <element ref="ows:Identifier">
		requireStartElement(xmlStream, singletonList(IDENTIFIER));
		String identifier = xmlStream.getElementText().trim();
		nextElement(xmlStream);

		// <element ref="wmts:LegendURL" minOccurs="0" maxOccurs="unbounded">
		while (!xmlStream.isEndElement()) {
			xmlStream.next();
		}

		return new Style(identifier, isDefault);
	}

	private String parseTileMatrixSetLink(XMLStreamReader xmlStream) throws XMLStreamException {
		nextElement(xmlStream);
		skipToRequiredElementOnSameLevel(xmlStream, TILE_MATRIX_SET);
		String tileMatrixSet = xmlStream.getElementText().trim();
		nextElement(xmlStream);

		if (TILE_MATRIX_SET_LIMITS.equals(xmlStream.getName())) {
			skipElement(xmlStream);
		}
		return tileMatrixSet;
	}

	/**
	 * Returns the {@link TileMatrixSet}s defined in this document.
	 * @return tile matrix sets, can be empty, but never <code>null</code>
	 * @throws XMLStreamException
	 */
	public List<TileMatrixSet> parseTileMatrixSets() throws XMLStreamException {
		OMElement contentsElement = getElement(getRootElement(), new XPath("wmts:Contents", nsContext));
		XMLStreamReader xmlStream = contentsElement.getXMLStreamReader();
		skipStartDocument(xmlStream);
		nextElement(xmlStream);
		List<TileMatrixSet> tileMatrixSets = emptyList();
		if (skipToElementOnSameLevel(xmlStream, TILE_MATRIX_SET)) {
			tileMatrixSets = parseTileMatrixSets(xmlStream);
		}
		return tileMatrixSets;
	}

	private List<TileMatrixSet> parseTileMatrixSets(XMLStreamReader xmlStream) throws XMLStreamException {
		List<TileMatrixSet> tileMatrixSets = new ArrayList<TileMatrixSet>();
		while (xmlStream.isStartElement() && TILE_MATRIX_SET.equals(xmlStream.getName())) {
			TileMatrixSet tileMatrixSet = parseTileMatrixSet(xmlStream);
			tileMatrixSets.add(tileMatrixSet);
			nextElement(xmlStream);
		}
		return tileMatrixSets;
	}

	/**
	 * Consumes a <code>wmts:TileMatrixSet</code> element from the given XML stream.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wmts:TileMatrixSet&gt;)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/wmts:TileMatrixSet&gt;)</li>
	 * </ul>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wmts:TileMatrixSet&gt;), points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/wmts:TileMatrixSet&gt;) afterwards
	 * @return corresponding {@link TileMatrix} representation, never <code>null</code>
	 * @throws org.deegree.commons.xml.XMLParsingException if the element can not be
	 * parsed as a "wmts:TileMatrixSet" element
	 * @throws XMLStreamException
	 */
	TileMatrixSet parseTileMatrixSet(XMLStreamReader xmlStream) throws XMLStreamException {

		nextElement(xmlStream);

		// <element ref="ows:Title" minOccurs="0" maxOccurs="unbounded"/>
		// <element ref="ows:Abstract" minOccurs="0" maxOccurs="unbounded"/>
		// <element ref="ows:Keywords" minOccurs="0" maxOccurs="unbounded"/>

		// <element ref="ows:Identifier">
		skipToRequiredElementOnSameLevel(xmlStream, IDENTIFIER);
		String identifier = xmlStream.getElementText().trim();
		nextElement(xmlStream);

		// <element ref="ows:BoundingBox" minOccurs="0">
		Envelope boundingBox = null;
		if (BOUNDING_BOX.equals(xmlStream.getName())) {
			skipElement(xmlStream);
			boundingBox = parseBoundingBoxType(xmlStream, null);
			nextElement(xmlStream);
		}

		// <element ref="ows:SupportedCRS">
		requireStartElement(xmlStream, singletonList(SUPPORTED_CRS));
		String supportedCrs = xmlStream.getElementText().trim();
		nextElement(xmlStream);

		// <element name="WellKnownScaleSet" type="anyURI" minOccurs="0">
		String wellKnownScaleSet = null;
		if (WELL_KNOWN_SCALE_SET.equals(xmlStream.getName())) {
			wellKnownScaleSet = xmlStream.getElementText().trim();
			nextElement(xmlStream);
		}

		// <element ref="wmts:TileMatrix" maxOccurs="unbounded">
		skipToRequiredElementOnSameLevel(xmlStream, TILE_MATRIX);
		List<TileMatrix> matrices = new ArrayList<TileMatrix>();
		ICRS crs = CRSManager.getCRSRef(supportedCrs);
		while (xmlStream.isStartElement() && TILE_MATRIX.equals(xmlStream.getName())) {
			matrices.add(parseTileMatrix(xmlStream, crs));
			nextElement(xmlStream);
		}

		if (boundingBox != null) {
			enforceCrs(crs, boundingBox);
		}

		SpatialMetadata spatialMetadata = createSpatialMetadata(crs, boundingBox, matrices);
		return new TileMatrixSet(identifier, wellKnownScaleSet, matrices, spatialMetadata, null);
	}

	private void enforceCrs(ICRS crs, Envelope boundingBox) {
		boundingBox.getMin().setCoordinateSystem(crs);
		boundingBox.getMax().setCoordinateSystem(crs);
		boundingBox.setCoordinateSystem(crs);
	}

	private SpatialMetadata createSpatialMetadata(ICRS crs, Envelope boundingBox, List<TileMatrix> matrices) {
		Envelope matrixSetEnvelope = boundingBox;
		if (boundingBox == null) {
			matrixSetEnvelope = matrices.get(0).getSpatialMetadata().getEnvelope();
		}
		return new SpatialMetadata(matrixSetEnvelope, singletonList(crs));
	}

	/**
	 * Consumes a <code>wmts:TileMatrix</code> element from the given XML stream.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wmts:TileMatrix&gt;)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/wmts:TileMatrix&gt;)</li>
	 * </ul>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wmts:TileMatrix&gt;), points at the corresponding <code>END_ELEMENT</code>
	 * event (&lt;/wmts:TileMatrix&gt;) afterwards
	 * @param crs world coordinate reference system, must not be <code>null</code>
	 * @return corresponding {@link TileMatrix} representation, never <code>null</code>
	 * @throws org.deegree.commons.xml.XMLParsingException if the element can not be
	 * parsed as a "wmts:TileMatrix" element
	 * @throws XMLStreamException
	 */
	TileMatrix parseTileMatrix(XMLStreamReader xmlStream, ICRS crs) throws XMLStreamException {

		nextElement(xmlStream);

		// <element ref="ows:Title" minOccurs="0" maxOccurs="unbounded"/>
		// <element ref="ows:Abstract" minOccurs="0" maxOccurs="unbounded"/>
		// <element ref="ows:Keywords" minOccurs="0" maxOccurs="unbounded"/>

		// <element ref="ows:Identifier">
		skipToRequiredElementOnSameLevel(xmlStream, IDENTIFIER);
		String identifier = xmlStream.getElementText().trim();
		nextElement(xmlStream);

		// <element name="ScaleDenominator" type="double">
		requireStartElement(xmlStream, singletonList(SCALE_DENOMINATOR));
		double scaleDenominator = getElementTextAsDouble(xmlStream);
		nextElement(xmlStream);

		// <element name="TopLeftCorner" type="ows:PositionType">
		requireStartElement(xmlStream, singletonList(TOP_LEFT_CORNER));
		double[] topLeftCorner = parsePositionType(xmlStream);
		skipElement(xmlStream);
		nextElement(xmlStream);

		// <element name="TileWidth" type="positiveInteger">
		requireStartElement(xmlStream, singletonList(TILE_WIDTH));
		BigInteger tileSizeX = getElementTextAsBigInteger(xmlStream);
		nextElement(xmlStream);

		// <element name="TileHeight" type="positiveInteger">
		requireStartElement(xmlStream, singletonList(TILE_HEIGHT));
		BigInteger tileSizeY = getElementTextAsBigInteger(xmlStream);
		nextElement(xmlStream);

		// <element name="MatrixWidth" type="positiveInteger">
		requireStartElement(xmlStream, singletonList(MATRIX_WIDTH));
		BigInteger numTilesX = getElementTextAsBigInteger(xmlStream);
		nextElement(xmlStream);

		// <element name="MatrixHeight" type="positiveInteger">
		requireStartElement(xmlStream, singletonList(MATRIX_HEIGHT));
		BigInteger numTilesY = getElementTextAsBigInteger(xmlStream);
		nextElement(xmlStream);

		double resolution = calcResolution(scaleDenominator, crs);
		SpatialMetadata spatialMetadata = createSpatialMetadata(crs, resolution, topLeftCorner, tileSizeX, tileSizeY,
				numTilesX, numTilesY);

		return new TileMatrix(identifier, spatialMetadata, tileSizeX, tileSizeY, resolution, numTilesX, numTilesY);
	}

	private SpatialMetadata createSpatialMetadata(ICRS crs, double resolution, double[] topLeftCorner,
			BigInteger tileSizeX, BigInteger tileSizeY, BigInteger numTilesX, BigInteger numTilesY) {

		double worldWidth = tileSizeX.longValue() * numTilesX.longValue() * resolution;
		double worldHeight = tileSizeY.longValue() * numTilesY.longValue() * resolution;

		double min0 = topLeftCorner[0];
		double max0 = topLeftCorner[0] + worldWidth;
		double max1 = topLeftCorner[1];
		double min1 = topLeftCorner[1] - worldHeight;

		Point min = new DefaultPoint(null, crs, null, new double[] { min0, min1 });
		Point max = new DefaultPoint(null, crs, null, new double[] { max0, max1 });
		Envelope bbox = new DefaultEnvelope(null, crs, null, min, max);
		return new SpatialMetadata(bbox, singletonList(crs));
	}

}
