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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.rendering.r2d.legends;

import static java.awt.Font.PLAIN;
import static java.lang.Math.max;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;

import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.style.se.unevaluated.Continuation;
import org.deegree.style.se.unevaluated.Symbolizer;
import org.deegree.style.styling.Styling;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class StandardLegendItem implements LegendItem {

	private String text;

	private StandardLegendRenderer renderer;

	public StandardLegendItem(LinkedList<Styling> stylings, Continuation<LinkedList<Symbolizer<?>>> rule,
			Class<?> ruleType, String text, Renderer renderer, TextRenderer textRenderer) {
		this.text = text;
		this.renderer = new StandardLegendRenderer(ruleType, stylings, text, renderer, textRenderer, rule);
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public void paint(int origin, LegendOptions opts) {
		renderer.paint(origin, opts);
	}

	@Override
	public int getMaxWidth(LegendOptions opts) {
		int res = 2 * opts.spacing + opts.baseWidth;

		Font font = new Font("Arial", PLAIN, opts.textSize);

		if (text != null && text.length() > 0) {
			TextLayout layout = new TextLayout(text, font, new FontRenderContext(new AffineTransform(), true, false));
			res = (int) max(layout.getBounds().getWidth() + (2 * opts.baseWidth), res);
		}
		return res;
	}

}
