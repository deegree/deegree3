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
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.types.datetime.Date;
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

    ReturnableProperties rp = new ReturnableProperties();

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

    private List<Integer> recordInsertIDs = new ArrayList<Integer>();

    public ISOQPParsing( OMElement element, Connection connection ) {
        this.element = element;
        this.elementFull = element;
        this.connection = connection;

        setRootElement( element );
        nsContext.addNamespace( rootElement.getDefaultNamespace().getPrefix(),
                                rootElement.getDefaultNamespace().getNamespaceURI() );
        nsContext.addNamespace( CSW_PREFIX, CSWConstants.CSW_202_NS );

        try {
            parseAPISO( element );

        } catch ( IOException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Parses the recordelement that should be inserted into the backend. Every elementknot is put into an OMElement and
     * its atomic representation:
     * <p>
     * e.g. the "fileIdentifier" is put into an OMElement identifier and its identification-String is put into the
     * {@link QueryableProperties}.
     * 
     * @param element
     *            the record that should be inserted into the backend
     * @throws IOException
     */
    private void parseAPISO( OMElement element )
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

            if ( elem.getLocalName().equals( "dateStamp" ) ) {
                String[] dateStrings = getNodesAsStrings( elem, new XPath( "./gco:Date", nsContext ) );
                Date[] dates = new Date[dateStrings.length];
                Date date = null;
                for ( int i = 0; i < dateStrings.length; i++ ) {
                    try {
                        date = new Date( dateStrings[i] );
                    } catch ( ParseException e ) {

                        e.printStackTrace();
                    }
                    dates[i] = date;

                }

                qp.setModified( Arrays.asList( dates ) );

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
                                                            null );

                String crsAuthority = getNodeAsString( e,
                                                       new XPath( "./gmd:codeSpace/gco:CharacterString", nsContext ),
                                                       null );

                String crsVersion = getNodeAsString( e, new XPath( "./gmd:version/gco:CharacterString", nsContext ),
                                                     null );

                CRS crs = new CRS( crsAuthority, crsIdentification, crsVersion );
                qp.setCrs( crs );
                referenceSystemInfo = elem;
                elementFull.addChild( referenceSystemInfo );

                continue;

            }

            /*
             * if(elem.getLocalName().equals( "language" )){ language = elem; qp.setLanguage( language ); }
             */
            if ( elem.getLocalName().equals( "identificationInfo" ) ) {

                OMElement md_identification = getElement( elem, new XPath( "./gmd:MD_Identification", nsContext ) );

                OMElement _abstract = getElement( elem, new XPath( "./gmd:abstract", nsContext ) );

                OMElement bbox = getElement(
                                             elem,
                                             new XPath(
                                                        "./gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox",
                                                        nsContext ) );

                List<OMElement> descriptiveKeywords = getElements(
                                                                   elem,
                                                                   new XPath(
                                                                              "./gmd:MD_DataIdentification/gmd:descriptiveKeywords",
                                                                              nsContext ) );

                List<OMElement> topicCategories = getElements(
                                                               elem,
                                                               new XPath(
                                                                          "./gmd:MD_DataIdentification/gmd:topicCategory",
                                                                          nsContext ) );

                String graphicOverview = getNodeAsString(
                                                          elem,
                                                          new XPath(
                                                                     "./gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic",
                                                                     nsContext ), null );

                String[] titleElements = getNodesAsStrings(
                                                            elem,
                                                            new XPath(
                                                                       "./gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString",
                                                                       nsContext ) );

                double boundingBoxWestLongitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:westBoundLongitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                double boundingBoxEastLongitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:eastBoundLongitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                double boundingBoxSouthLatitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:southBoundLatitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                double boundingBoxNorthLatitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:northBoundLatitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                qp.setBoundingBox( new BoundingBox( boundingBoxWestLongitude, boundingBoxEastLongitude,
                                                    boundingBoxSouthLatitude, boundingBoxNorthLatitude ) );

                qp.setTitle( Arrays.asList( titleElements ) );

                // not necessary actually...
                rp.setGraphicOverview( graphicOverview );
                // TODO same with serviceType and serviceTypeVersion
                Keyword keywordClass = new Keyword();
                ;
                List<Keyword> listOfKeywords = new ArrayList<Keyword>();
                for ( OMElement md_keywords : descriptiveKeywords ) {
                    // keywordClass =
                    String keywordType = getNodeAsString(
                                                          md_keywords,
                                                          new XPath(
                                                                     "./gmd:MD_Keywords/gmd:type/MD_KeywordTypeCode/@codeListValue",
                                                                     nsContext ), null );

                    String keyword = getNodeAsString( md_keywords,
                                                      new XPath( "./gmd:MD_Keywords/gmd:keyword/gco:CharacterString",
                                                                 nsContext ), null );

                    String thesaurus = getNodeAsString(
                                                        md_keywords,
                                                        new XPath(
                                                                   "./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString",
                                                                   nsContext ), null );

                    for ( OMElement topicCategoriesElement : topicCategories ) {
                        String topicCategory = getNodeAsString( topicCategoriesElement,
                                                                new XPath( "./gmd:MD_TopicCategoryCode", nsContext ),
                                                                null );
                        keywordClass.setKeyword( topicCategory );
                        listOfKeywords.add( keywordClass );
                    }

                    keywordClass.setKeywordType( keywordType );
                    keywordClass.setKeyword( keyword );
                    keywordClass.setThesaurus( thesaurus );
                    listOfKeywords.add( keywordClass );

                }

                qp.setKeywords( listOfKeywords );

                // TODO relation -- aggregationInfo
                String[] _abstractStrings = getNodesAsStrings( _abstract,
                                                               new XPath( "./gco:CharacterString", nsContext ) );

                qp.set_abstract( Arrays.asList( _abstractStrings ) );

                identificationInfo = elem;
                elementFull.addChild( identificationInfo );
                continue;

            }

            if ( elem.getLocalName().equals( "distributionInfo" ) ) {
                List<OMElement> formats = getElements(
                                                       elem,
                                                       new XPath(
                                                                  "./gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format",
                                                                  nsContext ) );

                Format formatClass = null;
                List<Format> listOfFormats = new ArrayList<Format>();
                for ( OMElement md_format : formats ) {
                    formatClass = new Format();
                    String formatName = getNodeAsString( md_format, new XPath( "./gmd:name/gco:CharacterString",
                                                                               nsContext ), null );

                    String formatVersion = getNodeAsString( md_format, new XPath( "./gmd:version/gco:CharacterString",
                                                                                  nsContext ), null );

                    formatClass.setName( formatName );
                    formatClass.setVersion( formatVersion );

                    listOfFormats.add( formatClass );

                }

                qp.setFormat( listOfFormats );

                continue;
            }

        }

    }

    /**
     * TODO ExceptionHandling if there are properties that have to be in the insert statement
     * 
     * @throws IOException
     */
    public void executeInsertStatement()
                            throws IOException {

        generateMainDatabaseDataset();
        generateRecordBrief();
        generateRecordSummary();
        generateRecordFull();
        if ( qp.getTitle() != null ) {
            generateISOQP_titleStatement();
        }
        if ( qp.getType() != null ) {
            generateISOQP_typeStatement();
        }
        if ( qp.getSubject() != null ) {
            generateISOQP_keywordStatement();
        }
        if ( qp.getFormat() != null ) {
            generateISOQP_formatStatement();
        }
        // TODO relation
        if ( qp.get_abstract() != null ) {
            generateISOQP_AbstractStatement();
        }
        // TODO spatial
        if ( qp.getBoundingBox() != null ) {
            generateISOQP_boundingBoxStatement();
        }

    }

    /**
     * 
     */
    private void generateISOQP_AbstractStatement() {
        final String databaseTable = "isoqp_abstract";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            for ( String _abstract : qp.get_abstract() ) {
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, abstract) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + _abstract + "');";
            }
            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * 
     */
    private void generateISOQP_formatStatement() {
        final String databaseTable = "isoqp_format";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            for ( Format format : qp.getFormat() ) {
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + format.getName() + "');";
            }
            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * 
     */
    private void generateISOQP_keywordStatement() {
        final String databaseTable = "isoqp_keyword";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            for ( Keyword keyword : qp.getKeywords() ) {
                id++;
                sqlStatement = "INSERT INTO " + databaseTable
                               + " (id, fk_datasets, keywordtype, keyword, thesaurus) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + keyword.getKeywordType() + "','" + keyword.getKeyword()
                               + "','" + keyword.getThesaurus() + "');";
            }
            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * BE AWARE: the "modified" attribute is get from the first position in the list. The backend has the possibility to
     * add one such attribute. In the xsd-file there are more possible...
     * 
     */
    private void generateMainDatabaseDataset() {
        final String databaseTable = "datasets";
        String sqlStatement = "";

        try {
            stm = connection.createStatement();
            this.id = getLastDataset( connection, databaseTable );
            this.id++;
            sqlStatement = "INSERT INTO userdefinedqueryableproperties VALUES (" + id + ");";

            sqlStatement += "INSERT INTO "
                            + databaseTable
                            + " (id, version, status, anyText, identifier, modified, hassecurityconstraints, language, parentidentifier, source, association) VALUES ("
                            + this.id + ",null,null,'','" + qp.getIdentifier() + "','" + qp.getModified().get( 0 )
                            + "',FALSE,'','','', null);";
            System.out.println( sqlStatement );
            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateRecordBrief()
                            throws IOException {
        OMElement omElement = null;
        final String databaseTable = "recordbrief";

        String sqlStatement = "";
        int fk_datasets = this.id;
        int idDatabaseTable = 0;
        try {
            stm = connection.createStatement();
            idDatabaseTable = getLastDataset( connection, databaseTable );
            idDatabaseTable++;

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

                e.printStackTrace();
            }
            // -------------------
            sqlStatement = "INSERT INTO recordbrief (id, fk_datasets, format, data) VALUES (" + idDatabaseTable + ","
                           + fk_datasets + ", 2, '" + omElement.toString() + "');";

            stm.executeUpdate( sqlStatement );
            stm.close();

            generateDCBrief( databaseTable );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateRecordSummary() {
        final String databaseTable = "recordsummary";
        generateDCSummary( databaseTable );
    }

    private void generateDCBrief( String databaseTable ) {
        OMElement omElement = null;
        String sqlStatement = "";

        int fk_datasets = this.id;

        int idDatabaseTable = 0;
        try {
            stm = connection.createStatement();
            idDatabaseTable = getLastDataset( connection, databaseTable );
            idDatabaseTable++;

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

            sqlStatement = "INSERT INTO recordbrief (id, fk_datasets, format, data) VALUES (" + idDatabaseTable + ","
                           + fk_datasets + ", 1, '" + omElement.toString() + "');";

            recordInsertIDs.add( idDatabaseTable );

            System.out.println( "DC RecordBrief: " + sqlStatement );
            stm.executeUpdate( sqlStatement );
            stm.close();

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateDCSummary( String databaseTable ) {

        OMElement omElement = null;
        String sqlStatement = "";

        int fk_datasets = this.id;

        int idDatabaseTable = 0;
        try {
            stm = connection.createStatement();
            idDatabaseTable = getLastDataset( connection, databaseTable );
            idDatabaseTable++;

            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace namespace = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );
            OMNamespace namespaceDC = factory.createOMNamespace( "http://purl.org/dc/elements/1.1/", "dc" );
            OMNamespace namespaceOWS = factory.createOMNamespace( "http://www.opengis.net/ows", "ows" );
            OMNamespace namespaceDCT = factory.createOMNamespace( "http://purl.org/dc/terms/", "dct" );
            // TODO think about the right corners
            omElement = factory.createOMElement( "SummaryRecord", namespace );
            OMElement omIdentifier = factory.createOMElement( "identifier", namespaceDC );
            OMElement omType = factory.createOMElement( "type", namespaceDC );
            OMElement omBoundingBox = factory.createOMElement( "BoundingBox", namespaceOWS );
            OMElement omLowerCorner = factory.createOMElement( "LowerCorner", namespaceOWS );
            OMElement omUpperCorner = factory.createOMElement( "UpperCorner", namespaceOWS );

            omIdentifier.setText( qp.getIdentifier() );

            // dc:identifier
            omElement.addChild( omIdentifier );

            // dc:title
            for ( String title : qp.getTitle() ) {
                OMElement omTitle = factory.createOMElement( "title", namespaceDC );
                omTitle.setText( title );
                omElement.addChild( omTitle );
            }
            // dc:type
            if ( qp.getType() != null ) {
                omType.setText( qp.getType() );
            } else {
                omType.setText( "" );
            }
            omElement.addChild( omType );

            // dc:subject
            for ( Keyword subject : qp.getKeywords() ) {
                OMElement omSubject = factory.createOMElement( "subject", namespaceDC );
                omSubject.setText( subject.getKeyword() );
                omElement.addChild( omSubject );
            }

            // dc:format
            if ( qp.getFormat() != null ) {
                for ( Format format : qp.getFormat() ) {
                    OMElement omFormat = factory.createOMElement( "format", namespaceDC );
                    omFormat.setText( format.getName() );
                    omElement.addChild( omFormat );
                }
            } else {
                OMElement omFormat = factory.createOMElement( "format", namespaceDC );
                omElement.addChild( omFormat );
            }

            // dc:relation
            // TODO

            // dct:modified
            for ( Date date : qp.getModified() ) {
                OMElement omModified = factory.createOMElement( "modified", namespaceDCT );
                omModified.setText( date.toString() );
                omElement.addChild( omModified );
            }

            // dct:abstract
            for ( String _abstract : qp.get_abstract() ) {
                OMElement omAbstract = factory.createOMElement( "abstract", namespaceDCT );
                omAbstract.setText( _abstract.toString() );
                omElement.addChild( omAbstract );
            }

            // dct:spatial
            // TODO

            // ows:BoundingBox
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

            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                           + idDatabaseTable + "," + fk_datasets + ", 1, '" + omElement.toString() + "');";

            System.out.println( sqlStatement );
            stm.executeUpdate( sqlStatement );
            stm.close();

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateRecordFull()
                            throws IOException {
        final String databaseTable = "recordfull";
        String sqlStatement = "";
        int fk_datasets = this.id;
        int idDatabaseTable = 0;
        try {
            stm = connection.createStatement();
            idDatabaseTable = getLastDataset( connection, databaseTable );
            idDatabaseTable++;
            sqlStatement = "INSERT INTO recordfull (id, fk_datasets, format, data) VALUES (" + idDatabaseTable + ","
                           + fk_datasets + ", 2, '" + elementFull.toString() + "');";

            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateISOQP_titleStatement() {
        final String databaseTable = "isoqp_title";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            for ( String title : qp.getTitle() ) {
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, title) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + title + "');";
            }

            stm.executeUpdate( sqlStatement );
            stm.close();

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateISOQP_typeStatement() {
        final String databaseTable = "isoqp_type";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, type) VALUES (" + id + ","
                           + mainDatabaseTableID + ",'" + qp.getType() + "');";

            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateISOQP_boundingBoxStatement() {
        final String databaseTable = "isoqp_boundingbox";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, bbox) VALUES (" + id + ","
                           + mainDatabaseTableID + ",SetSRID('BOX3D(" + qp.getBoundingBox().getEastBoundLongitude()
                           + " " + qp.getBoundingBox().getNorthBoundLatitude() + ","
                           + qp.getBoundingBox().getWestBoundLongitude() + " "
                           + qp.getBoundingBox().getSouthBoundLatitude() + ")'::box3d,4326));";

            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {

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

    /**
     * @return the recordInsertIDs
     */
    public List<Integer> getRecordInsertIDs() {
        return recordInsertIDs;
    }

}
