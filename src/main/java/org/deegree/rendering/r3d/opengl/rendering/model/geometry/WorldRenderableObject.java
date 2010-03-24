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

package org.deegree.rendering.r3d.opengl.rendering.model.geometry;

import javax.media.opengl.GL;

import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.model.WorldObject;
import org.deegree.rendering.r3d.opengl.rendering.JOGLRenderable;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.LODSwitcher;

/**
 * The <code>WorldRenderableObject</code> defines a number of renderable quality levels, where each level may be a
 * PrototypeReference or a RenderableGeometry model. Which LOD is should be rendered is deterimined by applying the
 * {@link LODSwitcher} to the position and the error scalar.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class WorldRenderableObject extends WorldObject<RenderableQualityModelPart, RenderableQualityModel> implements
                                                                                                          JOGLRenderable {
    private static final long serialVersionUID = 2998719476993351372L;

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( WorldRenderableObject.class );

    private LODSwitcher switchLevels;

    /**
     * Creates a new WorldRenderableObject with given number of data quality levels (LOD)
     * 
     * @param id
     * @param time
     * @param bbox
     * 
     * @param levels
     */
    public WorldRenderableObject( String id, String time, Envelope bbox, int levels ) {
        this( id, time, bbox, new RenderableQualityModel[levels] );
    }

    /**
     * @param id
     *            of this object
     * @param time
     *            this object was created in the dbase
     * @param bbox
     *            of this object (may not be null)
     * @param qualityLevels
     *            this data object may render.
     */
    public WorldRenderableObject( String id, String time, Envelope bbox, RenderableQualityModel[] qualityLevels ) {
        super( id, time, bbox, qualityLevels );
    }

    /**
     * Renders the model at the given quality level or the lesser quality level if the requested one is not available.
     * 
     * @param context
     * @param params
     * @param level
     * @param geomBuffer
     */
    private void render( RenderContext glRenderContext, int level, DirectGeometryBuffer geomBuffer ) {
        if ( qualityLevels != null ) {
            if ( level >= 0 && qualityLevels.length > level ) {
                RenderableQualityModel model = qualityLevels[level];
                if ( model == null ) {
                    // first find the next less quality
                    for ( int i = level; i >= 0 && model == null; --i ) {
                        model = qualityLevels[i];
                    }
                }
                if ( model != null ) {
                    model.renderPrepared( glRenderContext, geomBuffer );
                }
            }
        }
        if ( LOG.isDebugEnabled() ) {
            debug( glRenderContext );
        }
    }

    @Override
    public void render( RenderContext glRenderContext ) {
        render( glRenderContext, calcQualityLevel( glRenderContext ), null );
    }

    /**
     * This method assumes the coordinates and normals are located in the given {@link DirectGeometryBuffer}.
     * 
     * @param glRenderContext
     * @param geomBuffer
     *            to be get the coordinates from.
     */
    public void renderPrepared( RenderContext glRenderContext, DirectGeometryBuffer geomBuffer ) {
        render( glRenderContext, calcQualityLevel( glRenderContext ), geomBuffer );
    }

    /**
     * @param glRenderContext
     * @return the level to render.
     */
    protected int calcQualityLevel( RenderContext glRenderContext ) {
        int level = qualityLevels.length - 1;

        if ( switchLevels != null ) {
            level = switchLevels.calcLevel( glRenderContext, getPosition(), level, getErrorScalar() );
        }
        return level;
    }

    /**
     * @return the number of ordinates in all qualitylevels, needed for the initialization of the direct buffer.
     */
    public int getOrdinateCount() {
        int result = 0;
        if ( qualityLevels != null ) {
            for ( RenderableQualityModel model : qualityLevels ) {
                if ( model != null ) {
                    result += model.getOrdinateCount();
                }
            }
        }
        return result;
    }

    /**
     * @return the number of ordinates in all qualitylevels, needed for the initialization of the direct buffer.
     */
    public int getTextureOrdinateCount() {
        int result = 0;
        if ( qualityLevels != null ) {
            for ( RenderableQualityModel model : qualityLevels ) {
                if ( model != null ) {
                    result += model.getTextureOrdinateCount();
                }
            }
        }
        return result;
    }

    /**
     * @param switchLevels
     */
    public void setSwitchLevels( LODSwitcher switchLevels ) {
        this.switchLevels = switchLevels;
    }

    private void debug( RenderContext context ) {
        GL gl = context.getContext();
        gl.glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[] { 1, 0, 0 }, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, new float[] { 1, 0, 0 }, 0 );
        float[] bbox = getModelBBox();
        gl.glBegin( GL.GL_QUADS );
        // Front face
        gl.glNormal3f( 0, 0, 1 );
        gl.glVertex3f( bbox[0], bbox[1], bbox[2] );
        gl.glVertex3f( bbox[3], bbox[4], bbox[2] );
        gl.glVertex3f( bbox[3], bbox[4], bbox[5] );
        gl.glVertex3f( bbox[0], bbox[1], bbox[5] );
        gl.glEnd();
        gl.glPopAttrib();

    }
}
