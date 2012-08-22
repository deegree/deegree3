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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLId;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.sdeapi.SDEConnection;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;

import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * Handles <code>Transaction</code> requests to SQL based datastores.
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AbstractSDERequestHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractSDERequestHandler.class );

    /**
     * feature type column
     */
    protected static final String FT_COLUMN = "featuretype";

    /**
     * The prefix of feature types 'ft_'
     */
    protected static final String FT_PREFIX = "ft_";

    /**
     * datastore to operate upon
     */
    protected SDEDatastore datastore;

    /**
     * reference to the alias generator
     */    
    protected TableAliasGenerator aliasGenerator;

    /**
     * a connection
     */
    protected SDEConnection conn;

    /**
     * Creates a new instance of <code>AbstractSDERequestHandler</code> from the given parameters.
     * 
     * @param datastore
     * @param aliasGenerator
     * @param conn
     */
    public AbstractSDERequestHandler( SDEDatastore datastore, TableAliasGenerator aliasGenerator, SDEConnection conn ) {
        this.datastore = datastore;
        this.aliasGenerator = aliasGenerator;
        this.conn = conn;
    }

    /**
     * Returns the underlying <code>AbstractSQLDatastore</code>.
     * 
     * @return the underlying <code>AbstractSQLDatastore</code>.
     */
    public SDEDatastore getDatastore() {
        return this.datastore;
    }

    /**
     * Returns the underlying <code>AbstractSQLDatastore</code>.
     * 
     * @return the underlying <code>AbstractSQLDatastore</code>.
     */
    public SDEConnection getConnection() {
        return this.conn;
    }

    /**
     * Returns the underlying <code>AbstractSQLDatastore</code>.
     * 
     * @return the underlying <code>AbstractSQLDatastore</code>.
     */
    public TableAliasGenerator getAliasGenerator() {
        return this.aliasGenerator;
    }

    /**
     * Determines the feature ids that are matched by the given filter.
     * 
     * @param ft
     * @param filter
     * @return the feature ids that are matched by the given filter.
     * @throws DatastoreException
     */
    public FeatureId[] determineAffectedFIDs( MappedFeatureType ft, Filter filter )
                            throws DatastoreException {

        SDEWhereBuilder whereBuilder = this.datastore.getWhereBuilder( new MappedFeatureType[] { ft }, null, filter,
                                                                       new TableAliasGenerator() );

        // if no filter is given
        FeatureId[] fids = null;
        SeQuery stmt = null;
        try {
            stmt = buildInitialFIDSelect( ft, whereBuilder );
            stmt.execute();
            fids = extractFeatureIds( stmt, ft );
        } catch ( Exception e ) {
            throw new DatastoreException( "Error performing the delete transaction operation on mapped feature type '"
                                          + ft.getName() + "'." );
        } finally {
            try {
                if ( stmt != null ) {
                    stmt.close();
                }
            } catch ( Exception e ) {
                // eat'em keeps you healthy
            }
        }
        return fids;
    }

    /**
     * Builds the initial SELECT statement that retrieves the feature ids that are matched by the
     * given <code>WhereBuilder</code>.
     * <p>
     * The statement is structured like this:
     * <ul>
     * <li><code>SELECT</code></li>
     * <li>comma-separated list of qualified fid fields</li>
     * <li><code>FROM</code></li>
     * <li>comma-separated list of tables and their aliases (this is needed to constrain the paths
     * to selected XPath-PropertyNames)</li>
     * <li><code>WHERE</code></li>
     * <li>SQL representation of the Filter expression</li>
     * </ul>
     * 
     * @param rootFeatureType
     * @param whereBuilder
     * @return the query
     */
    protected SeQuery buildInitialFIDSelect( MappedFeatureType rootFeatureType, SDEWhereBuilder whereBuilder ) {
        SeQuery query = null;
        try {
            StringBuffer whereCondition = new StringBuffer();
            whereBuilder.appendWhereCondition( whereCondition );
            SeSqlConstruct constr = new SeSqlConstruct( rootFeatureType.getTable(), whereCondition.toString() );
            String[] fidColumns = getFeatureIdColumns( rootFeatureType );
            query = new SeQuery( getConnection().getConnection(), fidColumns, constr );
            if ( whereBuilder.getFilter() instanceof ComplexFilter ) {
                // There is NO chance, to make a new SeCoordinateReference equal to the existing crs
                // of the requested layer.
                // So, we give it a chance, by passing the layer definitions (and its associated
                // crs) to the whereBuilder method
                List<SeLayer> layers = getConnection().getConnection().getLayers();
                SeFilter[] spatialFilter = whereBuilder.buildSpatialFilter( (ComplexFilter) whereBuilder.getFilter(),
                                                                            layers );
                if ( null != spatialFilter && 0 < spatialFilter.length ) {
                    query.setSpatialConstraints( SeQuery.SE_OPTIMIZE, false, spatialFilter );
                }
            }
            query.prepareQuery();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return query;
    }

    /**
     * Appends the alias qualified columns that make up the feature id to the given query.
     * 
     * @param featureType
     * @return the feature id columns
     */
    protected String[] getFeatureIdColumns( MappedFeatureType featureType ) {
        MappingField[] fidFields = featureType.getGMLId().getIdFields();
        String[] columns = new String[fidFields.length];
        for ( int i = 0; i < fidFields.length; i++ ) {
            columns[i] = fidFields[i].getField();
        }
        return columns;
    }

    /**
     * @param stmt
     * @param ft
     * @return the ids
     * @throws Exception
     */
    public FeatureId[] extractFeatureIds( SeQuery stmt, MappedFeatureType ft )
                            throws Exception {
        List<FeatureId> featureIdList = new ArrayList<FeatureId>();
        MappedGMLId gmlId = ft.getGMLId();
        MappingField[] idFields = gmlId.getIdFields();
        SeRow row = null;
        for ( ;; ) {
            try {
                row = stmt.fetch();
            } catch ( Exception e ) {
                row = null;
            }
            if ( null == row )
                break;
            Object[] idValues = new Object[idFields.length];
            for ( int i = 0; i < idValues.length; i++ ) {
                idValues[i] = row.getObject( i );
            }
            featureIdList.add( new FeatureId( ft, idValues ) );
        }
        return featureIdList.toArray( new FeatureId[featureIdList.size()] );
    }

    /**
     * Builds a helper map that contains the column names of the feature type's table as keys. Each
     * column name is mapped to a <code>List</code> containing the <code>MappingField</code>
     * instances that refer to this column.
     * <p>
     * The following MappingField instances of the feature type's annotation are used to build the
     * map:
     * <ul>
     * <li>MappingFields from the wfs:gmlId - annotation element of the feature type definition</li>
     * <li>MappingFields in the annotations of the property element definitions; if the property's
     * content is stored in a related table, the MappingFields used in the first wfs:Relation
     * element's wfs:From element are considered</li>
     * </ul>
     * 
     * @param featureType
     *            feature type for which the map is built
     * @param requestedProperties
     *            requested properties
     * @param withIdFields
     * @return key class: String (column names), value class: List (containing MappingField
     *         instances)
     */
    protected Map<String, List<MappingField>> buildColumnsMap( MappedFeatureType featureType,
                                                               PropertyType[] requestedProperties, boolean withIdFields ) {
        Map<String, List<MappingField>> columnsMap = new HashMap<String, List<MappingField>>();

        // add table columns that are necessary to build the feature's gml id
        if ( withIdFields ) {
            MappingField[] idFields = featureType.getGMLId().getIdFields();
            for ( int i = 0; i < idFields.length; i++ ) {
                List<MappingField> mappingFieldList = columnsMap.get( idFields[i].getField() );
                if ( mappingFieldList == null ) {
                    mappingFieldList = new ArrayList<MappingField>();
                }
                mappingFieldList.add( idFields[i] );
                columnsMap.put( idFields[i].getField(), mappingFieldList );
            }
        }

        // add columns that are necessary to build the requested feature properties
        for ( int i = 0; i < requestedProperties.length; i++ ) {
            MappedPropertyType pt = (MappedPropertyType) requestedProperties[i];

            TableRelation[] tableRelations = pt.getTableRelations();
            if ( tableRelations != null && tableRelations.length != 0 ) {
                // if property is not stored in feature type's table, retrieve key fields of
                // the first relation's 'From' element
                MappingField[] fields = tableRelations[0].getFromFields();
                for ( int j = 0; j < fields.length; j++ ) {
                    List<MappingField> list = columnsMap.get( fields[j].getField() );
                    if ( list == null ) {
                        list = new ArrayList<MappingField>();
                    }
                    list.add( fields[j] );
                    columnsMap.put( fields[j].getField(), list );
                }
                // if (content instanceof FeaturePropertyContent) {
                // if (tableRelations.length == 1) {
                // // if feature property contains an abstract feature type, retrieve
                // // feature type as well (stored in column named "FT_fk")
                // MappedFeatureType subFeatureType = ( (FeaturePropertyContent) content )
                // .getFeatureTypeReference().getFeatureType();
                // if (subFeatureType.isAbstract()) {
                // String typeColumn = FT_PREFIX + fields [0].getField();
                // columnsMap.put (typeColumn, new ArrayList ());
                // }
                // }
                // }
            } else {
                MappingField field = null;
                if ( pt instanceof MappedSimplePropertyType ) {
                    SimpleContent content = ( (MappedSimplePropertyType) pt ).getContent();
                    if ( content instanceof MappingField ) {
                        field = (MappingField) content;
                    } else {
                        // ignore virtual properties here
                        continue;
                    }
                } else if ( pt instanceof MappedGeometryPropertyType ) {
                    field = ( (MappedGeometryPropertyType) pt ).getMappingField();
                } else {
                    String msg = "Unsupported property type: '" + pt.getClass().getName()
                                 + "' in QueryHandler.buildColumnsMap(). ";
                    LOG.logError( msg );
                    throw new IllegalArgumentException( msg );
                }
                List<MappingField> list = columnsMap.get( field.getField() );
                if ( list == null ) {
                    list = new ArrayList<MappingField>();
                }
                list.add( field );
                columnsMap.put( field.getField(), list );
            }
        }
        return columnsMap;
    }

    /**
     * Builds a lookup map that contains <code>MappingField</code> instances as keys. Each
     * <code>MappingField</code> is mapped to the index position (an <code>Integer</code>) of
     * the MappingField's field name in the given column array.
     * 
     * @param columns
     * @param columnsMap
     * @return key class: MappingField, value class: Integer (index of field name in columns)
     */
    protected Map<MappingField, Integer> buildMappingFieldMap( String[] columns,
                                                               Map<String, List<MappingField>> columnsMap ) {
        Map<MappingField, Integer> mappingFieldMap = new HashMap<MappingField, Integer>();
        for ( int i = 0; i < columns.length; i++ ) {
            Integer resultPos = new Integer( i );
            List<MappingField> mappingFieldList = columnsMap.get( columns[i] );
            Iterator<MappingField> iter = mappingFieldList.iterator();
            while ( iter.hasNext() ) {
                mappingFieldMap.put( iter.next(), resultPos );
            }
        }
        return mappingFieldMap;
    }
}
