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
import javax.vecmath.Vector3f;

import org.deegree.rendering.r3d.opengl.rendering.Renderable;
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

    private static final Map<String, Renderable> prototypes = new HashMap<String, Renderable>();

    /**
     * @param context
     * @param prototype
     * @param eye
     *            of the beholder
     */
    public synchronized static void render( GL context, PrototypeReference prototype, Vector3f eye ) {
        if ( prototype == null || prototype.getTransformationMatrix() == null || prototype.getPrototypeID() == null ) {
            return;
        }
        Renderable model = prototypes.get( prototype.getPrototypeID() );
        if ( model == null ) {
            return;
        }
        context.glPushMatrix();
        context.glLoadMatrixf( prototype.getTransformationMatrix().getValues() );
        // render prototype
        model.render( context, eye );
        context.glPopMatrix();
    }

    /**
     * @param id
     * @param model
     */
    public static synchronized void addPrototype( String id, RenderableQualityModel model ) {
        if ( id == null || "".equals( id ) || model == null || prototypes.containsKey( id ) ) {
            return;
        }
        prototypes.put( id, model );
    }

    /**
     * @return the reference to the prototype map (not a copy), changes to the map will reflect the pool.
     */
    public static final Map<String, Renderable> getPrototypes() {
        return prototypes;
    }
}
