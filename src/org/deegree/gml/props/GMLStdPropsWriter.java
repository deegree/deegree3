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
package org.deegree.gml.props;

import static org.deegree.commons.xml.CommonNamespaces.XLNNS;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.gml.GMLVersion;

/**
 * Stream-based writer for the {@link GMLStdProps} that can occur at the beginning of every GML encoded object.
 * 
 * TODO handle gml:metadataProperty, gml:identifier and gml:descriptionReference
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLStdPropsWriter {

    private final GMLVersion version;

    private final String gmlNs;

    private final XMLStreamWriter writer;

    /**
     * Creates a new {@link GMLStdPropsWriter} instance.
     * 
     * @param version
     *            GML version, must not be <code>null</code>
     * @param writer
     *            XML stream used to write the output, must not be <code>null</code>
     */
    public GMLStdPropsWriter( GMLVersion version, XMLStreamWriter writer ) {
        this.version = version;
        this.gmlNs = version.getNamespace();
        this.writer = writer;
    }

    /**
     * Writes a GML representation of the given {@link GMLStdProps} to the stream.
     * 
     * @param props
     *            properties to be exported, must not be <code>null</code>
     * @throws XMLStreamException
     */
    public void write( GMLStdProps props )
                            throws XMLStreamException {
        switch ( version ) {
        case GML_2:
            writeGML2( props );
            break;
        case GML_30:
        case GML_31:
            writeGML3( props );
            break;
        case GML_32:
            writeGML32( props );
            break;
        }
    }

    private void writeGML2( GMLStdProps props )
                            throws XMLStreamException {

        StringOrRef description = props.getDescription();
        if ( description != null ) {
            writer.writeStartElement( "gml", "description", gmlNs );
            if ( description.getRef() != null ) {
                writer.writeAttribute( XLNNS, "xlink", description.getRef() );
            }
            if ( description.getString() != null ) {
                writer.writeCharacters( description.getString() );
            }
            writer.writeEndElement();
        }

        for ( CodeType name : props.getNames() ) {
            writer.writeStartElement( "gml", "name", gmlNs );
            if ( name.getCodeSpace() != null ) {
                writer.writeAttribute( "codeSpace", name.getCodeSpace() );
            }
            if ( name.getCode() != null ) {
                writer.writeCharacters( name.getCode() );
            }
            writer.writeEndElement();
            // in GML 2, only one gml:name is allowed
            break;
        }
    }

    private void writeGML3( GMLStdProps props )
                            throws XMLStreamException {

        StringOrRef description = props.getDescription();
        if ( description != null ) {
            writer.writeStartElement( "gml", "description", gmlNs );
            if ( description.getRef() != null ) {
                writer.writeAttribute( XLNNS, "xlink", description.getRef() );
            }
            if ( description.getString() != null ) {
                writer.writeCharacters( description.getString() );
            }
            writer.writeEndElement();
        }

        for ( CodeType name : props.getNames() ) {
            writer.writeStartElement( "gml", "name", gmlNs );
            if ( name.getCodeSpace() != null ) {
                writer.writeAttribute( "codeSpace", name.getCodeSpace() );
            }
            if ( name.getCode() != null ) {
                writer.writeCharacters( name.getCode() );
            }
            writer.writeEndElement();
        }
    }

    private void writeGML32( GMLStdProps props )
                            throws XMLStreamException {

        StringOrRef description = props.getDescription();
        if ( description != null ) {
            writer.writeStartElement( "gml", "description", gmlNs );
            if ( description.getRef() != null ) {
                writer.writeAttribute( XLNNS, "xlink", description.getRef() );
            }
            if ( description.getString() != null ) {
                writer.writeCharacters( description.getString() );
            }
            writer.writeEndElement();
        }

        for ( CodeType name : props.getNames() ) {
            writer.writeStartElement( "gml", "name", gmlNs );
            if ( name.getCodeSpace() != null ) {
                writer.writeAttribute( "codeSpace", name.getCodeSpace() );
            }
            if ( name.getCode() != null ) {
                writer.writeCharacters( name.getCode() );
            }
            writer.writeEndElement();
        }
    }
}
