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

import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;

import org.deegree.style.styling.components.Mark;
import org.deegree.style.styling.components.Mark.SimpleMark;
import org.deegree.workspace.Initializable;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WellKnownNameManager implements Initializable {

	private static final Logger LOG = LoggerFactory.getLogger(WellKnownNameManager.class);

	private static ServiceLoader<WellKnownNameLoader> wellLnownNameLoader;

	private static List<WellKnownNameLoader> loaders;

	@Override
	public void init(Workspace ws) {
		wellLnownNameLoader = ServiceLoader.load(WellKnownNameLoader.class, ws.getModuleClassLoader());
	}

	private static synchronized void check() {
		if (loaders == null) {
			loaders = new ArrayList<>();
			try {
				for (WellKnownNameLoader loader : wellLnownNameLoader) {
					LOG.debug("Laoading MarkLoader {} [Order: {}]", loader.getClass(), loader.order());
					loaders.add(loader);
				}
				Collections.sort(loaders, comparingInt(WellKnownNameLoader::order));
			}
			catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public static boolean load(Mark mark, String wellKnownName, Function<String, URL> resolver) {
		try {
			// workaround for static marks
			mark.wellKnown = SimpleMark.valueOf(wellKnownName.toUpperCase());
			return true;
		}
		catch (IllegalArgumentException e) {
			LOG.trace("Could not Load wellKnownName as SimpleMark: {}", e.getMessage());
		}

		check();
		requireNonNull(loaders, "MarkFactory has to been initialized from Workspace");

		Rectangle2D bounds = null;
		String wkn;
		int boundsStart = wellKnownName.lastIndexOf('[');
		if (boundsStart == -1) {
			wkn = wellKnownName;
		}
		else {
			wkn = wellKnownName.substring(0, boundsStart);
			bounds = handleBounds(wellKnownName, boundsStart);
		}

		Shape s = null;
		for (WellKnownNameLoader loader : loaders) {
			s = loader.parse(wkn, resolver);
			if (s != null) {
				if (bounds != null) {
					loader.apply(mark, BoundedShape.of(s, bounds));
				}
				else {
					loader.apply(mark, s);
				}
				return true;
			}
		}

		return false;
	}

	private static Rectangle2D handleBounds(String wellKnownName, int boundsStart) {
		try {
			int boundsEnd = wellKnownName.indexOf(']', boundsStart);
			if (boundsEnd != -1) {
				double[] vals = splitAsDoubles(wellKnownName.substring(boundsStart + 1, boundsEnd), ",");
				if (vals.length == 2) {
					return new Rectangle2D.Double(0.0, 0.0, vals[0], vals[1]);
				}
				else if (vals.length == 4) {
					return new Rectangle2D.Double(vals[0], vals[1], vals[2], vals[3]);
				}
				else {
					throw new InvalidParameterException("Invalid number of ordinates specified");
				}
			}
			else {
				throw new InvalidParameterException("Invalid Format");
			}
		}
		catch (Exception ex) {
			LOG.warn("Invalid bounds specified, either use [widht,heigt] or [minx,miny,widht,height].");
			LOG.warn("Bounds are ignored for WellKnownName {} error: {}", wellKnownName, ex.getMessage());
			LOG.trace("Exception", ex);
			// ignore bounds
		}
		return null;
	}

}