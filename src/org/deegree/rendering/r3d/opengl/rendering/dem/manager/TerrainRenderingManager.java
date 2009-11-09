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

import org.deegree.commons.concurrent.ExecutionFinishedEvent;
import org.deegree.commons.concurrent.Executor;
import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.rendering.r3d.multiresolution.SpatialSelection;
import org.deegree.rendering.r3d.multiresolution.crit.ViewFrustumCrit;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.FragmentTexture;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.GLUT;

/**
 * 
 * The <code>TerrainRenderingManager</code> class manages the current fragments, it uses fragment shaders if multiple
 * texture are requested.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: $, $Date: $
 * 
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

    /**
     * 
     * @param fragmentManager
     * @param maxPixelError
     * @param maxProjectedTexelSize
     */
    public TerrainRenderingManager( RenderFragmentManager fragmentManager, double maxPixelError,
                                    double maxProjectedTexelSize ) {
        this.fragmentManager = fragmentManager;
        this.maxPixelError = maxPixelError;
        this.maxProjectedTexelSize = maxProjectedTexelSize;
    }

    /**
     * Renders a view-optimized representation of the terrain geometry using the given scale and textures to the
     * specified GL context.
     * 
     * @param glRenderContext
     * @param disableElevationModel
     * @param textureManagers
     */
    public void render( RenderContext glRenderContext, boolean disableElevationModel, TextureManager[] textureManagers ) {

        // ensure correct zScale (zScale = 0 does not work as expected)
        float zScale = ( disableElevationModel ) ? 0.001f : glRenderContext.getTerrainScale();

        // adapt geometry LOD (fragments)
        updateLOD( glRenderContext, zScale, textureManagers );

        // determine textures for each fragment
        Map<RenderMeshFragment, List<FragmentTexture>> fragmentToTextures = getTextures( glRenderContext, activeLOD,
                                                                                         textureManagers );

        glRenderContext.getContext().glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT );
        // glRenderContext.getContext().glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT, new float[] { 0.8f, 0.8f, 0.8f, 1 }, 0
        // );
        glRenderContext.getContext().glMaterialfv( GL.GL_FRONT, GL.GL_DIFFUSE, new float[] { 0.8f, 0.8f, 0.8f, 1 }, 0 );
        glRenderContext.getContext().glMaterialfv( GL.GL_FRONT, GL.GL_SPECULAR, new float[] { 0.02f, 0.02f, 0.02f, 1 },
                                                   0 );
        glRenderContext.getContext().glMaterialf( GL.GL_FRONT, GL.GL_SHININESS, 1.5f );
        // render fragments with textures
        render( glRenderContext, fragmentToTextures, textureManagers, zScale );
        if ( LOG.isDebugEnabled() ) {
            displayStats( glRenderContext );
        }
        glRenderContext.getContext().glPopAttrib();
    }

    /**
     * @return the current lod
     */
    public Collection<RenderMeshFragment> getCurrentLOD() {
        return activeLOD;
    }

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
            e.printStackTrace();
        }
        LOG.debug( "Loading of " + activeLOD.size() + " fragments: " + ( System.currentTimeMillis() - begin )
                   + " milliseconds." );
    }

    private Map<RenderMeshFragment, List<FragmentTexture>> getTextures( RenderContext glRenderContext,
                                                                        Set<RenderMeshFragment> fragments,
                                                                        TextureManager[] textureManagers ) {

        long begin = System.currentTimeMillis();
        LOG.debug( "Texturizing " + fragments.size() + " fragments, managers: " + textureManagers.length );

        // fetch textures in parallel threads (with timeout)
        List<Callable<Map<RenderMeshFragment, FragmentTexture>>> workers = new ArrayList<Callable<Map<RenderMeshFragment, FragmentTexture>>>(
                                                                                                                                              textureManagers.length );
        for ( TextureManager manager : textureManagers ) {
            workers.add( new TextureWorker( glRenderContext, fragments, manager, (float) maxProjectedTexelSize ) );
        }
        Executor exec = Executor.getInstance();
        List<ExecutionFinishedEvent<Map<RenderMeshFragment, FragmentTexture>>> results = null;
        try {
            // TODO get timeout from configuration
            results = exec.performSynchronously( workers, (long) 30 * 1000 );
        } catch ( InterruptedException e ) {
            LOG.error( e.getMessage(), e );
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
            LOG.debug( "Fetching of textures: " + ( System.currentTimeMillis() - begin ) + " milliseconds." );
        } else {
            LOG.warn( "No textures retrieved from the datasources." );
        }

        return meshFragmentToTexture;
    }

    private void render( RenderContext glRenderContext,
                         Map<RenderMeshFragment, List<FragmentTexture>> fragmentToTextures,
                         TextureManager[] textureManagers, float zScale ) {

        long begin = System.currentTimeMillis();
        GL gl = glRenderContext.getContext();
        try {
            fragmentManager.requireOnGPU( activeLOD, gl );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        LOG.debug( "GPU upload of " + activeLOD.size() + " fragments: " + ( System.currentTimeMillis() - begin )
                   + " milliseconds." );

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
                fragment.render( gl, textures, glRenderContext.getShaderProgramIds()[textures.size() - 1] );
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
        LOG.debug( "Rendering of " + activeLOD.size() + " fragments took: " + elapsed + " ms." );
    }

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
            numTexels += textureTile.getPixelsX() * textureTile.getPixelsY();
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
     * @return the fragmentManager
     */
    public final RenderFragmentManager getFragmentManager() {
        return fragmentManager;
    }

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
