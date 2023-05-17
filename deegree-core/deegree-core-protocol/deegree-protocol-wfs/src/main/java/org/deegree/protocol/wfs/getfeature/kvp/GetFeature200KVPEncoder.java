/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.protocol.wfs.getfeature.kvp;

import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.CommonNamespaces.FES_PREFIX;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ResolveMode;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter200XMLEncoder;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wfs.WFSVersion;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.AdHocQuery;
import org.deegree.protocol.wfs.query.BBoxQuery;
import org.deegree.protocol.wfs.query.FeatureIdQuery;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.StoredQuery;

/**
 * Encodes {@link GetFeature} objects as KVP Parameters according to WFS specification
 * 2.0.0.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetFeature200KVPEncoder {

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###.########",
			new DecimalFormatSymbols(Locale.ENGLISH));

	private static final NamespaceBindings NS_CONTEXT = CommonNamespaces.getNamespaceContext();

	static {
		NS_CONTEXT.addNamespace(FES_PREFIX, FES_20_NS);
	}

	/**
	 * Encodes a GetFeature request to KVP Parameters. Currently only one query per
	 * GetFeature request is supported.
	 * @param getFeature the {@link GetFeature}-object to serialize, never
	 * <code>null</code>
	 * @return a KVP list of the encoded GetFeature instance, never <code>null</code>
	 * @throws UnsupportedEncodingException if an exception occurred during export of a
	 * filter
	 * @throws XMLStreamException if an exception occurred during export of a filter
	 * @throws FactoryConfigurationError if an exception occurred during export of a
	 * filter
	 * @throws UnknownCRSException if an exception occurred during export of a filter
	 * @throws TransformationException if an exception occurred during export of a filter
	 */
	public static Map<String, String> export(GetFeature getFeature) throws UnsupportedEncodingException,
			XMLStreamException, FactoryConfigurationError, UnknownCRSException, TransformationException {
		if (!WFSVersion.WFS_200.getOGCVersion().equals(getFeature.getVersion())) {
			String failure = "Serialization of other versions than 2.0.0 are currently not supported by this encoder!";
			throw new IllegalArgumentException(failure);
		}
		Map<String, String> kvp = new LinkedHashMap<String, String>();
		exportBaseParams(kvp);
		exportQueryParams(getFeature, kvp);
		exportPresentationParameters(getFeature, kvp);
		exportResolveParameters(getFeature, kvp);
		return kvp;
	}

	private static void exportBaseParams(Map<String, String> kvp) {
		kvp.put("SERVICE", "WFS");
		kvp.put("VERSION", "2.0.0");
		kvp.put("REQUEST", "GetFeature");
	}

	private static void exportQueryParams(GetFeature getFeature, Map<String, String> kvp)
			throws UnsupportedEncodingException, XMLStreamException, FactoryConfigurationError, UnknownCRSException,
			TransformationException {
		List<Query> queries = getFeature.getQueries();
		if (queries == null || queries.size() == 0)
			throw new IllegalArgumentException("At least one query is required!");
		if (queries.size() > 1)
			throw new IllegalArgumentException("Currently only one query per request is supported!");

		Query query = queries.get(0);
		if (query instanceof StoredQuery) {
			exportStoredQuery(((StoredQuery) queries.get(0)), kvp);
		}
		else if (query instanceof AdHocQuery) {
			exportAdHocQuery((AdHocQuery) query, kvp);
		}
		else {
			throw new IllegalArgumentException(
					"Query class " + query.getClass() + " is not supported by this encoder!");
		}
	}

	private static void exportPresentationParameters(GetFeature getFeature, Map<String, String> kvp) {
		StandardPresentationParams presentationParams = getFeature.getPresentationParams();
		if (presentationParams != null) {
			ResultType resultType = getFeature.getPresentationParams().getResultType();
			if (resultType != null) {
				kvp.put("RESULTTYPE", presentationParams.getResultType().toString().toLowerCase());
			}

			String outputFormat = getFeature.getPresentationParams().getOutputFormat();
			if ((outputFormat != null) && (!outputFormat.equals(""))) {
				kvp.put("OUTPUTFORMAT", outputFormat);
			}

			BigInteger maxFeatures = getFeature.getPresentationParams().getCount();
			if (maxFeatures != null) {
				kvp.put("COUNT", maxFeatures.toString());
			}

			BigInteger startIndex = getFeature.getPresentationParams().getStartIndex();
			if (startIndex != null) {
				kvp.put("STARTINDEX", startIndex.toString());
			}
		}
	}

	private static void exportResolveParameters(GetFeature getFeature, Map<String, String> kvp) {
		ResolveParams resolveParams = getFeature.getResolveParams();

		if (resolveParams != null) {
			ResolveMode resolveMode = resolveParams.getMode();
			if (resolveMode != null) {
				kvp.put("RESOLVE", resolveMode.name().toLowerCase());
			}
			String resolveDepth = getFeature.getResolveParams().getDepth();
			if (resolveDepth != null && !resolveDepth.equals("")) {
				kvp.put("RESOLVEDEPTH", resolveDepth);
			}
			BigInteger resolveTimeout = getFeature.getResolveParams().getTimeout();
			if (resolveTimeout != null) {
				kvp.put("RESOLVETIMEOUT", resolveTimeout.toString());
			}
		}
	}

	private static void exportStoredQuery(StoredQuery storedQuery, Map<String, String> kvp) {
		kvp.put("STOREDQUERY_ID", storedQuery.getId());
		Map<String, OMElement> storedQueryParams = storedQuery.getParams();
		for (Entry<String, OMElement> storedQueryParam : storedQueryParams.entrySet()) {
			String key = storedQueryParam.getKey().toUpperCase();
			if (!kvp.containsKey(key)) {
				String value = retrieveLiteral(storedQueryParam.getValue());
				kvp.put(key, value);
			}
		}
	}

	private static void exportAdHocQuery(AdHocQuery query, Map<String, String> kvp) throws XMLStreamException,
			FactoryConfigurationError, UnknownCRSException, TransformationException, UnsupportedEncodingException {
		exportTypeNames(kvp, query.getTypeNames());
		exportSrsName(query, kvp);
		exportSortBy(query, kvp);
		exportProjectionClauses(query, kvp);

		if (query instanceof BBoxQuery) {
			exportBbox(query, kvp);
		}
		else if (query instanceof FeatureIdQuery) {
			exportResourceIds((FeatureIdQuery) query, kvp);
		}
		else if (query instanceof FilterQuery) {
			exportFilter((FilterQuery) query, kvp);
		}
		else {
			throw new IllegalArgumentException(
					"Query class " + query.getClass() + " is not supported by this encoder!");
		}
	}

	private static void exportTypeNames(Map<String, String> kvp, TypeName[] typeNames) {
		if (typeNames != null && typeNames.length > 0) {
			StringBuilder typeNameBuilder = new StringBuilder();
			StringBuilder aliasBuilder = new StringBuilder();
			Map<String, String> prefixToNamespaceUrls = new HashMap<String, String>();
			boolean atLeastOneAliasIsSet = isAtLeastOneAliasSet(typeNames);
			for (TypeName typeName : typeNames) {
				if (typeNameBuilder.length() > 0) {
					typeNameBuilder.append(',');
					aliasBuilder.append(',');
				}
				String alias = typeName.getAlias();
				if (alias != null)
					aliasBuilder.append(alias);
				QName featureTypeName = typeName.getFeatureTypeName();
				String prefix = featureTypeName.getPrefix();
				boolean isSchemaElement = typeName.isSchemaElement();
				if (isSchemaElement)
					typeNameBuilder.append("schema-element(");
				if (prefix != null && prefix.length() > 0)
					typeNameBuilder.append(prefix).append(':');
				typeNameBuilder.append(featureTypeName.getLocalPart());
				if (isSchemaElement)
					typeNameBuilder.append(')');

				String namespaceUri = featureTypeName.getNamespaceURI();
				if (prefix != null && prefix.length() > 0 && namespaceUri != null && namespaceUri.length() > 0)
					prefixToNamespaceUrls.put(prefix, namespaceUri);
			}
			kvp.put("TYPENAMES", typeNameBuilder.toString());
			if (atLeastOneAliasIsSet)
				kvp.put("ALIASES", aliasBuilder.toString());
			if (prefixToNamespaceUrls.size() > 0)
				kvp.put("NAMESPACES", retrieveNamespaces(prefixToNamespaceUrls));
		}
	}

	private static void exportSrsName(AdHocQuery query, Map<String, String> kvp) {
		ICRS srsName = query.getSrsName();
		if (srsName != null)
			kvp.put("SRSNAME", srsName.getName());
	}

	private static void exportSortBy(AdHocQuery query, Map<String, String> kvp) {
		SortProperty[] sortBys = query.getSortBy();
		if (sortBys != null && sortBys.length > 0) {
			StringBuilder sortByBuilder = new StringBuilder();
			for (SortProperty sortBy : sortBys) {
				if (sortByBuilder.length() > 0)
					sortByBuilder.append(',');
				boolean isAsc = sortBy.getSortOrder();
				ValueReference sortProperty = sortBy.getSortProperty();

				sortByBuilder.append(sortProperty.getAsText());
				sortByBuilder.append(' ');
				if (isAsc)
					sortByBuilder.append("ASC");
				else
					sortByBuilder.append("DESC");
			}
			kvp.put("SORTBY", sortByBuilder.toString());
		}
	}

	private static void exportProjectionClauses(AdHocQuery query, Map<String, String> kvp) {
		ProjectionClause[] projectionClauses = query.getProjectionClauses();
		if (projectionClauses != null && projectionClauses.length > 0) {
			StringBuilder projectionClauseBuilder = new StringBuilder();
			for (ProjectionClause projectionClause : projectionClauses) {
				if (projectionClause instanceof PropertyName) {
					if (projectionClauseBuilder.length() > 0)
						projectionClauseBuilder.append(',');
					ValueReference propertyName = ((PropertyName) projectionClause).getPropertyName();
					projectionClauseBuilder.append(propertyName.getAsText());
				}
			}
			kvp.put("PROPERTYNAME", projectionClauseBuilder.toString());
		}
	}

	private static void exportBbox(AdHocQuery query, Map<String, String> kvp) {
		Envelope bbox = ((BBoxQuery) query).getBBox();
		if (bbox != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(DECIMAL_FORMAT.format(bbox.getMin().get0())).append(",");
			sb.append(DECIMAL_FORMAT.format(bbox.getMin().get1())).append(",");
			sb.append(DECIMAL_FORMAT.format(bbox.getMax().get0())).append(",");
			sb.append(DECIMAL_FORMAT.format(bbox.getMax().get1()));
			if (bbox.getCoordinateSystem() != null)
				sb.append(',').append(bbox.getCoordinateSystem().getName());
			kvp.put("BBOX", sb.toString());
		}
	}

	private static void exportResourceIds(FeatureIdQuery query, Map<String, String> kvp) {
		String[] resourceIds = query.getFeatureIds();
		if (resourceIds != null && resourceIds.length > 0) {
			StringBuilder resourceIdBuilder = new StringBuilder();
			for (String resourceId : resourceIds) {
				if (resourceIdBuilder.length() > 0)
					resourceIdBuilder.append(',');
				resourceIdBuilder.append(resourceId);
			}
			kvp.put("RESOURCEID", resourceIdBuilder.toString());
		}
	}

	private static void exportFilter(FilterQuery query, Map<String, String> kvp) throws XMLStreamException,
			FactoryConfigurationError, UnknownCRSException, TransformationException, UnsupportedEncodingException {
		Filter filter = query.getFilter();
		if (filter != null) {
			kvp.put("FILTER", retrieveFilter(filter));
		}
	}

	private static String retrieveFilter(Filter filter) throws XMLStreamException, FactoryConfigurationError,
			UnknownCRSException, TransformationException, UnsupportedEncodingException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter xmlWriter = null;
		try {
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(bos, "UTF-8");
			Filter200XMLEncoder.export(filter, xmlWriter);
		}
		finally {
			if (xmlWriter != null)
				xmlWriter.close();
		}
		return bos.toString("UTF-8");
	}

	private static String retrieveLiteral(OMElement value) {
		XMLAdapter adapter = new XMLAdapter();
		XPath xpath = new XPath("//" + FES_PREFIX + ":Literal", NS_CONTEXT);
		return adapter.getNodeAsString(value, xpath, null);
	}

	private static String retrieveNamespaces(Map<String, String> prefixToNamespaceUrls) {
		StringBuilder namespacesBuilder = new StringBuilder();
		for (Entry<String, String> prefixToNamespaceUrl : prefixToNamespaceUrls.entrySet()) {
			if (namespacesBuilder.length() > 0)
				namespacesBuilder.append(',');
			namespacesBuilder.append("xmlns(");
			namespacesBuilder.append(prefixToNamespaceUrl.getKey());
			namespacesBuilder.append(',');
			namespacesBuilder.append(prefixToNamespaceUrl.getValue());
			namespacesBuilder.append(')');
		}
		return namespacesBuilder.toString();
	}

	private static boolean isAtLeastOneAliasSet(TypeName[] typeNames) {
		for (TypeName typeName : typeNames) {
			String alias = typeName.getAlias();
			if (alias != null && alias.length() > 0)
				return true;
		}
		return false;
	}

}