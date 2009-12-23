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
package org.deegree.gml.dictionary;

import static junit.framework.Assert.assertEquals;
import static org.deegree.gml.GMLVersion.GML_30;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Test;

/**
 * Tests that check the correct reading of {@link Definition} and {@link Dictionary} objects from GML documents.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLDictionaryReaderTest {

    @Test
    public void testReadExampleDictionary()
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        URL url = GMLDictionaryReaderTest.class.getResource( "example_dictionary.gml" );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                         url.openStream() );
        // skip START_DOCUMENT event
        xmlStream.nextTag();

        GMLDictionaryReader dictReader = new GMLDictionaryReader( GML_30, xmlStream, new GMLDocumentIdContext( GML_30 ) );

        Dictionary dict = (Dictionary) dictReader.read();
        assertEquals( "CodeLists", dict.getId() );
        assertEquals( 2, dict.size() );
        assertEquals( "XP_HorizontaleAusrichtung", dict.get( 0 ).getId() );
        assertEquals( "XP_BedeutungenBereich", dict.get( 1 ).getId() );
    }

    @Test
    public void testWriteExampleDictionary()
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        URL url = GMLDictionaryReaderTest.class.getResource( "example_dictionary.gml" );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                         url.openStream() );
        // skip START_DOCUMENT event
        xmlStream.nextTag();

        GMLDictionaryReader dictReader = new GMLDictionaryReader( GML_30, xmlStream, new GMLDocumentIdContext( GML_30 ) );

        Dictionary dict = (Dictionary) dictReader.read();        

        XMLMemoryStreamWriter xmlWriter = new XMLMemoryStreamWriter();
        GMLDictionaryWriter writer = new GMLDictionaryWriter( GML_30, new FormattingXMLStreamWriter( xmlWriter.getXMLStreamWriter() ));
        writer.write( dict );
        xmlWriter.getXMLStreamWriter().close();
//        System.out.println (xmlWriter.toString());
    }
}
