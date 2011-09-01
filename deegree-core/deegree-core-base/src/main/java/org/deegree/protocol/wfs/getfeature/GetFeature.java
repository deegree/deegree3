//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/ogcwebservices/wfs/operation/DescribeFeatureType.java $
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
package org.deegree.protocol.wfs.getfeature;

import org.deegree.commons.tom.ResolveMode;
import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;
import org.deegree.protocol.wfs.query.Query;

/**
 * Represents a <code>GetFeature</code> request to a WFS.
 * 
 * @see Query
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetFeature extends AbstractWFSRequest {

    // using Integer instead of int here, so it can be null (unspecified)
    private final Integer startIndex;

    // using Integer instead of int here, so it can be null (unspecified)
    private final Integer count;

    private final String outputFormat;

    private final ResultType resultType;

    private final ResolveMode resolveMode;

    // positive Integer, "*" (unlimited) or null (unspecified)
    private final String resolveDepth;

    // using Integer instead of int here, so it can be null (unspecified)
    private final Integer resolveTimeout;

    private final Query[] queries;

    /**
     * Creates a new {@link GetFeature} request.
     * 
     * @param version
     *            protocol version, must not be <code>null</code>
     * @param handle
     *            client-generated identifier, may be <code>null</code>
     * @param startIndex
     *            index within the result set from which the server shall begin returning results, non-negative integer
     *            or <code>null</code> (unspecified)
     * @param count
     *            limits the number of returned results, non-negative integer or <code>null</code> (unspecified)
     * @param outputFormat
     *            requested output format, may be <code>null</code> (unspecified)
     * @param resultType
     *            query response mode (result or hits), may be <code>null</code> (unspecified)
     * @param resolveMode
     *            mode for resolving resource references in the output, may be <code>null</code> (unspecified)
     * @param resolveDepth
     *            depth to which nested resource references shall be resolved in the response document, range of valid
     *            values for this parameter consists of positive integers, "*" (unlimited) and <code>null</code>
     *            (unspecified)
     * @param resolveTimeout
     *            number of seconds to allow for resolving resource references, may be <code>null</code> (unspecified)
     * @param queries
     *            the queries to be performed in the request, must not be <code>null</code> and must contain at least
     *            one entry
     */
    public GetFeature( Version version, String handle, Integer startIndex, Integer count, String outputFormat,
                       ResultType resultType, ResolveMode resolveMode, String resolveDepth, Integer resolveTimeout,
                       Query[] queries ) {
        super( version, handle );
        this.startIndex = startIndex;
        this.count = count;
        this.outputFormat = outputFormat;
        this.resultType = resultType;
        this.resolveMode = resolveMode;
        this.resolveDepth = resolveDepth;
        this.resolveTimeout = resolveTimeout;
        this.queries = queries;
    }

    /**
     * Returns the index within the result set from which the server shall begin returning results.
     * 
     * @return index within the result set from which the server shall begin returning results (non-negative integer),
     *         can be <code>null</code> (unspecified)
     */
    public Integer getStartIndex() {
        return startIndex;
    }

    /**
     * Returns the limit for the number of returned results.
     * 
     * @return limit for the number of returned results (non-negative integer), can be <code>null</code> (unspecified)
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Returns the requested output format.
     * 
     * @return requested output format, or <code>null</code> if unspecified
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * Returns the requested query mode (result or hits).
     * 
     * @return requested query mode, or <code>null</code> (unspecified)
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * Returns the mode for resolving resource references in the output.
     * 
     * @return resolve mode, can be <code>null</code> (unspecified)
     */
    public ResolveMode getResolveMode() {
        return resolveMode;
    }

    /**
     * Returns the depth to which nested resource references shall be resolved in the response document.
     * 
     * @return depth (positive integer), "*" (unlimited) or <code>null</code> (unspecified)
     */
    public String getResolveDepth() {
        return resolveDepth;
    }

    /**
     * Return the number of number of seconds to allow for resolving resource references.
     * 
     * @return number of seconds to allow for reference resolving, positive integer or <code>null</code> (unspecified)
     */
    public Integer getResolveTimeout() {
        return resolveTimeout;
    }

    /**
     * The queries to be performed in the request.
     * 
     * @return the queries to be performed, never <code>null</code> and must contain at least one entry
     */
    public Query[] getQueries() {
        return queries;
    }
}
