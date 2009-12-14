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
package org.deegree.record.persistence.sqltransform.postgres;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

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

/**
 * Here the filterexpression is syntactically splitted into its components. To handle the expression a specific
 * knowledge about the database, which is underlying, is needed. This class transforms a filterexpression into a
 * PostGres datastore readable format.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class TransformatorPostGres {

    private Writer writer = new StringWriter();

    private Set<String> table = new HashSet<String>();

    private Set<String> column = new HashSet<String>();

    private ExpressionFilterHandling expressionFilterHandling = new ExpressionFilterHandling();

    private ExpressionFilterObject expressObject;

    public TransformatorPostGres( Filter constraint ) {

        if ( constraint != null ) {
            try {
                filterExpressionToConstraintString( constraint );
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {

        }
    }

    /**
     * @return the stringWriter
     */
    public Writer getStringWriter() {
        return writer;
    }

    /**
     * @return the table
     */
    public Set<String> getTable() {
        return table;
    }

    /**
     * @return the column
     */
    public Set<String> getColumn() {
        return column;
    }

    /**
     * Parsed filter expression is transformed into a String.
     * 
     * @param filter
     * @return
     * @throws IOException
     */
    private void filterExpressionToConstraintString( Filter filter )
                            throws IOException {

        Type type = filter.getType();

        switch ( type ) {

        case OPERATOR_FILTER:

            OperatorFilter opFilter = (OperatorFilter) filter;

            org.deegree.filter.Operator.Type typeOperator = opFilter.getOperator().getType();

            operatorFilterHandling( opFilter, typeOperator, writer );

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
    private void operatorFilterHandling( OperatorFilter opFilter, org.deegree.filter.Operator.Type typeOperator,
                                         Writer writer )
                            throws IOException {

        switch ( typeOperator ) {

        case SPATIAL:
            SpatialOperator spaOp = (SpatialOperator) opFilter.getOperator();
            SpatialOperatorTransformingPostGIS spa = new SpatialOperatorTransformingPostGIS( spaOp, writer );
            table.addAll( spa.getTable() );
            column.addAll( spa.getColumn() );
            // return spa.getSpatialOperation();
            break;

        case LOGICAL:
            LogicalOperator logOp = (LogicalOperator) opFilter.getOperator();
            SubType typeLogical = logOp.getSubType();
            // String stringLogical = "";
            int count;

            switch ( typeLogical ) {

            case AND:

                And andOp = (And) logOp;
                Operator[] paramsAnd = andOp.getParams();
                // stringLogical = "";
                count = 0;
                for ( Operator opParam : paramsAnd ) {
                    if ( count != paramsAnd.length - 1 ) {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        // stringLogical += "(";
                        // stringLogical += operatorFilterHandling( opera, opParam.getType() );
                        // stringLogical += ")";
                        // stringLogical += " AND ";
                        writer.append( '(' );
                        operatorFilterHandling( opera, opParam.getType(), writer );
                        writer.append( ')' );
                        writer.append( " AND " );

                        count++;
                    } else {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        // stringLogical += "(";
                        // stringLogical += operatorFilterHandling( opera, opParam.getType() );
                        // stringLogical += ")";
                        writer.append( '(' );
                        operatorFilterHandling( opera, opParam.getType(), writer );
                        writer.append( ')' );
                    }
                }

                break;

            case OR:
                Or orOp = (Or) logOp;
                Operator[] paramsOr = orOp.getParams();
                // stringLogical = "";
                count = 0;
                for ( Operator opParam : paramsOr ) {
                    if ( count != paramsOr.length - 1 ) {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        writer.append( '(' );
                        operatorFilterHandling( opera, opParam.getType(), writer );
                        writer.append( ')' );
                        writer.append( " OR " );
                        count++;
                    } else {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        writer.append( '(' );
                        operatorFilterHandling( opera, opParam.getType(), writer );
                        writer.append( ')' );
                    }
                }

                break;

            case NOT:
                Not notOp = (Not) logOp;
                Operator[] paramsNot = notOp.getParams();
                // stringLogical = "";
                for ( Operator opParam : paramsNot ) {
                    OperatorFilter opera = new OperatorFilter( opParam );
                    writer.append( " NOT " );
                    writer.append( '(' );
                    operatorFilterHandling( opera, opParam.getType(), writer );
                    writer.append( ')' );

                }

                break;

            }

            break;

        case COMPARISON:
            // String stringComparison = "";

            ComparisonOperator compOp = (ComparisonOperator) opFilter.getOperator();
            org.deegree.filter.comparison.ComparisonOperator.SubType typeComparison = compOp.getSubType();

            switch ( typeComparison ) {

            case PROPERTY_IS_EQUAL_TO:
                PropertyIsEqualTo propertyIsEqualTo = (PropertyIsEqualTo) compOp;
                writer.append( expressionArrayHandling( propertyIsEqualTo.getParameter1(), " = ",
                                                        propertyIsEqualTo.getParameter2() ) );

                break;
            case PROPERTY_IS_NOT_EQUAL_TO:
                PropertyIsNotEqualTo propertyIsNotEqualTo = (PropertyIsNotEqualTo) compOp;
                writer.append( expressionArrayHandling( propertyIsNotEqualTo.getParameter1(), " != ",
                                                        propertyIsNotEqualTo.getParameter2() ) );

                break;
            case PROPERTY_IS_LESS_THAN:
                PropertyIsLessThan propertyIsLessThan = (PropertyIsLessThan) compOp;
                writer.append( expressionArrayHandling( propertyIsLessThan.getParameter1(), " < ",
                                                        propertyIsLessThan.getParameter2() ) );

                break;
            case PROPERTY_IS_GREATER_THAN:
                PropertyIsGreaterThan propertyIsGreaterThan = (PropertyIsGreaterThan) compOp;
                writer.append( expressionArrayHandling( propertyIsGreaterThan.getParameter1(), " > ",
                                                        propertyIsGreaterThan.getParameter2() ) );

                break;
            case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
                PropertyIsLessThanOrEqualTo propertyIsLessThanOrEqualTo = (PropertyIsLessThanOrEqualTo) compOp;
                writer.append( expressionArrayHandling( propertyIsLessThanOrEqualTo.getParameter1(), " <= ",
                                                        propertyIsLessThanOrEqualTo.getParameter2() ) );

                break;
            case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
                PropertyIsGreaterThanOrEqualTo propertyIsGreaterThanOrEqualTo = (PropertyIsGreaterThanOrEqualTo) compOp;
                writer.append( expressionArrayHandling( propertyIsGreaterThanOrEqualTo.getParameter1(), " >= ",
                                                        propertyIsGreaterThanOrEqualTo.getParameter2() ) );

                break;
            case PROPERTY_IS_LIKE:
                PropertyIsLike propertyIsLike = (PropertyIsLike) compOp;
                // TODO wildcard and so on...
                writer.append( propIsLikeHandling( propertyIsLike.getParams() ) );

                break;
            case PROPERTY_IS_NULL:
                PropertyIsNull propertyIsNull = (PropertyIsNull) compOp;
                writer.append( propIsNull( propertyIsNull.getParams() ) );

                break;
            case PROPERTY_IS_BETWEEN:
                PropertyIsBetween propertyIsBetween = (PropertyIsBetween) compOp;
                writer.append( propIsBetweenHandling( propertyIsBetween.getLowerBoundary(),
                                                      propertyIsBetween.getUpperBoundary() ) );

                break;
            }

            break;
        }

    }

    /**
     * Handles the {@link Expression}s that are identified by the filter
     * <p>
     * Assumes that there are two arguments.
     * 
     * @param expressionArray
     */
    private String expressionArrayHandling( Expression expression1, String compOp, Expression expression2 ) {

        String s = "";
        expressObject = expressionFilterHandling.expressionFilterHandling( expression1.getType(), expression1 );
        table.addAll( expressObject.getTable() );
        column.addAll( expressObject.getColumn() );
        s += expressObject.getExpression();
        s += compOp;
        expressObject = expressionFilterHandling.expressionFilterHandling( expression2.getType(), expression2 );
        table.addAll( expressObject.getTable() );
        column.addAll( expressObject.getColumn() );
        s += expressObject.getExpression();

        return s;
    }

    /**
     * Handles the {@link Expression} for a BETWEEN statement.
     * 
     * @param lowerBoundary
     * @param upperBoundary
     * @return
     */
    private String propIsBetweenHandling( Expression lowerBoundary, Expression upperBoundary ) {

        String s = "";
        s += " BETWEEN ";
        expressObject = expressionFilterHandling.expressionFilterHandling( lowerBoundary.getType(), lowerBoundary );
        table.addAll( expressObject.getTable() );
        column.addAll( expressObject.getColumn() );
        s += expressObject.getExpression();
        s += " AND ";
        expressObject = expressionFilterHandling.expressionFilterHandling( upperBoundary.getType(), upperBoundary );
        table.addAll( expressObject.getTable() );
        column.addAll( expressObject.getColumn() );
        s += expressObject.getExpression();

        return s;
    }

    /**
     * Handles the {@link Expression} for a LIKE statement
     * 
     * @param compOp
     * @return
     */
    private String propIsLikeHandling( Expression[] compOp ) {
        String s = "";
        int counter = 0;

        for ( Expression exp : compOp ) {
            expressObject = expressionFilterHandling.expressionFilterHandling( exp.getType(), exp );
            table.addAll( expressObject.getTable() );
            column.addAll( expressObject.getColumn() );
            if ( counter != compOp.length - 1 ) {
                counter++;

                s += expressObject.getExpression();
                s += " LIKE ";

            } else {
                s += expressObject.getExpression();
            }

        }
        return s;
    }

    /**
     * Handles the {@link Expression} for a IS NULL statement
     * 
     * @param compOp
     * @return
     */
    private String propIsNull( Expression[] compOp ) {
        String s = "";

        for ( Expression exp : compOp ) {
            expressObject = expressionFilterHandling.expressionFilterHandling( exp.getType(), exp );
            table.addAll( expressObject.getTable() );
            column.addAll( expressObject.getColumn() );
            s += expressObject.getExpression();
            s += " IS NULL ";

        }
        return s;
    }

}
