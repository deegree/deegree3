//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.featureinfo.parsing;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.Assert;

import org.deegree.feature.FeatureCollection;
import org.junit.Test;

/**
 * Test feature info parsing workarounds.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureInfoParserTest {

    @Test
    public void testEsriCollection()
                            throws XMLStreamException {
        InputStream in = FeatureInfoParserTest.class.getResourceAsStream( "esri1.xml" );
        XMLInputFactory fac = XMLInputFactory.newInstance();
        XMLStreamReader xin = fac.createXMLStreamReader( in );
        xin.next();
        FeatureCollection fc = FeatureInfoParser.parseAsFeatureCollection( xin, "test" );
        Assert.assertEquals( 1, fc.size() );
    }

    @Test
    public void testEmptyEsriCollection()
                            throws XMLStreamException {
        InputStream in = FeatureInfoParserTest.class.getResourceAsStream( "esri2.xml" );
        XMLInputFactory fac = XMLInputFactory.newInstance();
        XMLStreamReader xin = fac.createXMLStreamReader( in );
        xin.next();
        FeatureCollection fc = FeatureInfoParser.parseAsFeatureCollection( xin, "test" );
        Assert.assertEquals( 0, fc.size() );
    }

    @Test
    public void testNamespacedEsriCollection()
                            throws XMLStreamException {
        InputStream in = FeatureInfoParserTest.class.getResourceAsStream( "esriwithnamespace.xml" );
        XMLInputFactory fac = XMLInputFactory.newInstance();
        XMLStreamReader xin = fac.createXMLStreamReader( in );
        xin.next();
        FeatureCollection fc = FeatureInfoParser.parseAsFeatureCollection( xin, "test" );
        Assert.assertEquals( 8, fc.size() );
    }

}
