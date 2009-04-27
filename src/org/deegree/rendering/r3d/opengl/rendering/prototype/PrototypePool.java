//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/prototypes/PrototypePool.java $
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

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactoryCreator;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.rendering.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.JOGLRenderable;
import org.deegree.rendering.r3d.opengl.rendering.RenderableGeometry;
import org.deegree.rendering.r3d.opengl.rendering.RenderableQualityModel;

/**
 * The <code>Prototypes</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 15598 $, $Date: 2009-01-12 15:03:49 +0100 (Mo, 12 Jan 2009) $
 * 
 */
public class PrototypePool {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( PrototypePool.class );

    private static final Map<String, RenderablePrototype> prototypes = new HashMap<String, RenderablePrototype>();

    static {
        prototypes.put( "box", createBoxPrototype() );
    }

    /**
     * @param context
     * @param prototype
     * @param eye
     *            of the beholder
     */
    public synchronized static void render( GL context, ViewParams params, PrototypeReference prototype ) {
        if ( prototype == null || prototype.getPrototypeID() == null ) {
            return;
        }
        JOGLRenderable model = prototypes.get( prototype.getPrototypeID() );
        System.out.println( "model: " + prototype.getPrototypeID() );
        if ( model == null ) {
            LOG.warn( "No model found for prototype: " + prototype.getPrototypeID() );
            return;
        }
        context.glPushMatrix();
        // context.glLoadMatrixf( prototype.getTransformationMatrix().getValues() );
        float[] loc = prototype.getLocation();
        context.glTranslatef( loc[0], loc[1], loc[2] );
        System.out.println( "Location: " + Vectors3f.asString( loc ) );
        context.glRotatef( prototype.getAngle(), 0, 0, 1 );

        context.glScalef( prototype.getWidth(), prototype.getDepth(), prototype.getHeight() );
        // render prototype
        model.render( context, params );
        context.glPopMatrix();
    }

    /**
     * @return
     */
    private static RenderablePrototype createBoxPrototype() {
        RenderableQualityModel rqm = new RenderableQualityModel();

        RenderableGeometry rg = new BOXGeometry();
        rqm.addQualityModelPart( rg );
        Envelope env = GeometryFactoryCreator.getInstance().getGeometryFactory().createEnvelope( 0, 0, 1, 1, null );
        return new RenderablePrototype( "box", "yeah", env, rqm );
    }

    /**
     * @param id
     * @param model
     * @param geometryBuffer
     */
    public static synchronized void addPrototype( String id, RenderablePrototype model,
                                                  DirectGeometryBuffer geometryBuffer ) {
        if ( id == null || "".equals( id ) || model == null || prototypes.containsKey( id ) ) {
            return;
        }
        prototypes.put( id, model );
    }

    /**
     * @return the reference to the prototype map (not a copy), changes to the map will reflect the pool.
     */
    public static final Map<String, RenderablePrototype> getPrototypes() {
        return prototypes;
    }
}
