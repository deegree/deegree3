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

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.Texture;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.wpvs.configuration.RenderingConfiguration;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.image.TextureLoader;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TexturedSurface extends ColoredSurface {
    private static ILogger LOG = LoggerFactory.getLogger( TexturedSurface.class );

    private Texture texture = null;

    private BufferedImage textureImg = null;

    /**
     * creates a TexturedSurface from a geometry, Material and a texture image. Since a texture image be somehow
     * transparent it is useful to be able to define a surfaces color.
     *
     * @param objectID
     *            an Id for this Surface, for example a db primary key
     * @param parentID
     *            an Id for the parent of this Surface, for example if this is a wall the parent is the building.
     * @param geometry
     *            the ogc:geometry surface which holds the point references of a polygon, not to be confused with a j3d
     *            Object which this class represents.
     * @param material
     * @param transparency
     * @param textureImg
     * @param textureCoords
     */
    public TexturedSurface( String objectID, String parentID, Geometry geometry, Material material, float transparency,
                            BufferedImage textureImg, List<TexCoord2f> textureCoords ) {
        super( objectID, parentID, geometry, material, transparency );

        this.textureImg = textureImg;
        createTexture( textureImg );
        setAppearance( createAppearance() );
        addGeometry( geometry, textureCoords );
    }

    private void createTexture( BufferedImage textureImg ) {
        try {
            texture = new TextureLoader( textureImg, TextureLoader.GENERATE_MIPMAP ).getTexture();
            texture.setEnable( true );
            texture.setMipMapMode( Texture.MULTI_LEVEL_MIPMAP );
            texture.setCapability( Texture.ALLOW_ENABLE_WRITE );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * @return the texture of this surface.
     */
    public BufferedImage getTexture() {
        return textureImg;
    }

    /**
     * this method must be called before adding the surface to a Group
     */
    @Override
    public void compile() {
        //GeometryInfo geomInfo = getGeometryInfo( surface );
        setAppearanceOverrideEnable( true );
    }

    /**
     * Adds the given surface and texcoords to this shape.
     *
     * @param surface
     *            to be added
     * @param texCoords
     *            to be added.
     */
    public void addGeometry( Geometry surface, List<TexCoord2f> texCoords ) {
        List<Point3d> coordinates = new ArrayList<Point3d>();
        List<Integer> contourCount = new ArrayList<Integer>( 200 );
        if ( surface != null ) {
            if ( surface instanceof MultiSurface ) {
                for ( Surface multiSurface : ( (MultiSurface) surface ).getAllSurfaces() ) {
                    extractSurface( multiSurface, coordinates, contourCount );
                }
            } else if ( surface instanceof Surface ) {
                extractSurface( (Surface) surface, coordinates, contourCount );
            } else {
                throw new UnsupportedOperationException( "Don't know how to create a textured surface from given geometry" );
            }
        }
        GeometryInfo geometryInfo = new GeometryInfo( GeometryInfo.POLYGON_ARRAY );

        geometryInfo.setCoordinates( coordinates.toArray( new Point3d[0] ) );
        int[] sc = new int[contourCount.size()];
        int stripCountNumber = 0;
        for ( int i = 0; i < contourCount.size(); ++i ) {
            sc[i] = contourCount.get( i ).intValue();
            stripCountNumber += sc[i];
        }
        LOG.logDebug( "Geometry has number of vertices: " + coordinates.size()
                      + " the number of Strip counts adds up to: " + stripCountNumber );
        geometryInfo.setStripCounts( sc );
        // we don't actually need the contours, just an array which has the right length.
        geometryInfo.setContourCounts( new int[] { sc.length } );

        if ( texCoords != null ) {
            geometryInfo.setTextureCoordinateParams( 1, 2 );
            geometryInfo.setTextureCoordinates( 0, texCoords.toArray( new TexCoord2f[0] ) );
        }
        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals( geometryInfo );

        geometryInfo.convertToIndexedTriangles();
        addGeometry( geometryInfo.getIndexedGeometryArray( true ) );
    }

//    /**
//     * Adds the given surface and texcoords to this shape.
//     *
//     * @param surface
//     *            to be added
//     * @param texCoords
//     *            to be added.
//     */
//    public void addGeometry( Surface surface, List<TexCoord2f> texCoords ) {
//        LOG.logDebug( "Adding geometry to textured surface: " + surface + "with texCoords: " + texCoords );
//        List<Point3d> coordinates = new ArrayList<Point3d>();
//        List<Integer> contourCount = new ArrayList<Integer>( 200 );
//        if ( surface != null ) {
//            // SurfaceBoundary sb = surface.getSurfaceBoundary();
//            // if ( sb != null ) {
//            // Ring outerRing = sb.getExteriorRing();
//            // if ( outerRing != null ) {
//            // Position[] pos = outerRing.getPositions();
//            // if ( pos != null ) {
//            // // the surface start and end point are the same, ignoring the last node.
//            // for ( int i = 0; i < pos.length - 1; ++i ) {
//            // coordinates.add( pos[i].getAsPoint3d() );
//            // }
//            // contourCount.add( new Integer( pos.length - 1 ) );
//            // }
//            //
//            // Ring[] innerRings = sb.getInteriorRings();
//            // if ( innerRings != null ) {
//            // for ( Ring innerRing : innerRings ) {
//            // pos = innerRing.getPositions();
//            // if ( pos != null ) {
//            // // the surface start and end point are the same, ignoring the last node.
//            // for ( int i = 0; i < pos.length - 1; ++i ) {
//            // coordinates.add( pos[i].getAsPoint3d() );
//            // }
//            // contourCount.add( new Integer( pos.length - 1 ) );
//            // }
//            // }
//            // }
//            // }
//            //
//            // }
//            extractSurface( surface, coordinates, contourCount );
//        }
//        GeometryInfo geometryInfo = new GeometryInfo( GeometryInfo.POLYGON_ARRAY );
//
//        geometryInfo.setCoordinates( coordinates.toArray( new Point3d[0] ) );
//        int[] sc = new int[contourCount.size()];
//        int stripCountNumber = 0;
//        for ( int i = 0; i < contourCount.size(); ++i ) {
//            sc[i] = contourCount.get( i ).intValue();
//            stripCountNumber += sc[i];
//        }
//        LOG.logDebug( "Geometry has number of vertices: " + coordinates.size()
//                      + " the number of Strip counts adds up to: " + stripCountNumber );
//        geometryInfo.setStripCounts( sc );
//        // we don't actually need the contours, just an array which has the right length.
//        geometryInfo.setContourCounts( new int[] { sc.length } );
//
//        if ( texCoords != null ) {
//            geometryInfo.setTextureCoordinateParams( 1, 2 );
//            geometryInfo.setTextureCoordinates( 0, texCoords.toArray( new TexCoord2f[0] ) );
//        }
//        NormalGenerator ng = new NormalGenerator();
//        ng.generateNormals( geometryInfo );
//
//        geometryInfo.convertToIndexedTriangles();
//        addGeometry( geometryInfo.getGeometryArray() );
//    }

    /**
     * Creates an appearance by setting the texture and default texture coordinates if none were given.
     *
     * @return the altered appearance
     */
    public Appearance createAppearance() {
        Appearance ap = getAppearance();
        if ( texture != null ) {
            ap.setTexture( texture );
            ap.setTextureAttributes( RenderingConfiguration.getInstance().getTextureAttributes() );
        }
        return ap;
    }
}
