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
package org.deegree.metadata.iso.persistence;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.jdbc.InsertRow;
import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.jdbc.TransactionRow;
import org.deegree.commons.jdbc.UpdateRow;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSUtils;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.io.WKBWriter;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.iso.persistence.parsing.QueryableProperties;
import org.deegree.metadata.iso.types.BoundingBox;
import org.deegree.metadata.iso.types.Constraint;
import org.deegree.metadata.iso.types.Format;
import org.deegree.metadata.iso.types.Keyword;
import org.deegree.metadata.iso.types.OperatesOnData;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;

import com.vividsolutions.jts.io.ParseException;

/**
 * Here are all the queryable properties encapsulated which have to put into the backend. Here is the functionality of
 * the INSERT and UPDATE action.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class TransactionHelper extends SqlHelper {

    private static final Logger LOG = getLogger( TransactionHelper.class );

    private AnyText anyTextConfig;

    TransactionHelper( Type connectionType, AnyText anyTextConfig ) {
        super( connectionType );
        this.anyTextConfig = anyTextConfig;

    }

    /**
     * Generates and inserts the maindatabasetable that is needed for the queryable properties databasetables to derive
     * from.
     * <p>
     * BE AWARE: the "modified" attribute is get from the first position in the list. The backend has the possibility to
     * add one such attribute. In the xsd-file there are more possible...
     * 
     * @param conn
     *            the SQL connection
     * @return the primarykey of the inserted dataset which is the foreignkey for the queryable properties
     *         databasetables
     * @throws MetadataStoreException
     * @throws XMLStreamException
     */
    int executeInsert( Connection conn, ISORecord rec )
                            throws MetadataStoreException, XMLStreamException {
        int internalId = 0;
        InsertRow ir = new InsertRow( new QTableName( mainTable ), null );
        try {
            internalId = getLastDatasetId( conn, mainTable );
            internalId++;

            ir.addPreparedArgument( idColumn, internalId );
            ir.addPreparedArgument( recordColumn, rec.getAsByteArray() );
            ir.addPreparedArgument( "fileidentifier", rec.getIdentifier() );
            ir.addPreparedArgument( "version", null );
            ir.addPreparedArgument( "status", null );

            appendValues( rec, ir, conn );

            LOG.debug( ir.getSql() );
            ir.performInsert( conn );

            QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
            updateCRSTable( false, conn, internalId, qp );
            updateKeywordTable( false, conn, internalId, qp );
            updateOperatesOnTable( false, conn, internalId, qp );
            updateConstraintTable( false, conn, internalId, qp );

        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", ir.getSql(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        }
        return internalId;
    }

    public int executeDelete( Connection connection, AbstractWhereBuilder builder )
                            throws MetadataStoreException {
        LOG.debug( Messages.getMessage( "INFO_EXEC", "delete-statement" ) );
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<Integer> deletableDatasets;
        int deleted = 0;
        try {
            StringBuilder header = getPreparedStatementDatasetIDs( builder );
            getPSBody( builder, header );
            preparedStatement = connection.prepareStatement( header.toString() );
            int i = 1;
            if ( builder.getWhere() != null ) {
                for ( SQLLiteral o : builder.getWhere().getLiterals() ) {
                    preparedStatement.setObject( i++, o.getValue() );
                }
            }
            if ( builder.getOrderBy() != null ) {
                for ( SQLLiteral o : builder.getOrderBy().getLiterals() ) {
                    preparedStatement.setObject( i++, o.getValue() );
                }
            }
            LOG.debug( Messages.getMessage( "INFO_TA_DELETE_FIND", preparedStatement.toString() ) );

            rs = preparedStatement.executeQuery();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append( "DELETE FROM " );
            stringBuilder.append( mainTable );
            stringBuilder.append( " WHERE " ).append( idColumn );
            stringBuilder.append( " = ?" );

            deletableDatasets = new ArrayList<Integer>();
            if ( rs != null ) {
                while ( rs.next() ) {
                    deletableDatasets.add( rs.getInt( 1 ) );
                }
                rs.close();
                for ( int d : deletableDatasets ) {
                    preparedStatement = connection.prepareStatement( stringBuilder.toString() );
                    preparedStatement.setInt( 1, d );

                    LOG.debug( Messages.getMessage( "INFO_TA_DELETE_DEL", preparedStatement.toString() ) );
                    deleted = deleted + preparedStatement.executeUpdate();
                }
            }
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", preparedStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } finally {
            JDBCUtils.close( rs, preparedStatement, null, LOG );
        }
        return deleted;
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
    int executeUpdate( Connection conn, ISORecord rec, String fileIdentifier )
                            throws MetadataStoreException {
        PreparedStatement stm = null;
        ResultSet rs = null;

        StringWriter s = new StringWriter( 150 );
        int requestedId = -1;

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

            if ( requestedId > -1 ) {
                UpdateRow ur = new UpdateRow( new QTableName( mainTable ) );
                ur.addPreparedArgument( "version", null );
                ur.addPreparedArgument( "status", null );
                ur.addPreparedArgument( recordColumn, rec.getAsByteArray() );

                appendValues( rec, ur, conn );

                ur.setWhereClause( idColumn + " = " + Integer.toString( requestedId ) );
                LOG.debug( stm.toString() );
                ur.performUpdate( conn );

                QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
                updateCRSTable( true, conn, requestedId, qp );
                updateKeywordTable( true, conn, requestedId, qp );
                updateOperatesOnTable( true, conn, requestedId, qp );
                updateConstraintTable( true, conn, requestedId, qp );
            }
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", s.toString(), e.getMessage() );
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

    private void appendValues( ISORecord rec, TransactionRow tr, Connection conn )
                            throws SQLException {
        tr.addPreparedArgument( "abstract", concatenate( Arrays.asList( rec.getAbstract() ) ) );
        tr.addPreparedArgument( "anytext", rec.getAnyText( anyTextConfig ) );
        tr.addPreparedArgument( "language", rec.getLanguage() );
        Timestamp modified = null;
        if ( rec.getModified() != null ) {
            modified = new Timestamp( rec.getModified().getDate().getTime() );
        }
        tr.addPreparedArgument( "modified", modified );
        tr.addPreparedArgument( "parentid", rec.getParentIdentifier() );
        tr.addPreparedArgument( "type", rec.getType() );
        tr.addPreparedArgument( "title", concatenate( Arrays.asList( rec.getTitle() ) ) );
        tr.addPreparedArgument( "hassecurityconstraints", rec.isHasSecurityConstraints() );

        QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
        tr.addPreparedArgument( "topiccategories", concatenate( qp.getTopicCategory() ) );
        tr.addPreparedArgument( "alternateTitles", concatenate( qp.getAlternateTitle() ) );
        Timestamp revDate = null;
        if ( qp.getRevisionDate() != null ) {
            revDate = new Timestamp( qp.getRevisionDate().getDate().getTime() );
        }
        tr.addPreparedArgument( "revisiondate", revDate );
        Timestamp createDate = null;
        if ( qp.getCreationDate() != null ) {
            createDate = new Timestamp( qp.getCreationDate().getDate().getTime() );
        }
        tr.addPreparedArgument( "creationdate", createDate );
        Timestamp pubDate = null;
        if ( qp.getPublicationDate() != null ) {
            pubDate = new Timestamp( qp.getPublicationDate().getDate().getTime() );
        }
        tr.addPreparedArgument( "publicationdate", pubDate );
        tr.addPreparedArgument( "organisationname", qp.getOrganisationName() );
        tr.addPreparedArgument( "resourceid", qp.getResourceIdentifier() );
        tr.addPreparedArgument( "resourcelanguage", qp.getResourceLanguage() );
        tr.addPreparedArgument( "geographicdescriptioncode", concatenate( qp.getGeographicDescriptionCode_service() ) );
        tr.addPreparedArgument( "denominator", qp.getDenominator() );
        tr.addPreparedArgument( "distancevalue", qp.getDistanceValue() );
        tr.addPreparedArgument( "distanceuom", qp.getDistanceUOM() );
        Timestamp begTmpExten = null;
        if ( qp.getTemporalExtentBegin() != null ) {
            begTmpExten = new Timestamp( qp.getTemporalExtentBegin().getDate().getTime() );
        }
        tr.addPreparedArgument( "tempextent_begin", begTmpExten );
        Timestamp endTmpExten = null;
        if ( qp.getTemporalExtentEnd() != null ) {
            endTmpExten = new Timestamp( qp.getTemporalExtentEnd().getDate().getTime() );
        }
        tr.addPreparedArgument( "tempextent_end", endTmpExten );
        tr.addPreparedArgument( "servicetype", qp.getServiceType() );
        tr.addPreparedArgument( "servicetypeversion", concatenate( qp.getServiceTypeVersion() ) );
        tr.addPreparedArgument( "couplingtype", qp.getCouplingType() );
        tr.addPreparedArgument( "formats", getFormats( qp.getFormat() ) );
        tr.addPreparedArgument( "operations", concatenate( qp.getOperation() ) );
        tr.addPreparedArgument( "degree", qp.isDegree() );
        tr.addPreparedArgument( "lineage", concatenate( qp.getLineages() ) );
        tr.addPreparedArgument( "resppartyrole", qp.getRespPartyRole() );
        tr.addPreparedArgument( "spectitle", concatenate( qp.getSpecificationTitle() ) );
        Timestamp specDate = null;
        if ( qp.getSpecificationDate() != null ) {
            specDate = new Timestamp( qp.getSpecificationDate().getDate().getTime() );
        }
        tr.addPreparedArgument( "specdate", specDate );
        tr.addPreparedArgument( "specdatetype", qp.getSpecificationDateType() );

        Geometry geom = calculateMainBBox( qp.getBoundingBox() );
        byte[] wkb;
        try {
            wkb = WKBWriter.write( geom );
            StringBuilder sb = new StringBuilder();
            if ( connectionType == Type.MSSQL ) {
                sb.append( "geometry::STGEOMFROMWKB(?, 0)" );
            } else {
                if ( JDBCUtils.useLegayPostGISPredicates( conn, LOG ) ) {
                    sb.append( "SetSRID(GeomFromWKB(?)," );
                } else {
                    sb.append( "SetSRID(ST_GeomFromWKB(?)," );
                }
                sb.append( "-1)" );
            }
            tr.addPreparedArgument( "bbox", wkb, sb.toString() );

        } catch ( ParseException e ) {
            String msg = "Could not write as WKB " + geom + ": " + e.getMessage();
            LOG.debug( msg, e );
            throw new IllegalArgumentException();
        }
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

    private Envelope calculateMainBBox( List<BoundingBox> bbox ) {
        if ( bbox == null || bbox.isEmpty() )
            return null;
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
        GeometryFactory gf = new GeometryFactory();
        return gf.createEnvelope( west, south, east, north, CRSUtils.EPSG_4326 );
    }

    private void updateConstraintTable( boolean isUpdate, Connection connection, int operatesOnId,
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

                    int localId = updateTable( isUpdate, connection, operatesOnId, constraintTable );
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

    private void updateCRSTable( boolean isUpdate, Connection conn, int operatesOnId, QueryableProperties qp )
                            throws MetadataStoreException {
        List<CRSCodeType> crss = qp.getCrs();
        if ( crss != null && crss.size() > 0 ) {
            for ( CRSCodeType crs : crss ) {
                InsertRow ir = new InsertRow( new QTableName( crsTable ), null );
                try {
                    int localId = updateTable( isUpdate, conn, operatesOnId, crsTable );
                    ir.addPreparedArgument( idColumn, localId );
                    ir.addPreparedArgument( fk_main, operatesOnId );
                    ir.addPreparedArgument( "authority",
                                            ( crs.getCodeSpace() != null && crs.getCodeSpace().length() > 0 ) ? crs.getCodeSpace()
                                                                                                             : null );
                    ir.addPreparedArgument( "crsid",
                                            ( crs.getCode() != null && crs.getCode().length() > 0 ) ? crs.getCode()
                                                                                                   : null );
                    ir.addPreparedArgument( "version",
                                            ( crs.getCodeVersion() != null && crs.getCodeVersion().length() > 0 ) ? crs.getCodeVersion()
                                                                                                                 : null );
                    ir.performInsert( conn );
                } catch ( SQLException e ) {
                    String msg = Messages.getMessage( "ERROR_SQL", ir.getSql(), e.getMessage() );
                    LOG.debug( msg );
                    throw new MetadataStoreException( msg );
                }
            }
        }
    }

    private void updateKeywordTable( boolean isUpdate, Connection conn, int operatesOnId, QueryableProperties qp )
                            throws MetadataStoreException {
        List<Keyword> keywords = qp.getKeywords();
        if ( keywords != null && keywords.size() > 0 ) {
            for ( Keyword keyword : keywords ) {
                InsertRow ir = new InsertRow( new QTableName( keywordTable ), null );
                try {
                    int localId = updateTable( isUpdate, conn, operatesOnId, keywordTable );
                    ir.addPreparedArgument( idColumn, localId );
                    ir.addPreparedArgument( fk_main, operatesOnId );
                    ir.addPreparedArgument( "keywordtype", keyword.getKeywordType() );
                    ir.addPreparedArgument( "keywords", concatenate( keyword.getKeywords() ) );
                    ir.performInsert( conn );
                } catch ( SQLException e ) {
                    String msg = Messages.getMessage( "ERROR_SQL", ir.getSql(), e.getMessage() );
                    LOG.debug( msg );
                    throw new MetadataStoreException( msg );
                }
            }
        }
    }

    private void updateOperatesOnTable( boolean isUpdate, Connection conn, int operatesOnId, QueryableProperties qp )
                            throws MetadataStoreException {
        List<OperatesOnData> opOns = qp.getOperatesOnData();
        if ( opOns != null && opOns.size() > 0 ) {
            for ( OperatesOnData opOn : opOns ) {
                InsertRow ir = new InsertRow( new QTableName( opOnTable ), null );
                try {
                    int localId = updateTable( isUpdate, conn, operatesOnId, opOnTable );
                    ir.addPreparedArgument( idColumn, localId );
                    ir.addPreparedArgument( fk_main, operatesOnId );
                    ir.addPreparedArgument( "operateson", opOn.getOperatesOnId() );
                    ir.addPreparedArgument( "operatesonid", opOn.getOperatesOnIdentifier() );
                    ir.addPreparedArgument( "operatesonname", opOn.getOperatesOnName() );
                    ir.performInsert( conn );
                } catch ( SQLException e ) {
                    String msg = Messages.getMessage( "ERROR_SQL", ir.getSql(), e.getMessage() );
                    LOG.debug( msg );
                    throw new MetadataStoreException( msg );
                }
            }
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

    private int updateTable( boolean isUpdate, Connection connection, int operatesOnId, String databaseTable )
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

    private String concatenate( List<String> values ) {
        if ( values == null || values.isEmpty() )
            return null;
        String s = "";
        for ( String value : values ) {
            if ( value != null ) {
                s = s + '|' + value.replace( "\'", "\'\'" );
            }
        }
        if ( !values.isEmpty() )
            s = s + '|';
        return s;
    }
}