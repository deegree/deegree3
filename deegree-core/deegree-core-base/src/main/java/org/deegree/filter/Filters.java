//$HeadURL$
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
package org.deegree.filter;

import static java.util.Arrays.asList;
import static org.deegree.filter.Filter.Type.OPERATOR_FILTER;
import static org.deegree.filter.Operator.Type.LOGICAL;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNil;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.Add;
import org.deegree.filter.expression.Div;
import org.deegree.filter.expression.Mul;
import org.deegree.filter.expression.Sub;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.Contains;
import org.deegree.filter.spatial.Crosses;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.Equals;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.spatial.Overlaps;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.spatial.SpatialOperator.SubType;
import org.deegree.filter.spatial.Touches;
import org.deegree.filter.spatial.Within;
import org.deegree.filter.temporal.After;
import org.deegree.filter.temporal.AnyInteracts;
import org.deegree.filter.temporal.Before;
import org.deegree.filter.temporal.Begins;
import org.deegree.filter.temporal.BegunBy;
import org.deegree.filter.temporal.During;
import org.deegree.filter.temporal.EndedBy;
import org.deegree.filter.temporal.Meets;
import org.deegree.filter.temporal.MetBy;
import org.deegree.filter.temporal.OverlappedBy;
import org.deegree.filter.temporal.TContains;
import org.deegree.filter.temporal.TEquals;
import org.deegree.filter.temporal.TOverlaps;
import org.deegree.filter.temporal.TemporalOperator;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various static methods for performing standard tasks on {@link Filter} objects.
 * 
 * @see Filter
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Filters {

    private static Logger LOG = LoggerFactory.getLogger( Filters.class );

    /**
     * Extract all operators from an And-filter (which can all be filtered individually, as each operator can only
     * narrow the result).
     *
     * @return list of operators, can be <code>null</code> (not an And-filter)
     */
    public static List<Operator> extractAndOperands( final Filter filter ) {
        if ( filter != null && filter.getType() == OPERATOR_FILTER ) {
            final OperatorFilter operatorFilter = (OperatorFilter) filter;
            final Operator operator = operatorFilter.getOperator();
            if ( operator.getType() == LOGICAL ) {
                final LogicalOperator logicalOperator = (LogicalOperator) operator;
                if ( logicalOperator.getSubType() == LogicalOperator.SubType.AND ) {
                    return asList( logicalOperator.getParams() );
                }
            }
        }
        return null;
    }

    /**
     * Tries to extract a {@link BBOX} constraint from the given {@link Filter} that can be used as a pre-filtering step
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
    public static BBOX extractPrefilterBBoxConstraint( Filter filter ) {
        BBOX env = null;
        if ( filter != null && filter.getType() == OPERATOR_FILTER ) {
            OperatorFilter of = (OperatorFilter) filter;
            Operator oper = of.getOperator();
            env = extractBBox( oper );
        }
        return env;
    }

    private static BBOX extractBBox( Operator oper ) {
        switch ( oper.getType() ) {
        case COMPARISON: {
            return null;
        }
        case LOGICAL: {
            LogicalOperator logical = (LogicalOperator) oper;
            switch ( logical.getSubType() ) {
            case AND:
                BBOX env = null;
                for ( Operator child : logical.getParams() ) {
                    BBOX childEnv = extractBBox( child );
                    if ( childEnv != null ) {
                        if ( env == null ) {
                            env = childEnv;
                        } else {
                            env = merge( env, childEnv );
                        }
                    }
                }
                return env;
            case OR:
                return null;
            case NOT:
                return null;
            }
            return null;
        }
        case SPATIAL: {
            return extractBBox( (SpatialOperator) oper );
        }
        }
        return null;
    }

    private static BBOX merge( BBOX bbox1, BBOX bbox2 ) {
        // TODO handle different SRS
        Envelope env = bbox1.getBoundingBox().merge( bbox2.getBoundingBox() );
        Expression expr = bbox1.getParam1();
        if ( expr == null || !expr.equals( bbox2.getParam1() ) ) {
            expr = null;
        }
        return new BBOX( expr, env );
    }

    private static BBOX extractBBox( SpatialOperator oper ) {
        SubType type = oper.getSubType();
        switch ( type ) {
        case BBOX:
            return (BBOX) oper;
        case CONTAINS:
            // Oracle does not like zero-extent bboxes
            if ( !( ( (Contains) oper ).getGeometry() instanceof Point ) )
                return new BBOX( ( (Contains) oper ).getParam1(), ( (Contains) oper ).getGeometry().getEnvelope() );
            return null;
        case CROSSES:
            return new BBOX( ( (Crosses) oper ).getParam1(), ( (Crosses) oper ).getGeometry().getEnvelope() );
        case DWITHIN:
            // TOOD use enlarged bbox
            return null;
        case EQUALS:
            return new BBOX( ( (Equals) oper ).getParam1(), ( (Equals) oper ).getGeometry().getEnvelope() );
        case INTERSECTS:
            return new BBOX( ( (Intersects) oper ).getParam1(), ( (Intersects) oper ).getGeometry().getEnvelope() );
        case OVERLAPS:
            return new BBOX( ( (Overlaps) oper ).getParam1(), ( (Overlaps) oper ).getGeometry().getEnvelope() );
        case WITHIN:
            return new BBOX( ( (Within) oper ).getParam1(), ( (Within) oper ).getGeometry().getEnvelope() );
        default: {
            return null;
        }
        }
    }

    /**
     * Adds a bounding box constraint to the given {@link Filter}.
     * 
     * @param bbox
     *            bounding box, can be <code>null</code>
     * @param filter
     *            filter expression, can be <code>null</code>
     * @param propName
     *            can be <code>null</code>
     * @return combined filter or <code>null</code> (if bbox and filter are <code>null</code>)
     */
    public static Filter addBBoxConstraint( final Envelope bbox, final Filter filter, final ValueReference propName ) {
        return addBBoxConstraint( bbox, filter, propName, false );
    }

    /**
     * Adds a bounding box constraint to the given {@link Filter}.
     * 
     * @param bbox
     *            bounding box, can be <code>null</code>
     * @param filter
     *            filter expression, can be <code>null</code>
     * @param propName
     *            can be <code>null</code>
     * @param allowFalsePositives
     *            set to <code>true</code>, if false positives are acceptable (may enable faster index-only checks)
     * @return combined filter or <code>null</code> (if bbox and filter are <code>null</code>)
     */
    public static Filter addBBoxConstraint( final Envelope bbox, final Filter filter, final ValueReference propName,
                                            final boolean allowFalsePositives ) {
        if ( bbox == null ) {
            return filter;
        }
        if ( filter instanceof IdFilter ) {
            LOG.warn( "Not adding bbox to filter, as the filter is an IdFilter." );
            return filter;
        }
        Filter bboxFilter = null;
        BBOX bboxOperator = new BBOX( propName, bbox, allowFalsePositives );
        if ( filter == null ) {
            bboxFilter = new OperatorFilter( bboxOperator );
        } else {
            And andOperator = new And( bboxOperator, ( (OperatorFilter) filter ).getOperator() );
            bboxFilter = new OperatorFilter( andOperator );
        }
        return bboxFilter;
    }

    /**
     * @param filter
     *            can be null
     * @return the reverse of #addBBoxConstraint
     */
    public static Pair<Filter, Envelope> splitOffBBoxConstraint( Filter filter ) {
        Pair<Filter, Envelope> p = new Pair<Filter, Envelope>();
        if ( filter instanceof OperatorFilter ) {
            OperatorFilter f = (OperatorFilter) filter;

            if ( f.getOperator() instanceof BBOX ) {
                p.second = ( (BBOX) f.getOperator() ).getBoundingBox();
            } else if ( f.getOperator() instanceof And && ( (And) f.getOperator() ).getParams()[0] instanceof BBOX ) {
                Operator[] ops = ( (And) f.getOperator() ).getParams();
                p.second = ( (BBOX) ops[0] ).getBoundingBox();
                if ( ops.length == 2 ) {
                    p.first = new OperatorFilter( ops[1] );
                } else {
                    p.first = new OperatorFilter( new And( Arrays.copyOfRange( ops, 1, ops.length - 1 ) ) );
                }
            } else {
                p.first = filter;
            }
        } else {
            p.first = filter;
        }
        return p;
    }

    /**
     * Returns all {@link ValueReference}s contained in the given {@link Filter} (taking nesting into account).
     * 
     * @param filter
     *            filter to be traversed, must not be <code>null</code>
     * @return {@link ValueReference}s found on any nodes of the {@link Filter}, can be empty, but never
     *         <code>null</code>
     */
    public static ValueReference[] getPropertyNames( Filter filter ) {

        List<ValueReference> propNames = null;
        switch ( filter.getType() ) {
        case OPERATOR_FILTER: {
            propNames = new LinkedList<ValueReference>();
            addPropertyNames( ( (OperatorFilter) filter ).getOperator(), propNames );
            break;
        }
        case ID_FILTER: {
            propNames = Collections.emptyList();
            break;
        }
        }
        return propNames.toArray( new ValueReference[propNames.size()] );
    }

    private static void addPropertyNames( Operator operator, List<ValueReference> propNames ) {
        Operator.Type type = operator.getType();
        switch ( type ) {
        case COMPARISON:
            ComparisonOperator compOper = (ComparisonOperator) operator;
            for ( Expression expr : compOper.getParams() ) {
                addPropertyNames( expr, propNames );
            }
            break;
        case LOGICAL:
            LogicalOperator logicalOper = (LogicalOperator) operator;
            for ( Operator param : logicalOper.getParams() ) {
                addPropertyNames( param, propNames );
            }
            break;
        case SPATIAL:
            SpatialOperator spatialOper = (SpatialOperator) operator;
            for ( Object param : spatialOper.getParams() ) {
                if ( param instanceof Expression ) {
                    addPropertyNames( (Expression) param, propNames );
                }
            }
            break;
        }
    }

    private static void addPropertyNames( Expression expr, List<ValueReference> propNames ) {
        if ( expr instanceof ValueReference ) {
            propNames.add( (ValueReference) expr );
        } else {
            for ( Expression child : expr.getParams() ) {
                addPropertyNames( child, propNames );
            }
        }
    }

    /**
     * Returns all {@link Geometry}-values contained in the given {@link Filter} (taking nesting into account).
     * 
     * @param filter
     *            filter to be traversed, must not be <code>null</code>
     * @return {@link Geometry}s found on any nodes of the {@link Filter}, can be empty, but never <code>null</code>
     */
    public static Geometry[] getGeometries( Filter filter ) {
        List<Geometry> geometries = null;
        switch ( filter.getType() ) {
        case OPERATOR_FILTER: {
            geometries = new LinkedList<Geometry>();
            addGeometries( ( (OperatorFilter) filter ).getOperator(), geometries );
            break;
        }
        case ID_FILTER: {
            geometries = Collections.emptyList();
            break;
        }
        }
        return geometries.toArray( new Geometry[geometries.size()] );
    }

    private static void addGeometries( Operator operator, List<Geometry> geometries ) {
        Operator.Type type = operator.getType();
        switch ( type ) {
        case LOGICAL:
            LogicalOperator logicalOper = (LogicalOperator) operator;
            for ( Operator param : logicalOper.getParams() ) {
                addGeometries( param, geometries );
            }
            break;
        case SPATIAL:
            SpatialOperator spatialOper = (SpatialOperator) operator;
            for ( Object param : spatialOper.getParams() ) {
                if ( param instanceof Geometry ) {
                    geometries.add( (Geometry) param );
                }
            }
            break;
        case COMPARISON:
            // nothing to do
            break;
        }
    }

    private static OperatorFilter combine( boolean and, OperatorFilter f1, OperatorFilter f2 ) {
        if ( f1 == null || f2 == null ) {
            return f1 == null ? f2 : f1;
        }
        Operator o1 = f1.getOperator();
        Operator o2 = f2.getOperator();
        if ( and ) {
            return new OperatorFilter( new And( o1, o2 ) );
        }
        return new OperatorFilter( new Or( o1, o2 ) );
    }

    public static OperatorFilter and( OperatorFilter f1, OperatorFilter f2 ) {
        return combine( true, f1, f2 );
    }

    public static OperatorFilter or( OperatorFilter f1, OperatorFilter f2 ) {
        return combine( false, f1, f2 );
    }

    /**
     * Sets the {@link CRS} for all geometries contained in the given {@link Filter} that do not have crs information.
     * 
     * @param filter
     *            filter to process, must not be <code>null</code>
     * @param crs
     *            crs to set, must not be <code>null</code>
     */
    public static void setDefaultCRS( Filter filter, ICRS crs ) {
        for ( Geometry geom : getGeometries( filter ) ) {
            if ( geom.getCoordinateSystem() == null ) {
                // TODO propagate to deeper levels / change behavior of setCoordinateSystem()
                geom.setCoordinateSystem( crs );
            }
        }
    }

    /**
     * Tries to repair an expression's broken namespace bindings using the hints in the map.
     * 
     * @param e
     * @param bindings
     *            binds local names to qualified names
     * @param validNames
     *            a set of valid qnames
     * @return the repaired expression
     */
    public static Expression repair( Expression e, Map<String, QName> bindings, Set<QName> validNames ) {
        switch ( e.getType() ) {
        case ADD:
            Add a = (Add) e;
            return new Add( repair( a.getParameter1(), bindings, validNames ), repair( a.getParameter2(), bindings,
                                                                                       validNames ) );
        case CUSTOM:
            return e;
        case DIV:
            Div d = (Div) e;
            return new Div( repair( d.getParameter1(), bindings, validNames ), repair( d.getParameter2(), bindings,
                                                                                       validNames ) );
        case FUNCTION:
            // workaround seems to produce errors, so function expressions are not fixed now
            return e;
            // Function f = (Function) e;
            // List<Expression> ps = new ArrayList<Expression>();
            // for ( Expression ex : f.getParameters() ) {
            // ps.add( repair( ex, bindings, validNames ) );
            // }
            // return new Function( f.getName(), ps );
        case LITERAL:
            return e;
        case MUL:
            Mul m = (Mul) e;
            return new Mul( repair( m.getParameter1(), bindings, validNames ), repair( m.getParameter2(), bindings,
                                                                                       validNames ) );
        case SUB:
            Sub s = (Sub) e;
            return new Sub( repair( s.getParameter1(), bindings, validNames ), repair( s.getParameter2(), bindings,
                                                                                       validNames ) );
        case VALUE_REFERENCE:
            ValueReference vr = (ValueReference) e;
            QName name = vr.getAsQName();
            if ( name != null && !validNames.contains( name ) ) {
                if ( !bindings.containsKey( name.getLocalPart() ) ) {
                    LOG.warn( "Error while trying to repair an expression: local name {} could still not be resolved to a proper qname.",
                              name.getLocalPart() );
                    return e;
                }
                LOG.debug( "Repairing namespace binding for property name {}", name.getLocalPart() );
                return new ValueReference( bindings.get( name.getLocalPart() ) );
            }
            return e;
        }
        return e;
    }

    private static ComparisonOperator repair( ComparisonOperator o, Map<String, QName> bindings, Set<QName> validNames ) {
        Expression[] exs = o.getParams();
        for ( int i = 0; i < exs.length; ++i ) {
            exs[i] = repair( exs[i], bindings, validNames );
        }
        switch ( o.getSubType() ) {
        case PROPERTY_IS_BETWEEN:
            // the ordering is a bit confusing here...
            return new PropertyIsBetween( exs[1], exs[0], exs[2], o.isMatchCase(), o.getMatchAction() );
        case PROPERTY_IS_EQUAL_TO:
            return new PropertyIsEqualTo( exs[0], exs[1], o.isMatchCase(), o.getMatchAction() );
        case PROPERTY_IS_GREATER_THAN:
            return new PropertyIsGreaterThan( exs[0], exs[1], o.isMatchCase(), o.getMatchAction() );
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
            return new PropertyIsGreaterThanOrEqualTo( exs[0], exs[1], o.isMatchCase(), o.getMatchAction() );
        case PROPERTY_IS_LESS_THAN:
            return new PropertyIsLessThan( exs[0], exs[1], o.isMatchCase(), o.getMatchAction() );
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
            return new PropertyIsLessThanOrEqualTo( exs[0], exs[1], o.isMatchCase(), o.getMatchAction() );
        case PROPERTY_IS_LIKE:
            PropertyIsLike pil = (PropertyIsLike) o;
            return new PropertyIsLike( exs[0], exs[1], pil.getWildCard(), pil.getSingleChar(), pil.getEscapeChar(),
                                       o.isMatchCase(), o.getMatchAction() );
        case PROPERTY_IS_NIL:
            PropertyIsNil pin = (PropertyIsNil) o;
            return new PropertyIsNil( exs[0], pin.getNilReason(), o.getMatchAction() );
        case PROPERTY_IS_NOT_EQUAL_TO:
            return new PropertyIsNotEqualTo( exs[0], exs[1], o.isMatchCase(), o.getMatchAction() );
        case PROPERTY_IS_NULL:
            return new PropertyIsNull( exs[0], o.getMatchAction() );
        }
        return o;
    }

    private static LogicalOperator repair( LogicalOperator o, Map<String, QName> bindings, Set<QName> validNames ) {
        Operator[] os = o.getParams();
        for ( int i = 0; i < os.length; ++i ) {
            os[i] = repair( os[i], bindings, validNames );
        }
        switch ( o.getSubType() ) {
        case AND:
            return new And( os );
        case NOT:
            return new Not( os[0] );
        case OR:
            return new Or( os );
        }
        return o;
    }

    private static SpatialOperator repair( SpatialOperator o, Map<String, QName> bindings, Set<QName> validNames ) {
        Object[] os = o.getParams();
        for ( int i = 0; i < os.length; ++i ) {
            if ( os[i] instanceof Expression ) {
                os[i] = repair( (Expression) os[i], bindings, validNames );
            }
        }
        switch ( o.getSubType() ) {
        case BBOX:
            return new BBOX( (Expression) os[0], (Envelope) os[1] );
        case BEYOND:
            Beyond b = (Beyond) o;
            return new Beyond( (Expression) os[0], (Geometry) os[1], b.getDistance() );
        case CONTAINS:
            return new Contains( (Expression) os[0], (Geometry) os[1] );
        case CROSSES:
            return new Crosses( (Expression) os[0], (Geometry) os[1] );
        case DISJOINT:
            return new Disjoint( (Expression) os[0], (Geometry) os[1] );
        case DWITHIN:
            DWithin d = (DWithin) o;
            return new DWithin( (Expression) os[0], (Geometry) os[1], d.getDistance() );
        case EQUALS:
            return new Equals( (Expression) os[0], (Geometry) os[1] );
        case INTERSECTS:
            return new Intersects( (Expression) os[0], (Geometry) os[1] );
        case OVERLAPS:
            return new Overlaps( (Expression) os[0], (Geometry) os[1] );
        case TOUCHES:
            return new Touches( (Expression) os[0], (Geometry) os[1] );
        case WITHIN:
            return new Within( (Expression) os[0], (Geometry) os[1] );
        }
        return o;
    }

    private static TemporalOperator repair( TemporalOperator o, Map<String, QName> bindings, Set<QName> validNames ) {
        Expression p1 = repair( o.getParameter1(), bindings, validNames );
        Expression p2 = repair( o.getParameter2(), bindings, validNames );
        switch ( o.getSubType() ) {
        case AFTER:
            return new After( p1, p2 );
        case ANYINTERACTS:
            return new AnyInteracts( p1, p2 );
        case BEFORE:
            return new Before( p1, p2 );
        case BEGINS:
            return new Begins( p1, p2 );
        case BEGUNBY:
            return new BegunBy( p1, p2 );
        case DURING:
            return new During( p1, p2 );
        case ENDEDBY:
            return new EndedBy( p1, p2 );
        case MEETS:
            return new Meets( p1, p2 );
        case METBY:
            return new MetBy( p1, p2 );
        case OVERLAPPEDBY:
            return new OverlappedBy( p1, p2 );
        case TCONTAINS:
            return new TContains( p1, p2 );
        case TEQUALS:
            return new TEquals( p1, p2 );
        case TOVERLAPS:
            return new TOverlaps( p1, p2 );
        }
        return o;
    }

    private static Operator repair( Operator o, Map<String, QName> bindings, Set<QName> validNames ) {
        switch ( o.getType() ) {
        case COMPARISON:
            return repair( (ComparisonOperator) o, bindings, validNames );
        case LOGICAL:
            return repair( (LogicalOperator) o, bindings, validNames );
        case SPATIAL:
            return repair( (SpatialOperator) o, bindings, validNames );
        case TEMPORAL:
            return repair( (TemporalOperator) o, bindings, validNames );
        }
        return o;
    }

    /**
     * Tries to repair broken property name bindings in the filter.
     * 
     * @param filter
     * @param validNames
     *            a set of valid qnames
     * @return the repaired filter
     */
    public static <T extends Filter> T repair( T filter, Set<QName> validNames ) {
        if ( !( filter instanceof OperatorFilter ) ) {
            return filter;
        }

        Map<String, QName> bindings = new HashMap<String, QName>();
        for ( QName name : validNames ) {
            bindings.put( name.getLocalPart(), name );
        }

        Operator o = ( (OperatorFilter) filter ).getOperator();
        return (T) new OperatorFilter( repair( o, bindings, validNames ) );
    }

}
