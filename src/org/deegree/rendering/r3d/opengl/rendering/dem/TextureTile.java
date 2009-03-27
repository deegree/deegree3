//$HeadURL$
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

package org.deegree.rendering.r3d.opengl.rendering.dem;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextureTile {

    private static final Logger LOG = LoggerFactory.getLogger( TextureTile.class );    
    
    private int[] textureID;

    private float minX, minY, maxX, maxY;

    private float metersPerPixel;

    private int pixelsX, pixelsY;

    private ByteBuffer imageData;
    
    private static int idsInUse = 0;
    
    // counts the number of references, if 0, data can be unloaded
    private int numReferences;

    public TextureTile( float minX, float minY, float maxX, float maxY, int pixelsX, int pixelsY, ByteBuffer imageData ) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        if ( maxX != minX ) {
            metersPerPixel = ( maxX - minX ) / pixelsX;
        } else {
            metersPerPixel = ( maxY - minY ) / pixelsY;
        }

        this.pixelsX = pixelsX;
        this.pixelsY = pixelsY;
        this.imageData = imageData;
    }

    public ByteBuffer getImageData() {
        return imageData;
    }

    public float getMinX() {
        return minX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public float getMetersPerPixel() {
        return metersPerPixel;
    }

    public int getPixelsX() {
        return pixelsX;
    }

    public int getPixelsY() {
        return pixelsY;
    }

    public int enable ( GL gl ) {
        loadToGPU( gl );
        return textureID[0];
    }

    private void loadToGPU( GL gl ) {
        if ( textureID == null ) {
            textureID = new int[1];
            idsInUse++;
            gl.glGenTextures( 1, textureID, 0 );
            gl.glBindTexture( GL.GL_TEXTURE_2D, textureID[0] );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
            imageData.rewind();
            gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGB, pixelsX, pixelsY, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE,
                             imageData );
        }
        numReferences++;
    }

    public void unloadFromGPU( GL gl ) {
        numReferences--;
        if (numReferences < 0) {
            throw new RuntimeException();
        }
        if (numReferences == 0 && textureID != null ) {
            LOG.info ("No more references. Deleting.");
            gl.glDeleteTextures( 1, textureID, 0 );
            textureID = null;
            idsInUse--;
        }
    }

    @Override
    public String toString() {
        String s = "{minX=" + minX + ",minY=" + minY + ",maxX=" + maxX + ",maxY=" + maxY + ",meters/pixel="
                   + metersPerPixel + "}";
        return s;
    }
}
