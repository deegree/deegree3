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
package org.deegree.services.csw.getrecords;

import static org.deegree.protocol.csw.CSWConstants.VERSION_202;
import static org.deegree.protocol.csw.CSWConstants.ConstraintLanguage.CQLTEXT;
import static org.deegree.protocol.csw.CSWConstants.ConstraintLanguage.FILTER;

import java.io.StringReader;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.protocol.i18n.Messages;
import org.deegree.services.csw.AbstractCSWKVPAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the method for parsing a {@Link GetRecords} KVP request via Http-GET.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class GetRecordsKVPAdapter extends AbstractCSWKVPAdapter {

	private static Logger LOG = LoggerFactory.getLogger(GetRecordsKVPAdapter.class);

	/**
	 * Parses the {@link GetRecords} kvp request and decides which version has to parse
	 * because of the requested version
	 * @param normalizedKVPParams that are requested as key to a value.
	 * @return {@link GetRecords}
	 */
	public static GetRecords parse(Map<String, String> normalizedKVPParams, String defaultOutputFormat,
			String defaultOutputSchema) {
		Version version = Version.parseVersion(KVPUtils.getRequired(normalizedKVPParams, "VERSION"));
		GetRecords result = null;
		if (VERSION_202.equals(version)) {
			result = parse202(VERSION_202, normalizedKVPParams, defaultOutputFormat, defaultOutputSchema);

		}
		else {
			String msg = Messages.get("UNSUPPORTED_VERSION", version, Version.getVersionsString(VERSION_202));
			throw new InvalidParameterValueException(msg);
		}

		return result;

	}

	/**
	 * Parses the {@link GetRecords} request on basis of CSW version 2.0.2
	 * @param version that is requested, 2.0.2
	 * @param normalizedKVPParams that are requested containing all mandatory and optional
	 * parts regarding CSW spec
	 * @return {@link GetRecords}
	 */
	private static GetRecords parse202(Version version, Map<String, String> normalizedKVPParams,
			String defaultOutputFormat, String defaultOutputSchema) {

		// optional: 'NAMESPACE'
		Map<String, String> nsBindings = extractNamespaceBindings(normalizedKVPParams);
		if (nsBindings == null) {
			nsBindings = Collections.emptyMap();
		}

		NamespaceBindings nsContext = new NamespaceBindings();
		if (nsBindings != null) {
			for (String key : nsBindings.keySet()) {
				nsContext.addNamespace(key, nsBindings.get(key));
			}
		}

		// typeName (mandatory)
		QName[] typeNames = extractTypeNames(normalizedKVPParams, null);
		if (typeNames == null) {
			String msg = Messages.get("CSW_MISSING_PARAMETER_TYPENAMES");

			throw new MissingParameterException(msg);
		}

		// outputFormat (optional)
		String outputFormat = KVPUtils.getDefault(normalizedKVPParams, "outputFormat", defaultOutputFormat);

		// resultTpye (optional)
		String resultTypeStr = KVPUtils.getDefault(normalizedKVPParams, "RESULTTYPE", ResultType.hits.name());
		ResultType resultType = ResultType.determineResultType(resultTypeStr);

		// requestId (optional)
		String requestId = normalizedKVPParams.get("REQUESTID");

		// outputSchema String
		String outputSchemaString = KVPUtils.getDefault(normalizedKVPParams, "OUTPUTSCHEMA", defaultOutputSchema);

		URI outputSchema = URI.create(outputSchemaString);

		// startPosition int 1..*
		int startPosition = KVPUtils.getInt(normalizedKVPParams, "STARTPOSITION", 1);

		// maxRecords int 1..*
		int maxRecords = KVPUtils.getInt(normalizedKVPParams, "MAXRECORDS", 10);

		// elementName List<String>
		List<String> elementNameList = KVPUtils.splitAll(normalizedKVPParams, "ELEMENTNAME");

		String[] elementName = new String[elementNameList.size()];
		int counter = 0;
		for (String s : elementNameList) {
			elementName[counter++] = s;
		}

		/**
		 * NOTE: Spec says nothing about the handling which properties should exported if
		 * there is just an ELEMENTNAME provided. So, ELEMENTSETNAME is handled as a
		 * required attribute and is set to "summary" if there is nothing specified in the
		 * request. This can be used in the "elementSet"-attribute in the
		 * <Code>SearchResult</Code> parameter of the Response.
		 */
		String elementSetNameString = KVPUtils.getDefault(normalizedKVPParams, "ELEMENTSETNAME",
				ReturnableElement.summary.name());
		ReturnableElement elementSetName = ReturnableElement.determineReturnableElement(elementSetNameString);

		String constraintLanguageString = normalizedKVPParams.get("CONSTRAINTLAGNUAGE");
		// ConstraintLanguage Enum Language is specified
		ConstraintLanguage constraintLanguage = null;
		// constraint String Languagequery is specified
		String constraintString = normalizedKVPParams.get("CONSTRAINT");
		Filter constraint = null;

		if (constraintString != null) {
			// "Filterexpression" -> Filterexpression
			// TODO what if no begin and end tag?? -> <....>
			constraintString = constraintString.substring(1, constraintString.length() - 1);

			XMLStreamReader xmlStream = null;
			String constraintStringVersion = normalizedKVPParams.get("CONSTRAINT_LANGUAGE_VERSION");
			Version versionConstraint = Version.parseVersion(constraintStringVersion);

			try {
				// TODO remove usage of wrapper (necessary at the moment to work around
				// problems
				// with AXIOM's

				xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(constraintString));
				// skip START_DOCUMENT
				xmlStream.nextTag();

				if (versionConstraint.equals(new Version(1, 1, 0))) {

					constraint = Filter110XMLDecoder.parse(xmlStream);

				}
				else if (versionConstraint.equals(new Version(1, 0, 0))) {
					constraint = Filter100XMLDecoder.parse(xmlStream);
				}
				else {
					String msg = Messages.get("CSW_FILTER_VERSION_NOT_SPECIFIED", versionConstraint,
							Version.getVersionsString(new Version(1, 1, 0)),
							Version.getVersionsString(new Version(1, 0, 0)));
					LOG.info(msg);
					throw new InvalidParameterValueException(msg);
				}
			}
			catch (XMLStreamException e) {
				String msg = "FilterParsingException: There went something wrong while parsing the filter expression, so please check this!";
				LOG.debug(msg);
				throw new XMLParsingException(xmlStream, e.getMessage());
			}
			// If one is specified the other one has to be specified, as well.
			if (constraint == null && constraintLanguageString == null) {
				// TODO there is no filter expression available
			}
			else if (constraint != null && constraintLanguageString == null) {
				throw new MissingParameterException(
						"If there is a Constraint denoted then there should be a ConstraintLanguage provided");
			}
			else if (constraint == null && constraintLanguageString != null) {
				throw new MissingParameterException(
						"If there is a ConstraintLanguage denoted then there should be a Constraint provided");
			}
			else {
				if (constraintLanguageString.equalsIgnoreCase(FILTER.name())) {

					constraintLanguage = FILTER;
				}
				else if (constraintLanguageString.equalsIgnoreCase(CQLTEXT.name())) {
					constraintLanguage = CQLTEXT;
				}
			}
		}

		// sortBy List<String>
		List<String> sortByStrList = KVPUtils.splitAll(normalizedKVPParams, "SORTBY");
		SortProperty[] sortBy = getSortBy(sortByStrList, nsContext);

		// distributedSearch (optional; default = false)
		boolean distributedSearch = KVPUtils.getBoolean(normalizedKVPParams, "DISTRIBUTEDSEARCH", false);
		// TODO wenn true, dann hopCount darf auch gesetzt werden Spec 156
		// hopCount (optional; default = 2)
		int hopCount = KVPUtils.getInt(normalizedKVPParams, "HOPCOUNT", 2);

		// responseHandler String Spec 156
		// CSW processing synchron or asynchron
		// TODO
		String responseHandler = normalizedKVPParams.get("RESPONSEHANDLER");

		Query query = new Query(elementSetName, elementName, constraint, constraintLanguage, sortBy, typeNames, null);

		return new GetRecords(version, nsContext, outputFormat, resultType, requestId, outputSchema, startPosition,
				maxRecords, distributedSearch, hopCount, responseHandler, query, null);
	}

	/**
	 * sorts a string list ascending or descending
	 * @param sortByStrList
	 * @param nsContext
	 * @return
	 */
	private static SortProperty[] getSortBy(List<String> sortByStrList, NamespaceBindings nsContext) {
		SortProperty[] result = null;
		if (sortByStrList != null) {
			result = new SortProperty[sortByStrList.size()];
			int counter = 0;
			for (String s : sortByStrList) {
				if (s.endsWith(" D")) {
					String sortbyProp = s.substring(0, s.indexOf(" "));
					result[counter++] = new SortProperty(new ValueReference(sortbyProp, nsContext), false);

				}
				else {
					if (s.endsWith(" A")) {
						String sortbyProp = s.substring(0, s.indexOf(" "));
						result[counter++] = new SortProperty(new ValueReference(sortbyProp, nsContext), true);

					}
					else {
						result[counter++] = new SortProperty(new ValueReference(s, nsContext), true);
					}
				}
			}
		}
		return result;
	}

}