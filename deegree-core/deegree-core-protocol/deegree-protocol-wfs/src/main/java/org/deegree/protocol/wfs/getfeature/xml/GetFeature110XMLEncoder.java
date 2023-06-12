/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.getfeature.xml;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.OGC_PREFIX;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_PREFIX;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XPathUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter110XMLEncoder;
import org.deegree.protocol.wfs.WFSConstants;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.jaxen.NamespaceContext;

/**
 * Encodes {@link GetFeature} objects according to WFS specification 1.1.0.
 *
 * @author <a href="mailto:Tschirner@bafg.de">Sven Tschirner</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetFeature110XMLEncoder {

	/**
	 * Serializes a {@link GetFeature}-object for WFS 1.1.0 GetFeature-requests
	 * @param getFeature the {@link GetFeature}-object to be serialized, must not be
	 * <code>null</code>
	 * @param namespaceBindings possible way to insert all additional namespaces for
	 * property-name usage within this GetFeature-request directly in the root element,
	 * can be <code>null</code>
	 * @param writer target of the xml stream, must not be <code>null</code>
	 * @throws XMLStreamException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 * @throws FilterEvaluationException
	 */
	public static void export(GetFeature getFeature, NamespaceBindings namespaceBindings, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException, FilterEvaluationException {

		writer.writeStartDocument();
		writer.writeStartElement(WFSConstants.WFS_PREFIX, "GetFeature", WFSConstants.WFS_NS);
		writer.writeNamespace(WFSConstants.WFS_PREFIX, WFSConstants.WFS_NS);
		writer.writeNamespace(CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS);
		writer.writeAttribute(CommonNamespaces.XSINS, "schemaLocation",
				"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd");
		if (namespaceBindings != null) {
			writeNamespaceDeclarations(namespaceBindings, writer);
		}

		/* write <GetFeature>-element attributes */

		Version version = getFeature.getVersion();
		if ((version != null)) {
			if (version.compareTo(new Version(1, 1, 0)) != 0) {
				throw new IllegalArgumentException(
						"Only WFS-GetFeature 1.1.0 serialization is supported by this encoder");
			}
			writer.writeAttribute("version", version.toString());
		}

		String handle = getFeature.getHandle();
		if ((handle != null) && (!handle.equals(""))) {
			writer.writeAttribute("handle", handle);
		}

		ResultType resultType = getFeature.getPresentationParams().getResultType();
		if (resultType != null) {
			writer.writeAttribute("resultType", resultType.toString().toLowerCase());
		}

		String outputFormat = getFeature.getPresentationParams().getOutputFormat();
		if ((outputFormat != null) && (!outputFormat.equals(""))) {
			writer.writeAttribute("outputFormat", outputFormat);
		}

		BigInteger maxFeatures = getFeature.getPresentationParams().getCount();
		if (maxFeatures != null) {
			writer.writeAttribute("maxFeatures", maxFeatures.toString());
		}

		String traverseXlinkDepth = getFeature.getResolveParams().getDepth();
		if ((traverseXlinkDepth != null) && (!traverseXlinkDepth.equals(""))) {
			writer.writeAttribute("traverseXlinkDepth", traverseXlinkDepth);
		}
		else { /* otherwise set this mandatory attribute to value '*' */
			writer.writeAttribute("traverseXlinkDepth", "*");
		}

		BigInteger resolveTimeout = getFeature.getResolveParams().getTimeout();
		if (resolveTimeout != null) {
			BigInteger traverseXlinkExpiry = resolveTimeout.divide(BigInteger.valueOf(60));
			writer.writeAttribute("traverseXlinkExpiry", "" + traverseXlinkExpiry);
		}

		/* write <query> child elements */
		List<Query> queries = getFeature.getQueries();
		if (queries != null) {
			for (Query nextQuery : queries) {
				if (nextQuery != null) {
					if (nextQuery instanceof FilterQuery) {
						export((FilterQuery) nextQuery, writer);
					}
					else {
						throw new IllegalArgumentException("Only WFS-GetFeature 1.1.0 XML serialization "
								+ "is supported by this encoder, so no KVP Queries");
					}
				}
				else {
					throw new IllegalArgumentException(
							"At least one query has to be declared for a valid WFS-GetFeature request");
				}
			}
		}
		else {
			throw new IllegalArgumentException(
					"At least one query has to be declared for a valid WFS-GetFeature request");
		}

		writer.writeEndElement();
	}

	/**
	 * Serializes a {@link FilterQuery}-object
	 * @param query the {@link FilterQuery}-object to be serialized
	 * @param writer target of the xml stream
	 * @throws XMLStreamException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 * @throws FilterEvaluationException
	 */
	private static void export(FilterQuery query, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException, FilterEvaluationException {
		writer.writeStartElement(WFSConstants.WFS_PREFIX, "Query", WFSConstants.WFS_NS);

		/* write attributes */
		String handle = query.getHandle();
		if ((handle != null) && (!handle.equals(""))) {
			writer.writeAttribute("handle", handle);
		}

		TypeName[] typeNames = query.getTypeNames();
		if ((typeNames.length == 0) || ((typeNames.length == 1) && (typeNames[0] == null))) {
			throw new IllegalArgumentException(
					"At least one type-name has to be specified for a valid WFS-GetFeature request");
		}
		else if (typeNames.length > 1) {
			throw new UnsupportedOperationException("Currently, join operations are not supported");
		}
		else { /* only one typeName specified for the current query */
			QName qname = typeNames[0].getFeatureTypeName();
			if ((qname.getNamespaceURI() != null) && (!qname.getNamespaceURI().equals(""))) {
				writer.writeNamespace(qname.getPrefix(), qname.getNamespaceURI());
				writer.writeAttribute("typeName", qname.getPrefix() + ":" + qname.getLocalPart());
			}
			else {
				writer.writeAttribute("typeName", typeNames[0].getFeatureTypeName().toString());
			}
		}

		String featureVersion = query.getFeatureVersion();
		if ((featureVersion != null) && (!featureVersion.equals(""))) {
			writer.writeAttribute("featureVersion", featureVersion);
		}

		ICRS srsName = query.getSrsName();
		if ((srsName != null)) {
			writer.writeAttribute("srsName", srsName.getName());
		}

		/* write child elements */
		ProjectionClause[] projectionClauses = query.getProjectionClauses();
		if (projectionClauses != null) {
			for (ProjectionClause projectionClause : projectionClauses) {
				if (projectionClause instanceof PropertyName) {
					PropertyName nextProperty = (PropertyName) projectionClause;
					if (nextProperty != null) {
						ResolveParams resolveParams = nextProperty.getResolveParams();
						if (resolveParams.getMode() == null && resolveParams.getDepth() == null
								&& resolveParams.getTimeout() == null) {
							QName qname = nextProperty.getPropertyName().getAsQName();
							if (qname != null) {
								writer.writeStartElement(WFS_PREFIX, "PropertyName", WFS_NS);
								writePropertyNameCharacters(nextProperty.getPropertyName(), writer);
								writer.writeEndElement();
							}
						}
						else {
							writer.writeStartElement(WFSConstants.WFS_PREFIX, "XlinkPropertyName", WFSConstants.WFS_NS);

							String traverseXlinkDepth = resolveParams.getDepth();
							BigInteger traverseXlinkExpiry = resolveParams.getTimeout();
							if (traverseXlinkExpiry != null) {
								traverseXlinkExpiry = traverseXlinkExpiry.divide(BigInteger.valueOf(60));
							}

							/*
							 * attribute traverseXlinkDepth is mandatory, must not be null
							 */
							if ((traverseXlinkDepth != null) && (!traverseXlinkDepth.equals(""))) {
								writer.writeAttribute("traverseXlinkDepth", traverseXlinkDepth);
							}
							else {
								writer.writeAttribute("traverseXlinkDepth", "*");
							}

							if (traverseXlinkExpiry != null) {
								writer.writeAttribute("traverseXlinkExpiry", traverseXlinkExpiry.toString());
							}

							writePropertyNameCharacters(nextProperty.getPropertyName(), writer);

							writer.writeEndElement();
						}
					}
				}
			}
		}

		// Function[] functions = query.getFunctions();
		// if ( functions != null ) {
		// for ( Function nextFunction : functions ) {
		// if ( nextFunction != null ) {
		// Filter110XMLEncoder.export( nextFunction, writer );
		// }
		// }
		// }

		if (query.getFilter() != null) {
			Filter110XMLEncoder.export(query.getFilter(), writer);
		}

		SortProperty[] sortProperties = query.getSortBy();
		if (sortProperties.length != 0) {

			writer.writeStartElement(OGC_PREFIX, "SortBy", OGCNS);
			writer.writeNamespace(OGC_PREFIX, OGCNS);

			for (SortProperty nextSortProperty : sortProperties) {
				if (nextSortProperty.getSortProperty() != null) {
					writer.writeStartElement(CommonNamespaces.OGC_PREFIX, "SortProperty", CommonNamespaces.OGCNS);
					Filter110XMLEncoder.export(nextSortProperty.getSortProperty(), writer);
					writer.writeStartElement(CommonNamespaces.OGC_PREFIX, "SortOrder", CommonNamespaces.OGCNS);
					writer.writeCharacters((nextSortProperty.getSortOrder() ? "ASC" : "DESC"));
					writer.writeEndElement();
					writer.writeEndElement();
				}
			}
			writer.writeEndElement();
		}
	}

	/**
	 * Writes property-names and declares corresponding namespaces inside a naming
	 * xml-element (e.g. <PropertyName> relating to {@link ValueReference} or
	 * <XlinkPropertyName> relating to {@link PropertyName})
	 * @param propertyName name of the property which encapsulates the characters and
	 * namespace-prefix-mappings which are serialized by this method
	 * @param writer target of the xml stream
	 * @throws XMLStreamException
	 * @throws FilterEvaluationException
	 */
	private static void writePropertyNameCharacters(ValueReference propertyName, XMLStreamWriter writer)
			throws XMLStreamException, FilterEvaluationException {

		if (propertyName.getAsQName() != null) { /* has just one element step */
			QName qname = propertyName.getAsQName();

			if ((qname.getNamespaceURI() != null) && (!qname.getNamespaceURI().equals(""))) {
				boolean prefixBound = (writer.getPrefix(qname.getNamespaceURI()) != null) ? true : false;
				if (prefixBound) {
					writer.writeCharacters(writer.getPrefix(qname.getNamespaceURI()) + ":" + qname.getLocalPart());
				}
				else {
					writer.writeNamespace(qname.getPrefix(), qname.getNamespaceURI());
					writer.writeCharacters(qname.getPrefix() + ":" + qname.getLocalPart());
				}
			}
			else {
				writer.writeCharacters(qname.getLocalPart());
			}

		}
		else { /* property is an xpath-expression */
			NamespaceContext namespaceContext = propertyName.getNsContext();
			/*
			 * with adjusted Deegree-NamespaceContext class there's a getter-method for
			 * the prefix/namespace-map
			 */
			if ((namespaceContext != null) && (namespaceContext instanceof NamespaceBindings)) {
				NamespaceBindings namespaceBindings = (NamespaceBindings) namespaceContext;
				Set<String> usedPrefixes = XPathUtils.extractPrefixes(propertyName.getAsXPath());
				NamespaceBindings usedNamespaceBindings = new NamespaceBindings();

				for (String usedPrefix : usedPrefixes) {
					if (namespaceBindings.translateNamespacePrefixToUri(usedPrefix) != null) {
						usedNamespaceBindings.addNamespace(usedPrefix,
								namespaceBindings.translateNamespacePrefixToUri(usedPrefix));
					}
					else {
						throw new FilterEvaluationException(
								"found prefix '" + usedPrefix + "' which is not bound to a namespace");
					}
				}

				writeNamespaceDeclarations(usedNamespaceBindings, writer);
			}

			writer.writeCharacters(propertyName.getAsText());
		}
	}

	/**
	 * attributes an open xml-element inside the {@link XMLStreamWriter} with
	 * namespaces-declarations passed by the parameter <code>namespaceContext</code>
	 * @param namespaceBindings contains the namespace-prefix mappings used for
	 * xpath-expressions
	 * @param writer target of the xml stream
	 * @throws XMLStreamException
	 */
	private static void writeNamespaceDeclarations(NamespaceBindings namespaceBindings, XMLStreamWriter writer)
			throws XMLStreamException {
		if (namespaceBindings != null) {
			Iterator<String> iterateOverPrefixes = namespaceBindings.getPrefixes();

			while (iterateOverPrefixes.hasNext()) {

				String nextPrefix = iterateOverPrefixes.next();

				if (!nextPrefix.equalsIgnoreCase(CommonNamespaces.XMLNS_PREFIX)) {
					boolean prefixBound = (writer.getPrefix(namespaceBindings.getNamespaceURI(nextPrefix)) != null)
							? true : false;
					if (!prefixBound) {
						writer.writeNamespace(nextPrefix, namespaceBindings.getNamespaceURI(nextPrefix));
					}
				}
			}
		}
	}

}
