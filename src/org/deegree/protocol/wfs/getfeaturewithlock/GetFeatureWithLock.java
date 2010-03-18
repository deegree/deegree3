//$HeadURL$
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

package org.deegree.protocol.wfs.getfeaturewithlock;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.Query;
import org.deegree.protocol.wfs.getfeature.ResultType;

/**
 * Represents a <code>GetFeatureWithLock</code> request to a WFS.
 * 
 * @see GetFeature
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetFeatureWithLock extends GetFeature {

    private Integer expiry;

    /**
     * Creates a new {@link GetFeatureWithLock} request.
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
     * @param expiry
     *            expiry time (in minutes) before the features are unlocked automatically, may be null (unspecified)
     */
    public GetFeatureWithLock( Version version, String handle, ResultType resultType, String outputFormat, Integer maxFeatures,
                       String traverseXlinkDepth, Integer traverseXlinkExpiry, Query[] queries, Integer expiry ) {
        super( version, handle, resultType, outputFormat, maxFeatures, traverseXlinkDepth, traverseXlinkExpiry, queries );
        this.expiry = expiry;
    }    
 
    /**
     * Returns the expiry time for the acquired locks.
     * 
     * @return the expiry time for the acquired locks, can be null (unspecified)
     */
    public Integer getExpiry() {
        return expiry;
    }    
}
