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

package org.deegree.rendering.r3d.opengl.tesselation;

import org.deegree.rendering.r3d.model.geometry.TexturedGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableTexturedGeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>TexturedGeometryCallBack</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TexturedGeometryCallBack extends GeometryCallBack {

    private final transient static Logger LOG = LoggerFactory.getLogger( TexturedGeometryCallBack.class );

    /**
     * @param geom
     */
    public TexturedGeometryCallBack( TexturedGeometry geom ) {
        super( geom );
    }

    @Override
    public void combineData( double[] coords, Object[] coordinateData, float[] weights, Object[] outData,
                             Object originalVertex ) {
        LOG.trace( "Tesselation combining textured vertex data." );
        LOG.trace( "Coordinates of vertex: " + coords[0] + "," + coords[1] + "," + coords[2] );
        TexturedVertex[] cd = new TexturedVertex[coordinateData.length];
        for ( int i = 0; i < coordinateData.length; ++i ) {
            cd[i] = (TexturedVertex) coordinateData[i];
        }
        outData[0] = new TexturedVertex( coords, cd, weights );
    }

    @Override
    public void vertexData( Object newVertex, Object originalVertex ) {
        if ( LOG.isTraceEnabled() ) {
            StringBuilder sb = new StringBuilder( "New tesselate textured vertex:\n" );
            sb.append( newVertex );
            LOG.trace( sb.toString() );
        }
        addVertex( (TexturedVertex) newVertex );
    }

    @Override
    public Vertex createNewVertex( int currentVertexLocation ) {
        float[] coords = getGeometry().getCoordinateForVertex( currentVertexLocation );
        float[] texCoords = ( (TexturedGeometry) getGeometry() ).getTextureCoordinateForVertex( currentVertexLocation );
        return new TexturedVertex( coords, null, texCoords );
    }

    @Override
    public RenderableGeometry createRenderableGeometry( boolean useDirectBuffers ) {
        return new RenderableTexturedGeometry( getTesselatedCoordinates(), getOpenGLType(), calculateNormals(),
                                               getGeometry().getStyle(),
                                               ( (TexturedGeometry) getGeometry() ).getTexture(),
                                               getTesselatedTextureCoordinates(), useDirectBuffers );
    }

    /**
     * @return the texture coordinates of the vertices created by the tesselation process.
     */
    protected float[] getTesselatedTextureCoordinates() {
        float[] texture_coords = new float[getTesselatedVertices().size() * 2];
        for ( int vertex = 0; vertex < getTesselatedVertices().size(); ++vertex ) {
            TexturedVertex v = (TexturedVertex) getTesselatedVertices().get( vertex );
            texture_coords[vertex * 2] = v.tex_u;
            texture_coords[( vertex * 2 ) + 1] = v.tex_v;
        }
        return texture_coords;
    }
}
