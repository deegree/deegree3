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
package org.deegree.gml.props;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.gml.GMLVersion;

/**
 * Parser for the {@link StandardGMLObjectProps} that can occur at the beginning of every GML object.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLStandardPropsParser {

    protected GMLVersion version;

    /**
     * Creates a new {@link GMLStandardPropsParser} for the specified GML version.
     * 
     * @param version
     *            GML version, must not be <code>null</code>
     */
    public GMLStandardPropsParser( GMLVersion version ) {
        this.version = version;
    }

    /**
     * Returns the object representation for the <code>StandardObjectProperties</code> element group of the given
     * <code>gml:_GML</code> element event.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_GML&gt;)</li>
     * <li>Postcondition: cursor points at the first tag event (<code>START_ELEMENT/END_ELEMENT</code>) that does not
     * belong to an element from the <code>StandardObjectProperties</code> group</li>
     * </ul>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_GML&gt;), points at the at the
     *            first tag event (<code>START_ELEMENT/END_ELEMENT</code>) that does not belong to an element from the
     *            <code>StandardObjectProperties</code> group afterwards
     * @return corresponding {@link StandardGMLObjectProps} object, never <code>null</code>
     * @throws XMLStreamException
     */
    public StandardGMLObjectProps parse( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        StandardGMLObjectProps props = null;
        switch ( version ) {
        case GML_2:
            props = parse2( xmlStream );
            break;
        case GML_30:
        case GML_31:
            props = parse31( xmlStream );
            break;
        case GML_32:
            props = parse32( xmlStream );
            break;
        }
        return props;
    }

    private StandardGMLObjectProps parse2( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        int event = xmlStream.nextTag();

        // 'gml:metaDataProperty' (0...unbounded)
        Object[] metadata = null;
        while ( event == START_ELEMENT && new QName( GMLNS, "metaDataProperty" ).equals( xmlStream.getName() ) ) {
            parseMetadataProperty( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:description' (0...1)
        StringOrRef description = null;
        if ( event == START_ELEMENT && new QName( GMLNS, "description" ).equals( xmlStream.getName() ) ) {
            description = parseDescription( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:name' (0...1)
        List<CodeType> names = new LinkedList<CodeType>();
        while ( event == START_ELEMENT && new QName( GMLNS, "name" ).equals( xmlStream.getName() ) ) {
            names.add( parseName( xmlStream ) );
            xmlStream.nextTag();
        }

        return new StandardGMLObjectProps( metadata, description, null, names.toArray( new CodeType[names.size()] ) );
    }

    private StandardGMLObjectProps parse31( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        int event = xmlStream.nextTag();

        // 'gml:metaDataProperty' (0...unbounded)
        Object[] metadata = null;
        while ( event == START_ELEMENT && new QName( GMLNS, "metaDataProperty" ).equals( xmlStream.getName() ) ) {
            parseMetadataProperty( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:description' (0...1)
        StringOrRef description = null;
        if ( event == START_ELEMENT && new QName( GMLNS, "description" ).equals( xmlStream.getName() ) ) {
            description = parseDescription( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:name' (0...unbounded)
        List<CodeType> names = new LinkedList<CodeType>();
        while ( event == START_ELEMENT && new QName( GMLNS, "name" ).equals( xmlStream.getName() ) ) {
            names.add( parseName( xmlStream ) );
            xmlStream.nextTag();
        }

        return new StandardGMLObjectProps( metadata, description, null, names.toArray( new CodeType[names.size()] ) );
    }

    private StandardGMLObjectProps parse32( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        int event = xmlStream.nextTag();

        // 'gml:metaDataProperty' (0...unbounded)
        Object[] metadata = null;
        while ( event == START_ELEMENT && new QName( GML3_2_NS, "metaDataProperty" ).equals( xmlStream.getName() ) ) {
            parseMetadataProperty( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:description' (0...1)
        StringOrRef description = null;
        if ( event == START_ELEMENT && new QName( GML3_2_NS, "description" ).equals( xmlStream.getName() ) ) {
            description = parseDescription( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:identifier' (0...1)
        CodeType identifier = null;
        while ( event == START_ELEMENT && new QName( GML3_2_NS, "identifier" ).equals( xmlStream.getName() ) ) {
            identifier = parseIdentifier( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:name' (0...unbounded)
        List<CodeType> names = new LinkedList<CodeType>();
        while ( event == START_ELEMENT && new QName( GML3_2_NS, "name" ).equals( xmlStream.getName() ) ) {
            names.add( parseName( xmlStream ) );
            xmlStream.nextTag();
        }

        return new StandardGMLObjectProps( metadata, description, identifier,
                                           names.toArray( new CodeType[names.size()] ) );
    }

    private Object parseMetadataProperty( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        xmlStream.skipElement();
        // TODO
        return null;
    }

    private StringOrRef parseDescription( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        String ref = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        String string = xmlStream.getElementText().trim();
        return new StringOrRef( string, ref );
    }

    private CodeType parseIdentifier( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        String codeSpace = xmlStream.getAttributeValue( null, "codeSpace" );
        String code = xmlStream.getElementText().trim();
        if ( codeSpace == null ) {
            throw new XMLStreamException( "The gml:identifier property must have a codeSpace attribute." );
        }
        return new CodeType( code, codeSpace );
    }

    private CodeType parseName( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        String codeSpace = xmlStream.getAttributeValue( null, "codeSpace" );
        String code = xmlStream.getElementText().trim();
        return new CodeType( code, codeSpace );
    }
}
