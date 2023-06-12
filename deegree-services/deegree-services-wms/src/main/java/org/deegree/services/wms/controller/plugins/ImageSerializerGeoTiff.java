/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
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

package org.deegree.services.wms.controller.plugins;

import static javax.imageio.ImageIO.write;
import static org.deegree.coverage.raster.geom.RasterGeoReference.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffWriter;
import org.deegree.rendering.r2d.ImageSerializer;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.slf4j.Logger;

/**
 * <code>ImageSerializerGeoTiff</code>
 *
 * @author <a href="mailto:niklasch@grit.de">Sebastian Niklasch</a>
 */

public class ImageSerializerGeoTiff implements ImageSerializer {

	private static final Logger LOG = getLogger(ImageSerializerGeoTiff.class);

	private String formatName = "tiff";

	@Override
	public void serialize(RenderingInfo rinfo, BufferedImage img, OutputStream out) throws IOException {
		if (rinfo != null && rinfo.getEnvelope() != null) {
			long ts, te;
			RasterGeoReference geoRef = create(OriginLocation.OUTER, rinfo.getEnvelope(), img.getWidth(),
					img.getHeight());

			ts = System.currentTimeMillis();
			GeoTiffWriter.save(img, geoRef, out);
			te = System.currentTimeMillis();

			LOG.debug("Encoding into {} duration {} ms", formatName, te - ts);
		}
		else {
			LOG.debug("Rendering without spatial information, because no envelope is availible. Using ImageIO");
			write(img, formatName, out);
		}
	}

}
