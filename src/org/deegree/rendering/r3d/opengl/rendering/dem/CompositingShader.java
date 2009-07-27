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
package org.deegree.rendering.r3d.opengl.rendering.dem;

/**
 * The <code>CompositingShader</code> generates a fragment shader file for a number of texture units. The applied
 * colormodel is sort of a 'decall' functionality with glColor and semi transparent support.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: $, $Date: $
 *
 */
public class CompositingShader {

    /**
     * @param numTextures
     * @return create a fragment shader program which is programmed with a 'DECAL'-like strukture with semi-transpartent
     *         supprt, for the given number of texture units.
     */
    public static String getGLSLCode( int numTextures ) {

        StringBuffer sb = new StringBuffer();

        // first line "uniform sampler2D tex0,tex1,tex2...;"
        sb.append( "uniform sampler2D tex0" );
        for ( int i = 1; i < numTextures; i++ ) {
            sb.append( ",tex" );
            sb.append( i );
        }
        sb.append( ';' );

        // begin main method
        sb.append( "\n\nvoid main()\n{\n" );
        sb.append( "    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n" );

        // add a GL_DECAL-like function for each texture unit
        for ( int i = 0; i < numTextures; i++ ) {
            sb.append( "\n    vec4 tex" + i + "Color = texture2D(tex" + i + ", gl_TexCoord[" + i + "].st);\n" );
            sb.append( "    gl_FragColor = (1.0 - tex" + i + "Color.a) * gl_FragColor + tex" + i + "Color.a * tex" + i
                       + "Color;\n" );
        }

        // end main method
        sb.append( "\n   gl_FragColor = gl_FragColor * gl_Color;\n" );
        sb.append( "}\n" );
        return sb.toString();
    }
}
