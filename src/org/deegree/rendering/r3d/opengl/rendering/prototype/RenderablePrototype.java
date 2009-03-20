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

package org.deegree.rendering.r3d.opengl.rendering.prototype;

import javax.vecmath.Tuple3f;

import org.deegree.model.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.WorldRenderableObject;

/**
 * The <code>RenderablePrototype</code> is a {@link WorldRenderableObject} which only has one level of detail.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RenderablePrototype extends WorldRenderableObject {

    /**
     * 
     */
    private static final long serialVersionUID = -6438620186289443235L;

    /**
     * @param id
     * @param time
     * @param bbox
     */
    public RenderablePrototype( String id, String time, Envelope bbox ) {
        super( id, time, bbox, 0 );
    }

    /**
     * @param id
     * @param time
     * @param bbox
     * @param qualityLevel
     */
    public RenderablePrototype( String id, String time, Envelope bbox, RenderableQualityModel qualityLevel ) {
        super( id, time, bbox, new RenderableQualityModel[] { qualityLevel } );
    }

    /**
     * @param eye
     * @return the level to render.
     */
    @Override
    protected int calcQualityLevel( Tuple3f eye ) {
        return 0;
    }

}
