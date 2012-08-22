//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.geographie.uni-bonn.de/deegree/
 and
 lat/lon GmbH
 http://lat-lon.de/

 Additional copyright notes:

 This class uses some code fragments taken from J3D.org open source project
 which has been published under LGPL at www.jd3.org.

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

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.ogcwebservices.wpvs.j3d;

import static org.deegree.framework.log.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Vector3f;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.util.ImageUtils;
import org.deegree.ogcwebservices.wpvs.configuration.RenderingConfiguration;
import org.j3d.geom.GeometryData;
import org.j3d.geom.UnsupportedTypeException;
import org.j3d.geom.terrain.ElevationGridGenerator;

/**
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 *         $Revision$, $Date$
 */
public class TexturedHeightMapTerrain extends HeightMapTerrain {

    private static final ILogger LOG = getLogger( TexturedHeightMapTerrain.class );

    /**
     * @param width
     *            width of the terrains bbox
     * @param depth
     *            depth/height of the terrains bbox
     * @param heights
     *            terrain data; heights
     * @param translation
     *            of the lowerleft point to the Java3D model.
     * @param geometryType
     *            for a description see
     *            {@link HeightMapTerrain#HeightMapTerrain(float, float, float[][], Vector3f, int, boolean)}
     * @param centerTerrain
     */
    public TexturedHeightMapTerrain( float width, float depth, float[][] heights, Vector3f translation,
                                     int geometryType, boolean centerTerrain ) {
        super( width, depth, heights, translation, geometryType, centerTerrain );
    }

    /**
     * No translation of the lowerleft point.
     * 
     * @param width
     *            width of the terrains bbox
     * @param depth
     *            depth/height of the terrains bbox
     * @param heights
     *            terrain data; heights
     * @param geometryType
     *            for a description see
     *            {@link HeightMapTerrain#HeightMapTerrain(float, float, float[][], Vector3f, int, boolean)}
     * @param centerTerrain
     * @param texture
     */
    public TexturedHeightMapTerrain( float width, float depth, float[][] heights, int geometryType,
                                     boolean centerTerrain, BufferedImage texture ) {

        super( width, depth, heights, new Vector3f( 0, 0, 0 ), geometryType, centerTerrain );
        setTexture( texture );
    }

    /**
     * No translation of the lowerleft point.
     * 
     * @param width
     *            width of the terrains bbox
     * @param depth
     *            depth/height of the terrains bbox
     * @param heights
     *            terrain data; heights
     * @param geometryType
     *            for a description see
     *            {@link HeightMapTerrain#HeightMapTerrain(float, float, float[][], Vector3f, int, boolean)}
     * @param centerTerrain
     * @param textureFile
     */
    public TexturedHeightMapTerrain( float width, float depth, float[][] heights, int geometryType,
                                     boolean centerTerrain, URL textureFile ) {

        super( width, depth, heights, new Vector3f( 0, 0, 0 ), geometryType, centerTerrain );
        try {
            setTexture( ImageUtils.loadImage( textureFile ) );
        } catch ( Exception e ) {
            LOG.logError( e );
        }
    }

    /**
     * Must be called before rendering the terrain!!i<br>
     */
    @Override
    public void createTerrain() {
        // System.out.println( "terrain#material: " +getAppearance().getMaterial() );
        ElevationGridGenerator gridGenerator = new ElevationGridGenerator( getTerrainWidth(), getTerrainDepth(),
                                                                           getTerrainHeights()[0].length,
                                                                           getTerrainHeights().length,
                                                                           getTranslation(), isTerrainCentered() );

        // set the terrain into the elevation grid handler
        gridGenerator.setTerrainDetail( getTerrainHeights(), 0 );

        GeometryData data = new GeometryData();
        data.geometryType = getGeometryType();
        data.geometryComponents = GeometryData.NORMAL_DATA;

        if ( getTexture() != null ) {
            data.geometryComponents |= GeometryData.TEXTURE_2D_DATA;
        }
        try {
            gridGenerator.generate( data );
        } catch ( UnsupportedTypeException ute ) {
            LOG.logError( "Geometry type is not supported.", ute );
        }
        int format = GeometryArray.COORDINATES | GeometryArray.NORMALS;
        if ( getTexture() != null ) {
            format |= GeometryArray.TEXTURE_COORDINATE_2;
        }
        if ( !RenderingConfiguration.getInstance().isTerrainShadingEnabled() ) {
            format |= GeometryArray.COLOR_3;
        }
        GeometryArray geom = createGeometryArray( data, format );

        geom.setCoordinates( 0, data.coordinates );
        geom.setNormals( 0, data.normals );
        if ( !RenderingConfiguration.getInstance().isTerrainShadingEnabled() ) {
            float[] colors = new float[geom.getVertexCount() * 3];
            Arrays.fill( colors, 1 );
            geom.setColors( 0, colors );
        }

        if ( getTexture() != null ) {
            geom.setTextureCoordinates( 0, 0, data.textureCoordinates );
        }
        setGeometry( geom );
    }

    // private void addTexture() {
    // Appearance appearance = getAppearance();
    // appearance.setMaterial( targetMaterial );
    //
    // PolygonAttributes targetPolyAttr = new PolygonAttributes();
    // int capabilities = PolygonAttributes.ALLOW_MODE_WRITE |
    // PolygonAttributes.ALLOW_CULL_FACE_WRITE |
    // PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE |
    // PolygonAttributes.POLYGON_FILL;
    // targetPolyAttr.setCapability( capabilities );
    // appearance.setPolygonAttributes( targetPolyAttr );
    // if ( getTexture() != null ) {
    // try {
    // Texture texture = new TextureLoader( getTexture() ).getTexture();
    // texture.setEnable( true );
    // texture.setCapability( Texture.ALLOW_ENABLE_WRITE );
    // appearance.setTexture( texture );
    // } catch ( Exception e ) {
    // e.printStackTrace();
    // }
    // setCapability( Shape3D.ALLOW_GEOMETRY_WRITE );
    // setAppearance( appearance );
    // }
    // }

}
