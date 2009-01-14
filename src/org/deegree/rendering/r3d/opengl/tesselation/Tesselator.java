//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.rendering.r3d.opengl.tesselation;

import java.util.ArrayList;

import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;

import org.deegree.rendering.r3d.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.geometry.TexturedGeometry;
import org.deegree.rendering.r3d.opengl.rendering.RenderableGeometry;
import org.deegree.rendering.r3d.opengl.rendering.RenderableQualityModel;

/**
 * The <code>Tesselator</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Tesselator {

    private GLU glu;

    public Tesselator() {
        glu = new GLU();
    }

    public static void main( String[] args ) {
        Tesselator t = new Tesselator();
        ArrayList<SimpleAccessGeometry> simpleAccessGeometries = new ArrayList<SimpleAccessGeometry>();
        simpleAccessGeometries.add( t.createStar() );
        simpleAccessGeometries.add( t.createTexturedConcav() );
        GeometryQualityModel gqm = new GeometryQualityModel( simpleAccessGeometries );
        RenderableQualityModel rqm = t.createRenderableQM( gqm );

    }

    private SimpleAccessGeometry createStar() {
        float[] coords = new float[] { 0, 0, 1, -1, 0, -1, 1, 0, -1, 0, 0, -1, -1, 0, 1, 1, 0, 1 };
        // float[] coords = new float[] { 0, 0, 1, -1, 0, -1, 1, 0, -1, 0, 0, -1 };

        return new SimpleAccessGeometry( coords );
    }

    private TexturedGeometry createTexturedConcav() {
        float[] coords = new float[] { -1, -1, 1, 0.5f, -.5f, 0, 0.4f, -.4f, -.4f, -0.8f, -.2f, -1f, -.1f, .1f, -.2f,
                                      -.2f, -.2f, .2f, -1, -1, 1 };
        float[] texCoords = new float[] { 0, 1, 1, .6f, .9f, .3f, .1f, 0, .5f, .4f, .6f, .5f, 0, 1 };

        return new TexturedGeometry( coords,
                                     "/home/rutger/workspace/2.2_testing_igeo_standard/images/logo-deegree.png",
                                     texCoords );
    }

    public synchronized RenderableQualityModel createRenderableQM( GeometryQualityModel originalObject ) {
        GLUtessellator tess = glu.gluNewTess();
        ArrayList<SimpleAccessGeometry> geometryPatches = originalObject.getGeometryPatches();
        ArrayList<RenderableGeometry> results = new ArrayList<RenderableGeometry>( geometryPatches.size() );
        for ( SimpleAccessGeometry geom : geometryPatches ) {
            results.add( tesselatePolygon( tess, geom ) );
        }
        glu.gluDeleteTess( tess );
        return new RenderableQualityModel( results );
    }

    private void registerCallback( GLUtessellator tess, GeometryCallBack cb ) {
        glu.gluTessCallback( tess, GLU.GLU_TESS_BEGIN, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_EDGE_FLAG_DATA, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_VERTEX_DATA, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_END_DATA, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_COMBINE_DATA, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_ERROR_DATA, cb );
    }

    private final RenderableGeometry tesselatePolygon( GLUtessellator tess, SimpleAccessGeometry geom ) {
        final GeometryCallBack callBack;
        if ( geom instanceof TexturedGeometry ) {
            callBack = new TexturedGeometryCallBack( (TexturedGeometry) geom );
        } else {
            callBack = new GeometryCallBack( geom );
        }
        registerCallback( tess, callBack );

        int numberOfOrdinates = geom.getGeometry().length;
        int[] innerRings = geom.getInnerRings();
        // the current ring
        int currentRing = 1;
        // the current position of the ring in the 3d float array float[0]=x ; float[1]=y; float[2]=z;
        int ringBegin = 0;
        // end of the ring in the 3d float array, if no rings, just use the whole geometry
        int ringEnd = numberOfOrdinates;
        boolean hasRings = ( innerRings != null && innerRings.length > 0 );
        glu.gluTessBeginPolygon( tess, null );
        {
            do {
                if ( hasRings ) {
                    // we've got innerrings
                    if ( currentRing < innerRings.length ) {
                        ringEnd = innerRings[currentRing++];
                    } else {
                        ringEnd = numberOfOrdinates;
                    }
                }
                tesselateRing( tess, ringBegin, ringEnd, callBack );
                // the beginning of the new ring or coords.length if no more rings.
                ringBegin = ringEnd;
            } while ( ringBegin < numberOfOrdinates );
            // begin/next contour are in the tesselate ring function, just close the last contour
            glu.gluTessEndContour( tess );
        }
        glu.gluTessEndPolygon( tess );
        return callBack.createRenderableGeometry();
    }

    /**
     * Tesselate a ring indexed by the begin and end.
     * 
     * @param glu
     * @param tess
     * @param begin
     * @param end
     * @param coords
     * @param callBack
     */
    private final void tesselateRing( GLUtessellator tess, int begin, int end, GeometryCallBack callBack ) {
        if ( begin == 0 ) {
            glu.gluTessBeginContour( tess );
        } else {
            glu.gluNextContour( tess, GLU.GLU_INTERIOR );
        }
        {
            for ( int i = begin; i < end; i += 3 ) {
                Vertex vertex = callBack.createNewVertex( i / 3 );
                glu.gluTessVertex( tess, vertex.getCoordsAsDouble(), 0, vertex );
            }
        }
    }
}
