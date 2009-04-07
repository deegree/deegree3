//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/prototypes/PrototypeReference.java $
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

package org.deegree.rendering.r3d.opengl.rendering.prototype;

import java.io.IOException;
import java.io.Serializable;

import org.deegree.commons.utils.AllocatedHeapMemory;
import org.deegree.rendering.r3d.opengl.math.GLTransformationMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>PrototypeData</code> saves information of a prototype, and the transformation matrix to apply before
 * rendering the prototype.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 15512 $, $Date: 2009-01-06 12:12:13 +0100 (Di, 06 Jan 2009) $
 * 
 */
public class PrototypeReference implements Serializable {

    private final static Logger LOG = LoggerFactory.getLogger( PrototypeReference.class );

    /**
     * 
     */
    private static final long serialVersionUID = 417932269664979569L;

    private transient String prototypeID;

    private transient GLTransformationMatrix gLTransformationMatrix;

    private transient float angle;

    private transient float width;

    private transient float height;

    private transient float depth;

    private float[] location;

    /**
     * Construct a reference to a prototype, by supplying an id and a transformation matrix
     * 
     * @param prototypeID
     * @param gLTransformationMatrix
     */
    public PrototypeReference( String prototypeID, GLTransformationMatrix gLTransformationMatrix ) {
        this.prototypeID = prototypeID;
        this.gLTransformationMatrix = gLTransformationMatrix;
    }

    /**
     * Construct a reference to a prototype, by supplying an id and the transformations
     * 
     * @param prototypeID
     * @param angle
     * @param location
     * @param width
     * @param height
     * @param depth
     */
    public PrototypeReference( String prototypeID, float angle, float[] location, float width, float height, float depth ) {
        this.prototypeID = prototypeID;
        this.angle = angle;
        this.location = location;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * @return the prototypeID
     */
    public final String getPrototypeID() {
        return prototypeID;
    }

    /**
     * @param prototypeID
     *            the prototypeID to set
     */
    public final void setPrototypeID( String prototypeID ) {
        this.prototypeID = prototypeID;
    }

    /**
     * @return the gLTransformationMatrix
     */
    public final GLTransformationMatrix getTransformationMatrix() {
        return gLTransformationMatrix;
    }

    /**
     * @param gLTransformationMatrix
     *            the gLTransformationMatrix to set
     */
    public final void setTransformationMatrix( GLTransformationMatrix gLTransformationMatrix ) {
        this.gLTransformationMatrix = gLTransformationMatrix;
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
        out.writeUTF( prototypeID );
        out.writeObject( gLTransformationMatrix );
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
        prototypeID = in.readUTF();
        gLTransformationMatrix = (GLTransformationMatrix) in.readObject();
    }

    /**
     * @return the approximate size of this prototype in bytes.
     */
    public long getApproximateSizeInBytes() {
        long localSize = AllocatedHeapMemory.INSTANCE_SIZE;
        if ( gLTransformationMatrix != null ) {
            // transform matrix
            localSize += AllocatedHeapMemory.instanceAndReferenceSize( true );
            localSize += 16 * ( AllocatedHeapMemory.FLOAT_SIZE               );
            localSize += AllocatedHeapMemory.DOUBLE_SIZE;
        }
        localSize = AllocatedHeapMemory.sizeOfString( prototypeID, true, true );

        return localSize;
    }

    /**
     * @return the gLTransformationMatrix
     */
    public final GLTransformationMatrix getGLTransformationMatrix() {
        return gLTransformationMatrix;
    }

    /**
     * @return the angle
     */
    public final float getAngle() {
        return angle;
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
     * @return the depth
     */
    public final float getDepth() {
        return depth;
    }

    /**
     * @return
     */
    public float[] getLocation() {
        return location;
    }

}
