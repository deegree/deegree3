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

package org.deegree.rendering.r3d.opengl.rendering;

import static org.slf4j.LoggerFactory.getLogger;

import javax.media.opengl.GL;

import org.slf4j.Logger;

/**
 * Compiles, attaches and links shader (vertex and fragment) shaders to a OpenGL shader program.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class ShaderProgram {
    private static final Logger LOG = getLogger( ShaderProgram.class );

    private int oglProgramId = -1;

    /**
     * @param glContext
     *            the context for which this shader program was created.
     * @param vertexShaderSource
     *            the program to attach and link against this open gl program.
     * @return the id of the linked vertex shader.
     * @throws RuntimeException
     *             if the shader could not be compiled or linked.
     */
    public int createVertexShader( GL glContext, String vertexShaderSource )
                            throws RuntimeException {
        LOG.debug( "Adding vertex shader source: " + vertexShaderSource );
        return compileShader( glContext, GL.GL_VERTEX_SHADER, vertexShaderSource );
    }

    /**
     * @param glContext
     *            the context for which this shader program was created.
     * @param fragmentShaderSource
     *            the program to attach and link against this open gl program.
     * @return the id of the linked vertex shader.
     * @throws RuntimeException
     *             if the shader could not be compiled or linked.
     */
    public int createFragmentShader( GL glContext, String fragmentShaderSource )
                            throws RuntimeException {
        LOG.debug( "Adding fragment shader source: " + fragmentShaderSource );
        return compileShader( glContext, GL.GL_FRAGMENT_SHADER, fragmentShaderSource );
    }

    /**
     * Creates, Compiles, links and validates the given shader source.
     * 
     * @param gl
     *            to be link against
     * @param GL_SHADER_ID
     *            either {@link GL#GL_VERTEX_SHADER} or {@link GL#GL_FRAGMENT_SHADER}
     * @param shaderSource
     *            to be used
     * @param shaderProgramId
     *            the id to attach and link the shader too, if -1 a new program will be generated.
     * @return the id of the shader program and the id of teh shader.
     */
    private int compileShader( GL gl, int GL_SHADER_ID, String shaderSource )
                            throws RuntimeException {

        if ( oglProgramId == -1 ) {
            oglProgramId = gl.glCreateProgram();
        }
        int shaderId = -1;
        try {
            shaderId = compileShaderProgram( gl, GL_SHADER_ID, shaderSource );
        } catch ( RuntimeException r ) {
            LOG.error( "Could not compile " + ( ( GL.GL_VERTEX_SHADER == GL_SHADER_ID ) ? "vertex" : "fragment" )
                       + " shader from source: \n" + shaderSource + " \nbecause: " + r.getLocalizedMessage(), r );
            throw ( r );
        }

        return shaderId;
    }

    /**
     * Compiles the given shader program int to the given shader.
     * 
     * @param gl
     *            context to compile for
     * @param GL_SHADER_PROG_ID
     *            either {@link GL#GL_VERTEX_SHADER} or {@link GL#GL_FRAGMENT_SHADER}
     * @param shaderSource
     *            the source to compile.
     * @return the shader id, not the program id.
     */
    private int compileShaderProgram( GL gl, int GL_SHADER_PROG_ID, String shaderSource ) {
        int shaderId = gl.glCreateShader( GL_SHADER_PROG_ID );
        gl.glShaderSource( shaderId, 1, new String[] { shaderSource }, (int[]) null, 0 );
        gl.glCompileShader( shaderId );

        int[] status = new int[1];
        gl.glGetShaderiv( shaderId, GL.GL_COMPILE_STATUS, status, 0 );
        if ( status[0] == GL.GL_FALSE ) {
            int[] length = new int[1];
            gl.glGetShaderiv( shaderId, GL.GL_INFO_LOG_LENGTH, length, 0 );
            byte[] infoLog = new byte[length[0]];
            gl.glGetShaderInfoLog( shaderId, length[0], length, 0, infoLog, 0 );
            String msg = new String( infoLog );
            LOG.error( msg );
            throw new RuntimeException( "Error while compiling shader program: " + msg );
        }
        return shaderId;
    }

    /**
     * Links the given shader id to the given context.
     * 
     * @param gl
     *            context to compile for
     * @param shaderId
     *            to link
     * 
     */
    private void linkShaderProgram( GL gl ) {

        // link program
        gl.glLinkProgram( oglProgramId );
        int[] linkStatus = new int[1];
        gl.glGetProgramiv( oglProgramId, GL.GL_LINK_STATUS, linkStatus, 0 );
        if ( linkStatus[0] == GL.GL_FALSE ) {
            int[] length = new int[1];
            gl.glGetProgramiv( oglProgramId, GL.GL_INFO_LOG_LENGTH, length, 0 );
            byte[] infoLog = new byte[length[0]];
            gl.glGetProgramInfoLog( oglProgramId, length[0], length, 0, infoLog, 0 );
            String msg = new String( infoLog );
            throw new RuntimeException( msg );
        }
    }

    /**
     * Validates the given program
     * 
     * @param gl
     *            the program is linked against
     * @param shaderProgramId
     *            to validate.
     */
    private void validateShaderProgram( GL gl ) {
        // validate program
        gl.glValidateProgram( oglProgramId );
        int[] validateStatus = new int[1];
        gl.glGetProgramiv( oglProgramId, GL.GL_VALIDATE_STATUS, validateStatus, 0 );
        if ( validateStatus[0] == GL.GL_FALSE ) {
            int[] length = new int[1];
            gl.glGetProgramiv( oglProgramId, GL.GL_INFO_LOG_LENGTH, length, 0 );
            byte[] infoLog = new byte[length[0]];
            gl.glGetProgramInfoLog( oglProgramId, length[0], length, 0, infoLog, 0 );
            String msg = new String( infoLog );
            // LOG.error( msg );
            throw new RuntimeException( "Shader validation failed: " + msg );
        }
    }

    /**
     * @param gl
     *            context to enable this shader program for.
     * @return true if the program is in use.
     */
    public boolean useProgram( GL gl ) {
        // link program
        if ( gl.glIsProgram( oglProgramId ) ) {
            gl.glUseProgram( oglProgramId );
            return true;
        }
        LOG.warn( "The given programId {} is invalid in the given context.", oglProgramId );

        return false;
    }

    /**
     * @param gl
     *            the open gl context for which the given shader id should be valid.
     * @param shaderId
     *            a vertex or fragment shader id to be tested.
     * @return true if this shader program is valid and the open gl context thinks the given shader id is a valid shader
     *         id.
     */
    public boolean isShaderIdValid( GL gl, int shaderId ) {
        return gl.glIsProgram( oglProgramId ) && gl.glIsShader( shaderId );
    }

    /**
     * @param gl
     *            the context of this shader program.
     * @param shaderId
     *            to attach to this program.
     * @return true iff this program and the given shader id are valid and the id could be attached to the program
     *         (attaching is not linking!).
     */
    public boolean attachShader( GL gl, int shaderId ) {
        boolean result = false;
        if ( isShaderIdValid( gl, shaderId ) ) {
            gl.glAttachShader( oglProgramId, shaderId );
            result = true;
        } else {
            LOG.warn( "Either the program id: " + oglProgramId + " or the given shader id: " + shaderId
                      + " are not valid, cannot attach the shader." );
        }
        return result;
    }

    /**
     * Link this program with all it's attached shaders.
     * 
     * @param gl
     * @return true if the linkage was successful and the validation was positive.
     * @throws RuntimeException
     *             if an OGL exception occurred.
     */
    public boolean linkProgram( GL gl )
                            throws RuntimeException {
        boolean result = false;
        try {
            linkShaderProgram( gl );
            result = true;
        } catch ( RuntimeException r ) {
            LOG.error( "Could not link shader because: " + r.getLocalizedMessage(), r );
            throw ( r );
        }
        // if ( LOG.isDebugEnabled() ) {
        try {
            validateShaderProgram( gl );
        } catch ( RuntimeException r ) {
            LOG.warn( "Shader program source: was not valid because: " + r.getLocalizedMessage() );
            result = false;
        }
        return result;
    }

    /**
     * @param gl
     *            the context of this shader program.
     * @param shaderId
     *            to be detached from this program.
     * @return true iff the current program id and the given shaderId are valid and the shader could be detached from
     *         this program.
     */
    public boolean detachShader( GL gl, int shaderId ) {
        boolean result = false;
        if ( isShaderIdValid( gl, shaderId ) ) {
            gl.glDetachShader( oglProgramId, shaderId );
            result = true;
        } else {
            LOG.warn( "Either the program id: " + oglProgramId + " or the given shader id: " + shaderId
                      + " are not valid, cannot detach the shader." );
        }
        return result;
    }

    /**
     * @return the ogl id of the this program.
     */
    public int getOGLId() {
        return oglProgramId;
    }

    /**
     * Disables this shader program and set the context to use the fixed shaders (0).
     * 
     * @param gl
     *            context to be set.
     */
    public void disable( GL gl ) {
        gl.glUseProgram( 0 );
    }
}
