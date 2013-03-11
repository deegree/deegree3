//$HeadURL$
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
package org.deegree.services.wms.dynamic;

import static org.deegree.commons.utils.ArrayUtils.splitAsIntList;
import static org.deegree.feature.utils.DBUtils.findSrid;
import static org.deegree.services.wms.MapService.fillInheritedInformation;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringPair;
import org.deegree.feature.persistence.simplesql.SimpleSQLFeatureStore;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.model.layers.DynamicSQLLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.style.se.parser.PostgreSQLReader;
import org.slf4j.Logger;

/**
 * <code>PostGISLayerLoader</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(trace = "logs stack traces")
public class PostGISUpdater extends LayerUpdater {

    private static final Logger LOG = getLogger( PostGISUpdater.class );

    private String connId;

    private final Layer parent;

    private final MapService service;

    private final HashMap<StringPair, DynamicSQLLayer> layers = new HashMap<StringPair, DynamicSQLLayer>();

    private final HashMap<String, SimpleSQLFeatureStore> stores = new HashMap<String, SimpleSQLFeatureStore>();

    private PostgreSQLReader styles;

    private final String schema;

    private final DeegreeWorkspace workspace;

    /**
     * @param connId
     * @param parent
     * @param service
     * @param baseSystemId
     *            to resolve relative references in sld blobs
     */
    public PostGISUpdater( String connId, String schema, Layer parent, MapService service, String baseSystemId,
                           DeegreeWorkspace workspace ) {
        this.connId = connId;
        this.workspace = workspace;
        this.schema = schema == null ? "public" : schema;
        this.parent = parent;
        this.service = service;
        this.styles = new PostgreSQLReader( connId, schema, baseSystemId, workspace );
    }

    /**
     * @return the connection id
     */
    public String getConnectionID() {
        return connId;
    }

    private StringPair generateSQL( String connid, String sourcetable )
                            throws SQLException {
        Connection conn = null;
        ResultSet rs = null;
        try {
            ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
            conn = mgr.get( connid );
            String tableName = sourcetable;

            String schema = this.schema;
            if ( tableName.indexOf( "." ) != -1 ) {
                schema = tableName.substring( 0, tableName.indexOf( "." ) );
            }

            if ( tableName.indexOf( "." ) != -1 ) {
                tableName = tableName.substring( tableName.indexOf( "." ) + 1 );
            }

            int srid = findSrid( connid, tableName, schema );

            rs = conn.getMetaData().getColumns( null, schema, tableName, null );
            StringBuilder sb = new StringBuilder( "select " );
            String geom = null;
            while ( rs.next() ) {
                String cn = rs.getString( "COLUMN_NAME" );
                int tp = rs.getInt( "DATA_TYPE" );
                switch ( tp ) {
                case Types.OTHER:
                    sb.append( "asbinary(\"" ).append( cn ).append( "\") as \"" ).append( cn ).append( "\", " );
                    geom = cn;
                    break;
                default:
                    sb.append( "\"" ).append( cn ).append( "\", " );
                }
            }
            sb.delete( sb.length() - 2, sb.length() );
            sb.append( " from " ).append( schema ).append( "." ).append( tableName ).append( " where \"" ).append( geom );
            sb.append( "\" && st_geomfromtext(?, " ).append( srid ).append( ")" );

            String sourcequery = sb.toString();

            String bbox = "select astext(ST_Estimated_Extent('" + schema + "', '" + tableName + "', '" + geom
                          + "')) as bbox";
            return new StringPair( sourcequery, bbox );
        } finally {
            if ( conn != null ) {
                conn.close();
            }
            if ( rs != null ) {
                rs.close();
            }
        }
    }

    @Override
    public boolean update() {
        boolean changed = false;

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        LinkedList<Layer> toRemove = new LinkedList<Layer>();
        for ( Layer l : parent.getChildren() ) {
            if ( l.getName() != null && l instanceof DynamicSQLLayer ) {
                toRemove.add( l );
                service.layers.remove( l.getName() );
            }
        }
        parent.getChildren().removeAll( toRemove );
        try {
            ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
            conn = mgr.get( connId );

            stmt = conn.prepareStatement( "select name, title, connectionid, sourcetable, sourcequery, symbolcodes, symbolfield, crs, namespace, bboxquery from "
                                          + schema + ".layers" );
            rs = stmt.executeQuery();
            while ( rs.next() ) {
                String name = rs.getString( "name" );
                // check for existing layers
                if ( name != null ) {
                    Layer l = service.getLayer( name );
                    if ( l != null && l.getParent() == parent ) {
                        continue;
                    }
                }
                String title = rs.getString( "title" );
                if ( title == null ) {
                    title = name;
                }

                String connectionid = rs.getString( "connectionid" );
                if ( connectionid == null ) {
                    connectionid = connId;
                }

                String sourcetable = rs.getString( "sourcetable" );
                String sourcequery = rs.getString( "sourcequery" );
                String symbolcodes = rs.getString( "symbolcodes" );
                List<Integer> codes = symbolcodes == null ? Collections.<Integer> emptyList()
                                                         : splitAsIntList( symbolcodes, "," );
                String symbolfield = rs.getString( "symbolfield" );
                String crs = rs.getString( "crs" );
                String namespace = rs.getString( "namespace" );
                namespace = namespace == null ? "http://www.deegree.org/app" : namespace;
                String bbox = rs.getString( "bboxquery" );

                if ( sourcequery == null && sourcetable == null ) {
                    LOG.debug( "Skipping layer '{}' because no data source was defined.", title );
                    continue;
                }

                if ( sourcequery == null ) {
                    StringPair queries = generateSQL( connectionid, sourcetable );
                    sourcequery = queries.first;
                    if ( bbox == null ) {
                        bbox = queries.second;
                    }
                }

                SimpleSQLFeatureStore ds = stores.get( sourcequery + crs + namespace );
                if ( ds == null ) {
                    changed = true;
                    layers.remove( new StringPair( name, title ) );
                    ds = new SimpleSQLFeatureStore( connectionid, crs, sourcequery, name == null ? title : name,
                                                    namespace, "app", bbox,
                                                    Collections.<Pair<Integer, String>> emptyList() );
                    try {
                        ds.init( workspace );
                    } catch ( ResourceInitException e ) {
                        LOG.info( "Data source of layer '{}' could not be initialized: '{}'.", title,
                                  e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                        continue;
                    }
                    stores.put( sourcequery + crs + namespace, ds );
                }

                if ( name != null ) {
                    LOG.debug( "Creating new requestable layer with name '{}', title '{}'.", name, title );
                } else {
                    LOG.debug( "Creating new unrequestable layer with title '{}'.", title );
                }

                DynamicSQLLayer layer = layers.get( new StringPair( name, title ) );
                if ( layer == null ) {
                    changed = true;
                    layer = new DynamicSQLLayer( service, name, title, parent, ds, styles, codes, symbolfield );
                }
                if ( name != null ) {
                    service.layers.put( name, layer );
                }
                parent.getChildren().add( layer );
            }
            fillInheritedInformation( parent, parent.getSrs() );
            changed |= cleanup( parent, service );
        } catch ( SQLException e ) {
            LOG.warn( "Database with connection id '{}' is not available at the moment.", connId );
            LOG.trace( "Stack trace:", e );
        } finally {
            JDBCUtils.close( rs, stmt, conn, LOG );
        }
        return changed;
    }

}
