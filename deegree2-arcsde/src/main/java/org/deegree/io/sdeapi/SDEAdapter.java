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

import java.util.ArrayList;

import org.deegree.framework.util.TimeTools;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceBoundary;
import org.deegree.model.spatialschema.SurfaceInterpolationImpl;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeXmlDoc;

/**
 * Adapter class for exporting deegree geometries to WKT and to wrap WKT code geometries to deegree
 * geometries.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class SDEAdapter {

    // private static final ILogger LOG = LoggerFactory.getLogger( SDEAdapter.class );

    /**
     *
     * @param shape
     * @param coordinateSystem 
     * @return the corresponding <tt>Geometry</tt>
     * @throws GeometryException
     *             if type unsupported or conversion failed
     * @throws SeException
     */
    public static Geometry wrap( SeShape shape, CoordinateSystem coordinateSystem )
                            throws GeometryException, SeException {

        Geometry geo = null;

        if ( shape == null ) {
            return null;
        }

        switch ( shape.getType() ) {
        case SeShape.TYPE_POINT: {
            geo = wrapPoint( shape, coordinateSystem );
            break;
        }
        case SeShape.TYPE_SIMPLE_LINE:
        case SeShape.TYPE_LINE: {
            geo = wrapCurve( shape, coordinateSystem );
            break;
        }
        case SeShape.TYPE_POLYGON: {
            geo = wrapSurface( shape, coordinateSystem );
            break;
        }
        case SeShape.TYPE_MULTI_POINT: {
            geo = wrapMultiPoint( shape, coordinateSystem );
            break;
        }
        case SeShape.TYPE_MULTI_SIMPLE_LINE:
        case SeShape.TYPE_MULTI_LINE: {
            geo = wrapMultiCurve( shape, coordinateSystem );
            break;
        }
        case SeShape.TYPE_MULTI_POLYGON: {
            geo = wrapMultiSurface( shape, coordinateSystem );
            break;
        }
        }

        return geo;
    }

    /**
     * @param geom
     *            geometry
     * @param crs
     *
     * @return the shape
     * @throws GeometryException
     * @throws SeException
     */
    public static SeShape export( Geometry geom, SeCoordinateReference crs )
                            throws GeometryException, SeException {

        SeShape sb = null;

        if ( geom instanceof Point ) {
            sb = export( (Point) geom, crs );
        } else if ( geom instanceof Curve ) {
            sb = export( (Curve) geom, crs );
        } else if ( geom instanceof Surface ) {
            sb = export( (Surface) geom, crs );
        } else if ( geom instanceof MultiPoint ) {
            sb = export( (MultiPoint) geom, crs );
        } else if ( geom instanceof MultiCurve ) {
            sb = export( (MultiCurve) geom, crs );
        } else if ( geom instanceof MultiSurface ) {
            sb = export( (MultiSurface) geom, crs );
        }

        return sb;
    }

    /**
     * @param point
     *            point geometry
     *
     */
    private static SeShape export( Point point, SeCoordinateReference crs )
                            throws SeException {

        SDEPoint pt = new SDEPoint( point.getX(), point.getY() );
        SeShape shp = new SeShape( crs );
        shp.generatePoint( 1, new SDEPoint[] { pt } );

        return shp;
    }

    /**
     *
     * @param cur
     *            curve geometry
     *
     *
     * @throws GeometryException
     */
    private static SeShape export( Curve cur, SeCoordinateReference crs )
                            throws GeometryException, SeException {

        Position[] pos = cur.getAsLineString().getPositions();
        SDEPoint[] ptArray = new SDEPoint[pos.length];

        for ( int i = 0; i < pos.length; i++ ) {
            ptArray[i] = new SDEPoint( pos[i].getX(), pos[i].getY() );
        }
        int numParts = 1;
        int[] partOffSets = new int[numParts];
        partOffSets[0] = 0;

        SeShape line = new SeShape( crs );
        line.generateSimpleLine( pos.length, numParts, partOffSets, ptArray );

        return line;
    }

    /**
     *
     *
     * @param sur
     *
     *
     * @throws SeException
     */
    private static SeShape export( Surface sur, SeCoordinateReference crs )
                            throws SeException {

        int numParts = 1;
        SurfaceBoundary sbo = sur.getSurfaceBoundary();
        Ring ex = sbo.getExteriorRing();
        Ring[] rings = sbo.getInteriorRings();

        int[] partOffsets = new int[numParts];
        partOffsets[0] = 0;
        int numPts = sbo.getExteriorRing().getPositions().length;
        if ( rings != null ) {
            for ( int i = 0; i < rings.length; i++ ) {
                numPts += rings[i].getPositions().length;
                numParts++;
            }
        }

        SDEPoint[] ptArray = new SDEPoint[numPts];

        int cnt = 0;
        for ( int i = 0; i < ex.getPositions().length; i++ ) {
            ptArray[cnt++] = new SDEPoint( ex.getPositions()[i].getX(), ex.getPositions()[i].getY() );
        }

        if ( rings != null ) {
            for ( int k = 0; k < rings.length; k++ ) {
                for ( int i = 0; i < rings[k].getPositions().length; i++ ) {
                    ptArray[cnt++] = new SDEPoint( rings[k].getPositions()[i].getX(), rings[k].getPositions()[i].getY() );
                }
            }
        }

        SeShape polygon = new SeShape( crs );
        polygon.generatePolygon( numPts, numParts, partOffsets, ptArray );

        return polygon;
    }

    /**
     * @param mp
     * @param crs
     * @throws SeException
     */
    private static SeShape export( MultiPoint mp, SeCoordinateReference crs )
                            throws SeException {

        SDEPoint[] pt = new SDEPoint[mp.getSize()];

        for ( int i = 0; i < pt.length; i++ ) {
            pt[i] = new SDEPoint( mp.getPointAt( i ).getX(), mp.getPointAt( i ).getY() );
        }
        SeShape shp = new SeShape( crs );
        shp.generatePoint( pt.length, pt );

        return shp;
    }

    /**
     *
     *
     * @param mc
     *
     *
     * @throws GeometryException
     */
    private static SeShape export( MultiCurve mc, SeCoordinateReference crs )
                            throws GeometryException, SeException {

        int numParts = mc.getSize();
        int[] partOffSets = new int[numParts];
        int numPts = 0;
        for ( int i = 0; i < numParts; i++ ) {
            partOffSets[i] = numPts;
            numPts += mc.getCurveAt( i ).getAsLineString().getNumberOfPoints();
        }
        SDEPoint[] ptArray = new SDEPoint[numPts];
        int cnt = 0;
        for ( int k = 0; k < numParts; k++ ) {
            Position[] pos = mc.getCurveAt( k ).getAsLineString().getPositions();
            for ( int i = 0; i < pos.length; i++ ) {
                ptArray[cnt++] = new SDEPoint( pos[i].getX(), pos[i].getY() );
            }
        }

        SeShape line = new SeShape( crs );
        line.generateSimpleLine( numPts, numParts, partOffSets, ptArray );

        return line;
    }

    /**
     *
     *
     * @param ms
     *
     *
     * @throws SeException
     */
    private static SeShape export( MultiSurface ms, SeCoordinateReference crs )
                            throws SeException {

        int numParts = ms.getSize();
        int[] partOffSets = new int[numParts];
        int numPts = 0;
        for ( int i = 0; i < numParts; i++ ) {
            partOffSets[i] = numPts;
            SurfaceBoundary sbo = ms.getSurfaceAt( i ).getSurfaceBoundary();
            Ring ex = sbo.getExteriorRing();
            Ring[] inner = sbo.getInteriorRings();
            numPts += ex.getPositions().length;
            if ( inner != null ) {
                for ( int j = 0; j < inner.length; j++ ) {
                    numPts += inner[j].getPositions().length;
                }
            }
        }
        SDEPoint[] ptArray = new SDEPoint[numPts];
        int cnt = 0;
        for ( int k = 0; k < numParts; k++ ) {
            SurfaceBoundary sbo = ms.getSurfaceAt( k ).getSurfaceBoundary();
            Ring ex = sbo.getExteriorRing();
            Ring[] inner = sbo.getInteriorRings();
            Position[] pos = ex.getPositions();
            for ( int i = 0; i < pos.length; i++ ) {
                ptArray[cnt++] = new SDEPoint( pos[i].getX(), pos[i].getY() );
            }
            if ( inner != null ) {
                for ( int j = 0; j < inner.length; j++ ) {
                    pos = inner[j].getPositions();
                    for ( int i = 0; i < pos.length; i++ ) {
                        ptArray[cnt++] = new SDEPoint( pos[i].getX(), pos[i].getY() );
                    }
                }
            }
        }

        SeShape polygon = new SeShape( crs );
        polygon.generatePolygon( numPts, numParts, partOffSets, ptArray );

        return polygon;
    }

    /**
     * creates a Point from a SeShape
     *
     * @param shape
     * @param coordinateSystem 
     */
    private static Point wrapPoint( SeShape shape, CoordinateSystem coordinateSystem )
                            throws SeException {

        /*
         * ArrayList al = shape.getAllPoints( SeShape.TURN_DEFAULT, true ); // Retrieve the array of
         * SDEPoints SDEPoint[] points = (SDEPoint[])al.get( 0 );
         *
         * Point point = GeometryFactory.createPoint( points[0].getX(), points[0].getY(), null );
         */
        double[][][] xyz = shape.getAllCoords( SeShape.TURN_DEFAULT );
        Point point = GeometryFactory.createPoint( xyz[0][0][0], xyz[0][0][1], coordinateSystem );

        return point;
    }

    /**
     * creates a Curve from a SeShape
     *
     * @param shape
     * @param coordinateSystem 
     */
    private static Curve wrapCurve( SeShape shape, CoordinateSystem coordinateSystem )
                            throws GeometryException, SeException {

        ArrayList<?> al = shape.getAllPoints( SeShape.TURN_DEFAULT, true );
        // Retrieve the array of SDEPoints
        SDEPoint[] points = (SDEPoint[]) al.get( 0 );
        // // Retrieve the part offsets array.
        // int[] partOffset = (int[])al.get( 1 );
        // // Retrieve the sub-part offsets array.
        // int[] subPartOffset = (int[])al.get( 2 );

        int numPoints = shape.getNumOfPoints();

        Position[] gmSimpleLinePosition = new Position[numPoints];

        for ( int pt = 0; pt < numPoints; pt++ ) {
            gmSimpleLinePosition[pt] = GeometryFactory.createPosition( points[pt].getX(), points[pt].getY() );
        }

        Curve curve = GeometryFactory.createCurve( gmSimpleLinePosition, coordinateSystem );

        return curve;
    }

    /**
     * creates a Surface
     *
     * @param shape
     * @param coordinateSystem 
     */
    private static Surface wrapSurface( SeShape shape, CoordinateSystem coordinateSystem )
                            throws GeometryException, SeException {

        ArrayList<?> al = shape.getAllPoints( SeShape.TURN_DEFAULT, true );
        // Retrieve the array of SDEPoints
        SDEPoint[] points = (SDEPoint[]) al.get( 0 );
        // Retrieve the part offsets array.
        // int[] partOffset = (int[])al.get( 1 );
        // Retrieve the sub-part offsets array.
        int[] subPartOffset = (int[]) al.get( 2 );

        int numSubParts = shape.getNumSubParts( 1 );

        Position[] gmPolygonExteriorRing = new Position[shape.getNumPoints( 1, 1 )];

        for ( int pt = 0; pt < shape.getNumPoints( 1, 1 ); pt++ ) {
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

        Surface sur = GeometryFactory.createSurface( gmPolygonExteriorRing, gmPolygonInteriorRings,
                                                     new SurfaceInterpolationImpl(), coordinateSystem );

        return sur;
    }

    /**
     * creates a MultiPoint from a WKT
     *
     * @param shape
     * @param coordinateSystem 
     */
    private static MultiPoint wrapMultiPoint( SeShape shape, CoordinateSystem coordinateSystem )
                            throws SeException {

        ArrayList<?> al = shape.getAllPoints( SeShape.TURN_DEFAULT, true );
        // Retrieve the array of SDEPoints
        SDEPoint[] points = (SDEPoint[]) al.get( 0 );

        int numPoints = shape.getNumOfPoints();

        Point[] gmPoints = new Point[numPoints];

        for ( int pt = 0; pt < numPoints; pt++ ) {
            gmPoints[pt] = GeometryFactory.createPoint( points[pt].getX(), points[pt].getY(), coordinateSystem );
        }

        MultiPoint gmMultiPoint = GeometryFactory.createMultiPoint( gmPoints );

        return gmMultiPoint;
    }

    /**
     * creates a MultiCurve from a WKT
     *
     * @param shape
     * @param coordinateSystem 
     */
    private static MultiCurve wrapMultiCurve( SeShape shape, CoordinateSystem coordinateSystem )
                            throws GeometryException, SeException {

        ArrayList<?> al = shape.getAllPoints( SeShape.TURN_DEFAULT, true );
        // Retrieve the array of SDEPoints
        SDEPoint[] points = (SDEPoint[]) al.get( 0 );

        // Retrieve the part offsets array.
        int[] partOffset = (int[]) al.get( 1 );

        int numParts = shape.getNumParts();

        Curve[] gmCurves = new Curve[numParts];

        for ( int partNo = 0; partNo < numParts; partNo++ ) {
            int lastPoint = shape.getNumPoints( partNo + 1, 1 ) + partOffset[partNo];
            Position[] gmMultiSimpleLinePosition = new Position[shape.getNumPoints( partNo + 1, 1 )];
            int i = 0;

            for ( int pt = partOffset[partNo]; pt < lastPoint; pt++ ) {
                gmMultiSimpleLinePosition[i] = GeometryFactory.createPosition( points[pt].getX(), points[pt].getY() );
                i++;
            }

            gmCurves[partNo] = GeometryFactory.createCurve( gmMultiSimpleLinePosition, coordinateSystem );
        }

        MultiCurve gmMultiCurve = GeometryFactory.createMultiCurve( gmCurves );

        return gmMultiCurve;
    }

    /**
     * creates a MultiSurface from a WKT
     *
     * @param shape
     * @param coordinateSystem 
     */
    private static MultiSurface wrapMultiSurface( SeShape shape, CoordinateSystem coordinateSystem )
                            throws GeometryException, SeException {

        ArrayList<?> al = shape.getAllPoints( SeShape.TURN_DEFAULT, true );
        // Retrieve the array of SDEPoints
        SDEPoint[] points = (SDEPoint[]) al.get( 0 );
        // Retrieve the part offsets array.
        int[] partOffset = (int[]) al.get( 1 );
        // Retrieve the sub-part offsets array.
        int[] subPartOffset = (int[]) al.get( 2 );

        int numParts = shape.getNumParts();

        Surface[] gmMultiPolygonSurface = new Surface[numParts];
        boolean subParts = false;

        if ( partOffset.length < subPartOffset.length ) {
            subParts = true;
        }

        for ( int partNo = 0, partEnd = 0; partNo < partOffset.length; partNo++ ) {
            Position[] gmMultiPolygonExteriorRing = new Position[shape.getNumPoints( partNo + 1, 1 )];
            Position[][] gmMultiPolygonInteriorRings = null;
            int nSubParts = shape.getNumSubParts( partNo + 1 );

            if ( nSubParts > 1 ) {
                gmMultiPolygonInteriorRings = new Position[( nSubParts - 1 )][];
            }

            if ( ( partOffset.length - partNo ) == 1 ) {
                partEnd = points.length; // If this is the last part, scan through to
                // points.length
            } else {
                partEnd = subPartOffset[partOffset[partNo + 1]]; // Otherwise scan to the offset
                // of next part
            }

            int subPartNo = partOffset[partNo];
            int pointNo = subPartOffset[partOffset[partNo]];
            boolean exterior = true;
            int i = 0;
            int subPartIndex = -1;

            for ( ; ( pointNo < points.length ) && ( pointNo < partEnd ); pointNo++ ) {
                if ( subParts ) {
                    if ( ( subPartNo < subPartOffset.length ) && ( pointNo == subPartOffset[subPartNo] ) ) {
                        subPartNo++;
                        i = 0;
                    }
                }

                if ( exterior ) {
                    gmMultiPolygonExteriorRing[i] = GeometryFactory.createPosition( points[pointNo].getX(),
                                                                                    points[pointNo].getY() );

                    i++;

                    if ( ( subPartNo < subPartOffset.length ) && ( pointNo == ( subPartOffset[subPartNo] - 1 ) ) ) {
                        exterior = false;
                    }
                } else {
                    // When i=0 we are starting a new subPart. I compute
                    // and assign the size of the second dimension of gmMultiPolygonInteriorRings
                    if ( i == 0 ) {
                        subPartIndex++; // Used to address each interior ring

                        gmMultiPolygonInteriorRings[subPartIndex] = new Position[subPartOffset[subPartNo]
                                                                                 - subPartOffset[subPartNo - 1]];
                    }

                    gmMultiPolygonInteriorRings[subPartIndex][i] = GeometryFactory.createPosition(
                                                                                                   points[pointNo].getX(),
                                                                                                   points[pointNo].getY() );

                    i++;
                }
            } // End for

            gmMultiPolygonSurface[partNo] = GeometryFactory.createSurface( gmMultiPolygonExteriorRing,
                                                                           gmMultiPolygonInteriorRings,
                                                                           new SurfaceInterpolationImpl(), null );
        } // End for

        MultiSurface gmMultiSurface = GeometryFactory.createMultiSurface( gmMultiPolygonSurface );

        return gmMultiSurface;
    }

    /**
     * Set a row value with the appropriate setting method. If the datatype is unknown it will be
     * assigned by following rules:<br> - first, check the dataype of the given value and choose an
     * appropriate setter method<br> - or, if it isn't assignable this way, take the setString
     * method
     *
     * @param row
     * @param pos -
     *            the column position (0 - n)
     * @param value -
     *            the column value
     * @param sdetype -
     *            the datatype, expressed as SeColumnDefinition constant or -1, if unknown
     * @throws SeException
     */
    public static void setRowValue( SeRow row, int pos, Object value, int sdetype )
                            throws SeException {
        if ( -1 == sdetype )
            sdetype = findSDEType( value );
        switch ( sdetype ) {
        case SeColumnDefinition.TYPE_BLOB: {
            row.setBlob( pos, (java.io.ByteArrayInputStream) value );
            break;
        }
        case SeColumnDefinition.TYPE_CLOB: {
            row.setClob( pos, (java.io.ByteArrayInputStream) value );
            break;
        }
        case SeColumnDefinition.TYPE_DATE: {
            if ( null != value && value instanceof String )
                value = TimeTools.createCalendar( (String) value ).getTime();
            if ( value instanceof java.util.Date )
                row.setDate( pos, (java.util.Date) value );
            else if ( value instanceof java.util.Calendar )
                row.setTime( pos, (java.util.Calendar) value );
            break;
        }
        case SeColumnDefinition.TYPE_FLOAT64: {
            if ( null != value && value instanceof String )
                value = new Double( (String) value );
            row.setDouble( pos, (java.lang.Double) value );
            break;
        }
        case SeColumnDefinition.TYPE_FLOAT32: {
            if ( null != value && value instanceof String )
                value = new Float( (String) value );
            row.setFloat( pos, (java.lang.Float) value );
            break;
        }
        case SeColumnDefinition.TYPE_INT16: {
            if ( null != value && value instanceof String )
                value = new Short( (String) value );
            row.setShort( pos, (java.lang.Short) value );
            break;
        }
        case SeColumnDefinition.TYPE_INT32: {
            if ( null != value && value instanceof String )
                value = new Integer( (String) value );
            row.setInteger( pos, (java.lang.Integer) value );
            break;
        }
        case SeColumnDefinition.TYPE_INT64: {
            if ( null != value && value instanceof String )
                value = new Long( (String) value );
            row.setLong( pos, (java.lang.Long) value );
            break;
        }
        case SeColumnDefinition.TYPE_NCLOB: {
            row.setNClob( pos, (java.io.ByteArrayInputStream) value );
            break;
        }
        case SeColumnDefinition.TYPE_NSTRING: {
            row.setNString( pos, (String) value );
            break;
        }
        case SeColumnDefinition.TYPE_RASTER: {
            row.setRaster( pos, (SeRasterAttr) value );
            break;
        }
        case SeColumnDefinition.TYPE_SHAPE: {
            row.setShape( pos, (SeShape) value );
            break;
        }
        case SeColumnDefinition.TYPE_STRING: {
            row.setString( pos, (String) value );
            break;
        }
        case SeColumnDefinition.TYPE_UUID: {
            row.setUuid( pos, (String) value );
            break;
        }
        case SeColumnDefinition.TYPE_XML: {
            row.setXml( pos, (SeXmlDoc) value );
            break;
        }
        default: {
            row.setString( pos, value.toString() );
            break;
        }
        }
    }

    /**
     * Find an appropriate ArcSDE datataype for the given object value
     *
     * @param value
     * @return sdetype
     */
    public static int findSDEType( Object value ) {
        if ( null == value )
            return -1;
        else if ( value instanceof java.lang.Integer )
            return SeColumnDefinition.TYPE_INT16;
        else if ( value instanceof java.lang.Double )
            return SeColumnDefinition.TYPE_FLOAT64;
        else if ( value instanceof java.lang.Float )
            return SeColumnDefinition.TYPE_FLOAT32;
        else if ( value instanceof java.util.Calendar )
            return SeColumnDefinition.TYPE_DATE;
        else if ( value instanceof java.util.Date )
            return SeColumnDefinition.TYPE_DATE;
        else if ( value instanceof java.sql.Date )
            return SeColumnDefinition.TYPE_DATE;
        else if ( value instanceof SeRasterAttr )
            return SeColumnDefinition.TYPE_RASTER;
        else if ( value instanceof SeShape )
            return SeColumnDefinition.TYPE_SHAPE;
        else if ( value instanceof java.lang.String )
            return SeColumnDefinition.TYPE_STRING;
        else if ( value instanceof SeXmlDoc )
            return SeColumnDefinition.TYPE_XML;
        else
            return -1;
    }

    /**
     * Map SQL datatypes to appropriate ArcSDE datataypes.
     *
     * @param sqltype
     * @return sdetype
     */
    public static int mapSQL2SDE( int sqltype ) {
        switch ( sqltype ) {
        case java.sql.Types.ARRAY:
            return -1;
        case java.sql.Types.BIGINT:
            return SeColumnDefinition.TYPE_INT64;
        case java.sql.Types.BINARY:
            return SeColumnDefinition.TYPE_STRING;
        case java.sql.Types.BIT:
            return -1;
        case java.sql.Types.BLOB:
            return SeColumnDefinition.TYPE_BLOB;
        case java.sql.Types.BOOLEAN:
            return -1;
        case java.sql.Types.CHAR:
            return SeColumnDefinition.TYPE_STRING;
        case java.sql.Types.CLOB:
            return SeColumnDefinition.TYPE_CLOB;
        case java.sql.Types.DATALINK:
            return -1;
        case java.sql.Types.DATE:
            return SeColumnDefinition.TYPE_DATE;
        case java.sql.Types.DECIMAL:
            return SeColumnDefinition.TYPE_FLOAT64;
        case java.sql.Types.DISTINCT:
            return -1;
        case java.sql.Types.DOUBLE:
            return SeColumnDefinition.TYPE_FLOAT64;
        case java.sql.Types.FLOAT:
            return SeColumnDefinition.TYPE_FLOAT32;
        case java.sql.Types.INTEGER:
            return SeColumnDefinition.TYPE_INT32;
        case java.sql.Types.JAVA_OBJECT:
            return -1;
        case java.sql.Types.LONGVARBINARY:
            return -1;
        case java.sql.Types.LONGVARCHAR:
            return -1;
        case java.sql.Types.NULL:
            return -1;
        case java.sql.Types.NUMERIC:
            return SeColumnDefinition.TYPE_FLOAT64;
        case java.sql.Types.OTHER:
            return -1;
        case java.sql.Types.REAL:
            return SeColumnDefinition.TYPE_FLOAT32;
        case java.sql.Types.REF:
            return -1;
        case java.sql.Types.SMALLINT:
            return SeColumnDefinition.TYPE_INT16;
        case java.sql.Types.STRUCT:
            return SeColumnDefinition.TYPE_SHAPE;
        case java.sql.Types.TIME:
            return SeColumnDefinition.TYPE_DATE;
        case java.sql.Types.TIMESTAMP:
            return SeColumnDefinition.TYPE_DATE;
        case java.sql.Types.TINYINT:
            return SeColumnDefinition.TYPE_INT16;
        case java.sql.Types.VARBINARY:
            return SeColumnDefinition.TYPE_STRING;
        case java.sql.Types.VARCHAR:
            return SeColumnDefinition.TYPE_STRING;
        default:
            return -1;
        }
    }

    /**
     * Map ArcSDE datatypes to appropriate SQL datataypes.
     *
     * @param sdetype
     * @return sqltype
     */
    public static int mapSDE2SQL( int sdetype ) {
        switch ( sdetype ) {
        case SeColumnDefinition.TYPE_BLOB:
            return java.sql.Types.BLOB;
        case SeColumnDefinition.TYPE_CLOB:
            return java.sql.Types.CLOB;
        case SeColumnDefinition.TYPE_DATE:
            return java.sql.Types.DATE;
        case SeColumnDefinition.TYPE_FLOAT64:
            return java.sql.Types.DOUBLE;
        case SeColumnDefinition.TYPE_FLOAT32:
            return java.sql.Types.FLOAT;
        case SeColumnDefinition.TYPE_INT64: // Since ArcSDE v9.0
            return java.sql.Types.INTEGER;
        case SeColumnDefinition.TYPE_INT32:
            return java.sql.Types.INTEGER;
        case SeColumnDefinition.TYPE_NCLOB:
            return java.sql.Types.CLOB;
        case SeColumnDefinition.TYPE_NSTRING:
            return java.sql.Types.VARCHAR;
        case SeColumnDefinition.TYPE_RASTER:
            return -1;
        case SeColumnDefinition.TYPE_SHAPE:
            return java.sql.Types.STRUCT;
        case SeColumnDefinition.TYPE_INT16:
            return java.sql.Types.SMALLINT;
        case SeColumnDefinition.TYPE_STRING:
            return java.sql.Types.VARCHAR;
        case SeColumnDefinition.TYPE_UUID:
            return java.sql.Types.VARCHAR;
        case SeColumnDefinition.TYPE_XML:
            return java.sql.Types.VARCHAR;
        default:
            return -1;
        }
    }
}
