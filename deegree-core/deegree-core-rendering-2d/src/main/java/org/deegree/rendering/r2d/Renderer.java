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

package org.deegree.rendering.r2d;

import java.util.Collection;

import org.deegree.geometry.Geometry;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.Styling;

/**
 * <code>Renderer</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public interface Renderer {

	/**
	 * @param styling
	 * @param geom
	 */
	void render(PointStyling styling, Geometry geom);

	/**
	 * @param styling
	 * @param geom
	 */
	void render(LineStyling styling, Geometry geom);

	/**
	 * @param styling
	 * @param geom
	 */
	void render(PolygonStyling styling, Geometry geom);

	/**
	 * @param styling
	 * @param geoms
	 */
	void render(Styling styling, Collection<Geometry> geoms);

	/**
	 * @param styling
	 * @param geom
	 */
	void render(Styling styling, Geometry geom);

}
