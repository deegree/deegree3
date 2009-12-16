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

import static org.deegree.gml.GMLVersion.GML_30;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLObject;
import org.junit.Test;

public class DictionaryStreamReaderTest {

    @Test
    public void testParsingExample1()
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        URL url = DictionaryStreamReaderTest.class.getResource( "example_dictionary.gml" );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                         url.openStream() );
        // skip START_DOCUMENT event
        xmlStream.nextTag();

        GMLDocumentIdContext idContext = new GMLDocumentIdContext( GML_30 );
        
        DictionaryStreamReader dictReader = new DictionaryStreamReader( GML_30, xmlStream, idContext );
        Dictionary dict = (Dictionary) dictReader.parse();

        printDefinition( dict, "" );
        
        for (GMLObject o : idContext.getObjects().values()) {
            System.out.println (o.getId());
        }
    }
    
    private void printDefinition (Definition def, String indent) {
        System.out.println( indent + "-id: " + def.getId() );
        System.out.println( indent + "-description: " +def.getDescription() );
        for ( CodeType name : def.getNames() ) {
            System.out.println( indent + "-name: " + name );
        }        
        if (def instanceof Dictionary) {
            for ( Definition member : ((Dictionary) def) ) {
                printDefinition( member, indent + " " );
            }
        }
    }
}
