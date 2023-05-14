/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.wps.provider.jrxml.contentprovider;

import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsCodeType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsLanguageStringType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.ComplexInputImpl;
import org.deegree.services.wps.input.EmbeddedComplexInput;
import org.deegree.services.wps.input.ProcessletInput;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class Utils {

    public static ProcessletInputs getInputs( String parameterId, String mimeType, String schema,
                                              InputStream complexInput )
                            throws IOException, XMLStreamException, FactoryConfigurationError {
        List<ProcessletInput> inputs = new ArrayList<ProcessletInput>();
        ProcessletInputs in = new ProcessletInputs( inputs );

        ComplexInputDefinition definition = new ComplexInputDefinition();
        definition.setTitle( getAsLanguageStringType( parameterId ) );
        definition.setIdentifier( getAsCodeType( parameterId ) );
        ComplexFormatType format = new ComplexFormatType();

        format.setEncoding( "UTF-8" );
        format.setMimeType( mimeType );
        format.setSchema( schema );
        definition.setDefaultFormat( format );
        definition.setMaxOccurs( BigInteger.valueOf( 1 ) );
        definition.setMinOccurs( BigInteger.valueOf( 0 ) );

        File f = File.createTempFile( "tmpStore", "" );
        StreamBufferStore store = new StreamBufferStore( 1024, f );

        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( complexInput );
        XMLStreamWriter xmlWriter = null;
        try {
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( store );
            if ( xmlReader.getEventType() == START_DOCUMENT ) {
                xmlReader.nextTag();
            }
            XMLAdapter.writeElement( xmlWriter, xmlReader );
        } finally {
            try {
                xmlReader.close();
            } catch ( XMLStreamException e ) {
                // nothing to do
            }
            try {
                xmlWriter.close();
            } catch ( XMLStreamException e ) {
                // nothing to do
            }
            IOUtils.closeQuietly( store );
        }

        ComplexInputImpl mapProcesslet = new EmbeddedComplexInput( definition, new LanguageString( "title", "ger" ),
                                                                   new LanguageString( "summary", "ger" ), format,
                                                                   store );

        inputs.add( mapProcesslet );
        return in;
    }
}
