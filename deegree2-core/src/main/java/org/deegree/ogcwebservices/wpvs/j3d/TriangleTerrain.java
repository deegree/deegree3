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

package org.deegree.ogcwebservices.wpvs.j3d;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcwebservices.wpvs.utils.VisADWrapper;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 * The <code>TriangleTerrain</code> class respresents the Java3D shape of a set of measurepoints. Before this Terrrain
 * can be drawn the createTerrain method must be invoked, it will create triangles of the given measurepoints and will
 * add the optional texture to the apearance of the Shap3D.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class TriangleTerrain extends TerrainModel {

    private static ILogger LOG = LoggerFactory.getLogger( TriangleTerrain.class );

    private Envelope boundingBox;

    private double terrainWidth;

    private double terrainHeight;

    private List<Point3d> measurePoints = null;

    private double minimalHeightlevel;


    private double scale;

    /**
     * @param measurePoints
     *            indicating height values inside this terrain. They will be triangulated in the createTerrain method.
     * @param env the bbox of this triangle terrain.
     * @param minimalHeightlevel
     *            which will be used if the measurepoints have no height set.
     * @param scale
     *            to multiply onto the z-value of the measurepoints
     */
    public TriangleTerrain( List<Point3d> measurePoints, Envelope env, double minimalHeightlevel, double scale ) {
        super();
        this.measurePoints = measurePoints;
        this.minimalHeightlevel = minimalHeightlevel;
        this.scale = scale;

        boundingBox = env;

        this.terrainWidth = boundingBox.getWidth();
        this.terrainHeight = boundingBox.getHeight();
    }

    /**
     * @return the boundingBox of this TriangleTerrain
     */
    public Envelope getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void createTerrain() {
         List<float[][]> triangles = new ArrayList<float[][]>();
        if ( measurePoints == null || measurePoints.size() == 0 ) { // just a square, asssuming a simple plane,
            triangles = createFlatTerrain(  );
        } else {
            LOG.logDebug( "Trying to create triangles with the visad library" );
            long time = System.currentTimeMillis();
            VisADWrapper vw = new VisADWrapper( measurePoints, scale );
            triangles = vw.getTriangleCollectionAsList();
            if( triangles != null ){
                LOG.logDebug( new StringBuilder("Creation of ").append( triangles.size()).append( " triangles with the visad library was successfull. (took: ").append( (System.currentTimeMillis() - time) / 1000d).append(" seconds).").toString() );
            } else{
                LOG.logDebug( new StringBuilder("Creation of triangles with the visad library was NOT successfull. Creating a flat terrain.").toString() );
                triangles = createFlatTerrain( );
            }

        }

        double widthInv = 1d / terrainWidth;
        double heightInv = 1d / terrainHeight;
        Position originalLowerLeft = boundingBox.getMin();

        GeometryInfo geometryInfo = new GeometryInfo( GeometryInfo.TRIANGLE_ARRAY );

        BufferedImage texture = getTexture();
        if ( texture != null ){
            geometryInfo.setTextureCoordinateParams( 1, 2 );
        }

        Point3f[] coordinates = new Point3f[triangles.size() * 3];
        TexCoord2f[] texCoords = new TexCoord2f[triangles.size() * 3];

        int coordNr = 0;
        for ( float[][] triangleCoords : triangles ) {
            for ( int k = 0; k < 3; k++ ) {
                // System.out.println( Thread.currentThread() + "-> coordNR: " + coordNr );
                Point3f modelCoordinate = new Point3f( triangleCoords[k][0], triangleCoords[k][1], triangleCoords[k][2] );
                coordinates[coordNr] = modelCoordinate;

                if ( texture != null ) {
                    double texCoordX = ( modelCoordinate.x - originalLowerLeft.getX() ) * widthInv;
                    double texCoordY = ( modelCoordinate.y - originalLowerLeft.getY() ) * heightInv;
                    texCoordX = ( texCoordX > 1 ) ? 1 : ( ( texCoordX < 0 ) ? 0 : texCoordX );
                    texCoordY = ( texCoordY > 1 ) ? 1 : ( ( texCoordY < 0 ) ? 0 : texCoordY );
                    texCoords[coordNr] = new TexCoord2f( (float)texCoordX ,(float) texCoordY );
                }
                coordNr++;
            }

        }
        geometryInfo.setCoordinates( coordinates );
        if ( texture != null ) {
            geometryInfo.setTextureCoordinates( 0, texCoords );
        }
        geometryInfo.recomputeIndices();
        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals( geometryInfo );

        setGeometry( geometryInfo.getGeometryArray() );
    }

    private List<float[][]> createFlatTerrain( ){
        LOG.logDebug( "Creating a flat square at minimalTerrain height, which will represent the terrain." );
        List<float[][]> triangles = new ArrayList<float[][]>();
        Position min = boundingBox.getMin();
        Position max = boundingBox.getMax();
        float[][] triangle = new float[3][3];
        triangle[0][0] = (float) min.getX();
        triangle[0][1] = (float) min.getY();
        triangle[0][2] = (float) minimalHeightlevel;

        triangle[1][0] = (float) max.getX();
        triangle[1][1] = (float) min.getY();
        triangle[1][2] = (float) minimalHeightlevel;

        triangle[2][0] = (float) min.getX();
        triangle[2][1] = (float) max.getY();
        triangle[2][2] = (float) minimalHeightlevel;

        triangles.add( triangle );

        triangle = new float[3][3];
        triangle[0][0] = (float) max.getX();
        triangle[0][1] = (float) min.getY();
        triangle[0][2] = (float) minimalHeightlevel;

        triangle[1][0] = (float) max.getX();
        triangle[1][1] = (float) max.getY();
        triangle[1][2] = (float) minimalHeightlevel;

        triangle[2][0] = (float) min.getX();
        triangle[2][1] = (float) max.getY();
        triangle[2][2] = (float) minimalHeightlevel;
        triangles.add( triangle );
        return triangles;
    }

}
