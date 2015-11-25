//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.feature.persistence.sql.version;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.deegree.feature.persistence.sql.version.VersionParser.VersionCode;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class VersionParserTest {

    @Test
    public void testParseVersionInteger_FromInteger()
                            throws Exception {
        int version = VersionParser.parseVersionInteger( "2" );

        assertThat( version, is( 2 ) );
    }

    @Test
    public void testParseVersionInteger_FromZero()
                            throws Exception {
        int version = VersionParser.parseVersionInteger( "0" );

        assertThat( version, is( -1 ) );
    }

    @Test
    public void testParseVersionInteger_FromNegativeInteger()
                            throws Exception {
        int version = VersionParser.parseVersionInteger( "-3" );

        assertThat( version, is( -1 ) );
    }

    @Test
    public void testParseVersionInteger_FromDouble()
                            throws Exception {
        int version = VersionParser.parseVersionInteger( "2.9" );

        assertThat( version, is( -1 ) );
    }

    @Test
    public void testParseVersionInteger_FromNull()
                            throws Exception {
        int version = VersionParser.parseVersionInteger( null );

        assertThat( version, is( -1 ) );
    }

    @Test
    public void testParseVersionInteger_FromString()
                            throws Exception {
        int version = VersionParser.parseVersionInteger( "LATEST" );

        assertThat( version, is( -1 ) );
    }

    @Test
    public void testParseVersionCode_AllVersionCodes()
                            throws Exception {
        for ( VersionCode versionCode : VersionCode.values() ) {
            VersionCode version = VersionParser.getVersionCode( versionCode.name() );
            assertThat( version, is( versionCode ) );
        }
    }

    @Test
    public void testParseVersionCode_FromInteger()
                            throws Exception {
        VersionCode version = VersionParser.getVersionCode( "1" );

        assertThat( version, is( nullValue() ) );
    }

    @Test
    public void testParseVersionCode_FromNull()
                            throws Exception {
        VersionCode version = VersionParser.getVersionCode( null );

        assertThat( version, is( nullValue() ) );
    }

    @Test
    public void testParseVersionCode_FromInvalidString()
                            throws Exception {
        VersionCode version = VersionParser.getVersionCode( "LAST" );

        assertThat( version, is( nullValue() ) );
    }

    @Test
    public void testParseVersionCode_FromStringCaseSensitive()
                            throws Exception {
        VersionCode version = VersionParser.getVersionCode( "FiRSt" );

        assertThat( version, is( VersionCode.FIRST ) );
    }

}
