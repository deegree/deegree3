/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.gml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReference;
import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.AppSchema;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.gml.dictionary.Dictionary;
import org.deegree.gml.dictionary.GMLDictionaryReader;
import org.deegree.gml.feature.GMLFeatureReader;
import org.deegree.gml.feature.StreamFeatureCollection;
import org.deegree.gml.geometry.GML2GeometryReader;
import org.deegree.gml.geometry.GML3GeometryReader;
import org.deegree.gml.geometry.GMLGeometryReader;
import org.deegree.gml.reference.GmlDocumentIdContext;
import org.deegree.gml.reference.matcher.ReferencePatternMatcher;

/**
 * Stream-based reader for GML instance documents or GML document fragments. Currently
 * supports GML 2/3.0/3.1/3.2.
 *
 * <h4>Initialization</h4> A {@link GMLStreamReader} works by wrapping a
 * {@link XMLStreamReader} instance. Use the methods provided in {@link GMLInputFactory}
 * to create a {@link GMLStreamReader} instance.
 * <p>
 * TODO: Refactor, so configuration settings cannot be modified after creation (e.g. like
 * in XMLInputFactory).
 * </p>
 *
 * <pre>
 * ...
 *   GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_2, new URL ("...") );
 * ...
 * </pre>
 *
 * <h4>General usage</h4> After initialization, {@link #read()} is used to consume the
 * next GML object element (geometry, feature, feature collection, etc.) from the stream
 * and turn it into an object representation.
 *
 * <pre>
 * ...
 *   GMLObject object = gmlReader.read();
 * ...
 * </pre>
 *
 * It's vital that the underlying {@link XMLStreamReader} points to the
 * <code>START_ELEMENT</code> event of the GML element to be read. After calling, the
 * underlying {@link XMLStreamReader} will be positioned on the event after the
 * corresponding <code>END_ELEMENT</code> event.
 *
 * Depending on the actual element, a corresponding {@link GMLObject} instance will be
 * created ({@link Geometry}, {@link FeatureCollection}, {@link Feature}, etc.). In order
 * to work with the object, it has to be cast to the concrete type. Alternatively (if one
 * knows the category of GML element to be read beforehand), use on of the specific
 * <code>read</code> methods to avoid the cast:
 *
 * <pre>
 * ..
 *   Feature feature = gmlReader.readFeature();
 * ...
 * </pre>
 *
 * <h4>Reading GML features/feature collections</h4> TODO
 *
 * <h4>Specifying the application schema</h4> TODO
 *
 * <h4>Streaming vs. non-streaming</h4> TODO
 *
 * <h4>Reading other GML objects</h4> TODO
 *
 * <h4>Advanced options</h4> ...
 *
 * <h4>Notes</h4> Instances of this class are not thread-safe.
 *
 * @see GMLObject
 * @see GMLInputFactory
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GMLStreamReader {

	private final XMLStreamReaderWrapper xmlStream;

	private final GMLVersion version;

	private final GmlDocumentIdContext idContext;

	private GMLReferenceResolver resolver;

	private AppSchema schema;

	private ICRS defaultCRS;

	private GeometryFactory geomFac;

	private int defaultCoordDim = 2;

	private GMLGeometryReader geometryReader;

	private GMLFeatureReader featureReader;

	private GMLDictionaryReader dictReader;

	private boolean laxMode;

	private GMLReferenceResolver internalResolver;

	/**
	 * Creates a new {@link GMLStreamReader} instance.
	 * @param version GML version of the input, must not be <code>null</code>
	 * @param xmlStream XML stream used to read the input, must not be <code>null</code>
	 */
	GMLStreamReader(GMLVersion version, XMLStreamReaderWrapper xmlStream) {
		this.version = version;
		this.xmlStream = xmlStream;
		this.idContext = new GmlDocumentIdContext(version);
		this.geomFac = new GeometryFactory();
	}

	/**
	 * Returns the version of the GML input.
	 * @return the version of the GML input, never <code>null</code>
	 */
	public GMLVersion getVersion() {
		return version;
	}

	public AppSchema getAppSchema() {
		return schema;
	}

	/**
	 * Controls the application schema that is assumed when features or feature
	 * collections are parsed.
	 * @param schema application schema, can be <code>null</code> (use xsi:schemaLocation
	 * attribute to build the application schema)
	 */
	public void setApplicationSchema(AppSchema schema) {
		this.schema = schema;
		idContext.setApplicationSchema(schema);
	}

	/**
	 * Adds a {@link ReferencePatternMatcher} that checks if a url should be skipped or
	 * not.
	 * @param referencePatternMatcher the matcher to add, may be <code>null</code> (all
	 * urls are resolved)
	 */
	public void setReferencePatternMatcher(ReferencePatternMatcher referencePatternMatcher) {
		idContext.setReferencePatternMatcher(referencePatternMatcher);
	}

	public GMLReferenceResolver getResolver() {
		return resolver;
	}

	public int getDefaultCoordinateDimension() {
		return defaultCoordDim;
	}

	public void setDefaultCoordinateDimension(int defaultCoordDim) {
		this.defaultCoordDim = defaultCoordDim;
	}

	/**
	 * Controls the default CRS that is assumed when GML objects (especially geometries)
	 * without SRS information are parsed.
	 * @param defaultCRS default CRS, can be <code>null</code>
	 */
	public void setDefaultCRS(ICRS defaultCRS) {
		this.defaultCRS = defaultCRS;
	}

	public GeometryFactory getGeometryFactory() {
		return geomFac;
	}

	/**
	 * Controls the {@link GeometryFactory} instance to be used for creating geometries.
	 * @param geomFac geometry factory, can be <code>null</code> (use a default factory)
	 */
	public void setGeometryFactory(GeometryFactory geomFac) {
		this.geomFac = geomFac;
	}

	/**
	 * @return true if the stream's event is an {@link XMLStreamConstants#START_ELEMENT}
	 * && the current element's name is a known geometry (with respect to it's gml
	 * version).
	 */
	public boolean isGeometryElement() {
		GMLGeometryReader geomReader = getGeometryReader();
		return geomReader.isGeometryElement(getXMLReader());
	}

	/**
	 * @return true if the stream's event is an {@link XMLStreamConstants#START_ELEMENT}
	 * && the current element's name is a known geometry (with respect to it's gml
	 * version).
	 */
	public boolean isGeometryOrEnvelopeElement() {
		GMLGeometryReader geomReader = getGeometryReader();
		return geomReader.isGeometryOrEnvelopeElement(getXMLReader());
	}

	/**
	 * Sets the {@link GMLReferenceResolver} that the generated {@link GMLReference}s will
	 * use for resolving themselves.
	 * @param resolver
	 */
	public void setResolver(GMLReferenceResolver resolver) {
		this.resolver = resolver;
	}

	public void setInternalResolver(GMLReferenceResolver internalResolver) {
		this.internalResolver = internalResolver;
	}

	public GMLReferenceResolver getInternalResolver() {
		return internalResolver;
	}

	/**
	 * Enables or disables lax parsing (disable syntactical checks).
	 * @param laxMode <code>true</code>, if syntacical issues shall be ignored,
	 * <code>false</code> otherwise
	 */
	public void setLaxMode(final boolean laxMode) {
		this.laxMode = laxMode;
	}

	/**
	 * Returns the state of lax parsing.
	 * @return <code>true</code>, if syntacical issues shall be ignored,
	 * <code>false</code> otherwise
	 */
	public boolean getLaxMode() {
		return laxMode;
	}

	/**
	 * Returns the deegree model representation for the GML object element event that the
	 * cursor of the underlying xml stream points to.
	 * @return deegree model representation for the current GML object element, never
	 * <code>null</code>
	 * @throws XMLStreamException
	 * @throws UnknownCRSException
	 * @throws XMLParsingException
	 */
	public GMLObject read() throws XMLStreamException, XMLParsingException, UnknownCRSException {

		GMLObject object = null;
		QName elName = xmlStream.getName();
		if (schema != null && schema.getFeatureType(elName) != null) {
			object = readFeature();
		}
		else {
			// TODO
			object = readGeometry();
		}
		return object;
	}

	/**
	 * Returns the deegree model representation for the GML feature element event that the
	 * cursor of the underlying xml stream points to.
	 * @return deegree model representation for the current GML feature element, never
	 * <code>null</code>
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 */
	public Feature readFeature() throws XMLStreamException, XMLParsingException, UnknownCRSException {
		return getFeatureReader().parseFeature(xmlStream, defaultCRS);
	}

	/**
	 * Returns the deegree model representation for the GML feature collection element
	 * event that the cursor of the underlying xml stream points to.
	 * <p>
	 * Please note that {@link #readFeatureCollectionStream()} should be preferred
	 * (especially for large feature collections), because it does not build and store all
	 * features in memory at once.
	 * </p>
	 * @return deegree model representation for the current GML feature collection
	 * element, never <code>null</code>
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 */
	public FeatureCollection readFeatureCollection()
			throws XMLStreamException, XMLParsingException, UnknownCRSException {
		return (FeatureCollection) getFeatureReader().parseFeature(xmlStream, defaultCRS);
	}

	/**
	 * Returns a {@link StreamFeatureCollection} that allows stream-based access to the
	 * members of the feature collection that the cursor of the given
	 * <code>XMLStreamReader</code> points at.
	 * <p>
	 * This method does not automatically consume all events from the underlying XML
	 * stream. Instead, it allows the caller to control the consumption by iterating over
	 * the features in the returned collection.
	 * </p>
	 * @return deegree model representation for the current GML feature collection
	 * element, never <code>null</code>
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 */
	public StreamFeatureCollection readFeatureCollectionStream()
			throws XMLStreamException, XMLParsingException, UnknownCRSException {
		return getFeatureReader().getFeatureStream(xmlStream, defaultCRS);
	}

	/**
	 * Returns the deegree model representation for the GML geometry element event that
	 * the cursor of the underlying xml stream points to.
	 * @return deegree model representation for the current GML geometry element, never
	 * <code>null</code>
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 */
	public Geometry readGeometryOrEnvelope() throws XMLStreamException, XMLParsingException, UnknownCRSException {
		return getGeometryReader().parseGeometryOrEnvelope(xmlStream, defaultCRS);
	}

	/**
	 * Returns the deegree model representation for the GML geometry element event that
	 * the cursor of the underlying xml stream points to.
	 * @return deegree model representation for the current GML geometry element, never
	 * <code>null</code>
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 */
	public Geometry readGeometry() throws XMLStreamException, XMLParsingException, UnknownCRSException {
		return getGeometryReader().parse(xmlStream, defaultCRS);
	}

	/**
	 * Returns the deegree model representation for the GML dictionary element event that
	 * the cursor of the underlying xml stream points to.
	 * @return deegree model representation for the current GML dictionary element, never
	 * <code>null</code>
	 * @throws XMLStreamException
	 */
	public Dictionary readDictionary() throws XMLStreamException {
		return getDictionaryReader().readDictionary();
	}

	/**
	 * Returns the deegree model representation for the GML crs element event that the
	 * cursor of the underlying xml stream points to.
	 * @return deegree model representation for the current GML crs element, never
	 * <code>null</code>
	 * @throws XMLStreamException
	 */
	public CRS readCRS() throws XMLStreamException {
		throw new UnsupportedOperationException("Reading of crs is not implemented yet.");
	}

	/**
	 * Returns the {@link GmlDocumentIdContext} that keeps track of objects, identifiers
	 * and references.
	 * @return the {@link GmlDocumentIdContext}, never <code>null</code>
	 */
	public GmlDocumentIdContext getIdContext() {
		return idContext;
	}

	/**
	 * Returns the underlying {@link XMLStreamReader}.
	 * @return the underlying {@link XMLStreamReader}, never <code>null</code>
	 */
	public XMLStreamReader getXMLReader() {
		return xmlStream;
	}

	/**
	 * Closes the underlying XML stream.
	 * @throws XMLStreamException
	 */
	public void close() throws XMLStreamException {
		xmlStream.close();
	}

	/**
	 * Returns a configured {@link GMLFeatureReader} instance for calling specific feature
	 * parsing methods.
	 * @return a configured {@link GMLFeatureReader} instance, never <code>null</code>
	 */
	public GMLFeatureReader getFeatureReader() {
		if (featureReader == null) {
			featureReader = new GMLFeatureReader(this);
		}
		return featureReader;
	}

	/**
	 * Returns a configured {@link GMLGeometryReader} instance for calling specific
	 * geometry parsing methods.
	 * @return a configured {@link GMLGeometryReader} instance, never <code>null</code>
	 */
	public GMLGeometryReader getGeometryReader() {
		if (geometryReader == null) {
			switch (version) {
				case GML_2: {
					geometryReader = new GML2GeometryReader(this);
					break;
				}
				case GML_30:
				case GML_31:
				case GML_32: {
					geometryReader = new GML3GeometryReader(this);
					break;
				}
			}
		}
		return geometryReader;
	}

	/**
	 * Returns a configured {@link GMLDictionaryReader} instance for calling specific
	 * dictionary parsing methods.
	 * @return a configured {@link GMLDictionaryReader} instance, never <code>null</code>
	 */
	public GMLDictionaryReader getDictionaryReader() {
		if (dictReader == null) {
			dictReader = new GMLDictionaryReader(version, xmlStream, idContext);
		}
		return dictReader;
	}

}
