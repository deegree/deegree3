/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.geometry.metadata;

import static org.deegree.cs.coordinatesystems.GeographicCRS.WGS84;

import java.util.ArrayList;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.metadata.jaxb.EnvelopeType;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class SpatialMetadataConverter {

	/**
	 * @param env may be null
	 * @param crs should be a list of whitespace separated identifiers, may be null
	 */
	public static SpatialMetadata fromJaxb(EnvelopeType env, String crs) {
		Envelope envelope = fromJaxb(env);
		List<ICRS> list = fromJaxb(crs);
		return new SpatialMetadata(envelope, list);
	}

	/**
	 * @param crs should be a list of whitespace separated identifiers, may be null
	 */
	public static List<ICRS> fromJaxb(String crs) {
		ArrayList<ICRS> list = new ArrayList<ICRS>();
		if (crs == null) {
			return list;
		}

		for (String c : crs.split("\\s")) {
			if (!c.isEmpty()) {
				list.add(CRSManager.getCRSRef(c));
			}
		}

		return list;
	}

	/**
	 * @param env may be null
	 * @return null, if env is null
	 */
	public static Envelope fromJaxb(EnvelopeType env) {
		Envelope envelope = null;

		if (env != null) {
			String envCrs = env.getCrs();
			ICRS crs;
			if (envCrs == null) {
				crs = CRSManager.getCRSRef(WGS84);
			}
			else {
				crs = CRSManager.getCRSRef(envCrs);
			}

			Double[] points = env.getLowerCorner().toArray(new Double[] {});
			double[] min = new double[points.length];
			for (int i = 0; i < min.length; ++i) {
				min[i] = points[i];
			}
			points = env.getUpperCorner().toArray(new Double[] {});
			double[] max = new double[points.length];
			for (int i = 0; i < max.length; ++i) {
				max[i] = points[i];
			}
			envelope = new GeometryFactory().createEnvelope(min, max, crs);
		}

		return envelope;
	}

}
