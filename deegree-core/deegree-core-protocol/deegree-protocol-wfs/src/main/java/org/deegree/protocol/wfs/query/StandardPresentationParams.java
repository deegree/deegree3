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
package org.deegree.protocol.wfs.query;

import java.math.BigInteger;

import org.deegree.protocol.wfs.getfeature.ResultType;

/**
 * Encapsulates standard parameters for controlling the presentation of {@link Query}
 * results.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class StandardPresentationParams {

	private final BigInteger startIndex;

	private final BigInteger count;

	private final ResultType resultType;

	private final String outputFormat;

	/**
	 * Creates a new {@link StandardPresentationParams} instance.
	 * @param startIndex index within the result set from which the server shall begin
	 * returning results (conting starts at 0), can be <code>null</code> (unspecified)
	 * @param count limit for the number of returned results (non-negative integer), can
	 * be <code>null</code> (unspecified)
	 * @param resultType requested query mode (result or hits), can be <code>null</code>
	 * (unspecified)
	 * @param outputFormat requested output format, can be <code>null</code> (unspecified)
	 */
	public StandardPresentationParams(BigInteger startIndex, BigInteger count, ResultType resultType,
			String outputFormat) {
		this.startIndex = startIndex;
		this.count = count;
		this.resultType = resultType;
		this.outputFormat = outputFormat;
	}

	/**
	 * Returns the index within the result set from which the server shall begin returning
	 * results (counting starts at 0).
	 * @return index within the result set from which the server shall begin returning
	 * results (non-negative integer, counting starts at 0), can be <code>null</code>
	 * (unspecified)
	 */
	public BigInteger getStartIndex() {
		return startIndex;
	}

	/**
	 * Returns the limit for the number of returned results.
	 * @return limit for the number of returned results (non-negative integer), can be
	 * <code>null</code> (unspecified)
	 */
	public BigInteger getCount() {
		return count;
	}

	/**
	 * Returns the requested query mode (result or hits).
	 * @return requested query mode, or <code>null</code> (unspecified)
	 */
	public ResultType getResultType() {
		return resultType;
	}

	/**
	 * Returns the requested output format.
	 * @return requested output format, or <code>null</code> if unspecified
	 */
	public String getOutputFormat() {
		return outputFormat;
	}

}
