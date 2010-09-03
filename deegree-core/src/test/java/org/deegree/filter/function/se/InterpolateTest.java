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

package org.deegree.filter.function.se;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.deegree.rendering.r2d.se.parser.SymbologyParser;
import org.deegree.rendering.r2d.se.parser.SymbologyParserTest;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * <code>InterpolateTest</code>
 * 
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InterpolateTest extends TestCase {

    private static final Logger LOG = getLogger( InterpolateTest.class );

    private static Interpolate interp = null;

    @BeforeClass
    private void loadCategorizeFromXml()
                            throws URISyntaxException, XMLStreamException, FileNotFoundException {
        URI uri = SymbologyParserTest.class.getResource( "setest18.xml" ).toURI();
        LOG.debug( "Loading resource: {}", uri );
        File f = new File( uri );
        final XMLInputFactory fac = XMLInputFactory.newInstance();
        XMLStreamReader in = fac.createXMLStreamReader( f.toString(), new FileInputStream( f ) );
        in.next();
        if ( in.getEventType() == XMLStreamConstants.START_DOCUMENT ) {
            in.nextTag();
        }
        in.require( XMLStreamConstants.START_ELEMENT, null, "RasterSymbolizer" );
        Symbolizer<RasterStyling> symb = SymbologyParser.INSTANCE.parseRasterSymbolizer( in, null );
        RasterStyling rs = symb.getBase();
        interp = rs.interpolate;
    }

    @Test
    public void testInterpolate()
                            throws FileNotFoundException, URISyntaxException, XMLStreamException {
        loadCategorizeFromXml();
        interp.buildLookupArrays();
        LOG.debug( "Interpolate: {}", interp );
        test( -0.51 );
        test( 0 );
        test( 0.49 );
        test( 0.5 );
        test( 0.51 );
        test( 1 );
        test( 2 );
        test( 7 );
        test( 8 );
        test( 9 );
        test( 15 );
        test( 16 );
        test( 17 );
        test( 31 );
        test( 32 );
        test( 33 );
        test( 48 );
        test( 48.3 );
        test( 50 );

        test( 225 );
        test( 240 );
        test( 250 );
    }

    private void test( double x ) {
        LOG.debug( "Lookup({}): {}", x, interp.lookup2Color( x ).toString() );
        LOG.debug( "------------------" );
        // assertEquals( x1, x2 );
    }

}
