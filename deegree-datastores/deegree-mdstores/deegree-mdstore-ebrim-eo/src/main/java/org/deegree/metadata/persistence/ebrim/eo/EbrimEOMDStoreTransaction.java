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
package org.deegree.metadata.persistence.ebrim.eo;

import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.SLOTURN;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.EOTYPE.ACQUPLATFORM;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.EOTYPE.ARCHIVINGINFO;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.EOTYPE.BROWSEINFO;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.EOTYPE.DATALAYER;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.EOTYPE.MASKINFO;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.EOTYPE.PRODUCT;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.EOTYPE.PRODUCTINFO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.jdbc.InsertRow;
import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.filter.Filter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.expression.SQLArgument;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBWriter;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.ebrim.AliasedRIMType;
import org.deegree.metadata.ebrim.Association;
import org.deegree.metadata.ebrim.Classification;
import org.deegree.metadata.ebrim.ClassificationNode;
import org.deegree.metadata.ebrim.ExtrinsicObject;
import org.deegree.metadata.ebrim.RIMType;
import org.deegree.metadata.ebrim.RegistryObject;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.ebrim.eo.mapping.EOPropertyNameMapper;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.EOTYPE;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.Table;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType;
import org.deegree.metadata.persistence.transaction.DeleteOperation;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.metadata.persistence.transaction.UpdateOperation;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.postgis.PostGISWhereBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.ParseException;

/**
 * {@link MetadataStoreTransaction} implementation for the {@link EbrimEOMDStore}.
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class EbrimEOMDStoreTransaction implements MetadataStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( EbrimEOMDStoreTransaction.class );

    private final Connection conn;

    private final boolean useLegacyPredicates;

    public EbrimEOMDStoreTransaction( Connection conn, boolean useLegacyPredicates ) {
        this.conn = conn;
        this.useLegacyPredicates = useLegacyPredicates;
    }

    @Override
    public void commit()
                            throws MetadataStoreException {
        LOG.debug( "Performing commit of transaction." );
        try {
            conn.commit();
        } catch ( SQLException e ) {
            String msg = "Commit failed: " + e.getMessage();
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } finally {
            JDBCUtils.close( conn );
        }
    }

    @Override
    public void rollback()
                            throws MetadataStoreException {
        LOG.debug( "Performing rollback of transaction." );
        try {
            conn.rollback();
        } catch ( SQLException e ) {
            String msg = "Rollback failed: " + e.getMessage();
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } finally {
            JDBCUtils.close( conn );
        }
    }

    @Override
    public List<String> performInsert( InsertOperation insert )
                            throws MetadataStoreException, MetadataInspectorException {
        List<String> identifierList = new ArrayList<String>();
        for ( MetadataRecord record : insert.getRecords() ) {
            if ( record != null ) {
                // TODO: suppor of other registryObjects
                RegistryPackage ebrimRecord = new RegistryPackage( record.getAsOMElement() );
                // TODO: inspect records

                insert( ebrimRecord );
                identifierList.add( ebrimRecord.getIdentifier() );
            }
        }
        return identifierList;
    }

    @Override
    public int performDelete( DeleteOperation operation )
                            throws MetadataStoreException {

        Filter constraint = operation.getConstraint();

        try {
            QName[] typeNames = new QName[] { new QName( RIMType.RegistryPackage.name() ) };
            EOPropertyNameMapper propMapper = new EOPropertyNameMapper( typeNames, useLegacyPredicates );
            if ( !( constraint instanceof OperatorFilter ) ) {
                throw new MetadataStoreException( "Delete using id filters is not supported yet." );
            }
            AbstractWhereBuilder wb = new PostGISWhereBuilder(
                                                               new EOPropertyNameMapper( typeNames, useLegacyPredicates ),
                                                               (OperatorFilter) constraint, null, false,
                                                               useLegacyPredicates );

            AliasedRIMType returnType = propMapper.getReturnType( typeNames );
            StringBuilder innerSelect = new StringBuilder( "SELECT " );
            innerSelect.append( propMapper.getTableAlias( returnType ) );
            innerSelect.append( ".internalId FROM " );
            boolean first = true;
            for ( AliasedRIMType queryType : propMapper.getQueryTypes() ) {
                if ( !first ) {
                    innerSelect.append( "," );
                }
                innerSelect.append( propMapper.getTable( queryType ).name() );
                innerSelect.append( " AS " );
                innerSelect.append( propMapper.getTableAlias( queryType ) );
                first = false;
            }
            if ( wb.getWhere() != null ) {
                innerSelect.append( " WHERE " ).append( wb.getWhere().getSQL() );
            }

            StringBuilder delete = new StringBuilder( "DELETE " );
            delete.append( " FROM " );
            delete.append( propMapper.getTable( returnType ) );
            delete.append( " WHERE internalId IN (" );
            delete.append( innerSelect );
            delete.append( ")" );

            PreparedStatement stm = conn.prepareStatement( delete.toString() );
            int i = 1;
            if ( wb.getWhere() != null ) {
                for ( SQLArgument argument : wb.getWhere().getArguments() ) {
                    argument.setArgument( stm, i++ );
                }
            }
            LOG.debug( "Execute: " + stm.toString() );
            return stm.executeUpdate();
        } catch ( Throwable t ) {
            String msg = "Delete failed: " + t.getMessage();
            LOG.debug( msg, t );
            throw new MetadataStoreException( msg );
        }
    }

    @Override
    public int performUpdate( UpdateOperation update )
                            throws MetadataStoreException, MetadataInspectorException {
        throw new UnsupportedOperationException( "Updating of ebRIM EO records is not implemented yet." );
    }

    /**
     * @param registryPackage
     * @throws MetadataStoreException
     */
    private void insert( RegistryPackage registryPackage )
                            throws MetadataStoreException {
        // TODO: use autogencolumn!
        InsertRow ir = new InsertRow( new QTableName( Table.idxtb_registrypackage.name() ), null );
        try {
            int id = getNewId( conn );
            ir.addPreparedArgument( "internalId", id );
            ir.addPreparedArgument( "id", registryPackage.getId() );
            ir.addPreparedArgument( "externalId", registryPackage.getExtId() );
            ir.addPreparedArgument( "name", registryPackage.getName() );
            ir.addPreparedArgument( "description", registryPackage.getDesc() );
            ir.addPreparedArgument( "data", getAsByteArray( registryPackage.getElement() ) );

            LOG.debug( "Execute statement " + ir.getSql() );
            
            ir.performInsert( conn );

            for ( EOTYPE type : EOTYPE.values() ) {
                for ( ExtrinsicObject eo : registryPackage.getExtrinsicObjects( type.getType() ) ) {
                    insertExtrinsicObject( eo, id, conn );
                }
            }
            for ( Association association : registryPackage.getAssociations() ) {
                insertAssociation( association, id, conn );
            }
            for ( Classification classification : registryPackage.getClassifications() ) {
                insertClassification( classification, id, conn );
            }
            for ( ClassificationNode classificationNode : registryPackage.getClassificationNodes() ) {
                insertClassificationNode( classificationNode, id, conn );
            }
        } catch ( SQLException e ) {
            String msg = "Insert failed: " + e.getMessage();
            LOG.debug( msg, e );
            throw new MetadataStoreException( msg );
        }
    }

    private int getNewId( Connection conn )
                            throws SQLException {
        int result = 0;
        String selectIDRows = null;
        selectIDRows = "SELECT nextval('globalseq')";
        ResultSet rsBrief = conn.createStatement().executeQuery( selectIDRows );
        while ( rsBrief.next() ) {
            result = rsBrief.getInt( 1 );
        }
        rsBrief.close();
        return result;
    }

    private void prepareSlot( SlotMapping slot, ExtrinsicObject extrinsicObject, InsertRow ir ) {
        if ( SlotType._geom.equals( slot.getType() ) ) {
            Geometry geom = (Geometry) extrinsicObject.getGeometrySlotValue( SLOTURN + slot.getName() );
            if ( geom != null ) {
                try {
                    byte[] wkt = WKBWriter.write( geom );
                    StringBuilder sb = new StringBuilder();
                    if ( useLegacyPredicates ) {
                        sb.append( "SetSRID(GeomFromWKB(?)," );
                    } else {
                        sb.append( "SetSRID(ST_GeomFromWKB(?)," );
                    }
                    sb.append( "-1)" );
                    ir.addPreparedArgument( slot.getColumn(), wkt, sb.toString() );
                } catch ( ParseException e ) {
                    String msg = "Could not write as WKB " + geom + ": " + e.getMessage();
                    LOG.debug( msg, e );
                    throw new IllegalArgumentException();
                }
            }
            return;
        }
        if ( SlotType._multiple.equals( slot.getType() ) ) {
            String[] slotValue = extrinsicObject.getSlotValueList( SLOTURN + slot.getName() );
            ir.addPreparedArgument( slot.getColumn(), concatenate( slotValue ) );
        }
        String slotValue = extrinsicObject.getSlotValue( SLOTURN + slot.getName() );
        if ( slotValue != null ) {
            switch ( slot.getType() ) {
            case _date:
                try {
                    ir.addPreparedArgument( slot.getColumn(),
                                            new Timestamp( ( DateUtils.parseISO8601Date( slotValue ).getTime() ) ) );
                } catch ( java.text.ParseException e ) {
                    String msg = "Could not parse as Date:" + slotValue;
                    LOG.debug( msg, e );
                    throw new IllegalArgumentException( msg );

                }
                break;
            case _double:
                try {
                    ir.addPreparedArgument( slot.getColumn(), Double.parseDouble( slotValue ) );
                } catch ( NumberFormatException e ) {
                    String msg = "Could not parse as double:" + slotValue;
                    LOG.debug( msg, e );
                    throw new IllegalArgumentException( msg );
                }
                break;
            case _int:
                try {
                    ir.addPreparedArgument( slot.getColumn(), Integer.parseInt( slotValue ) );
                } catch ( NumberFormatException e ) {
                    String msg = "Could not parse as integer:" + slotValue;
                    LOG.debug( msg, e );
                    throw new IllegalArgumentException( msg );
                }
                break;
            default:
                ir.addPreparedArgument( slot.getColumn(), slotValue );
                break;
            }
        }
    }

    // TODO: remove whitespaces
    private byte[] getAsByteArray( OMElement root ) {
        root.declareDefaultNamespace( "http://www.isotc211.org/2005/gmd" );
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream( 20000 );
            root.serialize( out );
            out.close();
            return out.toByteArray();
        } catch ( XMLStreamException e ) {
            return root.toString().getBytes();
        } catch ( IOException e ) {
            return root.toString().getBytes();
        }
    }

    private void insertExtrinsicObject( ExtrinsicObject eo, int regPackId, Connection conn )
                            throws SQLException {
        InsertRow ir = new InsertRow( new QTableName( Table.idxtb_extrinsicobject.name() ), null );
        addRegistryObject( ir, eo );

        ir.addPreparedArgument( "resource", eo.getResource() );
        ir.addPreparedArgument( "isopaque", eo.isOpaque() );
        ir.addPreparedArgument( "data", getAsByteArray( eo.getElement() ) );

        ir.addPreparedArgument( "fk_registrypackage", regPackId );

        List<SlotMapping> slots = null;
        if ( PRODUCT.getType().equals( eo.getObjectType() ) ) {
            slots = PRODUCT.getSlots();
        } else if ( ACQUPLATFORM.getType().equals( eo.getObjectType() ) ) {
            ir.addPreparedArgument( "ap_shortName", eo.getName() );
            slots = ACQUPLATFORM.getSlots();
        } else if ( MASKINFO.getType().equals( eo.getObjectType() ) ) {
            ir.addPreparedArgument( "mi_type", eo.getName() );
            slots = MASKINFO.getSlots();
        } else if ( ARCHIVINGINFO.getType().equals( eo.getObjectType() ) ) {
            ir.addPreparedArgument( "ai_archivingCenter", eo.getName() );
            slots = ARCHIVINGINFO.getSlots();
        } else if ( PRODUCTINFO.getType().equals( eo.getObjectType() ) ) {
            slots = PRODUCTINFO.getSlots();
        } else if ( DATALAYER.getType().equals( eo.getObjectType() ) ) {
            ir.addPreparedArgument( "dl_specy", eo.getName() );
            slots = DATALAYER.getSlots();
        } else if ( BROWSEINFO.getType().equals( eo.getObjectType() ) ) {
            ir.addPreparedArgument( "bi_type", eo.getName() );
            slots = BROWSEINFO.getSlots();
        }
        if ( slots != null ) {
            for ( SlotMapping slot : slots ) {
                prepareSlot( slot, eo, ir );
            }
        }
        ir.performInsert( conn );
    }

    private void insertAssociation( Association association, int regPackId, Connection conn )
                            throws SQLException {
        InsertRow ir = new InsertRow( new QTableName( Table.idxtb_association.name() ), null );
        addRegistryObject( ir, association );
        ir.addPreparedArgument( "sourceObject", association.getSourceObject() );
        ir.addPreparedArgument( "targetObject", association.getTargetObject() );
        ir.addPreparedArgument( "associationType", association.getAssociationType() );
        ir.addPreparedArgument( "data", getAsByteArray( association.getElement() ) );
        ir.addPreparedArgument( "fk_registrypackage", regPackId );
        ir.performInsert( conn );
    }

    private void insertClassification( Classification classification, int regPackId, Connection conn )
                            throws SQLException {
        InsertRow ir = new InsertRow( new QTableName( Table.idxtb_classification.name() ), null );
        addRegistryObject( ir, classification );
        ir.addPreparedArgument( "classificationNode", classification.getClassificationNode() );
        ir.addPreparedArgument( "classifiedObject", classification.getClassifiedObject() );
        ir.addPreparedArgument( "classificationScheme", classification.getClassificationScheme() );
        ir.addPreparedArgument( "data", getAsByteArray( classification.getElement() ) );
        ir.addPreparedArgument( "fk_registrypackage", regPackId );
        ir.performInsert( conn );
    }

    private void insertClassificationNode( ClassificationNode classificationNode, int regPackId, Connection conn )
                            throws SQLException {
        InsertRow ir = new InsertRow( new QTableName( Table.idxtb_classificationNode.name() ), null );
        addRegistryObject( ir, classificationNode );
        ir.addPreparedArgument( "parent", classificationNode.getParent() );
        ir.addPreparedArgument( "code", classificationNode.getCode() );
        ir.addPreparedArgument( "path", classificationNode.getPath() );
        ir.addPreparedArgument( "data", getAsByteArray( classificationNode.getElement() ) );
        ir.addPreparedArgument( "fk_registrypackage", regPackId );
        ir.performInsert( conn );
    }

    private void addRegistryObject( InsertRow ir, RegistryObject ro ) {
        ir.addPreparedArgument( "id", ro.getId() );
        ir.addPreparedArgument( "objectType", ro.getObjectType() );
        ir.addPreparedArgument( "home", ro.getHome() );
        ir.addPreparedArgument( "lid", ro.getLid() );
        ir.addPreparedArgument( "status", ro.getStatus() );
        ir.addPreparedArgument( "externalId", ro.getExtId() );
        ir.addPreparedArgument( "name", ro.getName() );
        ir.addPreparedArgument( "description", ro.getDesc() );
        ir.addPreparedArgument( "versionInfo", ro.getVersionInfo() );
    }

    private String concatenate( String[] slotValue ) {
        if ( slotValue == null || slotValue.length == 0 )
            return null;
        String s = "";
        for ( String value : slotValue ) {
            s = s + '|' + value;
        }
        if ( slotValue.length > 0 )
            s = s + '|';
        return s;
    }
}