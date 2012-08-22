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
import java.util.Vector;

import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceInterpolation;
import org.deegree.model.spatialschema.SurfaceInterpolationImpl;
import org.deegree.model.table.DefaultTable;
import org.deegree.model.table.Table;
import org.deegree.model.table.TableException;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeShapeFilter;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;

/**
 * This class handles a complete ArcSDE request: If instanciated, the class can open a
 * connection/instance of the specified ArcSDE server, set a bounding box as a spatial filter to
 * query the defined layer. The resultset of the query contains the geometries as well as the
 * tabular data associated with them. The table is stored as a deegree Table object whereas the
 * geometries are stored as an array of deegree GM_Objects. Depending on the datatype of the
 * geometries, the array of GM_Objects might be GM_Point, GM_Curve etc.
 * <p>
 * Some bits of sample code to create a query:
 * <p>
 * <code>
 *        SpatialQuery sq = new SpatialQuery();<br>
 *        try {<br>
 *        &nbsp;sq.openConnection(server, instance, database, user, password);<br>
 *        &nbsp;sq.setLayer(layer);<br>
 *        &nbsp;sq.setSpatialFilter(minX, minY, maxX, maxY);<br>
 *        &nbsp;sp.runSpatialQuery();<br>
 *        &nbsp;GM_Object[] deegree_gm_obj = sq.getGeometries();<br>
 *        &nbsp;Table deegree_table = sq.getTable();<br>
 *        &nbsp;sq.closeConnection();<br>
 *        } catch ( SeException sexp ) {<br>
 *        }<br>
 * </code>
 *
 * @author <a href="mailto:bedel@giub.uni-bonn.de">Markus Bedel</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
public class SpatialQuery {

    private static final ILogger LOG = LoggerFactory.getLogger( SpatialQuery.class );

    // Connection to SDE
    private SeConnection conn = null;

    // Currently opened Layer and associated Table
    private SeLayer layer = null;

    // Current Spatial Filter - a BoundingBox
    private SeShape spatialFilter = null;

    private SeTable table = null;

    // The Query ResultObjects
    private Geometry[] deegreeGeom = null;

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
    public SpatialQuery( String server, int port, String database, String user, String password ) throws SeException {
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
        String spatialCol = null;

        for ( int i = 0; i < layerList.size(); i++ ) {
            SeLayer layer = (SeLayer) layerList.elementAt( i );
            if ( layer.getQualifiedName().trim().equalsIgnoreCase( layername ) ) {
                spatialCol = layer.getSpatialColumn();
                break;
            }
        }

        layer = new SeLayer( conn, layername, spatialCol );
        table = new SeTable( conn, layer.getQualifiedName() );

    }

    /**
     * Get the current SDE layer <br>
     * returns null if it not yet set.
     *
     * @return null if it not yet set.
     */
    public SeLayer getLayer() {
        return layer;
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

        Envelope layerBBox = GeometryFactory.createEnvelope( layer.getExtent().getMinX(), layer.getExtent().getMinY(),
                                                             layer.getExtent().getMaxX(), layer.getExtent().getMaxY(),
                                                             null );

        Envelope query = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, null );
        query = query.createIntersection( layerBBox );

        if ( query != null ) {
            spatialFilter = new SeShape( layer.getCoordRef() );
            SeExtent extent = new SeExtent( query.getMin().getX(), query.getMin().getY(), query.getMax().getX(),
                                            query.getMax().getY() );
            spatialFilter.generateRectangle( extent );
        } else {
            spatialFilter = null;
        }

    }

    /**
     * Get the current Spatial Filter <br>
     * returns null if it not yet set.
     *
     * @return null if it not yet set.
     */
    public SeShape getSpatialFilter() {
        return spatialFilter;
    }

    /**
     * Get GM_Object[] containing the queried Geometries <br>
     * returns null if no query has been done yet.
     *
     * @return null if no query has been done yet.
     */
    public Geometry[] getGeometries() {
        return deegreeGeom;
    }

    /**
     * Runs a spatial query against the opened layer using the specified spatial filter. <br>
     * throws SeException
     *
     * @param cols
     * @return the resulting table
     * @throws SeException
     * @throws DeegreeSeException
     */
    public Table runSpatialQuery( String[] cols )
                            throws SeException, DeegreeSeException {

        Table deegreeTable = null;
        if ( spatialFilter != null ) {
            SeShapeFilter[] filters = new SeShapeFilter[1];

            filters[0] = new SeShapeFilter( layer.getQualifiedName(), layer.getSpatialColumn(), spatialFilter,
                                            SeFilter.METHOD_ENVP );

            SeColumnDefinition[] tableDef = table.describe();
            if ( cols == null || cols.length == 0 ) {
                cols = new String[tableDef.length];
                for ( int i = 0; i < tableDef.length; i++ ) {
                    cols[i] = tableDef[i].getName();
                }
            }

            SeSqlConstruct sqlCons = new SeSqlConstruct( layer.getQualifiedName() );
            SeQuery spatialQuery = new SeQuery( conn, cols, sqlCons );

            spatialQuery.prepareQuery();
            spatialQuery.setSpatialConstraints( SeQuery.SE_OPTIMIZE, false, filters );
            spatialQuery.execute();

            SeRow row = spatialQuery.fetch();

            int numRows = 0;
            if ( row != null ) {
                int numCols = row.getNumColumns();
                // Fetch all the features that satisfied the query
                deegreeTable = initTable( row );

                ArrayList<Geometry> list = new ArrayList<Geometry>( 20000 );
                Object[] tableObj = null;

                while ( row != null ) {
                    int colNum = 0;
                    tableObj = new Object[deegreeTable.getColumnCount()];

                    for ( int i = 0; i < numCols; i++ ) {
                        SeColumnDefinition colDef = row.getColumnDef( i );

                        if ( row.getIndicator( (short) i ) != SeRow.SE_IS_NULL_VALUE ) {
                            switch ( colDef.getType() ) {
                            case SeColumnDefinition.TYPE_INT16:
                                tableObj[colNum++] = row.getShort( i );
                                break;
                            case SeColumnDefinition.TYPE_INT32:
                                tableObj[colNum++] = row.getInteger( i );
                                break;
                            case SeColumnDefinition.TYPE_FLOAT32:
                                tableObj[colNum++] = row.getFloat( i );
                                break;
                            case SeColumnDefinition.TYPE_FLOAT64:
                                tableObj[colNum++] = row.getDouble( i );
                                break;
                            case SeColumnDefinition.TYPE_STRING:
                                tableObj[colNum++] = row.getString( i );
                                break;
                            case SeColumnDefinition.TYPE_BLOB:
                                ByteArrayInputStream bis = (ByteArrayInputStream) row.getObject( i );
                                tableObj[colNum++] = bis;
                                break;
                            case SeColumnDefinition.TYPE_DATE:
                                tableObj[colNum++] = row.getTime( i ).getTime();
                                break;
                            case SeColumnDefinition.TYPE_RASTER:
                                LOG.logInfo( colDef.getName() + " : Cant handle this" );
                                break;
                            case SeColumnDefinition.TYPE_SHAPE:
                                SeShape spVal = row.getShape( i );
                                createGeometry( spVal, list );
                                break;
                            default:
                                LOG.logInfo( "Unknown Table DataType" );
                                break;
                            } // End switch(type)
                        } // End if
                    } // End for

                    numRows++;

                    try {
                        deegreeTable.appendRow( tableObj );
                    } catch ( TableException tex ) {
                        throw new DeegreeSeException( tex.toString() );
                    }

                    row = spatialQuery.fetch();
                } // End while
                spatialQuery.close();

                deegreeGeom = list.toArray( new Geometry[list.size()] );
            } else {
                try {
                    deegreeTable = new DefaultTable( layer.getQualifiedName(), new String[] { "NONE" },
                                                     new int[] { Types.VARCHAR }, 2 );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
                deegreeGeom = new Geometry[0];
            }
        } else {
            try {
                deegreeTable = new DefaultTable( layer.getQualifiedName(), new String[] { "NONE" },
                                                 new int[] { Types.VARCHAR }, 2 );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            deegreeGeom = new Geometry[0];
        }

        return deegreeTable;
    } // End method runSpatialQuery

    /**
     * Initialize Table object - used with first row of the SpatialQuery This method sets the
     * TableName, TableColumnNames and their DataTypes <br>
     * throws SeException
     */
    private Table initTable( SeRow row )
                            throws SeException, DeegreeSeException {
        ArrayList<String> colNames = new ArrayList<String>( 50 );
        ArrayList<Integer> colTypes = new ArrayList<Integer>( 50 );
        Table deegreeTable = null;
        SeColumnDefinition colDef = null;

        for ( int i = 0; i < row.getNumColumns(); i++ ) {
            try {
                colDef = row.getColumnDef( i );
            } catch ( SeException sexp ) {
                sexp.printStackTrace();
                throw new DeegreeSeException( sexp.toString() );
            }

            switch ( colDef.getType() ) {
            case SeColumnDefinition.TYPE_INT16:
                colNames.add( colDef.getName().toUpperCase() );
                colTypes.add( new Integer( Types.SMALLINT ) );
                break;
            case SeColumnDefinition.TYPE_INT32:
                colNames.add( colDef.getName().toUpperCase() );
                colTypes.add( new Integer( Types.INTEGER ) );
                break;
            case SeColumnDefinition.TYPE_FLOAT32:
                colNames.add( colDef.getName().toUpperCase() );
                colTypes.add( new Integer( Types.FLOAT ) );
                break;
            case SeColumnDefinition.TYPE_FLOAT64:
                colNames.add( colDef.getName().toUpperCase() );
                colTypes.add( new Integer( Types.DOUBLE ) );
                break;
            case SeColumnDefinition.TYPE_STRING:
                colNames.add( colDef.getName().toUpperCase() );
                colTypes.add( new Integer( Types.VARCHAR ) );
                break;
            case SeColumnDefinition.TYPE_BLOB:
                // there is an open issue with fetching blobs,
                // look at this document:
                // "ArcSDE 8.1 Java API - BLOB columns"
                // http://support.esri.com/Search/KbDocument.asp?dbid=17068
                colNames.add( colDef.getName().toUpperCase() );
                colTypes.add( new Integer( Types.ARRAY ) );
                break;
            case SeColumnDefinition.TYPE_DATE:
                colNames.add( colDef.getName().toUpperCase() );
                colTypes.add( new Integer( Types.DATE ) );
                break;
            default:
                break;
            }
        }

        String[] colN = new String[colNames.size()];
        colN = colNames.toArray( colN );

        int[] colT = new int[colTypes.size()];
        for ( int i = 0; i < colT.length; i++ ) {
            colT[i] = colTypes.get( i ).intValue();
        }

        try {
            deegreeTable = new DefaultTable( layer.getQualifiedName(), colN, colT, 20000 );
        } catch ( TableException tex ) {
            tex.printStackTrace();
            throw new DeegreeSeException( tex.toString() );
        }
        return deegreeTable;
    } // End Method initTable

    /**
     * CreateGeometry - used with every row of the SpatialQuery Depending on the layers' geometries
     * datatype different operations are made to create the appropriate object. <br>
     * Available ArcSDE ShapeTypes: <br>
     * TYPE_POINT (impl) <br>
     * TYPE_MULTI_POINT (impl) <br>
     * TYPE_SIMPLE_LINE (impl) <br>
     * TYPE_MULTI_SIMPLE_LINE (impl) <br>
     * TYPE_LINE (impl) <br>
     * TYPE_MULTI_LINE (impl) <br>
     * TYPE_POLYGON (impl) <br>
     * TYPE_MULTI_POLYGON (impl) <br>
     * TYPE_NIL (impl)
     *
     * <br>
     * throws SeException
     */
    private void createGeometry( SeShape shape, ArrayList<Geometry> list )
                            throws SeException, DeegreeSeException {

        int shptype = shape.getType();

        ArrayList<?> al = shape.getAllPoints( SeShape.TURN_DEFAULT, true );
        // Retrieve the array of SDEPoints
        SDEPoint[] points = (SDEPoint[]) al.get( 0 );
        // Retrieve the part offsets array.
        int[] partOffset = (int[]) al.get( 1 );
        // Retrieve the sub-part offsets array.
        int[] subPartOffset = (int[]) al.get( 2 );

        int numPoints = shape.getNumOfPoints();

        int numParts = shape.getNumParts();

        switch ( shptype ) {
        // a single point
        case SeShape.TYPE_NIL:
            Point gmPoint = GeometryFactory.createPoint( -9E9, -9E9, null );
            list.add( gmPoint );
            LOG.logInfo( "Found SeShape.TYPE_NIL." );
            LOG.logInfo( "The queried layer does not have valid geometries" );
            break;
        // a single point
        case SeShape.TYPE_POINT:
            gmPoint = GeometryFactory.createPoint( points[0].getX(), points[0].getY(), null );
            list.add( gmPoint );
            break;
        // an array of points
        case SeShape.TYPE_MULTI_POINT:
            Point[] gmPoints = new Point[numPoints];

            for ( int pt = 0; pt < numPoints; pt++ ) {
                gmPoints[pt] = GeometryFactory.createPoint( points[pt].getX(), points[pt].getY(), null );
            }

            try {
                MultiPoint gmMultiPoint = GeometryFactory.createMultiPoint( gmPoints );
                list.add( gmMultiPoint );
            } catch ( Exception gme ) {
                gme.printStackTrace();
                throw new DeegreeSeException( gme.toString() );
            }

            break;
        // a single line, simple as it does not intersect itself
        case SeShape.TYPE_SIMPLE_LINE:
            // or a single, non-simple line
        case SeShape.TYPE_LINE:

            Position[] gmSimpleLinePosition = new Position[numPoints];

            for ( int pt = 0; pt < numPoints; pt++ ) {
                gmSimpleLinePosition[pt] = GeometryFactory.createPosition( points[pt].getX(), points[pt].getY() );
            }

            try {
                Curve gmCurve = GeometryFactory.createCurve( gmSimpleLinePosition, null );
                list.add( gmCurve );
            } catch ( Exception gme ) {
                gme.printStackTrace();
                throw new DeegreeSeException( gme.toString() );
            }

            break;
        // an array of lines, simple as they do not intersect with themself
        case SeShape.TYPE_MULTI_SIMPLE_LINE:
            // or an array of non-simple lines
        case SeShape.TYPE_MULTI_LINE:

            Curve[] gmCurves = new Curve[numParts];

            for ( int partNo = 0; partNo < numParts; partNo++ ) {
                int lastPoint = shape.getNumPoints( partNo + 1, 1 ) + partOffset[partNo];
                Position[] gmMultiSimpleLinePosition = new Position[shape.getNumPoints( partNo + 1, 1 )];
                int i = 0;

                for ( int pt = partOffset[partNo]; pt < lastPoint; pt++ ) {
                    gmMultiSimpleLinePosition[i] = GeometryFactory.createPosition( points[pt].getX(), points[pt].getY() );
                    i++;
                }

                try {
                    gmCurves[partNo] = GeometryFactory.createCurve( gmMultiSimpleLinePosition, null );
                } catch ( Exception gme ) {
                    gme.printStackTrace();
                    throw new DeegreeSeException( gme.toString() );
                }
            }

            try {
                MultiCurve gmMultiCurve = GeometryFactory.createMultiCurve( gmCurves );
                list.add( gmMultiCurve );
            } catch ( Exception gme ) {
                gme.printStackTrace();
                throw new DeegreeSeException( gme.toString() );
            }

            break;
        // a single polygon which might contain islands
        case SeShape.TYPE_POLYGON:

            int numSubParts = shape.getNumSubParts( 1 );
            Position[] gmPolygonExteriorRing = new Position[shape.getNumPoints( 1, 1 )];

            int kk = shape.getNumPoints( 1, 1 );
            for ( int pt = 0; pt < kk; pt++ ) {
                gmPolygonExteriorRing[pt] = GeometryFactory.createPosition( points[pt].getX(), points[pt].getY() );
            }

            Position[][] gmPolygonInteriorRings = null;

            // if it is a donut create inner rings
            if ( numSubParts > 1 ) {
                gmPolygonInteriorRings = new Position[numSubParts - 1][];

                int j = 0;

                for ( int subPartNo = 1; subPartNo < numSubParts; subPartNo++ ) {
                    int lastPoint = shape.getNumPoints( 1, subPartNo + 1 ) + subPartOffset[subPartNo];
                    Position[] gmPolygonPosition = new Position[shape.getNumPoints( 1, subPartNo + 1 )];
                    int i = 0;

                    for ( int pt = subPartOffset[subPartNo]; pt < lastPoint; pt++ ) {
                        gmPolygonPosition[i] = GeometryFactory.createPosition( points[pt].getX(), points[pt].getY() );
                        i++;
                    }

                    gmPolygonInteriorRings[j] = gmPolygonPosition;
                    j++;
                }
            }

            try {
                Surface gmSurface = GeometryFactory.createSurface( gmPolygonExteriorRing, gmPolygonInteriorRings,
                                                                   new SurfaceInterpolationImpl(), null );
                list.add( gmSurface );
            } catch ( Exception gme ) {
                gme.printStackTrace();
                throw new DeegreeSeException( gme.toString() );
            }

            break;
        // an array of polygons which might contain islands
        case SeShape.TYPE_MULTI_POLYGON:

            Surface[] gmMultiPolygonSurface = getMultiPolygon( shape, points, partOffset, subPartOffset );

            try {
                MultiSurface gmMultiSurface = GeometryFactory.createMultiSurface( gmMultiPolygonSurface );
                list.add( gmMultiSurface );
            } catch ( Exception gme ) {
                gme.printStackTrace();
                throw new DeegreeSeException( gme.toString() );
            }

            break;
        default:
            LOG.logInfo( "Unknown GeometryType - ID: " + shape.getType() );
            break;
        } // End of switch
    } // End Method createGeometry

    /**
     * @param shape
     * @param points
     * @param partOffset
     * @param subPartOffset
     * @throws SeException
     */
    private Surface[] getMultiPolygon( SeShape shape, SDEPoint[] points, int[] partOffset, int[] subPartOffset )
                            throws SeException, DeegreeSeException {
        Surface[] surfaces = new Surface[partOffset.length];
        int hh = 0;
        for ( int i = 0; i < partOffset.length; i++ ) {
            // cnt = number of all rings of the current polygon (part)
            int cnt = shape.getNumSubParts( i + 1 );

            // exterior ring
            int count = shape.getNumPoints( i + 1, 1 );
            Position[] ex = new Position[count];
            int off = subPartOffset[hh];
            for ( int j = 0; j < count; j++ ) {
                ex[j] = GeometryFactory.createPosition( points[j + off].getX(), points[j + off].getY() );
            }

            // interior ring
            Position[][] inn = null;
            if ( cnt > 1 ) {
                inn = new Position[cnt - 1][];
            }
            hh++;
            for ( int j = 1; j < cnt; j++ ) {
                inn[j - 1] = new Position[shape.getNumPoints( i + 1, j + 1 )];
                off = subPartOffset[hh];
                for ( int k = 0; k < inn[j - 1].length; k++ ) {
                    inn[j - 1][k] = GeometryFactory.createPosition( points[j + off - 1].getX(),
                                                                    points[j + off - 1].getY() );
                }
                hh++;
            }

            try {
                SurfaceInterpolation si = new SurfaceInterpolationImpl();
                surfaces[i] = GeometryFactory.createSurface( ex, inn, si, null );
            } catch ( Exception e ) {
                throw new DeegreeSeException( StringTools.stackTraceToString( e ) );
            }
        }

        return surfaces;
    }

} // End Class SpatialQueryEx
