//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/ogcwebservices/wfs/operation/DescribeFeatureType.java $
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstraße 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 ---------------------------------------------------------------------------*/
package org.deegree.protocol.wfs.getfeature;

import org.deegree.commons.types.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;

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

    private ResultType resultType;

    private String outputFormat;

    // using Integer instead of int here, so it can be null (unspecified)
    private Integer maxFeatures;

    // positive Integer, "*" (unlimited) or null (unspecified)
    private String traverseXlinkDepth;

    // using Integer instead of int here, so it can be null (unspecified)
    private Integer traverseXlinkExpiry;

    private Query[] queries;

    /**
     * Creates a new {@link GetFeature} request.
     * 
     * @param version
     *            protocol version, may not be null
     * @param handle
     *            client-generated identifier, may be null
     * @param resultType
     *            query mode (result or hits), may be null
     * @param outputFormat
     *            requested output format, may be null
     * @param maxFeatures
     *            maximum number of features that should be generated (positive integer), may be null
     * @param traverseXlinkDepth
     *            the depth to which nested property XLink linking element locator attribute (href) XLinks are traversed
     *            and resolved if possible, the range of valid values for this parameter consists of positive integers,
     *            "*" (unlimited) and null (unspecified)
     * @param traverseXlinkExpiry
     *            indicates how long the WFS should wait to receive a response to a nested GetGmlObject request (in
     *            minutes), this attribute is only relevant if a value is specified for the traverseXlinkDepth
     *            attribute, may be null
     * @param queries
     *            the queries to be performed in the request, must not be null and must contain at least one entry
     */
    public GetFeature( Version version, String handle, ResultType resultType, String outputFormat, Integer maxFeatures,
                       String traverseXlinkDepth, Integer traverseXlinkExpiry, Query[] queries ) {
        super( version, handle );
        this.resultType = resultType;
        this.outputFormat = outputFormat;
        this.maxFeatures = maxFeatures;
        this.traverseXlinkDepth = traverseXlinkDepth;
        this.traverseXlinkExpiry = traverseXlinkExpiry;
        this.queries = queries;
    }

    /**
     * Returns the requested query mode (result or hits).
     * 
     * @return the requested query mode, or null if unspecified
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * Returns the requested output format.
     * 
     * @return the requested output format, or null if unspecified
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * Returns the maximum number of features that should be generated.
     * 
     * @return the maximum number of features (positive integer), or null if unspecified
     */
    public Integer getMaxFeatures() {
        return maxFeatures;
    }

    /**
     * Returns the depth to which nested property XLink linking element locator attribute (href) XLinks are traversed
     * and resolved if possible. The range of valid values for this parameter consists of positive integers, "*"
     * (unlimited) and null (unspecified).
     * 
     * @return the depth (positive integer), "*" (unlimited) or null (unspecified)
     */
    public String getTraverseXlinkDepth() {
        return traverseXlinkDepth;
    }

    /**
     * Return the number of minutes that the WFS should wait to receive a response to a nested GetGmlObject request.
     * This is only relevant if a value is specified for the traverseXlinkDepth parameter.
     * 
     * @return the number of minutes to wait for nested GetGmlObject responses (positive integer) or null (unspecified)
     */
    public Integer getTraverseXlinkExpiry() {
        return traverseXlinkExpiry;
    }
    
    /**
     * The queries to be performed in the request.
     * 
     * @return the queries to be performed, never null and must contain at least one entry
     */
    public Query [] getQueries () {
        return queries;
    }
}
