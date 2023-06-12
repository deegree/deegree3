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
package org.deegree.cs;

import static java.lang.Integer.parseInt;
import static org.deegree.commons.utils.MapUtils.calcDegreeResFromScale;
import static org.deegree.commons.utils.MapUtils.calcMetricResFromScale;
import static org.deegree.cs.components.Unit.DEGREE;
import static org.deegree.cs.components.Unit.METRE;
import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.cs.components.IUnit;
import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.slf4j.Logger;

/**
 * TODO: move this!
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class CRSUtils {

	private static final Logger LOG = getLogger(CRSUtils.class);

	/** The commonly used geographic 'EPSG:4326', with axis order X, Y. */
	public static final CRS EPSG_4326 = GeographicCRS.WGS84;

	/**
	 * Calculates the resolution (world units / pixel) for the given scale denominator (1
	 * / map scale) and coordinate reference system (determines the world units).
	 * @param scaleDenominator scale denominator (1 / map scale)
	 * @param crs coordinate reference system, must not be <code>null</code>
	 * @return resolution in world units per pixel
	 */
	public static double calcResolution(double scaleDenominator, ICRS crs) {
		IUnit units = crs.getAxis()[0].getUnits();
		return calcResolution(scaleDenominator, units);
	}

	/**
	 * Calculates the resolution (world units / pixel) for the given scale denominator (1
	 * / map scale) and unit system.
	 * @param scaleDenominator scale denominator (1 / map scale)
	 * @param units units, must not be <code>null</code>
	 * @return resolution in world units per pixel
	 */
	public static double calcResolution(double scaleDenominator, IUnit units) {
		if (units.equals(METRE)) {
			return calcMetricResFromScale(scaleDenominator);
		}
		else if (units.equals(DEGREE)) {
			return calcDegreeResFromScale(scaleDenominator);
		}
		String msg = "Unhandled unit type: " + units
				+ ". Conversion from scale denominator to resolution not implemented";
		throw new IllegalArgumentException(msg);
	}

	/**
	 * Retrieves an equivalent {@link ICRS} with authoritative axis ordering.
	 *
	 * NOTE: Due to the current state of the CRS database, this method is a hack. As soon
	 * as the CRS-DB has been sanitized by removing the custom XY-variants of EPSG-CRS, it
	 * is not required anymore and should be removed.
	 * @param crs CRS with authoritative or non-authoritative (forced XY) axis-ordering,
	 * must not be <code>null</code>
	 * @return equivalent CRS with authoritative axis ordering, never <code>null</code>
	 * @throws UnknownCRSException
	 */
	public static ICRS getAxisAwareCrs(final ICRS crs) throws UnknownCRSException {
		if (isAxisAware(crs)) {
			return crs;
		}
		for (final String crsString : crs.getOrignalCodeStrings()) {
			final String lowerCrsString = crsString.toLowerCase();
			if (lowerCrsString.startsWith("epsg:")) {
				final String epsgCode = lowerCrsString.substring(5);
				return getAxisAwareCrs(epsgCode);
			}
		}
		throw new RuntimeException("Unable to determine axis-aware CRS variant for " + crs.getAlias());
	}

	/**
	 * Determines whether the given {@link ICRS} has authoritative axis ordering.
	 *
	 * NOTE: Due to the current state of the CRS database, this method is a bit of a hack.
	 * As soon as the CRS-DB has been sanitized by removing the custom XY-variants of
	 * EPSG-CRS, it will not be required anymore and should be removed.
	 * @param crs CRS with authoritative or non-authoritative (forced XY) axis-ordering,
	 * must not be <code>null</code>
	 * @return <code>true</code>, if the given CRS uses authoritative axis ordering,
	 * <code>false</code> otherwise
	 * @throws UnknownCRSException
	 */
	public static boolean isAxisAware(final ICRS crs) throws UnknownCRSException {
		final String alias = crs.getAlias().toLowerCase();
		if (isUrnEpsgIdentifier(alias) || isOgcCrsIdentifier(alias)) {
			LOG.debug(alias + " is considered axis aware");
			return true;
		}
		for (final String crsString : crs.getOrignalCodeStrings()) {
			final String lowerCrsString = crsString.toLowerCase();
			if (isUrnEpsgIdentifier(lowerCrsString)) {
				LOG.debug(crs.getAlias() + " is considered axis aware");
				return true;
			}
			if (isOgcCrsIdentifier(lowerCrsString)) {
				LOG.debug(crs.getAlias() + " is considered axis aware");
				return true;
			}
		}
		LOG.debug(crs.getAlias() + " is not considered axis aware");
		return false;
	}

	private static boolean isUrnEpsgIdentifier(final String lowerCrsString) {
		return lowerCrsString.startsWith("urn:ogc:def:crs:epsg::");
	}

	private static boolean isOgcCrsIdentifier(final String lowerCrsString) {
		return lowerCrsString.startsWith("crs:");
	}

	public static ICRS getAxisAwareCrs(final String epsgCode) {
		final String identifierWithCorrectOrder = "urn:ogc:def:crs:epsg::" + epsgCode;
		return CRSManager.getCRSRef(identifierWithCorrectOrder);
	}

	public static final int getEpsgCode(final ICRS crs) {
		for (final String crsString : crs.getOrignalCodeStrings()) {
			final String lowerCrsString = crsString.toLowerCase();
			if (lowerCrsString.contains("epsg:")) {
				return Integer.parseInt(lowerCrsString.substring(lowerCrsString.lastIndexOf(":") + 1));
			}
		}
		throw new IllegalArgumentException("Unable to determine EPSG code for " + crs.getAlias());
	}

}
