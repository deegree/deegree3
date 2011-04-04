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
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.CRSCodeType;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.iso.MSSQLMappingsISODC;
import org.deegree.metadata.persistence.iso.PostGISMappingsISODC;
import org.deegree.metadata.persistence.iso.parsing.QueryableProperties;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;
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

    private AnyText anyTextConfig;

    public GenerateQueryableProperties( Type dbtype, AnyText anyTextConfig ) {
        this.connectionType = dbtype;
        this.anyTextConfig = anyTextConfig;
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
            s_columns.append( idColumn ).append( ',' ).append( recordColumn ).append( ",fileidentifier,version,status," );
            s_values.append( "?,?,?,?,?," );
            s_columns.append( "abstract, anytext, language, modified, parentid, type, title, hassecurityconstraints, topiccategories, " );
            s_values.append( "?,?,?,?,?,?,?,?,?," );
            s_columns.append( "alternateTitles, revisiondate, creationdate, publicationdate, organisationname, resourceid, resourcelanguage, " );
            s_values.append( "?,?,?,?,?,?,?," );
            s_columns.append( "geographicdescriptioncode, denominator, distancevalue, distanceuom, tempextent_begin, tempextent_end, " );
            s_values.append( "?,?,?,?,?,?," );
            s_columns.append( "servicetype, servicetypeversion,  couplingtype, formats, operations,  degree, lineage, resppartyrole, " );
            s_values.append( "?,?,?,?,?,?,?,?," );
            s_columns.append( "spectitle, specdate, specdatetype " );
            s_values.append( "?,?,?" );
            String bbox = getBBox( rec.getParsedElement().getQueryableProperties().getBoundingBox() );
            if ( bbox != null && bbox.length() > 0 ) {
                s_columns.append( ", bbox" );
                s_values.append( ',' ).append( bbox );
            }

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

            appendValues( rec, stm, 6 );

            LOG.debug( stm.toString() );
            stm.executeUpdate();
            stm.close();
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", sqlStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } catch ( ParseException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", sqlStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        }
        return operatesOnId;
    }

    private void appendValues( ISORecord rec, PreparedStatement stm, int next )
                            throws SQLException, ParseException {
        // abstract, anytext, language, modified, parentid, type, title, hassecurityconstraints, topiccategories,
        stm.setString( next++, concatenate( Arrays.asList( rec.getAbstract() ) ) );
        stm.setString( next++, rec.getAnyText( anyTextConfig ) );
        stm.setString( next++, rec.getLanguage() );
        if ( rec.getModified() != null ) {
            stm.setTimestamp( next, new Timestamp( rec.getModified().getDate().getTime() ) );
        } else {
            stm.setTimestamp( next, null );
        }
        next++;
        stm.setString( next++, rec.getParentIdentifier() );
        stm.setString( next++, rec.getType() );
        stm.setString( next++, concatenate( Arrays.asList( rec.getTitle() ) ) );
        stm.setBoolean( next++, rec.isHasSecurityConstraints() );

        QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
        stm.setString( next++, concatenate( qp.getTopicCategory() ) );
        // alternateTitles, revisiondate, creationdate, publicationdate, organisationname, resourceid,
        // resourcelanguage
        stm.setString( next++, concatenate( qp.getAlternateTitle() ) );
        if ( qp.getRevisionDate() != null ) {
            stm.setTimestamp( next, new Timestamp( qp.getRevisionDate().getDate().getTime() ) );
        } else {
            stm.setTimestamp( next, null );
        }
        next++;
        if ( qp.getCreationDate() != null ) {
            stm.setTimestamp( next, new Timestamp( qp.getCreationDate().getDate().getTime() ) );
        } else {
            stm.setTimestamp( next, null );
        }
        next++;
        if ( qp.getPublicationDate() != null ) {
            stm.setTimestamp( next, new Timestamp( qp.getPublicationDate().getDate().getTime() ) );
        } else {
            stm.setTimestamp( next, null );
        }
        next++;
        stm.setString( next++, qp.getOrganisationName() );
        stm.setString( next++, qp.getResourceIdentifier() );
        stm.setString( next++, qp.getResourceLanguage() );

        // "geographicdescriptioncode, denominator, distancevalue, distanceuom, tempextent_begin, tempextent_end
        stm.setString( next++, concatenate( qp.getGeographicDescriptionCode_service() ) );
        stm.setInt( next++, qp.getDenominator() );
        stm.setFloat( next++, qp.getDistanceValue() );
        stm.setString( next++, qp.getDistanceUOM() );
        if ( qp.getTemporalExtentBegin() != null ) {
            stm.setTimestamp( next, new Timestamp( qp.getTemporalExtentBegin().getDate().getTime() ) );
        } else {
            stm.setTimestamp( next, null );
        }
        next++;
        if ( qp.getTemporalExtentEnd() != null ) {
            stm.setTimestamp( next, new Timestamp( qp.getTemporalExtentEnd().getDate().getTime() ) );
        } else {
            stm.setTimestamp( next, null );
        }
        next++;
        // servicetype, servicetypeversion, couplingtype, formats, operations, degree, lineage, resppartyrole
        stm.setString( next++, qp.getServiceType() );
        stm.setString( next++, concatenate( qp.getServiceTypeVersion() ) );
        stm.setString( next++, qp.getCouplingType() );
        stm.setString( next++, getFormats( qp.getFormat() ) );
        stm.setString( next++, concatenate( qp.getOperation() ) );
        stm.setBoolean( next++, qp.isDegree() );
        stm.setString( next++, concatenate( qp.getLineages() ) );
        stm.setString( next++, qp.getRespPartyRole() );

        // spectitle, specdate, specdatetype, bbox
        stm.setString( next++, concatenate( qp.getSpecificationTitle() ) );
        if ( qp.getSpecificationDate() != null ) {
            stm.setTimestamp( next, new Timestamp( qp.getSpecificationDate().getDate().getTime() ) );
        } else {
            stm.setTimestamp( next, null );
        }
        next++;
        stm.setString( next++, qp.getSpecificationDateType() );
        // stm.setObject( next++, getBBox( qp.getBoundingBox() ), Types. );
    }

    private String getBBox( List<BoundingBox> bboxs ) {
        BoundingBox bbox = calculateMainBBox( bboxs );
        StringBuffer sb = new StringBuffer();
        if ( bbox != null ) {
            LOG.debug( "Boundingbox = " + bboxs );
            double east = bbox.getEastBoundLongitude();
            double north = bbox.getNorthBoundLatitude();
            double west = bbox.getWestBoundLongitude();
            double south = bbox.getSouthBoundLatitude();
            if ( connectionType == Type.MSSQL ) {
                sb.append( "geometry::STGeomFromText('POLYGON((" + west ).append( " " + south );
                sb.append( "," + west ).append( " " + north );
                sb.append( "," + east ).append( " " + north );
                sb.append( "," + east ).append( " " + south );
                sb.append( "," + west ).append( " " + south ).append( "))', 0)" );
            } else if ( connectionType == Type.PostgreSQL ) {
                sb.append( "SetSRID('BOX3D(" + west ).append( " " + south ).append( "," + east );
                sb.append( " " + north ).append( ")'::box3d,-1)" );
            }
        }
        return ( sb.toString() != null && sb.length() > 0 ) ? sb.toString() : null;
    }

    private String getFormats( List<Format> list ) {
        StringBuffer sb = new StringBuffer();
        if ( list != null && list.size() > 0 ) {
            sb.append( '\'' );
            for ( Format f : list ) {
                sb.append( '|' ).append( f.getName() );
            }
            if ( !list.isEmpty() ) {
                sb.append( "|" );
            }
            sb.append( "'," );
        }
        return ( sb.toString() != null && sb.length() > 0 ) ? sb.toString() : null;
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
                s.append( " version = ?, " );
                s.append( " status = ?, " );
                s.append( recordColumn ).append( " = ?, " );
                s.append( "abstract=?, anytext=?, language=?, modified=?, parentid=?, type=?, title=?, hassecurityconstraints=?, topiccategories=?, " );
                s.append( "alternateTitles=?, revisiondate=?, creationdate=?, publicationdate=?, organisationname=?, resourceid=?, resourcelanguage=?, " );
                s.append( "geographicdescriptioncode=?, denominator=?, distancevalue=?, distanceuom=?, tempextent_begin=?, tempextent_end=?, " );
                s.append( "servicetype=?, servicetypeversion=?,  couplingtype=?, formats=?, operations=?,  degree=?, lineage=?, resppartyrole=?, " );
                s.append( "spectitle=?, specdate=?, specdatetype=?," );
                s.append( " bbox = " + getBBox( rec.getParsedElement().getQueryableProperties().getBoundingBox() ) );

                s.append( "WHERE " );
                s.append( idColumn ).append( '=' );
                s.append( Integer.toString( requestedId ) );
                stm = conn.prepareStatement( s.toString() );

                stm.setObject( 1, null );
                stm.setObject( 2, null );
                stm.setBytes( 3, rec.getAsByteArray() );

                appendValues( rec, stm, 4 );

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
        QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
        generateIDXTB_CRSStatement( isUpdate, connection, operatesOnId, qp );
        generateIDXTB_KeywordStatement( isUpdate, connection, operatesOnId, qp );
        generateIDXTB_OperatesOnStatement( isUpdate, connection, operatesOnId, qp );
        generateIDXTB_ConstraintStatement( isUpdate, connection, operatesOnId, qp );

    }

    private void generateIDXTB_ConstraintStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                    QueryableProperties qp )
                            throws MetadataStoreException {
        List<Constraint> constraintss = qp.getConstraints();
        if ( constraintss != null && constraintss.size() > 0 ) {
            PreparedStatement stm = null;
            StringWriter sw = null;
            try {
                for ( Constraint constraint : constraintss ) {
                    sw = new StringWriter( 300 );
                    sw.append( "INSERT INTO " ).append( constraintTable );
                    sw.append( '(' ).append( idColumn ).append( ',' ).append( fk_main ).append( ",conditionapptoacc,accessconstraints,otherconstraints,classification)" );
                    sw.append( "VALUES( ?,?,?,?,?,? )" );

                    int localId = executeQPDatabasetables( isUpdate, connection, operatesOnId, constraintTable );
                    stm = connection.prepareStatement( sw.toString() );
                    stm.setInt( 1, localId );
                    stm.setInt( 2, operatesOnId );
                    stm.setString( 3, concatenate( constraint.getLimitations() ) );
                    stm.setString( 4, concatenate( constraint.getAccessConstraints() ) );
                    stm.setString( 5, concatenate( constraint.getOtherConstraints() ) );
                    stm.setString( 6, constraint.getClassification() );
                    stm.executeUpdate();
                }
            } catch ( SQLException e ) {
                String msg = Messages.getMessage( "ERROR_SQL", sw, e.getMessage() );
                LOG.debug( msg );
                throw new MetadataStoreException( msg );
            }
            closeStm( stm );
        }
    }

    private void generateIDXTB_CRSStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                             QueryableProperties qp )
                            throws MetadataStoreException {
        List<CRSCodeType> crss = qp.getCrs();
        if ( crss != null && crss.size() > 0 ) {
            PreparedStatement stm = null;
            StringWriter sw = null;
            try {
                for ( CRSCodeType crs : crss ) {
                    sw = new StringWriter( 300 );
                    sw.append( "INSERT INTO " ).append( crsTable );
                    sw.append( '(' ).append( idColumn ).append( ',' ).append( fk_main ).append( ", authority, crsid, version)" );
                    sw.append( "VALUES( ?,?,?,?,? )" );

                    int localId = executeQPDatabasetables( isUpdate, connection, operatesOnId, crsTable );
                    stm = connection.prepareStatement( sw.toString() );
                    stm.setInt( 1, localId );
                    stm.setInt( 2, operatesOnId );
                    stm.setString( 3,
                                   ( crs.getCodeSpace() != null && crs.getCodeSpace().length() > 0 ) ? crs.getCodeSpace()
                                                                                                    : null );
                    stm.setString( 4, ( crs.getCode() != null && crs.getCode().length() > 0 ) ? crs.getCode() : null );
                    stm.setString( 5,
                                   ( crs.getCodeVersion() != null && crs.getCodeVersion().length() > 0 ) ? crs.getCodeVersion()
                                                                                                        : null );
                    stm.executeUpdate();
                }
            } catch ( SQLException e ) {
                String msg = Messages.getMessage( "ERROR_SQL", sw, e.getMessage() );
                LOG.debug( msg );
                throw new MetadataStoreException( msg );
            }
            closeStm( stm );
        }
    }

    private void generateIDXTB_KeywordStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                 QueryableProperties qp )
                            throws MetadataStoreException {
        List<Keyword> keywords = qp.getKeywords();
        if ( keywords != null && keywords.size() > 0 ) {
            PreparedStatement stm = null;
            StringWriter sw = null;
            try {
                for ( Keyword keyword : keywords ) {
                    sw = new StringWriter( 300 );
                    sw.append( "INSERT INTO " ).append( keywordTable );
                    sw.append( '(' ).append( idColumn ).append( ',' ).append( fk_main ).append( ", keywords, keywordtype)" );
                    sw.append( "VALUES( ?,?,?,? )" );

                    int localId = executeQPDatabasetables( isUpdate, connection, operatesOnId, keywordTable );
                    stm = connection.prepareStatement( sw.toString() );
                    stm.setInt( 1, localId );
                    stm.setInt( 2, operatesOnId );
                    stm.setString( 3, concatenate( keyword.getKeywords() ) );
                    stm.setString( 4, keyword.getKeywordType() );
                    stm.executeUpdate();
                }
            } catch ( SQLException e ) {
                String msg = Messages.getMessage( "ERROR_SQL", sw, e.getMessage() );
                LOG.debug( msg );
                throw new MetadataStoreException( msg );
            }

            closeStm( stm );
        }
    }

    private void generateIDXTB_OperatesOnStatement( boolean isUpdate, Connection connection, int operatesOnId,
                                                    QueryableProperties qp )
                            throws MetadataStoreException {
        List<OperatesOnData> opOns = qp.getOperatesOnData();
        if ( opOns != null && opOns.size() > 0 ) {
            PreparedStatement stm = null;
            StringWriter sw = null;
            try {
                for ( OperatesOnData opOn : opOns ) {
                    sw = new StringWriter( 300 );
                    sw.append( "INSERT INTO " ).append( opOnTable );
                    sw.append( '(' ).append( idColumn ).append( ',' ).append( fk_main ).append( ", operateson, operatesonid, operatesonname )" );
                    sw.append( "VALUES ( ?,?,?,?,? )" );

                    int localId = executeQPDatabasetables( isUpdate, connection, operatesOnId, opOnTable );
                    stm = connection.prepareStatement( sw.toString() );
                    stm.setInt( 1, localId );
                    stm.setInt( 2, operatesOnId );
                    stm.setString( 3, opOn.getOperatesOnId() );
                    stm.setString( 4, opOn.getOperatesOnIdentifier() );
                    stm.setString( 5, opOn.getOperatesOnName() );
                    stm.executeUpdate();
                }
            } catch ( SQLException e ) {
                String msg = Messages.getMessage( "ERROR_SQL", sw, e.getMessage() );
                LOG.debug( msg );
                throw new MetadataStoreException( msg );
            }
            closeStm( stm );
        }
    }

    private void closeStm( PreparedStatement stm ) {
        if ( stm != null )
            try {
                stm.close();
            } catch ( SQLException e ) {
                LOG.warn( "Could not close stm: ", e.getMessage() );
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
     * @return
     * @throws MetadataStoreException
     */
    private int executeQPDatabasetables( boolean isUpdate, Connection connection, int operatesOnId, String databaseTable )
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
            closeStm( stm );
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", sqlStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        }
        return localId;
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
        if ( values == null || values.isEmpty() )
            return null;
        String s = "";
        for ( String value : values ) {
            s = s + '|' + stringInspectation( value );
        }
        if ( !values.isEmpty() )
            s = s + '|';
        return s;
    }

}
