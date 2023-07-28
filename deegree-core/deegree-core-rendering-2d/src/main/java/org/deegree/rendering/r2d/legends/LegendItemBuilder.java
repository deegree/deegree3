/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.rendering.r2d.legends;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.rendering.r2d.Java2DRasterRenderer;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.style.se.unevaluated.Continuation;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.se.unevaluated.Symbolizer;
import org.deegree.style.styling.RasterStyling;
import org.deegree.style.styling.Styling;

/**
 * Builds legend items.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class LegendItemBuilder {

	static List<LegendItem> prepareLegend(Style style, Java2DRenderer renderer, Java2DTextRenderer textRenderer,
			Java2DRasterRenderer rasterRenderer) {
		List<LegendItem> items = new LinkedList<LegendItem>();
		LinkedList<Class<?>> ruleTypes = style.getRuleTypes();
		Iterator<Class<?>> types = ruleTypes.iterator();
		LinkedList<String> ruleTitles = style.getRuleTitles();
		Iterator<String> titles = ruleTitles.iterator();
		LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> rules;
		rules = new LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>>(style.getRules());
		Iterator<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> ruleIterator = rules.iterator();
		ArrayList<LinkedList<Styling>> bases = style.getBases();

		for (LinkedList<Styling> styles : bases) {
			boolean raster = false;
			for (Styling s : styles) {
				if (s instanceof RasterStyling) {
					items.add(new RasterLegendItem((RasterStyling) s, renderer, rasterRenderer, textRenderer));
					raster = true;
				}
			}
			if (!raster) {
				LegendItem item = new StandardLegendItem(styles, ruleIterator.next().first, types.next(), titles.next(),
						renderer, textRenderer);
				items.add(item);
			}
		}

		return items;
	}

}
