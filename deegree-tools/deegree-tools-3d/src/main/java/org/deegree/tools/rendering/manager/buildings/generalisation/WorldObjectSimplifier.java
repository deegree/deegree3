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
package org.deegree.tools.rendering.manager.buildings.generalisation;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModelPart;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.PrototypeReference;
import org.deegree.rendering.r3d.opengl.tesselation.Tesselator;

import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

/**
 * This class simplifies a 3D-Object. Two different simplifications will be performed. The first one create a simplified
 * version of the original geometry by calculating and simplifying the convex hull of its two-dimensional projection.
 * The second one calculates a transformation matrix to move, rotated und stretch a box of size 1,1,1 located at 0,0,0
 * to the 3D envelope of an object.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 */
public class WorldObjectSimplifier {

    private Tesselator tesselator;

    /**
     * Instantiate the simplifier..
     */
    public WorldObjectSimplifier() {
        tesselator = new Tesselator( false );
    }

    /**
     * For this five steps will be performed.
     * <ul>
     * <li>1. map all points to a planar horizontal surface
     * <li>2. calculate convex hull of the points
     * <li>3. simplify convex hull (using Douglas-Peucker algorithm)
     * <li>4. extrude polygon represented by convex hull to height of the original geometry (first simplification
     * result)
     * <li>5. calculate 3D bounding box (currently not supported)
     * </ul>
     *
     * @param wro
     *            to get the qualitylevel from.
     * @param levelOfInterest
     *            the simplifications should be created from.
     * @param targetQualityLevel
     *            to which the generalisation should be saved.
     *
     */
    public void createSimplified3DObject( WorldRenderableObject wro, int levelOfInterest, int targetQualityLevel ) {
        if ( wro.getNumberOfQualityLevels() == 0 || 0 > levelOfInterest
             || levelOfInterest > wro.getNumberOfQualityLevels() ) {
            throw new IllegalArgumentException( "No objects in WorldRenderableObject to create a simplified form from." );
        }
        RenderableQualityModel rqm = wro.getQualityLevel( levelOfInterest );
        if ( rqm == null || rqm.getPrototypeReference() != null ) {
            throw new IllegalArgumentException(
                                                "The given QualityModel: "
                                                                        + levelOfInterest
                                                                        + " in the WorldRenderableObject is not applicable for the simplification algorithm." );
        }

        float[] coordinates = retrieveCoords( rqm );
        RegressionForm form = new RegressionForm( coordinates );

        Coordinate[] convexHull = createConvexHull( rqm );

        wro.setQualityLevel( targetQualityLevel, createRenderableGeometry( convexHull, form.getMinZ(), form.getMaxZ() ) );

        // wro.setQualityLevel( 0, createPrototypeReference( form ) );
    }

    /**
     * @param rqm
     * @param bbox
     * @return
     */
    private RenderableQualityModel createPrototypeReference( RegressionForm form ) {
        // get 2D projection of 3D object required for calculating convex hull
        // Coordinate[] coordinates = projectToPlane( rqm );

        float angle = form.getRotationAngle();
        float width = form.getWidth();
        float depth = form.getDepth();
        float height = form.getHeight();

        return new RenderableQualityModel( new PrototypeReference( "box", angle, form.getCentroid(), width, height,
                                                                   depth ) );

    }

    /**
     * @param coordinates
     * @param bbox
     */
    private RenderableQualityModel createRenderableGeometry( Coordinate[] coordinates, float minz, float maxz ) {
        RenderableQualityModel result = new RenderableQualityModel();
        for ( int i = 0; i < coordinates.length - 1; ++i ) {
            result.addQualityModelPart( createVerticalGeometry( coordinates[i], coordinates[i + 1], minz, maxz ) );
        }
        // the bottom is of no use
        // result.addQualityModelPart( createHorizontalGeometry( coordinates, minz ) );
        // and the top
        result.addQualityModelPart( createHorizontalGeometry( coordinates, maxz ) );

        return result;
    }

    /**
     * @param coordinates
     * @param minz
     * @return
     */
    private RenderableQualityModelPart createHorizontalGeometry( Coordinate[] coordinates, float zValue ) {
        // float[] normal = new float[] { 0, 0, -1 };
        float[] coords = new float[coordinates.length * 3];
        // float[] normals = new float[coordinates.length * 3];
        for ( int cIndex = 0, i = 0; i < coordinates.length; ++i ) {
            // x
            // normals[cIndex] = normal[0];
            coords[cIndex++] = (float) coordinates[i].x;

            // y
            // normals[cIndex] = normal[1];
            coords[cIndex++] = (float) coordinates[i].y;

            // z
            // normals[cIndex] = normal[2];
            coords[cIndex++] = zValue;

        }
        SimpleAccessGeometry sag = new SimpleAccessGeometry( coords );
        // RenderableGeometry rg = new RenderableGeometry( coords, GL.GL_POLYGON, normals, false );
        return tesselator.tesselateGeometry( sag );
    }

    /**
     * @param first
     * @param coordinate2
     * @return
     */
    private RenderableGeometry createVerticalGeometry( Coordinate first, Coordinate second, float minz, float maxz ) {
        float[] geom = new float[] { (float) first.x, (float) first.y, minz, (float) first.x, (float) first.y, maxz,
                                    (float) second.x, (float) second.y, maxz, (float) second.x, (float) second.y, minz };
        float[] normal = new float[3];
        float[] normals = new float[geom.length];
        Vectors3f.normalizedNormal( geom, 0, normal );
        normals[0] = normals[3] = normals[6] = normals[9] = normal[0];
        normals[1] = normals[4] = normals[7] = normals[10] = normal[1];
        normals[2] = normals[5] = normals[8] = normals[11] = normal[2];
        return new RenderableGeometry( geom, GL.GL_QUADS, normals, false );

    }

    /**
     * @param distanceTolerance
     * @return
     */
    private Coordinate[] createConvexHull( RenderableQualityModel rqm ) {
        // get 2D projection of 3D object required for calculating convex hull
        Coordinate[] coordinates = projectToPlane( rqm );

        // calculate convex hull using JTS
        ConvexHull ch = new ConvexHull( coordinates, new org.locationtech.jts.geom.GeometryFactory() );
        Geometry dp = DouglasPeuckerSimplifier.simplify( ch.getConvexHull(), 0.1 );
        if ( dp instanceof Polygon ) {
            return ( (Polygon) dp ).getExteriorRing().getCoordinates();
        } else if ( dp instanceof GeometryCollection ) {
            GeometryCollection collection = (GeometryCollection) dp;
            List<Coordinate> allCoordinates = new LinkedList<Coordinate>();
            for ( int i = 0; i < collection.getNumGeometries(); ++i ) {
                Geometry geom = collection.getGeometryN( i );
                if ( geom != null ) {
                    if ( geom instanceof Polygon ) {
                        Coordinate[] pC = ( (Polygon) geom ).getExteriorRing().getCoordinates();
                        allCoordinates.addAll( Arrays.asList( pC ) );
                    }
                }
            }
            return allCoordinates.toArray( new Coordinate[0] );
        }
        return dp.getCoordinates();
    }

    /**
     *
     * @param rqm
     * @return List of points of a 3D object projected onto x/y-plane
     */
    private Coordinate[] projectToPlane( RenderableQualityModel rqm ) {

        int length = 0;
        for ( RenderableQualityModelPart qmp : rqm.getQualityModelParts() ) {
            length += ( (RenderableGeometry) qmp ).getVertexCount();
        }
        Coordinate[] coords = new Coordinate[length];
        int i = 0;
        for ( RenderableQualityModelPart qmp : rqm.getQualityModelParts() ) {
            RenderableGeometry rg = (RenderableGeometry) qmp;
            FloatBuffer coordBuffer = rg.getReadOnlyCoordBuffer();
            coordBuffer.rewind();
            int cap = coordBuffer.capacity();
            while ( coordBuffer.position() < cap ) {
                coords[i++] = new Coordinate( coordBuffer.get(), coordBuffer.get() );
                // ignore z.
                coordBuffer.get();
            }
        }
        return coords;
    }

    /**
     *
     * @param rqm
     * @return List of points of a 3D object projected onto x/y-plane
     */
    private float[] retrieveCoords( RenderableQualityModel rqm ) {
        int length = 0;
        for ( RenderableQualityModelPart qmp : rqm.getQualityModelParts() ) {
            length += ( (RenderableGeometry) qmp ).getVertexCount();
        }
        float[] coords = new float[length * 3];
        int i = 0;
        for ( RenderableQualityModelPart qmp : rqm.getQualityModelParts() ) {
            RenderableGeometry rg = (RenderableGeometry) qmp;
            FloatBuffer coordBuffer = rg.getReadOnlyCoordBuffer();
            coordBuffer.rewind();
            int cap = coordBuffer.capacity();
            while ( coordBuffer.position() < cap ) {
                coords[i++] = coordBuffer.get();
                coords[i++] = coordBuffer.get();
                coords[i++] = coordBuffer.get();
            }
        }
        return coords;
    }

    /**
     *
     * The <code>RegressionForm</code> class calculates a linear regression approximation over a couple of points in
     * the xy plane. See http://de.wikipedia.org/wiki/Regressionsanalyse for more information.
     *
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     *
     */
    private class RegressionForm {
        private double a;

        private double b;

        private float width;

        private float depth;

        private float height;

        private float maxZ;

        private float minZ;

        private float[] centroid;

        /**
         * @param coordinates
         *            to do a regresssion analysis for
         */
        public RegressionForm( float[] coordinates ) {
            double middleX = 0;
            double middleY = 0;
            int length = coordinates.length / 3;
            for ( int i = 0; i + 2 < coordinates.length; i += 3 ) {
                middleX += coordinates[i];
                middleY += coordinates[i + 1];
            }
            middleX *= 1d / length;
            middleY *= 1d / length;

            double upperB = 0;
            double lowerB = 0;
            for ( int i = 0; i + 2 < length; i += 3 ) {
                double x = coordinates[i] - middleX;
                upperB += x * ( coordinates[i + 1] - middleY );
                lowerB += x * x;
            }
            this.b = upperB / lowerB;
            this.a = middleY - b * middleX;
            calculateWidthAndDepthAndHeight( coordinates );
        }

        /**
         * Calculate the width and the depth of the given points by applying the regression form
         *
         * @param coordinates
         *            to be used.
         */
        public void calculateWidthAndDepthAndHeight( float[] coordinates ) {

            centroid = new float[3];

            float minX = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float minY = Float.MAX_VALUE;
            float maxY = Float.MIN_VALUE;

            minZ = Float.MAX_VALUE;
            maxZ = Float.MIN_VALUE;

            for ( int i = 0; i + 2 < coordinates.length; i += 3 ) {
                double nY = a + ( b * coordinates[i] );
                double nX = ( nY - a ) / b;
                if ( nY < minY ) {
                    minY = (float) nY;
                }
                if ( nY > maxY ) {
                    maxY = (float) nY;
                }
                if ( nX < minX ) {
                    minX = (float) nX;
                }
                if ( nX > maxX ) {
                    maxX = (float) nX;
                }

                if ( coordinates[i + 2] < minZ ) {
                    minZ = coordinates[i + 2];
                }
                if ( coordinates[i + 2] > maxZ ) {
                    maxZ = coordinates[i + 2];
                }
            }
            this.width = maxX - minX;
            this.depth = maxY - minY;
            this.height = maxZ - minZ;
            centroid[0] = (float) ( minX + ( width * 0.5 ) );
            centroid[1] = (float) ( minY + ( depth * 0.5 ) );
            centroid[2] = minZ;
        }

        /**
         * @return the width
         */
        public final float getWidth() {
            return width;
        }

        /**
         * @return the depth
         */
        public final float getDepth() {
            return depth;
        }

        /**
         * @return the height
         */
        public final float getHeight() {
            return height;
        }

        /**
         * @return the centroid
         */
        public final float[] getCentroid() {
            return centroid;
        }

        /**
         * @return the minimal z value of all visited points
         */
        public float getMinZ() {
            return minZ;
        }

        /**
         * @return the maximal z value of all visited points
         */
        public float getMaxZ() {
            return maxZ;
        }

        /**
         * Calculate a rotation angle (in degrees) by using the slope of the linear regression
         *
         * @return the rotation angle (from the 'x'-axis) in degrees.
         */
        public float getRotationAngle() {
            double hypo = Math.sqrt( ( b * b ) + 1 );
            return (float) Math.toDegrees( Math.asin( b / hypo ) );
        }

    }
}
