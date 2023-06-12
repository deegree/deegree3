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

import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.metadata.ebrim.AdhocQuery;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.services.csw.AbstractCSWRequest;

/**
 * Represents a <Code>GetRecords</Code> request to a CSW.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class GetRecords extends AbstractCSWRequest {

	private final String requestId;

	private final URI outputSchema;

	private final int startPosition;

	private final int maxRecords;

	private final boolean distributedSearch;

	private final int hopCount;

	private final String responseHandler;

	private final ResultType resultType;

	private final Query query;

	private AdhocQuery adhocQuery;

	private final OMElement holeRequest;

	/**
	 * Creates a new {@link GetRecords} request.
	 * @param version protocol version
	 * @param namespaces
	 * @param outputFormat controls the format of the output regarding to a MIME-type
	 * (default: application/xml)
	 * @param resultType mode of the response that is requested
	 * @param requestId UUID
	 * @param outputSchema indicates the schema of the output (default:
	 * http://www.opengis.net/cat/csw/2.0.2)
	 * @param startPosition used to specify at which position should be started
	 * @param maxRecords defines the maximum number of records that should be returned
	 * @param distributedSearch
	 * @param hopCount
	 * @param responseHandler
	 * @param query the query of the GetRecords request, never <code>null</code>
	 * @param holeRequest
	 */
	public GetRecords(Version version, NamespaceBindings namespaces, String outputFormat, ResultType resultType,
			String requestId, URI outputSchema, int startPosition, int maxRecords, boolean distributedSearch,
			int hopCount, String responseHandler, Query query, OMElement holeRequest) {
		super(version, namespaces, query != null ? query.getQueryTypeNames() : new QName[0], outputFormat);
		this.resultType = resultType;
		this.requestId = requestId;
		this.outputSchema = outputSchema;
		this.startPosition = startPosition;
		this.maxRecords = maxRecords;
		this.distributedSearch = distributedSearch;
		this.hopCount = hopCount;
		this.responseHandler = responseHandler;
		this.query = query;
		this.holeRequest = holeRequest;
	}

	public GetRecords(Version version, NamespaceBindings namespaces, String outputFormat, ResultType resultType,
			String requestId, URI outputSchema, int startPosition, int maxRecords, boolean distributedSearch,
			int hopCount, String responseHandler, AdhocQuery adhocQuery, OMElement holeRequest) {
		this(version, namespaces, outputFormat, resultType, requestId, outputSchema, startPosition, maxRecords,
				distributedSearch, hopCount, responseHandler, (Query) null, holeRequest);
		this.adhocQuery = adhocQuery;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @return the outputSchema
	 */
	public URI getOutputSchema() {
		return outputSchema;
	}

	/**
	 * @return the startPosition
	 */
	public int getStartPosition() {
		return startPosition;
	}

	/**
	 * @return the maxRecords
	 */
	public int getMaxRecords() {
		return maxRecords;
	}

	/**
	 * @return the distributedSearch
	 */
	public boolean isDistributedSearch() {
		return distributedSearch;
	}

	/**
	 * @return the hopCount
	 */
	public int getHopCount() {
		return hopCount;
	}

	/**
	 * @return the responseHandler
	 */
	public String getResponseHandler() {
		return responseHandler;
	}

	/**
	 * @return the resultType
	 */
	public ResultType getResultType() {
		return resultType;
	}

	/**
	 * Returns the query.
	 * @return query, can be <code>null</code> (if an AdhocQuery is used)
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * @return the query
	 */
	public AdhocQuery getAdhocQuery() {
		return adhocQuery;
	}

	/**
	 * @return the holeRequest
	 */
	public OMElement getXMLRequest() {
		return holeRequest;
	}

}