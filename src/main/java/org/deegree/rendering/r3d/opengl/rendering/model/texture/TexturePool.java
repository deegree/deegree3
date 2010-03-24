//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/texture/TexturePool.java $
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

package org.deegree.rendering.r3d.opengl.rendering.model.texture;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GLException;

import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

/**
 * The <code>TexturePool</code> holds static references to texture files, used in the scene.
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

    /**
     * Add all files from the given directory to the pool, use the file name as the key.
     *
     * @param textureDir
     *            to scan for files.
     */
    public static synchronized void addTexturesFromDirectory( File textureDir ) {
        if ( textureDir.exists() ) {
            if ( textureDir.isDirectory() ) {
                File[] files = textureDir.listFiles();
                if ( files != null ) {
                    for ( File f : files ) {
                        if ( f != null ) {
                            String key = f.getName();
                            idToFile.put( key, f.getAbsolutePath() );
                            // loadTextureFromFile( key );
                        }
                    }
                }
            }
        }
    }

    /**
     * @param f
     *            file to be added to the pool
     * @return the key for the file.
     */
    public static synchronized String addTexture( File f ) {
        String result = null;
        if ( f != null && f.exists() && !f.isDirectory() ) {
            result = f.getName();
            idToFile.put( result, f.getAbsolutePath() );
        }
        return result;
    }

    /**
     * Add the given file with the given key to the map.
     *
     * @param key
     *            to be used.
     * @param textureFile
     *            to be added.
     */
    public static synchronized void addTexture( String key, File textureFile ) {
        if ( key != null && !"".equals( key ) && textureFile.exists() && !textureFile.isDirectory() ) {
            if ( idToFile.containsKey( key ) ) {
                LOG.warn( "Ignoring texture key: " + key
                          + " because it is already present in the texture pool with file: " + idToFile.get( key ) );
            } else {
                idToFile.put( key, textureFile.getAbsolutePath() );
            }
        } else {
            LOG.warn( "Ignoring texture key: " + key
                      + " because it is null, empty or the file does not exist or is a directory." );
        }
    }

    /**
     * @param glRenderContext
     * @param texture
     * @return the texture mapped to the given string or <code>null</code> if no texture with that id was found.
     */
    public static synchronized Texture getTexture( RenderContext glRenderContext, String texture ) {
        return getTexture( texture );
    }

    /**
     * @param glRenderContext
     * @param texture
     */
    public static synchronized void loadTexture( RenderContext glRenderContext, String texture ) {

        Texture tex = getTexture( glRenderContext, texture );
        if ( tex == null ) {
            LOG.warn( "No texture for id: " + texture );
            return;
        }
        tex.bind();
    }

    private synchronized static Texture getTexture( String id ) {
        Texture tex = idToTexture.get( id );
        if ( tex == null ) {
            tex = loadTextureFromFile( id );
        }
        return tex;
    }

    /**
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
     * @return the width of the referenced texture of 0 if the texture was not found.
     */
    public static int getWidth( String texture ) {
        Texture tex = getTexture( texture );
        if ( tex != null ) {
            return tex.getWidth();
        }
        return 0;
    }

}
