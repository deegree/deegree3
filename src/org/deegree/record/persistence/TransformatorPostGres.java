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
package org.deegree.record.persistence;

import static org.deegree.record.persistence.MappingInfo.ColumnType.DATE;
import static org.deegree.record.persistence.MappingInfo.ColumnType.STRING;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
import org.deegree.filter.logical.LogicalOperator.SubType;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;

/**
 * Here the Filterexpression is syntactically splitted into its components. To handle the expression a specific
 * knowledge about the database, which is underlying, is needed. This class transforms a filterexpression into a
 * PostGres datastore readable format.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class TransformatorPostGres {

    private String expression;

    private ResultType resultType;

    private int maxRecords;

    private SetOfReturnableElements setOfReturnableElements;

    private Set<String> table = new HashSet<String>();

    private Set<String> column = new HashSet<String>();

    private static Map<String, MappingInfo> propToTableAndCol = new HashMap<String, MappingInfo>();

    static {
        propToTableAndCol.put( "Title", new MappingInfo( "isoqp_title", "title", STRING ) );
        propToTableAndCol.put( "Abstract", new MappingInfo( "isoqp_abstract", "abstract", STRING ) );
        propToTableAndCol.put( "BoundingBox", new MappingInfo( "isoqp_BoundingBox", "bbox", STRING ) );
        propToTableAndCol.put( "Type", new MappingInfo( "isoqp_type", "type", STRING ) );
        propToTableAndCol.put( "Format", new MappingInfo( "isoqp_format", "format", STRING ) );
        // propToTableAndCol.put( "Language", new MappingInfo( "datasets", "language", STRING ) );
        propToTableAndCol.put( "Subject", new MappingInfo( "isoqp_topiccategory", "topiccategory", STRING ) );
        propToTableAndCol.put( "AnyText", new MappingInfo( "datasets", "anytext", STRING ) );
        propToTableAndCol.put( "Identifier", new MappingInfo( "datasets", "identifier", STRING ) );
        propToTableAndCol.put( "Modified", new MappingInfo( "datasets", "modified", DATE ) );
        propToTableAndCol.put( "CRS", new MappingInfo( "isoqp_crs", "crs", STRING ) );
        propToTableAndCol.put( "Association", new MappingInfo( "isoqp_association", "relation", STRING ) );
        propToTableAndCol.put( "Source", new MappingInfo( "datasets", "source", STRING ) );

    }

    public TransformatorPostGres( Filter constraint, ResultType resultType,
                                  SetOfReturnableElements setOfReturnableElements, int maxRecords ) {
        this.resultType = resultType;
        this.setOfReturnableElements = setOfReturnableElements;
        this.maxRecords = maxRecords;

        if ( constraint != null ) {
            filterExpressionToConstraintString( constraint );
        } else {

        }
    }

    /**
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @return the resultType
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * @return the maxRecords
     */
    public int getMaxRecords() {
        return maxRecords;
    }

    /**
     * @return the setOfReturnableElements
     */
    public SetOfReturnableElements getSetOfReturnableElements() {
        return setOfReturnableElements;
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
     */
    private void filterExpressionToConstraintString( Filter filter ) {

        Type type = filter.getType();

        switch ( type ) {

        case OPERATOR_FILTER:

            OperatorFilter opFilter = (OperatorFilter) filter;

            org.deegree.filter.Operator.Type typeOperator = opFilter.getOperator().getType();
            expression = operatorFilterHandling( opFilter, typeOperator );

        case ID_FILTER:
            // TODO
            break;
        }

    }

    /**
     * Handles the {@link Expression} that is identified during the parsing of the {@link Operator}s
     * 
     * @param typeExpression
     * @param exp
     */
    private String expressionFilterHandling( org.deegree.filter.Expression.Type typeExpression, Expression exp ) {
        switch ( typeExpression ) {

        case ADD:
            // TODO
            break;

        case SUB:
            // TODO
            break;

        case MUL:
            // TODO
            break;

        case DIV:
            // TODO
            break;

        case PROPERTY_NAME:
            PropertyName propertyName = (PropertyName) exp;

            for ( String s : propToTableAndCol.keySet() ) {
                if ( propertyName.getPropertyName().equals( s ) ) {
                    MappingInfo m = propToTableAndCol.get( s );

                    table.add( m.getTable() );
                    column.add( m.getColumn() );
                    return m.getTable() + "." + m.getColumn();

                }
            }

            return "";

        case LITERAL:
            Literal<?> literal = (Literal<?>) exp;
            String value = "'" + literal.getValue().toString() + "'";
            return value;

        case FUNCTION:
            // TODO
            break;

        }
        return "";
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
     */
    private String operatorFilterHandling( OperatorFilter opFilter, org.deegree.filter.Operator.Type typeOperator ) {

        switch ( typeOperator ) {

        case SPATIAL:
            SpatialOperator spaOp = (SpatialOperator) opFilter.getOperator();
            org.deegree.filter.spatial.SpatialOperator.SubType typeSpatial = spaOp.getSubType();
            String stringSpatial = "";

            switch ( typeSpatial ) {

            case BBOX:

                BBOX bboxOp = (BBOX) spaOp;
                Object[] paramsBBox = bboxOp.getParams();
                stringSpatial = "";

                for ( Object opParam : paramsBBox ) {

                    if ( opParam != bboxOp.getBoundingBox() ) {
                        String exp = ( (PropertyName) opParam ).getPropertyName();
                        stringSpatial += expressionFilterHandling(
                                                                   ( (PropertyName) opParam ).getType(),
                                                                   new PropertyName(
                                                                                     exp,
                                                                                     ( (PropertyName) opParam ).getNsContext() ) );
                        stringSpatial += " && SetSRID('BOX3D( ";
                    } else {
                        double[] minArray = ( (Envelope) opParam ).getMin().getAsArray();
                        double[] maxArray = ( (Envelope) opParam ).getMax().getAsArray();
                        for ( double min : minArray ) {
                            stringSpatial += min + " ";
                        }
                        stringSpatial += ",";
                        for ( double max : maxArray ) {
                            stringSpatial += max + " ";
                        }
                        stringSpatial += " )'::box3d, 4326)";
                    }
                    System.out.println( stringSpatial );

                }

                return stringSpatial;

            case BEYOND:
                Beyond beyondOp = (Beyond) spaOp;
                Object[] paramsBeyond = beyondOp.getParams();
                stringSpatial = "";

                for ( Object opParam : paramsBeyond ) {

                    if ( opParam != beyondOp.getParams() ) {
                        String exp = ( (PropertyName) opParam ).getPropertyName();
                        stringSpatial += expressionFilterHandling(
                                                                   ( (PropertyName) opParam ).getType(),
                                                                   new PropertyName(
                                                                                     exp,
                                                                                     ( (PropertyName) opParam ).getNsContext() ) );
                        stringSpatial += " && SetSRID('BOX3D( ";
                    } else {
                        double[] minArray = ( (Envelope) opParam ).getMin().getAsArray();
                        double[] maxArray = ( (Envelope) opParam ).getMax().getAsArray();
                        for ( double min : minArray ) {
                            stringSpatial += min + " ";
                        }
                        stringSpatial += ",";
                        for ( double max : maxArray ) {
                            stringSpatial += max + " ";
                        }
                        stringSpatial += " )'::box3d, 4326)";
                    }
                    System.out.println( stringSpatial );

                }

                return stringSpatial;

            case CONTAINS:

                return stringSpatial;

            case CROSSES:

                return stringSpatial;

            case DISJOINT:
                Disjoint disjointOp = (Disjoint) spaOp;
                Object[] paramsDisjoint = disjointOp.getParams();
                stringSpatial = "";

                for ( Object opParam : paramsDisjoint ) {

                    if ( opParam != disjointOp.getParams() ) {
                        String exp = ( (PropertyName) opParam ).getPropertyName();
                        stringSpatial += expressionFilterHandling(
                                                                   ( (PropertyName) opParam ).getType(),
                                                                   new PropertyName(
                                                                                     exp,
                                                                                     ( (PropertyName) opParam ).getNsContext() ) );
                        stringSpatial += " && SetSRID('BOX3D( ";
                    } else {
                        double[] minArray = ( (Envelope) opParam ).getMin().getAsArray();
                        double[] maxArray = ( (Envelope) opParam ).getMax().getAsArray();
                        for ( double min : minArray ) {
                            stringSpatial += min + " ";
                        }
                        stringSpatial += ",";
                        for ( double max : maxArray ) {
                            stringSpatial += max + " ";
                        }
                        stringSpatial += " )'::box3d, 4326)";
                    }
                    System.out.println( stringSpatial );

                }

                return stringSpatial;


            case DWITHIN:
                DWithin dWithinOp = (DWithin) spaOp;
                Object[] paramsDWithin = dWithinOp.getParams();
                stringSpatial = "";

                for ( Object opParam : paramsDWithin ) {

                    if ( opParam != dWithinOp.getParams() ) {
                        String exp = ( (PropertyName) opParam ).getPropertyName();
                        stringSpatial += expressionFilterHandling(
                                                                   ( (PropertyName) opParam ).getType(),
                                                                   new PropertyName(
                                                                                     exp,
                                                                                     ( (PropertyName) opParam ).getNsContext() ) );
                        stringSpatial += " && SetSRID('BOX3D( ";
                    } else {
                        double[] minArray = ( (Envelope) opParam ).getMin().getAsArray();
                        double[] maxArray = ( (Envelope) opParam ).getMax().getAsArray();
                        for ( double min : minArray ) {
                            stringSpatial += min + " ";
                        }
                        stringSpatial += ",";
                        for ( double max : maxArray ) {
                            stringSpatial += max + " ";
                        }
                        stringSpatial += " )'::box3d, 4326)";
                    }
                    System.out.println( stringSpatial );

                }

                return stringSpatial;

            case EQUALS:

                return stringSpatial;

            case INTERSECTS:

                return stringSpatial;

            case OVERLAPS:

                return stringSpatial;

            case TOUCHES:

                return stringSpatial;

            case WITHIN:

                return stringSpatial;

            }

            break;

        case LOGICAL:
            LogicalOperator logOp = (LogicalOperator) opFilter.getOperator();
            SubType typeLogical = logOp.getSubType();
            String stringLogical = "";
            int count;

            switch ( typeLogical ) {

            case AND:

                And andOp = (And) logOp;
                Operator[] paramsAnd = andOp.getParams();
                stringLogical = "";
                count = 0;
                for ( Operator opParam : paramsAnd ) {
                    if ( count != paramsAnd.length - 1 ) {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        stringLogical += "(";
                        stringLogical += operatorFilterHandling( opera, opParam.getType() );
                        stringLogical += ")";
                        stringLogical += " AND ";
                        count++;
                    } else {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        stringLogical += "(";
                        stringLogical += operatorFilterHandling( opera, opParam.getType() );
                        stringLogical += ")";
                    }
                }

                return stringLogical;

            case OR:
                Or orOp = (Or) logOp;
                Operator[] paramsOr = orOp.getParams();
                stringLogical = "";
                count = 0;
                for ( Operator opParam : paramsOr ) {
                    if ( count != paramsOr.length - 1 ) {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        stringLogical += "(";
                        stringLogical += operatorFilterHandling( opera, opParam.getType() );
                        stringLogical += ")";
                        stringLogical += " OR ";
                        count++;
                    } else {
                        OperatorFilter opera = new OperatorFilter( opParam );
                        stringLogical += "(";
                        stringLogical += operatorFilterHandling( opera, opParam.getType() );
                        stringLogical += ")";
                    }
                }

                return stringLogical;

            case NOT:
                Not notOp = (Not) logOp;
                Operator[] paramsNot = notOp.getParams();
                stringLogical = "";
                for ( Operator opParam : paramsNot ) {
                    OperatorFilter opera = new OperatorFilter( opParam );
                    stringLogical += " NOT ";
                    stringLogical += "(";
                    stringLogical += operatorFilterHandling( opera, opParam.getType() );
                    stringLogical += ")";

                }

                return stringLogical;

            }

            break;

        case COMPARISON:
            String stringComparison = "";

            ComparisonOperator compOp = (ComparisonOperator) opFilter.getOperator();
            org.deegree.filter.comparison.ComparisonOperator.SubType typeComparison = compOp.getSubType();

            switch ( typeComparison ) {

            case PROPERTY_IS_EQUAL_TO:
                PropertyIsEqualTo propertyIsEqualTo = (PropertyIsEqualTo) compOp;
                stringComparison += expressionArrayHandling( propertyIsEqualTo.getParameter1(), " = ",
                                                             propertyIsEqualTo.getParameter2() );
                return stringComparison;

            case PROPERTY_IS_NOT_EQUAL_TO:
                PropertyIsNotEqualTo propertyIsNotEqualTo = (PropertyIsNotEqualTo) compOp;
                stringComparison += expressionArrayHandling( propertyIsNotEqualTo.getParameter1(), " != ",
                                                             propertyIsNotEqualTo.getParameter2() );
                return stringComparison;

            case PROPERTY_IS_LESS_THAN:
                PropertyIsLessThan propertyIsLessThan = (PropertyIsLessThan) compOp;
                stringComparison += expressionArrayHandling( propertyIsLessThan.getParameter1(), " < ",
                                                             propertyIsLessThan.getParameter2() );
                return stringComparison;

            case PROPERTY_IS_GREATER_THAN:
                PropertyIsGreaterThan propertyIsGreaterThan = (PropertyIsGreaterThan) compOp;
                stringComparison += expressionArrayHandling( propertyIsGreaterThan.getParameter1(), " > ",
                                                             propertyIsGreaterThan.getParameter2() );
                return stringComparison;

            case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
                PropertyIsLessThanOrEqualTo propertyIsLessThanOrEqualTo = (PropertyIsLessThanOrEqualTo) compOp;
                stringComparison += expressionArrayHandling( propertyIsLessThanOrEqualTo.getParameter1(), " <= ",
                                                             propertyIsLessThanOrEqualTo.getParameter2() );
                return stringComparison;

            case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
                PropertyIsGreaterThanOrEqualTo propertyIsGreaterThanOrEqualTo = (PropertyIsGreaterThanOrEqualTo) compOp;
                stringComparison += expressionArrayHandling( propertyIsGreaterThanOrEqualTo.getParameter1(), " >= ",
                                                             propertyIsGreaterThanOrEqualTo.getParameter2() );
                return stringComparison;

            case PROPERTY_IS_LIKE:
                PropertyIsLike propertyIsLike = (PropertyIsLike) compOp;
                // TODO wildcard and so on...
                stringComparison += propIsLikeHandling( propertyIsLike.getParams() );
                return stringComparison;

            case PROPERTY_IS_NULL:
                PropertyIsNull propertyIsNull = (PropertyIsNull) compOp;
                stringComparison += propIsNull( propertyIsNull.getParams() );
                return stringComparison;

            case PROPERTY_IS_BETWEEN:
                PropertyIsBetween propertyIsBetween = (PropertyIsBetween) compOp;
                stringComparison += propIsBetweenHandling( propertyIsBetween.getLowerBoundary(),
                                                           propertyIsBetween.getUpperBoundary() );
                return stringComparison;

            }

            break;
        }
        return "";

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

        s += expressionFilterHandling( expression1.getType(), expression1 );
        s += compOp;
        s += expressionFilterHandling( expression2.getType(), expression2 );

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
        s += expressionFilterHandling( lowerBoundary.getType(), lowerBoundary );
        s += " AND ";
        s += expressionFilterHandling( upperBoundary.getType(), upperBoundary );

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
            if ( counter != compOp.length - 1 ) {
                counter++;
                s += expressionFilterHandling( exp.getType(), exp );
                s += " LIKE ";

            } else {
                s += expressionFilterHandling( exp.getType(), exp );
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

            s += expressionFilterHandling( exp.getType(), exp );
            s += " IS NULL ";

        }
        return s;
    }

}
