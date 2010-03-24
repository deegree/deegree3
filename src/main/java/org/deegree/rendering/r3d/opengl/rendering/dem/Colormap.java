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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Arrays;

import javax.media.opengl.GL;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.ShaderProgram;
import org.slf4j.Logger;

/**
 * Implementation of a shader based color map for the WPVS. Colors will be linear interpolated between the min and max
 * color.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class Colormap {

    private final static String UNIFORM_MIN_COLOR = "minColor";

    private static final String UNIFORM_HEIGHT_COLOR = "heightColor";

    private final static String UNIFORM_DIFF_VEC = "diffVec";

    private final static String UNIFORM_MIN_Z = "zValues";

    private static final Logger LOG = getLogger( Colormap.class );

    private final float[] minColor;

    private final float[] maxColor;

    private float[] heightColor;

    private final float[] difVec;

    private final float zMax;

    private final float zMin;

    private final float invDifZ;

    private ShaderProgram shaderProgram;

    /**
     * Instantiate with min = red, max = green and height = brownish
     * 
     * @param zMin
     *            the minimum value of the terrain
     * @param zMax
     *            the maximum value of the terrain
     */
    public Colormap( float zMin, float zMax ) {
        this( zMin, zMax, new float[] { 1, 0, 0 }, new float[] { 0, 1, 0 }, new float[] { 0.25f, 0.11f, 0.09f } );
    }

    /**
     * 
     * @param zMin
     * @param zMax
     * @param minColor
     * @param maxColor
     * @param heightColor
     */
    public Colormap( float zMin, float zMax, float[] minColor, float[] maxColor, float[] heightColor ) {
        this.zMin = zMin;
        this.zMax = zMax;
        this.minColor = Arrays.copyOf( minColor, minColor.length );
        this.maxColor = Arrays.copyOf( maxColor, maxColor.length );
        this.heightColor = Arrays.copyOf( heightColor, heightColor.length );
        this.difVec = Vectors3f.sub( minColor, maxColor );
        this.invDifZ = 1 / ( zMax - zMin );
    }

    /**
     * @return the minColor
     */
    public final float[] getMinColor() {
        return minColor;
    }

    /**
     * @return the maxColor
     */
    public final float[] getMaxColor() {
        return maxColor;
    }

    /**
     * @return the difVec
     */
    public final float[] getDifVec() {
        return difVec;
    }

    /**
     * @return the zMin
     */
    public float getzMin() {
        return zMin;
    }

    /**
     * @return the zMax
     */
    public float getzMax() {
        return zMax;
    }

    private boolean setupShaderProgram( GL gl ) {
        this.shaderProgram = new ShaderProgram();
        int vertShaderId = shaderProgram.createVertexShader( gl, createVertexShader() );
        int fragShaderId = shaderProgram.createFragmentShader( gl, createFragmentShader( 1 ) );
        if ( !shaderProgram.attachShader( gl, vertShaderId ) ) {
            // rb: unable to attach the given shader id, ignoring.
            LOG.warn( "Could not attach the vertex part to the color map shader program, ignoring color map." );
            return false;
        }
        if ( !shaderProgram.attachShader( gl, fragShaderId ) ) {
            LOG.warn( "Could not attach the fragment part to the color map shader program, ignoring color map." );
            return false;
        }
        if ( !shaderProgram.linkProgram( gl ) ) {
            LOG.warn( "Could not link the shaders to the color map shader program, ignoring color map." );
            return false;
        }
        return true;

    }

    /**
     * @param glRenderContext
     */
    public void enable( RenderContext glRenderContext ) {
        GL gl = glRenderContext.getContext();
        if ( shaderProgram == null ) {
            if ( !setupShaderProgram( gl ) ) {
                this.shaderProgram = null;
                return;
            }
        }

        if ( shaderProgram.useProgram( gl ) ) {
            int mCLoc = gl.glGetUniformLocation( shaderProgram.getOGLId(), UNIFORM_MIN_COLOR );
            int maxCLoc = gl.glGetUniformLocation( shaderProgram.getOGLId(), UNIFORM_HEIGHT_COLOR );
            int dVLoc = gl.glGetUniformLocation( shaderProgram.getOGLId(), UNIFORM_DIFF_VEC );
            int miZLoc = gl.glGetUniformLocation( shaderProgram.getOGLId(), UNIFORM_MIN_Z );

            if ( mCLoc == -1 || maxCLoc == -1 || dVLoc == -1 || miZLoc == -1 ) {
                LOG.warn( "Could not get uniform location of Vertex shader, no color map is enabled: {},{},{},{}.",
                          new Object[] { mCLoc, maxCLoc, dVLoc, miZLoc, } );
                return;
            }

            gl.glUniform4f( mCLoc, minColor[0], minColor[1], minColor[2], 1 );
            gl.glUniform4f( maxCLoc, heightColor[0], heightColor[1], heightColor[2], 1 );
            gl.glUniform4f( dVLoc, difVec[0], difVec[1], difVec[2], 1 );
            gl.glUniform4f( miZLoc, zMin, invDifZ, 0, 0 );
        }

    }

    /**
     * @param glRenderContext
     */
    public void disable( RenderContext glRenderContext ) {
        if ( shaderProgram != null ) {
            shaderProgram.disable( glRenderContext.getContext() );
        }
    }

    private static String createVertexShader() {
        StringBuilder sb = new StringBuilder( "" );
        sb.append( "uniform vec4 minColor;\n" );
        sb.append( "uniform vec4 heightColor;\n" );
        sb.append( "uniform vec4 diffVec;\n" );
        sb.append( "uniform vec4 zValues;\n" ); // x == the minValue, y=the inverse difference Value
        sb.append( "varying vec3 view;\n" );
        sb.append( "varying vec3 normal;\n" );
        sb.append( "varying float isHeight;\n" );

        sb.append( "\n\nvoid main()\n{\n" );

        sb.append( "  float dist = ( gl_Vertex.z - zValues.x ) * zValues.y;\n" );

        sb.append( "  if( mod( dist, 0.1) < 0.001 ) {\n" );
        sb.append( "    isHeight = -1;\n" );
        sb.append( "  } else {\n" );
        sb.append( "    isHeight = 1;\n" );
        sb.append( "  }\n" );
        sb.append( "  dist = clamp( dist, 0.0,1.0);\n" );
        sb.append( "  vec4 diffColor = diffVec * dist;\n" );// calculate the color
        sb.append( "  gl_FrontColor = minColor - diffColor;\n" );

        sb.append( "  \n" );
        sb.append( "  view = vec3(gl_ModelViewMatrix * gl_Vertex);\n" );
        sb.append( "  normal = normalize(gl_NormalMatrix * gl_Normal);\n" );

        sb.append( "  \n" );
        sb.append( "  gl_Position = ftransform();\n" );// set the position
        sb.append( "  gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;\n" );// set the position
        sb.append( "}\n" ); // end of main
        return sb.toString();
    }

    /**
     * 
     * @return a fragment shader which implements the
     */
    private static String createFragmentShader( int numberOfLights ) {
        StringBuilder sb = new StringBuilder( "" );
        sb.append( "uniform vec4 heightColor;\n" );
        sb.append( "varying vec3 normal;\n" );
        sb.append( "varying vec3 view;\n" );
        sb.append( "varying float isHeight;\n" );

        sb.append( "#define MAX_LIGHTS " ).append( numberOfLights ).append( "\n" );

        sb.append( "void main (void)\n" );
        sb.append( "{\n" );
        sb.append( "  vec3 N = normalize(normal);\n" );
        sb.append( "  vec4 finalColor = gl_Color;\n" );
        sb.append( "  vec4 tmpColor = vec4(0,0,0,0);\n" );
        sb.append( "  float maxSpecPart = 1.0;\n" );
        sb.append( "  if(  isHeight<0  ){\n" );
        sb.append( "    finalColor = heightColor;\n" );
        sb.append( "    maxSpecPart = 0.5;\n" );
        sb.append( "  }\n" );
        sb.append( "  for (int i=0;i<MAX_LIGHTS;i++)\n" );
        sb.append( "  {\n" );
        sb.append( "    vec3 L = normalize(gl_LightSource[i].position.xyz - view);\n" );
        sb.append( "    vec3 E = normalize(-view);\n" ); // we are in Eye Coordinates, so EyePos is (0,0,0)
        sb.append( "    vec3 R = normalize(-reflect(L,N));\n" );

        // calculate Ambient Term, rb just a little smaller:
        sb.append( "    vec4 Iamb = 0.5*(gl_FrontLightProduct[i].ambient);\n" );

        // calculate Diffuse Term:
        sb.append( "    vec4 Idiff = gl_FrontLightProduct[i].diffuse * max(dot(N,L), 0.0);\n" );
        sb.append( "    Idiff = clamp(Idiff, 0.0, 1.0);\n" );

        // calculate Specular Term:
        sb.append( "    vec4 Ispec = gl_FrontLightProduct[i].specular * pow(max(dot(R,E),0.0),0.3*gl_FrontMaterial.shininess);\n" );
        sb.append( "    Ispec = clamp(Ispec, 0.0, maxSpecPart);\n" );

        sb.append( "    tmpColor += Iamb + Idiff + Ispec;\n" );
        sb.append( "  }\n" );

        // write Total Color:
        sb.append( "  gl_FragColor = finalColor + tmpColor;\n" );
        sb.append( "}\n" );
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "{minColor: " ).append( Arrays.toString( minColor ) ).append( "}," );
        sb.append( "{maxColor: " ).append( Arrays.toString( maxColor ) ).append( "}," );
        sb.append( "{heightColor: " ).append( Arrays.toString( heightColor ) ).append( "}," );
        sb.append( "{minZ: " ).append( zMin ).append( "}," );
        sb.append( "{maxZ: " ).append( zMax ).append( "}" );
        return sb.toString();
    }
}
