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
package org.deegree.gml.feature;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.geometry.Envelope;
import org.deegree.gml.geometry.GML311GeometryDecoder;
import org.deegree.gml.props.GMLStandardPropsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLStandardFeaturePropsParser extends GMLStandardPropsParser {

    private static final Logger LOG = LoggerFactory.getLogger( GMLStandardFeaturePropsParser.class );

    private static final GML311GeometryDecoder decoder = new GML311GeometryDecoder();

    /**
     * Returns the object representation for the <code>StandardObjectProperties</code> element group of the given
     * <code>gml:_Feature</code> element event.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_GML&gt;)</li>
     * <li>Postcondition: cursor points at the first tag event (<code>START_ELEMENT/END_ELEMENT</code>) that does not
     * belong to an element from the <code>StandardObjectProperties</code> group</li>
     * </ul>
     * <p>
     * GML 3.1.1 specifies the following standard properties for any feature type:
     * 
     * <pre>
     * &lt;sequence&gt;
     *    &lt;element ref=&quot;gml:metaDataProperty&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt;
     *    &lt;element ref=&quot;gml:description&quot; minOccurs=&quot;0&quot;/&gt;
     *    &lt;element ref=&quot;gml:name&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt;
     *    &lt;element ref=&quot;gml:boundedBy&quot; minOccurs=&quot;0&quot;/&gt;
     *    &lt;element ref=&quot;gml:location&quot; minOccurs=&quot;0&quot;/&gt;
     * &lt;/sequence&gt;
     * </pre>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Feature&gt;), points at the at the
     *            first tag event (<code>START_ELEMENT/END_ELEMENT</code>) following the standard properties
     * @return corresponding {@link StandardGMLFeatureProps} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     *             if a syntactical error occurs
     * @throws XMLStreamException
     */
    public static StandardGMLFeatureProps parse311( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        int event = xmlStream.nextTag();

        // 'gml:metaDataProperty' (0...unbounded)
        Object[] metadata = new Object[0];
        while ( event == START_ELEMENT && new QName( GMLNS, "metaDataProperty" ).equals( xmlStream.getName() ) ) {
            LOG.debug( "gml:metaDataProperty" );
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

        // 'gml:boundedBy' (0...1)
        Envelope boundedBy = null;
        if ( event == START_ELEMENT && new QName( GMLNS, "boundedBy" ).equals( xmlStream.getName() ) ) {
            boundedBy = parseBoundedBy311( xmlStream );
            xmlStream.nextTag();
        }

        // 'gml:location' (0...1)
        if ( event == START_ELEMENT && new QName( GMLNS, "location" ).equals( xmlStream.getName() ) ) {
            LOG.debug( "gml:location" );
            parseLocation311( xmlStream );
            xmlStream.nextTag();
        }
        return new StandardGMLFeatureProps( metadata, description, names.toArray( new CodeType[names.size()] ),
                                            boundedBy );
    }

    protected static Envelope parseBoundedBy311( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException, XMLParsingException {

        xmlStream.nextTag();

        if ( xmlStream.getEventType() != XMLStreamConstants.START_ELEMENT
             || !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:boundedBy element. Must contain a child element in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        Envelope envelope = null;
        if ( name.equals( "Envelope" ) ) {
            envelope = decoder.parseEnvelope( xmlStream );
        } else if ( name.equals( "Null" ) ) {
            xmlStream.skipElement();
            envelope = null;
        } else {
            String msg = "Invalid {" + GMLNS + "}boundedBy element. Must contain either a {" + GMLNS
                         + "}Envelope or a {" + GMLNS + "}Null element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "boundedBy" );
        return envelope;
    }

    protected static void parseLocation311( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        xmlStream.skipElement();
    }
}
