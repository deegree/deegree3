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

package org.deegree.style.styling.components;

import org.deegree.style.styling.Copyable;

import java.util.LinkedList;
import java.util.List;

import static org.deegree.style.styling.components.Font.Style.NORMAL;

/**
 * <code>Font</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Font implements Copyable<Font> {

	/**
	 * Default is empty list.
	 */
	public List<String> fontFamily = new LinkedList<String>();

	/**
	 * Default is NORMAL.
	 */
	public Style fontStyle = NORMAL;

	/**
	 * Default is false.
	 */
	public boolean bold;

	/**
	 * Default is 10.
	 */
	public double fontSize = 10;

	/**
	 * Default no strikethrough
	 */
	public boolean strikethrough = false;

	/**
	 * Default strikethrough offset/position of 0.25 of text height (deegree specific)
	 *
	 * default values are base on Font defaults defined in:
	 * @see java.awt.Font#defaultLineMetrics(java.awt.font.FontRenderContext)
	 * @see sun.font.Font2D#getStyleMetrics(float, float[], int)
	 */
	@SuppressWarnings("JavadocReference")
	public double strikethroughOffset = 0.25d;

	/**
	 * Default strikethrough width multiplied with font-size (deegree specific)
	 *
	 * default values are base on Font defaults defined in:
	 * @see java.awt.Font#defaultLineMetrics(java.awt.font.FontRenderContext)
	 * @see sun.font.Font2D#getStyleMetrics(float, float[], int)
	 */
	@SuppressWarnings("JavadocReference")
	public double strikethroughWidth = 1.0 / 12.0;

	/**
	 * Default no underline
	 */
	public boolean underline = false;

	/**
	 * Default underline offset width multiplied with font-size (deegree specific)
	 *
	 * default values are base on Font defaults defined in:
	 * @see java.awt.Font#defaultLineMetrics(java.awt.font.FontRenderContext)
	 * @see sun.font.Font2D#getStyleMetrics(float, float[], int)
	 */
	@SuppressWarnings("JavadocReference")
	public double underlineOffset = 1.0 / 18.0;

	/**
	 * Default underline width multiplied with font-size (deegree specific) * default
	 * values are base on Font defaults defined in:
	 * @see java.awt.Font#defaultLineMetrics(java.awt.font.FontRenderContext)
	 * @see sun.font.Font2D#getStyleMetrics(float, float[], int)
	 */
	@SuppressWarnings("JavadocReference")
	public double underlineWidth = 1.0 / 12.0;

	/**
	 * <code>Style</code>
	 *
	 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
	 */
	public static enum Style {

		/** * */
		OBLIQUE,
		/** * */
		ITALIC,
		/** * */
		NORMAL

	}

	@Override
	public Font copy() {
		Font copy = new Font();
		copy.fontFamily.addAll(fontFamily);
		copy.fontStyle = fontStyle;
		copy.bold = bold;
		copy.fontSize = fontSize;
		copy.strikethrough = strikethrough;
		copy.strikethroughWidth = strikethroughWidth;
		copy.strikethroughOffset = strikethroughOffset;
		copy.underline = underline;
		copy.underlineWidth = underlineOffset;
		copy.underlineOffset = underlineWidth;
		return copy;
	}

}
