//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.feature;

import static org.deegree.commons.utils.CollectionUtils.clearNulls;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.filter.Filters.addBBoxConstraint;
import static org.deegree.layer.persistence.feature.FilterBuilder.buildFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.BinaryComparisonOperator;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNil;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.temporal.TemporalOperator;
import org.deegree.geometry.Envelope;
import org.deegree.layer.LayerQuery;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.style.se.parser.SymbologyParser.FilterContinuation;
import org.deegree.style.se.unevaluated.Style;

/**
 * Builds feature store queries for feature layers.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class QueryBuilder {

    private FeatureStore featureStore;

    private OperatorFilter filter;

    private QName ftName;

    private Envelope bbox;

    private LayerQuery query;

    private ValueReference geomProp;

    private SortProperty[] sortBy;

    private String layerName;

    private Style style;

    QueryBuilder( FeatureStore featureStore, OperatorFilter filter, QName ftName, Envelope bbox, LayerQuery query,
                  ValueReference geomProp, SortProperty[] sortBy, String layerName, Style style ) {
        this.featureStore = featureStore;
        this.filter = filter;
        this.ftName = ftName;
        this.bbox = bbox;
        this.query = query;
        this.geomProp = geomProp;
        this.sortBy = sortBy;
        this.layerName = layerName;
        this.style = style;
    }

    List<Query> buildMapQueries() {
        List<Query> queries = new ArrayList<Query>();
        Integer maxFeats = query.getRenderingOptions().getMaxFeatures( layerName );
        final int maxFeatures = maxFeats == null ? -1 : maxFeats;
        final List<ValueReference> valueReferences = collectValueReferences();
        if ( ftName == null && featureStore != null ) {
            final Filter filter2 = filter;
            queries.addAll( map( featureStore.getSchema().getFeatureTypes( null, false, false ),
                                 new Mapper<Query, FeatureType>() {
                                     @Override
                                     public Query apply( FeatureType u ) {
                                         Filter fil = addBBoxConstraint( bbox, filter2, geomProp, true );
                                         return createQuery( u.getName(), fil, round( query.getScale() ), maxFeatures,
                                                             query.getResolution(), sortBy, valueReferences );
                                     }
                                 } ) );
        } else {
            Query fquery = createQuery( ftName, addBBoxConstraint( bbox, filter, geomProp, true ),
                                        round( query.getScale() ), maxFeatures, query.getResolution(), sortBy,
                                        valueReferences );
            queries.add( fquery );
        }

        return queries;
    }

    List<Query> buildInfoQueries() {
        List<Query> queries = new ArrayList<Query>();
        if ( ftName == null ) {
            queries.addAll( map( featureStore.getSchema().getFeatureTypes( null, false, false ),
                                 new Mapper<Query, FeatureType>() {
                                     @Override
                                     public Query apply( FeatureType u ) {
                                         Filter f;
                                         if ( filter == null ) {
                                             f = buildFilter( null, u, bbox );
                                         } else {
                                             f = buildFilter( ( (OperatorFilter) filter ).getOperator(), u, bbox );
                                         }
                                         return createQuery( u.getName(), f, -1, query.getFeatureCount(), -1, sortBy );
                                     }
                                 } ) );
            clearNulls( queries );
        } else {
            Filter f;
            if ( filter == null ) {
                f = buildFilter( null, featureStore.getSchema().getFeatureType( ftName ), bbox );
            } else {
                f = buildFilter( ( (OperatorFilter) filter ).getOperator(),
                                 featureStore.getSchema().getFeatureType( ftName ), bbox );
            }
            queries.add( createQuery( ftName, f, -1, query.getFeatureCount(), -1, sortBy ) );
        }
        return queries;
    }

    static Query createQuery( QName ftName, Filter filter, int scale, int maxFeatures, double resolution,
                              SortProperty[] sort ) {
        TypeName[] typeNames = new TypeName[] { new TypeName( ftName, null ) };
        return new Query( typeNames, filter, sort, scale, maxFeatures, resolution );
    }

    static Query createQuery( QName ftName, Filter filter, int scale, int maxFeatures, double resolution,
                              SortProperty[] sort, List<ValueReference> styleValueReferences ) {
        TypeName[] typeNames = new TypeName[] { new TypeName( ftName, null ) };
        return new Query( typeNames, filter, sort, scale, maxFeatures, resolution, styleValueReferences );
    }

    static List<ValueReference> parseValueReferencesFromFilter( OperatorFilter filter ) {
        List<ValueReference> allValueReferences = new ArrayList<ValueReference>();
        if ( filter != null ) {
            Operator operator = filter.getOperator();
            addValueReferences( operator, allValueReferences );
        }
        return allValueReferences;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static List<ValueReference> parseValueReferencesFromStyle( Style style ) {
        List<ValueReference> valueReferencesFromStyle = new ArrayList<ValueReference>();
        if ( style != null ) {
            LinkedList<Pair<?, ?>> rules = (LinkedList) style.getRules();
            for ( Pair<?, ?> p : rules ) {
                if ( p.first != null && p.first instanceof FilterContinuation ) {
                    FilterContinuation contn = (FilterContinuation) p.first;
                    if ( contn.filter != null ) {
                        valueReferencesFromStyle.addAll( parseValueReferencesFromFilter( (OperatorFilter) contn.filter ) );
                    }
                }
            }
        }
        return valueReferencesFromStyle;
    }

    private static void addValueReferences( Operator operator, List<ValueReference> allValueReferences ) {
        switch ( operator.getType() ) {
        case COMPARISON:
            addValueReferences( (ComparisonOperator) operator, allValueReferences );
            break;
        case LOGICAL:
            addValueReferences( (LogicalOperator) operator, allValueReferences );
            break;
        case SPATIAL:
            // spatial operators are not required (spatial properties are always requested)
            break;
        case TEMPORAL:
            TemporalOperator temporalOperator = (TemporalOperator) operator;
            addValueReferences( temporalOperator.getParameter1(), allValueReferences );
            addValueReferences( temporalOperator.getParameter2(), allValueReferences );
            break;
        default:
            break;
        }
    }

    private static void addValueReferences( ComparisonOperator operator, List<ValueReference> allValueReferences ) {
        switch ( operator.getSubType() ) {
        case PROPERTY_IS_BETWEEN:
            PropertyIsBetween propertyIsBetween = (PropertyIsBetween) operator;
            addValueReferences( propertyIsBetween.getLowerBoundary(), allValueReferences );
            addValueReferences( propertyIsBetween.getExpression(), allValueReferences );
            addValueReferences( propertyIsBetween.getUpperBoundary(), allValueReferences );
            break;
        case PROPERTY_IS_EQUAL_TO:
        case PROPERTY_IS_GREATER_THAN:
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
        case PROPERTY_IS_LESS_THAN:
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
        case PROPERTY_IS_NOT_EQUAL_TO:
            BinaryComparisonOperator binaryComparisonOperator = (BinaryComparisonOperator) operator;
            addValueReferences( binaryComparisonOperator.getParameter1(), allValueReferences );
            addValueReferences( binaryComparisonOperator.getParameter2(), allValueReferences );
            break;
        case PROPERTY_IS_LIKE:
            PropertyIsLike propertyIsLike = (PropertyIsLike) operator;
            addValueReferences( propertyIsLike.getExpression(), allValueReferences );
            break;
        case PROPERTY_IS_NIL:
            PropertyIsNil propertyIsNil = (PropertyIsNil) operator;
            addValueReferences( propertyIsNil.getPropertyName(), allValueReferences );
            break;
        case PROPERTY_IS_NULL:
            PropertyIsNull propertyIsNull = (PropertyIsNull) operator;
            addValueReferences( propertyIsNull.getPropertyName(), allValueReferences );
            break;
        default:
            break;
        }
    }

    private static void addValueReferences( LogicalOperator operator, List<ValueReference> allValueReferences ) {
        Operator[] params = operator.getParams();
        for ( Operator param : params ) {
            addValueReferences( param, allValueReferences );
        }
    }

    private static void addValueReferences( Expression expression, List<ValueReference> allValueReferences ) {
        if ( Expression.Type.VALUE_REFERENCE.equals( expression.getType() ) )
            allValueReferences.add( (ValueReference) expression );
    }

    private List<ValueReference> collectValueReferences() {
        List<ValueReference> valueReferences = new ArrayList<ValueReference>();
        valueReferences.addAll( style.retrieveValueReferences() );
        valueReferences.addAll( parseValueReferencesFromFilter( filter ) );
        valueReferences.addAll( parseValueReferencesFromStyle( style ) );
        return valueReferences;
    }

}
