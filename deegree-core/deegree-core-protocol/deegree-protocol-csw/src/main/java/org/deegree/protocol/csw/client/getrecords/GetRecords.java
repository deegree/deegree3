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
package org.deegree.protocol.csw.client.getrecords;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.Version;
import org.deegree.filter.Filter;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.protocol.csw.client.AbstractDiscoveryRequest;

/**
 * Represents a <code>GetRecords</code> request to a CSW.
 * <p>
 * Supported versions:
 * <ul>
 * <li>CSW 2.0.2</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetRecords extends AbstractDiscoveryRequest {

	private int startPosition = 1;

	private int maxRecords = 10;

	private ResultType resultType = ResultType.hits;

	private List<QName> typeNames = Collections
		.singletonList(new QName(CSWConstants.CSW_202_NS, "Record", CSWConstants.CSW_202_PREFIX));

	private final Filter constraint;

	private int hopCount = 0;

	/**
	 * @param version the version of the CSW, currently only 2.0.2 is supported
	 * @param startPosition the index of the first record to return (1 based), must be
	 * equal to or greater than 1
	 * @param maxRecords maximum number of record to return, must be a positive integer
	 * equal to or greater than 1
	 * @param outputFormat never <code>null</code>
	 * @param outputSchema never <code>null</code>
	 * @param typeNames never <code>null</code>
	 * @param resultType never <code>null</code>
	 * @param elementSetName never <code>null</code>
	 * @param constraint may be <code>null</code> if the response should not be filtered
	 */
	public GetRecords(Version version, int startPosition, int maxRecords, String outputFormat, String outputSchema,
			List<QName> typeNames, ResultType resultType, ReturnableElement elementSetName, Filter constraint) {
		super(version, elementSetName, outputFormat, outputSchema);
		if (startPosition <= 0) {
			throw new IllegalArgumentException("StartPosition mus be greater than or equal to 1!");
		}
		this.startPosition = startPosition;
		this.maxRecords = maxRecords;
		this.typeNames = typeNames;
		this.resultType = resultType;
		this.constraint = constraint;
	}

	/**
	 * @param version the version of the CSW, currently only 2.0.2 is supported
	 * @param startPosition the index of the first record to return (1 based), must be
	 * equal to or greater than 1
	 * @param maxRecords maximum number of record to return, must be a positive integer
	 * equal to or greater than 1
	 * @param outputFormat never <code>null</code>
	 * @param outputSchema never <code>null</code>
	 * @param typeNames never <code>null</code>
	 * @param resultType never <code>null</code>
	 * @param elementSetName never <code>null</code>
	 * @param constraint may be <code>null</code> if the response should not be filtered
	 * @param hopCount the maximum number of message hops before the search is terminated.
	 * Each catalogue node decrements this value when the request is received, and must
	 * not forward the request if hopCount=0. If negative 0 is assumed.
	 */
	public GetRecords(Version version, int startPosition, int maxRecords, String outputFormat, String outputSchema,
			List<QName> typeNames, ResultType resultType, ReturnableElement elementSetName, Filter constraint,
			int hopCount) {
		super(version, elementSetName, outputFormat, outputSchema);
		if (startPosition <= 0) {
			throw new IllegalArgumentException("StartPosition mus be greater than or equal to 1!");
		}
		this.startPosition = startPosition;
		this.maxRecords = maxRecords;
		this.typeNames = typeNames;
		this.resultType = resultType;
		this.constraint = constraint;
		if (hopCount >= 0) {
			this.hopCount = hopCount;
		}
	}

	/**
	 * Creates a new {@link GetRecords} instance with default values:
	 *
	 * <pre>
	 *  outputSchema=http://www.opengis.net/cat/csw/2.0.2.
	 *  outputFormat=application/xml
	 *  startPosition=1
	 *  maxRecords=10
	 *  hopCount=not specified
	 * </pre>
	 * @param version the version of the CSW, currently only 2.0.2 is supported
	 * @param resultType never <code>null</code>
	 * @param elementSetName never <code>null</code>
	 * @param constraint may be <code>null</code> if the response should not be filtered
	 * @param hopCount
	 */
	public GetRecords(Version version, ResultType resultType, ReturnableElement elementSetName, Filter constraint) {
		super(version, elementSetName, "application/xml", "http://www.opengis.net/cat/csw/2.0.2.");
		this.resultType = resultType;
		this.constraint = constraint;
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
	 * @return the typeNames
	 */
	public List<QName> getTypeNames() {
		return typeNames;
	}

	/**
	 * @return the resultType
	 */
	public ResultType getResultType() {
		return resultType;
	}

	/**
	 * @return the constraint
	 */
	public Filter getConstraint() {
		return constraint;
	}

	/**
	 * @return the hopCount
	 */
	public int getHopCount() {
		return hopCount;
	}

}
