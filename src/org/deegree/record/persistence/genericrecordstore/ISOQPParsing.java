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

import static org.deegree.record.persistence.MappingInfo.ColumnType.STRING;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.record.persistence.MappingInfo;

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

    private int id;

    private Connection connection;

    private OMElement element;

    private OMElement elementFull;

    private OMElement recordFull;

    private OMElement identifier = null;

    private OMElement hierarchyLevel = null;

    private OMElement identificationInfo = null;

    private OMElement referenceSystemInfo = null;

    private Statement stm;

    public ISOQPParsing( OMElement element, Connection connection ) {
        this.element = element;
        this.elementFull = element;
        this.connection = connection;

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
                String type = getNodeAsString( elem, new XPath( "./gmd:MD_ScopeCode/@codeListValue", nsContext ),
                                               "Datasets" );

                qp.setType( type );

                hierarchyLevel = elem;
                elementFull.addChild( hierarchyLevel );
                continue;
            }

            if ( elem.getLocalName().equals( "referenceSystemInfo" ) ) {
                OMElement e = getElement(
                                          elem,
                                          new XPath(
                                                     "./gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier",
                                                     nsContext ) );
                String crsIdentification = getNodeAsString( e,
                                                            new XPath( "./gmd:code/gco:CharacterString", nsContext ),
                                                            "" );

                String crsAuthority = getNodeAsString( e,
                                                       new XPath( "./gmd:codeSpace/gco:CharacterString", nsContext ),
                                                       "" );

                String crsVersion = getNodeAsString( e, new XPath( "./gmd:version/gco:CharacterString", nsContext ), "" );
                
                CRS crs = new CRS(crsAuthority, crsIdentification, crsVersion);
                qp.setCrs( crs );
                referenceSystemInfo = elem;
                elementFull.addChild( referenceSystemInfo );
                
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

    public void executeInsertStatement()
                            throws IOException {

        generateMainDatabaseDataset( qp );
        generateRecordBrief( qp, id );
        generateRecordFull( this.elementFull, id );
        if ( qp.getTitle() != null ) {
            generateISOQP_titleStatement( qp.getTitle(), id );
        }
        if ( qp.getType() != null ) {
            generateISOQP_typeStatement( qp.getType(), id );
        }
        if ( qp.getBoundingBox() != null ) {
            generateISOQP_boundingBoxStatement( qp.getBoundingBox(), id );
        }

    }

    private void generateMainDatabaseDataset( QueryableProperties qp ) {
        final String databaseTable = "datasets";
        String sqlStatement = "";

        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO userdefinedqueryableproperties VALUES (" + id + ");";
            sqlStatement += "INSERT INTO "
                            + databaseTable
                            + " (id, version, status, anyText, identifier, modified, hassecurityconstraints, language, parentidentifier, source, association) VALUES ("
                            + id + ",null,null,'','" + qp.getIdentifier() + "',null,FALSE,'','','', null);";
            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void generateRecordBrief( QueryableProperties qp, int fk_datasets )
                            throws IOException {
        OMElement omElement = null;
        final String databaseTable = "recordbrief";

        String sqlStatement = "";

        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            id++;

            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace namespace = factory.createOMNamespace( rootElement.getDefaultNamespace().getNamespaceURI(),
                                                               "gmd" );

            omElement = factory.createOMElement( "MD_Metadata", namespace );
            omElement.addChild( identifier );
            if ( hierarchyLevel != null ) {
                omElement.addChild( hierarchyLevel );
            }
            if ( identificationInfo != null ) {
                omElement.addChild( identificationInfo );
            }
            // -------------------
            Writer writer = new StringWriter();
            try {
                omElement.serialize( writer );

            } catch ( XMLStreamException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // -------------------
            sqlStatement = "INSERT INTO recordbrief (id, fk_datasets, format, data) VALUES (" + id + "," + fk_datasets
                           + ", 2, '" + omElement.toString() + "');";

            stm.executeUpdate( sqlStatement );
            stm.close();
            id++;
            generateDCBrief( qp, id, fk_datasets, databaseTable );
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void generateDCBrief( QueryableProperties qp, int id, int fk_datasets, String databaseTable ) {
        OMElement omElement = null;
        String sqlStatement = "";

        // if this method is used standalone
        int idTemp = 0;
        try {
            stm = connection.createStatement();
            idTemp = getLastDataset( connection, databaseTable );

            if ( idTemp == id ) {
                id++;
            }
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace namespace = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );
            OMNamespace namespaceDC = factory.createOMNamespace( "http://purl.org/dc/elements/1.1/", "dc" );
            OMNamespace namespaceOWS = factory.createOMNamespace( "http://www.opengis.net/ows", "ows" );

            // TODO think about the right corners
            omElement = factory.createOMElement( "BriefRecord", namespace );
            OMElement omIdentifier = factory.createOMElement( "identifier", namespaceDC );
            OMElement omType = factory.createOMElement( "type", namespaceDC );
            OMElement omBoundingBox = factory.createOMElement( "BoundingBox", namespaceOWS );
            OMElement omLowerCorner = factory.createOMElement( "LowerCorner", namespaceOWS );
            OMElement omUpperCorner = factory.createOMElement( "UpperCorner", namespaceOWS );

            omIdentifier.setText( qp.getIdentifier() );

            omElement.addChild( omIdentifier );

            for ( String title : qp.getTitle() ) {
                OMElement omTitle = factory.createOMElement( "title", namespaceDC );
                omTitle.setText( title );
                omElement.addChild( omTitle );
            }
            if ( qp.getType() != null ) {
                omType.setText( qp.getType() );
            } else {
                omType.setText( "" );
            }
            omElement.addChild( omType );

            omLowerCorner.setText( qp.getBoundingBox().getEastBoundLongitude() + " "
                                   + qp.getBoundingBox().getSouthBoundLatitude() );
            omUpperCorner.setText( qp.getBoundingBox().getWestBoundLongitude() + " "
                                   + qp.getBoundingBox().getNorthBoundLatitude() );
            omBoundingBox.addChild( omLowerCorner );
            omBoundingBox.addChild( omUpperCorner );
            if ( qp.getCrs() != null ) {
                omBoundingBox.addAttribute( "crs", qp.getCrs().toString(), namespaceOWS );
            }

            omElement.addChild( omBoundingBox );

            sqlStatement = "INSERT INTO recordbrief (id, fk_datasets, format, data) VALUES (" + id + "," + fk_datasets
                           + ", 1, '" + omElement.toString() + "');";

            stm.executeUpdate( sqlStatement );
            stm.close();

        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void generateRecordFull( OMElement element, int fk_datasets )
                            throws IOException {
        final String databaseTable = "recordfull";
        String sqlStatement = "";
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO recordfull (id, fk_datasets, format, data) VALUES (" + id + "," + fk_datasets
                           + ", 2, '" + element.toString() + "');";

            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void generateISOQP_titleStatement( List<String> titles, int mainDatabaseTableID ) {
        final String databaseTable = "isoqp_title";
        String sqlStatement = "";
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            for ( String title : titles ) {
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, title) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + title + "');";
            }

            stm.executeUpdate( sqlStatement );
            stm.close();

        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void generateISOQP_typeStatement( String type, int mainDatabaseTableID ) {
        final String databaseTable = "isoqp_type";
        String sqlStatement = "";
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, type) VALUES (" + id + ","
                           + mainDatabaseTableID + ",'" + type + "');";

            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void generateISOQP_boundingBoxStatement( BoundingBox bbox, int mainDatabaseTableID ) {
        final String databaseTable = "isoqp_boundingbox";
        String sqlStatement = "";
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, bbox) VALUES (" + id + ","
                           + mainDatabaseTableID + ",SetSRID('BOX3D(" + bbox.getEastBoundLongitude() + " "
                           + bbox.getNorthBoundLatitude() + "," + bbox.getWestBoundLongitude() + " "
                           + bbox.getSouthBoundLatitude() + ")'::box3d,4326));";

            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private int getLastDataset( Connection conn, String databaseTable )
                            throws SQLException {
        int result = 0;
        String selectIDRows = "SELECT COUNT(*) from " + databaseTable;
        ResultSet rsBrief = conn.createStatement().executeQuery( selectIDRows );

        while ( rsBrief.next() ) {

            result = rsBrief.getInt( 1 );

        }
        rsBrief.close();
        return result;

    }

}
