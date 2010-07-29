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
package org.deegree.record.persistence.genericrecordstore.generating;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.cs.CRS;
import org.deegree.record.persistence.genericrecordstore.PostGISMappingsISODC;
import org.deegree.record.persistence.genericrecordstore.parsing.ParsedProfileElement;
import org.deegree.record.persistence.genericrecordstore.parsing.QueryableProperties;
import org.deegree.record.persistence.genericrecordstore.parsing.ReturnableProperties;
import org.deegree.record.persistence.neededdatastructures.Format;
import org.deegree.record.persistence.neededdatastructures.Keyword;
import org.deegree.record.persistence.neededdatastructures.OperatesOnData;
import org.slf4j.Logger;

/**
 * Here are all the queryable properties encapsulated which have to put into the backend. Here is the functionality of
 * the INSERT and UPDATE action.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GenerateQueryableProperties {

    private static final Logger LOG = getLogger( GenerateQueryableProperties.class );

    /**
     * Generates and inserts the maindatabasetable that is needed for the queryable properties databasetables to derive
     * from.
     * <p>
     * BE AWARE: the "modified" attribute is get from the first position in the list. The backend has the possibility to
     * add one such attribute. In the xsd-file there are more possible...
     * 
     * @param connection
     *            the SQL connection
     * @param stm
     * @param parsedElement
     * @return the primarykey of the inserted dataset which is the foreignkey for the queryable properties
     *         databasetables
     */
    public int generateMainDatabaseDataset( Connection connection, ParsedProfileElement parsedElement ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.datasets.name();
        StringWriter sqlStatement = new StringWriter( 1000 );
        boolean isCaseSensitive = true;
        PreparedStatement stm = null;
        int operatesOnId = 0;
        // Timestamp tmsp = new Timestamp(0000-00-00);
        try {

            operatesOnId = getLastDatasetId( connection, databaseTable );
            operatesOnId++;

            sqlStatement.append( "INSERT INTO "
                                 + databaseTable
                                 + " ("
                                 + PostGISMappingsISODC.CommonColumnNames.id.name()
                                 + ", version, status, anyText, modified, hassecurityconstraints, language, parentidentifier, source, association) VALUES (?,?,?,?,?,?,?,?,?,?);" );

            stm = connection.prepareStatement( sqlStatement.toString() );
            stm.setObject( 1, operatesOnId );
            stm.setObject( 2, null );
            stm.setObject( 3, null );
            stm.setObject( 4, generateISOQP_AnyTextStatement( isCaseSensitive, parsedElement.getQueryableProperties(),
                                                              parsedElement.getReturnableProperties() ) );
            if ( !parsedElement.getQueryableProperties().getModified().equals( new Date( "0000-00-00" ) ) ) {
                String time = parsedElement.getQueryableProperties().getModified().toString();
                stm.setTimestamp(
                                  5,
                                  Timestamp.valueOf( DateUtils.formatJDBCTimeStamp( DateUtils.parseISO8601Date( time ) ) ) );
            } else {
                stm.setTimestamp( 5, null );
            }

            stm.setObject( 6, parsedElement.getQueryableProperties().isHasSecurityConstraints() );
            stm.setObject( 7, parsedElement.getQueryableProperties().getLanguage() );
            stm.setObject( 8, parsedElement.getQueryableProperties().getParentIdentifier() );
            stm.setObject( 9, null );
            stm.setObject( 10, null );
            stm.executeUpdate();
            stm.close();

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

        return operatesOnId;

    }

    /**
     * Method that encapsulates the generating for all the queryable properties.
     * 
     * @param isUpdate
     * @param connection
     * @param stm
     * @param operatesOnId
     * @param parsedElement
     */
    public void executeQueryableProperties( boolean isUpdate, Connection connection, int operatesOnId,
                                            ParsedProfileElement parsedElement ) {

        if ( parsedElement.getQueryableProperties().getIdentifier() != null ) {
            generateQP_IdentifierStatement( isUpdate, connection, operatesOnId, parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getTitle() != null ) {
            generateISOQP_TitleStatement( isUpdate, connection, operatesOnId, parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getType() != null ) {
            generateISOQP_TypeStatement( isUpdate, connection, operatesOnId, parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getKeywords() != null ) {
            generateISOQP_KeywordStatement( isUpdate, connection, operatesOnId, parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getTopicCategory() != null ) {
            generateISOQP_TopicCategoryStatement( isUpdate, connection, operatesOnId,
                                                  parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getFormat() != null ) {
            generateISOQP_FormatStatement( isUpdate, connection, operatesOnId, parsedElement.getQueryableProperties() );
        }
        // TODO relation
        if ( parsedElement.getQueryableProperties().get_abstract() != null ) {
            generateISOQP_AbstractStatement( isUpdate, connection, operatesOnId, parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getAlternateTitle() != null ) {
            generateISOQP_AlternateTitleStatement( isUpdate, connection, operatesOnId,
                                                   parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getCreationDate() != null ) {
            generateISOQP_CreationDateStatement( isUpdate, connection, operatesOnId,
                                                 parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getPublicationDate() != null ) {
            generateISOQP_PublicationDateStatement( isUpdate, connection, operatesOnId,
                                                    parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getRevisionDate() != null ) {
            generateISOQP_RevisionDateStatement( isUpdate, connection, operatesOnId,
                                                 parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getResourceIdentifier() != null ) {
            generateISOQP_ResourceIdentifierStatement( isUpdate, connection, operatesOnId,
                                                       parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getServiceType() != null ) {
            generateISOQP_ServiceTypeStatement( isUpdate, connection, operatesOnId,
                                                parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getServiceTypeVersion() != null ) {
            generateISOQP_ServiceTypeVersionStatement( isUpdate, connection, operatesOnId,
                                                       parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getGeographicDescriptionCode_service() != null ) {
            generateISOQP_GeographicDescriptionCode_ServiceStatement( isUpdate, connection, operatesOnId,
                                                                      parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getOperation() != null ) {
            generateISOQP_OperationStatement( isUpdate, connection, operatesOnId,
                                              parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getDenominator() != 0
             || ( parsedElement.getQueryableProperties().getDistanceValue() != 0 && parsedElement.getQueryableProperties().getDistanceUOM() != null ) ) {
            generateISOQP_SpatialResolutionStatement( isUpdate, connection, operatesOnId,
                                                      parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getOrganisationName() != null ) {
            generateISOQP_OrganisationNameStatement( isUpdate, connection, operatesOnId,
                                                     parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getResourceLanguage() != null ) {
            generateISOQP_ResourceLanguageStatement( isUpdate, connection, operatesOnId,
                                                     parsedElement.getQueryableProperties() );
        }

        if ( ( ( parsedElement.getQueryableProperties().getTemporalExtentBegin() != null && parsedElement.getQueryableProperties().getTemporalExtentEnd() != null ) ) ) {
            generateISOQP_TemporalExtentStatement( isUpdate, connection, operatesOnId,
                                                   parsedElement.getQueryableProperties() );
        }

        if ( parsedElement.getQueryableProperties().getOperatesOnData() != null ) {
            generateISOQP_OperatesOnStatement( isUpdate, connection, operatesOnId,
                                               parsedElement.getQueryableProperties() );
        }
        if ( parsedElement.getQueryableProperties().getCouplingType() != null ) {
            generateISOQP_CouplingTypeStatement( isUpdate, connection, operatesOnId,
                                                 parsedElement.getQueryableProperties() );
        }
        // TODO spatial
        LOG.info( "Boundingbox = null?? " + parsedElement.getQueryableProperties().getBoundingBox() );
        if ( parsedElement.getQueryableProperties().getBoundingBox() != null ) {
            generateISOQP_BoundingBoxStatement( isUpdate, connection, operatesOnId,
                                                parsedElement.getQueryableProperties() );
        }
        // if ( qp.getCrs() != null || qp.getCrs().size() != 0 ) {
        // generateISOQP_CRSStatement( isUpdate );
        // }

        generateADDQP_DegreeStatement( isUpdate, connection, operatesOnId, parsedElement.getQueryableProperties() );

        if ( parsedElement.getQueryableProperties().getLimitation() != null ) {
            generateADDQP_LimitationsStatement( isUpdate, connection, operatesOnId,
                                                parsedElement.getQueryableProperties() );
        }

        if ( parsedElement.getQueryableProperties().getLineage() != null ) {
            generateADDQP_LineageStatement( isUpdate, connection, operatesOnId, parsedElement.getQueryableProperties() );
        }

        if ( parsedElement.getQueryableProperties().getAccessConstraints() != null ) {
            generateADDQP_AccessConstraintsStatement( isUpdate, connection, operatesOnId,
                                                      parsedElement.getQueryableProperties() );
        }

        if ( parsedElement.getQueryableProperties().getOtherConstraints() != null ) {
            generateADDQP_OtherConstraintsStatement( isUpdate, connection, operatesOnId,
                                                     parsedElement.getQueryableProperties() );
        }

        if ( parsedElement.getQueryableProperties().getClassification() != null ) {
            generateADDQP_ClassificationStatement( isUpdate, connection, operatesOnId,
                                                   parsedElement.getQueryableProperties() );
        }

        if ( parsedElement.getQueryableProperties().getSpecificationTitle() != null ) {
            generateADDQP_SpecificationStatement( isUpdate, connection, operatesOnId,
                                                  parsedElement.getQueryableProperties() );
        }

    }

    /**
     * Puts the identifier for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateQP_IdentifierStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                 QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.qp_identifier.name();
        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String identifierString : qp.getIdentifier() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", identifier)" );

            s_POST.append( "'" + identifierString + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

        }

    }

    /**
     * Common procedure to execute a queryable property statement.
     * 
     * @param isUpdate
     *            if this is an update action
     * @param connection
     *            the SQL connection
     * @param operatesOnId
     *            on which dataset is working on
     * @param databaseTable
     *            the specific databasetable
     * @param queryablePropertyStatement_PRE
     *            the precondition to generate an executable statement
     * @param queryablePropertyStatement_POST
     *            the postcondition to generate an executable statement
     */
    private void executeQueryablePropertiesDatabasetables( boolean isUpdate, Connection connection, int operatesOnId,
                                                           String databaseTable, Writer queryablePropertyStatement_PRE,
                                                           Writer queryablePropertyStatement_POST ) {
        StringWriter sqlStatement = new StringWriter( 500 );
        PreparedStatement stm = null;

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE "
                                     + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + " = ?;" );
                stm = connection.prepareStatement( sqlStatement.toString() );
                stm.setObject( 1, operatesOnId );
                stm.executeUpdate();
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement = new StringWriter( 500 );
            sqlStatement.append( queryablePropertyStatement_PRE.toString() + " VALUES ( ?,?,"
                                 + queryablePropertyStatement_POST.toString() );

            stm = connection.prepareStatement( sqlStatement.toString() );
            stm.setObject( 1, localId );
            stm.setObject( 2, operatesOnId );
            stm.executeUpdate();
            stm.close();

            /*
             * clean the Writer...TODO is that really necessary?
             */
            StringBuffer buf = ( (StringWriter) queryablePropertyStatement_POST ).getBuffer();
            buf.setLength( 0 );
            buf = ( (StringWriter) queryablePropertyStatement_PRE ).getBuffer();
            buf.setLength( 0 );
            buf = sqlStatement.getBuffer();
            buf.setLength( 0 );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the anyText for this dataset into the database.
     * 
     * @param isUpdate
     */
    private String generateISOQP_AnyTextStatement( boolean isCaseSensitive, QueryableProperties qp,
                                                   ReturnableProperties rp ) {

        StringWriter anyText = new StringWriter();
        String stopWord = " # ";

        // Keywords
        for ( Keyword keyword : qp.getKeywords() ) {
            if ( keyword.getKeywordType() != null ) {
                anyText.append( keyword.getKeywordType() + stopWord );
            }
            if ( keyword.getThesaurus() != null ) {
                anyText.append( keyword.getThesaurus() + stopWord );
            }
            if ( keyword.getKeywords() != null ) {
                for ( String keywordString : keyword.getKeywords() ) {
                    anyText.append( keywordString + stopWord );
                }
            }
        }

        // title
        if ( qp.getTitle() != null ) {
            for ( String title : qp.getTitle() ) {
                anyText.append( title + stopWord );
            }
        }

        // abstract
        if ( qp.get_abstract() != null ) {
            for ( String _abstract : qp.get_abstract() ) {
                anyText.append( _abstract + stopWord );
            }
        }
        // format
        if ( qp.getFormat() != null ) {
            for ( Format format : qp.getFormat() ) {
                anyText.append( format.getName() + stopWord );
                // anyText.append( format.getVersion() + stopWord );
            }
        }

        // type
        anyText.append( qp.getType() + stopWord );

        // crs
        if ( qp.getCrs() != null ) {
            for ( CRS crs : qp.getCrs() ) {
                anyText.append( crs.getName() + stopWord );

            }
        }

        // creator
        anyText.append( rp.getCreator() + stopWord );

        // contributor
        anyText.append( rp.getContributor() + stopWord );

        // publisher
        anyText.append( rp.getPublisher() + stopWord );

        // language
        anyText.append( qp.getLanguage() + stopWord );

        // relation
        if ( rp.getRelation() != null ) {
            for ( String relation : rp.getRelation() ) {
                anyText.append( relation + stopWord );
            }
        }

        // rights
        if ( rp.getRights() != null ) {
            for ( String rights : rp.getRights() ) {
                anyText.append( rights + stopWord );
            }
        }

        // alternateTitle
        if ( qp.getAlternateTitle() != null ) {
            for ( String alternateTitle : qp.getAlternateTitle() ) {
                anyText.append( alternateTitle + stopWord );
            }
        }
        // organisationName
        anyText.append( qp.getOrganisationName() + stopWord );

        // topicCategory
        if ( qp.getTopicCategory() != null ) {
            for ( String topicCategory : qp.getTopicCategory() ) {
                anyText.append( topicCategory + stopWord );
            }
        }
        // resourceLanguage
        if ( qp.getResourceLanguage() != null ) {
            for ( String resourceLanguage : qp.getResourceLanguage() ) {
                anyText.append( resourceLanguage + stopWord );
            }
        }
        // geographicDescriptionCode
        anyText.append( qp.getGeographicDescriptionCode_service() + stopWord );

        // spatialResolution
        anyText.append( qp.getDistanceUOM() + stopWord );

        // serviceType
        anyText.append( qp.getServiceType() + stopWord );

        // operation
        if ( qp.getOperation() != null ) {
            for ( String operation : qp.getOperation() ) {
                anyText.append( operation + stopWord );
            }
        }

        // operatesOnData
        if ( qp.getOperatesOnData() != null ) {
            for ( OperatesOnData data : qp.getOperatesOnData() ) {
                anyText.append( data.getOperatesOn() + stopWord );
                anyText.append( data.getOperatesOnIdentifier() + stopWord );
                anyText.append( data.getOperatesOnName() + stopWord );

            }
        }

        // couplingType
        anyText.append( qp.getCouplingType() + stopWord );

        if ( isCaseSensitive == true ) {
            return anyText.toString();
        }
        return anyText.toString().toLowerCase();

    }

    /**
     * Puts the organisationname for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OrganisationNameStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                          QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_organisationname.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name() + ", "
                      + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", organisationname)" );

        s_POST.append( "'" + qp.getOrganisationName() + "');" );

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the temporalextent for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TemporalExtentStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                        QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_temporalextent.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );
        try {
            if ( ( qp.getTemporalExtentBegin() != null || !qp.getTemporalExtentBegin().equals( new Date( "0000-00-00" ) ) )
                 && ( qp.getTemporalExtentEnd() != null || !qp.getTemporalExtentEnd().equals( new Date( "0000-00-00" ) ) ) ) {

                s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                              + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name()
                              + ", tempextent_begin, tempextent_end)" );

                s_POST.append( "'" + qp.getTemporalExtentBegin() + "','" + qp.getTemporalExtentEnd() + "');" );
            }
        } catch ( ParseException e ) {

            e.printStackTrace();
        }

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the spatialresolution for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_SpatialResolutionStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                           QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_spatialresolution.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name() + ", "
                      + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name()
                      + ", denominator, distancevalue, distanceuom)" );

        s_POST.append( qp.getDenominator() + "," + qp.getDistanceValue() + ",'" + qp.getDistanceUOM() + "');" );

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the couplingtype for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_CouplingTypeStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                      QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_couplingtype.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name() + ", "
                      + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", couplingtype)" );

        s_POST.append( "'" + qp.getCouplingType() + "');" );

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the operatesondata for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OperatesOnStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                    QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_operatesondata.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );
        // String operatesOnString;

        for ( OperatesOnData operatesOnData : qp.getOperatesOnData() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name()
                          + ", operateson, operatesonidentifier, operatesonname)" );

            s_POST.append( "'" + operatesOnData.getOperatesOn() + "','" + operatesOnData.getOperatesOnIdentifier()
                           + "','" + operatesOnData.getOperatesOnName() + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the operation for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OperationStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                   QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_operation.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String operation : qp.getOperation() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", operation)" );

            s_POST.append( "'" + operation + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the geographicDescriptionCode for the type "service" for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_GeographicDescriptionCode_ServiceStatement( boolean isUpdate, Connection connection,
                                                                           int operatesOnId, QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_geographicdescriptioncode.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String geoDescCode : qp.getGeographicDescriptionCode_service() ) {

            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name()
                          + ", geographicdescriptioncode)" );

            s_POST.append( "'" + geoDescCode + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }
    }

    /**
     * Puts the servicetypeversion for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ServiceTypeVersionStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                            QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_servicetypeversion.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name() + ", "
                      + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", servicetypeversion)" );

        s_POST.append( "'" + qp.getServiceTypeVersion() + "');" );

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the servicetype for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ServiceTypeStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                     QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_servicetype.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name() + ", "
                      + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", servicetype)" );

        s_POST.append( "'" + qp.getServiceType() + "');" );

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the resourcelanguage for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ResourceLanguageStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                          QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_resourcelanguage.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name() + ", "
                      + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", resourcelanguage)" );

        s_POST.append( "'" + qp.getResourceLanguage() + "');" );

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the revisiondate for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_RevisionDateStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                      QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_revisiondate.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        try {
            if ( qp.getRevisionDate() != null || !qp.getRevisionDate().equals( new Date( "0000-00-00" ) ) ) {
                String revisionDateAttribute = "'" + qp.getRevisionDate() + "'";
                s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                              + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", revisiondate)" );

                s_POST.append( revisionDateAttribute + ");" );

                executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE,
                                                          s_POST );
            }
        } catch ( ParseException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the creationdate for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_CreationDateStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                      QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_creationdate.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        try {
            if ( qp.getCreationDate() != null || !qp.getCreationDate().equals( new Date( "0000-00-00" ) ) ) {
                String creationDateAttribute = "'" + qp.getCreationDate() + "'";
                s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                              + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", creationdate)" );

                s_POST.append( creationDateAttribute + ");" );

                executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE,
                                                          s_POST );
            }
        } catch ( ParseException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the publicationdate for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_PublicationDateStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                         QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_publicationdate.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        try {
            if ( qp.getPublicationDate() == null || qp.getPublicationDate().equals( new Date( "0000-00-00" ) ) ) {
                String publicationDateAttribute = "'" + qp.getPublicationDate() + "'";
                s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                              + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", publicationdate)" );

                s_POST.append( publicationDateAttribute + ");" );

                executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE,
                                                          s_POST );
            }
        } catch ( ParseException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the resourceIdentifier for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ResourceIdentifierStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                            QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_resourceidentifier.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String resourceId : qp.getResourceIdentifier() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", resourceidentifier)" );

            s_POST.append( "'" + resourceId + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the alternatetitle for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_AlternateTitleStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                        QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_alternatetitle.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String alternateTitle : qp.getAlternateTitle() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", alternatetitle)" );

            s_POST.append( "'" + alternateTitle + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the title for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TitleStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                               QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_title.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String title : qp.getTitle() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", title)" );

            s_POST.append( "'" + title + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the type for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TypeStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                              QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_type.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name() + ", "
                      + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", type)" );

        s_POST.append( "'" + qp.getType() + "');" );

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the keyword for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_KeywordStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                 QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_keyword.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( Keyword keyword : qp.getKeywords() ) {

            for ( String keywordString : keyword.getKeywords() ) {
                s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                              + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name()
                              + ", keywordtype, keyword, thesaurus)" );

                s_POST.append( "'" + keyword.getKeywordType() + "','" + keywordString + "','" + keyword.getThesaurus()
                               + "');" );

                executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE,
                                                          s_POST );
            }
        }

    }

    /**
     * Puts the topiccategory for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TopicCategoryStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                       QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_topiccategory.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String topicCategory : qp.getTopicCategory() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", topiccategory)" );

            s_POST.append( "'" + topicCategory + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the format for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_FormatStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_format.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( Format format : qp.getFormat() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", "
                          + PostGISMappingsISODC.CommonColumnNames.format.name() + ")" );

            s_POST.append( "'" + format.getName() + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the abstract for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_AbstractStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                  QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_abstract.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String _abstract : qp.get_abstract() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", abstract)" );

            s_POST.append( "'" + _abstract + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the degree for this dataset into the database.
     * 
     * @param isUpdate
     * @param connection
     * @param stm
     * @param operatesOnId
     * @param qp
     */
    private void generateADDQP_DegreeStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.addqp_degree.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name() + ", "
                      + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", degree)" );

        s_POST.append( "'" + qp.isDegree() + "');" );

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the lineage for this dataset into the database.
     * 
     * @param isUpdate
     * @param connection
     * @param stm
     * @param operatesOnId
     * @param qp
     */
    private void generateADDQP_LineageStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                 QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.addqp_lineage.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name() + ", "
                      + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", lineage)" );

        s_POST.append( "'" + qp.getLineage() + "');" );

        executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

    }

    /**
     * Puts the specification for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateADDQP_SpecificationStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                       QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.addqp_specification.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String specificationTitle : qp.getSpecificationTitle() ) {

            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name()
                          + ", specificationTitle, specificationDateType, specificationDate)" );

            s_POST.append( "'" + specificationTitle + "','" + qp.getSpecificationDateType() + "','"
                           + qp.getSpecificationDate() + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }
    }

    /**
     * Puts the accessConstraints for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateADDQP_AccessConstraintsStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                           QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.addqp_accessconstraint.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String accessConstraint : qp.getAccessConstraints() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", accessconstraint)" );

            s_POST.append( "'" + accessConstraint + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the limitation for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateADDQP_LimitationsStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                     QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.addqp_limitation.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String limitation : qp.getLimitation() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", limitation)" );

            s_POST.append( "'" + limitation + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the otherConstaints for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateADDQP_OtherConstraintsStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                          QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.addqp_otherconstraint.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String otherConstraint : qp.getOtherConstraints() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", otherConstraint)" );

            s_POST.append( "'" + otherConstraint + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );
        }

    }

    /**
     * Puts the classification for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateADDQP_ClassificationStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                        QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.addqp_classification.name();

        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );

        for ( String classification : qp.getClassification() ) {
            s_PRE.append( "INSERT INTO " + databaseTable + " (" + PostGISMappingsISODC.CommonColumnNames.id.name()
                          + ", " + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + ", classification)" );

            s_POST.append( "'" + classification + "');" );

            executeQueryablePropertiesDatabasetables( isUpdate, connection, operatesOnId, databaseTable, s_PRE, s_POST );

        }

    }

    /**
     * Puts the boundingbox for this dataset into the database.
     * 
     * @param isUpdate
     */
    // TODO one record got one or more bboxes?
    private void generateISOQP_BoundingBoxStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                     QueryableProperties qp ) {
        final String databaseTable = PostGISMappingsISODC.DatabaseTables.isoqp_BoundingBox.name();
        StringWriter sqlStatement = new StringWriter( 500 );
        PreparedStatement stm = null;
        double east = qp.getBoundingBox().getEastBoundLongitude();
        double north = qp.getBoundingBox().getNorthBoundLatitude();
        double west = qp.getBoundingBox().getWestBoundLongitude();
        double south = qp.getBoundingBox().getSouthBoundLatitude();

        int localId = 0;
        try {

            if ( isUpdate == false ) {
                localId = getLastDatasetId( connection, databaseTable );
                localId++;
                sqlStatement.append( "INSERT INTO " ).append( databaseTable ).append( '(' );
                sqlStatement.append( PostGISMappingsISODC.CommonColumnNames.id.name() ).append( ',' );
                sqlStatement.append( PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() );
                sqlStatement.append( ", bbox) VALUES (" + localId ).append( "," + operatesOnId );
                sqlStatement.append( ",SetSRID('BOX3D(" + east ).append( " " + north ).append( "," + west );
                sqlStatement.append( " " + south ).append( ")'::box3d,-1));" );
            } else {
                sqlStatement.append( "UPDATE " ).append( databaseTable ).append( " SET bbox = SetSRID('BOX3D(" + east );
                sqlStatement.append( " " + north ).append( "," + west ).append( " " + south );
                sqlStatement.append( ")'::box3d,-1) WHERE " );
                sqlStatement.append( PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() );
                sqlStatement.append( " = " + operatesOnId + ";" );
            }
            stm = connection.prepareStatement( sqlStatement.toString() );
            LOG.debug( "boundinbox: " + stm );
            stm.executeUpdate();
            stm.close();

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    // /**
    // * Puts the crs for this dataset into the database.<br>
    // * Creation of the CRS element. <br>
    // * TODO its not clear where to get all the elements...
    // *
    // * @param isUpdate
    // */
    // private void generateISOQP_CRSStatement( boolean isUpdate, Connection connection, Statement stm, int
    // operatesOnId,
    // QueryableProperties qp ) {
    // final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_crs.name();
    //
    // StringWriter s_PRE = new StringWriter( 200 );
    // StringWriter s_POST = new StringWriter( 50 );
    //
    // for ( CRS crs : qp.getCrs() ) {
    // s_PRE.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, authority, id_crs, version)" );
    //
    // s_POST.append( crs.getName() + "," + CRS.EPSG_4326 + "," + crs.getName() + ");" );
    //
    // executeQueryablePropertiesDatabasetables( isUpdate, connection, stm, operatesOnId, databaseTable, s_PRE,
    // s_POST );
    // }
    //
    // }

    /**
     * Provides the last known id in the databaseTable. So it is possible to insert new datasets into this table come
     * from this id.
     * 
     * @param conn
     * @param databaseTable
     *            the databaseTable that is requested.
     * @return the last Primary Key ID of the databaseTable.
     * @throws SQLException
     */
    private int getLastDatasetId( Connection conn, String databaseTable )
                            throws SQLException {
        int result = 0;
        String selectIDRows = "SELECT " + PostGISMappingsISODC.CommonColumnNames.id.name() + " from " + databaseTable
                              + " ORDER BY " + PostGISMappingsISODC.CommonColumnNames.id.name() + " DESC LIMIT 1";
        ResultSet rsBrief = conn.createStatement().executeQuery( selectIDRows );

        while ( rsBrief.next() ) {

            result = rsBrief.getInt( 1 );

        }
        rsBrief.close();
        return result;

    }

}
