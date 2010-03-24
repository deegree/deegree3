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

package org.deegree.rendering.r3d.opengl.rendering.model.geometry;

import org.deegree.rendering.r3d.model.QualityModelPart;
import org.deegree.rendering.r3d.opengl.rendering.JOGLRenderable;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;

/**
 * The <code>RenderableQualityModelPart</code> a part of a {@link RenderableQualityModel}, normally a geometry.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 *
 */
public interface RenderableQualityModelPart extends QualityModelPart, JOGLRenderable {

    /**
     * @return the number of ordinates in the implementation.
     */
    public int getOrdinateCount();

    /**
     * @return the number of texture ordinates in this quality model part
     */
    public int getTextureOrdinateCount();

    /**
     * This method is a more specific render method than the {@link JOGLRenderable#render(RenderContext)}, it defines a
     * contract that the geometryBuffer holds the geometry for the implementation and the implementation holds indizes
     * which are valid for the given buffer.
     *
     * @param glRenderContext
     *            holding all relevant data for the current gl context.
     * @param geometryBuffer
     *            holding the vertices, normals and texture coordinates for an instance of an implementation.
     */
    public void renderPrepared( RenderContext glRenderContext, DirectGeometryBuffer geometryBuffer );

}
