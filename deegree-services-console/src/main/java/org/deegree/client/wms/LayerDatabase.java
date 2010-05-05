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
package org.deegree.client.wms;

import static java.lang.Boolean.FALSE;
import static java.util.Collections.sort;
import static org.deegree.commons.jdbc.Util.findSrid;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import lombok.Getter;
import lombok.Setter;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.LayerDatabaseHelper;
import org.deegree.commons.jdbc.Util;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.wms.WMSController;
import org.deegree.services.wms.dynamic.LayerUpdater;
import org.deegree.services.wms.dynamic.PostGISUpdater;
import org.ol4jsf.component.map.Map;
import org.slf4j.Logger;

/**
 * <code>LayerDatabase</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@SessionScoped
public class LayerDatabase implements Serializable {

    private static final long serialVersionUID = 4427068290103023263L;

    private static final Logger LOG = getLogger( LayerDatabase.class );

    @Getter
    private boolean addOpen;

    @Getter
    private LinkedList<String> connections;

    @Getter
    @Setter
    private String selectedConnection;

    @Getter
    private LinkedList<String> tables;

    @Getter
    @Setter
    private String selectedTable;

    @Getter
    @Setter
    private String selectedSchema;

    @Getter
    private LinkedList<String> schemas;

    @Getter
    private Boolean manySchemas;

    @Getter
    private LinkedList<String> wmsConnections;

    @Getter
    private Boolean doesHaveWmsConnection;

    @Getter
    private Boolean doesHaveMultipleWmsConnections;

    @Setter
    @Getter
    private String selectedWmsConnection;

    @Getter
    @Setter
    private String crs;

    @Getter
    @Setter
    private Map openlayersMap;

    /**
     * 
     */
    public LayerDatabase() {
        addOpen = FALSE;
        connections = new LinkedList<String>( ConnectionManager.getConnectionIds() );
        connections.remove( "LOCK_DB" );
        sort( connections );
        ListIterator<String> iter = connections.listIterator();
        while ( iter.hasNext() ) {
            String cur = iter.next();
            Connection conn = null;
            try {
                conn = ConnectionManager.getConnection( cur );
                if ( conn.getMetaData().getDriverName().contains( "Oracle" ) ) {
                    iter.remove();
                }
            } catch ( SQLException e ) {
                iter.remove();
                LOG.trace( "Stack trace: ", e );
            } finally {
                if ( conn != null ) {
                    try {
                        conn.close();
                    } catch ( SQLException e ) {
                        LOG.trace( "Stack trace: ", e );
                    }
                }
            }
        }
        if ( !connections.isEmpty() ) {
            selectedConnection = connections.getFirst();
            fetchTables( null );
        }
        WMSController controller = (WMSController) OGCFrontController.getServiceController( WMSController.class );
        doesHaveWmsConnection = FALSE;
        doesHaveMultipleWmsConnections = FALSE;
        if ( controller != null ) {
            wmsConnections = new LinkedList<String>();
            for ( LayerUpdater updater : controller.getMapService().getDynamics() ) {
                if ( updater instanceof PostGISUpdater ) {
                    wmsConnections.add( ( (PostGISUpdater) updater ).getConnectionID() );
                    doesHaveWmsConnection = true;
                }
            }
        }
        if ( doesHaveWmsConnection ) {
            selectedWmsConnection = wmsConnections.getFirst();
            doesHaveMultipleWmsConnections = wmsConnections.size() > 1;
        }
    }

    /**
     * @return true, if num schemas &gt; 1
     */
    public boolean getManySchemas() {
        return addOpen && manySchemas;
    }

    /**
     * @return null
     */
    public String tableChanged() {
        int srid = findSrid( selectedConnection, selectedTable, selectedSchema );
        crs = srid > 0 ? "EPSG:" + srid : "EPSG:4326";
        return null;
    }

    /**
     * @param evt
     */
    public void switchAddOpen( ActionEvent evt ) {
        addOpen = !addOpen;
        if ( addOpen ) {
            fetchSchemas( null );
        }
    }

    /**
     * @return something to reload the page
     */
    public String addLayer() {
        if ( !LayerDatabaseHelper.addLayer( selectedWmsConnection, selectedTable, selectedTable, selectedConnection,
                                            selectedTable, crs ) ) {
            // TODO error
            return "error";
        }

        openlayersMap.setValid( false );

        return null;
    }

    public String removeLayer() {
        return null;
    }

    /**
     * @param evt
     * 
     */
    public void fetchSchemas( AjaxBehaviorEvent evt ) {
        schemas = Util.fetchGeometrySchemas( selectedConnection );
        manySchemas = schemas.size() > 1;
        sort( schemas );
        if ( !schemas.isEmpty() ) {
            selectedSchema = schemas.getFirst();
            fetchTables( null );
        }
    }

    /**
     * @param evt
     */
    public void fetchTables( AjaxBehaviorEvent evt ) {
        tables = Util.fetchGeometryTables( selectedConnection, selectedSchema );
        sort( tables );
        if ( !tables.isEmpty() ) {
            selectedTable = tables.getFirst();
        }
        tableChanged();
    }

}
