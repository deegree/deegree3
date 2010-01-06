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
package org.deegree.record.persistence.genericrecordstore;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.csw.CSWConstants;

/**
 * The parsing for the ISO application profile. 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ISOQPParsing extends XMLAdapter {

    private NamespaceContext nsContext = new NamespaceContext( XMLAdapter.nsContext );

    protected final static String CSW_PREFIX = "csw";

    QueryableProperties qp = new QueryableProperties();

    // private OMElement recordBrief;
    private Writer writer;

    private OMElement element;

    private OMElement elementFull;

    private OMElement recordFull;

    private OMElement identifier = null;

    private OMElement hierarchyLevel = null;

    private OMElement identificationInfo = null;

    public ISOQPParsing( OMElement element ) {
        this.element = element;
        this.elementFull = element;

        setRootElement( element );
        nsContext.addNamespace( rootElement.getDefaultNamespace().getPrefix(),
                                rootElement.getDefaultNamespace().getNamespaceURI() );
        nsContext.addNamespace( CSW_PREFIX, CSWConstants.CSW_202_NS );

        try {
            parsing( element );

            // this.writer = generateRecordFull(element, generateRecordBrief( qp, writer, id ), id);
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void parsing( OMElement element )
                            throws IOException {

        // /*/self::node() => /*/self::* => /* -> Transaction
        // */MD_Metadata -> null
        // //MD_Metadata => ./gmd:MD_Metadata -> null
        // */self::* => * => ./* => ./child::*-> fileIdentifier, identificationInfo
        // ./. => ./self::node() -> MD_Metadata
        // //. -> jedes element...alles also
        // /*/*/gmd:MD_Metadata -> MD_Metadata
        List<OMElement> recordElements = getElements( rootElement, new XPath( "*", nsContext ) );
        for ( OMElement elem : recordElements ) {

            if ( elem.getLocalName().equals( "fileIdentifier" ) ) {
                qp.setIdentifier( getNodeAsString( elem, new XPath( "./gco:CharacterString", nsContext ), null ) );

                identifier = elem;
                elementFull.addChild( identifier );

                continue;

            }
            if ( elem.getLocalName().equals( "hierarchyLevel" ) ) {

                qp.setType( getNodeAsString( elem, new XPath( "./gco:CharacterString", nsContext ), null ) );

                hierarchyLevel = elem;
                elementFull.addChild( hierarchyLevel );
                continue;
            }

            /*
             * if(elem.getLocalName().equals( "language" )){ language = elem; qp.setLanguage( language ); }
             */
            if ( elem.getLocalName().equals( "identificationInfo" ) ) {

                String[] titleElements = getNodesAsStrings(
                                                            elem,
                                                            new XPath(
                                                                       "./gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString",
                                                                       nsContext ) );

                double boundingBoxWestLongitude = getNodeAsDouble(
                                                                   elem,
                                                                   new XPath(
                                                                              "./gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                double boundingBoxEastLongitude = getNodeAsDouble(
                                                                   elem,
                                                                   new XPath(
                                                                              "./gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                double boundingBoxSouthLatitude = getNodeAsDouble(
                                                                   elem,
                                                                   new XPath(
                                                                              "./gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                double boundingBoxNorthLatitude = getNodeAsDouble(
                                                                   elem,
                                                                   new XPath(
                                                                              "./gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                qp.setBoundingBox( new BoundingBox( boundingBoxWestLongitude, boundingBoxEastLongitude,
                                                    boundingBoxSouthLatitude, boundingBoxNorthLatitude ) );

                qp.setTitle( Arrays.asList( titleElements ) );

                identificationInfo = elem;
                elementFull.addChild( identificationInfo );
                continue;

            }

        }

    }

    public Writer generateInsertStatement( Writer writer, int id )
                            throws IOException {
        Writer insertStatement;
        insertStatement = generateRecordBrief( qp, writer, id );
        insertStatement = generateRecordFull( this.elementFull, insertStatement, id );

        return insertStatement;
    }

    public Writer generateRecordBrief( QueryableProperties qp, Writer writer, int id )
                            throws IOException {
        OMElement omElement = null;

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = factory.createOMNamespace( rootElement.getDefaultNamespace().getNamespaceURI(), "gmd" );
        /*
         * if(rootElement.getDefaultNamespace().equals( qp.getIdentifier().getNamespace() )){
         * System.out.println("hier bin ich"); qp.setIdentifier( factory.createOMElement(
         * qp.getIdentifier().getLocalName(), namespace ) );
         * 
         * }
         */

        omElement = factory.createOMElement( "MD_Metadata", namespace );
        omElement.addChild( identifier );
        if ( hierarchyLevel != null ) {
            omElement.addChild( hierarchyLevel );
        }
        if ( identificationInfo != null ) {
            omElement.addChild( identificationInfo );
        }

        /*
         * id, version, status, anyText, identifier, modified, hassecurityconstraint, language, parentidentifier,
         * source, association
         */
        writer.append( "INSERT INTO datasets VALUES (" + id + ",null,null,'','" + qp.getIdentifier()
                       + "',null,FALSE,'','', '', null);" );

        writer.append( "INSERT INTO recordbrief (fk_datasets, format, data) VALUES (" + id + ", 1, '"
                       + omElement.toString() + "');" );

        return writer;
    }

    private Writer generateRecordFull( OMElement element, Writer writer, int id )
                            throws IOException {

        System.out.println( element );
        writer.append( "INSERT INTO recordfull (fk_datasets, format, data) VALUES (" + id + ", 1, '"
                       + element.toString() + "');" );

        return writer;
    }

    /**
     * @return the writer
     */
    public Writer getWriter() {
        return writer;
    }

}
