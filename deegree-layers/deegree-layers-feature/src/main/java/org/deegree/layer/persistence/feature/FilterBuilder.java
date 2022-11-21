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

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.filter.Expression;
import org.deegree.filter.Filters;
import org.deegree.filter.MatchAction;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Or;
import org.deegree.filter.spatial.Intersects;
import org.deegree.geometry.Envelope;
import org.deegree.layer.LayerQuery;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.utils.Styles;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2_OR_3;

/**
 * Responsible for building feature layer filters.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * @version $Revision: $, $Date: $
 */
class FilterBuilder {

    static OperatorFilter buildFilterForMap( OperatorFilter filter, Style style, LayerQuery query,
                                             DimensionFilterBuilder dimFilterBuilder, List<String> headers )
                            throws OWSException {
        style = style.filter( query.getScale() );
        filter = Filters.and( filter, Styles.getStyleFilters( style, query.getScale() ) );
        filter = Filters.and( filter, query.getFilter() );
        filter = Filters.and( filter, dimFilterBuilder.getDimensionFilter( query.getDimensions(), headers ) );
        return filter;
    }

    static OperatorFilter buildFilter( Operator operator, FeatureType ft, Envelope clickBox ) {
        if ( ft == null ) {
            if ( operator == null ) {
                return null;
            }
            return new OperatorFilter( operator );
        }
        LinkedList<Operator> list = findOperators( ft, clickBox );
        if ( list.size() > 1 ) {
            Or or = new Or( list.toArray( new Operator[list.size()] ) );
            if ( operator == null ) {
                return new OperatorFilter( or );
            }
            return new OperatorFilter( new And( operator, or ) );
        }
        if ( list.isEmpty() ) {
            // obnoxious case where feature has no geometry properties (but features may have extra geometry props)
            if ( operator == null ) {
                return new OperatorFilter( new Intersects( null, clickBox ) );
            }
            return new OperatorFilter( new And( operator, new Intersects( null, clickBox ) ) );
        }
        if ( operator == null ) {
            return new OperatorFilter( list.get( 0 ) );
        }
        return new OperatorFilter( new And( operator, list.get( 0 ) ) );
    }

    private static LinkedList<Operator> findOperators( FeatureType ft, Envelope clickBox ) {
        LinkedList<Operator> list = new LinkedList<Operator>();
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            if ( pt instanceof GeometryPropertyType
                 && ( ( (GeometryPropertyType) pt ).getCoordinateDimension() == DIM_2 ||
                      ( (GeometryPropertyType) pt ).getCoordinateDimension() == DIM_2_OR_3 ) ) {
                list.add( new Intersects( new ValueReference( pt.getName() ), clickBox ) );
            }
        }
        return list;
    }

    static OperatorFilter appendRequestFilter( OperatorFilter filter, LayerQuery query, Set<QName> propertyNames ) {
        OperatorFilter requestFilter = buildRequestFilter( query, propertyNames );
        return Filters.and( filter, requestFilter );
    }

    static OperatorFilter buildRequestFilter( LayerQuery layerQuery, Set<QName> propertyNames ) {
        Pair<String, List<String>> requestFilter = layerQuery.requestFilter();
        if ( requestFilter == null )
            return null;

        List<ComparisonOperator> operators = createOperatorsIfPropertyIsKnown( requestFilter, propertyNames );
        if ( operators.isEmpty() )
            return null;
        if ( operators.size() == 1 )
            return new OperatorFilter( operators.get( 0 ) );
        return new OperatorFilter( new Or( operators.toArray( new Operator[operators.size()] ) ) );
    }

    private static List<ComparisonOperator> createOperatorsIfPropertyIsKnown( Pair<String, List<String>> requestFilter,
                                                                              Set<QName> propertyNames ) {
        String filterProperty = requestFilter.getFirst();
        if ( propertyIsknown( filterProperty, propertyNames ) ) {
            return createOperatorsIfPropertyIsKnown( requestFilter, filterProperty );
        }
        return Collections.emptyList();
    }

    private static List<ComparisonOperator> createOperatorsIfPropertyIsKnown( Pair<String, List<String>> requestFilter,
                                                                              String filterProperty ) {
        List<ComparisonOperator> operators = new ArrayList<ComparisonOperator>();
        List<String> filterValues = requestFilter.getSecond();
        for ( String filterValue : filterValues ) {
            Expression filterPropertyExpression = new ValueReference( new QName( filterProperty ) );
            Expression filterValueExpression = new Literal<PrimitiveValue>( filterValue );
            PropertyIsEqualTo isEqualTo = new PropertyIsEqualTo( filterPropertyExpression,
                                                                 filterValueExpression, false,
                                                                 MatchAction.ALL );
            operators.add( isEqualTo );
        }
        return operators;
    }

    private static boolean propertyIsknown( String filterProperty, Set<QName> propertyNames ) {
        for ( QName propertyName : propertyNames ) {
            if ( filterProperty.equals( propertyName.getLocalPart() ) )
                return true;
        }
        return false;
    }

}