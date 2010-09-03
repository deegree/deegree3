//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/prototypes/PrototypeReference.java $
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

package org.deegree.rendering.r3d.opengl.rendering.model.prototype;

import java.io.Serializable;

import org.deegree.commons.utils.memory.AllocatedHeapMemory;

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

    /**
     *
     */
    private static final long serialVersionUID = 417932269664979569L;

    private transient String prototypeID;

    private transient float angle;

    private transient float width;

    private transient float height;

    private transient float depth;

    private float[] location;

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
     * @return the approximate size of this prototype in bytes.
     */
    public long getApproximateSizeInBytes() {
        long localSize = AllocatedHeapMemory.INSTANCE_SIZE;
        localSize += AllocatedHeapMemory.FLOAT_SIZE * 4;
        localSize += AllocatedHeapMemory.sizeOfFloatArray( location, true );
        localSize = AllocatedHeapMemory.sizeOfString( prototypeID, true, true );

        return localSize;
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
     * @return the location (translation) of this reference.
     */
    public float[] getLocation() {
        return location;
    }

}
