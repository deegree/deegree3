/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
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
package org.deegree.services.wfs.format.csv;

import static org.apache.commons.csv.CSVFormat.DEFAULT;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.apache.commons.csv.CSVPrinter;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.ows.StringOrRef;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.LengthPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.io.WKTWriter;
import org.deegree.gml.reference.FeatureReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writer for CSV documents.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CsvFeatureWriter {

	private static final Logger LOG = LoggerFactory.getLogger(CsvFeatureWriter.class);

	public static final String CRS = "CRS";

	private final CSVPrinter csvPrinter;

	private final ICRS requestedCRS;

	private final FeatureType featureType;

	private final List<QName> propertyNames;

	public CsvFeatureWriter(Writer writer, ICRS requestedCRS, FeatureType featureType) throws IOException {
		this.propertyNames = findPropertyNamesToOutput(featureType);
		this.csvPrinter = new CSVPrinter(writer, DEFAULT.withHeader(createHeaders()));
		this.requestedCRS = requestedCRS;
		this.featureType = featureType;
	}

	/**
	 * Writes a new Feature into the FeatureCollection.
	 * @param feature the feature to write, never <code>null</code>
	 * @throws IOException if GeoJSON could no be written
	 * @throws TransformationException if a geometry to export cannot be transformed to
	 * CRS:84
	 * @throws UnknownCRSException if the CRS of the geometry is not supported
	 */
	public void write(Feature feature) throws IOException {
		QName featureName = feature.getName();
		LOG.debug("Exporting Feature {} with ID {}", featureName, feature.getId());
		try {
			List<String> record = createRecord(feature);
			csvPrinter.printRecord(record);
		}
		catch (IOException e) {
			LOG.debug("Export of feature with ID " + feature.getId() + " failed", e);
			throw e;
		}
		catch (TransformationException | UnknownCRSException e) {
			LOG.debug("Export of feature with ID " + feature.getId() + " failed", e);
			throw new IOException("Could not write Feature as CSV Entry.", e);
		}
	}

	private List<String> createRecord(Feature feature)
			throws TransformationException, UnknownCRSException, IOException {
		List<String> csvEntry = new ArrayList<>();
		for (QName propertyName : propertyNames) {
			List<Property> properties = feature.getProperties(propertyName);
			if (properties.isEmpty()) {
				csvEntry.add(null);
			}
			else {
				csvEntry.add(export(propertyName, properties));
			}
		}
		csvEntry.add(crsAsString());
		return csvEntry;
	}

	private List<QName> findPropertyNamesToOutput(FeatureType featureType) {
		return featureType.getPropertyDeclarations()
			.stream()
			.filter(p -> isSupportedProperty(p))
			.map(p -> p.getName())
			.collect(Collectors.toList());
	}

	private boolean isSupportedProperty(PropertyType p) {
		if (p instanceof CustomPropertyType) {
			return ((CustomPropertyType) p).getXSDValueType()
				.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE;
		}
		boolean isSupportedProperty = p instanceof CodePropertyType || p instanceof EnvelopePropertyType
				|| p instanceof MeasurePropertyType || p instanceof LengthPropertyType
				|| p instanceof FeaturePropertyType || p instanceof GeometryPropertyType
				|| p instanceof SimplePropertyType || p instanceof StringOrRef;
		if (!isSupportedProperty)
			LOG.warn("Property with name " + p.getName() + " cannot be exported as CSV");
		return isSupportedProperty;
	}

	private String[] createHeaders() {
		List<String> headers = propertyNames.stream().map(name -> name.toString()).collect(Collectors.toList());
		headers.add(CRS);
		return headers.toArray(new String[headers.size()]);
	}

	private String crsAsString() {
		if (requestedCRS != null) {
			return requestedCRS.getAlias();
		}
		return null;
	}

	private String export(QName propertyName, List<Property> properties)
			throws TransformationException, UnknownCRSException, IOException {
		StringBuilder sb = new StringBuilder();
		int propertyIndex = 0;
		for (Property property : properties) {
			String propertyAsString = propertyAsString(propertyName, property);
			if (propertyAsString != null) {
				if (propertyIndex > 0)
					sb.append(" | ");
				sb.append(propertyAsString.replace("\n", ""));
			}
			propertyIndex++;
		}
		if (sb.length() == 0)
			return null;
		return sb.toString();
	}

	private String propertyAsString(QName propertyName, Property property)
			throws TransformationException, UnknownCRSException, IOException {
		if (property instanceof SimpleProperty) {
			return export(property.getValue());
		}
		else if (property instanceof FeatureReference) {
			return ((FeatureReference) property).getURI();
		}
		else if (property instanceof Geometry) {
			return export((Geometry) property);
		}
		else if (property instanceof GenericProperty) {
			return export(property.getValue());
		}
		throw new IOException(
				"Unhandled property type '" + property.getClass() + "' (property name " + propertyName + " )");
	}

	private String export(Geometry geometry) throws UnknownCRSException, TransformationException {
		Geometry geom = transformGeometryIfRequired(geometry);
		return WKTWriter.write(geom);
	}

	private String export(TypedObjectNode node) throws TransformationException, UnknownCRSException, IOException {
		if (node == null) {
			return null;
		}
		if (node instanceof PrimitiveValue) {
			return export((PrimitiveValue) node);
		}
		if (node instanceof Geometry) {
			return export((Geometry) node);
		}
		if (node instanceof GenericXMLElement) {
			return export(((GenericXMLElement) node).getValue());
		}
		if (node instanceof FeatureReference) {
			return ((FeatureReference) node).getURI();
		}
		throw new IOException("Unhandled node type '" + node.getClass());
	}

	private String export(PrimitiveValue value) {
		if (value == null) {
			return null;
		}
		return value.getAsText();
	}

	private Geometry transformGeometryIfRequired(Geometry geometry)
			throws UnknownCRSException, TransformationException {
		if (requestedCRS == null || requestedCRS.equals(geometry.getCoordinateSystem())) {
			return geometry;
		}
		GeometryTransformer geometryTransformer = new GeometryTransformer(requestedCRS);
		return geometryTransformer.transform(geometry);
	}

}