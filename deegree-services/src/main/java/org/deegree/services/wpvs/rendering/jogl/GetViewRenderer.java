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

package org.deegree.services.wpvs.rendering.jogl;

import static org.deegree.rendering.r3d.opengl.JOGLUtils.getFrameBufferRGB;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.deegree.commons.utils.SunInfo;
import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.commons.utils.nio.PooledByteBuffer;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.JOGLUtils;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.dem.Colormap;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TerrainRenderingManager;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TextureManager;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.RenderableManager;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.TreeRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;
import org.deegree.services.wpvs.PerspectiveViewService;
import org.deegree.services.wpvs.controller.getview.GetView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.texture.Texture;

/**
 * Performs {@link GetView} requests for the {@link PerspectiveViewService}.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetViewRenderer implements GLEventListener {

    private final static Logger LOG = LoggerFactory.getLogger( GetViewRenderer.class );

    private GLU glu;

    private GetView request;

    private BufferedImage resultImage;

    private final TerrainRenderingManager demRenderer;

    private final List<TextureManager> textureManagers;

    private final List<RenderableManager<?>> renderableRenderers;

    private ViewParams viewParams;

    private boolean renderDEM;

    private final String copyrightID;

    private final int width;

    private final int height;

    @SuppressWarnings("unused")
    private final double copyrightScale;

    private final PooledByteBuffer imageBuffer;

    private RenderContext context;

    private final double sceneLatitude;

    private Colormap colormap;

    /**
     * @param request
     * @param glRenderContext
     * @param imageBuffer
     * @param textureManagers
     * @param demRenderer
     * @param colormap
     *            to render instead of a texture.
     * @param buildingRenderers
     *            containing requested buildings
     * @param treeRenderers
     *            containing requested trees.
     * @param copyrightID
     *            the texture id of the copyright.
     * @param copyrightScale
     *            the id of the copyright image
     * @param sceneLatitude
     *            the latitude of the requested scene, necessary to calculate the light position.
     * 
     */
    public GetViewRenderer( GetView request, RenderContext glRenderContext, PooledByteBuffer imageBuffer,
                            TerrainRenderingManager demRenderer, Colormap colormap,
                            List<TextureManager> textureManagers, List<RenderableManager<?>> renderableRenderers,
                            String copyrightID, double copyrightScale, double sceneLatitude ) {
        this.imageBuffer = imageBuffer;
        this.demRenderer = demRenderer;
        this.textureManagers = textureManagers;
        this.renderableRenderers = renderableRenderers;
        this.copyrightID = copyrightID;
        this.copyrightScale = copyrightScale;
        this.sceneLatitude = sceneLatitude;
        glu = new GLU();
        this.request = request;
        this.viewParams = request.getViewParameters();
        this.width = viewParams.getScreenPixelsX();
        this.height = viewParams.getScreenPixelsY();
        this.context = glRenderContext;
        this.colormap = colormap;
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        GL gl = drawable.getGL();
        LOG.trace( "display( GLAutoDrawable ) called" );

        long begin = System.currentTimeMillis();
        gl.glLoadIdentity();
        init( gl );
        reshape( gl );

        gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

        context.setContext( gl );

        setBackground( context );

        // set perspective
        Point3d eyePos = viewParams.getViewFrustum().getEyePos();
        Point3d lookingAt = viewParams.getViewFrustum().getLookingAt();
        Vector3d viewerUp = viewParams.getViewFrustum().getUp();
        glu.gluLookAt( eyePos.x, eyePos.y, eyePos.z, lookingAt.x, lookingAt.y, lookingAt.z, viewerUp.x, viewerUp.y,
                       viewerUp.z );

        //
        TextureManager[] tManager = new TextureManager[0];
        if ( !textureManagers.isEmpty() ) {
            tManager = textureManagers.toArray( new TextureManager[textureManagers.size()] );
        }
        demRenderer.render( context, renderDEM, colormap, tManager );
        if ( renderableRenderers != null ) {
            Collections.sort( renderableRenderers, new Comparator<RenderableManager<?>>() {
                @Override
                public int compare( RenderableManager<?> o1, RenderableManager<?> o2 ) {
                    return ( o1 instanceof TreeRenderer ) ? 1 : -1;
                }
            } );
            for ( RenderableManager<?> br : renderableRenderers ) {
                br.render( context );
            }
        }

        renderCopyright( context );
        gl.glFinish();
        LOG.trace( "Rendering scene took: " + ( System.currentTimeMillis() - begin ) + " ms." );
        writeResult( gl );
    }

    private void init( GL gl ) {
        float[] cc = JOGLUtils.convertColorFloats( request.getBackgroundColor() );
        gl.glClearColor( cc[0], cc[1], cc[2], 0.0f );

        SunInfo pos = request.getSceneParameters().getSunPosition();

        float[] light_position = pos.getEucledianPosition( sceneLatitude );
        Vectors3f.scale( -1, light_position );

        float[] ambientAndDiffuse = pos.calculateSunlight( sceneLatitude );

        // float intens = pos.calcSunlightIntensity( ambientAndDiffuse, 0.5f );
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_POSITION, new float[] { light_position[0], light_position[1],
                                                                 light_position[2], 0 }, 0 );
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_AMBIENT, new float[] { ambientAndDiffuse[0], ambientAndDiffuse[1],
                                                                ambientAndDiffuse[2], 1 }, 0 );

        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[] { ambientAndDiffuse[0], ambientAndDiffuse[1],
                                                                           ambientAndDiffuse[2], 1 }, 0 );
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_DIFFUSE, new float[] { ambientAndDiffuse[0], ambientAndDiffuse[1],
                                                                ambientAndDiffuse[2], 1 }, 0 );

    }

    /**
     * @param gl
     */
    private void renderCopyright( RenderContext context ) {
        Texture copyImage = TexturePool.getTexture( context, copyrightID );
        if ( copyImage != null ) {
            float tH = copyImage.getHeight();
            float tW = copyImage.getWidth();

            float quadWidth = tW;
            float quadHeight = tH;
            /**
             * rb: Scaling of copyright image could be realized with following code.
             */
            // if ( width > height ) {
            // quadWidth = (float) ( width * copyrightScale );
            // quadHeight = quadWidth * ( ( tW > tH ) ? ( tH / tW ) : ( tW / tH ) );
            // } else {
            // quadHeight = (float) ( height * copyrightScale );
            // // if the width of the texture is larger then the height, use the scalediff to calculate the texwidth.
            // quadWidth = quadHeight * ( ( tW > tH ) ? ( tW / tH ) : ( tH / tW ) );
            // }
            context.getContext().glEnable( GL.GL_ALPHA_TEST );
            context.getContext().glAlphaFunc( GL.GL_GREATER, 0.4f );
            draw2D( context, 0, 0, quadWidth, quadHeight, copyImage, true );
            context.getContext().glDisable( GL.GL_ALPHA_TEST );
            context.getContext().glAlphaFunc( GL.GL_ALWAYS, 1 );
        }
    }

    /**
     * @param gl
     */
    private void reshape( GL gl ) {
        float aspect = (float) width / (float) height;
        LOG.trace( "reshape( GLAutoDrawable, " + 0 + ", " + 0 + ", " + width + ", " + height + " ) called, aspect: "
                   + aspect );

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glLoadIdentity();
        gl.glViewport( 0, 0, width, height );
        glu.gluPerspective( viewParams.getViewFrustum().getFOVY(), aspect, viewParams.getViewFrustum().getZNear(),
                            viewParams.getViewFrustum().getZFar() );
        gl.glMatrixMode( GL.GL_MODELVIEW );
    }

    /**
     * @param gl
     */
    private void setBackground( RenderContext context ) {
        Texture skyImage = TexturePool.getTexture( context, request.getSceneParameters().getSkyImage() );
        if ( skyImage != null ) {
            draw2D( context, 0, 0, width, height, skyImage, false );
        }
    }

    /**
     * Draw a 2d quad width given width height and location, and use given texture.
     * 
     * @param gl
     *            to render to
     * @param x
     * @param y
     * @param quadWidth
     * @param quadHeight
     * @param texture
     * @param useDepth
     *            true if the depth buffer should be enabled.
     */
    private void draw2D( RenderContext context, float x, float y, float quadWidth, float quadHeight, Texture texture,
                         boolean useDepth ) {
        GL gl = context.getContext();
        if ( !useDepth ) {
            gl.glDisable( GL.GL_DEPTH_TEST );
        }

        gl.glDisable( GL.GL_LIGHTING );
        gl.glEnable( GL.GL_TEXTURE_2D );

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D( 0, width, 0, height );

        gl.glMatrixMode( GL.GL_MODELVIEW );

        gl.glPushMatrix();
        gl.glLoadIdentity();

        texture.bind();
        gl.glBegin( GL.GL_QUADS );
        {
            gl.glTexCoord2f( 0, 0 );
            gl.glVertex2f( x, quadHeight );

            gl.glTexCoord2f( 1f, 0f );
            gl.glVertex2f( quadWidth, quadHeight );

            gl.glTexCoord2f( 1f, 1f );
            gl.glVertex2f( quadWidth, y );

            gl.glTexCoord2f( 0f, 1f );
            gl.glVertex2f( x, y );

        }
        gl.glEnd();
        texture.disable();
        gl.glPopMatrix();

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glPopMatrix();
        gl.glMatrixMode( GL.GL_MODELVIEW );

        gl.glDisable( GL.GL_TEXTURE_2D );
        gl.glEnable( GL.GL_LIGHTING );
        if ( !useDepth ) {
            gl.glEnable( GL.GL_DEPTH_TEST );
        }
    }

    @Override
    public void displayChanged( GLAutoDrawable arg0, boolean arg1, boolean arg2 ) {
        LOG.info( "display changed event called." );
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        // enable debuging.
        drawable.setGL( new DebugGL( drawable.getGL() ) );
        LOG.info( "init event called." );
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
        LOG.info( "reshape event called." );
    }

    private void writeResult( GL gl ) {
        ByteBuffer buffer = imageBuffer.getBuffer();
        int limit = buffer.limit();
        buffer.limit( width * height * 3 );
        resultImage = getFrameBufferRGB( gl, buffer, 0, 0, width, height, null );
        buffer.limit( limit );
    }

    /**
     * Returns the rendered image.
     * 
     * @return the rendered image, never <code>null</code>
     */
    public BufferedImage getResultImage() {
        return resultImage;
    }
}
