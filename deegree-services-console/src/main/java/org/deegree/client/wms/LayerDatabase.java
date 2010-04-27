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

import static java.util.Collections.sort;
import static org.deegree.commons.jdbc.Util.findSrid;

import java.io.Serializable;
import java.util.LinkedList;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.LayerDatabaseHelper;
import org.deegree.commons.jdbc.Util;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.wms.WMSController;
import org.deegree.services.wms.dynamic.LayerUpdater;
import org.deegree.services.wms.dynamic.PostGISUpdater;
import org.ol4jsf.component.map.Map;

/**
 * <code>WMSDatabase</code>
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

    private boolean open;

    private LinkedList<String> connections;

    private String selectedConnection;

    private LinkedList<String> tables;

    private String selectedTable;

    private String selectedSchema;

    private LinkedList<String> schemas;

    private boolean manySchemas;

    private LinkedList<String> wmsConnections;

    private boolean hasWmsConnection;

    private boolean hasMultipleWmsConnections;

    private String selectedWmsConnection;

    private String crs;

    private Map openlayersMap;

    /**
     * 
     */
    public LayerDatabase() {
        open = false;
        connections = new LinkedList<String>( ConnectionManager.getConnectionIds() );
        connections.remove( "LOCK_DB" );
        sort( connections );
        if ( !connections.isEmpty() ) {
            selectedConnection = connections.getFirst();
            fetchTables( null );
        }
        WMSController controller = (WMSController) OGCFrontController.getServiceController( WMSController.class );
        hasWmsConnection = false;
        hasMultipleWmsConnections = false;
        if ( controller != null ) {
            wmsConnections = new LinkedList<String>();
            for ( LayerUpdater updater : controller.getMapService().getDynamics() ) {
                if ( updater instanceof PostGISUpdater ) {
                    wmsConnections.add( ( (PostGISUpdater) updater ).getConnectionID() );
                    hasWmsConnection = true;
                }
            }
        }
        if ( hasWmsConnection ) {
            selectedWmsConnection = wmsConnections.getFirst();
            hasMultipleWmsConnections = wmsConnections.size() > 1;
        }
    }

    /**
     * @param map
     */
    public void setOpenlayersMap( Map map ) {
        System.out.println( map );
        openlayersMap = map;
    }

    /**
     * @return the map
     */
    public Map getOpenlayersMap() {
        return openlayersMap;
    }

    /**
     * @return true, if more than one wms connection
     */
    public boolean getHasMultipleWmsConnections() {
        return hasMultipleWmsConnections;
    }

    /**
     * @return the connection ids
     */
    public LinkedList<String> getConnections() {
        return connections;
    }

    /**
     * @return whether the panel is open
     */
    public boolean getOpen() {
        return open;
    }

    /**
     * @return true, if a dynamic PostGIS layer is in the WMS
     */
    public boolean getHasWmsConnection() {
        return hasWmsConnection;
    }

    /**
     * @return the selected wms connection
     */
    public String getSelectedWmsConnection() {
        return selectedWmsConnection;
    }

    /**
     * @return the wms connections
     */
    public LinkedList<String> getWmsConnections() {
        return wmsConnections;
    }

    /**
     * @return true, if num schemas &gt; 1
     */
    public boolean getManySchemas() {
        return open && manySchemas;
    }

    /**
     * @param evt
     */
    public void switchOpen( ActionEvent evt ) {
        open = !open;
        if ( open ) {
            fetchSchemas( null );
        }
    }

    /**
     * @param conn
     */
    public void setSelectedWmsConnection( String conn ) {
        selectedWmsConnection = conn;
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
     * @return the crs
     */
    public String getCrs() {
        int srid = findSrid( selectedConnection, selectedTable, selectedSchema );
        return crs = srid > 0 ? "EPSG:" + srid : "EPSG:4326";
    }

    /**
     * @param c
     */
    public void setCrs( String c ) {
        crs = c;
    }

    /**
     * @return all schemas
     */
    public LinkedList<String> getSchemas() {
        return schemas;
    }

    /**
     * @return the selected schema
     */
    public String getSelectedSchema() {
        return selectedSchema;
    }

    /**
     * @param table
     */
    public void setSelectedTable( String table ) {
        selectedTable = table;
    }

    /**
     * @param schema
     */
    public void setSelectedSchema( String schema ) {
        selectedSchema = schema;
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
    }

    /**
     * @return the selected connection
     */
    public String getSelectedConnection() {
        return selectedConnection;
    }

    /**
     * @param c
     */
    public void setSelectedConnection( String c ) {
        selectedConnection = c;
    }

    /**
     * @return the available tables
     */
    public LinkedList<String> getTables() {
        return tables;
    }

    /**
     * @return the selected table
     */
    public String getSelectedTable() {
        return selectedTable;
    }

}
