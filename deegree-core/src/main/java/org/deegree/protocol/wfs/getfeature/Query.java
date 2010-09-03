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

import org.deegree.cs.CRS;
import org.deegree.filter.sort.SortProperty;

/**
 * Represents a <code>Query</code> operation as a part of a {@link GetFeature} request.
 * 
 * @see GetFeature
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class Query {

    private final String handle;

    private final TypeName[] typeNames;

    private final String featureVersion;

    private final CRS srsName;

    private final SortProperty[] sortBy;

    /**
     * Creates a new {@link Query} instance.
     * 
     * @param handle
     *            client-generated query identifier, may be null
     * @param typeNames
     *            requested feature types (with optional aliases), can be null
     * @param featureVersion
     *            version of the feature instances to be retrieved, may be null
     * @param srsName
     *            WFS-supported SRS that should be used for returned feature geometries, may be null
     * @param sortBy
     *            properties whose values should be used to order the set of feature instances that satisfy the query,
     *            may be null
     */
    public Query( String handle, TypeName[] typeNames, String featureVersion, CRS srsName, 
                  SortProperty[] sortBy ) {
        this.handle = handle;
        this.typeNames = typeNames;
        this.featureVersion = featureVersion;
        this.srsName = srsName;
        this.sortBy = sortBy;
    }

    /**
     * Returns the client-generated identifier supplied with the query.
     * 
     * @return the client-generated identifier, may be null
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Returns the requested feature types (with optional aliases).
     * 
     * @return the requested feature types, never null and contains always one entry
     */
    public TypeName[] getTypeNames() {
        return typeNames;
    }

    /**
     * Returns the version of the feature instances to be retrieved.
     * 
     * @return the version of the feature instances to be retrieved, may be null
     */
    public String getFeatureVersion() {
        return featureVersion;
    }

    /**
     * Returns the SRS that should be used for returned feature geometries.
     * 
     * @return the SRS that should be used for returned feature geometries, may be null
     */
    public CRS getSrsName() {
        return srsName;
    }

    /**
     * Returns the properties whose values should be used to order the set of feature instances that satisfy the query.
     * 
     * @return sort criteria, may be null
     */
    public SortProperty[] getSortBy() {
        return sortBy;
    }
}
