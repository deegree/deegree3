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
package org.deegree.metadata.persistence.iso.generating;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.cs.CRSCodeType;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.iso.MSSQLMappingsISODC;
import org.deegree.metadata.persistence.iso.PostGISMappingsISODC;
import org.deegree.metadata.persistence.iso.parsing.QueryableProperties;
import org.deegree.metadata.persistence.types.BoundingBox;
import org.deegree.metadata.persistence.types.Format;
import org.deegree.metadata.persistence.types.Keyword;
import org.deegree.metadata.persistence.types.OperatesOnData;
import org.deegree.metadata.persistence.types.inspire.Constraint;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;

/**
 * Here are all the queryable properties encapsulated which have to put into the backend. Here is the functionality of
 * the INSERT and UPDATE action.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenerateQueryableProperties {

    private static final Logger LOG = getLogger( GenerateQueryableProperties.class );

    private String idColumn;

    private String fileIdColumn;

    private String recordColumn;

    private String fk_main;

    private Type connectionType;

    private String mainTable;

    private String crsTable;

    private String keywordTable;

    private String constraintTable;

    private String opOnTable;

    public GenerateQueryableProperties( Type dbtype ) {
        this.connectionType = dbtype;
        if ( connectionType == Type.PostgreSQL ) {
            idColumn = PostGISMappingsISODC.CommonColumnNames.id.name();
            fk_main = PostGISMappingsISODC.CommonColumnNames.fk_main.name();
            recordColumn = PostGISMappingsISODC.CommonColumnNames.recordfull.name();
            fileIdColumn = PostGISMappingsISODC.CommonColumnNames.fileidentifier.name();
            mainTable = PostGISMappingsISODC.DatabaseTables.idxtb_main.name();
            crsTable = PostGISMappingsISODC.DatabaseTables.idxtb_crs.name();
            keywordTable = PostGISMappingsISODC.DatabaseTables.idxtb_keyword.name();
            opOnTable = PostGISMappingsISODC.DatabaseTables.idxtb_operatesondata.name();
            constraintTable = PostGISMappingsISODC.DatabaseTables.idxtb_constraint.name();
        }
        if ( connectionType == Type.MSSQL ) {
            idColumn = MSSQLMappingsISODC.CommonColumnNames.id.name();
            fk_main = MSSQLMappingsISODC.CommonColumnNames.fk_main.name();
            recordColumn = PostGISMappingsISODC.CommonColumnNames.recordfull.name();
            fileIdColumn = PostGISMappingsISODC.CommonColumnNames.fileidentifier.name();
            mainTable = PostGISMappingsISODC.DatabaseTables.idxtb_main.name();
            crsTable = PostGISMappingsISODC.DatabaseTables.idxtb_crs.name();
            keywordTable = PostGISMappingsISODC.DatabaseTables.idxtb_keyword.name();
            opOnTable = PostGISMappingsISODC.DatabaseTables.idxtb_operatesondata.name();
            constraintTable = PostGISMappingsISODC.DatabaseTables.idxtb_constraint.name();
        }
    }

    /**
     * Generates and inserts the maindatabasetable that is needed for the queryable properties databasetables to derive
     * from.
     * <p>
     * BE AWARE: the "modified" attribute is get from the first position in the list. The backend has the possibility to
     * add one such attribute. In the xsd-file there are more possible...
     * 
     * @param connection
     *            the SQL connection
     * @return the primarykey of the inserted dataset which is the foreignkey for the queryable properties
     *         databasetables
     * @throws MetadataStoreException
     * @throws XMLStreamException
     */
    public int generateMainDatabaseDataset( Connection connection, ISORecord rec )
                            throws MetadataStoreException, XMLStreamException {

        int operatesOnId = 0;
        StringWriter sqlStatement = new StringWriter( 500 );
        try {

            operatesOnId = getLastDatasetId( connection, mainTable );
            operatesOnId++;

            StringWriter s_columns = new StringWriter( 200 );
            StringWriter s_values = new StringWriter( 500 );
            createQPInsertPart( rec, s_columns, s_values );
            s_columns.append( idColumn ).append( ',' ).append( recordColumn ).append( ",fileidentifier,version,status" );
            s_values.append( "?,?,?,?,?" );

            // concatenate all
            PreparedStatement stm = null;

            sqlStatement.append( "INSERT INTO " ).append( mainTable );
            sqlStatement.append( " (" ).append( s_columns.toString() ).append( ')' );
            sqlStatement.append( " VALUES (" ).append( s_values.toString() ).append( ')' );
            stm = connection.prepareStatement( sqlStatement.toString() );

            stm.setInt( 1, operatesOnId );
            stm.setBytes( 2, rec.getAsByteArray() );
            stm.setString( 3, rec.getIdentifier() );
            stm.setObject( 4, null );
            stm.setObject( 5, null );

            LOG.debug( stm.toString() );
            stm.executeUpdate();
            stm.close();
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", sqlStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        }
        return operatesOnId;
    }

    /**
     * 
     * @param conn
     *            the database connection
     * @param rec
     *            the record to update
     * @param fileIdentifier
     *            the fileIdentifer of the record to update, can be <code>null</code> when the identifer of the record
     *            is the one to use for updating
     * @return the database id of the updated record
     * @throws MetadataStoreException
     *             if updating fails
     */
    public int updateMainDatabaseTable( Connection conn, ISORecord rec, String fileIdentifier )
                            throws MetadataStoreException {
        PreparedStatement stm = null;
        ResultSet rs = null;

        StringWriter s = new StringWriter( 150 );
        String time = null;
        int requestedId = 0;

        String idToUpdate = ( fileIdentifier == null ? rec.getIdentifier() : fileIdentifier );
        try {
            s.append( "SELECT " ).append( idColumn );
            s.append( " FROM " ).append( mainTable );
            s.append( " WHERE " ).append( fileIdColumn ).append( " = ?" );
            LOG.debug( s.toString() );

            stm = conn.prepareStatement( s.toString() );
            stm.setObject( 1, idToUpdate );
            rs = stm.executeQuery();
            s = new StringWriter( 500 );

            while ( rs.next() ) {
                requestedId = rs.getInt( 1 );
                LOG.debug( "resultSet: " + rs.getInt( 1 ) );
            }

            if ( requestedId != 0 ) {
                s.append( "UPDATE " ).append( mainTable ).append( " SET " );

                createQPUpdatePart( rec, s );

                s.append( " version = ?, " );
                s.append( " status = ?, " );
                s.append( " modified = ?, " );
                s.append( recordColumn ).append( " = ? " );

                s.append( "WHERE " );
                s.append( idColumn ).append( '=' );
                s.append( Integer.toString( requestedId ) );
                stm = conn.prepareStatement( s.toString() );

                stm.setObject( 1, null );
                stm.setObject( 2, null );
                // TODO should be anyText
                if ( rec.getModified() != null ) {
                    time = rec.getModified().toString();
                    stm.setTimestamp( 3,
                                      Timestamp.valueOf( DateUtils.formatJDBCTimeStamp( DateUtils.parseISO8601Date( time ) ) ) );
                } else {
                    stm.setTimestamp( 3, null );
                }
                stm.setBytes( 4, rec.getAsByteArray() );
                LOG.debug( stm.toString() );
                stm.executeUpdate();
                stm.close();
            }
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", s.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } catch ( ParseException e ) {
            String msg = Messages.getMessage( "ERROR_PARSING", time, e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } catch ( FactoryConfigurationError e ) {
            LOG.debug( "error: " + e.getMessage(), e );
            throw new MetadataStoreException( e.getMessage() );
        } finally {
            JDBCUtils.close( rs );
        }
        return requestedId;
    }

    private void createQPInsertPart( ISORecord rec, StringWriter s_columns, StringWriter s_values ) {
        QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
        append( s_columns, s_values, rec.getAbstract(), "abstract" );
        append( s_columns, s_values, rec.getAnyText(), "anytext" );
        append( s_columns, s_values, rec.getLanguage(), "language" );
        append( s_columns, s_values, rec.getModified(), "modified" );
        append( s_columns, s_values, rec.getParentIdentifier(), "parentid" );
        append( s_columns, s_values, rec.getType(), "type" );
        append( s_columns, s_values, rec.getTitle(), "title" );
        append( s_columns, s_values, rec.isHasSecurityConstraints(), "hassecurityconstraints" );
        append( s_columns, s_values, qp.getTopicCategory(), "topiccategories" );
        append( s_columns, s_values, qp.getAlternateTitle(), "alternateTitles" );
        append( s_columns, s_values, qp.getRevisionDate(), "revisiondate" );
        append( s_columns, s_values, qp.getCreationDate(), "creationdate" );
        append( s_columns, s_values, qp.getPublicationDate(), "publicationdate" );
        append( s_columns, s_values, qp.getOrganisationName(), "organisationname" );
        append( s_columns, s_values, qp.getResourceIdentifier(), "resourceid" );
        append( s_columns, s_values, qp.getResourceLanguage(), "resourcelanguage" );
        append( s_columns, s_values, qp.getGeographicDescriptionCode_service(), "geographicdescriptioncode" );
        append( s_columns, s_values, qp.getDenominator(), "denominator" );
        append( s_columns, s_values, qp.getDistanceValue(), "distancevalue" );
        append( s_columns, s_values, qp.getDistanceUOM(), "distanceuom" );
        append( s_columns, s_values, qp.getTemporalExtentBegin(), "tempextent_begin" );
        append( s_columns, s_values, qp.getTemporalExtentEnd(), "tempextent_end" );
        append( s_columns, s_values, qp.getServiceType(), "servicetype" );
        append( s_columns, s_values, qp.getServiceTypeVersion(), "servicetypeversion" );
        append( s_columns, s_values, qp.getCouplingType(), "couplingtype" );
        List<Format> list = qp.getFormat();
        if ( list != null && list.size() > 0 ) {
            s_columns.append( "formats" ).append( ',' );
            s_values.append( '\'' );
            for ( Format f : list ) {
                s_values.append( '|' ).append( f.getName() );
            }
            if ( !list.isEmpty() ) {
                s_values.append( "|" );
            }
            s_values.append( "'," );
        }
        append( s_columns, s_values, qp.getOperation(), "operations" );
        append( s_columns, s_values, qp.isDegree(), "degree" );
        append( s_columns, s_values, qp.getLineages(), "lineage" );
        append( s_columns, s_values, qp.getRespPartyRole(), "resppartyrole" );
        append( s_columns, s_values, qp.getSpecificationTitle(), "spectitle" );
        append( s_columns, s_values, qp.getSpecificationDate(), "specdate" );
        append( s_columns, s_values, qp.getSpecificationDateType(), "specdatetype" );
        // append( s_PRE, s_POST, qp.get, "data" );
        BoundingBox bbox = calculateMainBBox( qp.getBoundingBox() );
        if ( bbox != null ) {
            LOG.debug( "Boundingbox = " + qp.getBoundingBox() );
            double east = bbox.getEastBoundLongitude();
            double north = bbox.getNorthBoundLatitude();
            double west = bbox.getWestBoundLongitude();
            double south = bbox.getSouthBoundLatitude();
            if ( connectionType == Type.MSSQL ) {
                s_columns.append( "bbox" ).append( ',' );
                s_values.append( "geometry::STGeomFromText('POLYGON((" + west ).append( " " + south );
                s_values.append( "," + west ).append( " " + north );
                s_values.append( "," + east ).append( " " + north );
                s_values.append( "," + east ).append( " " + south );
                s_values.append( "," + west ).append( " " + south ).append( "))', 0)" ).append( ',' );
            } else if ( connectionType == Type.PostgreSQL ) {
                s_columns.append( "bbox" ).append( ',' );
                s_values.append( "SetSRID('BOX3D(" + west ).append( " " + south ).append( "," + east );
                s_values.append( " " + north ).append( ")'::box3d,-1)" ).append( ',' );
            }
        }
    }

    private void createQPUpdatePart( ISORecord rec, StringWriter s ) {
        QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
        appendUpdate( s, Arrays.asList( rec.getAbstract() ), "abstract" );
        appendUpdate( s, rec.getAnyText(), "anytext" );
        appendUpdate( s, rec.getIdentifier(), "fileidentifier" );
        // appendUpdate( s, rec.getModified(), "modified" );
        appendUpdate( s, rec.getLanguage(), "language" );
        appendUpdate( s, rec.getParentIdentifier(), "parentid" );
        appendUpdate( s, rec.getType(), "type" );
        appendUpdate( s, Arrays.asList( rec.getTitle() ), "title" );
        s.append( "hassecurityconstraints=" ).append( Boolean.toString( rec.isHasSecurityConstraints() ) ).append( ',' );
        appendUpdate( s, qp.getTopicCategory(), "topiccategories" );
        appendUpdate( s, qp.getAlternateTitle(), "alternateTitles" );

        appendUpdate( s, qp.getRevisionDate(), "revisiondate" );
        appendUpdate( s, qp.getCreationDate(), "creationdate" );
        appendUpdate( s, qp.getPublicationDate(), "publicationdate" );
        appendUpdate( s, qp.getOrganisationName(), "organisationname" );
        appendUpdate( s, qp.getResourceIdentifier(), "resourceid" );
        appendUpdate( s, qp.getResourceLanguage(), "resourcelanguage" );
        appendUpdate( s, qp.getGeographicDescriptionCode_service(), "geographicdescriptioncode" );
        s.append( "denominator=" ).append( Integer.toString( qp.getDenominator() ) ).append( ',' );
        s.append( "distancevalue=" ).append( Float.isNaN( qp.getDistanceValue() ) ? "null"
                                                                                 : Float.toString( qp.getDistanceValue() ) ).append( ',' );

        appendUpdate( s, qp.getDistanceUOM(), "distanceuom" );
        appendUpdate( s, qp.getTemporalExtentBegin(), "tempextent_begin" );
        appendUpdate( s, qp.getTemporalExtentEnd(), "tempextent_end" );
        appendUpdate( s, qp.getServiceType(), "servicetype" );
        appendUpdate( s, qp.getServiceTypeVersion(), "servicetypeversion" );
        appendUpdate( s, qp.getCouplingType(), "couplingtype" );
        List<Format> list = qp.getFormat();
        if ( list != null && list.size() > 0 ) {
            s.append( "formats='" );
            for ( Format f : list ) {
                s.append( '|' ).append( f.getName() );
            }
            if ( !list.isEmpty() ) {
                s.append( "|" );
            }
            s.append( "'," );
        } else {
            s.append( "formats=null" );
        }
        appendUpdate( s, qp.getOperation(), "operations" );
        s.append( "degree=" ).append( Boolean.toString( qp.isDegree() ) ).append( ',' );
        appendUpdate( s, qp.getLineages(), "lineage" );
        appendUpdate( s, qp.getRespPartyRole(), "resppartyrole" );
        appendUpdate( s, qp.getSpecificationTitle(), "spectitle" );
        appendUpdate( s, qp.getSpecificationDate(), "specdate" );
        appendUpdate( s, qp.getSpecificationDateType(), "specdatetype" );
        BoundingBox bbox = calculateMainBBox( qp.getBoundingBox() );
        if ( bbox != null ) {
            LOG.debug( "Boundingbox = " + qp.getBoundingBox() );
            double east = bbox.getEastBoundLongitude();
            double north = bbox.getNorthBoundLatitude();
            double west = bbox.getWestBoundLongitude();
            double south = bbox.getSouthBoundLatitude();
            if ( connectionType == Type.MSSQL ) {
                s.append( "bbox=" );
                s.append( "geometry::STGeomFromText('POLYGON((" + west ).append( " " + south );
                s.append( "," + west ).append( " " + north );
                s.append( "," + east ).append( " " + north );
                s.append( "," + east ).append( " " + south );
                s.append( "," + west ).append( " " + south ).append( "))', 0)" ).append( ',' );
            } else if ( connectionType == Type.PostgreSQL ) {
                s.append( "bbox=" );
                s.append( "SetSRID('BOX3D(" + west ).append( " " + south ).append( "," + east ).append( " " + north ).append( ")'::box3d,-1)" ).append( ',' );
            }
        }
    }

    private void appendUpdate( StringWriter s, String value, String column ) {
        s.append( column ).append( '=' );
        if ( value == null )
            s.append( "null" );
        else
            s.append( '\'' ).append( value ).append( '\'' );
        s.append( ',' );
    }

    private void appendUpdate( StringWriter s, List<String> values, String column ) {
        s.append( column ).append( '=' );
        if ( values == null || values.isEmpty() )
            s.append( "null" );
        else
            s.append( '\'' ).append( concatenate( values ) ).append( '\'' );
        s.append( ',' );
    }

    private void appendUpdate( StringWriter s, Date date, String column ) {
        s.append( column ).append( '=' );
        if ( date == null )
            s.append( "null" );
        else
            s.append( "'" + date + "'" );
        s.append( ',' );
    }

    private BoundingBox calculateMainBBox( List<BoundingBox> bbox ) {
        if ( bbox == null || bbox.isEmpty() )
            return null;
        if ( bbox.size() == 1 )
            return bbox.get( 0 );
        double west = bbox.get( 0 ).getWestBoundLongitude();
        double east = bbox.get( 0 ).getEastBoundLongitude();
        double south = bbox.get( 0 ).getSouthBoundLatitude();
        double north = bbox.get( 0 ).getNorthBoundLatitude();
        for ( BoundingBox b : bbox ) {
            west = Math.min( west, b.getWestBoundLongitude() );
            east = Math.max( east, b.getEastBoundLongitude() );
            south = Math.min( south, b.getSouthBoundLatitude() );
            north = Math.max( north, b.getNorthBoundLatitude() );
        }
        return new BoundingBox( west, south, east, north );
    }

    /**
     * Method that encapsulates the generating for all the queryable properties.
     * 
     * @param isUpdate
     * @param connection
     * @param operatesOnId
     * @throws MetadataStoreException
     */
    public void executeQueryableProperties( boolean isUpdate, Connection connection, int operatesOnId, ISORecord rec )
                            throws MetadataStoreException {
        // createMainTableStm( isUpdate, rec, operatesOnId, connection );

        QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
        generateIDXTB_CRSStatement( isUpdate, connection, operatesOnId, qp );
        generateIDXTB_KeywordStatement( isUpdate, connection, operatesOnId, qp );
        generateIDXTB_OperatesOnStatement( isUpdate, connection, operatesOnId, qp );
        generateIDXTB_ConstraintStatement( isUpdate, connection, operatesOnId, qp );
    }

    private void generateIDXTB_ConstraintStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                    QueryableProperties qp )
                            throws MetadataStoreException {
        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );
        List<Constraint> constraintss = qp.getConstraints();
        if ( constraintss != null && constraintss.size() > 0 )
            for ( Constraint constraint : constraintss ) {
                s_PRE.append( "INSERT INTO " ).append( constraintTable );
                s_PRE.append( '(' ).append( idColumn ).append( ',' ).append( fk_main ).append( ",conditionapptoacc,accessconstraints,otherconstraints,classification)" );
                append( s_POST, constraint.getLimitations() ).append( ',' );
                append( s_POST, constraint.getAccessConstraints() ).append( ',' );
                append( s_POST, constraint.getOtherConstraints() ).append( ',' );
                append( s_POST, constraint.getClassification() ).append( ");" );
                executeQPDatabasetables( isUpdate, connection, operatesOnId, constraintTable, s_PRE, s_POST );
            }
    }

    private void generateIDXTB_CRSStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                             QueryableProperties qp )
                            throws MetadataStoreException {
        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );
        List<CRSCodeType> crss = qp.getCrs();
        if ( crss != null && crss.size() > 0 )
            for ( CRSCodeType crs : crss ) {
                s_PRE.append( "INSERT INTO " ).append( crsTable );
                s_PRE.append( '(' ).append( idColumn ).append( ',' ).append( fk_main ).append( ", authority, crsid, version)" );
                append( s_POST, crs.getCodeSpace() ).append( ',' );
                append( s_POST, crs.getCode() ).append( ',' );
                append( s_POST, crs.getCodeVersion() ).append( ");" );
                executeQPDatabasetables( isUpdate, connection, operatesOnId, crsTable, s_PRE, s_POST );
            }
    }

    private StringWriter append( StringWriter s, String value ) {
        if ( value != null ) {
            s.append( '\'' ).append( value ).append( '\'' );
        } else {
            s.append( "null" );
        }
        return s;
    }

    private StringWriter append( StringWriter s, List<String> value ) {
        if ( value != null && value.size() > 0 ) {
            s.append( '\'' ).append( concatenate( value ) ).append( '\'' );
        } else {
            s.append( "null" );
        }
        return s;
    }

    private void generateIDXTB_KeywordStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                 QueryableProperties qp )
                            throws MetadataStoreException {
        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );
        List<Keyword> keywords = qp.getKeywords();
        if ( keywords != null && keywords.size() > 0 )
            for ( Keyword keyword : keywords ) {
                s_PRE.append( "INSERT INTO " ).append( keywordTable );
                s_PRE.append( '(' ).append( idColumn ).append( ',' ).append( fk_main ).append( ", keywords, keywordtype)" );

                s_POST.append( "'" ).append( concatenate( keyword.getKeywords() ) ).append( "','" );
                s_POST.append( keyword.getKeywordType() ).append( "');" );
                executeQPDatabasetables( isUpdate, connection, operatesOnId, keywordTable, s_PRE, s_POST );
            }
    }

    private void generateIDXTB_OperatesOnStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                    QueryableProperties qp )
                            throws MetadataStoreException {
        StringWriter s_PRE = new StringWriter( 200 );
        StringWriter s_POST = new StringWriter( 50 );
        List<OperatesOnData> opOns = qp.getOperatesOnData();
        if ( opOns != null && opOns.size() > 0 )
            for ( OperatesOnData opOn : opOns ) {
                s_PRE.append( "INSERT INTO " ).append( opOnTable );
                s_PRE.append( '(' ).append( idColumn ).append( ',' ).append( fk_main ).append( ", operateson, operatesonid, operatesonname )" );

                s_POST.append( "'" ).append( opOn.getOperatesOnId() ).append( "','" );
                s_POST.append( opOn.getOperatesOnId() ).append( "','" );
                s_POST.append( opOn.getOperatesOnName() ).append( "');" );
                executeQPDatabasetables( isUpdate, connection, operatesOnId, opOnTable, s_PRE, s_POST );
            }
    }

    private void append( StringWriter s_PRE, StringWriter s_POST, boolean value, String dbColumn ) {
        s_PRE.append( dbColumn ).append( ',' );
        s_POST.append( Boolean.toString( value ) ).append( ',' );
    }

    private void append( StringWriter s_PRE, StringWriter s_POST, Number value, String dbColumn ) {
        s_PRE.append( dbColumn ).append( ',' );
        s_POST.append( "" + value ).append( ',' );
    }

    private void append( StringWriter s_PRE, StringWriter s_POST, Date date, String dbColumn ) {
        if ( date != null ) {
            s_PRE.append( dbColumn ).append( ',' );
            s_POST.append( "'" + date + "'" ).append( ',' );
        }
    }

    private void append( StringWriter s_PRE, StringWriter s_POST, String[] value, String dbColumn ) {
        if ( value != null && value.length > 0 ) {
            s_PRE.append( dbColumn ).append( ',' );
            s_POST.append( "'" + concatenate( Arrays.asList( value ) ) + "'" ).append( ',' );
        }
    }

    private void append( StringWriter s_PRE, StringWriter s_POST, List<String> value, String dbColumn ) {
        if ( value != null && value.size() > 0 ) {
            s_PRE.append( dbColumn ).append( ',' );
            s_POST.append( "'" + concatenate( value ) + "'" ).append( ',' );
        }
    }

    private void append( StringWriter s_PRE, StringWriter s_POST, String value, String dbColumn ) {
        if ( value != null ) {
            s_PRE.append( dbColumn ).append( ',' );
            s_POST.append( "'" + stringInspectation( value ) + "'" ).append( ',' );
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
     * @throws MetadataStoreException
     */
    private void executeQPDatabasetables( boolean isUpdate, Connection connection, int operatesOnId,
                                          String databaseTable, Writer queryablePropertyStatement_PRE,
                                          Writer queryablePropertyStatement_POST )
                            throws MetadataStoreException {
        StringWriter sqlStatement = new StringWriter( 500 );
        PreparedStatement stm = null;

        int localId = 0;
        try {

            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE " + fk_main + " = ?;" );
                stm = connection.prepareStatement( sqlStatement.toString() );
                stm.setInt( 1, operatesOnId );
                LOG.debug( stm.toString() );
                stm.executeUpdate();
            }
            sqlStatement = new StringWriter( 500 );
            sqlStatement.append( queryablePropertyStatement_PRE.toString() + " VALUES ( ?,?,"
                                 + queryablePropertyStatement_POST.toString() );

            stm = connection.prepareStatement( sqlStatement.toString() );
            LOG.debug( stm.toString() );
            stm.setInt( 1, localId );
            stm.setInt( 2, operatesOnId );
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
            String msg = Messages.getMessage( "ERROR_SQL", sqlStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        }

    }

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
        String selectIDRows = null;
        if ( connectionType == Type.PostgreSQL ) {
            selectIDRows = "SELECT " + idColumn + " from " + databaseTable + " ORDER BY " + idColumn + " DESC LIMIT 1";
        }
        if ( connectionType == Type.MSSQL ) {
            selectIDRows = "SELECT TOP 1 " + idColumn + " from " + databaseTable + " ORDER BY " + idColumn + " DESC";
        }
        ResultSet rsBrief = conn.createStatement().executeQuery( selectIDRows );

        while ( rsBrief.next() ) {

            result = rsBrief.getInt( 1 );

        }
        rsBrief.close();
        return result;

    }

    private String stringInspectation( String input ) {
        if ( input != null ) {
            input = input.replace( "\'", "\'\'" );
        }
        return input;
    }

    private String concatenate( List<String> values ) {
        String s = "";
        for ( String value : values ) {
            s = s + '|' + stringInspectation( value );
        }
        if ( !values.isEmpty() )
            s = s + '|';
        return s;
    }

}
