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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

public class TerrainRenderingManager implements KeyListener {

    private static final Logger LOG = LoggerFactory.getLogger( TerrainRenderingManager.class );

    private final GLUT glut = new GLUT();

    private static final long serialVersionUID = 1854116460506116944L;

    private float textureMaxPixelError = 1.0f;

    private float geometryMaxPixelError = 5.0f;

    private RenderFragmentManager fragmentManager;

    // contains the mesh fragments that make up the current LOD
    private Set<RenderMeshFragment> activeLOD = new HashSet<RenderMeshFragment>();

    private float scale = 1.0f;

    private boolean autoAdapt = true;

    private boolean texturize;

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
     * @param scale
     * @param textureManagers
     */
    public void render( GL gl, ViewParams params, float scale, TextureManager[] textureManagers ) {

        if ( autoAdapt ) {
            updateLOD( gl, params );
        }

        Map<RenderMeshFragment, List<FragmentTexture>> fragmentToTextures = Collections.EMPTY_MAP;
        if ( texturize ) {
            fragmentToTextures = getTextures( params, activeLOD, textureManagers );
        }

        render( gl, fragmentToTextures, textureManagers );

        displayStats( gl, params );
    }

    private void updateLOD( GL gl, ViewParams params ) {

        Set<RenderMeshFragment> nextLOD = getNewLOD( params );

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
                                                                        TextureManager[] textureManagers ) {

        LOG.info( "Texturizing " + fragments.size() + " fragments, managers: " + textureManagers.length );
        Map<RenderMeshFragment, List<FragmentTexture>> meshFragmentToTexture = new HashMap<RenderMeshFragment, List<FragmentTexture>>();
        for ( TextureManager manager : textureManagers ) {

            Map<RenderMeshFragment, FragmentTexture> fragmentToTexture = manager.getTextures( params, fragments );
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
                         TextureManager[] textureManagers ) {

        long begin = System.currentTimeMillis();

        try {
            fragmentManager.requireOnGPU( activeLOD, gl );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // set z-scale
        gl.glPushMatrix();
        gl.glScalef( 1.0f, 1.0f, scale );

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
        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, 0 );
        gl.glFinish();

        // reset z-scale
        gl.glPopMatrix();

        long elapsed = System.currentTimeMillis() - begin;
        LOG.info( "Rendering of " + activeLOD.size() + ": " + elapsed + " milliseconds." );
    }

    private Set<RenderMeshFragment> getNewLOD( ViewParams params ) {

        ViewFrustum frustum = params.getViewFrustum();
        ViewFrustumCrit crit = new ViewFrustumCrit( params, geometryMaxPixelError );
        SpatialSelection lodAdaptor = new SpatialSelection( fragmentManager.getMultiresolutionMesh(), crit, frustum );

        List<MeshFragment> fragments = lodAdaptor.determineLODFragment();
        Set<RenderMeshFragment> fragmentIds = new HashSet<RenderMeshFragment>( fragments.size() );
        for ( MeshFragment fragment : fragments ) {
            fragmentIds.add( fragmentManager.renderFragments[fragment.id] );
        }

        return fragmentIds;
    }

    /**
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed( KeyEvent ev ) {

        int k = ev.getKeyCode();
        ev.getModifiers();

        switch ( k ) {
        case KeyEvent.VK_F9: {
            texturize = !texturize;
            break;
        }
        case KeyEvent.VK_F10: {
            autoAdapt = !autoAdapt;
            break;
        }
        case KeyEvent.VK_F11: {
            showStructure = !showStructure;
            break;
        }
            // case KeyEvent.VK_F11: {
            // asyncFetching = asyncFetching;
            // break;
            // }
        case KeyEvent.VK_7: {
            geometryMaxPixelError -= 1;
            if ( geometryMaxPixelError <= 1 ) {
                geometryMaxPixelError = 1;
            }
            geometryMaxPixelError += 1;
            break;
        }
        case KeyEvent.VK_9: {
            textureMaxPixelError -= 1;
            if ( textureMaxPixelError <= 1 ) {
                textureMaxPixelError = 1;
            }
            break;
        }
        case KeyEvent.VK_0: {
            textureMaxPixelError += 1;
            break;
        }
        case KeyEvent.VK_PAGE_DOWN: {
            scale /= 1.01f;
            break;
        }
        case KeyEvent.VK_PAGE_UP: {
            scale *= 1.01f;
            break;
        }
        }
    }

    /**
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped( KeyEvent e ) {
        // nothing to do
    }

    /**
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased( KeyEvent e ) {
        // nothing to do
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
        glut.glutBitmapString( GLUT.BITMAP_HELVETICA_12, "auto adapt: " + ( autoAdapt ? "on" : "off" ) );
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
