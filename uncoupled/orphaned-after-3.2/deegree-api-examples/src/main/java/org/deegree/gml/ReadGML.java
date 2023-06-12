/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.gml;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.AppSchema;

/**
 * Example code for reading GML documents/document fragments with deegree's GML API.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * 
 */
public class ReadGML {

    /**
     * {@link AppSchema} is derived automatically
     * 
     * @throws IOException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public static void readSimpleFeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {

        System.out.println( "Reading simple feature collection (AppSchema derived from xsi:schemaLocation)" );
        URL url = ReadGML.class.getResource( "simple_featurecollection.xml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, url );
        FeatureCollection fc = gmlReader.readFeatureCollection();
        for (Feature member : fc) {
            System.out.println( "- " + member.getName() + ", id: " + member.getId() );
        }
        
        System.out.println( "Reading simple feature collection (DynamicAppSchema derived content)" );
        url = ReadGML.class.getResource( "simple_featurecollection_no_schema.xml" );
        gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, url );
        fc = gmlReader.readFeatureCollection();
        for (Feature member : fc) {
            System.out.println( "- " + member.getName() + ", id: " + member.getId() );
        }        
    }

    public static void readInspireAddresses()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {      
    }
    
    /**
     * @param args
     */
    public static void main( String[] args )
                            throws Exception {
        readSimpleFeatureCollection();
    }
}
