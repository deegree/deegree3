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

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.vecmath.Vector3f;

import org.deegree.rendering.r3d.QualityModel;
import org.deegree.rendering.r3d.opengl.rendering.prototype.PrototypePool;

/**
 * The <code>GeometryQualityModel</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class RenderableQualityModel extends QualityModel<RenderableGeometry> implements Renderable {

    /**
     * 
     */
    private static final long serialVersionUID = -7176025467303167541L;

    /**
     * Creates a GeometryQualityModel with an empty list of geometry patches
     * 
     */
    public RenderableQualityModel() {
        super();
    }

    /**
     * Creates a GeometryQualityModel with the given geometry patches
     * 
     * @param geometryPatches
     */
    public RenderableQualityModel( ArrayList<RenderableGeometry> geometryPatches ) {
        super( geometryPatches );
    }

    @Override
    public void render( GL context, Vector3f eye ) {
        if ( prototype != null ) {
            PrototypePool.render( context, prototype, eye );
        } else {
            // no prototype to render, trying geometries
            if ( geometryPatches != null && geometryPatches.size() > 0 ) {
                for ( RenderableGeometry data : geometryPatches ) {
                    if ( data != null ) {
                        data.render( context, eye );
                    }
                }
            }
        }
    }
}
