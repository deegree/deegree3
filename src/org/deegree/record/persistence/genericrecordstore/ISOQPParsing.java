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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.types.datetime.Date;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.crs.CRS;
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

    private OMFactory factory = OMAbstractFactory.getOMFactory();

    private OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );

    private OMNamespace namespaceDC = factory.createOMNamespace( "http://purl.org/dc/elements/1.1/", "dc" );

    private OMNamespace namespaceDCT = factory.createOMNamespace( "http://purl.org/dc/terms/", "dct" );

    private OMNamespace namespaceGMD = factory.createOMNamespace( "http://www.isotc211.org/2005/gmd", "" );

    private int id;

    private Connection connection;

    private OMElement element;

    private OMElement elementFull;

    private OMElement recordFull;

    private OMElement identifier = null;

    private OMElement hierarchyLevel = null;

    private OMElement hierarchyLevelName = null;

    private OMElement language = null;

    private OMElement dataQualityInfo = null;

    private OMElement characterSet = null;

    private OMElement metadataStandardName = null;

    private OMElement metadataStandardVersion = null;

    private OMElement parentIdentifier = null;

    private OMElement identificationInfo = null;

    private OMElement referenceSystemInfo = null;

    private OMElement distributionInfo = null;

    private Statement stm;

    private List<Integer> recordInsertIDs;

    public ISOQPParsing( OMElement element, Connection connection ) {
        this.element = element;
        this.elementFull = element;
        this.connection = connection;

        setRootElement( element );
        nsContext.addNamespace( rootElement.getDefaultNamespace().getPrefix(),
                                rootElement.getDefaultNamespace().getNamespaceURI() );
        nsContext.addNamespace( CSW_PREFIX, CSWConstants.CSW_202_NS );
        nsContext.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );

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

            qp.setAnyText( elem.toString() );

            if ( elem.getLocalName().equals( "fileIdentifier" ) ) {
                qp.setIdentifier( getNodeAsString( elem, new XPath( "./gco:CharacterString", nsContext ), null ) );

                identifier = elem;
                OMNamespace namespace = identifier.getNamespace();
                identifier.setNamespace( namespace );
                elementFull.addChild( identifier );

                continue;

            }
            if ( elem.getLocalName().equals( "hierarchyLevel" ) ) {
                String type = getNodeAsString( elem, new XPath( "./gmd:MD_ScopeCode/@codeListValue", nsContext ),
                                               "Datasets" );

                qp.setType( type );

                hierarchyLevel = elem;
                OMNamespace namespace = hierarchyLevel.getNamespace();
                hierarchyLevel.setNamespace( namespace );
                elementFull.addChild( hierarchyLevel );
                continue;
            }

            if ( elem.getLocalName().equals( "hierarchyLevelName" ) ) {

                hierarchyLevelName = elem;
                OMNamespace namespace = hierarchyLevelName.getNamespace();
                hierarchyLevelName.setNamespace( namespace );
                elementFull.addChild( hierarchyLevelName );
                continue;
            }

            if ( elem.getLocalName().equals( "dateStamp" ) ) {

                String dateString = getNodeAsString( elem, new XPath( "./gco:Date", nsContext ), "0000-00-00" );
                Date date = null;
                try {
                    date = new Date( dateString );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setModified( date );
                // String[] dateStrings = getNodesAsStrings( elem, new XPath( "./gco:Date", nsContext ) );
                // Date[] dates = new Date[dateStrings.length];
                // Date date = null;
                // for ( int i = 0; i < dateStrings.length; i++ ) {
                // try {
                // date = new Date( dateStrings[i] );
                // } catch ( ParseException e ) {
                //
                // e.printStackTrace();
                // }
                // dates[i] = date;
                //
                // }
                //
                // qp.setModified( Arrays.asList( dates ) );

                continue;
            }

            // TODO there are more than one refSysInfo!!
            if ( elem.getLocalName().equals( "referenceSystemInfo" ) ) {
                OMElement crsElement = getElement(
                                                   elem,
                                                   new XPath(
                                                              "./gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier",
                                                              nsContext ) );
                String crsIdentification = getNodeAsString( crsElement, new XPath( "./gmd:code/gco:CharacterString",
                                                                                   nsContext ), "" );

                String crsAuthority = getNodeAsString( crsElement, new XPath( "./gmd:codeSpace/gco:CharacterString",
                                                                              nsContext ), "" );

                String crsVersion = getNodeAsString( crsElement, new XPath( "./gmd:version/gco:CharacterString",
                                                                            nsContext ), "" );

                // CRS crs = new CRS( crsAuthority, crsIdentification, crsVersion );
                CRS crs = new CRS( crsIdentification );
                qp.setCrs( crs );
                referenceSystemInfo = elem;
                OMNamespace namespace = referenceSystemInfo.getNamespace();
                referenceSystemInfo.setNamespace( namespace );
                elementFull.addChild( referenceSystemInfo );

                continue;

            }

            if ( elem.getLocalName().equals( "language" ) ) {

                rp.setLanguage( getNodeAsString( elem, new XPath( "./gco:CharacterString", nsContext ), null ) );
                language = elem;
                continue;
            }

            if ( elem.getLocalName().equals( "dataQualityInfo" ) ) {

                dataQualityInfo = elem;
                continue;
            }

            if ( elem.getLocalName().equals( "characterSet" ) ) {

                characterSet = elem;
                continue;
            }

            if ( elem.getLocalName().equals( "metadataStandardName" ) ) {

                metadataStandardName = elem;
                continue;
            }

            if ( elem.getLocalName().equals( "metadataStandardVersion" ) ) {

                metadataStandardVersion = elem;
                continue;
            }
            if ( elem.getLocalName().equals( "parentIdentifier" ) ) {

                parentIdentifier = elem;
                continue;
            }

            if ( elem.getLocalName().equals( "identificationInfo" ) ) {

                OMElement md_identification = getElement( elem, new XPath( "./gmd:MD_Identification", nsContext ) );

                OMElement md_dataIdentification = getElement( elem,
                                                              new XPath( "./gmd:MD_DataIdentification", nsContext ) );

                OMElement ci_responsibleParty = getElement( md_dataIdentification,
                                                            new XPath( "./gmd:pointOfContact/gmd:CI_ResponsibleParty",
                                                                       nsContext ) );

                String resourceLanguage = getNodeAsString(
                                                           md_dataIdentification,
                                                           new XPath( "./gmd:language/gco:CharacterString", nsContext ),
                                                           null );
                qp.setResourceLanguage( resourceLanguage );

                String creator = getNodeAsString(
                                                  ci_responsibleParty,
                                                  new XPath(
                                                             "./gmd:organisationName[../gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gco:CharacterString",
                                                             nsContext ), null );

                rp.setCreator( creator );

                String publisher = getNodeAsString(
                                                    ci_responsibleParty,
                                                    new XPath(
                                                               "./gmd:organisationName[../gmd:role/gmd:CI_RoleCode/@codeListValue='publisher']/gco:CharacterString",
                                                               nsContext ), null );

                rp.setPublisher( publisher );

                String contributor = getNodeAsString(
                                                      ci_responsibleParty,
                                                      new XPath(
                                                                 "./gmd:organisationName[../gmd:role/gmd:CI_RoleCode/@codeListValue='author']/gco:CharacterString",
                                                                 nsContext ), null );
                rp.setContributor( contributor );

                String[] rightsElements = getNodesAsStrings(
                                                             md_dataIdentification,
                                                             new XPath(
                                                                        "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/@codeListValue",
                                                                        nsContext ) );
                rp.setRights( Arrays.asList( rightsElements ) );

                // OMElement sv_serviceIdentification = getElement( elem, new XPath( "./srv:SV_ServiceIdentification",
                // nsContext ) );

                // String couplingType = getNodeAsString( sv_serviceIdentification, new XPath(
                // "./srv:couplingType/srv:SV_CouplingType/@codeListValue", nsContext ), null );

                OMElement _abstract = getElement( md_dataIdentification, new XPath( "./gmd:abstract", nsContext ) );

                OMElement bbox = getElement(
                                             md_dataIdentification,
                                             new XPath(
                                                        "./gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox",
                                                        nsContext ) );

                List<OMElement> descriptiveKeywords = getElements( md_dataIdentification,
                                                                   new XPath( "./gmd:descriptiveKeywords", nsContext ) );

                List<OMElement> topicCategories = getElements( md_dataIdentification, new XPath( "./gmd:topicCategory",
                                                                                                 nsContext ) );

                String graphicOverview = getNodeAsString( md_dataIdentification,
                                                          new XPath( "./gmd:graphicOverview/gmd:MD_BrowseGraphic",
                                                                     nsContext ), null );

                String[] titleElements = getNodesAsStrings(
                                                            md_dataIdentification,
                                                            new XPath(
                                                                       "./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString",
                                                                       nsContext ) );

                String[] alternateTitleElements = getNodesAsStrings(
                                                                     md_dataIdentification,
                                                                     new XPath(
                                                                                "./gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString",
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

                qp.setAlternateTitle( Arrays.asList( alternateTitleElements ) );

                // not necessary actually...
                rp.setGraphicOverview( graphicOverview );
                // TODO same with serviceType and serviceTypeVersion
                Keyword keywordClass;

                List<Keyword> listOfKeywords = new ArrayList<Keyword>();
                for ( OMElement md_keywords : descriptiveKeywords ) {
                    keywordClass = new Keyword();
                    // keywordClass =
                    String keywordType = getNodeAsString(
                                                          md_keywords,
                                                          new XPath(
                                                                     "./gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue",
                                                                     nsContext ), null );

                    String[] keywords = getNodesAsStrings(
                                                           md_keywords,
                                                           new XPath(
                                                                      "./gmd:MD_Keywords/gmd:keyword/gco:CharacterString",
                                                                      nsContext ) );

                    String thesaurus = getNodeAsString(
                                                        md_keywords,
                                                        new XPath(
                                                                   "./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString",
                                                                   nsContext ), null );

                    keywordClass.setKeywordType( keywordType );

                    keywordClass.setKeywords( Arrays.asList( keywords ) );

                    keywordClass.setThesaurus( thesaurus );
                    listOfKeywords.add( keywordClass );

                }

                for ( OMElement topicCategoriesElement : topicCategories ) {
                    keywordClass = new Keyword();
                    String[] topicCategory = getNodesAsStrings( topicCategoriesElement,
                                                                new XPath( "./gmd:MD_TopicCategoryCode", nsContext ) );
                    keywordClass.setKeywords( Arrays.asList( topicCategory ) );
                    listOfKeywords.add( keywordClass );
                }

                qp.setKeywords( listOfKeywords );

                // TODO relation -- aggregationInfo
                String[] _abstractStrings = getNodesAsStrings( _abstract,
                                                               new XPath( "./gco:CharacterString", nsContext ) );

                qp.set_abstract( Arrays.asList( _abstractStrings ) );

                identificationInfo = elem;
                OMNamespace namespace = identificationInfo.getNamespace();
                identificationInfo.setNamespace( namespace );
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

            if ( elem.getLocalName().equals( "distributionInfo" ) ) {
                distributionInfo = elem;
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
            generateISOQP_TitleStatement();
        }
        if ( qp.getType() != null ) {
            generateISOQP_TypeStatement();
        }
        if ( qp.getSubject() != null ) {
            generateISOQP_KeywordStatement();
        }
        if ( qp.getFormat() != null ) {
            generateISOQP_FormatStatement();
        }
        // TODO relation
        if ( qp.get_abstract() != null ) {
            generateISOQP_AbstractStatement();
        }
        // TODO spatial
        if ( qp.getBoundingBox() != null ) {
            generateISOQP_BoundingBoxStatement();
        }

    }

    /**
     * 
     */
    public void executeUpdateStatement() {
        final String databaseTable = "datasets";

        StringWriter sqlStatementUpdate = new StringWriter( 500 );
        StringBuffer buf = new StringBuffer();
        int requestedId = 0;
        try {
            stm = connection.createStatement();
            sqlStatementUpdate.append( "SELECT " + databaseTable + ".id from " + databaseTable + " where "
                                       + databaseTable + ".identifier = '" + qp.getIdentifier() + "'" );
            System.out.println( sqlStatementUpdate.toString() );
            buf = sqlStatementUpdate.getBuffer();
            ResultSet rs = connection.createStatement().executeQuery( sqlStatementUpdate.toString() );

            if ( rs != null ) {
                while ( rs.next() ) {
                    requestedId = rs.getInt( 1 );
                    System.out.println( rs.getInt( 1 ) );
                }
                buf.setLength( 0 );
                rs.close();
            }
            if ( requestedId != 0 ) {
                // TODO version

                // TODO status

                // anyText
                if ( qp.getAnyText() != null ) {

                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET anyText = '" + qp.getAnyText()
                                              + "' WHERE id = " + requestedId );

                    buf = sqlStatementUpdate.getBuffer();
                    System.out.println( sqlStatementUpdate.toString() );
                    stm.executeUpdate( sqlStatementUpdate.toString() );
                    buf.setLength( 0 );

                }
                // identifier
                if ( qp.getIdentifier() != null ) {

                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET identifier = '" + qp.getIdentifier()
                                              + "' WHERE id = " + requestedId );

                    buf = sqlStatementUpdate.getBuffer();
                    System.out.println( sqlStatementUpdate.toString() );
                    stm.executeUpdate( sqlStatementUpdate.toString() );
                    buf.setLength( 0 );
                }
                // modified
                if ( !qp.getModified().equals( new Date( "0000-00-00" ) ) ) {
                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET modified = '" + qp.getModified()
                                              + "' WHERE id = " + requestedId );
                    buf = sqlStatementUpdate.getBuffer();
                    System.out.println( sqlStatementUpdate.toString() );
                    stm.executeUpdate( sqlStatementUpdate.toString() );
                    buf.setLength( 0 );
                }
                // TODO hassecurityconstraints

                // language
                if ( rp.getLanguage() != null ) {
                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET language = '" + rp.getLanguage()
                                              + "' WHERE id = " + requestedId );

                    buf = sqlStatementUpdate.getBuffer();
                    System.out.println( sqlStatementUpdate.toString() );
                    stm.executeUpdate( sqlStatementUpdate.toString() );
                    buf.setLength( 0 );
                }
                // TODO parentidentifier

                // TODO source

                // TODO association

                // recordBrief update
                updateRecord( requestedId, "recordbrief" );

                // recordSummary update
                updateRecord( requestedId, "recordsummary" );

                // recordFull update
                updateRecord( requestedId, "recordfull" );
            } else {
                // TODO think about what response should be written if there is no such dataset in the backend??
                String msg = "No dataset found for the identifier --> " + qp.getIdentifier() + " <--. ";
                throw new SQLException( msg );
            }

            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        } catch ( ParseException e ) {

            e.printStackTrace();
        } catch ( IOException e ) {

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
        String modifiedAttribute = "";
        try {
            stm = connection.createStatement();
            this.id = getLastDataset( connection, databaseTable );
            this.id++;
            // sqlStatement = "INSERT INTO userdefinedqueryableproperties VALUES (" + id + ");";

            if ( qp.getModified().equals( new Date( "0000-00-00" ) ) ) {
                qp.setModified( null );
                modifiedAttribute = "" + qp.getModified();
            } else {
                modifiedAttribute = "'" + qp.getModified() + "'";
            }

            sqlStatement += "INSERT INTO "
                            + databaseTable
                            + " (id, version, status, anyText, identifier, modified, hassecurityconstraints, language, parentidentifier, source, association) VALUES ("
                            + this.id + ",null,null,'','" + qp.getIdentifier() + "'," + modifiedAttribute
                            + ",FALSE,'','','', null);";
            System.out.println( sqlStatement );
            stm.executeUpdate( sqlStatement );
            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        } catch ( ParseException e ) {

            e.printStackTrace();
        }

    }

    private void updateRecord( int fk_datasets, String databaseTable )
                            throws IOException {
        
        String elementName = "";
        String isoOMElement = "";

        //final String databaseTable = "recordbrief";
        if(databaseTable.equals( "recordbrief" )){
            elementName = "BriefRecord";
            isoOMElement = setISOBriefElements().toString();
            
        }else if(databaseTable.equals( "recordsummary" )){
            elementName = "SummaryRecord";
            isoOMElement = setISOSummaryElements().toString();
            
        }else{
            elementName = "Record";
            isoOMElement = elementFull.toString();
        }
        

        StringWriter sqlStatement = new StringWriter( 500 );
        StringBuffer buf = new StringBuffer();
        OMElement omElement = null;

        try {
            // DC-update
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );

            omElement = factory.createOMElement( elementName, namespaceCSW );

            setDCBriefElements( factory, omElement );

            setBoundingBoxElement( factory, omElement );

            sqlStatement.write( "UPDATE " + databaseTable + " SET data = '" + omElement.toString()
                                + "' WHERE fk_datasets = " + fk_datasets + " AND format = " + 1 );

            buf = sqlStatement.getBuffer();
            System.out.println( sqlStatement.toString() );
            stm.executeUpdate( sqlStatement.toString() );
            buf.setLength( 0 );

            // ISO-update
            sqlStatement.write( "UPDATE " + databaseTable + " SET data = '" + isoOMElement
                                + "' WHERE fk_datasets = " + fk_datasets + " AND format = " + 2 );

            buf = sqlStatement.getBuffer();
            System.out.println( sqlStatement.toString() );
            stm.executeUpdate( sqlStatement.toString() );
            buf.setLength( 0 );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateRecordBrief()
                            throws IOException {

        final String databaseTable = "recordbrief";

        String sqlStatement = "";
        int fk_datasets = this.id;
        int idDatabaseTable = 0;

        try {
            stm = connection.createStatement();
            idDatabaseTable = getLastDataset( connection, databaseTable );
            idDatabaseTable++;

            // -------------------
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                           + idDatabaseTable + "," + fk_datasets + ", 2, '" + setISOBriefElements().toString() + "');";

            stm.executeUpdate( sqlStatement );
            stm.close();

            generateDCBrief( databaseTable );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

//    private void updateRecordSummary( int fk_datasets )
//                            throws IOException {
//
//        final String databaseTable = "recordsummary";
//
//        StringWriter sqlStatement = new StringWriter( 500 );
//        StringBuffer buf = new StringBuffer();
//        OMElement omElement = null;
//
//        try {
//            // DC-update
//            OMFactory factory = OMAbstractFactory.getOMFactory();
//            OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );
//
//            omElement = factory.createOMElement( "SummaryRecord", namespaceCSW );
//
//            setDCBriefElements( factory, omElement );
//
//            setBoundingBoxElement( factory, omElement );
//
//            sqlStatement.write( "UPDATE " + databaseTable + " SET data = '" + omElement.toString()
//                                + "' WHERE fk_datasets = " + fk_datasets + " AND format = " + 1 );
//
//            buf = sqlStatement.getBuffer();
//            System.out.println( sqlStatement.toString() );
//            stm.executeUpdate( sqlStatement.toString() );
//            buf.setLength( 0 );
//
//            // ISO-update
//            sqlStatement.write( "UPDATE " + databaseTable + " SET data = '" + setISOSummaryElements().toString()
//                                + "' WHERE fk_datasets = " + fk_datasets + " AND format = " + 2 );
//
//            buf = sqlStatement.getBuffer();
//            System.out.println( sqlStatement.toString() );
//            stm.executeUpdate( sqlStatement.toString() );
//            buf.setLength( 0 );
//
//        } catch ( SQLException e ) {
//
//            e.printStackTrace();
//        }
//
//    }

//    private void updateRecordFull( int fk_datasets )
//                            throws IOException {
//
//        final String databaseTable = "recordfull";
//
//        StringWriter sqlStatement = new StringWriter( 500 );
//        StringBuffer buf = new StringBuffer();
//        OMElement omElement = null;
//
//        try {
//            // DC-update
//            OMFactory factory = OMAbstractFactory.getOMFactory();
//            OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );
//
//            omElement = factory.createOMElement( "Record", namespaceCSW );
//
//            setDCBriefElements( factory, omElement );
//
//            setBoundingBoxElement( factory, omElement );
//
//            sqlStatement.write( "UPDATE " + databaseTable + " SET data = '" + omElement.toString()
//                                + "' WHERE fk_datasets = " + fk_datasets + " AND format = " + 1 );
//
//            buf = sqlStatement.getBuffer();
//            System.out.println( sqlStatement.toString() );
//            stm.executeUpdate( sqlStatement.toString() );
//            buf.setLength( 0 );
//
//            // ISO-update
//            sqlStatement.write( "UPDATE " + databaseTable + " SET data = '" + elementFull.toString()
//                                + "' WHERE fk_datasets = " + fk_datasets + " AND format = " + 2 );
//
//            buf = sqlStatement.getBuffer();
//            System.out.println( sqlStatement.toString() );
//            stm.executeUpdate( sqlStatement.toString() );
//            buf.setLength( 0 );
//
//        } catch ( SQLException e ) {
//
//            e.printStackTrace();
//        }
//
//    }

    /**
     * TODO ISO Summary record
     */
    private void generateRecordSummary() {
        final String databaseTable = "recordsummary";

        String sqlStatement = "";
        int fk_datasets = this.id;
        int idDatabaseTable = 0;
        try {
            stm = connection.createStatement();
            idDatabaseTable = getLastDataset( connection, databaseTable );
            idDatabaseTable++;

            // -------------------
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                           + idDatabaseTable + "," + fk_datasets + ", 2, '" + setISOSummaryElements().toString()
                           + "');";

            stm.executeUpdate( sqlStatement );
            stm.close();

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

        generateDCSummary( databaseTable );
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

            generateDCFull( databaseTable );
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateDCBrief( String databaseTable ) {
        OMElement omElement = null;
        String sqlStatement = "";

        int fk_datasets = this.id;

        int idDatabaseTable = 0;
        try {
            recordInsertIDs = new ArrayList<Integer>();
            stm = connection.createStatement();
            idDatabaseTable = getLastDataset( connection, databaseTable );
            idDatabaseTable++;

            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );

            omElement = factory.createOMElement( "BriefRecord", namespaceCSW );

            setDCBriefElements( factory, omElement );

            setBoundingBoxElement( factory, omElement );

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

            omElement = factory.createOMElement( "SummaryRecord", namespaceCSW );

            // summaryRecordElements
            setDCSummaryElements( factory, omElement );

            // ows:BoundingBox
            setBoundingBoxElement( factory, omElement );

            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                           + idDatabaseTable + "," + fk_datasets + ", 1, '" + omElement.toString() + "');";

            System.out.println( sqlStatement );
            stm.executeUpdate( sqlStatement );
            stm.close();

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * @param databaseTable
     */
    private void generateDCFull( String databaseTable ) {
        OMElement omElement = null;
        String sqlStatement = "";

        int fk_datasets = this.id;

        int idDatabaseTable = 0;
        try {
            stm = connection.createStatement();
            idDatabaseTable = getLastDataset( connection, databaseTable );
            idDatabaseTable++;

            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );

            omElement = factory.createOMElement( "Record", namespaceCSW );

            // dc RecordFull
            setDCFullElements( factory, omElement );

            // ows:BoundingBox
            setBoundingBoxElement( factory, omElement );

            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                           + idDatabaseTable + "," + fk_datasets + ", 1, '" + omElement.toString() + "');";

            System.out.println( "DC RecordFull: " + sqlStatement );
            stm.executeUpdate( sqlStatement );
            stm.close();

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    private void generateISOQP_TitleStatement() {
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

    private void generateISOQP_TypeStatement() {
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

    /**
     * 
     */
    private void generateISOQP_KeywordStatement() {
        final String databaseTable = "isoqp_keyword";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            for ( Keyword keyword : qp.getKeywords() ) {
                for ( String keywordString : keyword.getKeywords() ) {
                    id++;
                    sqlStatement = "INSERT INTO " + databaseTable
                                   + " (id, fk_datasets, keywordtype, keyword, thesaurus) VALUES (" + id + ","
                                   + mainDatabaseTableID + ",'" + keyword.getKeywordType() + "','" + keywordString
                                   + "','" + keyword.getThesaurus() + "');";
                    stm.executeUpdate( sqlStatement );
                }
            }

            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * 
     */
    private void generateISOQP_FormatStatement() {
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

    private void generateISOQP_BoundingBoxStatement() {
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

    private void generateISOQP_CRSStatement() {
        final String databaseTable = "isoqp_crs";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {
            stm = connection.createStatement();
            id = getLastDataset( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, authority, id_crs, version) VALUES ("
                           + id + "," + mainDatabaseTableID + "," + qp.getCrs().getName() + "," + qp.getCrs().getName()
                           + "," + qp.getCrs().getName() + ");";

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

    /**
     * 
     * 
     * @param factory
     * @param omElement
     */
    private void setDCBriefElements( OMFactory factory, OMElement omElement ) {

        OMElement omIdentifier = factory.createOMElement( "identifier", namespaceDC );
        OMElement omType = factory.createOMElement( "type", namespaceDC );

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
    }

    private void setDCSummaryElements( OMFactory factory, OMElement omElement ) {
        setDCBriefElements( factory, omElement );

        // dc:subject
        for ( Keyword subjects : qp.getKeywords() ) {
            for ( String subject : subjects.getKeywords() ) {

                OMElement omSubject = factory.createOMElement( "subject", namespaceDC );
                omSubject.setText( subject );
                omElement.addChild( omSubject );
            }
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
        // for ( Date date : qp.getModified() ) {
        // OMElement omModified = factory.createOMElement( "modified", namespaceDCT );
        // omModified.setText( date.toString() );
        // omElement.addChild( omModified );
        // }
        OMElement omModified = factory.createOMElement( "modified", namespaceDCT );
        omElement.addChild( omModified );

        // dct:abstract
        for ( String _abstract : qp.get_abstract() ) {
            OMElement omAbstract = factory.createOMElement( "abstract", namespaceDCT );
            omAbstract.setText( _abstract.toString() );
            omElement.addChild( omAbstract );
        }

        // dct:spatial
        // TODO

    }

    private void setDCFullElements( OMFactory factory, OMElement omElement ) {

        setDCSummaryElements( factory, omElement );

        OMElement omCreator = factory.createOMElement( "creator", namespaceDC );
        omCreator.setText( rp.getCreator() );
        omElement.addChild( omCreator );

        OMElement omPublisher = factory.createOMElement( "publisher", namespaceDC );
        omPublisher.setText( rp.getPublisher() );
        omElement.addChild( omPublisher );

        OMElement omContributor = factory.createOMElement( "contributor", namespaceDC );
        omContributor.setText( rp.getContributor() );
        omElement.addChild( omContributor );

        OMElement omSource = factory.createOMElement( "source", namespaceDC );
        omSource.setText( rp.getSource() );
        omElement.addChild( omSource );

        OMElement omLanguage = factory.createOMElement( "language", namespaceDC );
        omLanguage.setText( rp.getLanguage() );
        omElement.addChild( omLanguage );

        // dc:rights
        for ( String rights : rp.getRights() ) {
            OMElement omRights = factory.createOMElement( "rights", namespaceDC );
            omRights.setText( rights );
            omElement.addChild( omRights );
        }

    }

    private void setBoundingBoxElement( OMFactory factory, OMElement omElement ) {

        OMNamespace namespaceOWS = factory.createOMNamespace( "http://www.opengis.net/ows", "ows" );

        OMElement omBoundingBox = factory.createOMElement( "BoundingBox", namespaceOWS );
        OMElement omLowerCorner = factory.createOMElement( "LowerCorner", namespaceOWS );
        OMElement omUpperCorner = factory.createOMElement( "UpperCorner", namespaceOWS );
        // OMAttribute omCrs = factory.createOMAttribute( "crs", namespaceOWS, qp.getCrs() );
        System.out.println( qp.getCrs() );

        omLowerCorner.setText( qp.getBoundingBox().getEastBoundLongitude() + " "
                               + qp.getBoundingBox().getSouthBoundLatitude() );
        omUpperCorner.setText( qp.getBoundingBox().getWestBoundLongitude() + " "
                               + qp.getBoundingBox().getNorthBoundLatitude() );
        omBoundingBox.addChild( omLowerCorner );
        omBoundingBox.addChild( omUpperCorner );
        if ( qp.getCrs() != null ) {
            // omBoundingBox.addAttribute( omCrs );
        }

        omElement.addChild( omBoundingBox );

    }

    private OMElement setISOBriefElements() {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement omElement;
        // OMElement omIdentifier = factory.createOMElement( "fileIdentifier", namespaceGMD );

        omElement = factory.createOMElement( "MD_Metadata", namespaceGMD );
        omElement.addChild( identifier );
        if ( hierarchyLevel != null ) {
            omElement.addChild( hierarchyLevel );
        }
        if ( identificationInfo != null ) {
            omElement.addChild( identificationInfo );
        }
        return omElement;

    }

    private OMElement setISOSummaryElements() {

        // OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement omElement;
        // OMNamespace namespace = factory.createOMNamespace( rootElement.getDefaultNamespace().getNamespaceURI(), "gmd"
        // );

        omElement = setISOBriefElements();

        if ( distributionInfo != null ) {
            omElement.addChild( distributionInfo );
        }
        if ( hierarchyLevelName != null ) {
            omElement.addChild( hierarchyLevelName );
        }
        if ( language != null ) {
            omElement.addChild( language );
        }
        if ( dataQualityInfo != null ) {
            omElement.addChild( dataQualityInfo );
        }
        if ( characterSet != null ) {
            omElement.addChild( characterSet );
        }
        if ( metadataStandardName != null ) {
            omElement.addChild( metadataStandardName );
        }
        if ( metadataStandardVersion != null ) {
            omElement.addChild( metadataStandardVersion );
        }
        if ( parentIdentifier != null ) {
            omElement.addChild( parentIdentifier );
        }
        if ( referenceSystemInfo != null ) {
            omElement.addChild( referenceSystemInfo );
        }

        return omElement;

    }

}
