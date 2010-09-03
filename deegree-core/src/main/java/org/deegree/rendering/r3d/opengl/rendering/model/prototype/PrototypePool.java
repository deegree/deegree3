//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/prototypes/PrototypePool.java $
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

package org.deegree.rendering.r3d.opengl.rendering.model.prototype;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;

/**
 * The <code>PrototypePool</code> holds a set of prototyes which can be referenced by supplying their id. Before the
 * rendering of the prototype the translation, rotation and scale will ge applied to the modelview matrix. This implies,
 * that each prototype should be defined between [0,1] at the origin.
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
     * @param glRenderContext
     * @param prototype
     * @param buffer
     *            to be used for rendering.
     */
    public synchronized static void render( RenderContext glRenderContext, PrototypeReference prototype,
                                            DirectGeometryBuffer buffer ) {
        if ( prototype == null || prototype.getPrototypeID() == null ) {
            return;
        }
        RenderablePrototype model = prototypes.get( prototype.getPrototypeID() );

        if ( model == null ) {
            LOG.warn( "No model found for prototype: " + prototype.getPrototypeID() );
            return;
        }

        GL context = glRenderContext.getContext();

        context.glPushMatrix();

        float[] loc = prototype.getLocation();
        context.glTranslatef( loc[0], loc[1], loc[2] );
        context.glRotatef( prototype.getAngle(), 0, 0, 1 );
        context.glScalef( prototype.getWidth(), prototype.getDepth(), prototype.getHeight() );
        if ( buffer == null ) {
            model.render( null );
        } else {
            model.renderPrepared( glRenderContext, buffer );
        }
        context.glPopMatrix();
    }

    /**
     * @return
     */
    private static RenderablePrototype createBoxPrototype() {
        RenderableQualityModel rqm = new RenderableQualityModel();

        RenderableGeometry rg = new BOXGeometry();
        rqm.addQualityModelPart( rg );
        Envelope env = new GeometryFactory().createEnvelope( 0, 0, 1, 1, null );
        return new RenderablePrototype( "box", "yeah", env, rqm );
    }

    /**
     * @param id
     * @param model
     */
    public static synchronized void addPrototype( String id, RenderablePrototype model ) {
        if ( id == null || "".equals( id ) || model == null || prototypes.containsKey( id ) ) {
            LOG.info( "Ignoring prototype because no id/model was given." );
            return;
        }
        prototypes.put( id, model );
    }

}
