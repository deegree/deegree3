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

import static org.slf4j.LoggerFactory.getLogger;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.deegree.rendering.r3d.opengl.rendering.ShaderProgram;
import org.deegree.rendering.r3d.opengl.rendering.dem.CompositingShader;
import org.slf4j.Logger;

/**
 * The <code>ConfiguredOpenGLInitValues</code> class initializes a GL context with some configured values.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class ConfiguredOpenGLInitValues implements GLEventListener {

    private final static Logger LOG = getLogger( ConfiguredOpenGLInitValues.class );

    private final int numberOfTextureUnits;

    private ShaderProgram[] compositeTextureShaderPrograms;

    private final String LOCK = "LOCK";

    /**
     * @param numberOfTexturesUnits
     */
    public ConfiguredOpenGLInitValues( int numberOfTexturesUnits ) {
        this.numberOfTextureUnits = numberOfTexturesUnits;
        this.compositeTextureShaderPrograms = null;
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        // no rendering
    }

    @Override
    public void displayChanged( GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged ) {
        // no rendering
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        // enable debuging.
        drawable.setGL( new DebugGL( drawable.getGL() ) );
        GL gl = drawable.getGL();
        oldInit( gl );
        // gl.glEnable( GL.GL_CULL_FACE );
        // gl.glCullFace( GL.GL_BACK );

    }

    private void oldInit( GL gl ) {
        LOG.debug( "Initializing opengl context settings." );
        gl.glShadeModel( GL.GL_SMOOTH );
        gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_FILL );

        gl.glLightModelf( GL.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE );

        gl.glEnable( GL.GL_LIGHTING );
        gl.glEnable( GL.GL_LIGHT0 );

        gl.glDepthFunc( GL.GL_LEQUAL );
        gl.glEnable( GL.GL_DEPTH_TEST );

        // enable vertex arrays
        gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
        gl.glEnableClientState( GL.GL_NORMAL_ARRAY );
        // LOG.debug( "Created shader program id: " + shaderProgramId );

        createCompositingTextureShaderPrograms( gl );

    }

    /**
     * Create the dem compositing texture shader programs for the given context
     * 
     * @param gl
     */
    public void createCompositingTextureShaderPrograms( GL gl ) {
        LOG.debug( "building " + numberOfTextureUnits + " shader programs" );
        synchronized ( LOCK ) {
            if ( this.compositeTextureShaderPrograms == null ) {
                this.compositeTextureShaderPrograms = new ShaderProgram[this.numberOfTextureUnits];
                for ( int i = 0; i < compositeTextureShaderPrograms.length; i++ ) {
                    String shaderSource = CompositingShader.getGLSLCode( i + 1 );
                    compositeTextureShaderPrograms[i] = new ShaderProgram();
                    int shaderId = compositeTextureShaderPrograms[i].createFragmentShader( gl, shaderSource );
                    if ( compositeTextureShaderPrograms[i].attachShader( gl, shaderId ) ) {
                        compositeTextureShaderPrograms[i].linkProgram( gl );
                    } else {
                        LOG.warn( "Could not attach compositing texture shader program: " + i
                                  + " error messages should have been supplied before this message." );
                    }
                }
            }
        }
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
        // no rendering
    }

    /**
     * @return the shader programs for compositing multiple textures.
     */
    public ShaderProgram[] getCompositingTextureShaderPrograms() {
        return this.compositeTextureShaderPrograms;
    }

    /**
     * @return new float[] { .6f, .6f, .6f, 1 };
     */
    public static float[] getTerrainAmbient() {
        return new float[] { .6f, .6f, .6f, 1 };
    }

    /**
     * @return new float[] { .8f, .8f, .8f, 1 };
     */
    public static float[] getTerrainDiffuse() {
        return new float[] { .8f, .8f, .8f, 1 };
    }

    /**
     * @return float[]{.2f,.2f,.2f,1}
     */
    public static float[] getTerrainSpecular() {
        return new float[] { .2f, .2f, .2f, 1 };
    }

    /**
     * @return 1.5f, this value should be configured.
     */
    public static float getTerrainShininess() {
        return 1.5f;
    }

}
