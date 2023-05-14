/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.protocol.wps;

import java.util.LinkedList;

import org.deegree.services.wps.provider.sextante.ExampleData;

/**
 * This class wraps all literal test data as static attributes. <br>
 * A instance of this class presents one data set of test data.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class LiteralExampleData implements ExampleData {

    // all literal example data
    private static final LinkedList<LiteralExampleData> ALL_EXAMPLE_DATA = new LinkedList<LiteralExampleData>();

    // types of literal data
    private static final String BOOLEAN_TYPE = "boolean";

    private static final String NUMERICAL_VALUE_TYPE = "double";

    private static final String SELECTION_TYPE = "integer";

    private static final String STRING_TYPE = "string";

    // numerical value example data
    public static LiteralExampleData NUMERICAL_VALUE_0 = new LiteralExampleData( "0", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_1 = new LiteralExampleData( "1", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_2 = new LiteralExampleData( "2", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_3 = new LiteralExampleData( "3", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_4 = new LiteralExampleData( "4", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_5 = new LiteralExampleData( "5", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_10 = new LiteralExampleData( "10", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_50 = new LiteralExampleData( "50", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_100 = new LiteralExampleData( "100", NUMERICAL_VALUE_TYPE );
    
    public static LiteralExampleData NUMERICAL_VALUE_180 = new LiteralExampleData( "180", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_200 = new LiteralExampleData( "200", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_300 = new LiteralExampleData( "300", NUMERICAL_VALUE_TYPE );

    public static LiteralExampleData NUMERICAL_VALUE_400 = new LiteralExampleData( "400", NUMERICAL_VALUE_TYPE );

    // boolean value example data
    public static LiteralExampleData BOOLEAN_TRUE = new LiteralExampleData( "true", BOOLEAN_TYPE );

    public static LiteralExampleData BOOLEAN_FALSE = new LiteralExampleData( "false", BOOLEAN_TYPE );

    // selection value example data
    public static LiteralExampleData SELECTION_0 = new LiteralExampleData( "0", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_1 = new LiteralExampleData( "1", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_2 = new LiteralExampleData( "2", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_3 = new LiteralExampleData( "3", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_4 = new LiteralExampleData( "4", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_5 = new LiteralExampleData( "5", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_6 = new LiteralExampleData( "6", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_7 = new LiteralExampleData( "7", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_8 = new LiteralExampleData( "8", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_9 = new LiteralExampleData( "9", SELECTION_TYPE );

    public static LiteralExampleData SELECTION_10 = new LiteralExampleData( "10", SELECTION_TYPE );

    // string value example data
    public static LiteralExampleData STRING_VIEW = new LiteralExampleData( "VIEWS", STRING_TYPE );

    public static LiteralExampleData STRING_TEST = new LiteralExampleData( "sqrt({http://www.deegree.org/app}area)",
                                                                           STRING_TYPE );

    public static LiteralExampleData STRING_NAME_UPPERNAME_DATAORIGIN_AREA_QUERYBBOXOVERLAP = new LiteralExampleData(
                                                                                                                      "name,upperName,dataOrigin,area,queryBBOXOverlap",
                                                                                                                      STRING_TYPE );

    public static LiteralExampleData STRING_0 = new LiteralExampleData( "0", STRING_TYPE );

    // attributes of a literal
    private final String idCodeSpace;

    private final String value;

    private final String type;

    private final String uom;

    private LiteralExampleData( String idCodeSpace, String value, String type, String uom ) {

        this.idCodeSpace = idCodeSpace;
        this.value = value;
        this.type = type;
        this.uom = uom;

        ALL_EXAMPLE_DATA.add( this );
    }

    private LiteralExampleData( String value, String type ) {
        this( null, value, type, null );
    }

    public String getIdCodeSpace() {
        return idCodeSpace;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getUOM() {
        return uom;
    }

    public static LinkedList<LiteralExampleData> getAllData() {
        return ALL_EXAMPLE_DATA;
    }

    public static LinkedList<LiteralExampleData> getNumericalValues() {
        LinkedList<LiteralExampleData> result = new LinkedList<LiteralExampleData>();
        for ( LiteralExampleData data : ALL_EXAMPLE_DATA ) {
            if ( data.getType().equals( NUMERICAL_VALUE_TYPE ) )
                result.add( data );
        }
        return result;
    }

    public static LinkedList<LiteralExampleData> getBooleans() {
        LinkedList<LiteralExampleData> result = new LinkedList<LiteralExampleData>();
        for ( LiteralExampleData data : ALL_EXAMPLE_DATA ) {
            if ( data.getType().equals( BOOLEAN_TYPE ) )
                result.add( data );
        }
        return result;
    }

    public static LinkedList<LiteralExampleData> getSelections() {
        LinkedList<LiteralExampleData> result = new LinkedList<LiteralExampleData>();
        for ( LiteralExampleData data : ALL_EXAMPLE_DATA ) {
            if ( data.getType().equals( SELECTION_TYPE ) )
                result.add( data );
        }
        return result;
    }

    public static LinkedList<LiteralExampleData> getStrings() {
        LinkedList<LiteralExampleData> result = new LinkedList<LiteralExampleData>();
        for ( LiteralExampleData data : ALL_EXAMPLE_DATA ) {
            if ( data.getType().equals( STRING_TYPE ) )
                result.add( data );
        }
        return result;
    }

    public String toString() {
        String s = "";
        s += type + ": ";
        s += value;

        return s;
    }
}
