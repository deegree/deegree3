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
package org.deegree.protocol.wfs.getgmlobject;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;

/**
 * Represents a <code>GetGmlObject</code> request to a WFS.
 * <p>
 * From the WFS 1.1.0 spec.: The <code>GetGmlObject</code> operation allows retrieval of features and elements by ID
 * from a WFS. A <code>GetGmlObject</code> request is processed by a WFS and an XML document fragment containing the
 * result set is returned to the client.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetGmlObject extends AbstractWFSRequest {

    private String requestedId;

    private String outputFormat;

    // positive Integer, "*" (unlimited) or null (unspecified)
    private String traverseXlinkDepth;

    // using Integer instead of int here, so it can be null (unspecified)
    private Integer traverseXlinkExpiry;

    /**
     * Creates a new {@link GetGmlObject} request.
     * 
     * @param version
     *            protocol version, must not be null
     * @param handle
     *            client-generated identifier, may be null
     * @param requestedId
     *            the id of the requested object, must not be null
     * @param outputFormat
     *            a String format of the result set, may be null
     * @param traverseXlinkDepth
     *            the depth to which nested property XLink linking element locator attribute (href) XLinks are traversed
     *            and resolved if possible, the range of valid values for this parameter consists of positive integers,
     *            "*" (unlimited) and null (unspecified)
     * @param traverseXlinkExpiry
     *            indicates how long the WFS should wait to receive a response to a nested GetGmlObject request (in
     *            minutes), this attribute is only relevant if a value is specified for the traverseXlinkDepth
     *            attribute, may be null
     */
    public GetGmlObject( Version version, String handle, String requestedId, String outputFormat,
                         String traverseXlinkDepth, Integer traverseXlinkExpiry ) {
        super( version, handle );
        this.requestedId = requestedId;
        this.outputFormat = outputFormat;
        this.traverseXlinkDepth = traverseXlinkDepth;
        this.traverseXlinkExpiry = traverseXlinkExpiry;
    }

    /**
     * Returns the id of the requested object.
     * 
     * @return the id of the requested object, never null
     */
    public String getRequestedId() {
        return requestedId;
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
     * Return the number of minutes that the WFS should wait to receive a response to a nested <code>GetGmlObject</code>
     * request. This is only relevant if a value is specified for the <code>traverseXlinkDepth</code> parameter.
     * 
     * @return the number of minutes to wait for nested <code>GetGmlObject</code> responses (positive integer) or null
     *         (unspecified)
     */
    public Integer getTraverseXlinkExpiry() {
        return traverseXlinkExpiry;
    }
}
