//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.client.mdeditor.configuration;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.deegree.client.mdeditor.util.StringPairCompararator;
import org.deegree.commons.utils.StringPair;
import org.junit.Test;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class CodeListConfigurationParserTest extends TestCase {

    @Test
    public void testParseCodeListLabels()
                            throws ConfigurationException {
        Locale locale = new Locale( "de" );
        List<StringPair> codes = ConfigurationManager.getConfiguration().getCodeListLabels( "hierarchylevel", locale );
        Collections.sort( codes, new StringPairCompararator( false ) );

        assertNotNull( codes );
        assertEquals( 4, codes.size() );

        assertNotNull( codes.get( 0 ).first );
        assertEquals( "application", codes.get( 0 ).first );
        assertEquals( "Application", codes.get( 0 ).second );

        assertNotNull( codes.get( 1 ).first );
        assertEquals( "dataset", codes.get( 1 ).first );
        assertEquals( "Datensatz", codes.get( 1 ).second );

        assertNotNull( codes.get( 2 ).first );
        assertEquals( "dataseries", codes.get( 2 ).first );
        assertEquals( "Datenserie", codes.get( 2 ).second );

        assertNotNull( codes.get( 3 ).first );
        assertEquals( "test", codes.get( 3 ).first );
        assertEquals( "test", codes.get( 3 ).second );

        locale = new Locale( "en" );
        codes = ConfigurationManager.getConfiguration().getCodeListLabels( "hierarchylevel", locale );
        Collections.sort( codes, new StringPairCompararator( false ) );
        System.out.println( codes );
        assertNotNull( codes );
        assertEquals( 4, codes.size() );

        assertNotNull( codes.get( 0 ).first );
        assertEquals( "application", codes.get( 0 ).first );
        assertEquals( "Application", codes.get( 0 ).second );

        assertNotNull( codes.get( 1 ).first );
        assertEquals( "dataseries", codes.get( 1 ).first );
        assertEquals( "Dataseries", codes.get( 1 ).second );

        assertNotNull( codes.get( 2 ).first );
        assertEquals( "dataset", codes.get( 2 ).first );
        assertEquals( "Dataset", codes.get( 2 ).second );

        assertNotNull( codes.get( 3 ).first );
        assertEquals( "test", codes.get( 3 ).first );
        assertEquals( "test", codes.get( 3 ).second );

    }

    @Test
    public void testParseCodeListMetadata()
                            throws ConfigurationException {
        String value = ConfigurationManager.getConfiguration().getCodeListValue( "countries", "brd" );
        assertNotNull( value );
        assertTrue( value.contains( "5.301157108247309, 12.558214689996907, 45.42468009225714, 50.888249141163755" ) );

        value = ConfigurationManager.getConfiguration().getCodeListValue( "countries", "niedersachsen" );
        assertNotNull( value );
        assertEquals( "niedersachsen", value );
    }
}
