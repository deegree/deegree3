//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.opengl.rendering.dem.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import javax.media.opengl.GL;

import org.deegree.commons.concurrent.ExecutionFinishedEvent;
import org.deegree.commons.concurrent.Executor;
import org.deegree.commons.utils.LogUtils;
import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.rendering.r3d.multiresolution.SpatialSelection;
import org.deegree.rendering.r3d.multiresolution.crit.ViewFrustumCrit;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.dem.Colormap;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.FragmentTexture;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.GLUT;

/**
 * The <code>TerrainRenderingManager</code> class manages the current fragments, it uses fragment shaders if multiple
 * texture are requested.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: $, $Date: $
 */
public class TerrainRenderingManager {

    private static final Logger LOG = LoggerFactory.getLogger( TerrainRenderingManager.class );

    private final GLUT glut = new GLUT();

    private static final long serialVersionUID = 1854116460506116944L;

    private RenderFragmentManager fragmentManager;

    // contains the mesh fragments that make up the current LOD
    private Set<RenderMeshFragment> activeLOD = new HashSet<RenderMeshFragment>();

    private double maxPixelError;

    private double maxProjectedTexelSize;

    private final float[] diffuseColor;

    private final float[] specularColor;

    private final float shininess;

    private final float[] ambientColor;

    /**
     * 
     * @param fragmentManager
     * @param maxPixelError
     * @param maxProjectedTexelSize
     * @param ambientColor
     *            of the terrain
     * @param diffuseColor
     *            of the terrain
     * @param specularColor
     *            of the terrain
     * @param shininess
     *            of the terrain.
     */
    public TerrainRenderingManager( RenderFragmentManager fragmentManager, double maxPixelError,
                                    double maxProjectedTexelSize, float[] ambientColor, float[] diffuseColor,
                                    float[] specularColor, float shininess ) {
        this.fragmentManager = fragmentManager;
        this.maxPixelError = maxPixelError;
        this.maxProjectedTexelSize = maxProjectedTexelSize;
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.shininess = shininess;
    }

    /**
     * Renders a view-optimized representation of the terrain geometry using the given scale, colormap and/or textures
     * to the specified GL context.
     * 
     * @param glRenderContext
     * @param disableElevationModel
     * @param colorMap
     *            to render
     * @param textureManagers
     *            to retrieve the textures from.
     */
    public void render( RenderContext glRenderContext, boolean disableElevationModel, Colormap colorMap,
                        TextureManager[] textureManagers ) {

        try {
            // ensure correct zScale (zScale = 0 does not work as expected)
            float zScale = ( disableElevationModel ) ? 0.001f : glRenderContext.getTerrainScale();

            if ( glRenderContext.updateLOD() ) {
                // adapt geometry LOD (fragments)
                updateLOD( glRenderContext, zScale, textureManagers );
            }

            GL gl = glRenderContext.getContext();
            setupLighting( gl );

            // set z-scale
            boolean useScale = Math.abs( zScale - 1 ) > 0.0001;
            if ( useScale ) {
                gl.glPushMatrix();
                gl.glScalef( 1.0f, 1.0f, zScale );
                // normalize normal vectors
                gl.glEnable( GL.GL_NORMALIZE );
            }

            long time = System.currentTimeMillis();
            loadDEMOnGPU( glRenderContext );

            if ( ( textureManagers == null || textureManagers.length == 0 ) && colorMap == null ) {
                render( gl );
            } else {
                if ( colorMap != null ) {
                    if ( textureManagers != null && textureManagers.length > 0 ) {
                        LOG.debug( "Color map rendering can not be used with other textures, ignoring the texture managers." );
                    }
                    render( glRenderContext, colorMap );
                } else {
                    // render fragments with textures
                    render( glRenderContext, textureManagers );
                }
            }
            if ( LOG.isDebugEnabled() ) {
                String msg = LogUtils.createDurationTimeString( "Rendering of " + activeLOD.size() + " fragments",
                                                                time, false );
                LOG.debug( msg );
            }

            // make sure all fixed shaders are used.
            gl.glUseProgram( 0 );

            // disable current array buffer.
            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, 0 );

            if ( LOG.isDebugEnabled() ) {
                displayStats( glRenderContext );
            }
            // pop the lighting
            gl.glPopAttrib();

            if ( useScale ) {
                // reset the scale.
                gl.glPopMatrix();
                // normalize normal vectors
                gl.glDisable( GL.GL_NORMALIZE );
            }
        } catch ( Throwable t ) {
            // LOG.error( "Rendering did not succeed stack tracke.", t );
            LOG.error( "Rendering did not succeed because: " + t.getLocalizedMessage() );
        }

    }

    /**
     * @param gl
     * 
     */
    private void setupLighting( GL gl ) {
        gl.glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT );
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT, ambientColor, 0 );
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_DIFFUSE, diffuseColor, 0 );
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_SPECULAR, specularColor, 0 );
        gl.glMaterialf( GL.GL_FRONT, GL.GL_SHININESS, shininess );
    }

    /**
     * @return the current lod
     */
    public Set<RenderMeshFragment> getCurrentLOD() {
        return activeLOD;
    }

    /**
     * @return the fragmentManager
     */
    public final RenderFragmentManager getFragmentManager() {
        return fragmentManager;
    }

    /**
     * Create a dem lod for the given render context.
     * 
     * @param glRenderContext
     * @param scale
     * @param textureManagers
     */
    private void updateLOD( RenderContext glRenderContext, float scale, TextureManager[] textureManagers ) {

        long begin = System.currentTimeMillis();

        Set<RenderMeshFragment> nextLOD = getNewLOD( glRenderContext, scale, textureManagers );

        // determine fragments that can be released for the next frame
        Set<RenderMeshFragment> unloadFragments = new HashSet<RenderMeshFragment>( activeLOD );
        unloadFragments.removeAll( nextLOD );
        fragmentManager.release( unloadFragments, glRenderContext.getContext() );

        activeLOD = nextLOD;
        try {
            fragmentManager.require( activeLOD );
        } catch ( IOException e ) {
            LOG.error( "Could not load all required DEM LOD fragments for the given render context stack.", e );
            LOG.error( "Could not load all required DEM LOD fragments for the given render context because: "
                       + e.getLocalizedMessage() );
        }
        LOG.debug( "Loading of " + activeLOD.size() + " fragments: " + ( System.currentTimeMillis() - begin )
                   + " milliseconds." );
    }

    /**
     * Get all textures from the texture managers by first making requests and then requesting the managers.
     * 
     * @param glRenderContext
     * @param fragments
     * @param textureManagers
     * @return the textures for each render fragment
     */
    private Map<RenderMeshFragment, List<FragmentTexture>> getTextures( RenderContext glRenderContext,
                                                                        Set<RenderMeshFragment> fragments,
                                                                        TextureManager[] textureManagers ) {

        long begin = System.currentTimeMillis();
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Texturizing " + fragments.size() + " fragments, managers: " + textureManagers.length );
            LOG.debug( " Requested texture managers: {}", Arrays.toString( textureManagers ) );
        }

        // fetch textures in parallel threads (with timeout)
        int maxRequestTime = 1;
        List<Callable<Map<RenderMeshFragment, FragmentTexture>>> workers = new ArrayList<Callable<Map<RenderMeshFragment, FragmentTexture>>>(
                                                                                                                                              textureManagers.length );
        for ( TextureManager manager : textureManagers ) {
            workers.add( new TextureWorker( glRenderContext, fragments, manager, (float) maxProjectedTexelSize ) );
            maxRequestTime = Math.max( maxRequestTime, manager.getRequestTimeout() );
        }
        Executor exec = Executor.getInstance();
        List<ExecutionFinishedEvent<Map<RenderMeshFragment, FragmentTexture>>> results = null;
        try {
            results = exec.performSynchronously( workers, (long) maxRequestTime * 1000 );
        } catch ( InterruptedException e ) {
            LOG.debug( "Could not fetch the textures, stack.", e );
            LOG.error( "Could not fetch the textures because: " + e.getMessage() );
        }
        Map<RenderMeshFragment, List<FragmentTexture>> meshFragmentToTexture = new HashMap<RenderMeshFragment, List<FragmentTexture>>();
        if ( results != null ) {
            // build result map
            for ( ExecutionFinishedEvent<Map<RenderMeshFragment, FragmentTexture>> result : results ) {
                Map<RenderMeshFragment, FragmentTexture> fragmentToTexture;
                try {

                    // retrieve worker result (may produce an exception)
                    fragmentToTexture = result.getResult();

                    for ( RenderMeshFragment fragment : fragments ) {
                        FragmentTexture texture = fragmentToTexture.get( fragment );
                        if ( texture != null ) {
                            List<FragmentTexture> textures = meshFragmentToTexture.get( fragment );
                            if ( textures == null ) {
                                textures = new ArrayList<FragmentTexture>();
                                meshFragmentToTexture.put( fragment, textures );
                            }
                            textures.add( texture );
                        }
                    }
                } catch ( CancellationException e ) {
                    LOG.warn( "Timeout occured fetching textures." );
                } catch ( Throwable e ) {
                    LOG.error( "Could not fetch the textures, stack.", e );
                    // LOG.error( "Could not fetch the textures because: " + e.getMessage() );
                }
            }
            LOG.debug( "Fetching of textures: " + ( System.currentTimeMillis() - begin ) + " milliseconds." );
        } else {
            LOG.warn( "No textures retrieved from the datasources." );
        }

        return meshFragmentToTexture;
    }

    /**
     * Load the dem fragment on the GPU, in other words, enable the VBO's, of the normals, vertices and indizes.
     * 
     * @param glRenderContext
     */
    private void loadDEMOnGPU( RenderContext glRenderContext ) {
        long begin = System.currentTimeMillis();
        try {
            fragmentManager.requireOnGPU( activeLOD, glRenderContext.getContext() );
        } catch ( IOException e ) {
            LOG.debug( "Could not load the fragments on the gpu, stack.", e );
            LOG.error( "Could not load the fragments on the gpu because: " + e.getMessage() );
        }
        LOG.debug( "GPU upload of " + activeLOD.size() + " fragments: " + ( System.currentTimeMillis() - begin )
                   + " milliseconds." );

    }

    /**
     * Simply render all mesh fragments without any coloring or texturing.
     * 
     * @param gl
     */
    private void render( GL gl ) {
        for ( RenderMeshFragment fragment : activeLOD ) {
            fragment.render( gl );
        }
    }

    /**
     * Render the fragments with a colormap
     * 
     * @param glRenderContext
     * @param colorMap
     */
    private void render( RenderContext glRenderContext, Colormap colorMap ) {
        colorMap.enable( glRenderContext );
        render( glRenderContext.getContext() );
        colorMap.disable( glRenderContext );
    }

    /**
     * Render the meshfragments with textures.
     * 
     * @param glRenderContext
     * @param textureManagers
     */
    private void render( RenderContext glRenderContext, TextureManager[] textureManagers ) {
        // determine textures for each fragment
        Map<RenderMeshFragment, List<FragmentTexture>> fragmentToTextures = getTextures( glRenderContext, activeLOD,
                                                                                         textureManagers );
        GL gl = glRenderContext.getContext();
        for ( RenderMeshFragment fragment : activeLOD ) {
            List<FragmentTexture> textures = fragmentToTextures.get( fragment );
            if ( textures != null && textures.size() > 0 ) {
                int numberOfTextures = 0;
                for ( FragmentTexture texture : textures ) {
                    if ( texture != null ) {
                        textureManagers[numberOfTextures++].enable( Collections.singletonList( texture ), gl );
                    }
                }
                if ( numberOfTextures == 0 ) {
                    fragment.render( gl );
                } else {
                    fragment.render( gl, textures,
                                     glRenderContext.getCompositingTextureShaderProgram( numberOfTextures ) );
                }
            } else {
                fragment.render( gl );
            }
        }

        // rb: clean up the textures etc.
        if ( textureManagers.length > 0 ) {
            for ( TextureManager manager : textureManagers ) {
                if ( manager != null ) {
                    manager.cleanUp( gl );
                }
            }
        }

        for ( RenderMeshFragment fragment : activeLOD ) {
            List<FragmentTexture> textures = fragmentToTextures.get( fragment );
            if ( textures != null && textures.size() > 0 ) {
                for ( FragmentTexture texture : textures ) {
                    if ( texture != null ) {
                        if ( !texture.cachingEnabled() ) {
                            texture.clearAll( gl );
                        }
                    }
                }
            }
        }
    }

    /**
     * The new LOD is based on the current viewfrustum and the maximum size of any textures.
     * 
     * @param glRenderContext
     * @param zScale
     * @param textureManagers
     * @return
     */
    private Set<RenderMeshFragment> getNewLOD( RenderContext glRenderContext, float zScale,
                                               TextureManager[] textureManagers ) {

        ViewParams params = glRenderContext.getViewParams();
        ViewFrustum frustum = params.getViewFrustum();
        ViewFrustumCrit crit = new ViewFrustumCrit( params, (float) maxPixelError, zScale,
                                                    glRenderContext.getMaxTextureSize(), textureManagers,
                                                    (float) maxProjectedTexelSize );
        SpatialSelection lodAdaptor = new SpatialSelection( fragmentManager.getMultiresolutionMesh(), crit, frustum,
                                                            zScale );

        List<MeshFragment> fragments = lodAdaptor.determineLODFragment();
        Set<RenderMeshFragment> fragmentIds = new HashSet<RenderMeshFragment>( fragments.size() );
        for ( MeshFragment fragment : fragments ) {
            fragmentIds.add( fragmentManager.renderFragments[fragment.id] );
        }

        return fragmentIds;
    }

    private void displayStats( RenderContext glRenderContext ) {

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
            numTexels += textureTile.getWidth() * textureTile.getHeight();
        }

        ViewParams vp = glRenderContext.getViewParams();
        GL gl = glRenderContext.getContext();

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

    /**
     * Simple callable for retrieving the textures from the managers.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: $, $Date: $
     * 
     */
    private class TextureWorker implements Callable<Map<RenderMeshFragment, FragmentTexture>> {

        private final RenderContext glRenderContext;

        private final Set<RenderMeshFragment> fragments;

        private final TextureManager textureManager;

        private final float maxProjTexelSize;

        TextureWorker( RenderContext glRenderContext, Set<RenderMeshFragment> fragments, TextureManager textureManager,
                       float maxProjectedTexelSize ) {
            this.glRenderContext = glRenderContext;
            this.fragments = fragments;
            this.textureManager = textureManager;
            this.maxProjTexelSize = maxProjectedTexelSize;
        }

        @Override
        public Map<RenderMeshFragment, FragmentTexture> call()
                                throws Exception {
            return textureManager.getTextures( glRenderContext, maxProjTexelSize, fragments );
        }
    }
}
