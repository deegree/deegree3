/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import java.net.URI;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.i18n.Messages;
import org.deegree.services.csw.AbstractCSWRequestXMLAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class encapsulating the parsing an {@link GetRecords} XML request.
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public abstract class AbstractGetRecordsXMLAdapter extends AbstractCSWRequestXMLAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(GetRecordsXMLAdapter.class);

	/**
	 * Parses the {@link GetRecords} XML request by deciding which version has to be
	 * parsed because of the requested version.
	 * @param version
	 * @return {@Link GetRecords}
	 */
	public GetRecords parse(Version version, String defaultOutputFormat, String defaultOutputSchema) {

		if (version == null) {
			version = Version.parseVersion(getRequiredNodeAsString(rootElement, new XPath("@version", nsContext)));
		}

		GetRecords result = null;

		if (VERSION_202.equals(version)) {
			result = parse202(defaultOutputFormat, defaultOutputSchema);
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
	 * @return {@link GetRecords}
	 */
	private GetRecords parse202(String defaultOutputFormat, String defaultOutputSchema) {
		LOG.debug(rootElement.toString());

		String resultTypeStr = getNodeAsString(rootElement, new XPath("@resultType", nsContext),
				ResultType.hits.name());

		OMElement holeRequest = getElement(rootElement, new XPath(".", nsContext));
		ResultType resultType = ResultType.determineResultType(resultTypeStr);

		int maxRecords = getNodeAsInt(rootElement, new XPath("@maxRecords", nsContext), 10);

		int startPosition = getNodeAsInt(rootElement, new XPath("@startPosition", nsContext), 1);

		String outputFormat = getNodeAsString(rootElement, new XPath("@outputFormat", nsContext), defaultOutputFormat);

		String requestId = getNodeAsString(rootElement, new XPath("@requestId", nsContext), null);

		String outputSchemaString = getNodeAsString(rootElement, new XPath("@outputSchema", nsContext),
				defaultOutputSchema);

		URI outputSchema = URI.create(outputSchemaString);

		List<OMElement> getRecordsChildElements = getRequiredElements(rootElement, new XPath("*", nsContext));

		return parseSubElements(holeRequest, resultType, maxRecords, startPosition, outputFormat, requestId,
				outputSchema, getRecordsChildElements);
	}

	protected Query parseQuery(OMElement omElement) {
		return Query.getQuery(omElement);
	}

	protected abstract GetRecords parseSubElements(OMElement holeRequest, ResultType resultType, int maxRecords,
			int startPosition, String outputFormat, String requestId, URI outputSchema,
			List<OMElement> getRecordsChildElements);

}
