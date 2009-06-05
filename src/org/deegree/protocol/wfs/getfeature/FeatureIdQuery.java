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

import org.deegree.commons.filter.expression.Function;
import org.deegree.commons.filter.expression.PropertyName;
import org.deegree.commons.filter.sort.SortProperty;
import org.deegree.crs.CRS;

/**
 * A {@link Query} that selects features using a list of ids.
 * <p>
 * NOTE: Only KVP-based queries can be of this type. For XML-requests its only possible to use a filter constraint.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureIdQuery extends Query {

    private final String[] featureIds;

    /**
     * Creates a new {@link FilterQuery} instance.
     * 
     * @param handle
     *            client-generated query identifier, may be null
     * @param typeNames
     *            requested feature types (with optional aliases), must not be null and must always contain at least one
     *            entry
     * @param featureVersion
     *            version of the feature instances to be retrieved, may be null
     * @param srsName
     *            WFS-supported SRS that should be used for returned feature geometries, may be null
     * @param propertyNames
     *            properties of the features that should be retrieved, may be null
     * @param xLinkPropertyNames
     *            properties for which the the traversal of nested XLinks is selectively requested, may be null
     * @param functions
     *            properties for which a function value should be used instead of the original property value, may be
     *            null
     * @param sortBy
     *            properties whose values should be used to order the set of feature instances that satisfy the query,
     *            may be null
     * @param featureIds
     *            requested feature ids, must not be null
     */
    public FeatureIdQuery( String handle, TypeName[] typeNames, String featureVersion, CRS srsName,
                           PropertyName[] propertyNames, XLinkPropertyName[] xLinkPropertyNames, Function[] functions,
                           SortProperty[] sortBy, String[] featureIds ) {
        super( handle, typeNames, featureVersion, srsName, propertyNames, xLinkPropertyNames, functions, sortBy );
        if ( featureIds == null ) {
            throw new IllegalArgumentException();
        }
        this.featureIds = featureIds;
    }

    /**
     * Returns the requested feature ids.
     * 
     * @return the requested feature ids, never null
     */
    public String[] getFeatureIds() {
        return featureIds;
    }
}
