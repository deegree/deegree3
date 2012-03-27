//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.protocol.csw.client.getrecords;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.xml.Filter110XMLEncoder;
import org.deegree.protocol.csw.CSWConstants;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordsXMLEncoder {

    /**
     * @param getRecords
     * @param object
     * @param xmlWriter
     * @throws XMLStreamException
     * @throws TransformationException
     * @throws UnknownCRSException
     */
    public static void export( GetRecords getRecords, XMLStreamWriter writer )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartDocument();
        writer.writeStartElement( CSWConstants.CSW_202_PREFIX, "GetRecords", CSW_202_NS );
        writer.writeNamespace( CSWConstants.CSW_202_PREFIX, CSW_202_NS );
        writer.writeNamespace( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS );
        writer.writeAttribute( CommonNamespaces.XSINS, "schemaLocation", CSW_202_NS + " "
                                                                         + CSWConstants.CSW_202_DISCOVERY_SCHEMA );
        for ( QName tn : getRecords.getTypeNames() ) {
            writeNamespaceDeclaration( tn, writer );
        }
        writer.writeAttribute( "service", "CSW" );
        writer.writeAttribute( "version", CSWConstants.VERSION_202_STRING );
        writer.writeAttribute( "outputSchema", getRecords.getOutputSchema() );
        writer.writeAttribute( "outputFormat", getRecords.getOutputFormat() );
        writer.writeAttribute( "resultType", getRecords.getResultType().toString() );

        writer.writeAttribute( "startPosition", Integer.toString( getRecords.getStartPosition() ) );
        writer.writeAttribute( "maxRecords", Integer.toString( getRecords.getMaxRecords() ) );
        writer.writeStartElement( CSWConstants.CSW_202_PREFIX, "Query", CSW_202_NS );
        String typeNames = "";
        boolean isFirst = true;
        for ( QName tn : getRecords.getTypeNames() ) {
            if ( !isFirst )
                typeNames += ',';
            if ( tn.getNamespaceURI() != null ) {
                typeNames += tn.getPrefix() + ":";
            }
            typeNames += tn.getLocalPart();
            isFirst = false;
        }
        writer.writeAttribute( "typeNames", typeNames );
        writer.writeStartElement( CSWConstants.CSW_202_PREFIX, "ElementSetName", CSW_202_NS );
        writer.writeCharacters( getRecords.getElementSetName().toString() );
        writer.writeEndElement();
        
        if ( getRecords.getConstraint() != null ) {
            writer.writeStartElement( CSWConstants.CSW_202_PREFIX, "Constraint", CSW_202_NS );
            writer.writeAttribute( "version", "1.1.0" );
            Filter110XMLEncoder.export( getRecords.getConstraint(), writer );
            writer.writeEndElement();    
        }
    }

    private static void writeNamespaceDeclaration( QName qname, XMLStreamWriter writer )
                            throws XMLStreamException {
        if ( qname != null && qname.getNamespaceURI() != null ) {
            boolean prefixBound = writer.getPrefix( qname.getNamespaceURI() ) != null;
            if ( !prefixBound ) {
                writer.writeNamespace( qname.getPrefix(), qname.getNamespaceURI() );
            }
        }
    }

}
