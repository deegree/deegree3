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
package org.deegree.model.filterencoding.capabilities;

import org.deegree.ogcwebservices.getcapabilities.UnknownOperatorNameException;

/**
 * @author mschneider
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class OperatorFactory100 {

    // arithmetic operators as defined in filterCapabilities.xsd

    /**
     *
     */
    public final static String OPERATOR_LOGICAL_OPERATORS = "Logical_Operators";

    /**
     *
     */
    public final static String OPERATOR_SIMPLE_ARITHMETIC = "Simple_Arithmetic";

    /**
     *
     */
    public final static String OPERATOR_FUNCTIONS = "Functions";

    // comparison operators as defined in filterCapabilities.xsd

    /**
     *
     */
    public final static String OPERATOR_SIMPLE_COMPARISONS = "Simple_Comparisons";

    /**
     *
     */
    public final static String OPERATOR_LIKE = "Like";

    /**
     *
     */
    public final static String OPERATOR_BETWEEN = "Between";

    /**
     *
     */
    public final static String OPERATOR_NULL_CHECK = "NullCheck";

    // spatial operators as defined in filterCapabilities.xsd

    /**
     *
     */
    public final static String OPERATOR_BBOX = "BBOX";

    /**
     *
     */
    public final static String OPERATOR_EQUALS = "Equals";

    /**
     *
     */
    public final static String OPERATOR_DISJOINT = "Disjoint";

    /**
     *
     */
    public final static String OPERATOR_INTERSECT = "Intersect";

    /**
     *
     */
    public final static String OPERATOR_TOUCHES = "Touches";

    /**
     *
     */
    public final static String OPERATOR_CROSSES = "Crosses";

    /**
     *
     */
    public final static String OPERATOR_WITHIN = "Within";

    /**
     *
     */
    public final static String OPERATOR_CONTAINS = "Contains";

    /**
     *
     */
    public final static String OPERATOR_OVERLAPS = "Overlaps";

    /**
     *
     */
    public final static String OPERATOR_BEYOND = "Beyond";

    /**
     *
     */
    public final static String OPERATOR_DWITHIN = "DWithin";

    /**
     * @param name
     * @return a new instance
     * @throws UnknownOperatorNameException
     */
    public static SpatialOperator createSpatialOperator( String name )
                            throws UnknownOperatorNameException {
        if ( name.equals( OPERATOR_BBOX ) || name.equals( OPERATOR_EQUALS ) || name.equals( OPERATOR_DISJOINT )
             || name.equals( OPERATOR_INTERSECT ) || name.equals( OPERATOR_TOUCHES ) || name.equals( OPERATOR_CROSSES )
             || name.equals( OPERATOR_WITHIN ) || name.equals( OPERATOR_CONTAINS ) || name.equals( OPERATOR_OVERLAPS )
             || name.equals( OPERATOR_BEYOND ) || name.equals( OPERATOR_DWITHIN ) ) {
            return new SpatialOperator( name );
        }
        throw new UnknownOperatorNameException( "'" + name + "' is no known spatial operator." );
    }

    /**
     * @param name
     * @return a new instance
     * @throws UnknownOperatorNameException
     */
    public static Operator createComparisonOperator( String name )
                            throws UnknownOperatorNameException {
        if ( name.equals( OPERATOR_SIMPLE_COMPARISONS ) || name.equals( OPERATOR_LIKE )
             || name.equals( OPERATOR_BETWEEN ) || name.equals( OPERATOR_NULL_CHECK ) ) {
            return new Operator( name );
        }
        throw new UnknownOperatorNameException( "'" + name + "' is no known comparison operator." );
    }

    /**
     * @param name
     * @return a new instance
     * @throws UnknownOperatorNameException
     */
    public static Operator createArithmeticOperator( String name )
                            throws UnknownOperatorNameException {
        if ( name.equals( OPERATOR_SIMPLE_ARITHMETIC ) || name.equals( OPERATOR_FUNCTIONS ) ) {
            return new Operator( name );
        }
        throw new UnknownOperatorNameException( "'" + name + "' is no known arithmetic operator." );
    }

    /**
     * @param name
     * @param argumentCount
     * @return a new instance
     */
    public static Function createArithmeticFunction( String name, int argumentCount ) {
        return new Function( name, argumentCount );
    }
}
