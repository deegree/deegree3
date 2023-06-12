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

package org.deegree.services.wpvs;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;
import javax.vecmath.Vector3f;

/**
 * This class represents a <code>WPVSConfiguration</code> object.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 *
 */
public class WPVSConfiguration {

	/**
	 * Defines the up vector for the scene
	 */
	public static final Vector3f UP_VECTOR = new Vector3f(0, 0, 1);

	private static GLPbuffer buffer = createOffscreenBuffer();

	/**
	 * creates and returns a canvas for offscreen rendering
	 * @return a offscreen Canvas3D on which the the scene will be rendered.
	 */
	protected static synchronized GLPbuffer createOffscreenBuffer() {
		// Create the offscreen drawable (pBuffer). Note that the width
		// and height must be a power of two if it is to be used as a
		// texture.
		// GLCanvas d = new GLCanvas();
		if (GLDrawableFactory.getFactory().canCreateGLPbuffer()) {
			// System.out.println( "YES" );
			GLCapabilities caps = new GLCapabilities();
			GLPbuffer buf = GLDrawableFactory.getFactory().createGLPbuffer(caps, null, 800, 600, null// d.getContext()
			);
			return buf;
			// buffer.addGLEventListener(new
			// PBufferGLEventListener(pBufferTexID,128,128,glu));
		}
		System.err.println("Can't create a pBuffer.");
		// System.exit(1);

		return null;
	}

	/**
	 * @return the renderer
	 */
	public GLPbuffer getRenderer() {
		// TODO Auto-generated method stub
		return buffer;
	}

}
