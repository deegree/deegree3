//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs Schmitz & Schneider GbR
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.feature;

import static java.lang.System.currentTimeMillis;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.commons.utils.time.DateUtils.formatISO8601Date;
import static org.deegree.commons.utils.time.DateUtils.formatISO8601DateWOMS;
import static org.deegree.layer.dims.Dimension.formatDimensionValueList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.Filters;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Or;
import org.deegree.geometry.Envelope;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.dims.DimensionInterval;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.utils.Styles;
import org.slf4j.Logger;

/**
 * @author stranger
 * 
 */
public class FeatureLayer extends AbstractLayer {

    private static final Logger LOG = getLogger( FeatureLayer.class );

    private FeatureStore featureStore;

    private OperatorFilter filter;

    private final QName featureType;

    /**
     * @param md
     */
    protected FeatureLayer( LayerMetadata md, FeatureStore featureStore, QName featureType, OperatorFilter filter,
                            Map<String, Style> styles, Map<String, Style> legendStyles ) {
        super( md );
        this.featureStore = featureStore;
        this.featureType = featureType;
        this.filter = filter;
    }

    @Override
    public void paintMap( RenderContext context, RenderingInfo info, Style style ) {
        OperatorFilter filter = this.filter;
        filter = Filters.and( filter, Styles.getStyleFilters( style, info.getScale() ) );
        filter = Filters.and( filter, info.getExtraFilter( getMetadata().getName() ) );
    }

    /**
     * @param style
     * @param gm
     * @param queries
     * @return a list of dimension warnings
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    // public LinkedList<String> collectQueries( Style style, final RenderingInfo gm, LinkedList<Query> queries )
    // {
    // final Envelope bbox = gm.getEnvelope();
    //
    // Set<Expression> exprs = new HashSet<Expression>(Styles.getGeometryExpressions( style ));
    //
    // final ValueReference geomProp;
    //
    // if ( exprs.size() == 1 && exprs.iterator().next() instanceof ValueReference ) {
    // geomProp = (ValueReference) exprs.iterator().next();
    // } else {
    // geomProp = null;
    // }
    //
    // final Pair<OperatorFilter, LinkedList<String>> dimFilter = getDimensionFilter( gm.getDimensions() );
    // final Filter filter = Filters.and(gm.getExtraFilter( getMetadata().getName()), dimFilter.first );
    // if ( style != null ) {
    // QName ftName = featureType== null ? style.getFeatureType() : featureType;
    // if ( ftName != null && featureStore.getSchema().getFeatureType( ftName ) == null ) {
    // LOG.warn( "FeatureType '" + ftName + "' from style is not known to the FeatureStore." );
    // return new LinkedList<String>();
    // }
    // }
    //
    // QName featureType = style == null ? null : style.getFeatureType();
    // Integer maxFeats = gm.getExtensions().getMaxFeatures().get( getMetadata().getName() );
    // final int maxFeatures = maxFeats == null ? -1 : maxFeats;
    // if ( featureType == null && featureStore != null ) {
    // queries.addAll( map( featureStore.getSchema().getFeatureTypes( null, false, false ),
    // new Mapper<Query, FeatureType>() {
    // @Override
    // public Query apply( FeatureType u ) {
    // Filter fil = Filters.addBBoxConstraint( bbox, filter, geomProp );
    // return new Query( u.getName(), fil, round( gm.getScale() ), maxFeatures,
    // gm.getResolution() );
    // }
    // } ) );
    // } else {
    // Query query = new Query( featureType, Filters.addBBoxConstraint( bbox, filter, geomProp ),
    // round( gm.getScale() ), maxFeatures, gm.getResolution() );
    // queries.add( query );
    // }
    // return dimFilter == null ? new LinkedList<String>() : dimFilter.second;
    // }

}
