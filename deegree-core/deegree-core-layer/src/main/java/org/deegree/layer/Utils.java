/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.layer;

import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static org.deegree.cs.coordinatesystems.GeographicCRS.WGS84;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.deegree.commons.utils.MapUtils;
import org.deegree.cs.configuration.wkt.WKTParser;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.slf4j.Logger;

/**
 * <code>Utils</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Utils {

	private static final Logger LOG = getLogger(Utils.class);

	private static ICRS getAuto42001(double lon0, double lat0) {
		double zone = min(floor((lon0 + 180.0) / 6.0) + 1, 60);
		double central_meridian = -183.0 + zone * 6.0;
		double false_northing = (lat0 >= 0.0) ? 0.0 : 10000000.0;

		String wkt = "PROJCS[\"WGS 84 / Auto UTM\",";
		wkt += "    GEOGCS[\"WGS 84\",";
		wkt += "        DATUM[\"WGS_1984\",";
		wkt += "        SPHEROID[\"WGS_1984\", 6378137, 298.257223563]],";
		wkt += "        PRIMEM[\"Greenwich\", 0],";
		wkt += "        UNIT[\"Decimal_Degree\", 0.0174532925199433]],";
		wkt += "    PROJECTION[\"Transverse_Mercator\"],";
		wkt += "    PARAMETER[\"Central_Meridian\", " + central_meridian + "],";
		wkt += "    PARAMETER[\"Latitude_of_Origin\", 0],";
		wkt += "    PARAMETER[\"False_Easting\", 500000],";
		wkt += "    PARAMETER[\"False_Northing\", " + false_northing + "],";
		wkt += "    PARAMETER[\"Scale_Factor\", 0.9996],";
		wkt += "    UNIT[\"Meter\", 1]]";

		LOG.debug("Generated wkt: {}", wkt);

		try {
			return CRSManager.getCRSRef(WKTParser.parse(wkt));
		}
		catch (IOException e) {
			LOG.debug("Unknown error", e);
		}
		return null;
	}

	private static ICRS getAuto42002(double lon0, double lat0) {
		double central_meridian = lon0;
		double false_northing = (lat0 >= 0.0) ? 0.0 : 10000000.0;

		String wkt = "PROJCS[\"WGS 84 / Auto Tr. Mercator\",";
		wkt += "    GEOGCS[\"WGS 84\",";
		wkt += "        DATUM[\"WGS_1984\",";
		wkt += "        SPHEROID[\"WGS_1984\", 6378137, 298.257223563]],";
		wkt += "        PRIMEM[\"Greenwich\", 0],";
		wkt += "        UNIT[\"Decimal_Degree\", 0.0174532925199433]],";
		wkt += "    PROJECTION[\"Transverse_Mercator\"],";
		wkt += "    PARAMETER[\"Central_Meridian\", " + central_meridian + "],";
		wkt += "    PARAMETER[\"Latitude_of_Origin\", 0],";
		wkt += "    PARAMETER[\"False_Easting\", 500000],";
		wkt += "    PARAMETER[\"False_Northing\", " + false_northing + "],";
		wkt += "    PARAMETER[\"Scale_Factor\", 0.9996],";
		wkt += "    UNIT[\"Meter\", 1]]";

		LOG.debug("Generated wkt: {}", wkt);

		try {
			return CRSManager.getCRSRef(WKTParser.parse(wkt));
		}
		catch (IOException e) {
			LOG.debug("Unknown error", e);
		}
		return null;
	}

	private static ICRS getAuto42003(double lon0, double lat0) {
		double central_meridian = lon0;
		double latitude_of_origin = lat0;

		String wkt = "PROJCS[\"WGS 84 / Auto Orthographic\",";
		wkt += "    GEOGCS[\"WGS 84\",";
		wkt += "        DATUM[\"WGS_1984\",";
		wkt += "        SPHEROID[\"WGS_1984\", 6378137, 298.257223563]],";
		wkt += "        PRIMEM[\"Greenwich\", 0],";
		wkt += "        UNIT[\"Decimal_Degree\", 0.0174532925199433]],";
		wkt += "    PROJECTION[\"Orthographic\"],";
		wkt += "    PARAMETER[\"Central_Meridian\", " + central_meridian + "],";
		wkt += "    PARAMETER[\"Latitude_of_Origin\", " + latitude_of_origin + "],";
		wkt += "    UNIT[\"Meter\", 1]]";

		LOG.debug("Generated wkt: {}", wkt);

		try {
			return CRSManager.getCRSRef(WKTParser.parse(wkt));
		}
		catch (IOException e) {
			LOG.debug("Unknown error", e);
		}
		return null;
	}

	private static ICRS getAuto42004(double lon0, double lat0) {
		double central_meridian = lon0;
		double standard_parallel = lat0;

		String wkt = "PROJCS[\"WGS 84 / Auto Equirectangular\",";
		wkt += "    GEOGCS[\"WGS 84\",";
		wkt += "        DATUM[\"WGS_1984\",";
		wkt += "        SPHEROID[\"WGS_1984\", 6378137, 298.257223563]],";
		wkt += "        PRIMEM[\"Greenwich\", 0],";
		wkt += "        UNIT[\"Decimal_Degree\", 0.0174532925199433]],";
		wkt += "    PROJECTION[\"Equirectangular\"],";
		wkt += "    PARAMETER[\"Central_Meridian\", " + central_meridian + "],";
		wkt += "    PARAMETER[\"Latitude_of_Origin\", 0],";
		wkt += "    PARAMETER[\"Standard_Parallel_1\", " + standard_parallel + "],";
		wkt += "    UNIT[\"Meter\", 1]]";

		LOG.debug("Generated wkt: {}", wkt);

		try {
			return CRSManager.getCRSRef(WKTParser.parse(wkt));
		}
		catch (IOException e) {
			LOG.debug("Unknown error", e);
		}
		return null;
	}

	private static ICRS getAuto42005(double lon0) {
		double central_meridian = lon0;

		String wkt = "PROJCS[\"WGS 84 / Auto Mollweide\",";
		wkt += "    GEOGCS[\"WGS 84\",";
		wkt += "        DATUM[\"WGS_1984\",";
		wkt += "        SPHEROID[\"WGS_1984\", 6378137, 298.257223563]],";
		wkt += "        PRIMEM[\"Greenwich\", 0],";
		wkt += "        UNIT[\"Decimal_Degree\", 0.0174532925199433]],";
		wkt += "    PROJECTION[\"Mollweide\"],";
		wkt += "    PARAMETER[\"Central_Meridian\", " + central_meridian + "],";
		wkt += "    UNIT[\"Meter\", 1]]";

		LOG.debug("Generated wkt: {}", wkt);

		try {
			return CRSManager.getCRSRef(WKTParser.parse(wkt));
		}
		catch (IOException e) {
			LOG.debug("Unknown error", e);
		}
		return null;
	}

	/**
	 * @param id
	 * @param lon0
	 * @param lat0
	 * @return the corresponding auto generated crs
	 */
	public static ICRS getAutoCRS(int id, double lon0, double lat0) {
		switch (id) {
			case 42001: {
				return getAuto42001(lon0, lat0);
			}
			case 42002: {
				return getAuto42002(lon0, lat0);
			}
			case 42003: {
				return getAuto42003(lon0, lat0);
			}
			case 42004: {
				return getAuto42004(lon0, lat0);
			}
			case 42005: {
				return getAuto42005(lon0);
			}
			default:
				break;
		}

		return null;
	}

	/**
	 * @param mapWidth
	 * @param mapHeight
	 * @param bbox
	 * @param crs
	 * @return the WMS 1.1.1 scale (size of the diagonal pixel)
	 */
	public static double calcScaleWMS111(int mapWidth, int mapHeight, Envelope bbox, ICRS crs) {
		if (mapWidth == 0 || mapHeight == 0) {
			return 0;
		}
		double scale = 0;

		if (crs == null) {
			throw new RuntimeException("Invalid null crs.");
		}

		if ("m".equalsIgnoreCase(crs.getAxis()[0].getUnits().toString())) {
			/*
			 * this method to calculate a maps scale as defined in OGC WMS and SLD
			 * specification is not required for maps having a projected reference system.
			 * Direct calculation of scale avoids uncertainties
			 */
			double dx = bbox.getSpan0() / mapWidth;
			double dy = bbox.getSpan1() / mapHeight;
			scale = sqrt(dx * dx + dy * dy);
		}
		else {

			if (!crs.equals(WGS84)) {
				// transform the bounding box of the request to EPSG:4326
				GeometryTransformer trans = new GeometryTransformer(WGS84);
				try {
					bbox = trans.transform(bbox, crs);
				}
				catch (IllegalArgumentException e) {
					LOG.error("Unknown error", e);
				}
				catch (TransformationException e) {
					LOG.error("Unknown error", e);
				}
			}
			double dx = bbox.getSpan0() / mapWidth;
			double dy = bbox.getSpan1() / mapHeight;
			double minx = bbox.getMin().get0() + dx * (mapWidth / 2d - 1);
			double miny = bbox.getMin().get1() + dy * (mapHeight / 2d - 1);
			double maxx = bbox.getMin().get0() + dx * (mapWidth / 2d);
			double maxy = bbox.getMin().get1() + dy * (mapHeight / 2d);

			double distance = MapUtils.calcDistance(minx, miny, maxx, maxy);

			scale = distance / MapUtils.SQRT2;

		}

		return scale;
	}

	/**
	 * @param mapWidth
	 * @param mapHeight
	 * @param bbox
	 * @param crs
	 * @return the WMS 1.3.0 scale (horizontal size of the pixel, pixel size == 0.28mm)
	 */
	public static double calcScaleWMS130(int mapWidth, int mapHeight, Envelope bbox, ICRS crs) {
		if (mapWidth == 0 || mapHeight == 0) {
			return 0;
		}

		double scale = 0;

		if (crs == null) {
			throw new RuntimeException("Invalid crs: " + crs);
		}

		if ("m".equalsIgnoreCase(crs.getAxis()[0].getUnits().toString())) {
			/*
			 * this method to calculate a maps scale as defined in OGC WMS and SLD
			 * specification is not required for maps having a projected reference system.
			 * Direct calculation of scale avoids uncertainties
			 */
			double dx = bbox.getSpan0() / mapWidth;
			scale = dx / MapUtils.DEFAULT_PIXEL_SIZE;
		}
		else {

			if (!crs.equals(WGS84)) {
				// transform the bounding box of the request to EPSG:4326
				GeometryTransformer trans = new GeometryTransformer(WGS84);
				try {
					bbox = trans.transform(bbox, crs);
				}
				catch (IllegalArgumentException e) {
					LOG.error("Unknown error", e);
				}
				catch (TransformationException e) {
					LOG.error("Unknown error", e);
				}
			}
			double dx = bbox.getSpan0() / mapWidth;
			double dy = bbox.getSpan1() / mapHeight;

			double minx = bbox.getMin().get0() + dx * (mapWidth / 2d - 1);
			double miny = bbox.getMin().get1() + dy * (mapHeight / 2d - 1);
			double maxx = bbox.getMin().get0() + dx * (mapWidth / 2d);
			double maxy = bbox.getMin().get1() + dy * (mapHeight / 2d - 1);

			double distance = MapUtils.calcDistance(minx, miny, maxx, maxy);

			scale = distance / MapUtils.SQRT2 / MapUtils.DEFAULT_PIXEL_SIZE;

		}

		return scale;
	}

	/**
	 * @param env
	 * @param width
	 * @param height
	 * @return max(resx, resy)
	 */
	public static double calcResolution(Envelope env, int width, int height) {
		return max(env.getSpan0() / width, env.getSpan1() / height);
	}

}
