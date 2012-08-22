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

import org.deegree.model.feature.Feature;

/**
 * Encapsulates the information of a <Filter> element that contains an Operation (only) (as defined
 * in the Filter DTD). Operation is one of the following types:
 * <ul>
 * <li>spatial_ops</li>
 * <li>comparison_ops</li>
 * <li>logical_ops</li>
 * </ul>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ComplexFilter extends AbstractFilter {

    /**
     * Operation the ComplexFilter is based on
     */
    private Operation operation;

    /**
     * Constructs a new ComplexFilter based on the given operation.
     *
     * @param operation
     */
    public ComplexFilter( Operation operation ) {
        this.operation = operation;
    }

    /**
     * Constructs a new <tt>ComplexFilter<tt> that consists of an
     * empty <tt>LogicalOperation</tt> of the given type.
     * <p>
     * @param operatorId OperationDefines.AND, OperationDefines.OR or
     * 		  OperationDefines.NOT
     */
    public ComplexFilter( int operatorId ) {
        operation = new LogicalOperation( operatorId, new ArrayList<Operation>() );
    }

    /**
     * Constructs a new <tt>ComplexFilter<tt> that consists of a
     * <tt>LogicalOperation</tt> with the given <tt>Filter</tt>.
     * <p>
     * @param filter1 first Filter to be used
     * @param filter2 second Filter to be used
     * 	      null, if operatorId == OperationDefines.NOT
     * @param operatorId OperationDefines.AND, OperationDefines.OR or
     * 		  OperationDefines.NOT
     */
    public ComplexFilter( ComplexFilter filter1, ComplexFilter filter2, int operatorId ) {

        // extract the Operations from the Filters
        ArrayList<Operation> arguments = new ArrayList<Operation>();
        arguments.add( filter1.getOperation() );
        if ( filter2 != null )
            arguments.add( filter2.getOperation() );

        operation = new LogicalOperation( operatorId, arguments );
    }

    /**
     * Returns the contained Operation.
     *
     * @return the contained Operation.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Calculates the <tt>Filter</tt>'s logical value based on the certain property values of the
     * given feature.
     *
     * @param feature
     *            that determines the values of <tt>PropertyNames</tt> in the expression
     * @return true, if the <tt>Filter</tt> evaluates to true, else false
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    public boolean evaluate( Feature feature )
                            throws FilterEvaluationException {
        return operation.evaluate( feature );
    }

    public StringBuffer toXML() {
        return to110XML();
    }

    public StringBuffer to100XML() {
        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<ogc:Filter xmlns:ogc='http://www.opengis.net/ogc'>" );
        sb.append( operation.to100XML() );
        sb.append( "</ogc:Filter>\n" );
        return sb;
    }

    public StringBuffer to110XML() {
        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<ogc:Filter xmlns:ogc='http://www.opengis.net/ogc'>" );
        sb.append( operation.to110XML() );
        sb.append( "</ogc:Filter>\n" );
        return sb;
    }
}
