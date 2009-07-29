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
package org.deegree.feature.gml;

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.gml.GMLIdContext;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.junit.Test;

/**
 * Tests that check the extraction of {@link Feature}s from GML documents.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLFeatureDecoderTest {

    private static final String BASE_DIR = "testdata/features/";

    @Test
    public void testParsingPhilosopherFeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException {

        URL docURL = GMLFeatureDecoderTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( null, idContext );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );
    }
    
    @Test (expected=XMLParsingException.class)
    public void testParsingPhilosopherFeatureCollectionNoSchema()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException {

        URL docURL = GMLFeatureDecoderTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection_no_schema.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( null, idContext );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
    }
}
