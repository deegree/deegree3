//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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

import static java.util.Collections.emptyList;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_RESOLUTION;
import static org.deegree.feature.persistence.query.Query.QueryHint.HINT_SCALE;
import static org.deegree.filter.Filters.extractPrefilterBBoxConstraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.filter.Filter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
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

    private boolean strict;

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
        /** If present, the store can use a different LOD for the scale. */
        HINT_SCALE,
        /** If present, the store can simplify geometries according to the resolution. */
        HINT_RESOLUTION
    }

    private final TypeName[] typeNames;

    private final Filter filter;

    // private final String featureVersion;
    //
    // private final ICRS srsName;

    private final SortProperty[] sortBy;

    private final Map<QueryHint, Object> hints = new HashMap<QueryHint, Object>();

    private int maxFeatures = -1;

    private int startIndex = 0;

    private final List<ProjectionClause> projections;

    /**
     * Creates a new {@link Query} instance.
     * 
     * @param ftName
     *            name of the requested feature type, must not be <code>null</code>
     * @param filter
     *            additional filter constraints, may be <code>null</code>, if not <code>null</code>, all contained
     *            geometry operands must have a non-null {@link CRS}
     * @param scale
     *            if scale is positive, a scale query hint will be used
     * @param maxFeatures
     *            may be -1 if no limit needs to be exercised
     * @param resolution
     *            if resolution is positive, a pixel resolution hint will be used
     */
    public Query( QName ftName, Filter filter, int scale, int maxFeatures, double resolution ) {
        this.typeNames = new TypeName[] { new TypeName( ftName, null ) };
        this.filter = filter;
        this.maxFeatures = maxFeatures;
        if ( scale > 0 ) {
            hints.put( HINT_SCALE, scale );
        }
        if ( resolution > 0 ) {
            hints.put( HINT_RESOLUTION, resolution );
        }
        this.sortBy = new SortProperty[0];
        this.projections = emptyList();
    }

    /**
     * Creates a new {@link Query} instance.
     * 
     * @param typeNames
     *            feature type names to be queried, must not be <code>null</code> and contain at least one entry
     * @param filter
     *            filter to be applied, can be <code>null</code>, if not <code>null</code>, all contained geometry
     *            operands must have a non-null {@link CRS}
     * @param featureVersion
     *            specific feature version to be returned, can be <code>null</code>
     * @param srsName
     *            SRS for the returned geometries, can be <code>null</code>
     * @param sortBy
     *            sort criteria to be applied, can be <code>null</code>
     */
    public Query( TypeName[] typeNames, Filter filter, String featureVersion, ICRS srsName, SortProperty[] sortBy ) {
        this.typeNames = typeNames;
        this.filter = filter;
        if ( sortBy != null ) {
            this.sortBy = sortBy;
        } else {
            this.sortBy = new SortProperty[0];
        }
        this.projections = emptyList();
    }

    /**
     * Creates a new {@link Query} instance.
     *
     * @param typeNames
     *            feature type names to be queried, must not be <code>null</code> and contain at least one entry
     * @param filter
     *            filter to be applied, can be <code>null</code>, if not <code>null</code>, all contained geometry
     *            operands must have a non-null {@link CRS}
     * @param sortBy
     *            sort criteria to be applied, can be <code>null</code>
     * @param maxFeatures
     *            number of features to return, if not specified: -1
     * @param startIndex
     *            index of the first feature to return, default: 0
     */
    public Query( TypeName[] typeNames, Filter filter, SortProperty[] sortBy, int maxFeatures, int startIndex ) {
        this.typeNames = typeNames;
        this.filter = filter;
        this.maxFeatures = maxFeatures;
        this.startIndex = startIndex;
        if ( sortBy != null ) {
            this.sortBy = sortBy;
        } else {
            this.sortBy = new SortProperty[0];
        }
        this.projections = emptyList();
    }

    /**
     * Creates a new {@link Query} instance.
     * 
     * @param typeNames
     *            feature type names to be queried, must not be <code>null</code> and contain at least one entry
     * @param filter
     *            filter to be applied, can be <code>null</code>, if not <code>null</code>, all contained geometry
     *            operands must have a non-null {@link CRS}
     * @param sortBy
     *            sort criteria to be applied, can be <code>null</code>
     * @param scale
     *            if scale is positive, a scale query hint will be used
     * @param maxFeatures
     *            may be -1 if no limit needs to be exercised
     * @param resolution
     *            if resolution is positive, a pixel resolution hint will be used
     */
    public Query( TypeName[] typeNames, Filter filter, SortProperty[] sortBy, int scale, int maxFeatures,
                  double resolution ) {
        this.typeNames = typeNames;
        this.filter = filter;
        if ( sortBy != null ) {
            this.sortBy = sortBy;
        } else {
            this.sortBy = new SortProperty[0];
        }
        this.maxFeatures = maxFeatures;
        if ( scale > 0 ) {
            hints.put( HINT_SCALE, scale );
        }
        if ( resolution > 0 ) {
            hints.put( HINT_RESOLUTION, resolution );
        }
        this.projections = emptyList();
    }

    public void setHandleStrict( boolean strict ) {
        this.strict = strict;
    }

    public boolean isHandleStrict() {
        return this.strict;
    }

    public Object getHint( QueryHint code ) {
        return hints.get( code );
    }

    /**
     * Tries to extract a {@link BBOX} constraint from the query {@link Filter} that can be used as a pre-filtering step
     * to narrow the result set.
     * <p>
     * The returned {@link Envelope} is determined by the following strategy:
     * <ul>
     * <li>If the filter is an {@link OperatorFilter}, it is attempted to extract an {@link BBOX} constraint from it.</li>
     * <li>If no {@link BBOX} constraint can be extracted from the filter (not presented or nested in <code>Or</code> or
     * <code>Not</code> expressions, <code>null</code> is returned.</li>
     * </ul>
     * </p>
     * 
     * @return a {@link BBOX} suitable for pre-filtering feature candidates, can be <code>null</code>
     */
    public BBOX getPrefilterBBox() {
        return extractPrefilterBBoxConstraint( filter );
    }

    /**
     * Tries to extract an {@link Envelope} from the query {@link Filter} that can be used as a pre-filtering step to
     * narrow the result set.
     * <p>
     * The returned {@link Envelope} is determined by the following strategy:
     * <ul>
     * <li>If the filter is an {@link OperatorFilter}, it is attempted to extract an {@link BBOX} constraint from it.</li>
     * <li>If no {@link BBOX} constraint can be extracted from the filter (not presented or nested in <code>Or</code> or
     * <code>Not</code> expressions, <code>null</code> is returned.</li>
     * </ul>
     * </p>
     * 
     * @return a {@link Envelope} suitable for pre-filtering feature candidates, can be <code>null</code>
     */
    public Envelope getPrefilterBBoxEnvelope() {
        BBOX bbox = extractPrefilterBBoxConstraint( filter );
        if ( bbox == null ) {
            return null;
        }
        return bbox.getBoundingBox();
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

    /**
     * Returns the {@link Filter}.
     * 
     * @return filter, may be <code>null</code>
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Returns the sort criteria.
     * 
     * @return the sort criteria, never <code>null</code> (but may be empty)
     */
    public SortProperty[] getSortProperties() {
        return sortBy;
    }

    /**
     * Returns the projections to be applied to returned features.
     * 
     * @return projections to be applied to returned features, never <code>null</code> (but can be empty)
     */
    public List<ProjectionClause> getProjections() {
        return projections;
    }

    /**
     * @return -1, if no limit has been set
     */
    public int getMaxFeatures() {
        return maxFeatures;
    }

    /**
     * @return the index of the first feature to return
     */
    public int getStartIndex() {
        return startIndex;
    }

}