//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.feature.persistence.sql;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:wanhoff@lat-lon.de">Jeronimo Wanhoff</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class SQLFeatureStoreTest {

    
    @Test(expected = NullPointerException.class)
    public void testFixColumnNameIfStartingWithDigitWithNullString() {
        String column = null;
        SQLFeatureStore.fixColumnNameIfStartingWithDigit( column );
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFixColumnNameIfStartingWithDigitWithEmptyString() {
        String column = "";
        String actual = SQLFeatureStore.fixColumnNameIfStartingWithDigit( column );
        String expected = "";
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testFixColumnNameIfStartingWithDigitWithWithColumnStartingWithCharacter() {
        String column = "Hallo";
        String actual = SQLFeatureStore.fixColumnNameIfStartingWithDigit( column );
        String expected = "Hallo";
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testFixColumnNameIfStartingWithDigitWithWithColumnStartingWithDigit() {
        String column = "123";
        String actual = SQLFeatureStore.fixColumnNameIfStartingWithDigit( column );
        String expected = "\"123\"";
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testFixColumnNameIfStartingWithDigitWithWithTableAndColumnStartingWithCharacter() {
        String column = "table.Hallo";
        String actual = SQLFeatureStore.fixColumnNameIfStartingWithDigit( column );
        String expected = "table.Hallo";
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testFixColumnNameIfStartingWithDigitWithWithTableAndColumnStartingWithDigit() {
        String column = "table.123";
        String actual = SQLFeatureStore.fixColumnNameIfStartingWithDigit( column );
        String expected = "table.\"123\"";
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testFixColumnNameIfStartingWithDigitWithWithSchemaAndTableAndColumnStartingWithCharacter() {
        String column = "schema.table.Hallo";
        String actual = SQLFeatureStore.fixColumnNameIfStartingWithDigit( column );
        String expected = "schema.table.Hallo";
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testFixColumnNameIfStartingWithDigitWithWithSchemaAndTableAndColumnStartingWithDigit() {
        String column = "schema.table.123";
        String actual = SQLFeatureStore.fixColumnNameIfStartingWithDigit( column );
        String expected = "schema.table.\"123\"";
        Assert.assertEquals( expected, actual );
    }

}
