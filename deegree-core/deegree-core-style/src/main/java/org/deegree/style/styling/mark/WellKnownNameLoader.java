/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2022 by:
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.style.styling.mark;

import java.awt.Shape;
import java.net.URL;
import java.util.function.Function;

import org.deegree.style.styling.components.Mark;

/**
 * Loader for loading Mark from custom WellKnownName
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public interface WellKnownNameLoader {

	/**
	 * Parse a WellKnownName Text into a mark
	 * @param wellKnownName WellKnownName to be parsed
	 * @param resolver Resolver to resolve relative locations into URL, can be null
	 * @return The Shape or null if this Loader is not responsible for that type of
	 * WellKnownName
	 */
	public Shape parse(String wellKnownName, Function<String, URL> resolver);

	/**
	 * Apply the Shape to the Mark
	 * @param mark The Mark to be updated
	 * @param shape The previously created shape
	 */
	public default void apply(Mark mark, Shape shape) {
		mark.shape = shape;
	}

	/**
	 * Get order value for this Loader
	 *
	 * Used to sort multiple factories and create a order list of loader
	 * @return int of position in list
	 */
	public default int order() {
		return 1000;
	}

}