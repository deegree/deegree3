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
package org.deegree.model.filterencoding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcbase.PropertyPath;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class FilterTools {

    /**
     * Traverses the <tt>Filter</tt> -tree and returns the first BBOX-Operation that is found and
     * a <tt>Filter</tt> that is equal to the given one minus the BBOX-Operation.
     * <p>
     *
     * @param filter
     *            search starts here
     * @return [0]: <tt>Envelope</tt> (BBOX), [1]: <tt>Filter</tt>
     * @throws Exception
     */
    public static Object[] extractFirstBBOX( ComplexFilter filter )
                            throws Exception {

        // [0]: Envelope, [1]: Filter
        Object[] objects = new Object[2];
        objects[1] = filter;

        // sanity check (Filter empty)
        if ( filter == null ) {
            return objects;
        }

        // used as LIFO-queue
        Stack<Operation> operations = new Stack<Operation>();
        operations.push( filter.getOperation() );

        while ( !operations.isEmpty() ) {
            // get the first element of the queue
            Operation operation = operations.pop();

            switch ( operation.getOperatorId() ) {
            case OperationDefines.BBOX: {
                // found BBOX
                objects[0] = ( (SpatialOperation) operation ).getGeometry().getEnvelope();
                break;
            }
            case OperationDefines.AND: {
                List<Operation> arguments = ( (LogicalOperation) operation ).getArguments();

                for ( int i = 0; i < arguments.size(); i++ ) {
                    operations.push( arguments.get( i ) );
                }

                break;
            }
            }

            // BBOX found?
            if ( objects[0] != null ) {
                break;
            }
        }

        // special case: Filter contains only the BBOX-Operation
        if ( filter.getOperation().getOperatorId() == OperationDefines.BBOX ) {
            // objects[1] = null;
        }

        return objects;
    }

    /**
     * Traverses the <tt>Filter</tt> -tree and returns the first BBOX-Operation that is found. Be
     * aware of some limitations, using a shape datastore, only very simple filters are supported,
     * joins with or, and not are not supported.
     * <p>
     *
     * @param filter
     *            search starts here
     * @return the first found bbox or <code>null</code> if no bbox was found.
     */
    public static Envelope firstBBOX( ComplexFilter filter ) {

        Envelope result = null;
        if ( filter == null ) {
            return result;
        }

        // special case: Filter contains only the BBOX-Operation
        if ( filter.getOperation().getOperatorId() != OperationDefines.BBOX ) {
            Stack<Operation> operations = new Stack<Operation>();
            operations.push( filter.getOperation() );
            while ( !operations.isEmpty() && result == null ) {
                Operation operation = operations.pop();
                switch ( operation.getOperatorId() ) {
                case OperationDefines.BBOX:
                    // found BBOX
                    result = ( (SpatialOperation) operation ).getGeometry().getEnvelope();
                    break;
                case OperationDefines.AND:
                    List<Operation> arguments = ( (LogicalOperation) operation ).getArguments();
                    for ( Operation arg : arguments ) {
                        operations.push( arg );
                    }
                    break;
                }
            }
        } else {
            result = ( (SpatialOperation) filter.getOperation() ).getGeometry().getEnvelope();
        }
        return result;
    }

    /**
     * Traverses the <tt>Filter</tt> -tree and returns all spatial filter operations. Their
     * logical relationships are ignored, because within the intended target context, only AND
     * operations are possible.
     *
     * @param filter
     *            search starts here
     * @return <tt>Filter</tt>-array
     */
    public static SpatialOperation[] extractSpatialFilter( ComplexFilter filter ) {

        ArrayList<SpatialOperation> spatialOps = new ArrayList<SpatialOperation>();

        // sanity check (Filter empty)
        if ( filter == null ) {
            return ( new SpatialOperation[0] );
        }

        Stack<Operation> operations = new Stack<Operation>();
        operations.push( filter.getOperation() );

        while ( !operations.isEmpty() ) {
            // get the top element from the stack
            Operation operation = operations.pop();

            switch ( OperationDefines.getTypeById( operation.getOperatorId() ) ) {
            case OperationDefines.TYPE_SPATIAL: {
                spatialOps.add( (SpatialOperation) operation );
                break;
            }
            case OperationDefines.TYPE_LOGICAL: {
                List<Operation> arguments = ( (LogicalOperation) operation ).getArguments();
                for ( int i = 0; i < arguments.size(); i++ ) {
                    operations.push( arguments.get( i ) );
                }
                break;
            }
            default: {
                break;
            }
            }

        }

        return spatialOps.toArray( ( new SpatialOperation[spatialOps.size()] ) );
    }

    /**
     * returns all {@link PropertyPath} definitions from the passed {@link Filter}
     *
     * @see PropertyPath
     * @see Filter
     *
     * @param filter
     * @return all PropertyPath definitions from the passed Filter
     * @throws PropertyPathResolvingException
     */
    public static List<PropertyPath> extractPropertyPaths( Filter filter )
                            throws PropertyPathResolvingException {

        List<PropertyPath> pp = new ArrayList<PropertyPath>();

        if ( filter instanceof ComplexFilter ) {
            pp = extractPropertyNameMapFromOperation( ( (ComplexFilter) filter ).getOperation(), pp );
        } else if ( filter instanceof FeatureFilter ) {
            // TODO
            // throw new PropertyPathResolvingException( "FeatureFilter not implemented yet." );
        }

        return pp;

    }

    private static List<PropertyPath> extractPropertyNameMapFromOperation( Operation operation, List<PropertyPath> list )
                            throws PropertyPathResolvingException {
        switch ( OperationDefines.getTypeById( operation.getOperatorId() ) ) {
        case OperationDefines.TYPE_SPATIAL: {
            list.add( ( (SpatialOperation) operation ).getPropertyName().getValue() );
            break;
        }
        case OperationDefines.TYPE_COMPARISON: {
            extractPropertyPaths( (ComparisonOperation) operation, list );
            break;
        }
        case OperationDefines.TYPE_LOGICAL: {
            extractPropertyPaths( (LogicalOperation) operation, list );
            break;
        }
        default: {
            break;
        }
        }
        return list;
    }

    private static List<PropertyPath> extractPropertyPaths( ComparisonOperation operation, List<PropertyPath> list )
                            throws PropertyPathResolvingException {
        switch ( operation.getOperatorId() ) {
        case OperationDefines.PROPERTYISEQUALTO:
        case OperationDefines.PROPERTYISLESSTHAN:
        case OperationDefines.PROPERTYISGREATERTHAN:
        case OperationDefines.PROPERTYISLESSTHANOREQUALTO:
        case OperationDefines.PROPERTYISGREATERTHANOREQUALTO: {
            extractPropertyPaths( ( (PropertyIsCOMPOperation) operation ).getFirstExpression(), list );
            extractPropertyPaths( ( (PropertyIsCOMPOperation) operation ).getSecondExpression(), list );
            break;
        }
        case OperationDefines.PROPERTYISLIKE: {
            list.add( ( (PropertyIsLikeOperation) operation ).getPropertyName().getValue() );
            break;
        }
        case OperationDefines.PROPERTYISNULL: {
            extractPropertyPaths( ( (PropertyIsNullOperation) operation ).getPropertyName(), list );
            break;
        }
        case OperationDefines.PROPERTYISBETWEEN: {
            extractPropertyPaths( ( (PropertyIsBetweenOperation) operation ).getLowerBoundary(), list );
            extractPropertyPaths( ( (PropertyIsBetweenOperation) operation ).getUpperBoundary(), list );
            list.add( ( (PropertyIsBetweenOperation) operation ).getPropertyName().getValue() );
            break;
        }
        default: {
            break;
        }
        }
        return list;
    }

    private static List<PropertyPath> extractPropertyPaths( LogicalOperation operation, List<PropertyPath> list )
                            throws PropertyPathResolvingException {
        List<Operation> operationList = operation.getArguments();
        Iterator<Operation> it = operationList.iterator();
        while ( it.hasNext() ) {
            extractPropertyNameMapFromOperation( it.next(), list );
        }
        return list;
    }

    /**
     * returns all {@link PropertyPath} definitions from the passed {@link Expression}
     *
     * @see PropertyPath
     * @see Expression
     *
     * @param expression
     * @param list
     * @return all PropertyPath definitions from the passed Expression
     * @throws PropertyPathResolvingException
     */
    public static List<PropertyPath> extractPropertyPaths( Expression expression, List<PropertyPath> list )
                            throws PropertyPathResolvingException {
        switch ( expression.getExpressionId() ) {
        case ExpressionDefines.PROPERTYNAME: {
            list.add( ( (PropertyName) expression ).getValue() );
            break;
        }
        case ExpressionDefines.ADD:
        case ExpressionDefines.SUB:
        case ExpressionDefines.MUL:
        case ExpressionDefines.DIV: {
            extractPropertyPaths( ( (ArithmeticExpression) expression ).getFirstExpression(), list );
            extractPropertyPaths( ( (ArithmeticExpression) expression ).getSecondExpression(), list );
            break;
        }
        case ExpressionDefines.FUNCTION: {
            // TODO: What about PropertyNames used here?
            break;
        }
        case ExpressionDefines.EXPRESSION:
        case ExpressionDefines.LITERAL: {
            break;
        }
        }
        return list;
    }

}
