//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/math/GLTransformationMatrix.java $
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

package org.deegree.rendering.r3d.opengl.math;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * The <code>GLTransformationMatrix</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 15512 $, $Date: 2009-01-06 12:12:13 +0100 (Di, 06 Jan 2009) $
 * 
 */
public class GLTransformationMatrix extends Matrix4f {

    /**
     * 
     */
    private static final long serialVersionUID = -1578210859739414223L;

    /**
     * 
     */
    public GLTransformationMatrix() {
        super();
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     * @param arg5
     * @param arg6
     * @param arg7
     * @param arg8
     * @param arg9
     * @param arg10
     * @param arg11
     * @param arg12
     * @param arg13
     * @param arg14
     * @param arg15
     */
    public GLTransformationMatrix( float arg0, float arg1, float arg2, float arg3, float arg4, float arg5, float arg6,
                                 float arg7, float arg8, float arg9, float arg10, float arg11, float arg12,
                                 float arg13, float arg14, float arg15 ) {
        super( arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15 );
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public GLTransformationMatrix( float[] arg0 ) {
        super( arg0 );
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     */
    public GLTransformationMatrix( Matrix3f arg0, Vector3f arg1, float arg2 ) {
        super( arg0, arg1, arg2 );
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public GLTransformationMatrix( Matrix4d arg0 ) {
        super( arg0 );
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public GLTransformationMatrix( Matrix4f arg0 ) {
        super( arg0 );
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     */
    public GLTransformationMatrix( Quat4f arg0, Vector3f arg1, float arg2 ) {
        super( arg0, arg1, arg2 );
        // TODO Auto-generated constructor stub
    }

    /**
     * Convenience method for getting the correct opengl matrix
     * 
     * @return the column order matrix required by opengl.
     */
    public FloatBuffer getValues() {
        return FloatBuffer.wrap( new float[] { m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31,
                                              m32, m33 } );

    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for ( int i = 0; i < 16; ++i ) {
            b.append( getValues().get( i ) ).append( "\t" );
            if ( ( i + 1 ) % 4 == 0 ) {
                b.append( "\n" );
            }
        }
        return b.toString();
    }
}
