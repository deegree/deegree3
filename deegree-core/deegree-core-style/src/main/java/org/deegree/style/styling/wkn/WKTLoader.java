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
package org.deegree.style.styling.wkn;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.function.Function;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.EWKTReader;
import org.deegree.style.styling.mark.WellKnownNameLoader;
import org.deegree.style.styling.wkn.shape.ShapeConverterArc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.io.ParseException;

public class WKTLoader implements WellKnownNameLoader {

	private static final Logger LOG = LoggerFactory.getLogger(WKTLoader.class);

	public static final String PREFIX = "wkt://";

	@Override
	public Shape parse(String wellKnownName, Function<String, URL> resolver) {
		if (wellKnownName == null || !wellKnownName.startsWith(PREFIX))
			return null;

		String wkn = wellKnownName.substring(PREFIX.length());
		Shape s = null;
		try {
			EWKTReader reader = new EWKTReader();
			Geometry geom = reader.read(wkn);

			ShapeConverterArc converter = new ShapeConverterArc();

			Shape orig = converter.convert(geom);
			AffineTransform at = AffineTransform.getScaleInstance(1.0, -1.0);
			s = at.createTransformedShape(orig);
		}
		catch (ParseException ex) {
			LOG.warn("Could not Parse WKT {}: {}", wkn, ex.getMessage());
			LOG.trace("Exception", ex);
		}

		return s;
	}

}
