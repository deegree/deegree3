//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.feature.persistence.sql.xpath;

import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.types.FeatureType;

/**
 * Encapsulates the tuple of the {@link FeatureTypeMapping}, {@link FeatureTypeMapping} and the alias from the query.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class QueryFeatureTypeMapping {

    private final String alias;

    private final FeatureType featureType;

    private final FeatureTypeMapping featureTypeMapping;

    /**
     * @param featureType
     *            never <code>null</code>
     * @param ftMapping
     *            never <code>null</code>
     */
    public QueryFeatureTypeMapping( FeatureType featureType, FeatureTypeMapping ftMapping ) {
        this( null, featureType, ftMapping );
    }

    /**
     * 
     * @param alias
     *            from the query, may be <code>null</code>
     * @param featureType
     *            never <code>null</code>
     * @param ftMapping
     *            never <code>null</code>
     */
    public QueryFeatureTypeMapping( String alias, FeatureType featureType, FeatureTypeMapping featureTypeMapping ) {
        this.alias = alias;
        this.featureType = featureType;
        this.featureTypeMapping = featureTypeMapping;
    }

    /**
     * @return the alias from the query, may be <code>null</code>
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return the featureTypeMapping never <code>null</code>
     */
    public FeatureTypeMapping getFeatureTypeMapping() {
        return featureTypeMapping;
    }

    /**
     * @return the featureType never <code>null</code>
     */
    public FeatureType getFeatureType() {
        return featureType;
    }

}
