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
package org.deegree.cs.transformations;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test the accurancy of transformation from ETRS89 (epsg:4258) to the dutch RD CRS
 * (epsg:28992) and back.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class TransformationETRS89RDTest extends TransformationAccuracy {

	private static Logger LOG = LoggerFactory.getLogger(TransformationAccuracy.class);

	private static Map<String, Pair<Point3d, Point3d>> refPoints;

	// TODO: accurancy!!!
	/** the epsilon for meter based crs */
	public final static Point3d EPSILON_M = new Point3d(0.22, 0.22, 0.4);

	@BeforeClass
	public static void init() {
		// reference points from http://www.06-gps.nl (from 2011-03-01)
		refPoints = new HashMap<String, Pair<Point3d, Point3d>>();
		refPoints.put("Aachen        ", new Pair<Point3d, Point3d>(new Point3d(6.0884131056, 50.7679193333, 263.2272),
				new Point3d(204471.1828, 308901.5450, Double.NaN)));
		refPoints.put("Almere        ", new Pair<Point3d, Point3d>(new Point3d(5.2223736917, 52.3713968500, 87.8436),
				new Point3d(143774.7151, 487070.4229, Double.NaN)));
		refPoints.put("Alphen ad Rijn", new Pair<Point3d, Point3d>(new Point3d(4.6256366667, 52.1311444028, 59.6907),
				new Point3d(102855.4402, 460600.1294, Double.NaN)));
		refPoints.put("Ballum        ", new Pair<Point3d, Point3d>(new Point3d(5.6876860750, 53.4415523472, 54.5413),
				new Point3d(174967.3844, 606186.3636, Double.NaN)));
		refPoints.put("Beilen        ", new Pair<Point3d, Point3d>(new Point3d(6.5151038056, 52.8604162528, 71.3695),
				new Point3d(230961.8141, 542064.7310, Double.NaN)));
		refPoints.put("Borkum        ", new Pair<Point3d, Point3d>(new Point3d(6.7474411778, 53.5636544167, 54.3998),
				new Point3d(245130.1229, 620588.0094, Double.NaN)));
		refPoints.put("Den Burg      ", new Pair<Point3d, Point3d>(new Point3d(4.7985186278, 53.0503051750, 54.3516),
				new Point3d(115524.5923, 562763.2797, Double.NaN)));
		refPoints.put("Deventer      ", new Pair<Point3d, Point3d>(new Point3d(6.1879905139, 52.2371533306, 64.6381),
				new Point3d(209699.6961, 472423.4628, Double.NaN)));
		refPoints.put("Dordrecht     ", new Pair<Point3d, Point3d>(new Point3d(4.6624808111, 51.8112414000, 64.7394),
				new Point3d(105023.0913, 424984.0043, Double.NaN)));
		refPoints.put("Drachten      ", new Pair<Point3d, Point3d>(new Point3d(6.0827906722, 53.1088207417, 56.3361),
				new Point3d(201580.5884, 569339.0662, Double.NaN)));
		refPoints.put("Ede           ", new Pair<Point3d, Point3d>(new Point3d(5.6725972278, 52.0452496472, 84.1331),
				new Point3d(174578.7662, 450808.6125, Double.NaN)));
		refPoints.put("Eijsden       ", new Pair<Point3d, Point3d>(new Point3d(5.7122937361, 50.7894634333, 102.8836),
				new Point3d(177925.3327, 311112.3405, Double.NaN)));
		refPoints.put("Emden         ", new Pair<Point3d, Point3d>(new Point3d(7.0274956944, 53.3374356417, 56.9559),
				new Point3d(264259.0532, 595802.0520, Double.NaN)));
		refPoints.put("Epe           ", new Pair<Point3d, Point3d>(new Point3d(7.0042625000, 52.1612686222, 97.2166),
				new Point3d(265640.5062, 464911.6263, Double.NaN)));
		refPoints.put("Geldrop       ", new Pair<Point3d, Point3d>(new Point3d(5.5551299694, 51.4121434833, 73.2176),
				new Point3d(166682.9970, 380347.9512, Double.NaN)));
		refPoints.put("Geofort       ", new Pair<Point3d, Point3d>(new Point3d(5.1253336167, 51.8656149722, 56.1374),
				new Point3d(136963.1355, 430816.9001, Double.NaN)));
		refPoints.put("Heerhugowaard ", new Pair<Point3d, Point3d>(new Point3d(4.8245812972, 52.6646823806, 60.2769),
				new Point3d(116938.0110, 519839.0455, Double.NaN)));
		refPoints.put("Heesch        ", new Pair<Point3d, Point3d>(new Point3d(5.5126647139, 51.7285551556, 58.2836),
				new Point3d(163667.9305, 415543.4301, Double.NaN)));
		refPoints.put("Houten        ", new Pair<Point3d, Point3d>(new Point3d(5.1709076500, 52.0327578528, 57.2048),
				new Point3d(140157.4612, 449402.3254, Double.NaN)));
		refPoints.put("Hüthum        ", new Pair<Point3d, Point3d>(new Point3d(6.1927009750, 51.8429904500, 64.6411),
				new Point3d(210507.9993, 428574.9861, Double.NaN)));
		refPoints.put("Kaldenkirchen ", new Pair<Point3d, Point3d>(new Point3d(6.1809066611, 51.3204287694, 102.6869),
				new Point3d(210329.0509, 370432.2049, Double.NaN)));
		refPoints.put("Kleve         ", new Pair<Point3d, Point3d>(new Point3d(6.1421622250, 51.7684344250, 104.3803),
				new Point3d(207111.5859, 420243.2435, Double.NaN)));
		refPoints.put("Makkum        ", new Pair<Point3d, Point3d>(new Point3d(5.3974757389, 53.0601207222, 59.4095),
				new Point3d(155688.4467, 563694.6989, Double.NaN)));
		refPoints.put("Meppen        ", new Pair<Point3d, Point3d>(new Point3d(7.3154618833, 52.7158920444, 89.2434),
				new Point3d(285286.4727, 527127.6045, Double.NaN)));
		refPoints.put("Nieuwleusen   ", new Pair<Point3d, Point3d>(new Point3d(6.2826380278, 52.5872446806, 61.3642),
				new Point3d(215682.9545, 511450.1977, Double.NaN)));
		refPoints.put("Nordhorn      ", new Pair<Point3d, Point3d>(new Point3d(7.0773800083, 52.4359872167, 80.5422),
				new Point3d(269929.8387, 495585.8685, Double.NaN)));
		refPoints.put("Oostburg      ", new Pair<Point3d, Point3d>(new Point3d(3.4947291056, 51.3316140167, 52.0347),
				new Point3d(23117.0799, 373085.6628, Double.NaN)));
		refPoints.put("Oostvoorne    ", new Pair<Point3d, Point3d>(new Point3d(4.0871299500, 51.9182543556, 62.0243),
				new Point3d(65561.9878, 437440.7519, Double.NaN)));
		refPoints.put("Roosendaal    ", new Pair<Point3d, Point3d>(new Point3d(4.4769637778, 51.5234359417, 61.4295),
				new Point3d(91829.9871, 393110.2909, Double.NaN)));
		refPoints.put("Selfkant      ", new Pair<Point3d, Point3d>(new Point3d(5.9362525250, 51.0236844278, 106.3055),
				new Point3d(193522.8523, 337263.1035, Double.NaN)));
		refPoints.put("St. Niklaas   ", new Pair<Point3d, Point3d>(new Point3d(4.1509153722, 51.1410120361, 78.6909),
				new Point3d(68482.7961, 350902.8336, Double.NaN)));
		refPoints.put("Stavenisse    ", new Pair<Point3d, Point3d>(new Point3d(4.0138209889, 51.5880705667, 56.6576),
				new Point3d(59825.6713, 400803.7390, Double.NaN)));
		refPoints.put("Turnhout      ", new Pair<Point3d, Point3d>(new Point3d(4.9490955583, 51.3128758222, 81.3452),
				new Point3d(124454.5814, 369382.4685, Double.NaN)));
		refPoints.put("Urk           ", new Pair<Point3d, Point3d>(new Point3d(5.5943759806, 52.6684930917, 52.7467),
				new Point3d(169014.0638, 520135.2378, Double.NaN)));
		refPoints.put("Urk2          ", new Pair<Point3d, Point3d>(new Point3d(5.6023757750, 52.6637251250, 54.3906),
				new Point3d(169556.7934, 519606.2788, Double.NaN)));
		refPoints.put("Veendam       ", new Pair<Point3d, Point3d>(new Point3d(6.8650099111, 53.1042727639, 65.9224),
				new Point3d(253969.3938, 569622.6785, Double.NaN)));
		refPoints.put("Viersen       ", new Pair<Point3d, Point3d>(new Point3d(6.3922296528, 51.2587469250, 105.4610),
				new Point3d(225154.1714, 363752.1613, Double.NaN)));
		refPoints.put("Vreden        ", new Pair<Point3d, Point3d>(new Point3d(6.7736030667, 52.0537707556, 92.5498),
				new Point3d(250088.6191, 452626.2579, Double.NaN)));
		refPoints.put("Wijk aan Zee  ", new Pair<Point3d, Point3d>(new Point3d(4.6021460111, 52.5042496972, 58.6033),
				new Point3d(101696.6727, 502128.6865, Double.NaN)));

	}

	@Test
	public void testRefPoints() throws UnknownCRSException, TransformationException {
		ICRS sourceCRS = CRSManager.lookup("epsg:4258");
		ICRS targetCRS = CRSManager.lookup("epsg:28992");
		for (String station : refPoints.keySet()) {
			LOG.info("\n refence point from station " + station);
			Pair<Point3d, Point3d> points = refPoints.get(station);
			doForwardAndInverse(sourceCRS, targetCRS, points.first, points.second, EPSILON_M, EPSILON_M);
		}
	}

}
