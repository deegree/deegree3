//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2006 by: M.O.S.S. Computer Grafik Systeme GmbH
 Hohenbrunner Weg 13
 D-82024 Taufkirchen
 http://www.moss.de/

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.datastore.sde;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.MappingGeometryField;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.sdeapi.SDEAdapter;
import org.deegree.io.sdeapi.SDEConnection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcbase.PropertyPath;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeUpdate;

/**
 * Handler for <code>Update</code> operations contained in <code>Transaction</code> requests.
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class SDEUpdateHandler extends AbstractSDERequestHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( SDEUpdateHandler.class );

    /**
     * Creates a new <code>UpdateHandler</code> from the given parameters.
     * 
     * @param dsTa
     * @param aliasGenerator
     * @param conn
     */
    public SDEUpdateHandler( SDETransaction dsTa, TableAliasGenerator aliasGenerator, SDEConnection conn ) {
        super( dsTa.getDatastore(), aliasGenerator, conn );
    }

    /**
     * Performs an update operation against the associated datastore.
     * 
     * @param ft
     * @param properties
     * @param filter
     * @return number of updated (root) feature instances
     * @throws DatastoreException
     */
    public int performUpdate( MappedFeatureType ft, Map<PropertyPath, FeatureProperty> properties, Filter filter )
                            throws DatastoreException {

        // only for statistics???
        // FeatureId[] fids = determineAffectedFIDs( mappedFeatureType, filter );

        // process properties list
        // TODO: has to take care about properties in related tables
        ArrayList<MappedPropertyType> list = new ArrayList<MappedPropertyType>();
        for ( PropertyPath path : properties.keySet() ) {
            QualifiedName qn = path.getStep( path.getSteps() - 1 ).getPropertyName();
            MappedPropertyType pt = (MappedPropertyType) ft.getProperty( qn );
            if ( pt == null ) {
                String msg = "Internal error: unknown property type " + qn;
                LOG.logDebug( msg );
                throw new DatastoreException( msg );
            }
            if ( 0 < pt.getTableRelations().length ) {
                String msg = "Update of properties of related tables is not implemented yet " + qn;
                LOG.logDebug( msg );
                throw new DatastoreException( msg );
            }
            list.add( pt );
        }
        MappedPropertyType[] mappedPType = list.toArray( new MappedPropertyType[list.size()] );

        // prepare update
        SDEWhereBuilder whereBuilder = datastore.getWhereBuilder( new MappedFeatureType[] { ft }, null, filter,
                                                                  aliasGenerator );
        Map<String, List<MappingField>> columnsMap = buildColumnsMap( ft, mappedPType, false );
        String[] columns = columnsMap.keySet().toArray( new String[columnsMap.size()] );
        Map<MappingField, Integer> mappingFieldsMap = buildMappingFieldMap( columns, columnsMap );
        StringBuffer whereCondition = new StringBuffer();
        whereBuilder.appendWhereCondition( whereCondition );

        try {
            SeUpdate updater = new SeUpdate( conn.getConnection() );
            updater.setState( conn.getState().getId(), new SeObjectId( SeState.SE_NULL_STATE_ID ),
                              SeState.SE_STATE_DIFF_NOCHECK );
            updater.toTable( ft.getTable(), columns, whereCondition.toString() );
            updater.setWriteMode( true );
            SeRow row = updater.getRowToSet();
            if ( row.hasColumns() ) {
                SeColumnDefinition[] cd = row.getColumns();
                for ( int k = 0; k < cd.length; k++ ) {
                    LOG.logDebug( "*** col[" + k + "] name=" + cd[k].getName() + " type=" + cd[k].getType() + "/"
                                  + typeName( cd[k].getType() ) );
                }
            } else {
                LOG.logDebug( "*** no column definitions!!!" );
            }

            for ( PropertyPath path : properties.keySet() ) {
                QualifiedName qn = path.getStep( path.getSteps() - 1 ).getPropertyName();
                MappedPropertyType pt = (MappedPropertyType) ft.getProperty( qn );
                if ( pt instanceof MappedSimplePropertyType ) {
                    SimpleContent content = ( (MappedSimplePropertyType) pt ).getContent();
                    if ( content instanceof MappingField ) {
                        MappingField field = (MappingField) content;
                        Integer resultPos = mappingFieldsMap.get( field );
                        Object value = properties.get( path ).getValue();
                        SDEAdapter.setRowValue( row, resultPos.intValue(), value,
                                                SDEAdapter.mapSQL2SDE( field.getType() ) );
                    }
                } else if ( pt instanceof MappedGeometryPropertyType ) {
                    MappingGeometryField field = ( (MappedGeometryPropertyType) pt ).getMappingField();
                    Integer resultPos = mappingFieldsMap.get( field );
                    SeShape value = (SeShape) datastore.convertDegreeToDBGeometry( (Geometry) properties.get( path ).getValue() );
                    row.setShape( resultPos.intValue(), value );
                }
            }

            updater.execute();
            updater.close();
        } catch ( SeException e ) {
            e.printStackTrace();
            throw new DatastoreException( "update failed", e );
        }

        // return fids.length;
        return 1; // don't know, how many rows are affected and don't want to query it only for
        // statistics
    }

    /**
     * @param type
     * @return the String mapped from the given type
     */
    public static String typeName( int type ) {
        switch ( type ) {
        case SeColumnDefinition.TYPE_BLOB:
            return "BLOB";
        case SeColumnDefinition.TYPE_CLOB:
            return "CLOB";
        case SeColumnDefinition.TYPE_DATE:
            return "DATE";
        case SeColumnDefinition.TYPE_FLOAT32:
            return "FLOAT32";
        case SeColumnDefinition.TYPE_FLOAT64:
            return "FLOAT64";
        case SeColumnDefinition.TYPE_INT16:
            return "INT16";
        case SeColumnDefinition.TYPE_INT32:
            return "INT32";
        case SeColumnDefinition.TYPE_INT64:
            return "INT64";
        case SeColumnDefinition.TYPE_NCLOB:
            return "NCLOB";
        case SeColumnDefinition.TYPE_NSTRING:
            return "NSTRING";
        case SeColumnDefinition.TYPE_RASTER:
            return "RASTER";
        case SeColumnDefinition.TYPE_SHAPE:
            return "SHAPE";
        case SeColumnDefinition.TYPE_STRING:
            return "STRING";
        case SeColumnDefinition.TYPE_UUID:
            return "UUID";
        case SeColumnDefinition.TYPE_XML:
            return "XML";
        default:
            return "???";
        }
    }
}