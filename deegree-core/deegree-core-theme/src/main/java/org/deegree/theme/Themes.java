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
package org.deegree.theme;

import static org.deegree.commons.utils.CollectionUtils.addAllUncontained;

import java.util.ArrayList;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.Layer;

/**
 * Utility methods for using themes.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Themes {

	/**
	 * @param t
	 * @return all layers contained in this theme and subthemes
	 */
	public static List<Layer> getAllLayers(Theme t) {
		List<Layer> list = new ArrayList<Layer>();
		if (t == null) {
			return list;
		}
		list.addAll(t.getLayers());
		for (Theme c : t.getThemes()) {
			list.addAll(getAllLayers(c));
		}
		return list;
	}

	/**
	 * @param t
	 * @return all themes contained in this theme and subthemes
	 */
	public static List<Theme> getAllThemes(Theme t) {
		List<Theme> list = new ArrayList<Theme>();
		list.addAll(t.getThemes());
		for (Theme c : t.getThemes()) {
			list.addAll(getAllThemes(c));
		}
		return list;
	}

	private static Envelope aggregateFromLayer(Layer l, Envelope env, List<ICRS> crs) {
		SpatialMetadata smd = l.getMetadata().getSpatialMetadata();
		if (smd == null) {
			return env;
		}
		if (smd.getEnvelope() != null) {
			if (env == null) {
				env = smd.getEnvelope();
			}
			else {
				env = env.merge(smd.getEnvelope());
			}
		}
		if (smd.getCoordinateSystems() != null) {
			addAllUncontained(crs, smd.getCoordinateSystems());
		}
		return env;
	}

	private static Envelope aggregateFromTheme(Theme t, Envelope env, List<ICRS> crs) {
		aggregateSpatialMetadata(t);
		SpatialMetadata smd = t.getLayerMetadata().getSpatialMetadata();
		if (smd.getEnvelope() != null) {
			if (env == null) {
				env = smd.getEnvelope();
			}
			else {
				env = env.merge(smd.getEnvelope());
			}
		}
		if (smd.getCoordinateSystems() != null) {
			addAllUncontained(crs, smd.getCoordinateSystems());
		}
		return env;
	}

	public static void aggregateSpatialMetadata(Theme theme) {
		// TODO price question is, bottom up or top down inheritance? Possibly a combined
		// approach is desirable (top
		// down inheritance for configured theme values, bottom up for envelopes from
		// layers or so)
		SpatialMetadata curSmd = theme.getLayerMetadata().getSpatialMetadata();
		Envelope env = curSmd.getEnvelope();
		List<ICRS> crs = new ArrayList<ICRS>();
		if (curSmd.getCoordinateSystems() != null) {
			crs.addAll(curSmd.getCoordinateSystems());
		}
		for (Theme t : theme.getThemes()) {
			env = aggregateFromTheme(t, env, crs);
		}
		for (Layer l : theme.getLayers()) {
			env = aggregateFromLayer(l, env, crs);
		}
		theme.getLayerMetadata().setSpatialMetadata(new SpatialMetadata(env, crs));
	}

}
