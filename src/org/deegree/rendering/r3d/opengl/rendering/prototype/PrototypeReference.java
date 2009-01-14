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

import java.io.Serializable;

import org.deegree.rendering.r3d.opengl.math.GLTransformationMatrix;

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

    private String prototypeID;

    private GLTransformationMatrix gLTransformationMatrix;

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

}
