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

package org.deegree.rendering.r3d.opengl.rendering.model.geometry;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.vecmath.Point3d;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.PositionableModel;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.BufferUtil;

/**
 * The <code>BillBoard</code> class represents a billboard an object always facing the viewer, with the z-axis as it's
 * rotation axis.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class BillBoard extends RenderableQualityModel implements PositionableModel {

    /**
     *
     */
    private static final long serialVersionUID = -2746400840307665734L;

    private final static Logger LOG = LoggerFactory.getLogger( BillBoard.class );

    private final static float[] NORMAL = new float[] { 0, -1, 0 };

    private transient float[] location;

    private transient String textureID;

    private transient float width;

    private transient float height;

    private transient float error;

    private static final float DTR = 57.29577951308232087684f;

    private static final FloatBuffer coordBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( new float[] {
                                                                                                              -.5f,
                                                                                                              0,
                                                                                                              0, // ll
                                                                                                              .5f,
                                                                                                              0,
                                                                                                              0,// lr
                                                                                                              .5f,
                                                                                                              0,
                                                                                                              1,// ur
                                                                                                              -.5f, 0,
                                                                                                              1 }// ul
    ) );

    private static final FloatBuffer textureBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( new float[] {
                                                                                                                0.001f,
                                                                                                                0.999f,
                                                                                                                0.999f,
                                                                                                                0.999f,
                                                                                                                0.999f,
                                                                                                                0.001f,
                                                                                                                0.001f,
                                                                                                                0.001f } ) );

    private transient float[] bbox;

    /**
     * Constructs a billboard data structure with the given texture id.
     * 
     * @param texture
     * @param location
     *            of the billboard
     * @param width
     *            of this billboard
     * @param height
     *            of this billboard
     */
    public BillBoard( String texture, float[] location, float width, float height ) {
        super();
        this.location = location;
        if ( location == null ) {
            this.location = new float[] { 0, 0, 0 };
        }
        this.width = width;
        this.height = height;

        this.error = (float) Math.sqrt( this.width * this.width + this.height * this.height );

        this.textureID = texture;
    }

    @Override
    public void render( RenderContext glRenderContext ) {
        Point3d eye = glRenderContext.getViewParams().getViewFrustum().getEyePos();
        GL context = glRenderContext.getContext();
        context.glPushMatrix();
        context.glDepthMask( false );
        context.glEnable( GL.GL_TEXTURE_2D );
        context.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
        // the translation
        context.glTranslatef( location[0], location[1], location[2] );
        // the rotation
        calculateAndSetRotation( context, new float[] { (float) eye.x, (float) eye.y, (float) eye.z } );

        context.glScalef( width, 1, height );

        TexturePool.loadTexture( glRenderContext, textureID );

        // context.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, new float[] { 1, 1, 1, .1f }, 0 );
        context.glVertexPointer( 3, GL.GL_FLOAT, 0, coordBuffer );

        context.glTexCoordPointer( 2, GL.GL_FLOAT, 0, textureBuffer );
        // context.glDisableClientState( GL.GL_NORMAL_ARRAY );

        context.glDrawArrays( GL.GL_QUADS, 0, 4 );

        context.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
        context.glDisable( GL.GL_TEXTURE_2D );
        context.glDepthMask( true );
        context.glMatrixMode( GL.GL_MODELVIEW );
        context.glPopMatrix();

    }

    /**
     * Normalize the viewVector, the inner product (dot) between billboard normal and viewVector will allow the
     * computation of the cosine of the angle. However knowing the cosine alone is not enough, since the cos(a) =
     * cos(-a). Computing the cross product as well (the new up-vector) allows us to uniquely determine the angle. The
     * cross product vector will have the same direction as the up vector if the angle is positive. For negative angles
     * the up vector's direction will opposed to the up vector, effectively reversing the rotation. from
     * http://www.lighthouse3d.com/opengl/billboarding/index.php?billCyl
     * 
     * @param context
     *            to set the translation to
     * @param eye
     *            the position of the camera in world coordinates
     */
    private void calculateAndSetRotation( GL context, float[] eye ) {

        float[] viewVector = Vectors3f.sub( eye, location );
        // projection to the xy plane.
        viewVector[2] = 0;
        Vectors3f.normalizeInPlace( viewVector );
        double angleCosine = Vectors3f.dot( viewVector, NORMAL );
        // only do a rotation the angles are between -1 and 1.
        if ( ( angleCosine > 0.999999 ) || ( angleCosine < -0.999999 ) ) {
            angleCosine = ( angleCosine < 0 ) ? -1 : 1;
        }
        // negative or positive orientation?
        float[] newUp = Vectors3f.cross( NORMAL, viewVector );
        // context.glNormal3fv( NORMAL, 0 );
        context.glRotatef( DTR * (float) Math.acos( angleCosine ), newUp[0], newUp[1], newUp[2] );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        if ( location != null && location.length > 0 ) {
            sb.append( "\nlocation: " ).append( Vectors3f.asString( location ) );
        }
        sb.append( "\nwidth: " ).append( width );
        sb.append( "\nheight: " ).append( height );
        return sb.toString();
    }

    /**
     * Method called while serializing this object
     * 
     * @param out
     *            to write to.
     * @throws IOException
     */
    private void writeObject( java.io.ObjectOutputStream out )
                            throws IOException {
        LOG.trace( "Serializing to object stream" );
        out.writeObject( location );
        out.writeFloat( width );
        out.writeFloat( height );
        out.writeUTF( textureID );
    }

    /**
     * Method called while de-serializing (instancing) this object.
     * 
     * @param in
     *            to create the methods from.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject( java.io.ObjectInputStream in )
                            throws IOException, ClassNotFoundException {
        LOG.trace( "Deserializing from object stream" );
        location = (float[]) in.readObject();
        width = in.readFloat();
        height = in.readFloat();
        textureID = in.readUTF();
        this.error = (float) Math.sqrt( this.width * this.width + this.height * this.height );
    }

    /**
     * @return the bytes this geometry occupies
     */
    @Override
    public long sizeOf() {
        long localSize = super.sizeOf();
        localSize += AllocatedHeapMemory.sizeOfFloatArray( location, true );
        localSize += AllocatedHeapMemory.FLOAT_SIZE;
        localSize += AllocatedHeapMemory.FLOAT_SIZE;
        localSize += AllocatedHeapMemory.sizeOfString( textureID, true, true );
        return localSize;
    }

    @Override
    public boolean equals( Object obj ) {
        boolean result = false;
        if ( obj != null && obj instanceof BillBoard ) {

            BillBoard that = (BillBoard) obj;
            result = super.equals( that ) && this.textureID.equals( that.textureID )
                     && Vectors3f.equals( this.location, that.location, 1E-11f );
            if ( result ) {
                result = Math.abs( this.width - that.width ) < 1E-11f && Math.abs( this.height - that.height ) < 1E-11f;
            }
        }
        return result;
    }

    /**
     * @return the id of it's texture
     */
    public final String getTextureID() {
        return textureID;
    }

    /**
     * 
     * @return the location of this billboard (it's center axis)
     */
    public final float[] getLocation() {
        return location;
    }

    @Override
    public float[] getPosition() {
        return location;
    }

    /**
     * @return the width
     */
    public final float getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public final float getHeight() {
        return height;
    }

    /**
     * @param context
     * @param eye
     */
    public void renderPrepared( GL context, float[] eye ) {
        // the translation
        context.glTranslatef( location[0], location[1], location[2] );
        // the rotation
        calculateAndSetRotation( context, eye );
        context.glScalef( width, 1, height );
        context.glDrawArrays( GL.GL_QUADS, 0, 4 );
    }

    @Override
    public float getErrorScalar() {
        return error;
    }

    @Override
    public float getObjectHeight() {
        return location[2] + height;
    }

    @Override
    public float getGroundLevel() {
        return location[2];
    }

    @Override
    public float[] getModelBBox() {
        if ( bbox == null ) {
            bbox = new float[6];
            float half = width * 0.5f;
            bbox[0] = location[0] - half;
            bbox[1] = location[1] - half;
            bbox[2] = location[2];

            bbox[3] = location[0] + ( half );
            bbox[4] = location[1] + half;
            bbox[5] = location[2] + height;
        }
        return bbox;
    }
}
