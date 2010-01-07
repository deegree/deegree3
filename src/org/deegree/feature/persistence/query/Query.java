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
package org.deegree.feature.persistence.query;

import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_LOOSE_BBOX;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_NO_GEOMETRIES;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_SCALE;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.crs.CRS;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wfs.getfeature.TypeName;

/**
 * Encapsulates the parameter of a query to a {@link FeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Query {

    /**
     * Names for hints and additional parameters that a {@link FeatureStore} implementation may take into account to
     * increase efficient query processing.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum QueryHint {
        /** If present, the store shall apply the argument (an {@link Envelope} as a pre-filtering step. */
        HINT_LOOSE_BBOX,
        /** If present, the store can omit the geometry objects in the output. */
        HINT_NO_GEOMETRIES,
        /** If present, the store can use a different LOD for the scale. */
        HINT_SCALE
    }

    private final TypeName[] typeNames;

    private final Filter filter;

    private final String featureVersion;

    private final CRS srsName;

    private final SortProperty[] sortBy;

    private final Map<QueryHint, Object> hints = new HashMap<QueryHint, Object>();

    /**
     * Creates a new {@link Query} instance.
     * 
     * @param ftName
     *            name of the requested feature type, must not be <code>null</code>
     * @param looseBbox
     *            bounding box used for pre-filtering the features, can be <code>null</code> (no pre-filtering)
     *            {@link QueryHint#HINT_LOOSE_BBOX}
     * @param filter
     *            additional filter constraints, may be <code>null</code>
     * @param withGeometries
     *            if false, the feature store may omit the geometry property values in the result
     *            {@link QueryHint#HINT_NO_GEOMETRIES}
     * @param scale
     *            if scale is positive, a scale query hint will be used
     */
    public Query( QName ftName, Envelope looseBbox, Filter filter, boolean withGeometries, int scale ) {
        this.typeNames = new TypeName[] { new TypeName( ftName, null ) };
        this.filter = filter;
        this.featureVersion = null;
        this.srsName = null;
        this.sortBy = null;
        hints.put( HINT_LOOSE_BBOX, looseBbox );
        if ( !withGeometries ) {
            hints.put( HINT_NO_GEOMETRIES, Boolean.TRUE );
        }
        if ( scale > 0 ) {
            hints.put( HINT_SCALE, scale );
        }
    }

    /**
     * Creates a new {@link Query} instance.
     * 
     * @param typeNames
     *            feature type names to be queried, must not be <code>null</code> and contain at least one entry
     * @param filter
     *            filter to be applied, can be <code>null</code>
     * @param featureVersion
     *            specific feature version to be returned, can be <code>null</code>
     * @param srsName
     *            SRS for the returned geometries, can be <code>null</code>
     * @param sortBy
     *            sort criteria to be applied, can be <code>null</code>
     */
    public Query( TypeName[] typeNames, Filter filter, String featureVersion, CRS srsName, SortProperty[] sortBy ) {
        this.typeNames = typeNames;
        this.filter = filter;
        this.featureVersion = featureVersion;
        this.srsName = srsName;
        this.sortBy = sortBy;
    }

    /**
     * Creates a new {@link Query} instance that selects features based on an {@link IdFilter}.
     * 
     * @param filter
     *            filter to be applied, must not be <code>null</code>
     * @param featureVersion
     *            specific feature version to be returned, can be <code>null</code>
     * @param srsName
     *            SRS for the returned geometries, can be <code>null</code>
     * @param sortBy
     *            sort criteria to be applied, can be <code>null</code>
     */
    public Query( IdFilter filter, String featureVersion, CRS srsName, SortProperty[] sortBy ) {
        this.typeNames = new TypeName[0];
        this.filter = filter;
        this.featureVersion = featureVersion;
        this.srsName = srsName;
        this.sortBy = sortBy;
    }

    public Object getHint( QueryHint code ) {
        return hints.get( code );
    }

    public TypeName[] getTypeNames() {
        return typeNames;
    }

    public Filter getFilter() {
        return filter;
    }

    public SortProperty[] getSortProperties() {
        return sortBy;
    }
}
