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

package org.deegree.rendering.r3d.opengl.tesselation;

import java.util.ArrayList;

import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;

import org.deegree.rendering.r3d.model.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.model.geometry.TexturedGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModelPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>Tesselator</code> class is a {@link GLUtessellator} utility wrapper. Its main purpose is the creation of
 * a {@link RenderableQualityModel} out of a {@link GeometryQualityModel} by triangulating (tesselating) all it's
 * {@link SimpleAccessGeometry}.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Tesselator {
    private final transient static Logger LOG = LoggerFactory.getLogger( Tesselator.class );

    private GLU glu;

    private boolean useDirectBuffers;

    /**
     * Create a tesselator which triangulates all {@link SimpleAccessGeometry} of a {@link GeometryQualityModel}.
     * 
     * @param useDirectBuffers
     *            to use direct buffers instead of heap buffers.
     */
    public Tesselator( boolean useDirectBuffers ) {
        glu = new GLU();
        this.useDirectBuffers = useDirectBuffers;
    }

    /**
     * Create a renderable quality model from the given 'original' geometry model, by tesselating (triangulating) its
     * polygons.
     * 
     * @param objectID
     *            identifying the given quality model, if an error occurs. May be <code>null</code>
     * 
     * @param originalObject
     *            containing polygons
     * @return the renderable object.
     */
    public RenderableQualityModel createRenderableQM( String objectID, GeometryQualityModel originalObject ) {
        GLUtessellator tess = glu.gluNewTess();
        if ( originalObject == null ) {
            throw new NullPointerException( "The original object may not be null" );
        }
        ArrayList<SimpleAccessGeometry> geometryPatches = originalObject.getQualityModelParts();
        ArrayList<RenderableQualityModelPart> results = new ArrayList<RenderableQualityModelPart>(
                                                                                                   geometryPatches.size() );
        for ( SimpleAccessGeometry geom : geometryPatches ) {
            if ( geom != null ) {
                GeometryCallBack callBack = createAndRegisterCallBack( tess, geom );
                try {
                    RenderableGeometry result = tesselatePolygon( tess, callBack );
                    if ( result != null ) {
                        LOG.trace( "Resulting renderable has " + result.getVertexCount() + " number of vertices." );
                        results.add( result );
                    }
                } catch ( Exception e ) {
                    LOG.warn( "Error while tesselating following geometry (from a quality model"
                              + ( objectID == null || "".equals( objectID ) ? ")" : " with id: " + objectID + ")" )
                              + ":\n" + geom.toString() + "\n(are the vertices colinear?). Original error message was:"
                              + e.getLocalizedMessage() );
                }
            }
        }
        glu.gluDeleteTess( tess );
        return new RenderableQualityModel( results );
    }

    /**
     * Register the callback with the given tesselator.
     * 
     * @param tess
     *            to register with
     * @param cb
     *            the callback object.
     */
    private void registerCallback( GLUtessellator tess, GeometryCallBack cb ) {
        glu.gluTessCallback( tess, GLU.GLU_TESS_BEGIN, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_EDGE_FLAG_DATA, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_VERTEX_DATA, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_END_DATA, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_COMBINE_DATA, cb );
        glu.gluTessCallback( tess, GLU.GLU_TESS_ERROR_DATA, cb );
    }

    /**
     * Create a renderable geometry from the given {@link SimpleAccessGeometry} by tesselating it.
     * 
     * @param originalGeometry
     *            may not be null.
     * @return the {@link RenderableGeometry} or <code>null</code> if the given geometry could not be triangulated.
     */
    public final RenderableGeometry tesselateGeometry( SimpleAccessGeometry originalGeometry ) {
        GLUtessellator tess = glu.gluNewTess();
        if ( originalGeometry == null ) {
            throw new NullPointerException( "The original geometry may not be null" );
        }
        GeometryCallBack callBack = createAndRegisterCallBack( tess, originalGeometry );
        RenderableGeometry result = tesselatePolygon( tess, callBack );
        if ( result != null && LOG.isTraceEnabled() ) {
            LOG.trace( "Resulting renderable has " + result.getVertexCount() + " number of vertices." );
        }
        glu.gluDeleteTess( tess );
        return result;
    }

    /**
     * Create a callback for the given geometry and register it with the given tesselator object.
     * 
     * @param tess
     * @param geom
     * @return
     */
    private final GeometryCallBack createAndRegisterCallBack( GLUtessellator tess, SimpleAccessGeometry geom ) {
        final GeometryCallBack callBack;
        if ( geom instanceof TexturedGeometry ) {
            callBack = new TexturedGeometryCallBack( (TexturedGeometry) geom );
        } else {
            callBack = new GeometryCallBack( geom );
        }
        registerCallback( tess, callBack );
        return callBack;
    }

    /**
     * Tesselate the given geometry with respect to the innerrings and texture coordinates.
     * 
     * @param tess
     * @param geom
     * @return
     */
    @SuppressWarnings("null")
    private final RenderableGeometry tesselatePolygon( GLUtessellator tess, GeometryCallBack callBack ) {

        int numberOfVertices = callBack.getGeometry().getVertexCount();
        int[] innerRings = callBack.getGeometry().getInnerRings();
        // the current ring
        int currentRing = 0;
        // the current position of the ring in the 3d float array float[0]=x ; float[1]=y; float[2]=z;
        int ringBegin = 0;
        // end of the ring in the 3d float array, if no rings, just use the whole geometry
        int ringEnd = numberOfVertices;
        boolean hasRings = ( innerRings != null && innerRings.length > 0 );
        LOG.trace( "SimpleAccessGeometry has " + numberOfVertices + " number of vertices." );
        glu.gluTessBeginPolygon( tess, null );
        {
            do {
                if ( hasRings ) {
                    // we've got innerrings, eclipse doesn't seem to understand, that the innerrings can not be null
                    // (hasrings), so lets suppress warnings.
                    if ( currentRing < innerRings.length ) {
                        ringEnd = innerRings[currentRing++] / 3;
                    } else {
                        ringEnd = numberOfVertices;
                    }
                }
                LOG.trace( "polygon begin vertex: " + ringBegin );
                LOG.trace( "polygon end vertex: " + ringEnd );

                tesselateRing( tess, ringBegin, ringEnd, callBack );
                // the beginning of the new ring or coords.length if no more rings.
                ringBegin = ringEnd;
            } while ( ringBegin < numberOfVertices );
            // begin/next contour are in the tesselate ring function, just close the last contour
            glu.gluTessEndContour( tess );
        }
        glu.gluTessEndPolygon( tess );
        return callBack.createRenderableGeometry( useDirectBuffers );
    }

    /**
     * Tesselate a ring indexed by the begin and end vertices not array positions.
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
            for ( int i = begin; i < end; ++i ) {
                Vertex vertex = callBack.createNewVertex( i );
                glu.gluTessVertex( tess, vertex.getCoordsAsDouble(), 0, vertex );
            }
        }
    }
}
