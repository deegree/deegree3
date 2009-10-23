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
package org.deegree.commons.gml;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.types.gml.StandardGMLObjectProps;
import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLStandardPropsParser {

    /**
     * Returns the object representation for the <code>StandardObjectProperties</code> element group of the given
     * <code>gml:_GML</code> element event.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_GML&gt;)</li>
     * <li>Postcondition: cursor points at the first tag event (<code>START_ELEMENT/END_ELEMENT</code>) that does not
     * belong to an element from the <code>StandardObjectProperties</code> group</li>
     * </ul>
     * <p>
     * GML 3.1.1 specifies the <code>StandardObjectProperties</code> group as follows:
     * 
     * <pre>
     * &lt;sequence&gt;
     *    &lt;element ref=&quot;gml:metaDataProperty&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt;
     *    &lt;element ref=&quot;gml:description&quot; minOccurs=&quot;0&quot;/&gt;
     *    &lt;element ref=&quot;gml:name&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt;
     * &lt;/sequence&gt;
     * </pre>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_GML&gt;), points at the at the
     *            first tag event (<code>START_ELEMENT/END_ELEMENT</code>) that does not belong to an element from the
     *            <code>StandardObjectProperties</code> group afterwards
     * @return corresponding {@link StandardObjectProperties} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     *             if a syntactical error occurs
     * @throws XMLStreamException
     */
    public static StandardGMLObjectProps parse311( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        int event = xmlStream.nextTag();

        // 'gml:metaDataProperty' (0...unbounded)
        while ( event == START_ELEMENT && new QName( GMLNS, "metaDataProperty" ).equals( xmlStream.getName() ) ) {
            parseMetadataProperty311( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:description' (0...1)
        StringOrRef description = null;
        if ( event == START_ELEMENT && new QName( GMLNS, "description" ).equals( xmlStream.getName() ) ) {
            description = parseDescription311( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:name' (0...unbounded)
        List<CodeType> names = new LinkedList<CodeType>();
        while ( event == START_ELEMENT && new QName( GMLNS, "name" ).equals( xmlStream.getName() ) ) {
            names.add( parseName311( xmlStream ) );
            xmlStream.nextTag();
        }

        return new StandardGMLObjectProps( description, names.toArray( new CodeType[names.size()] ) );
    }

    protected static void parseMetadataProperty311( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        xmlStream.skipElement();
    }

    protected static StringOrRef parseDescription311( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        String ref = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        String string = xmlStream.getElementText().trim();
        return new StringOrRef( string, ref );
    }

    protected static CodeType parseName311( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        String codeSpace = xmlStream.getAttributeValue( null, "codeSpace" );
        String code = xmlStream.getElementText().trim();
        return new CodeType( code, codeSpace );
    }
}
