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

package org.deegree.rendering.r3d.opengl.rendering.managers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.vecmath.Point3d;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.rendering.JOGLRenderable;
import org.deegree.rendering.r3d.opengl.rendering.WorldRenderableObject;

/**
 * The <code>TreeManager</code> will hold the bill board references.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class BuildingRenderer extends RenderableManager<WorldRenderableObject> implements JOGLRenderable {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( BuildingRenderer.class );

    /**
     * @param validDomain
     * @param numberOfObjectsInLeaf
     */
    public BuildingRenderer( Envelope validDomain, int numberOfObjectsInLeaf ) {
        super( validDomain, numberOfObjectsInLeaf );
    }

    /**
     * 
     * @param eye
     * @return an ordered List of trees.
     */
    public List<WorldRenderableObject> getBuildingsForViewParameters( ViewParams params ) {
        BuildingComparator a = new BuildingComparator( params.getViewFrustum().getEyePos() );
        return getObjects( params, a );
    }

    @Override
    public void render( GL context, ViewParams params ) {
        long begin = System.currentTimeMillis();
        Point3d eye = params.getViewFrustum().getEyePos();
        float[] eye2 = new float[] { (float) eye.x, (float) eye.y, (float) eye.z };
        List<WorldRenderableObject> allBillBoards = getBuildingsForViewParameters( params );
        if ( !allBillBoards.isEmpty() ) {
            // back to front
            Collections.sort( allBillBoards, new DistComparator( eye ) );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Number of buildings from viewparams: " + allBillBoards.size() );
                LOG.debug( "Total number of buildings : " + size() );
            }
            Iterator<WorldRenderableObject> it = allBillBoards.iterator();
            while ( it.hasNext() ) {
                WorldRenderableObject b = it.next();
                context.glPushMatrix();
                // Texture t = TexturePool.getTexture( context, b.getTextureID() );
                // if ( t != null ) {
                // if ( currentTexture == null || t.getTextureObject() != currentTexture.getTextureObject() ) {
                // t.bind();
                // currentTexture = t;
                // }
                // }
                b.render( context, params );
                context.glPopMatrix();
            }
            // context.glDisable( GL.GL_TEXTURE_2D );
            // context.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
        }

    }

    private class BuildingComparator implements Comparator<WorldRenderableObject> {
        private float[] eye;

        /**
         * @param eye
         *            to compare this billboard to.
         * 
         */
        public BuildingComparator( Point3d eye ) {
            this.eye = new float[] { (float) eye.x, (float) eye.y, (float) eye.z };
        }

        @Override
        public int compare( WorldRenderableObject o1, WorldRenderableObject o2 ) {
            float distA = Vectors3f.distance( eye, o1.getPosition() );
            float distB = Vectors3f.distance( eye, o2.getPosition() );
            return Float.compare( distA, distB );
        }

    }

    private class DistComparator implements Comparator<WorldRenderableObject> {
        private float[] eye;

        /**
         * @param eye
         *            to compare this billboard to.
         * 
         */
        public DistComparator( Point3d eye ) {
            this.eye = new float[] { (float) eye.x, (float) eye.y, (float) eye.z };
        }

        @Override
        public int compare( WorldRenderableObject o1, WorldRenderableObject o2 ) {
            float distA = Vectors3f.distance( eye, o1.getPosition() );
            float distB = Vectors3f.distance( eye, o2.getPosition() );
            // /**
            // * Trees that are near to each other might have the same texture.
            // */
            // if ( Math.abs( distA - distB ) < 35 ) {
            // int res = o1.getTextureID().compareTo( o2.getTextureID() );
            // if ( res == 0 ) {
            // res = -Float.compare( distA, distB );
            // }
            // return res;
            // }
            return -Float.compare( distA, distB );
        }

    }
}
