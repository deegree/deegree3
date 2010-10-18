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
package org.deegree.services.sos.getobservation;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.observation.model.Measurement;
import org.deegree.observation.model.MeasurementCollection;
import org.deegree.observation.model.Property;

/**
 * This is an xml adapter for DataArray elements after the SWE 1.0.1 spec.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class DataArray101XMLAdapter extends XMLAdapter {

    private final static String SWE_NS = "http://www.opengis.net/swe/1.0.1";

    private static final String tokenSeparator = ",";

    private static final String decimalSeparator = ".";

    private static final String blockSeparator = "@@";

    /**
     * Export a MeasurementCollection as swe:DataArray.
     * 
     * @param writer
     * @param collection
     * 
     * @throws XMLStreamException
     */
    public static void export( XMLStreamWriter writer, MeasurementCollection collection )
                            throws XMLStreamException {
        writer.setPrefix( "swe", SWE_NS );
        writer.writeStartElement( SWE_NS, "DataArray" );

        exportCount( writer, collection.size() );
        exportElementType( writer, collection );
        exportEncoding( writer );
        exportValues( writer, collection );

        writer.writeEndElement();
    }

    private static void exportElementType( XMLStreamWriter writer, MeasurementCollection collection )
                            throws XMLStreamException {
        writer.writeStartElement( SWE_NS, "elementType" );
        writer.writeAttribute( "name", "Components" );
        writer.writeStartElement( SWE_NS, "SimpleDataRecord" );
        writer.writeStartElement( SWE_NS, "field" );
        writer.writeAttribute( "name", "timestamp" );
        writer.writeStartElement( SWE_NS, "Time" );
        writer.writeAttribute( "definition", "urn:ogc:property:time:iso8601" );
        writer.writeEndElement();
        writer.writeEndElement();

        for ( Property p : collection.getProperties() ) {
            exportQuantityField( writer, p.getColumnName(), p.getHref(), p.getOptionValue( "uom" ) );
        }

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportQuantityField( XMLStreamWriter writer, String shortName, String name, String uom )
                            throws XMLStreamException {
        writer.writeStartElement( SWE_NS, "field" );
        writer.writeAttribute( "name", shortName );
        writer.writeStartElement( SWE_NS, "Quantity" );
        writer.writeAttribute( "definition", name );
        writer.writeStartElement( SWE_NS, "uom" );
        writer.writeAttribute( "code", uom );
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportCount( XMLStreamWriter writer, int count )
                            throws XMLStreamException {
        writer.writeStartElement( SWE_NS, "elementCount" );
        writer.writeStartElement( SWE_NS, "Count" );
        writer.writeStartElement( SWE_NS, "value" );
        writer.writeCharacters( Integer.toString( count ) );
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportEncoding( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( SWE_NS, "encoding" );
        writer.writeStartElement( SWE_NS, "TextBlock" );
        writer.writeAttribute( "tokenSeparator", tokenSeparator );
        writer.writeAttribute( "decimalSeparator", decimalSeparator );
        writer.writeAttribute( "blockSeparator", blockSeparator );
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportValues( XMLStreamWriter writer, MeasurementCollection collection )
                            throws XMLStreamException {
        writer.writeStartElement( SWE_NS, "values" );
        for ( Measurement m : collection ) {
            exportValue( writer, m, collection.getProperties() );
            writer.writeCharacters( blockSeparator );
        }
        writer.writeEndElement();
    }

    private static void exportValue( XMLStreamWriter writer, Measurement m, List<Property> properties )
                            throws XMLStreamException {
        writer.writeCharacters( DateUtils.formatISO8601Date( m.getSamplingTime().getTime() ) );
        for ( Property property : properties ) {
            writer.writeCharacters( tokenSeparator );
            writer.writeCharacters( m.getResult( property ).getResultAsString() );
        }
        writer.writeCharacters( "\n" );
    }
}
