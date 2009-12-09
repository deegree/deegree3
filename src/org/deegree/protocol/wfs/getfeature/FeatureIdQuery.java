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

import org.deegree.crs.CRS;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;

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

    private final PropertyName[][] propertyNames;

    private final XLinkPropertyName[][] xLinkPropertyNames;

    /**
     * Creates a new {@link FeatureIdQuery} instance.
     * 
     * @param handle
     *            client-generated query identifier, may be null
     * @param typeNames
     *            requested feature types (with optional aliases), must not be <code>null</code>, but can be empty
     * @param featureVersion
     *            version of the feature instances to be retrieved, may be null
     * @param srsName
     *            WFS-supported SRS that should be used for returned feature geometries, may be null
     * @param propertyNames
     *            properties of the features that should be retrieved, may be null
     * @param xLinkPropertyNames
     *            properties for which the the traversal of nested XLinks is selectively requested, may be null
     * @param sortBy
     *            properties whose values should be used to order the set of feature instances that satisfy the query,
     *            may be null
     * @param featureIds
     *            requested feature ids, must not be null
     */
    public FeatureIdQuery( String handle, TypeName[] typeNames, String[] featureIds, String featureVersion,
                           CRS srsName, PropertyName[][] propertyNames, XLinkPropertyName[][] xLinkPropertyNames,
                           SortProperty[] sortBy ) {
        super( handle, typeNames, featureVersion, srsName, sortBy );
        if ( featureIds == null ) {
            throw new IllegalArgumentException();
        }
        this.xLinkPropertyNames = xLinkPropertyNames;
        this.propertyNames = propertyNames;
        this.featureIds = featureIds;
    }

    /**
     * <p>
     * From WFS Speification V1.1, clause 14.7.3.1: <i>A list of properties may be specified for each feature type that
     * is being queried. A "*" character can be used to indicate that all properties should be retrieved. There is a 1:1
     * mapping between each element in a FEATUREID or TYPENAME list and the PROPERTYNAME list. The absense of a value
     * also indicates that all properties should be fetched.</i>
     * </p>
     * 
     * @return the properties of the features that should be retrieved, may be null
     */
    public PropertyName[][] getPropertyNames() {
        return propertyNames;
    }

    /**
     * Returns the properties for which the the traversal of nested XLinks is selectively requested.
     * 
     * @return the properties for which the the traversal of nested XLinks is selectively requested, may be null
     */
    public XLinkPropertyName[][] getXLinkPropertyNames() {
        return xLinkPropertyNames;
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
