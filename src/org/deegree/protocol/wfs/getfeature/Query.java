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
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.PropertyName;
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

    private final PropertyName[] propertyNames;

    private final XLinkPropertyName[] xLinkPropertyNames;

    private final Function[] functions;

    private final SortProperty[] sortBy;

    /**
     * Creates a new {@link Query} instance.
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
     */
    public Query( String handle, TypeName[] typeNames, String featureVersion, CRS srsName,
                  PropertyName[] propertyNames, XLinkPropertyName[] xLinkPropertyNames, Function[] functions,
                  SortProperty[] sortBy ) {
        this.handle = handle;
        if ( typeNames == null || typeNames.length == 0 ) {
            throw new IllegalArgumentException();
        }
        this.typeNames = typeNames;
        this.featureVersion = featureVersion;
        this.srsName = srsName;
        this.propertyNames = propertyNames;
        this.xLinkPropertyNames = xLinkPropertyNames;
        this.functions = functions;
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
     * Returns the properties of the features that should be retrieved.
     * <p>
     * From WFS 1.1.0 schema (wfs.xsd): <i>While a Web Feature Service should endeavour to satisfy the exact request
     * specified, in some instance this may not be possible. Specifically, a Web Feature Service must generate a valid
     * GML3 response to a Query operation. The schema used to generate the output may include properties that are
     * mandatory. In order that the output validates, these mandatory properties must be specified in the request. If
     * they are not, a Web Feature Service may add them automatically to the Query before processing it. Thus a client
     * application should, in general, be prepared to receive more properties than it requested.</i>
     * </p>
     *
     * @return the properties of the features that should be retrieved, may be null
     */
    public PropertyName[] getPropertyNames() {
        return propertyNames;
    }

    /**
     * Returns the properties for which the the traversal of nested XLinks is selectively requested.
     *
     * @return the properties for which the the traversal of nested XLinks is selectively requested, may be null
     */
    public XLinkPropertyName[] getXLinkPropertyNames() {
        return xLinkPropertyNames;
    }

    /**
     * Returns the functions that should be fetched instead of the original property values.
     *
     * @return the functions that should be fetched instead of the original property values, may be null
     */
    public Function[] getFunctions() {
        return functions;
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
