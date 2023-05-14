/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Point;
import org.deegree.style.styling.TextStyling;
import org.deegree.rendering.r2d.Label;

/**
 * <code>LabelRenderer</code>
 *
 * @author Florian Bingel
 */
public interface LabelRenderer {

	void createLabel(TextStyling styling, String text, Collection<Geometry> geoms);

	void createLabel(TextStyling styling, String text, Geometry geom);

	/**
	 * Render a text styling with a string and a geometry.
	 * @param styling
	 * @param text
	 * @param geom
	 */
	Label createLabel(TextStyling styling, Font font, String text, Point p);

	/**
	 * Render a text styling with a string and a geometry.
	 * @param styling
	 * @param text
	 * @param geom
	 */
	void render(List<Label> pLables);

	void render(Label label);

	void render();

	List<Label> getLabels();

}
