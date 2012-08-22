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
package org.deegree.ogcwebservices.wpvs.utils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

import visad.Delaunay;
import visad.FlatField;
import visad.FunctionType;
import visad.Irregular2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

/**
 * A wrapper for VisAD objects. This class takes care of collecting points to build a TIN, of TIN creation itself and
 * its output as a geometry collection.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 *
 */
public class VisADWrapper {

    private static final ILogger LOG = LoggerFactory.getLogger( VisADWrapper.class );

    /**
     * A list for hold points representing te DEM/TIN.
     */
    private List<Point3d> pointsList;

    private double scale;

    /**
     * Initializes the object by creating a common domain field from the geometrical information (the envelope, the
     * width and the height) supplied. The envelope cannot the null, nor can the dimensions by < 1.
     *
     * @param ptsList
     *            a list of Points
     * @param scale
     *            to multiply to the z-value
     */
    public VisADWrapper( List<Point3d> ptsList, double scale ) {
        this.pointsList = ptsList;
        this.scale = scale;
    }

    /**
     * Add <code>Point</code>s to the internal list. Lists without any elements (or null lists) are ignored.
     *
     * @param points
     *            to be added to the list
     */
    public final void addPoints( List<Point3d> points ) {

        if ( points == null || points.size() == 0 ) {
            return;
        }

        this.pointsList.addAll( points );
    }

    /**
     * Generates a list of tringles containing the triangles representing the TIN. Triangles are represented float[3][3]
     *
     * @return a collection of <code>float[3][3]</code>, each of which representing a TIN triangle
     *
     */
    public final List<float[][]> getTriangleCollectionAsList() {

        List<float[][]> list = null;
        try {
            FlatField tinField = triangulatePoints();
            if ( tinField == null )
                return null;
            list = toGeoCollectionList( tinField );
        } catch ( NoClassDefFoundError ncdfe ) {
            LOG.logError( "WPVS: It seems that the visad library could not be found: " + ncdfe.getLocalizedMessage(),
                          ncdfe );
        } catch ( VisADException ve ) {
            LOG.logError( ve.getLocalizedMessage(), ve );
        }
        return list;
    }

    /**
     * Triangulate <code>GM_Point</code>s contained in <code>gmPointsList</code> using the Clarkson algorithm. This
     * method returns a <code>FlatField</code> containing all points triangulated and with their elevation values.<br/>
     *
     * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
     *
     * @return a <code>FlatField</code> containing a TIN (with an <code>Irregular2DSet</code> as its domain set and
     *         the elevation values)
     *
     */
    private FlatField triangulatePoints()
                                         throws NoClassDefFoundError {

        if ( this.pointsList == null || this.pointsList.size() < 3 ) {
            throw new IllegalArgumentException( "Points list cannot be null and must contain at least 3 GM_Points." );
        }

        // removing double points
        ArrayList<Point3d> remove = new ArrayList<Point3d>();
        for ( int i = 0; i < pointsList.size(); ++i ) {
            Point3d p = pointsList.get( i );

            if ( !remove.contains( p ) ) {
                for ( int j = i + 1; j < pointsList.size(); ++j ) {
                    Point3d tmpP = pointsList.get( j );
                    if ( ( Math.abs( tmpP.x - p.x ) < 0.001 ) && ( Math.abs( tmpP.y - p.y ) < 0.001 ) ) {
                        remove.add( tmpP );
                    }
                }
            }
        }
        for ( Point3d p : remove ) {
            pointsList.remove( p );
        }
        float[][] triPoints = new float[3][this.pointsList.size()];
        int cnt = 0;

        for ( Point3d p : this.pointsList ) {
            triPoints[0][cnt] = (float) p.x;
            triPoints[1][cnt] = (float) p.y;
            triPoints[2][cnt++] = (float) ( p.z * scale );
        }

        try {
            FunctionType functionType = new FunctionType( new RealTupleType( RealType.XAxis, RealType.YAxis ),
                                                          RealType.ZAxis );
            float[][] ptsXY = new float[][] { triPoints[0], triPoints[1] };

            // ptsXY = Delaunay.perturb(ptsXY,0.1f, false);
            // ptsXY = Delaunay.perturb( ptsXY, 5.5f, false );
            Delaunay delan = Delaunay.factory( ptsXY, false );
            // Delaunay delan = new DelaunayClarkson( ptsXY );
            // Delaunay delan = new DelaunayWatson( ptsXY );
            // DelaunayFast delan = new DelaunayFast( ptsXY );
            // delan.setNonConvex();
            try {
                // delan.improve( ptsXY, 5 );
            } catch ( Exception e ) {
                // just do noting
            }

            // Delaunay delan = new DelaunayClarkson( ptsXY );
            // Delaunay delan = new DelaunayFast( ptsXY );
            Irregular2DSet pointsSet = new Irregular2DSet( functionType.getDomain(), ptsXY, null, null, null, delan );

            FlatField ff = new FlatField( functionType, pointsSet );

            ff.setSamples( new float[][] { triPoints[2] }, true );

            return ff;

        } catch ( VisADException e ) {
            LOG.logError(e.getMessage(), e );
            return null;
        } catch ( RemoteException re ) {
            LOG.logError(re.getMessage(), re );
            return null;
        } catch ( IndexOutOfBoundsException ioobe ) {
            LOG.logError(ioobe.getMessage(), ioobe );
            return null;
        }
    }

    /**
     * Generated a list of triangles from the FlatField passed in as tinField
     *
     * @param tinField
     *            the FlatField containing triangles
     * @return a collection of <code>float[3][3]</code>, each of which representing a TIN triangle
     * @throws VisADException
     *             in the unlikely event that a VisAD exception is thrown
     * @throws NoClassDefFoundError
     *             in the unlikely event that the VisAD library could not be found.
     */
    private final List<float[][]> toGeoCollectionList( FlatField tinField )
                                                                           throws NoClassDefFoundError, VisADException {
        if ( tinField == null ) {
            throw new RuntimeException( "FlatField cannot be null." );
        }

        List<float[][]> geoCollec = new ArrayList<float[][]>( 5000 );

        Irregular2DSet domainSet = (Irregular2DSet) tinField.getDomainSet();
        float[][] xyPositions = domainSet.getSamples();
        float[][] zValues = tinField.getFloats();
        int[][] indices = domainSet.Delan.Tri;

        // loop over triangles...
        for ( int i = 0; i < indices.length; i++ ) {

            // indices[i].length == coords.length == 3
            // this is float[3][3] -> 3 points per triabngle, each point with 3 coords
            float[][] myCoords = new float[3][3];

            // ...then over points
            for ( int j = 0; j < indices[i].length; j++ ) {

                int index = indices[i][j];
                myCoords[j] = new float[3];
                myCoords[j][0] = xyPositions[0][index];
                myCoords[j][1] = xyPositions[1][index];
                myCoords[j][2] = zValues[0][index];
            }

            geoCollec.add( myCoords );
        }

        tinField = null;

        return geoCollec;
    }

    /**
     * Clear all points and invalidate list.
     *
     */
    public void clear() {
        this.pointsList.clear();
        this.pointsList = null;

    }

}
