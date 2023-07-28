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

package org.deegree.rendering.r2d;

import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.deegree.commons.utils.TunableParameter;
import org.deegree.style.styling.components.Graphic;
import org.slf4j.Logger;

/**
 * Renders svg images onto buffered images.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class SvgRenderer {

	private static final Logger LOG = getLogger(SvgRenderer.class);

	private final int cacheSize = TunableParameter.get("deegree.cache.svgrenderer", 256);

	final LinkedHashMap<String, BufferedImage> svgCache = new LinkedHashMap<>(cacheSize) {
		private static final long serialVersionUID = -6847956873232942891L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
			return size() > cacheSize;
		}
	};

	BufferedImage prepareSvg(Rectangle2D.Double rect, Graphic g) {
		BufferedImage img = null;
		final String cacheKey = createCacheKey(g.imageURL, rect.width, rect.height);
		if (svgCache.containsKey(cacheKey)) {
			img = svgCache.get(cacheKey);
		}
		else {
			SvgImageTranscoder t = new SvgImageTranscoder();

			if (rect.width > 0.0d) {
				t.addTranscodingHint(KEY_WIDTH, new Float(rect.width));
			}
			if (rect.height > 0.0d) {
				t.addTranscodingHint(KEY_HEIGHT, new Float(rect.height));
			}

			TranscoderInput input = new TranscoderInput(g.imageURL);
			SvgImageTranscoder.SvgImageOutput output = t.createOutput();

			try {
				t.transcode(input, output);
				img = output.getBufferedImage();
				svgCache.put(cacheKey, img);
			}
			catch (TranscoderException e) {
				LOG.warn("Could not rasterize svg '{}': {}", g.imageURL, e.getLocalizedMessage());
			}
		}
		return img;
	}

	String createCacheKey(String url, double width, double height) {
		return String.format("%s_%d_%d", url, round(width), round(height));
	}

}
