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
package org.deegree.gml.dictionary;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.props.GMLStdPropsWriter;

/**
 * Stream-based writer for GML dictionaries and definitions.
 * 
 * @see GMLStreamWriter
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLDictionaryWriter {

    private final GMLVersion version;

    private final XMLStreamWriter writer;

    private final GMLStdPropsWriter stdPropsWriter;

    private final String gmlNs;

    /**
     * Creates a new {link {@link GMLDictionaryWriter} instance.
     * 
     * @param version
     *            GML version of the output, must not be <code>null</code>
     * @param writer
     *            XML stream used to write the output, must not be <code>null</code>
     */
    public GMLDictionaryWriter( GMLVersion version, XMLStreamWriter writer ) {
        this.version = version;
        this.gmlNs = version.getNamespace();
        this.writer = writer;
        this.stdPropsWriter = new GMLStdPropsWriter( version, writer );
    }

    public void write( Definition def )
                            throws XMLStreamException {
        if ( def instanceof Dictionary ) {
            write( (Dictionary) def );
        } else {
            writer.writeStartElement( "gml", "Definition", gmlNs );
            if ( def.getId() != null ) {
                writer.writeAttribute( "gml", gmlNs, "id", def.getId() );
            }
            stdPropsWriter.write( def.getGMLProperties() );
            writer.writeEndElement();
        }
    }

    public void write( Dictionary dict )
                            throws XMLStreamException {
        if ( dict.isDefinitionCollection() ) {
            writer.writeStartElement( "gml", "DefinitionCollection", gmlNs );
        } else {
            writer.writeStartElement( "gml", "Dictionary", gmlNs );
        }
        if ( dict.getId() != null ) {
            writer.writeAttribute( "gml", gmlNs, "id", dict.getId() );
        }
        stdPropsWriter.write( dict.getGMLProperties() );
        for ( Definition def : dict ) {
            if ( dict.isDefinitionCollection() ) {
                writer.writeStartElement( "gml", "definitionMember", gmlNs );
            } else {
                writer.writeStartElement( "gml", "dictionaryEntry", gmlNs );
            }
            write( def );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }
}
