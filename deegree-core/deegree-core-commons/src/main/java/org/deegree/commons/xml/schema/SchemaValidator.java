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

package org.deegree.commons.xml.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.apache.xerces.xs.LSInputList;
import org.deegree.commons.proxy.ProxySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;

/**
 * Provides utility methods for the easy validation of XML instance documents against XML
 * schemas and for the validation of XML schema documents.
 * <p>
 * <h3>Validation of instance documents</h3> The XML schemas are either determined from
 * the <code>xsi:schemaLocation</code> attribute of the document or may be explicitly
 * specified. The validator uses the {@link RedirectingEntityResolver}, so OGC core
 * schemas are not fetched over the network, but loaded from a local copy.
 * </p>
 * <h3>Validation of schema documents</h3> The validator uses the
 * {@link RedirectingEntityResolver}, so OGC core schemas are not fetched over the
 * network, but loaded from a local copy.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SchemaValidator {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaValidator.class);

	/** Namespaces feature id (http://xml.org/sax/features/namespaces). */
	private static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

	/** Validation feature id (http://xml.org/sax/features/validation). */
	private static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

	/**
	 * Schema validation feature id (http://apache.org/xml/features/validation/schema).
	 */
	private static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

	/**
	 * Schema full checking feature id
	 * (http://apache.org/xml/features/validation/schema-full-checking).
	 */
	private static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

	/**
	 * Honour all schema locations feature id
	 * (http://apache.org/xml/features/honour-all-schemaLocations).
	 */
	private static final String HONOUR_ALL_SCHEMA_LOCATIONS_ID = "http://apache.org/xml/features/honour-all-schemaLocations";

	/**
	 * Validates the specified XML instance document according to the contained schema
	 * references ( <code>xsi:schemaLocation</code> attribute) and/or to the explicitly
	 * specified schema references.
	 * @param source provides the XML document to be validated, must not be null
	 * @param schemaUris URIs of schema documents to be considered, can be null (only the
	 * <code>xsi:schemaLocation</code> attribute is considered then)
	 * @return list of validation events (errors/warnings) that occured, never null, size
	 * of 0 means valid document
	 */
	public static List<SchemaValidationEvent> validate(InputStream source, String... schemaUris) {
		return validate(new XMLInputSource(null, null, null, source, null), schemaUris);
	}

	/**
	 * Validates the specified XML instance document according to the contained schema
	 * references ( <code>xsi:schemaLocation</code> attribute) and/or to the explicitly
	 * specified schema references.
	 * @param url provides the XML document to be validated, must not be null
	 * @param schemaUris URIs of schema documents to be considered, can be null (only the
	 * <code>xsi:schemaLocation</code> attribute is considered then)
	 * @return list of validation events (errors/warnings) that occured, never null, size
	 * of 0 means valid document
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static List<SchemaValidationEvent> validate(String url, String... schemaUris)
			throws MalformedURLException, IOException {
		InputStream is = ProxySettings.openURLConnection(new URL(url), null, null).getInputStream();
		return validate(new XMLInputSource(null, null, null, is, null), schemaUris);
	}

	/**
	 * Validates the specified XML instance document according to the contained schema
	 * references ( <code>xsi:schemaLocation</code> attribute) and/or to explicitly
	 * specified schema references.
	 * @param source provides the document to be validated, must not be <code>null</code>
	 * @param schemaUris URIs of schema documents to be considered, can be
	 * <code>null</code> (only the <code>xsi:schemaLocation</code> attribute is considered
	 * then)
	 * @return list of validation events (errors/warnings) that occurred, never
	 * <code>null</code>, size of 0 means valid document
	 */
	public static List<SchemaValidationEvent> validate(XMLInputSource source, String... schemaUris) {
		final List<SchemaValidationEvent> errors = new LinkedList<SchemaValidationEvent>();

		try {
			RedirectingEntityResolver resolver = new RedirectingEntityResolver();
			if (schemaUris != null) {
				for (int i = 0; i < schemaUris.length; i++) {
					schemaUris[i] = resolver.redirect(schemaUris[i]);
				}
			}
			GrammarPool grammarPool = (schemaUris == null ? null : GrammarPoolManager.getGrammarPool(schemaUris));
			XMLParserConfiguration parserConfig = createValidatingParser(new RedirectingEntityResolver(), grammarPool);
			parserConfig.setErrorHandler(new XMLErrorHandler() {
				@SuppressWarnings("synthetic-access")
				@Override
				public void error(String domain, String key, XMLParseException e) throws XNIException {
					errors.add(new SchemaValidationEvent(domain, key, e));
				}

				@SuppressWarnings("synthetic-access")
				@Override
				public void fatalError(String domain, String key, XMLParseException e) throws XNIException {
					errors.add(new SchemaValidationEvent(domain, key, e));
				}

				@SuppressWarnings("synthetic-access")
				@Override
				public void warning(String domain, String key, XMLParseException e) throws XNIException {
					errors.add(new SchemaValidationEvent(domain, key, e));
				}
			});
			parserConfig.parse(source);
		}
		catch (Exception e) {
			errors.add(new SchemaValidationEvent(e));
		}
		return errors;
	}

	/**
	 * Validates the specified XML schema document, additionally in conjunction with more
	 * schemas.
	 * @param inputSchemaUri provides the XML schema document to be validated, must not be
	 * null
	 * @param additionalUris additional schema documents to be considered, can be null
	 * @return list of validation events (errors/warnings) that occured, never null, size
	 * of 0 means valid document
	 */
	public static List<String> validateSchema(String inputSchemaUri, String... additionalUris) {
		LSInput input = new DOMInputImpl(null, inputSchemaUri, null);
		LSInput[] additionalSchemas = new LSInput[additionalUris.length];
		for (int i = 0; i < additionalUris.length; i++) {
			additionalSchemas[i] = new DOMInputImpl(null, additionalUris[i], null);
		}
		return validateSchema(input, additionalSchemas);
	}

	/**
	 * Validates the specified XML schema document, additionally in conjunction with more
	 * schemas.
	 * @param inputSchema provides the XML schema document to be validated, must not be
	 * null
	 * @param additionalUris additional schema documents to be considered, can be null
	 * @return list of validation events (errors/warnings) that occured, never null, size
	 * of 0 means valid document
	 */
	public static List<String> validateSchema(InputStream inputSchema, String... additionalUris) {
		LSInput input = new DOMInputImpl(null, null, null, inputSchema, null);
		LSInput[] additionalSchemas = new LSInput[additionalUris.length];
		for (int i = 0; i < additionalUris.length; i++) {
			additionalSchemas[i] = new DOMInputImpl(null, additionalUris[i], null);
		}
		return validateSchema(input, additionalSchemas);
	}

	/**
	 * Validates the specified XML schema document, additionally in conjunction with more
	 * schemas.
	 * @param inputSchema provides the XML schema document to be validated, must not be
	 * null
	 * @param additionalSchemas additional schema documents to be considered, can be null
	 * @return list of validation events (errors/warnings) that occured, never null, size
	 * of 0 means valid document
	 */
	public static List<String> validateSchema(LSInput inputSchema, LSInput... additionalSchemas) {

		final List<String> errors = new LinkedList<String>();

		XMLSchemaLoader schemaLoader = new XMLSchemaLoader();
		schemaLoader.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, true);
		// NOTE: don't set to true, or validation of WFS GetFeature responses will fail
		// (Xerces error?)!
		schemaLoader.setFeature(HONOUR_ALL_SCHEMA_LOCATIONS_ID, false);
		schemaLoader.setEntityResolver(new RedirectingEntityResolver());

		schemaLoader.setErrorHandler(new XMLErrorHandler() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void error(String domain, String key, XMLParseException e) throws XNIException {
				LOG.debug("Encountered error: " + toString(e));
				errors.add("Error: " + toString(e));
			}

			@SuppressWarnings("synthetic-access")
			@Override
			public void fatalError(String domain, String key, XMLParseException e) throws XNIException {
				LOG.debug("Encountered fatal error: " + toString(e));
				errors.add("Fatal error: " + toString(e));
			}

			@SuppressWarnings("synthetic-access")
			@Override
			public void warning(String domain, String key, XMLParseException e) throws XNIException {
				LOG.debug("Encountered warning: " + toString(e));
				errors.add("Warning: " + toString(e));
			}

			private String toString(XMLParseException e) {
				String s = e.getLocalizedMessage();
				s += " (line: " + e.getLineNumber() + ", column: " + e.getColumnNumber();
				s += e.getExpandedSystemId() != null ? ", SystemID: '" + e.getExpandedSystemId() + "')" : ")";
				return s;
			}
		});

		schemaLoader.loadInputList(new LSInputListImpl(inputSchema, additionalSchemas));
		return errors;
	}

	private static XMLParserConfiguration createValidatingParser(XMLEntityResolver entityResolver,
			GrammarPool grammarPool) throws XNIException {

		XMLParserConfiguration parserConfiguration = null;
		if (grammarPool == null) {
			parserConfiguration = new XIncludeAwareParserConfiguration();
		}
		else {
			parserConfiguration = new XIncludeAwareParserConfiguration(grammarPool.getSymbolTable(), grammarPool);
		}
		parserConfiguration.setFeature(NAMESPACES_FEATURE_ID, true);
		parserConfiguration.setFeature(VALIDATION_FEATURE_ID, true);
		parserConfiguration.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
		parserConfiguration.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, true);
		// NOTE: don't set to true, or validation of WFS GetFeature responses will fail
		// (Xerces error?)!
		parserConfiguration.setFeature(HONOUR_ALL_SCHEMA_LOCATIONS_ID, false);
		if (entityResolver != null) {
			parserConfiguration.setEntityResolver(entityResolver);
		}
		return parserConfiguration;
	}

}

/**
 * Simple <code>LSInputList</code> implementation.
 * <p>
 * Implements List to be already prepared for switch to Xerces 2.10 series.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
class LSInputListImpl implements LSInputList, List {

	private List<LSInput> inputs = new ArrayList<LSInput>();

	/**
	 * Creates a new {@link LSInputListImpl} instance.
	 * @param inputs inputs, must not be null
	 */
	LSInputListImpl(LSInput[] inputs) {
		this.inputs.addAll(Arrays.asList(inputs));
	}

	/**
	 * Creates a new {@link LSInputListImpl} instance.
	 * @param input first input, must not be null
	 * @param additionalInputs additional inputs, may be null or empty
	 */
	LSInputListImpl(LSInput input, LSInput... additionalInputs) {
		inputs.add(input);
		inputs.addAll(Arrays.asList(additionalInputs));
	}

	@Override
	public int getLength() {
		return inputs.size();
	}

	@Override
	public LSInput item(int i) {
		return inputs.get(i);
	}

	@Override
	public int size() {
		return inputs.size();
	}

	@Override
	public boolean isEmpty() {
		return inputs.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return inputs.contains(o);
	}

	@Override
	public Iterator<LSInput> iterator() {
		return inputs.iterator();
	}

	@Override
	public Object[] toArray() {
		return inputs.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return inputs.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return inputs.remove(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		return inputs.containsAll(c);
	}

	@Override
	public boolean addAll(Collection c) {
		return inputs.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection c) {
		return inputs.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection c) {
		return inputs.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
		return inputs.retainAll(c);
	}

	@Override
	public void clear() {
		inputs.clear();
	}

	@Override
	public boolean equals(Object o) {
		return inputs.equals(o);
	}

	@Override
	public int hashCode() {
		return inputs.hashCode();
	}

	@Override
	public LSInput get(int index) {
		return inputs.get(index);
	}

	public LSInput set(int index, LSInput element) {
		return inputs.set(index, element);
	}

	public void add(int index, LSInput element) {
		inputs.add(index, element);
	}

	@Override
	public LSInput remove(int index) {
		return inputs.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return inputs.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return inputs.lastIndexOf(o);
	}

	@Override
	public ListIterator<LSInput> listIterator() {
		return inputs.listIterator();
	}

	@Override
	public ListIterator<LSInput> listIterator(int index) {
		return inputs.listIterator(index);
	}

	@Override
	public List<LSInput> subList(int fromIndex, int toIndex) {
		return inputs.subList(fromIndex, toIndex);
	}

	@Override
	public boolean add(Object e) {
		return inputs.add((LSInput) e);
	}

	@Override
	public Object set(int index, Object element) {
		return inputs.set(index, (LSInput) element);
	}

	@Override
	public void add(int index, Object element) {
		inputs.add(index, (LSInput) element);
	}

}