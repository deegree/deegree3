//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.gml;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.CRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.OutsideCRSDomainException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;

/**
 * The <code>XMLTransformer</code> transforms any xml documents containing gml geometries. Only the geometries will be
 * transformed all other data (including comments and cdata) will be copied.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLTransformer extends GeometryTransformer {

    /**
     * @param targetCRS
     * @throws IllegalArgumentException
     */
    public XMLTransformer( CoordinateSystem targetCRS ) throws IllegalArgumentException {
        super( targetCRS );
    }

    /**
     * @param targetCRS
     * @throws IllegalArgumentException
     * @throws UnknownCRSException
     */
    public XMLTransformer( String targetCRS ) throws IllegalArgumentException, UnknownCRSException {
        super( targetCRS );
    }

    /**
     * @param transformation
     * @throws IllegalArgumentException
     */
    public XMLTransformer( Transformation transformation ) throws IllegalArgumentException {
        super( transformation );
    }

    /**
     * Transforms the given input stream, and streams the input into the output directly. If a geometry is found, the
     * geometry is transformed into the target crs. All other events are just copied.
     * 
     * @param reader
     *            an XMLStream containing some GML Geometries.
     * @param writer
     *            the output will be written to this writer, the writer have been opened (
     *            {@link XMLStreamWriter#writeStartDocument()}. No {@link XMLStreamWriter#writeEndDocument()} will be
     *            written as well.
     * @param sourceCRS
     *            to be used if the geometries do not define a srsName (or the like) attribute.
     * @param gmlVersion
     *            the version of the expected geometries.
     * @param testValidArea
     *            true if the incoming geometries should be checked against the valid domain of the crs they are defined
     *            in.
     * @param requestedTransformation
     *            can be <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws IllegalArgumentException
     * @throws OutsideCRSDomainException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void transform( XMLStreamReader reader, XMLStreamWriter writer, CoordinateSystem sourceCRS,
                           GMLVersion gmlVersion, boolean testValidArea, List<Transformation> requestedTransformation )
                            throws XMLStreamException, XMLParsingException, IllegalArgumentException,
                            OutsideCRSDomainException, UnknownCRSException, TransformationException {

        if ( reader == null ) {
            throw new NullPointerException( "The input stream may not be null" );
        }
        if ( writer == null ) {
            throw new NullPointerException( "The output stream may not be null" );
        }

        GMLStreamReader gmlReader = createGMLStreamReader( gmlVersion, reader );
        GMLStreamWriter gmlWriter = createGMLStreamWriter( gmlVersion, writer );
        transformStream( gmlReader, gmlWriter, sourceCRS, testValidArea, requestedTransformation );
    }

    /**
     * Transforms the given input stream, and streams the input into the output directly. If a geometry is found, the
     * geometry is transformed into the target crs. All other events are just copied.
     * 
     * @param reader
     *            an XMLStream containing some GML Geometries.
     * @param writer
     *            the output will be written to this writer, the writer have been opened (
     *            {@link XMLStreamWriter#writeStartDocument()}. No {@link XMLStreamWriter#writeEndDocument()} will be
     *            written as well.
     * @param gmlVersion
     *            the version of the expected geometries.
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws IllegalArgumentException
     * @throws OutsideCRSDomainException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void transform( XMLStreamReader reader, XMLStreamWriter writer, GMLVersion gmlVersion )
                            throws XMLStreamException, XMLParsingException, IllegalArgumentException,
                            OutsideCRSDomainException, UnknownCRSException, TransformationException {
        transform( reader, writer, null, gmlVersion, false, null );
    }

    private void transformStream( GMLStreamReader gmlReader, GMLStreamWriter gmlWriter, CoordinateSystem sourceCRS,
                                  boolean testValidArea, List<Transformation> toBeUsedTransformations )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            IllegalArgumentException, TransformationException, OutsideCRSDomainException {
        XMLStreamReader input = gmlReader.getXMLReader();
        int eventType = input.getEventType();
        if ( eventType == XMLStreamConstants.START_DOCUMENT ) {
            StAXParsingHelper.nextElement( input );
        }
        eventType = input.getEventType();
        if ( input.getEventType() != XMLStreamConstants.START_ELEMENT ) {
            throw new XMLStreamException( "Input stream does not point to a START_ELEMENT event." );
        }
        XMLStreamWriter output = gmlWriter.getXMLStream();
        int openElements = 0;
        boolean firstRun = true;
        while ( firstRun || openElements > 0 ) {
            firstRun = false;
            eventType = input.getEventType();
            switch ( eventType ) {
            case COMMENT:
                output.writeComment( input.getText() );
                break;
            case CDATA: {
                output.writeCData( input.getText() );
                break;
            }
            case CHARACTERS: {
                output.writeCharacters( input.getTextCharacters(), input.getTextStart(), input.getTextLength() );
                break;
            }
            case END_ELEMENT: {
                output.writeEndElement();
                openElements--;
                break;
            }
            case START_ELEMENT: {
                QName name = input.getName();
                if ( gmlReader.isGeometryOrEnvelopeElement() ) {
                    Geometry geom = gmlReader.readGeometryOrEnvelope();
                    if ( geom != null ) {
                        CoordinateSystem geomCRS = sourceCRS;
                        if ( geomCRS == null ) {
                            CRS gCRS = geom.getCoordinateSystem();
                            if ( gCRS != null ) {
                                geomCRS = gCRS.getWrappedCRS();
                            } else {
                                throw new TransformationException(
                                                                   "Could not determine Coordinate System of geometry: "
                                                                                           + geom );
                            }
                        }
                        geom = super.transform( geom, geomCRS, testValidArea, toBeUsedTransformations );
                        // write transformed geometry
                        gmlWriter.write( geom );
                    }
                } else {
                    output.writeStartElement( name.getPrefix() == null ? "" : name.getPrefix(), input.getLocalName(),
                                              input.getNamespaceURI() );

                    // copy all namespace bindings
                    for ( int i = 0; i < input.getNamespaceCount(); i++ ) {
                        String nsPrefix = input.getNamespacePrefix( i );
                        String nsURI = input.getNamespaceURI( i );
                        output.writeNamespace( nsPrefix, nsURI );
                    }

                    // copy all attributes
                    for ( int i = 0; i < input.getAttributeCount(); i++ ) {
                        String localName = input.getAttributeLocalName( i );
                        String nsPrefix = input.getAttributePrefix( i );
                        String value = input.getAttributeValue( i );
                        String nsURI = input.getAttributeNamespace( i );
                        if ( nsURI == null ) {
                            output.writeAttribute( localName, value );
                        } else {
                            output.writeAttribute( nsPrefix, nsURI, localName, value );
                        }
                    }
                    openElements++;
                    break;
                }
            }
            default: {
                break;
            }
            }
            if ( openElements > 0 ) {
                input.next();
            }
        }
    }
}
