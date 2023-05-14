/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.ows;

import static java.lang.Double.parseDouble;
import static org.deegree.cs.persistence.CRSManager.getCRSRef;

import java.util.ArrayList;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;

/**
 * Parser for KVP-encoded OWS Common constructs.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class OWSCommonKVPAdapter {

	private final static GeometryFactory geomFac = new GeometryFactory();

	/**
	 * Parses the given KVP parameter as an {@link Envelope} (according to OWS 1.1.0,
	 * 10.2.3).
	 * @param bboxStr encoded bounding box, must not be <code>null</code>
	 * @param defaultCrs crs to use when not explicitly encoded, may be <code>null</code>
	 * @return decoded {@link Envelope}, never <code>null</code>
	 */
	public static Envelope parseBBox(final String bboxStr, final ICRS defaultCrs) {
		final String[] tokens = bboxStr.split(",");
		final int n = tokens.length / 2;
		final List<Double> lowerCorner = new ArrayList<Double>(n);
		for (int i = 0; i < n; i++) {
			lowerCorner.add(parseDouble(tokens[i]));
		}
		final List<Double> upperCorner = new ArrayList<Double>(n);
		for (int i = n; i < 2 * n; i++) {
			upperCorner.add(parseDouble(tokens[i]));
		}
		final ICRS bboxCrs = getEncodedCrs(tokens);
		final ICRS crs = bboxCrs != null ? bboxCrs : defaultCrs;
		return geomFac.createEnvelope(lowerCorner, upperCorner, crs);
	}

	private static ICRS getEncodedCrs(final String[] tokens) {
		if (tokens.length % 2 == 1) {
			return getCRSRef(tokens[tokens.length - 1]);
		}
		return null;
	}

}
