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
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_RESOLUTION;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_SCALE;
import static org.deegree.filter.Filter.Type.OPERATOR_FILTER;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.cs.CRS;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.SpatialOperator;
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
        /** If present, the store can use a different LOD for the scale. */
        HINT_SCALE,
        /** If present, the store can simplify geometries according to the resolution. */
        HINT_RESOLUTION
    }

    private final TypeName[] typeNames;

    private final Filter filter;

    private final String featureVersion;

    private final CRS srsName;

    private final SortProperty[] sortBy;

    private final Map<QueryHint, Object> hints = new HashMap<QueryHint, Object>();

    private int maxFeatures = -1;

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
     * @param scale
     *            if scale is positive, a scale query hint will be used
     * @param maxFeatures
     *            may be -1 if no limit needs to be exercised
     * @param resolution
     *            if resolution is positive, a pixel resolution hint will be used
     */
    public Query( QName ftName, Envelope looseBbox, Filter filter, int scale, int maxFeatures, double resolution ) {
        this.typeNames = new TypeName[] { new TypeName( ftName, null ) };
        this.filter = filter;
        this.featureVersion = null;
        this.srsName = null;
        this.sortBy = null;
        this.maxFeatures = maxFeatures;
        hints.put( HINT_LOOSE_BBOX, looseBbox );
        if ( scale > 0 ) {
            hints.put( HINT_SCALE, scale );
        }
        if ( resolution > 0 ) {
            hints.put( HINT_RESOLUTION, resolution );
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

    /**
     * Returns an {@link Envelope} suitable for performing a spatial pre-filtering step on the set of feature
     * candidates.
     * <p>
     * The returned {@link Envelope} is determined by the following strategy:
     * <ul>
     * <li>If a loose bbox is available ({@link QueryHint#HINT_LOOSE_BBOX}), it is returned.</li>
     * <li>If no loose bbox is available, but the {@link Query} contains an {@link OperatorFilter}, it is attempted to
     * extract an {@link Envelope} from it. Note that the envelope is only used, when the corresponding property name
     * targets a property of the root feature (and not a property of a subfeature).</li>
     * <li>If neither a loose bbox is available, nor a bbox can be extracted from the filter, <code>null</code> is
     * returned.</li>
     * </ul>
     * </p>
     * 
     * @return an {@link Envelope} suitable for pre-filtering feature candidates, can be <code>null</code>
     */
    public Envelope getPrefilterBBox() {
        Envelope env = (Envelope) getHint( HINT_LOOSE_BBOX );
        if ( env == null && filter != null && filter.getType() == OPERATOR_FILTER ) {
            OperatorFilter of = (OperatorFilter) filter;
            Operator oper = of.getOperator();
            env = extractBBox( oper );
        }
        return env;
    }

    // TODO implement full strategy
    private Envelope extractBBox( Operator oper ) {
        switch ( oper.getType() ) {
        case COMPARISON: {
            return null;
        }
        case LOGICAL: {
            return null;
        }
        case SPATIAL: {
            return extractBBox( (SpatialOperator) oper );
        }
        }
        return null;
    }

    private Envelope extractBBox( SpatialOperator oper ) {
        switch ( oper.getSubType() ) {
        case BBOX: {
            return ( (BBOX) oper ).getBoundingBox();
        }
        default: {
            // TODO implement full strategy
            return null;
        }
        }
    }

    /**
     * Returns the names of the requested feature types.
     * 
     * @return the names of the requested feature types, never <code>null</code> (but may be empty for id filter
     *         queries)
     */
    public TypeName[] getTypeNames() {
        return typeNames;
    }

    public Filter getFilter() {
        return filter;
    }

    public SortProperty[] getSortProperties() {
        return sortBy;
    }

    /**
     * @return -1, if no limit has been set
     */
    public int getMaxFeatures() {
        return maxFeatures;
    }

}
