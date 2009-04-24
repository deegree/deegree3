//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.multiresolution;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import org.deegree.commons.utils.nio.PooledByteBuffer;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class MeshFragmentData {

    private int numTriangles;

    private final PooledByteBuffer rawBuffer;    
    
    private final FloatBuffer vertexBuffer;

    private final Buffer indexBuffer;

    private final FloatBuffer normalsBuffer;

    public MeshFragmentData( PooledByteBuffer rawBuffer, FloatBuffer vertexBuffer, FloatBuffer normalsBuffer, Buffer indexBuffer ) {
        this.rawBuffer = rawBuffer;
        this.vertexBuffer = vertexBuffer;
        this.normalsBuffer = normalsBuffer;
        this.indexBuffer = indexBuffer;
        this.numTriangles = indexBuffer.capacity() / 3;
    }

    public int getNumTriangles() {
        return numTriangles;
    }

    public FloatBuffer getVertices() {
        return vertexBuffer;
    }

    /**
     * Returns the buffer that contains the vertices of the triangles.
     * <p>
     * The returned buffer can be (depending on the number of vertices in the fragment):
     * <ul>
     * <li>a <code>ByteBuffer</code></li>
     * <li>a <code>ShortBuffer</code></li>
     * <li>an <code>IntBuffer</code></li>
     * </ul>
     * 
     * @return buffer the contains the triangles (as vertex indexes)
     */
    public Buffer getTriangles() {
        return indexBuffer;
    }

    public FloatBuffer getNormals() {
        return normalsBuffer;
    }
    
    public void freeBuffers () {
        rawBuffer.free();
    }
}
