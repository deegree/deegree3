//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.protocol.wfs.storedquery.xml;

import static org.deegree.commons.xml.CommonNamespaces.XMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XMLNS_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.protocol.wfs.storedquery.Parameter;
import org.deegree.protocol.wfs.storedquery.QueryExpressionText;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;

/**
 * Encodes {@link StoredQueryDefinition} objects according to the WFS 2.0.0 Specification.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StoredQueryDefinition200Encoder {

    private static final String WFS_200_PREFIX = "wfs";

    /**
     * 
     * Serializes the given {@link StoredQueryDefinition} object to XML.
     * 
     * @param storedQueryDefinition
     *            <code>StoredQueryDefinition</code> object to be serialized, never <code>null</code>
     * @param writer
     *            to export the StoredQueryDefinition in, never <code>null</code>
     * @throws XMLStreamException
     */
    public static void export( StoredQueryDefinition storedQueryDefinition, XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.setPrefix( WFS_200_PREFIX, WFS_200_NS );
        writer.writeStartElement( WFS_200_NS, "StoredQueryDefinition" );
        writer.writeNamespace( WFS_200_PREFIX, WFS_200_NS );
        writer.writeNamespace( XSI_PREFIX, XSINS );
        String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
        writer.writeAttribute( XSINS, "schemaLocation", schemaLocation );

        writer.writeAttribute( "id", storedQueryDefinition.getId() );
        writeTitles( storedQueryDefinition.getTitles(), writer );
        writeAbstracts( storedQueryDefinition.getAbstracts(), writer );
        exportMetadata( storedQueryDefinition.getMetadata(), writer );
        exportParameter( storedQueryDefinition, writer );
        exportQueryExpression( storedQueryDefinition, writer );
        writer.writeEndElement();
    }

    private static void exportParameter( StoredQueryDefinition storedQueryDefinition, XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( Parameter parameter : storedQueryDefinition.getParameters() ) {
            writer.writeStartElement( WFS_200_NS, "Parameter" );
            QName type = parameter.getType();
            writer.writeAttribute( "name", parameter.getName() );
            if ( ( type.getNamespaceURI() != null ) && ( !type.getNamespaceURI().equals( "" ) ) ) {
                writer.writeNamespace( type.getPrefix(), type.getNamespaceURI() );
                writer.writeAttribute( "type", type.getPrefix() + ":" + type.getLocalPart() );
            } else {
                writer.writeAttribute( "type", type.getLocalPart() );
            }
            writeTitles( parameter.getTitles(), writer );
            writeAbstracts( parameter.getAbstracts(), writer );
            exportMetadata( parameter.getMetadata(), writer );
            writer.writeEndElement();
        }
    }

    private static void exportQueryExpression( StoredQueryDefinition storedQueryDefinition, XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( QueryExpressionText queryExpressionText : storedQueryDefinition.getQueryExpressionTextEls() ) {
            writer.writeStartElement( WFS_200_NS, "QueryExpressionText" );
            writeReturnFeatureTypes( queryExpressionText, writer );
            writer.writeAttribute( "language", queryExpressionText.getLanguage() );
            writer.writeAttribute( "isPrivate", Boolean.toString( queryExpressionText.isPrivate() ) );
            for ( OMElement childEl : queryExpressionText.getChildEls() ) {
                childEl.serialize( writer );
            }
            writer.writeEndElement();
        }
    }

    private static void writeTitles( List<LanguageString> titles, XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( LanguageString title : titles ) {
            writer.writeStartElement( WFS_200_NS, "Title" );
            writer.writeCharacters( title.getString() );
            if ( title.getLanguage() != null )
                writer.writeAttribute( XMLNS_PREFIX, XMLNS, "lang", title.getLanguage() );
            writer.writeEndElement();
        }
    }

    private static void writeAbstracts( List<LanguageString> abstracts, XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( LanguageString abst : abstracts ) {
            writer.writeStartElement( WFS_200_NS, "Abstract" );
            writer.writeCharacters( abst.getString() );
            if ( abst.getLanguage() != null )
                writer.writeAttribute( XMLNS_PREFIX, XMLNS, "lang", abst.getLanguage() );
            writer.writeEndElement();
        }
    }

    private static void exportMetadata( List<OMElement> metadataEls, XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( OMElement metadataEl : metadataEls ) {
            metadataEl.serialize( writer );
        }
    }

    private static void writeReturnFeatureTypes( QueryExpressionText queryExpressionText, XMLStreamWriter writer )
                            throws XMLStreamException {
        StringBuilder returnFeatureTypes = new StringBuilder();
        boolean isFirst = true;
        for ( QName returnFeatureType : queryExpressionText.getReturnFeatureTypes() ) {
            if ( !isFirst )
                returnFeatureTypes.append( ' ' );
            boolean prefixBound = ( writer.getPrefix( returnFeatureType.getNamespaceURI() ) != null ) ? true : false;
            if ( prefixBound ) {
                returnFeatureTypes.append( writer.getPrefix( returnFeatureType.getNamespaceURI() ) );
            } else {
                writer.writeNamespace( returnFeatureType.getPrefix(), returnFeatureType.getNamespaceURI() );
                returnFeatureTypes.append( returnFeatureType.getPrefix() );
            }
            returnFeatureTypes.append( ':' ).append( returnFeatureType.getLocalPart() );
            isFirst = false;
        }
        writer.writeAttribute( "returnFeatureTypes", returnFeatureTypes.toString() );
    }

}