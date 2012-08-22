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
 * Defines codes and constants for easy coping with the different kinds of OperationsMetadata (both XML-Entities &
 * JavaObjects).
 *
 * @author Markus Schneider
 * @version 06.08.2002
 */
public class OperationDefines {

    // used to associate names with the OperationInfos
    private static Map<String, OperationInfo> names = null;

    // used to associate ids (Integers) with the OperationInfos
    private static Map<Integer, OperationInfo> ids = null;

    // different types of operations
    /**
     *
     */
    public static final int TYPE_SPATIAL = 0;

    /**
     *
     */
    public static final int TYPE_COMPARISON = 1;

    /**
     *
     */
    public static final int TYPE_LOGICAL = 2;

    /**
     *
     */
    public static final int TYPE_UNKNOWN = -1;

    // spatial operations
    /**
     *
     */
    public static final int EQUALS = 0;

    /**
     *
     */
    public static final int DISJOINT = 1;

    /**
     *
     */
    public static final int INTERSECTS = 2;

    /**
     *
     */
    public static final int TOUCHES = 3;

    /**
     *
     */
    public static final int CROSSES = 4;

    /**
     *
     */
    public static final int WITHIN = 5;

    /**
     *
     */
    public static final int CONTAINS = 6;

    /**
     *
     */
    public static final int OVERLAPS = 7;

    /**
     *
     */
    public static final int BEYOND = 8;

    /**
     *
     */
    public static final int BBOX = 9;

    // calvin added on 10/21/2003
    /**
     *
     */
    public static final int DWITHIN = 10;

    // comparison operations
    /**
     *
     */
    public static final int PROPERTYISEQUALTO = 100;

    /**
     *
     */
    public static final int PROPERTYISNOTEQUALTO = 108;

    /**
     *
     */
    public static final int PROPERTYISLESSTHAN = 101;

    /**
     *
     */
    public static final int PROPERTYISGREATERTHAN = 102;

    /**
     *
     */
    public static final int PROPERTYISLESSTHANOREQUALTO = 103;

    /**
     *
     */
    public static final int PROPERTYISGREATERTHANOREQUALTO = 104;

    /**
     *
     */
    public static final int PROPERTYISLIKE = 105;

    /**
     *
     */
    public static final int PROPERTYISNULL = 106;

    /**
     *
     */
    public static final int PROPERTYISBETWEEN = 107;

    /**
     *
     */
    public static final int PROPERTYISINSTANCEOF = 150;

    // logical operations
    /**
     *
     */
    public static final int AND = 200;

    /**
     *
     */
    public static final int OR = 201;

    /**
     *
     */
    public static final int NOT = 202;

    /**
     *
     */
    public static final int UNKNOWN = -1;

    static {
        if ( names == null )
            buildHashMaps();
    }

    /**
     * Returns the type of an operation for a given name.
     *
     * @param name
     *
     * @return TYPE_SPATIAL / TYPE_COMPARISON / TYPE_LOGICAL / TYPE_UNKNOWN
     */
    public static int getTypeByName( String name ) {
        OperationInfo operationInfo = names.get( name );
        if ( operationInfo == null )
            return TYPE_UNKNOWN;
        return operationInfo.type;
    }

    /**
     * Returns the id of an operation for a given name.
     *
     * @param name
     *
     * @return BBOX / PROPERTYISEQUAL / AND / ...
     */
    public static int getIdByName( String name ) {
        OperationInfo operationInfo = names.get( name.toLowerCase() );
        if ( operationInfo == null )
            return UNKNOWN;
        return operationInfo.id;
    }

    /**
     * Returns the type of an operation for a given id.
     *
     * @param id
     *
     * @return TYPE_SPATIAL / TYPE_COMPARISON / TYPE_LOGICAL / TYPE_UNKNOWN
     */
    public static int getTypeById( int id ) {
        OperationInfo operationInfo = ids.get( new Integer( id ) );
        if ( operationInfo == null )
            return TYPE_UNKNOWN;
        return operationInfo.type;
    }

    /**
     * Returns the name of an operation for a given id.
     *
     * @param id
     *
     * @return null / Name of operation
     */
    public static String getNameById( int id ) {

        OperationInfo operationInfo = ids.get( new Integer( id ) );
        if ( operationInfo == null )
            return null;
        return operationInfo.name;
    }

    private static void addOperationInfo( int id, String name, int type ) {
        OperationInfo operationInfo = new OperationInfo( id, type, name );
        names.put( name, operationInfo );
        names.put( name.toLowerCase(), operationInfo );
        names.put( name.toUpperCase(), operationInfo );
        ids.put( new Integer( id ), operationInfo );
    }

    private static void buildHashMaps() {
        names = new HashMap<String, OperationInfo>( 25 );
        ids = new HashMap<Integer, OperationInfo>( 25 );

        addOperationInfo( BBOX, "BBOX", TYPE_SPATIAL );
        addOperationInfo( EQUALS, "Equals", TYPE_SPATIAL );
        addOperationInfo( DISJOINT, "Disjoint", TYPE_SPATIAL );
        addOperationInfo( INTERSECTS, "Intersects", TYPE_SPATIAL );
        addOperationInfo( TOUCHES, "Touches", TYPE_SPATIAL );
        addOperationInfo( CROSSES, "Crosses", TYPE_SPATIAL );
        addOperationInfo( WITHIN, "Within", TYPE_SPATIAL );
        addOperationInfo( CONTAINS, "Contains", TYPE_SPATIAL );
        addOperationInfo( OVERLAPS, "Overlaps", TYPE_SPATIAL );
        addOperationInfo( BEYOND, "Beyond", TYPE_SPATIAL );
        addOperationInfo( DWITHIN, "DWithin", TYPE_SPATIAL );

        addOperationInfo( PROPERTYISEQUALTO, "PropertyIsEqualTo", TYPE_COMPARISON );
        addOperationInfo( PROPERTYISNOTEQUALTO, "PropertyIsNotEqualTo", TYPE_COMPARISON );
        addOperationInfo( PROPERTYISLESSTHAN, "PropertyIsLessThan", TYPE_COMPARISON );
        addOperationInfo( PROPERTYISGREATERTHAN, "PropertyIsGreaterThan", TYPE_COMPARISON );
        addOperationInfo( PROPERTYISLESSTHANOREQUALTO, "PropertyIsLessThanOrEqualTo", TYPE_COMPARISON );
        addOperationInfo( PROPERTYISGREATERTHANOREQUALTO, "PropertyIsGreaterThanOrEqualTo", TYPE_COMPARISON );
        addOperationInfo( PROPERTYISLIKE, "PropertyIsLike", TYPE_COMPARISON );
        addOperationInfo( PROPERTYISNULL, "PropertyIsNull", TYPE_COMPARISON );
        addOperationInfo( PROPERTYISBETWEEN, "PropertyIsBetween", TYPE_COMPARISON );
        addOperationInfo( PROPERTYISINSTANCEOF, "PropertyIsInstanceOf", TYPE_COMPARISON );

        addOperationInfo( AND, "And", TYPE_LOGICAL );
        addOperationInfo( OR, "Or", TYPE_LOGICAL );
        addOperationInfo( NOT, "Not", TYPE_LOGICAL );
    }
}

class OperationInfo {
    int id;

    int type;

    String name;

    OperationInfo( int id, int type, String name ) {
        this.id = id;
        this.type = type;
        this.name = name;
    }
}
