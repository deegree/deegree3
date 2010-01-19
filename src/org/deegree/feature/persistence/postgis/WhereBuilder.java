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
package org.deegree.feature.persistence.postgis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.SimplePropertyMappingType;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.geometry.Geometry;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates SQL-WHERE clauses from {@link Filter} expressions (to restrict SQL <code>ResultSet</code>s to rows/features
 * that match a given filter). Also handles the creation of ORDER-BY clauses.
 * <p>
 * Note that the generated WHERE and ORDER-BY clauses are sometimes not sufficient to guarantee that the
 * <code>ResultSet</code> only contains the targeted feature instances and/or keeps the requested feature order. This
 * happens when the {@link PropertyName}s used in the Filter/sort criteria are not mapped to columns in the database or
 * the contained XPath expressions are not mappable to an equivalent SQL expression. In these cases, one or both of the
 * methods {@link #needsPostFiltering}/{@link #needsPostSorting} return true and the corresponding
 * {@link FeatureResultSet} must be filtered/sorted in memory to guarantee the requested constraints/order.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class WhereBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( WhereBuilder.class );

    private final PostGISFeatureStore fs;

    private final FeatureType ft;

    private final FeatureTypeMapping mapping;

    private final OperatorFilter filter;

    private boolean needsPostFiltering;

    private boolean needsPostSorting;

    private StringBuilder whereClause = new StringBuilder();

    private Collection<Object> whereParams = new ArrayList<Object>();

    private StringBuilder orderBy = new StringBuilder();

    private Collection<Object> orderByParams = new ArrayList<Object>();

    /**
     * @param fs
     * @param ft
     *            {@link FeatureType} to be queried, must not be <code>null</code> and served by {@link #fs}
     * @param mapping
     *            relational mapping for {@link #ft}, must not be <code>null</code>
     * @param filter
     *            Filter to use for generating the WHERE clause, must not be <code>null</code>
     * @throws FilterEvaluationException
     *             if the filter is invalid
     */
    WhereBuilder( PostGISFeatureStore fs, FeatureType ft, FeatureTypeMapping mapping, OperatorFilter filter )
                            throws FilterEvaluationException {
        this.fs = fs;
        this.ft = ft;
        this.mapping = mapping;
        this.filter = filter;
        process( filter.getOperator() );
    }

    private void process( Operator op )
                            throws FilterEvaluationException {
        switch ( op.getType() ) {
        case COMPARISON: {
            process( (ComparisonOperator) op );
            break;
        }
        case LOGICAL: {
            process( (LogicalOperator) op );
            break;
        }
        case SPATIAL: {
            process( (SpatialOperator) op );
            break;
        }
        }
    }

    private void process( ComparisonOperator op )
                            throws FilterEvaluationException {
        switch ( op.getSubType() ) {
        case PROPERTY_IS_BETWEEN: {
            PropertyIsBetween propIsBetween = (PropertyIsBetween) op;
            process( propIsBetween.getUpperBoundary(), false );
            whereClause.append( ">=" );
            process( propIsBetween.getExpression(), false );
            whereClause.append( "<=" );
            process( propIsBetween.getLowerBoundary(), false );
            break;
        }
        case PROPERTY_IS_EQUAL_TO: {
            PropertyIsEqualTo propIsEqualTo = (PropertyIsEqualTo) op;
            process( propIsEqualTo.getParameter1(), !propIsEqualTo.getMatchCase() );
            whereClause.append( "=" );
            process( propIsEqualTo.getParameter2(), !propIsEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_GREATER_THAN: {
            PropertyIsGreaterThan propIsGT = (PropertyIsGreaterThan) op;
            process( propIsGT.getParameter1(), !propIsGT.getMatchCase() );
            whereClause.append( ">" );
            process( propIsGT.getParameter2(), !propIsGT.getMatchCase() );
            break;
        }
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO: {
            PropertyIsGreaterThanOrEqualTo propIsGTOrEqualTo = (PropertyIsGreaterThanOrEqualTo) op;
            process( propIsGTOrEqualTo.getParameter1(), !propIsGTOrEqualTo.getMatchCase() );
            whereClause.append( ">=" );
            process( propIsGTOrEqualTo.getParameter2(), !propIsGTOrEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_LESS_THAN: {
            PropertyIsLessThan propIsLT = (PropertyIsLessThan) op;
            process( propIsLT.getParameter1(), !propIsLT.getMatchCase() );
            whereClause.append( "<" );
            process( propIsLT.getParameter2(), !propIsLT.getMatchCase() );
            break;
        }
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO: {
            PropertyIsLessThanOrEqualTo propIsLTOrEqualTo = (PropertyIsLessThanOrEqualTo) op;
            process( propIsLTOrEqualTo.getParameter1(), !propIsLTOrEqualTo.getMatchCase() );
            whereClause.append( "<=" );
            process( propIsLTOrEqualTo.getParameter2(), !propIsLTOrEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_LIKE: {
            // TODO
            break;
        }
        case PROPERTY_IS_NOT_EQUAL_TO: {
            PropertyIsNotEqualTo propIsNotEqualTo = (PropertyIsNotEqualTo) op;
            process( propIsNotEqualTo.getParameter1(), !propIsNotEqualTo.getMatchCase() );
            whereClause.append( "<>" );
            process( propIsNotEqualTo.getParameter2(), !propIsNotEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_NULL: {
            PropertyIsNull propIsNull = (PropertyIsNull) op;
            process( propIsNull.getPropertyName(), false );
            whereClause.append( " IS NULL" );
            break;
        }
        }
    }

    private void process( LogicalOperator op )
                            throws FilterEvaluationException {
        switch ( op.getSubType() ) {
        case AND: {
            whereClause.append( "(" );
            process( op.getParams()[0] );
            whereClause.append( ")" );
            for ( int i = 1; i < op.getParams().length; i++ ) {
                whereClause.append( " AND (" );
                process( op.getParams()[i] );
                whereClause.append( ")" );
            }
            break;
        }
        case OR: {
            whereClause.append( "(" );
            process( op.getParams()[0] );
            whereClause.append( ")" );
            for ( int i = 1; i < op.getParams().length; i++ ) {
                whereClause.append( " OR (" );
                process( op.getParams()[i] );
                whereClause.append( ")" );
            }
            break;
        }
        case NOT: {
            whereClause.append( "NOT (" );
            process( op.getParams()[0] );
            whereClause.append( ")" );
            break;
        }
        }
    }

    private void process( SpatialOperator op )
                            throws FilterEvaluationException {
        switch ( op.getSubType() ) {
        case BBOX: {
            BBOX bbox = (BBOX) op;
            processGeometryArgument( bbox.getPropertyName() );
            whereClause.append( " && " );
            try {
                processGeometryArgument( bbox.getBoundingBox() );
            } catch ( SQLException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        }
        case BEYOND: {
            break;
        }
        case CONTAINS: {
            break;
        }
        case CROSSES: {
            break;
        }
        case DISJOINT: {
            break;
        }
        case DWITHIN: {
            break;
        }
        case EQUALS: {
            break;
        }
        case INTERSECTS: {
            break;
        }
        case OVERLAPS: {
            break;
        }
        case TOUCHES: {
            break;
        }
        case WITHIN: {
            break;
        }
        }
    }

    private void processGeometryArgument( PropertyName propName )
                            throws FilterEvaluationException {

        PropertyMappingType mapping = null;
        if ( propName == null ) {
            GeometryPropertyType geoPt = ft.getDefaultGeometryPropertyDeclaration();
            if ( geoPt == null ) {
                String msg = "Cannot evaluate spatial predicate: Feature type '" + ft.getName()
                             + "' does not define any spatial properties.";
                throw new FilterEvaluationException( msg );
            }
            mapping = this.mapping.getPropertyHints( geoPt.getName() );
        } else {
            mapping = getMapping( propName );
        }
        if ( !( mapping instanceof GeometryPropertyMappingType ) ) {
            String msg = "Cannot evaluate spatial operator on property name: '" + propName
                         + "' -- not a spatial property.";
            throw new FilterEvaluationException( msg );
        }
        GeometryPropertyMappingType geomMapping = (GeometryPropertyMappingType) mapping;
        String dbColumn = geomMapping.getGeometryDBColumn().getName();
        whereClause.append( "x2." + dbColumn );
    }

    private void processGeometryArgument( Geometry geometry ) throws SQLException {
        whereClause.append ("GeomFromWKB(?,-1)");
        whereParams.add( TypeMangler.toPostGIS( geometry ) );
    }

    private void process( Expression expr, boolean lowerCase )
                            throws FilterEvaluationException {
        switch ( expr.getType() ) {
        case ADD: {
            whereClause.append( "(" );
            process( expr.getParams()[0], false );
            whereClause.append( "+" );
            process( expr.getParams()[1], false );
            whereClause.append( ")" );
            break;
        }
        case DIV: {
            whereClause.append( "(" );
            process( expr.getParams()[0], false );
            whereClause.append( "/" );
            process( expr.getParams()[1], false );
            whereClause.append( ")" );
            break;
        }
        case FUNCTION: {
            break;
        }
        case LITERAL: {
            if ( lowerCase ) {
                whereClause.append( "LOWER(?)" );
            } else {
                whereClause.append( "?" );
            }
            whereParams.add( ( (Literal<?>) expr ).getValue().toString() );
            break;
        }
        case MUL: {
            whereClause.append( "(" );
            process( expr.getParams()[0], false );
            whereClause.append( "*" );
            process( expr.getParams()[1], false );
            whereClause.append( ")" );
            break;
        }
        case PROPERTY_NAME: {
            PropertyMappingType mapping = getMapping( (PropertyName) expr );
            if ( mapping != null ) {
                if ( mapping instanceof GeometryPropertyMappingType ) {
                    GeometryPropertyMappingType geomMapping = (GeometryPropertyMappingType) mapping;
                    String columnName = geomMapping.getGeometryDBColumn().getName();
                    whereClause.append ("x2.");
                    whereClause.append( columnName );
                } else if ( mapping instanceof SimplePropertyMappingType ) {
                    SimplePropertyMappingType simpleMapping = (SimplePropertyMappingType) mapping;
                    String columnName = simpleMapping.getDBColumn().getName();
                    whereClause.append ("x2.");
                    whereClause.append( columnName );
                } else {
                    String msg = "Mapping for property '" + ( (PropertyName) expr ).getPropertyName()
                                 + "' is not simple or a geometry -- not implemented, ignoring it.";
                    LOG.debug( msg );
                }
            }
            break;
        }
        case SUB: {
            whereClause.append( "(" );
            process( expr.getParams()[0], false );
            whereClause.append( "-" );
            process( expr.getParams()[1], false );
            whereClause.append( ")" );
            break;
        }
        }
    }

    /**
     * Returns the {@link PropertyMappingType} for the given {@link PropertyName}.
     * 
     * @param propName
     *            {@link PropertyName}, must not be <code>null</code>
     * @return corresponding {@link PropertyMappingType} or <code>null</code> if no relational mapping is possible
     * @throws FilterEvaluationException
     *             if the {@link PropertyName} is invalid with respect to the queried feature type
     */
    private PropertyMappingType getMapping( PropertyName propName )
                            throws FilterEvaluationException {

        Expr xpath = propName.getAsXPath();
        if ( !( xpath instanceof LocationPath ) ) {
            LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                       + "': the root expression is not a LocationPath." );
            return null;
        }
        List<QName> steps = new ArrayList<QName>();
        for ( Object step : ( (LocationPath) xpath ).getSteps() ) {
            if ( !( step instanceof NameStep ) ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                           + "': contains an expression that is not a NameStep." );
                return null;
            }
            NameStep namestep = (NameStep) step;
            if ( namestep.getPredicates() != null && !namestep.getPredicates().isEmpty() ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                           + "': contains a NameStep with a predicate (needs implementation)." );
                return null;
            }
            String prefix = namestep.getPrefix();
            String localPart = namestep.getLocalName();
            String namespace = propName.getNsContext().translateNamespacePrefixToUri( prefix );
            steps.add( new QName( namespace, localPart, prefix ) );
        }

        if ( steps.size() < 1 || steps.size() > 2 ) {
            LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                       + "': must contain one or two NameSteps (needs implementation)." );
            return null;
        }

        QName requestedProperty = null;
        if ( steps.size() == 1 ) {
            // step must be equal to a property name of the queried feature
            if ( ft.getPropertyDeclaration( steps.get( 0 ) ) == null ) {
                String msg = "Filter contains an invalid PropertyName '" + propName.getPropertyName()
                             + "'. The queried feature type '" + ft.getName()
                             + "' does not have a property with this name.";
                throw new FilterEvaluationException( msg );
            }
            requestedProperty = steps.get( 0 );
        } else {
            // 1. step must be equal to the name or alias of the queried feature
            if ( !ft.getName().equals( steps.get( 0 ) ) ) {
                String msg = "Filter contains an invalid PropertyName '" + propName.getPropertyName()
                             + "'. The first step does not equal the queried feature type '" + ft.getName() + "'.";
                throw new FilterEvaluationException( msg );
            }
            // 2. step must be equal to a property name of the queried feature
            if ( ft.getPropertyDeclaration( steps.get( 1 ) ) == null ) {
                String msg = "Filter contains an invalid PropertyName '" + propName.getPropertyName()
                             + "'. The second step does not equal any property of the queried feature type '"
                             + ft.getName() + "'.";
                throw new FilterEvaluationException( msg );
            }
            requestedProperty = steps.get( 1 );
        }
        return mapping.getPropertyHints( requestedProperty );
    }

    /**
     * @return
     */
    StringBuilder getWhereClause() {
        return whereClause;
    }

    Collection<Object> getWhereParams() {
        return whereParams;
    }

    /**
     * @return
     */
    StringBuilder getOrderBy() {
        return orderBy;
    }

    /**
     * Returns whether the <code>ResultSet</code> will need post-filtering in memory.
     * 
     * @return true, if the <code>ResultSet<code> must be re-filtered, false otherwise
     */
    boolean needsPostFiltering() {
        return needsPostFiltering;
    }

    /**
     * Returns whether the <code>ResultSet</code> will need post-sorting in memory.
     * 
     * @return true, if the <code>ResultSet<code> must be re-sorted, false otherwise
     */
    boolean needsPostSorting() {
        return needsPostSorting;
    }
}
