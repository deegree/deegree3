//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.gml;

import java.io.StringReader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.junit.Assert;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLStandardFeaturePropsParserTest {

    @Test
    public void testParsing311Empty() throws XMLStreamException, FactoryConfigurationError {
        String xml = "<Object xmlns=\"http://www.opengis.net/gml\">";
        xml += "</Object>";
        StandardFeatureProps props = parse311( xml );
        Assert.assertNull( props.getDescription() );
        Assert.assertEquals( 0, props.getNames().length );
        Assert.assertNull( props.getBoundedBy() );
    }

    @Test
    public void testParsing311Example1() throws XMLStreamException, FactoryConfigurationError {
        String xml = "<Object xmlns=\"http://www.opengis.net/gml\">";
        xml += "<description>A description property with an inline value.</description>";
        xml += "</Object>";
        StandardFeatureProps props = parse311( xml );
        Assert.assertEquals( "A description property with inline value.", props.getDescription().getString() );
        Assert.assertEquals( 0, props.getNames().length );
        Assert.assertNull( props.getBoundedBy() );
    }    

    @Test
    public void testParsing311Example2() throws XMLStreamException, FactoryConfigurationError {
        String xml = "<Object xmlns=\"http://www.opengis.net/gml\">";
        xml += "<description>A description property with inline value.</description>";
        xml += "<name>NAME1</name>";
        xml += "<name codeSpace=\"deegree\">NAME2</name>";
        xml += "</Object>";
        StandardFeatureProps props = parse311( xml );
        Assert.assertEquals( "A description property with inline value.", props.getDescription().getString() );
        Assert.assertEquals( 2, props.getNames().length );
        Assert.assertEquals( "NAME1",  props.getNames()[0].getCode());
        Assert.assertNull( props.getNames()[0].getCodeSpace());
        Assert.assertEquals( "NAME2",  props.getNames()[1].getCode());
        Assert.assertEquals( "deegree",  props.getNames()[1].getCodeSpace());
        Assert.assertNull( props.getBoundedBy() );
    }    
    
    private StandardFeatureProps parse311( String xml ) throws XMLStreamException, FactoryConfigurationError {
        XMLStreamReader innerReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader( xml ));
        XMLStreamReaderWrapper xmlStream = new XMLStreamReaderWrapper(innerReader, null );
        xmlStream.nextTag();
        return GMLStandardFeaturePropsParser.parse311( xmlStream );
    }
}
