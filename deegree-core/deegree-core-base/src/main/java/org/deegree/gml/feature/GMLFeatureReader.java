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
package org.deegree.gml.feature;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.tom.gml.GMLObjectCategory.TIME_SLICE;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.feature.property.ExtraProps.EXTRA_PROP_NS;
import static org.deegree.feature.property.ExtraProps.EXTRA_PROP_NS_GEOMETRY;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2_OR_3;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.ValueRepresentation.INLINE;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.timeslice.GenericTimeSlice;
import org.deegree.feature.timeslice.TimeSlice;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.DynamicAppSchema;
import org.deegree.feature.types.DynamicFeatureType;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.commons.AbstractGMLObjectReader;
import org.deegree.gml.reference.FeatureReference;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.gml.schema.WellKnownGMLTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractGMLObjectReader} for features and feature collections.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class GMLFeatureReader extends AbstractGMLObjectReader {

	private static final Logger LOG = LoggerFactory.getLogger(GMLFeatureReader.class);

	private static String FID = "fid";

	private static String GMLID = "id";

	public static final QName BOUNDED_BY_GML31 = new QName(GMLNS, "boundedBy", "gml");

	public static final QName BOUNDED_BY_GML32 = new QName(GML3_2_NS, "boundedBy", "gml");

	/**
	 * Creates a new {@link GMLFeatureReader} instance that is configured from the given
	 * {@link GMLStreamReader}.
	 * @param gmlStreamReader provides the configuration, must not be <code>null</code>
	 */
	public GMLFeatureReader(GMLStreamReader gmlStreamReader) {
		super(gmlStreamReader);
	}

	/**
	 * Returns the object representation for the feature (or feature collection) element
	 * event that the cursor of the given <code>XMLStreamReader</code> points at.
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event of the
	 * feature element, afterwards points at the next event after the
	 * <code>END_ELEMENT</code> event of the feature element
	 * @param crs default CRS for all descendant geometry properties, can be
	 * <code>null</code>
	 * @return object representation for the given feature element
	 * @throws XMLStreamException
	 * @throws UnknownCRSException
	 * @throws XMLParsingException
	 */
	public Feature parseFeature(XMLStreamReaderWrapper xmlStream, ICRS crs)
			throws XMLStreamException, XMLParsingException, UnknownCRSException {

		if (schema == null) {
			schema = buildAppSchema(xmlStream);
			gmlStreamReader.setApplicationSchema(schema);
		}
		if (schema instanceof DynamicAppSchema) {
			return parseFeatureDynamic(xmlStream, crs, (DynamicAppSchema) schema);
		}
		return parseFeatureStatic(xmlStream, crs);
	}

	/**
	 * Returns the object representation for the time slice element event that the cursor
	 * of the given <code>XMLStreamReader</code> points at.
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event of the
	 * time slice element, afterwards points at the next event after the
	 * <code>END_ELEMENT</code> event of the time slice element
	 * @param crs default CRS for all descendant geometry properties, can be
	 * <code>null</code>
	 * @return object representation for the given time slice element
	 * @throws XMLStreamException
	 * @throws UnknownCRSException
	 * @throws XMLParsingException
	 */
	public TimeSlice parseTimeSlice(final XMLStreamReaderWrapper xmlStream, final ICRS crs)
			throws XMLStreamException, XMLParsingException, UnknownCRSException {
		final String id = parseGmlId(xmlStream);
		final QName elName = xmlStream.getName();
		final GMLObjectType type = schema.getGmlObjectType(elName);
		if (type == null || type.getCategory() != TIME_SLICE) {
			final String msg = elName + " is not a valid TimeSlice element.";
			throw new XMLStreamException(msg);
		}
		final List<Property> props = parseProperties(xmlStream, crs, type);
		final TimeSlice timeSlice = new GenericTimeSlice(id, type, props);
		if (id != null && !"".equals(id)) {
			if (idContext.getObject(id) != null) {
				String msg = Messages.getMessage("ERROR_FEATURE_ID_NOT_UNIQUE", id);
				throw new XMLParsingException(xmlStream, msg);
			}
			idContext.addObject(timeSlice);
		}
		return timeSlice;
	}

	private Feature parseFeatureDynamic(XMLStreamReaderWrapper xmlStream, ICRS crs, DynamicAppSchema appSchema)
			throws XMLStreamException, XMLParsingException, UnknownCRSException {

		String fid = parseFeatureId(xmlStream);

		if (LOG.isDebugEnabled()) {
			LOG.debug("- parsing feature, gml:id=" + fid + " (begin): " + xmlStream.getCurrentEventInfo());
		}

		QName featureName = xmlStream.getName();
		FeatureType ft = lookupFeatureType(xmlStream, featureName, false);
		if (ft == null) {
			LOG.debug("- adding feature type '" + featureName + "'");
			ft = appSchema.addFeatureType(featureName);
		}
		else {
			LOG.debug("- found feature type '" + featureName + "'");
		}

		ICRS activeCRS = crs;
		List<Property> props = new ArrayList<Property>();
		PropertyType lastPropDecl = null;

		nextElement(xmlStream);

		while (xmlStream.getEventType() == START_ELEMENT) {
			QName propName = xmlStream.getName();
			LOG.debug("- property '" + propName + "'");

			Property property = null;
			PropertyType propDecl = ft.getPropertyDeclaration(propName);
			if (propDecl == null) {
				property = parsePropertyDynamic(propName, xmlStream, activeCRS, ft, lastPropDecl, appSchema);
				propDecl = property.getType();
			}
			else {
				property = parseProperty(xmlStream, propDecl, activeCRS);
			}

			if (property != null) {
				// if this is the "gml:boundedBy" property, override active CRS
				// (see GML spec. (where???))
				if (BOUNDED_BY_GML31.equals(propDecl.getName()) || BOUNDED_BY_GML32.equals(propDecl.getName())) {
					Envelope bbox = (Envelope) property.getValue();
					if (bbox.getCoordinateSystem() != null) {
						activeCRS = bbox.getCoordinateSystem();
						LOG.debug("- crs (from boundedBy): '" + activeCRS + "'");
					}
				}

				props.add(property);
			}

			xmlStream.nextTag();
			if (lastPropDecl != propDecl) {
				lastPropDecl = propDecl;
			}
		}

		Feature feature = ft.newFeature(fid, props, null);
		if (fid != null && !"".equals(fid)) {
			if (idContext.getObject(fid) != null) {
				String msg = Messages.getMessage("ERROR_FEATURE_ID_NOT_UNIQUE", fid);
				throw new XMLParsingException(xmlStream, msg);
			}
			idContext.addObject(feature);
		}

		return feature;
	}

	private Property parsePropertyDynamic(QName propName, XMLStreamReaderWrapper xmlStream, ICRS activeCRS,
			FeatureType ft, PropertyType lastPropDecl, DynamicAppSchema appSchema)
			throws XMLParsingException, XMLStreamException, UnknownCRSException {

		Map<QName, String> propAttributes = XMLStreamUtils.getAttributes(xmlStream);
		StringBuffer text = new StringBuffer();
		QName childElName = null;
		xmlStream.next();
		while (!xmlStream.isStartElement() && !xmlStream.isEndElement()) {
			if (xmlStream.isCharacters()) {
				text.append(xmlStream.getText());
			}
			xmlStream.next();
		}
		if (xmlStream.isStartElement()) {
			childElName = xmlStream.getName();
		}

		PropertyType propDecl = null;
		if (xmlStream.isEndElement()) {
			if (propAttributes.containsKey(new QName(XLNNS, "href"))) {
				LOG.debug("Detected complex (xlink-valued) property '" + propName + "'. Treating as feature property.");
				propDecl = ((DynamicFeatureType) ft).addFeaturePropertyDeclaration(lastPropDecl, propName, null);
			}
			else {
				LOG.debug("Detected simple property '" + propName + "'.");
				propDecl = ((DynamicFeatureType) ft).addSimplePropertyDeclaration(lastPropDecl, propName);
			}
		}
		else {
			if (gmlStreamReader.getGeometryReader().isGeometryElement(xmlStream)) {
				LOG.debug("Detected geometry property '" + propName + "'.");
				propDecl = ((DynamicFeatureType) ft).addGeometryPropertyDeclaration(lastPropDecl, propName);
			}
			else {
				LOG.debug("Detected complex non-geometry property '" + propName + "'. Treating as feature property.");
				FeatureType valueFt = schema.getFeatureType(childElName);
				if (valueFt == null) {
					valueFt = appSchema.addFeatureType(childElName);
				}
				propDecl = ((DynamicFeatureType) ft).addFeaturePropertyDeclaration(lastPropDecl, propName, valueFt);
			}
		}

		TypedObjectNode value = null;
		if (propDecl instanceof SimplePropertyType) {
			value = new PrimitiveValue(text.toString().trim(), new PrimitiveType(STRING));
		}
		else if (propDecl instanceof GeometryPropertyType) {
			value = gmlStreamReader.getGeometryReader().parse(xmlStream, activeCRS);
			XMLStreamUtils.nextElement(xmlStream);
		}
		else if (propDecl instanceof FeaturePropertyType) {
			String href = propAttributes.get(new QName(XLNNS, "href"));
			if (href != null) {
				FeatureReference refFeature = null;
				if (specialResolver != null) {
					refFeature = new FeatureReference(specialResolver, href, xmlStream.getSystemId());
				}
				else {
					refFeature = new FeatureReference(idContext, href, xmlStream.getSystemId());
				}
				idContext.addReference(refFeature);
				value = refFeature;
			}
			else {
				value = parseFeatureDynamic(xmlStream, activeCRS, appSchema);
				XMLStreamUtils.nextElement(xmlStream);
			}
		}
		return new GenericProperty(propDecl, value);
	}

	private Feature parseFeatureStatic(XMLStreamReaderWrapper xmlStream, ICRS crs)
			throws XMLStreamException, XMLParsingException, UnknownCRSException {

		Feature feature = null;
		String fid = parseFeatureId(xmlStream);

		QName featureName = xmlStream.getName();
		FeatureType ft = lookupFeatureType(xmlStream, featureName, true);

		if (LOG.isDebugEnabled()) {
			LOG.debug("- parsing feature, gml:id=" + fid + " (begin): " + xmlStream.getCurrentEventInfo());
		}
		List<Property> propertyList = parseProperties(xmlStream, crs, ft);
		if (LOG.isDebugEnabled()) {
			LOG.debug(" - parsing feature (end): " + xmlStream.getCurrentEventInfo());
		}

		int extraPropertyIdx = -1;
		int idx = 0;
		for (final Property prop : propertyList) {
			if (EXTRA_PROP_NS.equals(prop.getName().getNamespaceURI())) {
				extraPropertyIdx = idx;
				break;
			}
			idx++;
		}
		List<Property> extraPropertyList = null;
		if (extraPropertyIdx != -1) {
			extraPropertyList = new ArrayList<Property>(propertyList.subList(extraPropertyIdx, propertyList.size()));
			propertyList = new ArrayList<Property>(propertyList.subList(0, extraPropertyIdx));
		}
		ExtraProps extraProps = null;
		if (extraPropertyList != null) {
			extraProps = new ExtraProps(extraPropertyList.toArray(new Property[extraPropertyList.size()]));
		}
		feature = ft.newFeature(fid, propertyList, extraProps);

		if (fid != null && !"".equals(fid)) {
			if (idContext.getObject(fid) != null) {
				String msg = Messages.getMessage("ERROR_FEATURE_ID_NOT_UNIQUE", fid);
				throw new XMLParsingException(xmlStream, msg);
			}
			idContext.addObject(feature);
		}
		return feature;
	}

	private List<Property> parseProperties(XMLStreamReaderWrapper xmlStream, ICRS crs, GMLObjectType type)
			throws XMLStreamException, UnknownCRSException {
		Iterator<PropertyType> declIter = type.getPropertyDeclarations().iterator();
		PropertyType activeDecl = declIter.next();
		int propOccurences = 0;
		ICRS activeCRS = crs;
		List<Property> propertyList = new ArrayList<Property>();

		xmlStream.nextTag();

		while (xmlStream.getEventType() == START_ELEMENT) {
			QName propName = xmlStream.getName();
			if (LOG.isDebugEnabled()) {
				LOG.debug("- property '" + propName + "'");
			}

			if (propName.getNamespaceURI() != null && propName.getNamespaceURI().startsWith(EXTRA_PROP_NS)) {
				LOG.debug("Parsing extra property: " + propName);
				PropertyType pt = null;
				if (EXTRA_PROP_NS_GEOMETRY.equals(propName.getNamespaceURI())) {
					pt = new GeometryPropertyType(propName, 1, 1, null, null, GEOMETRY, DIM_2_OR_3, INLINE);
				}
				else {
					pt = new SimplePropertyType(propName, 1, 1, STRING, null, null);
				}
				Property prop = parseProperty(xmlStream, pt, activeCRS);
				propertyList.add(prop);
				xmlStream.nextTag();
				continue;
			}

			if (findConcretePropertyType(propName, activeDecl) != null) {
				// current property element is equal to active declaration
				if (activeDecl.getMaxOccurs() != -1 && propOccurences > activeDecl.getMaxOccurs()) {
					String msg = Messages.getMessage("ERROR_PROPERTY_TOO_MANY_OCCURENCES", propName,
							activeDecl.getMaxOccurs(), type.getName());
					throw new XMLParsingException(xmlStream, msg);
				}
			}
			else {
				// current property element is not equal to active declaration
				while (declIter.hasNext() && findConcretePropertyType(propName, activeDecl) == null) {
					if (!gmlStreamReader.getLaxMode() && propOccurences < activeDecl.getMinOccurs()) {
						String msg = null;
						if (activeDecl.getMinOccurs() == 1) {
							msg = Messages.getMessage("ERROR_PROPERTY_MANDATORY", activeDecl.getName(), type.getName());
						}
						else {
							msg = Messages.getMessage("ERROR_PROPERTY_TOO_FEW_OCCURENCES", activeDecl.getName(),
									activeDecl.getMinOccurs(), type.getName());
						}
						throw new XMLParsingException(xmlStream, msg);
					}
					activeDecl = declIter.next();
					propOccurences = 0;
				}
				if (findConcretePropertyType(propName, activeDecl) == null) {
					String msg = Messages.getMessage("ERROR_PROPERTY_UNEXPECTED", propName, type.getName());
					throw new XMLParsingException(xmlStream, msg);
				}
			}

			Property property = parseProperty(xmlStream, findConcretePropertyType(propName, activeDecl), activeCRS);
			if (property != null) {
				// if this is the "gml:boundedBy" property, override active CRS
				// (see GML spec. (where???))
				if (BOUNDED_BY_GML31.equals(activeDecl.getName()) || BOUNDED_BY_GML32.equals(activeDecl.getName())) {
					Envelope bbox = (Envelope) property.getValue();
					if (bbox != null && bbox.getCoordinateSystem() != null) {
						activeCRS = bbox.getCoordinateSystem();
						LOG.debug("- crs (from boundedBy): '" + activeCRS + "'");
					}
				}

				propertyList.add(property);
			}
			propOccurences++;
			xmlStream.nextTag();
		}
		return propertyList;
	}

	/**
	 * Returns a {@link StreamFeatureCollection} that allows stream-based access to the
	 * members of the feature collection that the cursor of the given
	 * <code>XMLStreamReader</code> points at.
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event of a
	 * feature collection element
	 * @param crs default CRS for all descendant geometry properties, can be
	 * <code>null</code>
	 * @return
	 * @throws XMLStreamException
	 */
	public StreamFeatureCollection getFeatureStream(XMLStreamReaderWrapper xmlStream, ICRS crs)
			throws XMLStreamException {

		if (schema == null) {
			schema = buildAppSchema(xmlStream);
			gmlStreamReader.setApplicationSchema(schema);
		}
		String fid = parseFeatureId(xmlStream);
		QName featureName = xmlStream.getName();
		FeatureCollectionType ft = (FeatureCollectionType) lookupFeatureType(xmlStream, featureName, true);
		return new StreamFeatureCollection(fid, ft, this, xmlStream, crs);
	}

	private AppSchema buildAppSchema(XMLStreamReaderWrapper xmlStream) throws XMLParsingException {
		String schemaLocation = xmlStream.getAttributeValue(XSINS, "schemaLocation");
		if (schemaLocation == null) {
			LOG.warn(Messages.getMessage("NO_SCHEMA_LOCATION", xmlStream.getSystemId()));
			return new DynamicAppSchema();
		}

		String[] tokens = schemaLocation.trim().split("\\s+");
		if (tokens.length % 2 != 0) {
			LOG.warn(Messages.getMessage("ERROR_SCHEMA_LOCATION_TOKENS_COUNT", xmlStream.getSystemId()));
			return new DynamicAppSchema();
		}
		String[] schemaUrls = new String[tokens.length / 2];
		for (int i = 0; i < schemaUrls.length; i++) {
			String schemaUrl = tokens[i * 2 + 1];
			try {
				if (xmlStream.getSystemId() == null) {
					// must be absolute, as SystemId is required for resolving relative
					// URLs
					schemaUrls[i] = new URL(schemaUrl).toString();
				}
				else {
					schemaUrls[i] = new URL(new URL(xmlStream.getSystemId()), schemaUrl).toString();
				}
			}
			catch (Throwable t) {
				LOG.warn(Messages.getMessage("INVALID_SCHEMA_LOCATION", xmlStream.getSystemId()));
				return new DynamicAppSchema();
			}
		}

		// TODO handle multi-namespace schemas
		AppSchema schema = null;
		try {
			GMLAppSchemaReader decoder = new GMLAppSchemaReader(gmlStreamReader.getVersion(), null, schemaUrls);
			schema = decoder.extractAppSchema();
		}
		catch (Throwable t) {
			LOG.warn(Messages.getMessage("BROKEN_SCHEMA", xmlStream.getSystemId(), t.getMessage()), t);
			return new DynamicAppSchema();
		}
		return schema;
	}

	/**
	 * Returns the feature type with the given name.
	 * <p>
	 * If no feature type with the given name is defined, an XMLParsingException is
	 * thrown.
	 * @param xmlStreamReader
	 * @param ftName feature type name to look up
	 * @return the feature type with the given name
	 * @throws XMLParsingException if no feature type with the given name is defined
	 */
	private FeatureType lookupFeatureType(XMLStreamReaderWrapper xmlStreamReader, QName ftName, boolean exception)
			throws XMLParsingException {

		FeatureType ft = null;
		ft = schema.getFeatureType(ftName);
		if (ft == null) {
			ft = WellKnownGMLTypes.getType(ftName);
		}
		if (ft == null && exception) {
			String msg = Messages.getMessage("ERROR_SCHEMA_FEATURE_TYPE_UNKNOWN", ftName);
			throw new XMLParsingException(xmlStreamReader, msg);
		}
		return ft;
	}

	/**
	 * Parses the feature id attribute from the feature <code>START_ELEMENT</code> event
	 * that the given <code>XMLStreamReader</code> points to.
	 * <p>
	 * Looks after 'gml:id' (GML 3) first, if no such attribute is present, the 'fid' (GML
	 * 2) attribute is used.
	 * @param xmlReader must point to the <code>START_ELEMENT</code> event of the feature
	 * @return the feature id, or "" (empty string) if neither a 'gml:id' nor a 'fid'
	 * attribute is present
	 */
	protected String parseFeatureId(XMLStreamReaderWrapper xmlReader) {

		String fid = xmlReader.getAttributeValue(gmlNs, GMLID);
		if (fid == null) {
			fid = xmlReader.getAttributeValue(null, FID);
		}

		// Check that the feature id has the correct form. "fid" and "gml:id" are both
		// based on the XML type "ID":
		// http://www.w3.org/TR/xmlschema11-2/#NCName Thus, they must match the NCName
		// production rule. This means that
		// they may not contain a separating colon (only at the first position a colon is
		// allowed) and must not start
		// with a digit.
		if (fid != null && fid.length() > 0 && !fid.matches("[^\\d][^:]+")) {
			String msg = Messages.getMessage("ERROR_INVALID_FEATUREID", fid);
			throw new IllegalArgumentException(msg);
		}
		return fid;
	}

}
