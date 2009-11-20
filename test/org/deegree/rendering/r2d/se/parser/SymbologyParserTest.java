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

import static org.deegree.rendering.r2d.se.parser.SymbologyParser.parse;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;

/**
 * <code>SLD100ParserTest</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SymbologyParserTest extends TestCase {

    private static final Logger LOG = getLogger( SymbologyParserTest.class );

    /**
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     * @throws FileNotFoundException
     */
    @Test
    public void testAll()
                            throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {
        final XMLInputFactory fac = XMLInputFactory.newInstance();
        final Class<SymbologyParserTest> cls = SymbologyParserTest.class;
        File dir = new File( cls.getResource( "SymbologyParserTest.class" ).getFile() ).getParentFile();
        for ( File f : dir.listFiles() ) {
            if ( f.getName().endsWith( ".xml" ) ) {
                LOG.info( "Expecting {} to parse fine.", f );
                XMLStreamReader in = fac.createXMLStreamReader( f.toString(), new FileInputStream( f ) );
                in.next();
                assertNotNull( parse( in ) );
            }
            if ( f.getName().endsWith( ".bad" ) ) {
                LOG.info( "Expecting {} to fail.", f );
                XMLStreamReader in = fac.createXMLStreamReader( f.toString(), new FileInputStream( f ) );
                in.next();
                try {
                    parse( in );
                    assertEquals( true, false );
                } catch ( XMLStreamException e ) {
                    assertNotNull( e );
                }
            }
        }

    }

}
