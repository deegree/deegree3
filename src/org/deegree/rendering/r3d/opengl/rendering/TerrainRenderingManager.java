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

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.swing.JFrame;

import org.deegree.commons.utils.JOGLUtils;
import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.rendering.r3d.multiresolution.SpatialSelection;
import org.deegree.rendering.r3d.multiresolution.crit.ViewFrustumCrit;
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

    private float textureMaxPixelError = 1.0f;

    private float geometryMaxPixelError = 5.0f;

    private RenderFragmentManager fragmentManager;

    // contains the mesh fragments that make up the current LOD
    private Set<RenderMeshFragment> activeLOD = new HashSet<RenderMeshFragment>();

    private boolean showStructure;

    private LODAnalyzer analyzer;

    private JFrame analyzerFrame;

    private long numTexels = 0;

    public TerrainRenderingManager( RenderFragmentManager fragmentManager ) {
        this.fragmentManager = fragmentManager;
    }

    /**
     * Renders a view-optimized representation of the terrain geometry using the given scale and textures to the
     * specified GL context.
     * 
     * @param gl
     * @param params
     * @param disableElevationModel
     * @param zScale
     *            scale factor for terrain height values
     * @param textureManagers
     */
    public void render( GL gl, ViewParams params, boolean disableElevationModel, float zScale,
                        TextureManager[] textureManagers ) {

        System.out.println( "num textures: " + textureManagers.length );

        if ( disableElevationModel || zScale < 0.001f ) {
            // ensure correct zScale (zScale = 0 does not work as expected)
            zScale = 0.001f;
        }

        // adapt geometry LOD (fragments)
        updateLOD( gl, params, zScale );

        // determine textures for each fragment
        Map<RenderMeshFragment, List<FragmentTexture>> fragmentToTextures = getTextures( params, activeLOD,
                                                                                         textureManagers, zScale );

        // render fragments with textures
        render( gl, fragmentToTextures, textureManagers, zScale );

        displayStats( gl, params );
    }

    private void updateLOD( GL gl, ViewParams params, float scale ) {

        Set<RenderMeshFragment> nextLOD = getNewLOD( params, scale );

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

        if ( showStructure ) {
            if ( analyzerFrame == null ) {
                analyzer = new LODAnalyzer();
                analyzerFrame = new JFrame( "LOD structure" );
                analyzerFrame.getContentPane().add( analyzer, BorderLayout.CENTER );
                analyzerFrame.setDefaultCloseOperation( javax.swing.WindowConstants.HIDE_ON_CLOSE );
                analyzerFrame.setSize( 200, 200 );
                analyzerFrame.setLocationByPlatform( true );
            }
            if ( !analyzerFrame.isVisible() ) {
                analyzerFrame.setVisible( true );
            }

            analyzer.updateParameters( activeLOD, params.getViewFrustum() );
            analyzer.repaint();
        }
    }

    private Map<RenderMeshFragment, List<FragmentTexture>> getTextures( ViewParams params,
                                                                        Set<RenderMeshFragment> fragments,
                                                                        TextureManager[] textureManagers, float zScale ) {

        LOG.info( "Texturizing " + fragments.size() + " fragments, managers: " + textureManagers.length );
        Map<RenderMeshFragment, List<FragmentTexture>> meshFragmentToTexture = new HashMap<RenderMeshFragment, List<FragmentTexture>>();
        for ( TextureManager manager : textureManagers ) {
            Map<RenderMeshFragment, FragmentTexture> fragmentToTexture = manager.getTextures( params, fragments, zScale );
            for ( RenderMeshFragment fragment : fragmentToTexture.keySet() ) {
                FragmentTexture texture = fragmentToTexture.get( fragment );
                List<FragmentTexture> textures = meshFragmentToTexture.get( fragment );
                if ( textures == null ) {
                    textures = new ArrayList<FragmentTexture>();
                    meshFragmentToTexture.put( fragment, textures );
                }
                textures.add( texture );
            }
        }
        return meshFragmentToTexture;
    }

    private void render( GL gl, Map<RenderMeshFragment, List<FragmentTexture>> fragmentToTextures,
                         TextureManager[] textureManagers, float zScale ) {

        long begin = System.currentTimeMillis();

//        // enable the needed number of texture units
//        int maxTextureCount = 0;
//        for ( List<FragmentTexture> textures : fragmentToTextures.values() ) {
//            if ( textures.size() > maxTextureCount ) {
//                maxTextureCount = textures.size();
//            }
//        }
//        LOG.info ("Enabling " + maxTextureCount + " texture units.");
//        for ( int i = 0; i < maxTextureCount; i++ ) {
//            int glTextureId = JOGLUtils.getTextureUnitConst( i );
//            gl.glEnable( glTextureId );
//            gl.glClientActiveTexture( glTextureId );
//            gl.glActiveTexture( glTextureId );
//            gl.glEnable( GL.GL_TEXTURE_2D );
//            gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
//        }

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
            if ( textures != null ) {
                int i = 0;
                for ( FragmentTexture texture : textures ) {
                    textureManagers[i++].enable( Collections.singletonList( texture ), gl );
                }
                fragment.render( gl, textures );
            } else {
                fragment.render( gl, null );
            }
        }

        // reset z-scale
        if ( zScale != 1.0f ) {
            gl.glPopMatrix();
            gl.glDisable( GL.GL_NORMALIZE );
        }

//        // disable all activated texture units
//        for ( int i = 0; i < maxTextureCount; i++ ) {
//            int glTextureId = JOGLUtils.getTextureUnitConst( i );
//            gl.glActiveTexture( glTextureId );
//            gl.glDisable( GL.GL_TEXTURE_2D );
//            gl.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
//            gl.glDisable( glTextureId );
//        }

        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Rendering of " + activeLOD.size() + ": " + elapsed + " milliseconds." );
    }

    private Set<RenderMeshFragment> getNewLOD( ViewParams params, float zScale ) {

        ViewFrustum frustum = params.getViewFrustum();
        ViewFrustumCrit crit = new ViewFrustumCrit( params, geometryMaxPixelError );
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
        glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12, "texture error: " + textureMaxPixelError );
        gl.glWindowPos2d( x, 88 );
        glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12, "geometry error: " + geometryMaxPixelError );
        gl.glColor3f( 1.0f, 1.0f, 1.0f );
    }
}
