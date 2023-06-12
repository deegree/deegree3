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

package org.deegree.rendering.r3d.opengl.rendering.dem.texturing;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;

/**
 * Implementations provide texture data for texturing of a 2D-domain made up of
 * {@link RenderMeshFragment} instances.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface TextureTileProvider {

	/**
	 * Returns the provided resolution (in world units per pixel).
	 * @return the provided resolution
	 */
	public double getNativeResolution();

	/**
	 * Get a {@link TextureTile} best fitting the request.
	 * @param request information about the request.
	 * @return a texture tile.
	 */
	public TextureTile getTextureTile(TextureTileRequest request);

	/**
	 * @param unitsPerPixel
	 * @return true if a the provider could deliver a texture for the given resolution.
	 */
	public boolean hasTextureForResolution(double unitsPerPixel);

	/**
	 * @return the bounding box for this {@link TextureTileProvider}
	 */
	public Envelope getEnvelope();

	/**
	 * @return the crs of the texture tile provider
	 */
	public ICRS getCRS();

}
