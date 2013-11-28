package org.deegree.feature.persistence.sql;

import org.junit.Assert;
import org.junit.Test;

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
