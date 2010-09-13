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

    public static final String BOOLEAN = "boolean";

    public static final String NUMERICAL_VALUE = "double";

    public static final String STRING = "string";

    // all literal example data
    private static final LinkedList<LiteralExampleData> ALL_LITERAL_EXAMPLE_DATA = new LinkedList<LiteralExampleData>();

    private final String id;

    private final String idCodeSpace;

    private final String value;

    private final String type;

    private final String uom;

    public static LiteralExampleData NUMERICAL_VALUE_1 = new LiteralExampleData( "one", "1", NUMERICAL_VALUE );

    public static LiteralExampleData NUMERICAL_VALUE_2 = new LiteralExampleData( "two", "2", NUMERICAL_VALUE );

    public static LiteralExampleData NUMERICAL_VALUE_3 = new LiteralExampleData( "three", "3", NUMERICAL_VALUE );

    public static LiteralExampleData NUMERICAL_VALUE_4 = new LiteralExampleData( "four", "4", NUMERICAL_VALUE );

    public LiteralExampleData( String id, String idCodeSpace, String value, String type, String uom ) {
        this.id = id;
        this.idCodeSpace = idCodeSpace;
        this.value = value;
        this.type = type;
        this.uom = uom;

        ALL_LITERAL_EXAMPLE_DATA.add( this );
    }

    public LiteralExampleData( String id, String value, String type ) {
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
        return ALL_LITERAL_EXAMPLE_DATA;
    }

    public String toString() {
        String s = "";
        s += type + ": ";
        s += value;

        return s;
    }
}
