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

package org.deegree.rendering.r2d.se.parser;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

/**
 * <code>SymbologyParserTest</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@RunWith(Parameterized.class)
public class SymbologyParserTest extends TestCase {

    private static final Logger LOG = getLogger( SymbologyParserTest.class );

    private String file;

    /**
     * @param testLabel
     * @param file
     */
    public SymbologyParserTest( String testLabel, String file ) {
        setName( testLabel ); // yeah, does not work, I know you nullchecker
        this.file = file;
    }

    /**
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws URISyntaxException
     */
    @Test
    public void singleTest()
                            throws XMLStreamException, FactoryConfigurationError, FileNotFoundException,
                            URISyntaxException {
        final XMLInputFactory fac = XMLInputFactory.newInstance();
        final Class<SymbologyParserTest> cls = SymbologyParserTest.class;

        if ( file.endsWith( ".xml" ) ) {
            LOG.debug( "Expecting {} to parse fine.", file );
            XMLStreamReader in = fac.createXMLStreamReader( cls.getResource( file ).toString(),
                                                            cls.getResourceAsStream( file ) );
            in.next();
            assertNotNull( SymbologyParser.INSTANCE.parse( in ) );
        }
        if ( file.endsWith( ".bad" ) ) {
            LOG.debug( "Expecting {} to fail.", file );
            XMLStreamReader in = fac.createXMLStreamReader( cls.getResource( file ).toString(),
                                                            cls.getResourceAsStream( file ) );
            in.next();
            try {
                SymbologyParser.INSTANCE.parse( in );
                assertEquals( true, false );
            } catch ( XMLStreamException e ) {
                assertNotNull( e );
            }
        }
    }

    /**
     * @return the files
     * @throws Exception
     */
    @Parameters
    public static LinkedList<Object[]> getFiles()
                            throws Exception {
        LinkedList<Object[]> tests = new LinkedList<Object[]>();

        // TODO think of a better way to do this (old hack of trying to find the bin directory seems not work w/ mvn)
        tests.add( new Object[] { "setest1.bad", "setest1.bad" } );
        tests.add( new Object[] { "setest1.xml", "setest1.xml" } );
        tests.add( new Object[] { "setest10.xml", "setest10.xml" } );
        tests.add( new Object[] { "setest11.xml", "setest11.xml" } );
        tests.add( new Object[] { "setest12.xml", "setest12.xml" } );
        tests.add( new Object[] { "setest13.xml", "setest13.xml" } );
        tests.add( new Object[] { "setest14.xml", "setest14.xml" } );
        tests.add( new Object[] { "setest15.xml", "setest15.xml" } );
        tests.add( new Object[] { "setest16.xml", "setest16.xml" } );
        tests.add( new Object[] { "setest17.xml", "setest17.xml" } );
        tests.add( new Object[] { "setest18.xml", "setest18.xml" } );
        tests.add( new Object[] { "setest19.xml", "setest19.xml" } );
        tests.add( new Object[] { "setest2.xml", "setest2.xml" } );
        tests.add( new Object[] { "setest20.xml", "setest20.xml" } );
        tests.add( new Object[] { "setest21.xml", "setest21.xml" } );
        tests.add( new Object[] { "setest22.xml", "setest22.xml" } );
        tests.add( new Object[] { "setest3.xml", "setest3.xml" } );
        tests.add( new Object[] { "setest4.xml", "setest4.xml" } );
        tests.add( new Object[] { "setest5.xml", "setest5.xml" } );
        tests.add( new Object[] { "setest6.xml", "setest6.xml" } );
        tests.add( new Object[] { "setest7.xml", "setest7.xml" } );
        tests.add( new Object[] { "setest8.xml", "setest8.xml" } );
        tests.add( new Object[] { "setest9.xml", "setest9.xml" } );
        tests.add( new Object[] { "sldtest1.bad", "sldtest1.bad" } );
        tests.add( new Object[] { "sldtest1.xml", "sldtest1.xml" } );
        tests.add( new Object[] { "sldtest10.xml", "sldtest10.xml" } );
        tests.add( new Object[] { "sldtest2.xml", "sldtest2.xml" } );
        tests.add( new Object[] { "sldtest3.xml", "sldtest3.xml" } );
        tests.add( new Object[] { "sldtest4.xml", "sldtest4.xml" } );
        tests.add( new Object[] { "sldtest5.xml", "sldtest5.xml" } );
        tests.add( new Object[] { "sldtest6.xml", "sldtest6.xml" } );
        tests.add( new Object[] { "sldtest7.xml", "sldtest7.xml" } );
        tests.add( new Object[] { "sldtest8.xml", "sldtest8.xml" } );
        tests.add( new Object[] { "sldtest9.xml", "sldtest9.xml" } );

        return tests;
    }

}
