//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.io.sdeapi;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.TimeTools;
import org.deegree.model.spatialschema.Geometry;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeUpdate;

/**
 * the class offers access to the transactional behavior of the a connection to ArcSDE
 *
 * @version $Revision$ $Date$
 */
public class Transaction {

    private static ILogger LOG = LoggerFactory.getLogger( Transaction.class );

    // Connection to SDE
    private SeConnection conn = null;

    // Currently opened Layer and associated Table
    private SeLayer layer = null;

    private HashMap<String, SeColumnDefinition> colDefs = null;

    private ArrayList<SeColumnDefinition> colDefsList = null;

    // Current Spatial Filter - a BoundingBox
    private SeShape spatialFilter = null;

    /**
     * Creates a new SpatialQuery object.
     *
     * @param server
     * @param port
     * @param database
     * @param user
     * @param password
     *
     * @throws SeException
     */
    public Transaction( String server, int port, String database, String user, String password ) throws SeException {
        openConnection( server, port, database, user, password );
    }

    /**
     * Connect to the ArcSDE server <br>
     * throws SeException
     *
     * @param server
     * @param port
     * @param database
     * @param user
     * @param password
     * @throws SeException
     */
    public void openConnection( String server, int port, String database, String user, String password )
                            throws SeException {

        conn = new SeConnection( server, port, database, user, password );

    }

    /**
     * Close the current connection to the ArcSDE server <br>
     * throws SeException
     *
     * @throws SeException
     */
    public void closeConnection()
                            throws SeException {
        conn.close();
    }

    /**
     * Set a SDE layer to work on and appropriate table <br>
     * throws SeException
     *
     * @param layername
     * @throws SeException
     */
    public void setLayer( String layername )
                            throws SeException {

        Vector<?> layerList = conn.getLayers();
        String spatialCol = "";

        for ( int i = 0; i < layerList.size(); i++ ) {
            SeLayer layer = (SeLayer) layerList.elementAt( i );

            if ( layer.getQualifiedName().trim().equalsIgnoreCase( layername ) ) {
                spatialCol = layer.getSpatialColumn();
                break;
            }
        }

        layer = new SeLayer( conn, layername, spatialCol );
        SeTable table = new SeTable( conn, layer.getQualifiedName() );
        SeColumnDefinition[] cols = table.describe();
        colDefs = new HashMap<String, SeColumnDefinition>();
        colDefsList = new ArrayList<SeColumnDefinition>();
        for ( int i = 0; i < cols.length; i++ ) {
            colDefs.put( cols[i].getName(), cols[i] );
            colDefsList.add( cols[i] );
        }

    }

    /**
     * Set a SpatialFilter to Query (BoundingBox) <br>
     * throws SeException
     *
     * @param minx
     * @param miny
     * @param maxx
     * @param maxy
     * @throws SeException
     */
    public void setSpatialFilter( double minx, double miny, double maxx, double maxy )
                            throws SeException {

        spatialFilter = new SeShape( layer.getCoordRef() );

        SeExtent extent = new SeExtent( minx, miny, maxx, maxy );
        spatialFilter.generateRectangle( extent );

    }

    /**
     * inserts a feature into the ArcSDE
     *
     * @param inRow
     *            feature/row to be inserted
     *
     * @throws SeException
     * @throws DeegreeSeException
     */
    public void insertFeature( HashMap<?, ?> inRow )
                            throws SeException, DeegreeSeException {

        ArrayList<String> list = new ArrayList<String>();
        // get all fields of the row where the values are not null
        for ( int i = 0; i < colDefsList.size(); i++ ) {
            SeColumnDefinition cd = colDefsList.get( i );
            if ( inRow.get( cd.getName() ) != null || inRow.get( cd.getName().toUpperCase() ) != null ) {
                list.add( cd.getName() );
            }
        }
        String[] columns = list.toArray( new String[list.size()] );

        SeInsert insert = null;
        try {
            // create an insert object
            insert = new SeInsert( conn );
            insert.intoTable( layer.getName(), columns );
            insert.setWriteMode( true );

            SeRow row = insert.getRowToSet();
            SeColumnDefinition[] cols = row.getColumns();

            // get reference system
            SeCoordinateReference coordref = layer.getCoordRef();
            for ( int i = 0; i < cols.length; i++ ) {
                Object o = inRow.get( cols[i].getName() );
                if ( o == null ) {
                    o = inRow.get( cols[i].getName().toUpperCase() );
                }
                if ( o != null ) {
                    int type = cols[i].getType();
                    row = setValue( row, i, type, o, coordref );
                }
            }
            // perform insert operation
            insert.execute();
            insert.flushBufferedWrites();
        } catch ( SeException e ) {
            throw e;
        } finally {
            // Making sure the insert stream was closed. If the stream isn't closed,
            // the resources used by the stream will be held/locked by the stream
            // until the associated connection is closed.
            try {
                insert.close();
            } catch ( SeException se ) {
                se.printStackTrace();
            }
        }

    }

    /**
     * fills the passed row with the also passed value considering its type
     *
     * @param row
     *            SDE row to insert
     * @param pos
     *            position where to set the value in the row
     * @param type
     *            value type
     * @param value
     *            value to insert
     */
    private SeRow setValue( SeRow row, int pos, int type, Object value, SeCoordinateReference crs )
                            throws SeException, DeegreeSeException {

        switch ( type ) {
        case SeColumnDefinition.TYPE_BLOB: {
            if ( value == null ) {
                row.setBlob( pos, null );
            } else {
                row.setBlob( pos, (ByteArrayInputStream) value );
            }
            break;
        }
        case SeColumnDefinition.TYPE_DATE: {
            if ( value != null && value instanceof String ) {
                value = TimeTools.createCalendar( (String) value ).getTime();
            }
            row.setDate( pos, (Date) value );
            break;
        }
        case SeColumnDefinition.TYPE_FLOAT64: {
            if ( value != null && value instanceof String ) {
                value = new Double( (String) value );
            }
            row.setDouble( pos, (Double) value );
            break;
        }
        case SeColumnDefinition.TYPE_FLOAT32: {
            if ( value != null && value instanceof String ) {
                value = new Float( (String) value );
            }
            row.setFloat( pos, (Float) value );
            break;
        }
        case SeColumnDefinition.TYPE_INT32: {
            if ( value != null && value instanceof String ) {
                value = new Integer( (String) value );
            }
            row.setInteger( pos, (Integer) value );
            break;
        }
        case SeColumnDefinition.TYPE_RASTER: {
            row.setBlob( pos, (ByteArrayInputStream) value );
            break;
        }
        case SeColumnDefinition.TYPE_SHAPE: {
            // TODO
            /*
             * if (value != null && value instanceof String) { // if value is a string try to
             * convert it into GML try { value = GMLGeometryAdapter.wrap( (String)value ); } catch
             * (Exception e) { throw new SeInvalidShapeException( "the passed value "+ "isn't a GML
             * geometry\n" + e); } } if (value != null && value instanceof GMLGeometry) { // if
             * value is a GML convert it into a deegree geometry try { value =
             * GMLGeometryAdapter.wrap( ((GMLGeometry)value).getAsElement() ); } catch (Exception e) {
             * throw new SeInvalidShapeException( "the passed value/GML "+ "can't be transformed "+ "
             * to a deegree geometry\n" + e); } }
             */
            try {
                if ( value != null ) {
                    SeShape shp = SDEAdapter.export( (Geometry) value, crs );
                    row.setShape( pos, shp );
                } else {
                    row.setShape( pos, null );
                }
            } catch ( Exception e ) {
                throw new DeegreeSeException( "the passed geometry can't  " + "be transformed to a SeShape\n" + e );
            }
            break;
        }
        case SeColumnDefinition.TYPE_INT16: {
            if ( value != null && value instanceof String ) {
                value = new Short( (String) value );
            }
            row.setShort( pos, (Short) value );
            break;
        }
        case SeColumnDefinition.TYPE_STRING: {
            row.setString( pos, (String) value );
            break;
        }
        }

        return row;
    }

    /**
     * updates a feature of the ArcSDE
     *
     * @param inRow
     *            update data
     * @param where
     *            none spatial condtions to limit the targeted rows
     * @param extent
     *            spatial condtion to limit the targeted rows (not considered yet)
     *
     * @throws SeException
     * @throws DeegreeSeException
     */
    public void updateFeature( HashMap<?, ?> inRow, String where, Geometry extent )
                            throws SeException, DeegreeSeException {

        ArrayList<String> list = new ArrayList<String>();

        // get all fields of the row where the values are not null
        Iterator<?> iterator = inRow.keySet().iterator();
        while ( iterator.hasNext() ) {
            Object o = iterator.next();
            if ( o != null ) {
                list.add( (String) o );
            }
        }
        String[] columns = list.toArray( new String[list.size()] );

        // get rows to be updated
        // SeQuery query = new SeQuery( conn, columns, sqlCons );
        SeUpdate update = new SeUpdate( conn );
        SeTable table = new SeTable( conn, layer.getQualifiedName() );
        // TODO use also spatial conditions
        update.toTable( table.getName(), columns, where.trim() );
        update.setWriteMode( true );

        SeRow row = update.getRowToSet();
        SeCoordinateReference coordref = layer.getCoordRef();

        if ( row != null ) {
            // while ( row != null ) {
            for ( int i = 0; i < columns.length; i++ ) {
                int type = colDefs.get( columns[i] ).getType();
                row = setValue( row, i, type, inRow.get( columns[i] ), coordref );
            }
            // row = update.getRowToSet();
            // }
            update.execute();
        } else {
            LOG.logWarning( "No rows fetched/updated" );
        }

        update.close();

    }

    /**
     * deletes a feature from the ArcSDE
     *
     * @param where
     *            none spatial condtions to limit the targeted rows
     * @param extent
     *            spatial condtion to limit the targeted rows (not considered yet)
     *
     * @throws SeException
     */
    public void deleteFeature( String where, Geometry extent )
                            throws SeException {

        // TODO use also spatial conditions
        SeDelete delete = new SeDelete( conn );
        delete.fromTable( layer.getQualifiedName(), where );
        delete.close();

    }

}
