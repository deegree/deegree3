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
package org.deegree.services.wps.provider.style;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static org.deegree.services.wps.provider.style.StyleProcessProvider.IN_PARAM_ID;
import static org.deegree.services.wps.provider.style.StyleProcessProvider.OUT_PARAM_ID;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.Pair;
import org.deegree.rendering.r2d.legends.Legends;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.PNGEncodeParam;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StyleProcesslet implements Processlet {

	private static final Logger LOG = LoggerFactory.getLogger(StyleProcesslet.class);

	@Override
	public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info)
			throws ProcessletException {
		ComplexInput input = (ComplexInput) in.getParameter(IN_PARAM_ID);
		try {
			XMLStreamReader sld = input.getValueAsXMLStream();

			SymbologyParser symbologyParser = SymbologyParser.INSTANCE;
			Style parsedStyle = symbologyParser.parse(sld);
			if (parsedStyle == null) {
				String msg = "Could not parse value of parameter " + IN_PARAM_ID + " as sld.";
				LOG.debug(msg);
				throw new ProcessletException(msg);
			}

			Legends legends = new Legends();
			Pair<Integer, Integer> legendSize = legends.getLegendSize(parsedStyle);
			BufferedImage img = new BufferedImage(legendSize.first, legendSize.second, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
			g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
			legends.paintLegend(parsedStyle, legendSize.first, legendSize.second, g);
			g.dispose();

			ComplexOutput output = (ComplexOutput) out.getParameter(OUT_PARAM_ID);
			OutputStream os = output.getBinaryOutputStream();
			PNGEncodeParam encodeParam = PNGEncodeParam.getDefaultEncodeParam(img);
			if (encodeParam instanceof PNGEncodeParam.Palette) {
				PNGEncodeParam.Palette p = (PNGEncodeParam.Palette) encodeParam;
				byte[] b = new byte[] { -127 };
				p.setPaletteTransparency(b);
			}
			com.sun.media.jai.codec.ImageEncoder encoder = ImageCodec.createImageEncoder("PNG", os, encodeParam);
			encoder.encode(img.getData(), img.getColorModel());
		}
		catch (Exception e) {
			LOG.error("Could not create legend.", e);
			throw new ProcessletException(e.getMessage());
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
	}

}
