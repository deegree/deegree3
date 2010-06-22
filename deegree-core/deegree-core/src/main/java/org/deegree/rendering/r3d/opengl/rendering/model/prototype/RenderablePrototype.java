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

package org.deegree.rendering.r3d.opengl.rendering.model.prototype;

import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;

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
        super( id, time, bbox, 1 );
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
     * @param glRenderContext
     *            necessary to calculate the right level
     * @return 0
     */
    @Override
    protected int calcQualityLevel( RenderContext glRenderContext ) {
        return 0;
    }

}
