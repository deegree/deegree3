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

import static org.deegree.coverage.raster.data.info.BandType.BAND_0;
import static org.deegree.coverage.raster.data.info.DataType.FLOAT;
import static org.deegree.coverage.raster.data.info.InterleaveType.PIXEL;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import static org.deegree.coverage.raster.utils.RasterFactory.createEmptyRaster;
import static org.deegree.rendering.r2d.legends.Legends.paintLegendText;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;

import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.rendering.r2d.RasterRenderer;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.RasterStyling;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.components.Fill;

/**
 * Renders raster legend items.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class RasterLegendRenderer {

	private static final ICRS mapcs = CRSManager.getCRSRef("CRS:1");

	private RasterStyling styling;

	private GeometryFactory geofac = new GeometryFactory();

	private Renderer renderer;

	private RasterRenderer rasterRenderer;

	private TextRenderer textRenderer;

	private LinkedList<String> texts;

	RasterLegendRenderer(RasterStyling styling, Renderer renderer, RasterRenderer rasterRenderer,
			TextRenderer textRenderer, LinkedList<String> texts) {
		this.styling = styling;
		this.renderer = renderer;
		this.rasterRenderer = rasterRenderer;
		this.textRenderer = textRenderer;
		this.texts = texts;
	}

	void paint(int origin, LegendOptions opts) {
		if (styling.interpolate != null) {
			Double[] datas = styling.interpolate.getDatas();
			int rasterHeight = (datas.length - 1) * (opts.baseHeight + 2 * opts.spacing);
			RasterDataInfo info = new RasterDataInfo(new BandType[] { BAND_0 }, FLOAT, PIXEL);
			int miny = origin - rasterHeight - opts.baseHeight / 2 - opts.spacing;
			int maxy = origin - opts.baseHeight / 2 - opts.spacing;
			Envelope bbox = geofac.createEnvelope(opts.spacing, miny, opts.baseWidth + opts.spacing, maxy, mapcs);
			RasterGeoReference rref = new RasterGeoReference(OUTER, 1, 1, opts.spacing, miny, mapcs);
			SimpleRaster raster = createEmptyRaster(info, bbox, rref);

			RasterData rasterData = raster.getRasterData();

			int row = 0;
			for (int i = 0; i < datas.length - 1; ++i) {
				double a = datas[i], b = datas[i + 1];
				double res = (b - a) / (opts.baseHeight + 2 * opts.spacing);
				for (int y = 0; y < opts.baseHeight + 2 * opts.spacing; ++y) {
					for (int k = 0; k < opts.baseWidth; ++k) {
						rasterData.setFloatSample(k, row, 0, (float) (a + res * y));
					}
					++row;
				}
			}

			rasterRenderer.render(styling, raster);

			for (String text : texts) {
				paintLegendText(origin, opts, text, textRenderer);
				origin -= opts.baseHeight + 2 * opts.spacing;
			}
		}
		if (styling.categorize != null) {
			Iterator<String> texts = this.texts.iterator();
			for (Color c : styling.categorize.getColors()) {
				PolygonStyling s = new PolygonStyling();
				s.fill = new Fill();
				s.fill.color = c;
				LinkedList<Styling> list = new LinkedList<Styling>();
				list.add(s);
				StandardLegendItem item = new StandardLegendItem(list, null, Polygon.class, texts.next(), renderer,
						textRenderer);
				item.paint(origin, opts);
				origin -= opts.baseHeight + 2 * opts.spacing;
			}
		}
	}

}
