//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/texture/TexturePool.java $
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

package org.deegree.rendering.r3d.opengl.rendering.texture;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

/**
 * The <code>Textures</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 15512 $, $Date: 2009-01-06 12:12:13 +0100 (Di, 06 Jan 2009) $
 * 
 */
public class TexturePool {

    private final transient static Logger LOG = LoggerFactory.getLogger( TexturePool.class );

    private static Map<String, Texture> idToTexture = new HashMap<String, Texture>();

    private static Map<String, String> idToFile = new HashMap<String, String>();

    private static Map<String, Integer> idToUnit = new HashMap<String, Integer>();
    static {
        idToFile.put( "4",
                      System.getProperty("user.home") + "/workspace/bonn_3doptimierung/resources/data/example_data/Platanus_acerifolia_M.tif" );
        idToFile.put( "3", System.getProperty("user.home") +"/workspace/bonn_3doptimierung/resources/data/example_data/Pinus_nigra_M.tif" );
        idToFile.put( "2",
                      System.getProperty("user.home") +"/workspace/bonn_3doptimierung/resources/data/example_data/Magnolia_grandiflora_M.png" );

        idToFile.put( "1", System.getProperty("user.home") +"jogl.png" );

    }

    /**
     * @param context
     * @param texture
     */
    public static synchronized void loadTexture( GL context, String texture ) {

        Texture tex = getTexture( texture );
        if ( tex == null ) {
            LOG.warn( "No texture for id: " + texture );
            return;
        }
        tex.bind();
        // bindTextureToUnit( context, texture );
        // System.out.println( tex.getTarget() );
        IntBuffer t = BufferUtil.newIntBuffer( 1 );
        context.glGetIntegerv( GL.GL_MAX_TEXTURE_IMAGE_UNITS, t );
        // System.out.println( "Units: " + t.get( 0 ) );

    }

    /**
     * @param context
     * @param texture
     */
    // private static void bindTextureToUnit( GL context, String texture ) {
    // Integer texUnit = get( texture );
    // if ( texUnit == null ) {
    // // System.out.println( "Binding" );
    // texUnit = calcFreeTextureUnit( context, texture );
    // context.glActiveTexture( texUnit );
    // tex.bind();
    // } else {
    // System.out.println( texUnit );
    // context.glDisable( GL.GL_TEXTURE_2D );
    // context.glEnable( GL.GL_TEXTURE_2D );
    // context.glActiveTexture( toGL_Texture( texUnit ) );
    // // System.out.println( "not binding" );
    // }
    // }
    private synchronized static Texture getTexture( String id ) {
        Texture tex = idToTexture.get( id );
        if ( tex == null ) {
            tex = loadTextureFromFile( id );
        }
        return tex;
    }

    private static Integer calcFreeTextureUnit( GL context, String id ) {
        IntBuffer t = BufferUtil.newIntBuffer( 1 );
        context.glGetIntegerv( GL.GL_MAX_TEXTURE_IMAGE_UNITS, t );
        int k = t.get( 0 );
        //System.out.println( "k: " + k );
        int result = -1;
        int i = 0;
        //System.out.println( idToUnit.values() );
        for ( ; i < k && result == -1; i++ ) {
            if ( !idToUnit.containsValue( new Integer( i ) ) ) {
                result = toGL_Texture( i );
            }
        }
        //System.out.println( "result: " + result );
        //System.out.println( "i: " + i );
        if ( result == -1 ) {
            idToUnit.remove( idToUnit.get( 0 ) );
            result = toGL_Texture( 0 );
            i = 0;
        }
        idToUnit.put( id, i );
        return result;
    }

    private static int toGL_Texture( int i ) {
        switch ( i ) {
        case 1:
            return GL.GL_TEXTURE1;
        case 2:
            return GL.GL_TEXTURE2;
        case 3:
            return GL.GL_TEXTURE3;
        case 4:
            return GL.GL_TEXTURE4;
        case 5:
            return GL.GL_TEXTURE5;
        case 6:
            return GL.GL_TEXTURE6;
        case 7:
            return GL.GL_TEXTURE7;
        case 8:
            return GL.GL_TEXTURE8;
        case 9:
            return GL.GL_TEXTURE9;
        case 10:
            return GL.GL_TEXTURE10;
        case 11:
            return GL.GL_TEXTURE11;
        case 12:
            return GL.GL_TEXTURE12;
        case 13:
            return GL.GL_TEXTURE13;
        case 14:
            return GL.GL_TEXTURE14;
        case 15:
            return GL.GL_TEXTURE15;
        case 16:
            return GL.GL_TEXTURE16;
        case 17:
            return GL.GL_TEXTURE17;
        case 18:
            return GL.GL_TEXTURE18;
        case 19:
            return GL.GL_TEXTURE19;
        case 20:
            return GL.GL_TEXTURE20;
        case 21:
            return GL.GL_TEXTURE21;
        case 22:
            return GL.GL_TEXTURE22;
        case 23:
            return GL.GL_TEXTURE23;
        case 24:
            return GL.GL_TEXTURE24;
        case 25:
            return GL.GL_TEXTURE25;
        case 26:
            return GL.GL_TEXTURE26;
        case 27:
            return GL.GL_TEXTURE27;
        case 28:
            return GL.GL_TEXTURE28;
        case 29:
            return GL.GL_TEXTURE29;
        case 30:
            return GL.GL_TEXTURE30;
        case 31:
            return GL.GL_TEXTURE31;
        default:
            return GL.GL_TEXTURE0;
        }
    }

    /**
     * @param context
     * @param texture
     */
    public static void dispose( String texture ) {

        Texture tex = idToTexture.get( texture );
        if ( tex != null ) {
            tex.dispose();
        }
    }

    private static synchronized Texture loadTextureFromFile( String textureID ) {
        Texture result = null;
        String fileName = idToFile.get( textureID );
        if ( fileName != null && !"".equals( fileName.trim() ) ) {
            File f = new File( fileName );
            try {
                result = TextureIO.newTexture( f, true );
            } catch ( GLException e ) {
                LOG.error( "Error while trying to load texture with fileName:" + fileName + " cause: "
                           + e.getLocalizedMessage(), e );
            } catch ( IOException e ) {
                LOG.error( "Error while trying to load texture with fileName:" + fileName + " cause: "
                           + e.getLocalizedMessage(), e );
            }
            if ( result != null ) {
                idToTexture.put( textureID, result );
            }
        }
        return result;
    }

    /**
     * @param texture
     * @return
     */
    public static int getWidth( String texture ) {
        Texture tex = getTexture( texture );
        if ( tex != null ) {
            return tex.getWidth();
        }
        return 0;
    }
}
