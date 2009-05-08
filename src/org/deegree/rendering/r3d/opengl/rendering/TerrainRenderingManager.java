//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.opengl.rendering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import org.deegree.commons.concurrent.ExecutionFinishedEvent;
import org.deegree.commons.concurrent.Executor;
import org.deegree.commons.utils.JOGLUtils;
import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.rendering.r3d.multiresolution.SpatialSelection;
import org.deegree.rendering.r3d.multiresolution.crit.ViewFrustumCrit;
import org.deegree.rendering.r3d.opengl.rendering.dem.CompositingShader;
import org.deegree.rendering.r3d.opengl.rendering.dem.FragmentTexture;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;
import org.deegree.rendering.r3d.opengl.rendering.dem.TextureManager;
import org.deegree.rendering.r3d.opengl.rendering.dem.TextureTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.GLUT;

public class TerrainRenderingManager {

    private static final Logger LOG = LoggerFactory.getLogger( TerrainRenderingManager.class );

    private final GLUT glut = new GLUT();

    private static final long serialVersionUID = 1854116460506116944L;

    private RenderFragmentManager fragmentManager;

    // contains the mesh fragments that make up the current LOD
    private Set<RenderMeshFragment> activeLOD = new HashSet<RenderMeshFragment>();

    // shaderProgramIds [i]: id of GL shader program for compositing i texture layers
    private int[] shaderProgramIds;

    // max texture size as reported by OpenGL driver
    private int maxTextureSize = 4096;

    private double maxPixelError;

    private double maxProjectedTexelSize;

    public TerrainRenderingManager( RenderFragmentManager fragmentManager, double maxPixelError,
                                    double maxProjectedTexelSize ) {
        this.fragmentManager = fragmentManager;
        this.maxPixelError = maxPixelError;
        this.maxProjectedTexelSize = maxProjectedTexelSize;
    }

    public void init( GLAutoDrawable drawable ) {
        LOG.trace( "init( GLAutoDrawable ) called" );

        GL gl = drawable.getGL();

        int numTextureUnits = 8;
        LOG.info( "building " + numTextureUnits + " shader programs" );
        shaderProgramIds = new int[numTextureUnits];
        for ( int i = 1; i <= numTextureUnits; i++ ) {
            LOG.info( "Building fragment shader for compositing " + i + " textures." );

            // generate and compile shader
            int shaderId = gl.glCreateShader( GL.GL_FRAGMENT_SHADER );
            gl.glShaderSource( shaderId, 1, new String[] { CompositingShader.getGLSLCode( i ) }, (int[]) null, 0 );
            gl.glCompileShader( shaderId );

            // create program and attach shader
            int shaderProgramId = gl.glCreateProgram();
            gl.glAttachShader( shaderProgramId, shaderId );

            // link program
            gl.glLinkProgram( shaderProgramId );
            int[] linkStatus = new int[1];
            gl.glGetProgramiv( shaderProgramId, GL.GL_LINK_STATUS, linkStatus, 0 );
            if ( linkStatus[0] == GL.GL_FALSE ) {
                int[] length = new int[1];
                gl.glGetProgramiv( shaderProgramId, GL.GL_INFO_LOG_LENGTH, length, 0 );
                byte[] infoLog = new byte[length[0]];
                gl.glGetProgramInfoLog( shaderProgramId, length[0], length, 0, infoLog, 0 );
                String msg = new String( infoLog );
                LOG.error( "shader source: " + CompositingShader.getGLSLCode( i ) );
                LOG.error( msg );
                throw new RuntimeException( msg );
            }

            // validate program
            gl.glValidateProgram( shaderProgramId );
            int[] validateStatus = new int[1];
            gl.glGetProgramiv( shaderProgramId, GL.GL_VALIDATE_STATUS, validateStatus, 0 );
            if ( validateStatus[0] == GL.GL_FALSE ) {
                int[] length = new int[1];
                gl.glGetProgramiv( shaderProgramId, GL.GL_INFO_LOG_LENGTH, length, 0 );
                byte[] infoLog = new byte[length[0]];
                gl.glGetProgramInfoLog( shaderProgramId, length[0], length, 0, infoLog, 0 );
                String msg = new String( infoLog );
                LOG.error( msg );
                throw new RuntimeException( msg );
            }
            shaderProgramIds[i - 1] = shaderProgramId;
        }
        
        System.out.println ("Max texture size: " + JOGLUtils.getMaxTextureSize(gl));
    }

    /**
     * Renders a view-optimized representation of the terrain geometry using the given scale and textures to the
     * specified GL context.
     * 
     * @param gl
     * @param params
     * @param disableElevationModel
     * @param textureManagers
     */
    public void render( GL gl, ViewParams params, boolean disableElevationModel, TextureManager[] textureManagers ) {

        // ensure correct zScale (zScale = 0 does not work as expected)
        float zScale = ( disableElevationModel || params.getTerrainScale() < 0.001f ) ? 0.001f
                                                                                     : params.getTerrainScale();

        // adapt geometry LOD (fragments)
        updateLOD( gl, params, zScale, textureManagers );

        // determine textures for each fragment
        Map<RenderMeshFragment, List<FragmentTexture>> fragmentToTextures = getTextures( params, activeLOD,
                                                                                         textureManagers );

        // render fragments with textures
        render( gl, fragmentToTextures, textureManagers, zScale );

        displayStats( gl, params );
    }

    public Collection<RenderMeshFragment> getCurrentLOD() {
        return activeLOD;
    }    
    
    private void updateLOD( GL gl, ViewParams params, float scale, TextureManager[] textureManagers ) {

        Set<RenderMeshFragment> nextLOD = getNewLOD( params, scale, textureManagers );

        // determine fragments that can be released for the next frame
        Set<RenderMeshFragment> unloadFragments = new HashSet<RenderMeshFragment>( activeLOD );
        unloadFragments.removeAll( nextLOD );
        fragmentManager.release( unloadFragments, gl );

        activeLOD = nextLOD;
        try {
            fragmentManager.require( activeLOD );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private Map<RenderMeshFragment, List<FragmentTexture>> getTextures( ViewParams params,
                                                                        Set<RenderMeshFragment> fragments,
                                                                        TextureManager[] textureManagers ) {

        LOG.info( "Texturizing " + fragments.size() + " fragments, managers: " + textureManagers.length );

        // fetch textures in parallel threads (with timeout)
        List<Callable<Map<RenderMeshFragment, FragmentTexture>>> workers = new ArrayList<Callable<Map<RenderMeshFragment, FragmentTexture>>>(
                                                                                                                                              textureManagers.length );
        for ( TextureManager manager : textureManagers ) {
            workers.add( new TextureWorker( params, fragments, manager, (float) maxProjectedTexelSize ) );
        }
        Executor exec = Executor.getInstance();
        List<ExecutionFinishedEvent<Map<RenderMeshFragment, FragmentTexture>>> results = null;
        try {
            // TODO get timeout from configuration
            results = exec.performSynchronously( workers, (long) 30 * 1000 );
        } catch ( InterruptedException e ) {
            LOG.error( e.getMessage(), e );
        }

        // build result map
        Map<RenderMeshFragment, List<FragmentTexture>> meshFragmentToTexture = new HashMap<RenderMeshFragment, List<FragmentTexture>>();
        for ( ExecutionFinishedEvent<Map<RenderMeshFragment, FragmentTexture>> result : results ) {
            Map<RenderMeshFragment, FragmentTexture> fragmentToTexture;
            try {

                // retrieve worker result (may produce an exception)
                fragmentToTexture = result.getResult();

                for ( RenderMeshFragment fragment : fragments ) {
                    FragmentTexture texture = fragmentToTexture.get( fragment );
                    List<FragmentTexture> textures = meshFragmentToTexture.get( fragment );
                    if ( textures == null ) {
                        textures = new ArrayList<FragmentTexture>();
                        meshFragmentToTexture.put( fragment, textures );
                    }
                    textures.add( texture );
                }
            } catch ( CancellationException e ) {
                LOG.warn( "Timeout occured fetching textures." );
            } catch ( Throwable e ) {
                LOG.debug( e.getMessage(), e );
            }
        }
        return meshFragmentToTexture;
    }

    private void render( GL gl, Map<RenderMeshFragment, List<FragmentTexture>> fragmentToTextures,
                         TextureManager[] textureManagers, float zScale ) {

        long begin = System.currentTimeMillis();

        try {
            fragmentManager.requireOnGPU( activeLOD, gl );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // set z-scale
        if ( zScale != 1.0f ) {
            gl.glPushMatrix();
            gl.glScalef( 1.0f, 1.0f, zScale );
            // normalize normal vectors
            gl.glEnable( GL.GL_NORMALIZE );
        }

        for ( RenderMeshFragment fragment : activeLOD ) {
            List<FragmentTexture> textures = fragmentToTextures.get( fragment );
            if ( textures != null && textures.size() > 0 ) {
                int i = 0;
                for ( FragmentTexture texture : textures ) {
                    textureManagers[i++].enable( Collections.singletonList( texture ), gl );
                }
                fragment.render( gl, textures, shaderProgramIds[textures.size() - 1] );
            } else {
                fragment.render( gl, null, 0 );
            }
        }

        // reset z-scale
        if ( zScale != 1.0f ) {
            gl.glPopMatrix();
            gl.glDisable( GL.GL_NORMALIZE );
        }

        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Rendering of " + activeLOD.size() + ": " + elapsed + " milliseconds." );
    }

    private Set<RenderMeshFragment> getNewLOD( ViewParams params, float zScale, TextureManager[] textureManagers ) {

        ViewFrustum frustum = params.getViewFrustum();
        ViewFrustumCrit crit = new ViewFrustumCrit( params, (float) maxPixelError, zScale, maxTextureSize,
                                                    textureManagers, (float) maxProjectedTexelSize );
        SpatialSelection lodAdaptor = new SpatialSelection( fragmentManager.getMultiresolutionMesh(), crit, frustum,
                                                            zScale );

        List<MeshFragment> fragments = lodAdaptor.determineLODFragment();
        Set<RenderMeshFragment> fragmentIds = new HashSet<RenderMeshFragment>( fragments.size() );
        for ( MeshFragment fragment : fragments ) {
            fragmentIds.add( fragmentManager.renderFragments[fragment.id] );
        }

        return fragmentIds;
    }

    private void displayStats( GL gl, ViewParams vp ) {

        // calculate the number of drawn texels
        Set<TextureTile> usedTextures = new HashSet<TextureTile>();
        // for ( RenderableMeshFragment fragment : activeLOD ) {
        // MeshFragmentTexture fragmentTexture = fragment.getTexture();
        // if ( fragmentTexture != null ) {
        // usedTextures.add( fragmentTexture.getTextureTile() );
        // }
        // }
        long numTexels = 0;
        for ( TextureTile textureTile : usedTextures ) {
            numTexels += textureTile.getPixelsX() * textureTile.getPixelsY();
        }

        int x = vp.getScreenPixelsX() - 120;
        gl.glDisable( GL.GL_TEXTURE_2D );
        gl.glColor3f( 1.0f, 1.0f, 1.0f );
        gl.glWindowPos2d( x, 20 );
        // glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12, "auto adapt: " + ( autoAdapt ? "on" : "off" ) );
        gl.glWindowPos2d( x, 34 );
        glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12, "texels: " + numTexels / 1000000 + " M" );
        gl.glWindowPos2d( x, 48 );
        glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12, "texture tiles: " + usedTextures.size() );
        gl.glWindowPos2d( x, 62 );
        glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12, "mesh fragments: " + activeLOD.size() );
        gl.glWindowPos2d( x, 74 );
        // glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12, "texture error: " + textureMaxPixelError );
        // gl.glWindowPos2d( x, 88 );
        // glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12, "geometry error: " + geometryMaxPixelError );
        gl.glColor3f( 1.0f, 1.0f, 1.0f );
    }

    private class TextureWorker implements Callable<Map<RenderMeshFragment, FragmentTexture>> {

        private final ViewParams params;

        private final Set<RenderMeshFragment> fragments;

        private final TextureManager textureManager;

        private final float maxProjectedTexelSize;

        private TextureWorker( ViewParams params, Set<RenderMeshFragment> fragments, TextureManager textureManager,
                               float maxProjectedTexelSize ) {
            this.params = params;
            this.fragments = fragments;
            this.textureManager = textureManager;
            this.maxProjectedTexelSize = maxProjectedTexelSize;
        }

        @Override
        public Map<RenderMeshFragment, FragmentTexture> call()
                                throws Exception {
            return textureManager.getTextures( params, maxProjectedTexelSize, fragments );
        }
    }
}
