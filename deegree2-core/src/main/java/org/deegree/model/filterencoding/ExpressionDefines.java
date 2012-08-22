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

import java.util.HashMap;
import java.util.Map;

/**
 * Defines codes and constants for easy coping with the different kinds of Expressions (both
 * XML-Entities & JavaObjects).
 *
 * @author Markus Schneider
 * @version 06.08.2002
 */
public class ExpressionDefines {

    // expression codes
    /**
     *
     */
    public static final int EXPRESSION = 0;

    /**
     *
     */
    public static final int PROPERTYNAME = 1;

    /**
     *
     */
    public static final int LITERAL = 2;

    /**
     *
     */
    public static final int FUNCTION = 3;

    /**
     *
     */
    public static final int ADD = 4;

    /**
     *
     */
    public static final int SUB = 5;

    /**
     *
     */
    public static final int MUL = 6;

    /**
     *
     */
    public static final int DIV = 7;

    /**
     *
     */
    public static final int UNKNOWN = -1;

    /**
     * Returns the id of an expression for a given name.
     *
     * @param name
     *
     * @return EXPRESSION / PROPERTYNAME / LITERAL / ...
     */
    public synchronized static int getIdByName( String name ) {
        if ( names == null )
            buildHashMaps();
        ExpressionInfo expression = names.get( name.toLowerCase() );
        if ( expression == null )
            return UNKNOWN;
        return expression.id;
    }

    /**
     * Returns the name of an expression for a given id.
     *
     * @param id
     *
     * @return null / Name of expression
     */
    public static String getNameById( int id ) {
        if ( names == null )
            buildHashMaps();
        ExpressionInfo expression = ids.get( new Integer( id ) );
        if ( expression == null )
            return null;
        return expression.name;
    }

    // used to associate names with the expressions
    private static Map<String, ExpressionInfo> names = null;

    // used to associate ids (Integers) with the expressions
    private static Map<Integer, ExpressionInfo> ids = null;

    private static void addExpression( int id, String name ) {
        ExpressionInfo expressionInfo = new ExpressionInfo( id, name );
        names.put( name.toLowerCase(), expressionInfo );
        ids.put( new Integer( id ), expressionInfo );
    }

    private static void buildHashMaps() {
        names = new HashMap<String, ExpressionInfo>();
        ids = new HashMap<Integer, ExpressionInfo>();
        addExpression( EXPRESSION, "Expression" );
        addExpression( PROPERTYNAME, "PropertyName" );
        addExpression( LITERAL, "Literal" );
        addExpression( FUNCTION, "Function" );
        addExpression( ADD, "Add" );
        addExpression( SUB, "Sub" );
        addExpression( MUL, "Mul" );
        addExpression( DIV, "Div" );
    }
}

class ExpressionInfo {

    int id;

    String name;

    ExpressionInfo( int id, String name ) {
        this.id = id;
        this.name = name;

    }
}
