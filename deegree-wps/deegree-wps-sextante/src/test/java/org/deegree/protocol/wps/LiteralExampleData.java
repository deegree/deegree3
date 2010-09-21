//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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

/**
 * This class wraps all literal test data as static attributes. <br>
 * A instance of this class presents one data set of test data.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class LiteralExampleData implements ExampleData {

    // all literal example data
    private static final LinkedList<LiteralExampleData> ALL_EXAMPLE_DATA = new LinkedList<LiteralExampleData>();

    // types of literal data
    public static final String TYPE_BOOLEAN = "boolean";

    public static final String TYPE_NUMERICAL_VALUE = "double";

    public static final String TYPE_SELECTION = "string";

    public static final String TYPE_STRING = "string";

    // numerical value example data
    public static LiteralExampleData NUMERICAL_VALUE_0 = new LiteralExampleData( "NUMERICAL_VALUE_0", "0",
                                                                                 TYPE_NUMERICAL_VALUE );

    public static LiteralExampleData NUMERICAL_VALUE_1 = new LiteralExampleData( "NUMERICAL_VALUE_1", "1",
                                                                                 TYPE_NUMERICAL_VALUE );

    public static LiteralExampleData NUMERICAL_VALUE_100 = new LiteralExampleData( "NUMERICAL_VALUE_100", "100",
                                                                                   TYPE_NUMERICAL_VALUE );

    public static LiteralExampleData NUMERICAL_VALUE_200 = new LiteralExampleData( "NUMERICAL_VALUE_200", "200",
                                                                                   TYPE_NUMERICAL_VALUE );

    public static LiteralExampleData NUMERICAL_VALUE_300 = new LiteralExampleData( "NUMERICAL_VALUE_300", "300",
                                                                                   TYPE_NUMERICAL_VALUE );

    public static LiteralExampleData NUMERICAL_VALUE_400 = new LiteralExampleData( "NUMERICAL_VALUE_400", "400",
                                                                                   TYPE_NUMERICAL_VALUE );

    // boolean value example data
    public static LiteralExampleData BOOLEAN_TRUE = new LiteralExampleData( "BOOLEAN_TRUE", "true", TYPE_BOOLEAN );

    public static LiteralExampleData BOOLEAN_FALSE = new LiteralExampleData( "BOOLEAN_FALSE", "false", TYPE_BOOLEAN );

    // selection value example data
    public static LiteralExampleData SELECTION_0 = new LiteralExampleData( "SELECTION_0", "0", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_1 = new LiteralExampleData( "SELECTION_1", "1", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_2 = new LiteralExampleData( "SELECTION_2", "2", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_3 = new LiteralExampleData( "SELECTION_3", "3", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_4 = new LiteralExampleData( "SELECTION_4", "4", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_5 = new LiteralExampleData( "SELECTION_5", "5", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_6 = new LiteralExampleData( "SELECTION_6", "6", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_7 = new LiteralExampleData( "SELECTION_7", "7", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_8 = new LiteralExampleData( "SELECTION_8", "8", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_9 = new LiteralExampleData( "SELECTION_9", "9", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_10 = new LiteralExampleData( "SELECTION_10", "10", TYPE_SELECTION );

    public static LiteralExampleData SELECTION_INTEGER = new LiteralExampleData( "SELECTION_INTEGER", "Integer",
                                                                                 TYPE_SELECTION );

    public static LiteralExampleData SELECTION_DOUBLE = new LiteralExampleData( "SELECTION_DOUBLE", "Double",
                                                                                TYPE_SELECTION );

    public static LiteralExampleData SELECTION_STRING = new LiteralExampleData( "SELECTION_STRING", "String",
                                                                                TYPE_SELECTION );

    // attributes of a literal
    private final String id;

    private final String idCodeSpace;

    private final String value;

    private final String type;

    private final String uom;

    private LiteralExampleData( String id, String idCodeSpace, String value, String type, String uom ) {
        this.id = id;
        this.idCodeSpace = idCodeSpace;
        this.value = value;
        this.type = type;
        this.uom = uom;

        ALL_EXAMPLE_DATA.add( this );
    }

    private LiteralExampleData( String id, String value, String type ) {
        this( id, null, value, type, null );
    }

    public String getId() {
        return id;
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

    public String toString() {
        String s = "";
        s += type + ": ";
        s += value;

        return s;
    }
}
