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
package org.deegree.record.persistence.sqltransform.postgis;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.Filter.Type;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
import org.deegree.filter.logical.LogicalOperator.SubType;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.record.persistence.sqltransform.ExpressionFilterHelper;
import org.deegree.record.persistence.sqltransform.ExpressionFilterObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Here the filterexpression is syntactically splitted into its components. To handle the expression a specific
 * knowledge about the database, which is underlying, is needed. This class transforms a filterexpression into a PostGIS
 * datastore processable format.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class TransformatorPostGIS {

    private static final Logger LOG = LoggerFactory.getLogger( TransformatorPostGIS.class );

    private Writer writer;

    // private ExpressionFilterHandling expressionFilterHandling;

    private ExpressionFilterObject expressObject;

    /**
     * Creates a new {@link TransformatorPostGIS} instance.
     * 
     * @param constraint
     *            the filter constraint
     */
    public TransformatorPostGIS( Filter constraint ) {

        if ( constraint != null ) {
            try {
                filterExpressionToConstraintString( constraint );
            } catch ( IOException e ) {

                e.printStackTrace();
            }
        }
    }

    /**
     * @return the stringWriter
     */
    public Writer getWriter() {
        return writer;
    }

    /**
     * @return the expressHelper
     */
    public ExpressionFilterHelper getExpressHelper() {
        return ExpressionFilterHelper.getInstance();
    }

    /**
     * Parsed filter expression is transformed into a String.
     * 
     * @param filter
     * @throws IOException
     */
    private void filterExpressionToConstraintString( Filter filter )
                            throws IOException {

        Type type = filter.getType();
        writer = new StringWriter( 200 );
        // expressionFilterHandling = new ExpressionFilterHandling();

        switch ( type ) {

        case OPERATOR_FILTER:

            OperatorFilter opFilter = (OperatorFilter) filter;

            org.deegree.filter.Operator.Type typeOperator = opFilter.getOperator().getType();

            writer = operatorFilterHandling( opFilter, typeOperator );

        case ID_FILTER:
            // TODO
            break;
        }

    }

    /**
     * 
     * Handles an {@link Expression} that should be parsed by XPath
     * 
     * @param exp
     * @return
     */
    private String parsedXPath( Expression exp ) {
        // TODO Auto-generated method stub
        return "";
    }

    /**
     * 
     * Handles the {@link Operator}s that are identified by the OGC filter parsing.
     * 
     * @param opFilter
     * @param typeOperator
     * @throws IOException
     */
    private Writer operatorFilterHandling( OperatorFilter opFilter, org.deegree.filter.Operator.Type typeOperator )
                            throws IOException {

        switch ( typeOperator ) {

        case SPATIAL:
            SpatialOperator spaOp = (SpatialOperator) opFilter.getOperator();
            SpatialOperatorTransformingPostGIS spa = new SpatialOperatorTransformingPostGIS( spaOp );
            writer.append( spa.getWriterSpatial().toString() );

            // ExpressionFilterHelper.getInstance().addTablesANDColumns( spa.getTables(), spa.getColumns() );

            break;

        case LOGICAL:
            LogicalOperator logOp = (LogicalOperator) opFilter.getOperator();
            SubType typeLogical = logOp.getSubType();

            int count;

            switch ( typeLogical ) {

            case AND:

                And andOp = (And) logOp;
                Operator[] paramsAnd = andOp.getParams();

                count = 0;
                for ( Operator opParam : paramsAnd ) {
                    if ( count != paramsAnd.length - 1 ) {
                        OperatorFilter opera = new OperatorFilter( opParam );

                        writer.append( '(' );
                        operatorFilterHandling( opera, opParam.getType() );
                        writer.append( ')' );
                        writer.append( " AND " );

                        count++;
                    } else {
                        OperatorFilter opera = new OperatorFilter( opParam );

                        writer.append( '(' );
                        operatorFilterHandling( opera, opParam.getType() );
                        writer.append( ')' );
                    }
                }

                break;

            case OR:
                Or orOp = (Or) logOp;
                Operator[] paramsOr = orOp.getParams();

                count = 0;
                for ( Operator opParam : paramsOr ) {
                    if ( count != paramsOr.length - 1 ) {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        writer.append( '(' );
                        operatorFilterHandling( opera, opParam.getType() );
                        writer.append( ')' );
                        writer.append( " OR " );
                        count++;
                    } else {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        writer.append( '(' );
                        operatorFilterHandling( opera, opParam.getType() );
                        writer.append( ')' );
                    }
                }

                break;

            case NOT:
                Not notOp = (Not) logOp;
                Operator[] paramsNot = notOp.getParams();

                for ( Operator opParam : paramsNot ) {
                    OperatorFilter opera = new OperatorFilter( opParam );
                    writer.append( " NOT " );
                    writer.append( '(' );
                    operatorFilterHandling( opera, opParam.getType() );
                    writer.append( ')' );

                }

                break;

            }

            break;

        case COMPARISON:

            ComparisonOperator compOp = (ComparisonOperator) opFilter.getOperator();
            org.deegree.filter.comparison.ComparisonOperator.SubType typeComparison = compOp.getSubType();

            switch ( typeComparison ) {

            case PROPERTY_IS_EQUAL_TO:
                PropertyIsEqualTo propertyIsEqualTo = (PropertyIsEqualTo) compOp;
                writer.append( expressionArrayHandling( propertyIsEqualTo.getParameter1(), " = ",
                                                        propertyIsEqualTo.getParameter2() ).toString() );

                break;
            case PROPERTY_IS_NOT_EQUAL_TO:
                PropertyIsNotEqualTo propertyIsNotEqualTo = (PropertyIsNotEqualTo) compOp;
                writer.append( expressionArrayHandling( propertyIsNotEqualTo.getParameter1(), " != ",
                                                        propertyIsNotEqualTo.getParameter2() ).toString() );

                break;
            case PROPERTY_IS_LESS_THAN:
                PropertyIsLessThan propertyIsLessThan = (PropertyIsLessThan) compOp;
                writer.append( expressionArrayHandling( propertyIsLessThan.getParameter1(), " < ",
                                                        propertyIsLessThan.getParameter2() ).toString() );

                break;
            case PROPERTY_IS_GREATER_THAN:
                PropertyIsGreaterThan propertyIsGreaterThan = (PropertyIsGreaterThan) compOp;
                writer.append( expressionArrayHandling( propertyIsGreaterThan.getParameter1(), " > ",
                                                        propertyIsGreaterThan.getParameter2() ).toString() );

                break;
            case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
                PropertyIsLessThanOrEqualTo propertyIsLessThanOrEqualTo = (PropertyIsLessThanOrEqualTo) compOp;
                writer.append( expressionArrayHandling( propertyIsLessThanOrEqualTo.getParameter1(), " <= ",
                                                        propertyIsLessThanOrEqualTo.getParameter2() ).toString() );

                break;
            case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
                PropertyIsGreaterThanOrEqualTo propertyIsGreaterThanOrEqualTo = (PropertyIsGreaterThanOrEqualTo) compOp;
                writer.append( expressionArrayHandling( propertyIsGreaterThanOrEqualTo.getParameter1(), " >= ",
                                                        propertyIsGreaterThanOrEqualTo.getParameter2() ).toString() );

                break;
            case PROPERTY_IS_LIKE:
                PropertyIsLike propertyIsLike = (PropertyIsLike) compOp;
                // TODO wildcard and so on...
                writer.append( propIsLikeHandling( propertyIsLike.getParams() ).toString() );

                break;
            case PROPERTY_IS_NULL:
                PropertyIsNull propertyIsNull = (PropertyIsNull) compOp;
                writer.append( propIsNull( propertyIsNull.getParams() ).toString() );

                break;
            case PROPERTY_IS_BETWEEN:
                PropertyIsBetween propertyIsBetween = (PropertyIsBetween) compOp;
                writer.append( propIsBetweenHandling( propertyIsBetween.getLowerBoundary(),
                                                      propertyIsBetween.getUpperBoundary() ).toString() );

                break;
            }

            break;
        }
        return writer;

    }

    /**
     * Handles the {@link Expression}s that are identified by the filter
     * <p>
     * Assumes that there are two arguments.
     * 
     * @param expressionArray
     */
    private StringWriter expressionArrayHandling( Expression expression1, String compOp, Expression expression2 ) {

        StringWriter s = new StringWriter( 100 );
        // expressObject = expressionFilterHandling.expressionFilterHandling( expression1.getType(), expression1 );
        //
        // ExpressionFilterHelper.getInstance().addTablesANDColumns( expressObject.getTables(),
        // expressObject.getColumns() );
        // ExpressionFilterHelper.getInstance().setPropertyName( expressObject.getPropertyName() );
        // s.append( expressObject.getExpression() );
        s.append( compOp );
        // expressObject = expressionFilterHandling.expressionFilterHandling( expression2.getType(), expression2 );
        // ExpressionFilterHelper.getInstance().addTablesANDColumns( expressObject.getTables(),
        // expressObject.getColumns() );
        // ExpressionFilterHelper.getInstance().setPropertyName( expressObject.getPropertyName() );
        // s.append( expressObject.getExpression() );

        return s;
    }

    /**
     * Handles the {@link Expression} for a BETWEEN statement.
     * 
     * @param lowerBoundary
     * @param upperBoundary
     * @return StringWriter
     */
    private StringWriter propIsBetweenHandling( Expression lowerBoundary, Expression upperBoundary ) {

        StringWriter s = new StringWriter( 100 );
        s.append( " BETWEEN " );
        // expressObject = expressionFilterHandling.expressionFilterHandling( lowerBoundary.getType(), lowerBoundary );
        //
        // ExpressionFilterHelper.getInstance().addTablesANDColumns( expressObject.getTables(),
        // expressObject.getColumns() );
        // ExpressionFilterHelper.getInstance().setPropertyName( expressObject.getPropertyName() );
        // s.append( expressObject.getExpression() );
        s.append( " AND " );
        // expressObject = expressionFilterHandling.expressionFilterHandling( upperBoundary.getType(), upperBoundary );
        //
        // ExpressionFilterHelper.getInstance().addTablesANDColumns( expressObject.getTables(),
        // expressObject.getColumns() );
        // ExpressionFilterHelper.getInstance().setPropertyName( expressObject.getPropertyName() );
        // s.append( expressObject.getExpression() );

        return s;
    }

    /**
     * Handles the {@link Expression} for a LIKE statement
     * 
     * @param compOp
     * @return
     */
    private StringWriter propIsLikeHandling( Expression[] compOp ) {
        StringWriter s = new StringWriter( 50 );
        int counter = 0;

        for ( Expression exp : compOp ) {
            // expressObject = expressionFilterHandling.expressionFilterHandling( exp.getType(), exp );
            //
            // ExpressionFilterHelper.getInstance().addTablesANDColumns( expressObject.getTables(),
            // expressObject.getColumns() );
            // ExpressionFilterHelper.getInstance().setPropertyName( expressObject.getPropertyName() );
            // if ( counter != compOp.length - 1 ) {
            // counter++;
            //
            // s.append( expressObject.getExpression() );
            // s.append( " LIKE " );
            //
            // } else {
            // s.append( expressObject.getExpression() );
            // }

        }
        return s;
    }

    /**
     * Handles the {@link Expression} for a IS NULL statement
     * 
     * @param compOp
     * @return
     */
    private StringWriter propIsNull( Expression[] compOp ) {
        StringWriter s = new StringWriter( 50 );

        for ( Expression exp : compOp ) {
            // expressObject = expressionFilterHandling.expressionFilterHandling( exp.getType(), exp );
            //
            // ExpressionFilterHelper.getInstance().addTablesANDColumns( expressObject.getTables(),
            // expressObject.getColumns() );
            // ExpressionFilterHelper.getInstance().setPropertyName( expressObject.getPropertyName() );
            // s.append( expressObject.getExpression() );
            s.append( " IS NULL " );

        }
        return s;
    }

}
