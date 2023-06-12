/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/

 (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
----------------------------------------------------------------------------*/
package org.deegree.style.styling.wkn;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.style.styling.mark.BoundedShape;
import org.deegree.style.styling.mark.WellKnownNameLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrueTypeFontLoader implements WellKnownNameLoader {

	private static final Logger LOG = LoggerFactory.getLogger(TrueTypeFontLoader.class);

	private static final String PREFIX = "ttf://";

	private static final Map<URL, Font> EXTERNAL_FONT_CACHE = new HashMap<>();

	private static FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(new AffineTransform(), false, false);

	@Override
	public Shape parse(String wellKnownName, Function<String, URL> resolver) {
		if (wellKnownName == null || !wellKnownName.startsWith(PREFIX))
			return null;

		Matcher m = Pattern.compile("[tT][tT][fF]://(.*)#(.*)").matcher(wellKnownName);
		if (!m.matches()) {
			throw new IllegalArgumentException("Invalid WellKnownName, use syntax ttf://<fontName>#<charCode>");
		}

		String fontFamilyName = m.group(1);
		String code = m.group(2);
		char character;
		try {
			// see if a unicode escape sequence has been used
			if (code.startsWith("U+") || code.startsWith("\\u"))
				code = "0x" + code.substring(2);

			// this will handle most numeric formats like decimal, hex and octal
			character = (char) Integer.decode(code).intValue();
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid character code specificated " + code, e);
		}

		// Load Character
		Font font = load(fontFamilyName, resolver);

		// handle charmap code reporting issues
		if (!font.canDisplay(character)) {
			char alternative = (char) (0xF000 | character);
			if (font.canDisplay(alternative)) {
				character = alternative;
			}
		}

		GlyphVector textGlyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT, new char[] { (char) character });
		Shape s = textGlyphVector.getOutline();

		// have the shape be centered in the origin, and sitting in a square of side 1
		Rectangle2D bounds = s.getBounds2D();
		AffineTransform tx = new AffineTransform();
		double max = Math.max(bounds.getWidth(), bounds.getHeight());
		// all shapes are defined looking "upwards" (see ShapeMarkFactory or
		// WellKnownMarkFactory)
		// but the fonts ones are flipped to compensate for the fact the y coords grow
		// from top
		// to bottom on the screen. We have to flip the symbol so that it conforms to the
		// other marks convention
		tx.scale(1 / max, -1 / max);
		tx.translate(-bounds.getCenterX(), -bounds.getCenterY());

		return BoundedShape.inv(tx.createTransformedShape(s), new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0));
	}

	private Font load(String fontFamilyName, Function<String, URL> resolver) {
		Font f = null;
		if (fontFamilyName.endsWith(".ttf")) {
			// asume file and resolve as file
			URL url = resolver != null ? resolver.apply(fontFamilyName) : null;
			if (url == null) {
				LOG.warn("Font \"{}\" cloud not be found/resolved to URL", fontFamilyName);
				throw new IllegalArgumentException("Font \"" + fontFamilyName + "\" not found");
			}

			f = EXTERNAL_FONT_CACHE.get(url);

			try (InputStream is = url.openStream()) {
				f = Font.createFont(Font.TRUETYPE_FONT, is);
				EXTERNAL_FONT_CACHE.put(url, f);
			}
			catch (FontFormatException | IOException e) {
				LOG.warn("Font \"{}\" cloud not be loaded: {}", fontFamilyName, e.getMessage());
				throw new IllegalArgumentException("Font \"" + fontFamilyName + "\" could not be loaded");
			}
		}
		else {
			f = new Font(fontFamilyName, Font.PLAIN, 12);
		}

		return f;
	}

}