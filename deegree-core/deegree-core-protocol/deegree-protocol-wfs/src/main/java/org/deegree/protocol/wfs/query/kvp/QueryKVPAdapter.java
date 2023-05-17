/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.query.kvp;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.MISSING_PARAMETER_VALUE;
import static org.deegree.commons.utils.kvp.KVPUtils.getBigInt;
import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.protocol.wfs.getfeature.ResultType.HITS;
import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ResolveMode;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter200XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.ows.OWSCommonKVPAdapter;
import org.deegree.protocol.wfs.AbstractWFSRequestKVPAdapter;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.BBoxQuery;
import org.deegree.protocol.wfs.query.FeatureIdQuery;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.StoredQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryKVPAdapter extends AbstractWFSRequestKVPAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(QueryKVPAdapter.class);

	protected static StandardPresentationParams parseStandardPresentationParameters100(Map<String, String> kvpUC) {

		// optional: MAXFEATURES
		BigInteger maxFeatures = KVPUtils.getBigInt(kvpUC, "MAXFEATURES", null);

		// ??? not in 1.0.0 spec, but CITE 1.0.0 test
		// (wfs:test1.0.0-basic-getfeature-get-3) suggests this parameter
		String outputFormat = kvpUC.get("OUTPUTFORMAT");

		return new StandardPresentationParams(null, maxFeatures, null, outputFormat);
	}

	protected static StandardPresentationParams parseStandardPresentationParameters110(Map<String, String> kvpUC) {

		// optional: 'OUTPUTFORMAT'
		String outputFormat = kvpUC.get("OUTPUTFORMAT");

		// optional: 'RESULTTYPE'
		ResultType resultType = RESULTS;
		if (kvpUC.get("RESULTTYPE") != null && kvpUC.get("RESULTTYPE").equalsIgnoreCase("hits")) {
			resultType = HITS;
		}

		// optional: MAXFEATURES
		BigInteger maxFeatures = KVPUtils.getBigInt(kvpUC, "MAXFEATURES", null);

		return new StandardPresentationParams(null, maxFeatures, resultType, outputFormat);
	}

	protected static StandardPresentationParams parseStandardPresentationParameters200(Map<String, String> kvpUC) {

		// optional: STARTINDEX
		BigInteger startIndex = getBigInt(kvpUC, "STARTINDEX", null);

		// optional: COUNT
		BigInteger count = getBigInt(kvpUC, "COUNT", null);

		// optional: OUTPUTFORMAT
		String outputFormat = kvpUC.get("OUTPUTFORMAT");

		// optional: RESULTTYPE
		ResultType resultType = null;
		String resultTypeStr = kvpUC.get("RESULTTYPE");
		if ("hits".equalsIgnoreCase(resultTypeStr)) {
			resultType = ResultType.HITS;
		}
		else if ("results".equalsIgnoreCase(resultTypeStr)) {
			resultType = ResultType.RESULTS;
		}

		return new StandardPresentationParams(startIndex, count, resultType, outputFormat);
	}

	protected static ResolveParams parseStandardResolveParameters110(Map<String, String> kvpUC) {

		// optional: 'TRAVERSEXLINKDEPTH'
		String traverseXlinkDepth = kvpUC.get("TRAVERSEXLINKDEPTH");

		// optional: 'TRAVERSEXLINKEXPIRY'
		BigInteger resolveTimeout = null;
		BigInteger traverseXlinkExpiry = getBigInt(kvpUC, "TRAVERSEXLINKEXPIRY", null);
		if (traverseXlinkExpiry != null) {
			resolveTimeout = BigInteger.valueOf(60).multiply(traverseXlinkExpiry);
		}

		return new ResolveParams(null, traverseXlinkDepth, resolveTimeout);
	}

	protected static ResolveParams parseStandardResolveParameters200(Map<String, String> kvpUC) {

		// optional: RESOLVE
		ResolveMode resolve = null;
		String resolveString = kvpUC.get("RESOLVE");
		if (resolveString != null) {
			if (resolveString.equalsIgnoreCase("local")) {
				resolve = ResolveMode.LOCAL;
			}
			else if (resolveString.equalsIgnoreCase("remote")) {
				resolve = ResolveMode.REMOTE;
			}
			else if (resolveString.equalsIgnoreCase("none")) {
				resolve = ResolveMode.NONE;
			}
			else if (resolveString.equalsIgnoreCase("all")) {
				resolve = ResolveMode.ALL;
			}
			else {
				LOG.warn("Invalid value (='{}') for resolve parameter.", resolveString);
			}
		}

		// optional: RESOLVEDEPTH
		String resolveDepth = kvpUC.get("RESOLVEDEPTH");

		// optional: RESOLVETIMEOUT
		BigInteger resolveTimeout = getBigInt(kvpUC, "RESOLVETIMEOUT", null);

		return new ResolveParams(resolve, resolveDepth, resolveTimeout);
	}

	/**
	 * @param kvpUC never <code>null</code>
	 * @param resolveParams resolve params from the query, may be <code>null</code>
	 * @return the parsed queries, never <code>null</code>
	 * @throws Exception
	 */
	protected static List<Query> parseQueries200(Map<String, String> kvpUC, ResolveParams resolveParams)
			throws Exception {

		List<Query> queries = null;
		if (kvpUC.containsKey("STOREDQUERY_ID")) {
			queries = parseStoredQuery200(kvpUC);
		}
		else {
			queries = parseAdhocQueries200(kvpUC, resolveParams);
		}
		return queries;
	}

	private static List<Query> parseAdhocQueries200(Map<String, String> kvpUC, ResolveParams resolveParams)
			throws Exception {

		// optional: 'NAMESPACE'
		Map<String, String> nsBindings = extractNamespaceBindings200(kvpUC.get("NAMESPACES"));
		NamespaceBindings nsContext = new NamespaceBindings();
		if (nsBindings != null) {
			for (String key : nsBindings.keySet()) {
				nsContext.addNamespace(key, nsBindings.get(key));
			}
		}

		// optional: 'BBOX'
		Envelope bbox = null;
		String bboxStr = kvpUC.get("BBOX");
		if (bboxStr != null) {
			bbox = OWSCommonKVPAdapter.parseBBox(bboxStr, null);
		}

		int numQueries = -1;

		// optional: ALIASES (can be a list of lists)
		List<String[]> aliasesList = new ArrayList<String[]>();
		if (kvpUC.get("ALIASES") != null) {
			List<String> params = KVPUtils.splitLists(kvpUC.get("ALIASES"));
			if (numQueries != -1 && params.size() != numQueries) {
				String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
				throw new OWSException(msg, MISSING_PARAMETER_VALUE);
			}
			else {
				numQueries = params.size();
			}
			for (String param : params) {
				String[] a = StringUtils.split(param, ",");
				aliasesList.add(a);
			}
		}

		// mandatory (optional if RESOURCEID is present): TYPENAMES (can be a list of
		// lists)
		List<TypeName[]> typeNamesList = new ArrayList<TypeName[]>();
		if (kvpUC.get("TYPENAMES") != null || kvpUC.get("TYPENAME") != null) {
			String param = kvpUC.get("TYPENAMES");
			if (param == null) {
				param = kvpUC.get("TYPENAME");
			}
			List<String> params = KVPUtils.splitLists(param);
			if (numQueries != -1 && params.size() != numQueries) {
				String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
				throw new OWSException(msg, MISSING_PARAMETER_VALUE);
			}
			else {
				numQueries = params.size();
			}
			for (int i = 0; i < numQueries; i++) {
				String[] alias = null;
				if (!aliasesList.isEmpty()) {
					alias = aliasesList.get(i);
				}
				String typeNameStr = params.get(i);
				String[] tokens = StringUtils.split(typeNameStr, ",");
				if (alias != null && alias.length != tokens.length) {
					String msg = "Number of entries in 'ALIASES' and 'TYPENAMES' parameters does not match.";
					throw new OWSException(msg, INVALID_PARAMETER_VALUE, "aliases");
				}
				TypeName[] typeName = new TypeName[tokens.length];
				for (int j = 0; j < tokens.length; j++) {
					String a = alias != null ? alias[j] : null;
					String token = tokens[j];
					if (token.startsWith("schema-element(") && token.endsWith(")")) {
						String prefixedName = token.substring(15, token.length() - 1);
						QName qName = resolveQName(prefixedName, nsContext);
						typeName[j] = new TypeName(qName, a, true);
					}
					else {
						QName qName = resolveQName(token, nsContext);
						typeName[j] = new TypeName(qName, a, false);
					}
				}
				typeNamesList.add(typeName);
			}
		}

		// optional: SRSNAME (can be a list of values)
		List<ICRS> srsNames = new ArrayList<ICRS>();
		if (kvpUC.get("SRSNAME") != null) {
			List<String> params = KVPUtils.splitLists(kvpUC.get("SRSNAME"));
			if (numQueries != -1 && params.size() != numQueries) {
				String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
				throw new OWSException(msg, MISSING_PARAMETER_VALUE);
			}
			else {
				numQueries = params.size();
			}
			for (String param : params) {
				srsNames.add(CRSManager.getCRSRef(param));
			}
		}

		// optional: PROPERTYNAME (can be a list of lists)
		List<PropertyName[]> projectionClausesList = new ArrayList<PropertyName[]>();
		if (kvpUC.get("PROPERTYNAME") != null) {
			List<String> params = KVPUtils.splitLists(kvpUC.get("PROPERTYNAME"));
			if (numQueries != -1 && params.size() != numQueries) {
				String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
				throw new OWSException(msg, MISSING_PARAMETER_VALUE);
			}
			else {
				numQueries = params.size();
			}
			for (String param : params) {
				String[] subParams = KVPUtils.splitList(param);
				PropertyName[] projectionClauses = new PropertyName[subParams.length];
				for (int i = 0; i < subParams.length; i++) {
					String subParam = subParams[i];
					ValueReference propName = new ValueReference(subParam, nsContext);
					projectionClauses[i] = new PropertyName(propName, resolveParams, null);
				}
				projectionClausesList.add(projectionClauses);
			}
		}

		// optional: FILTER (can be a list of values)
		List<Filter> filterList = new ArrayList<Filter>();
		if (kvpUC.get("FILTER") != null) {
			List<String> params = KVPUtils.splitLists(kvpUC.get("FILTER"));
			if (numQueries != -1 && params.size() != numQueries) {
				String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
				throw new OWSException(msg, MISSING_PARAMETER_VALUE);
			}
			else {
				numQueries = params.size();
			}
			for (String param : params) {
				filterList.add(parseFilter200(param));
			}
		}

		// optional: FILTER_LANGUAGE (can be a list of values)
		// List<String> filterLanguageList = new ArrayList<String>();
		if (kvpUC.get("FILTER_LANGUAGE") != null) {
			LOG.warn("Handling of FILTER_LANGUAGE parameter not implemented yet.");
		}

		// optional: RESOURCEID (can be a list of lists)
		List<String[]> resourceIdList = new ArrayList<String[]>();
		if (kvpUC.get("RESOURCEID") != null) {
			List<String> params = KVPUtils.splitLists(kvpUC.get("RESOURCEID"));
			if (numQueries != -1 && params.size() != numQueries) {
				String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
				throw new OWSException(msg, MISSING_PARAMETER_VALUE);
			}
			else {
				numQueries = params.size();
			}
			for (String param : params) {
				String[] subParams = KVPUtils.splitList(param);
				resourceIdList.add(subParams);
			}
		}

		// optional: SORTBY (can be a list of lists)
		List<SortProperty[]> sortByList = new ArrayList<SortProperty[]>();
		if (kvpUC.get("SORTBY") != null) {
			List<String> params = KVPUtils.splitLists(kvpUC.get("SORTBY"));
			if (numQueries != -1 && params.size() != numQueries) {
				String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
				throw new OWSException(msg, MISSING_PARAMETER_VALUE);
			}
			else {
				numQueries = params.size();
			}
			for (String param : params) {
				sortByList.add(getSortBy(param, nsContext));
			}
		}

		if (typeNamesList.isEmpty() && resourceIdList.isEmpty()) {
			String msg = "At least one of the parameters TYPENAMES and RESOURCEID must be present in KVP-encoded Ad hoc queries.";
			throw new OWSException(msg, MISSING_PARAMETER_VALUE);
		}
		if (!resourceIdList.isEmpty() && bbox != null) {
			String msg = "Parameters RESOURCEID and BBOX are mututally exclusive.";
			throw new OWSException(msg, INVALID_PARAMETER_VALUE);
		}
		if (!filterList.isEmpty() && !resourceIdList.isEmpty()) {
			String msg = "Parameters FILTER and RESOURCEID are mututally exclusive.";
			throw new OWSException(msg, INVALID_PARAMETER_VALUE);
		}
		if (!filterList.isEmpty() && bbox != null) {
			String msg = "Parameters FILTER and BBOX are mututally exclusive.";
			throw new OWSException(msg, INVALID_PARAMETER_VALUE);
		}

		List<Query> queries = new ArrayList<Query>(numQueries);
		if (!resourceIdList.isEmpty()) {
			for (int i = 0; i < numQueries; i++) {
				String[] resourceIds = resourceIdList.get(i);
				TypeName[] typeNames = new TypeName[0];
				if (!typeNamesList.isEmpty()) {
					typeNames = typeNamesList.get(i);
				}
				ICRS srsName = null;
				if (!srsNames.isEmpty()) {
					srsName = srsNames.get(i);
				}
				PropertyName[] projectionClauses = null;
				if (!projectionClausesList.isEmpty()) {
					projectionClauses = projectionClausesList.get(i);
				}
				SortProperty[] sortBy = null;
				if (!sortByList.isEmpty()) {
					sortBy = sortByList.get(i);
				}
				queries.add(new FeatureIdQuery(null, typeNames, null, srsName, projectionClauses, sortBy, resourceIds));
			}
		}
		else if (!typeNamesList.isEmpty()) {
			if (bbox != null) {
				for (int i = 0; i < numQueries; i++) {
					TypeName[] typeNames = typeNamesList.get(i);
					ICRS srsName = null;
					if (!srsNames.isEmpty()) {
						srsName = srsNames.get(i);
					}
					PropertyName[] projectionClauses = null;
					if (!projectionClausesList.isEmpty()) {
						projectionClauses = projectionClausesList.get(i);
					}
					SortProperty[] sortBy = null;
					if (!sortByList.isEmpty()) {
						sortBy = sortByList.get(i);
					}
					for (TypeName typeName : typeNames) {
						queries.add(new BBoxQuery(null, new TypeName[] { typeName }, null, srsName, projectionClauses,
								sortBy, bbox));
					}
				}
			}
			else {
				for (int i = 0; i < numQueries; i++) {
					Filter filter = null;
					if (!filterList.isEmpty()) {
						filter = filterList.get(i);
					}
					TypeName[] typeNames = typeNamesList.get(i);
					ICRS srsName = null;
					if (!srsNames.isEmpty()) {
						srsName = srsNames.get(i);
					}
					PropertyName[] projectionClauses = null;
					if (!projectionClausesList.isEmpty()) {
						projectionClauses = projectionClausesList.get(i);
					}
					SortProperty[] sortBy = null;
					if (!sortByList.isEmpty()) {
						sortBy = sortByList.get(i);
					}
					if (filter == null) {
						for (TypeName typeName : typeNames) {
							queries.add(new FilterQuery(null, new TypeName[] { typeName }, null, srsName,
									projectionClauses, sortBy, null));
						}
					}
					else {
						queries.add(new FilterQuery(null, typeNames, null, srsName, projectionClauses, sortBy, filter));
					}
				}
			}
		}
		return queries;
	}

	private static QName resolveQName(String prefixedName, NamespaceBindings nsBindings) {
		QName qName = null;
		String[] typeParts = prefixedName.split(":");
		if (typeParts.length == 2) {
			String nsUri = nsBindings == null ? null : nsBindings.getNamespaceURI(typeParts[0]);
			qName = new QName(nsUri, typeParts[1], typeParts[0]);
		}
		else {
			qName = new QName(typeParts[0]);
		}
		return qName;
	}

	protected static Filter parseFilter200(String filter) throws XMLStreamException, FactoryConfigurationError {

		String bindingPreamble = "<nsbindings xmlns=\"" + FES_20_NS + "\" xmlns:fes=\"" + FES_20_NS + "\" xmlns:gml=\""
				+ GML3_2_NS + "\">";
		String bindingEpilog = "</nsbindings>";
		StringReader sr = new StringReader(bindingPreamble + filter + bindingEpilog);
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(sr);
		skipStartDocument(xmlStream);
		nextElement(xmlStream);
		return Filter200XMLDecoder.parse(xmlStream);
	}

	private static List<Query> parseStoredQuery200(Map<String, String> kvpUC) {
		// mandatory: STOREDQUERY_ID
		String id = KVPUtils.getRequired(kvpUC, "STOREDQUERY_ID");

		Map<String, OMElement> paramNameToValue = new HashMap<String, OMElement>();
		for (String key : kvpUC.keySet()) {
			String literalValue = kvpUC.get(key);
			// TODO
			String xml = "<fes:Literal xmlns:fes=\"" + FES_20_NS + "\"><![CDATA[";
			xml += literalValue;
			xml += "]]></fes:Literal>";
			OMElement literalEl;
			try {
				literalEl = AXIOMUtil.stringToOM(xml);
				paramNameToValue.put(key, literalEl);
			}
			catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		List<Query> queries = new ArrayList<Query>();
		queries.add(new StoredQuery(null, id, paramNameToValue));
		return queries;
	}

	protected static String[] getFilters(String filterStr) {
		String[] filters = null;
		if (filterStr != null) {
			filters = filterStr.split("[)][(]");
			if (filters[0].startsWith("(")) {
				filters[0] = filters[0].substring(1);
			}

			String last = filters[filters.length - 1];
			if (last.endsWith(")")) {
				filters[filters.length - 1] = last.substring(0, last.length() - 1);
			}
		}
		return filters;
	}

	@SuppressWarnings("boxing")
	protected static Envelope createEnvelope(String bboxStr, ICRS srs) {
		String[] coordList = bboxStr.split(",");

		int n = coordList.length / 2;
		List<Double> lowerCorner = new ArrayList<Double>();
		for (int i = 0; i < n; i++) {
			lowerCorner.add(Double.parseDouble(coordList[i]));
		}
		List<Double> upperCorner = new ArrayList<Double>();
		for (int i = n; i < 2 * n; i++) {
			upperCorner.add(Double.parseDouble(coordList[i]));
		}

		GeometryFactory gf = new GeometryFactory();

		return gf.createEnvelope(lowerCorner, upperCorner, srs);
	}

	protected static PropertyName[][] getXLinkPropNames(PropertyName[][] propertyNames, String[][] ptxDepthAr,
			Integer[][] ptxExpAr) {
		PropertyName[][] result = null;
		if (propertyNames != null) {
			result = new PropertyName[propertyNames.length][];
			for (int i = 0; i < propertyNames.length; i++) {
				result[i] = new PropertyName[propertyNames[i].length];
				for (int j = 0; j < propertyNames[i].length; j++) {
					if (ptxDepthAr != null || ptxExpAr != null) {
						String resolveDepth = null;
						if (ptxDepthAr != null) {
							resolveDepth = ptxDepthAr[i][j];
						}
						BigInteger resolveTimeout = null;
						if (ptxExpAr != null) {
							resolveTimeout = ptxExpAr[i][j] == null ? null : BigInteger.valueOf(ptxExpAr[i][j] * 60);
						}
						ResolveParams propResolveParams = new ResolveParams(null, resolveDepth, resolveTimeout);
						result[i][j] = new PropertyName(propertyNames[i][j].getPropertyName(), propResolveParams, null);
					}
					else {
						result[i][j] = propertyNames[i][j];
					}
				}
			}
		}
		return result;
	}

	protected static TypeName[] getTypeNames(String typeStrList, Map<String, String> nsBindings) {
		TypeName[] result = null;
		if (typeStrList != null) {

			String[] typeList = typeStrList.split(",");
			result = new TypeName[typeList.length];

			for (int i = 0; i < typeList.length; i++) {
				String[] typeParts = typeList[i].split(":");
				if (typeParts.length == 2) {

					// check if it has an alias
					int equalSign;
					if ((equalSign = typeParts[1].indexOf("=")) != -1) {
						result[i] = new TypeName(new QName(nsBindings.get(typeParts[0]), typeParts[1], typeParts[0]),
								typeParts[1].substring(equalSign + 1));
					}
					else {
						result[i] = new TypeName(new QName(nsBindings.get(typeParts[0]), typeParts[1], typeParts[0]),
								null);
					}
				}
				else {
					result[i] = new TypeName(new QName(typeParts[0]), null);
				}
			}
		}
		else {
			result = new TypeName[0];
		}
		return result;
	}

	protected static TypeName[] getTypeNames100(String typeStrList) {
		TypeName[] result = null;
		if (typeStrList != null) {

			String[] typeList = typeStrList.split(",");
			result = new TypeName[typeList.length];

			for (int i = 0; i < typeList.length; i++) {
				String alias = null;
				String theRest = typeList[i];
				if (typeList[i].contains("=")) {
					alias = typeList[i].split("=")[0];
					theRest = typeList[i].split("=")[1];
				}

				String prefix = null;
				String local = theRest;
				if (theRest.contains(":")) {
					prefix = theRest.split(":")[0];
					local = theRest.split(":")[1];
				}

				QName qName = prefix == null ? new QName(local) : new QName("", local, prefix);
				result[i] = new TypeName(qName, alias);
			}
		}
		return result;
	}

	protected static SortProperty[] getSortBy(String sortbyStr, NamespaceBindings nsContext) {
		SortProperty[] result = null;
		if (sortbyStr != null) {
			String[] sortbyComm = KVPUtils.splitList(sortbyStr);
			result = new SortProperty[sortbyComm.length];
			for (int i = 0; i < sortbyComm.length; i++) {
				if (sortbyComm[i].endsWith(" D") || sortbyComm[i].endsWith(" DESC")) {
					String sortbyProp = sortbyComm[i].substring(0, sortbyComm[i].indexOf(" "));
					result[i] = new SortProperty(new ValueReference(sortbyProp, nsContext), false);
				}
				else {
					if (sortbyComm[i].endsWith(" A") || sortbyComm[i].endsWith(" ASC")) {
						String sortbyProp = sortbyComm[i].substring(0, sortbyComm[i].indexOf(" "));
						result[i] = new SortProperty(new ValueReference(sortbyProp, nsContext), true);
					}
					else {
						result[i] = new SortProperty(new ValueReference(sortbyComm[i], nsContext), true);
					}
				}
			}
		}
		return result;
	}

	protected static PropertyName[][] getPropertyNames(String propertyStr, NamespaceBindings nsContext) {
		PropertyName[][] result = null;
		if (propertyStr != null) {
			String[][] propComm = parseParamList(propertyStr);
			result = new PropertyName[propComm.length][];
			for (int i = 0; i < propComm.length; i++) {
				result[i] = new PropertyName[propComm[i].length];

				for (int j = 0; j < propComm[i].length; j++) {
					result[i][j] = new PropertyName(new ValueReference(propComm[i][j], nsContext), null, null);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("boxing")
	protected static Integer[][] parseParamListAsInts(String paramList) {
		String[][] strings = parseParamList(paramList);

		Integer[][] result = new Integer[strings.length][];
		for (int i = 0; i < strings.length; i++) {
			result[i] = new Integer[strings[i].length];

			for (int j = 0; j < strings[i].length; j++) {
				try {
					result[i][j] = Integer.parseInt(strings[i][j]);

				}
				catch (NumberFormatException e) {
					e.printStackTrace();
					throw new InvalidParameterValueException(e.getMessage(), e);
				}
			}
		}

		return result;
	}

	protected static String[][] parseParamList(String paramList) {
		String[] paramPar = paramList.split("[)][(]");

		if (paramPar[0].startsWith("(")) {
			paramPar[0] = paramPar[0].substring(1);
		}
		String last = paramPar[paramPar.length - 1];
		if (last.endsWith(")")) {
			paramPar[paramPar.length - 1] = last.substring(0, last.length() - 1);
		}

		// split on commas
		String[][] paramComm = new String[paramPar.length][];
		for (int i = 0; i < paramPar.length; i++) {
			paramComm[i] = paramPar[i].split(",");
		}

		return paramComm;
	}

	protected static Envelope parseBBox200(String bboxStr) {

		String[] coordList = bboxStr.split(",");

		// NOTE: Contradiction between spec and CITE tests (for omitted crsUri)
		// - WFS 1.1.0 spec, 14.3.3: coordinates should be in WGS84
		// - CITE tests, wfs:wfs-1.1.0-Basic-GetFeature-tc8.1: If no CRS reference is
		// provided, a service-defined
		// default value must be assumed.
		ICRS bboxCrs = null;
		if (coordList.length % 2 == 1) {
			bboxCrs = CRSManager.getCRSRef(coordList[coordList.length - 1]);
		}

		return createEnvelope(bboxStr, bboxCrs);
	}

}
