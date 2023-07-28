/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

package org.deegree.tools.crs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.transformations.polynomial.LeastSquareApproximation;
import org.deegree.cs.transformations.polynomial.PolynomialTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>PolynomialParameterCreator</code> allows for the calculation of a
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class PolynomialParameterCreator {

	private static Logger log = LoggerFactory.getLogger(PolynomialParameterCreator.class);

	/**
	 * Simple constructor which reads coordinate pairs from a different files (using the
	 * separator) and stores them in a list.
	 * @param sourceFile to read the source coordinates from
	 * @param targetFile to read the target coordinates from.
	 * @param seperator which separates the coordinates (e.g. ; or , )
	 * @param source the source crs in which the first read points are given
	 * @param target the target crs in which the read points are defined.
	 * @param transformationClass to use
	 * @param order of the polynomial.
	 * @throws IOException if the file could not be read.
	 */
	public PolynomialParameterCreator(File sourceFile, File targetFile, String seperator, ICRS source, ICRS target,
			String transformationClass, final int order) throws IOException {

		List<Point3d> from = readFromFile(sourceFile, source.getDimension(), seperator);
		List<Point3d> to = readFromFile(targetFile, target.getDimension(), seperator);
		if (from.size() != to.size()) {
			log.error("The number of coordinates in the from file( " + from.size() + ") differ from the targetFile ("
					+ to.size() + ") , this maynot be!");
			System.exit(1);
		}
		if (transformationClass == null || "".equals(transformationClass.trim())) {
			throw new IllegalArgumentException("The transformation class may not be null");
		}

		PolynomialTransformation transform = null;
		List<Double> params = new LinkedList<Double>();
		// 48.01903, 0.002305167, -0.0011897635, 1.0666529E-5, -8.303933E-6, 4.4940844E-6,
		// -3.8565862E-11,
		// 5.0593762E-11, -2.230412E-11, 2.968846E-12
		// 3.0465062, -1.5860682E-4, 4.3924665E-4, 1.2747373E-6, -1.103672E-6,
		// 5.936716E-7, -2.3854978E-12,
		// 3.0211528E-12, -1.2788576E-12, 1.4953932E-13

		params.add(new Double(1));
		if ("leastsquares".equals(transformationClass.toLowerCase().trim())) {
			transform = new LeastSquareApproximation(params, params, source, target, 1, 1);
		}
		if (transform != null) {
			float[][] calculatedParams = transform.createVariables(from, to, order);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < calculatedParams.length; ++i) {
				String t = "crs:" + (i == 0 ? "x" : "y") + "Parameters>";
				sb.append("<").append(t);
				for (int y = 0; y < calculatedParams[i].length; ++y) {
					sb.append(calculatedParams[i][y]);
					if ((y + 1) < calculatedParams[i].length) {
						sb.append(" ");
					}
				}
				sb.append("</").append(t).append("\n");
			}
			log.info("Resulted params:\n" + sb.toString());
		}
	}

	private List<Point3d> readFromFile(File f, int dim, String seperator) throws IOException {
		log.info("Trying to read reference points from file: " + f);
		List<Point3d> result = new ArrayList<Point3d>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String coords = br.readLine();
		int lineCount = 1;
		while (coords != null) {
			if (!coords.startsWith("#")) {
				String[] coordinates = coords.split(seperator);
				if (coordinates.length != dim) {
					log.warn(lineCount
							+ ") Each line must contain the number of coordinates fitting the dimension of crs (" + dim
							+ ") seperated by a '" + seperator + "'.");
				}
				else {
					Point3d coord = new Point3d();
					coord.x = Double.parseDouble(coordinates[0].replace(",", "."));
					coord.y = Double.parseDouble(coordinates[1].replace(",", "."));
					if (dim == 3) {
						coord.z = Double.parseDouble(coordinates[2].replace(",", "."));
					}
					result.add(coord);
				}
			}
			lineCount++;
			coords = br.readLine();
		}
		br.close();
		return result;

	}

	/**
	 * a starter method to test the quality of projections.
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			outputHelp();
		}
		Map<String, String> params = new HashMap<String, String>(5);
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg != null && !"".equals(arg.trim())) {
				arg = arg.trim();
				if (arg.equalsIgnoreCase("-?") || arg.equalsIgnoreCase("-h")) {
					outputHelp();
				}
				else {
					if (i + 1 < args.length) {
						String val = args[++i];
						if (val != null) {
							params.put(arg, val.trim());
						}
						else {
							System.out.println("Invalid value for parameter: " + arg);
						}
					}
					else {
						System.out.println("No value for parameter: " + arg);
					}
				}
			}
		}
		String sourceFile = params.get("-sourceFile");
		if (sourceFile == null || "".equals(sourceFile.trim())) {
			log.error("No file with reference points in the source CRS given (-sourceFile parameter)");
			System.exit(1);
		}

		String sourceCRS = params.get("-sourceCRS");
		if (sourceCRS == null || "".equals(sourceCRS.trim())) {
			log.error("No source CRS given (-sourceCRS parameter)");
			System.exit(1);
		}
		String targetCRS = params.get("-targetCRS");
		if (targetCRS == null || "".equals(targetCRS.trim())) {
			log.error("No target CRS given (-targetCRS parameter)");
			System.exit(1);
		}

		String targetFile = params.get("-targetFile");
		if (targetFile == null || "".equals(targetFile.trim())) {
			log.error("No file with reference points in the target CRS given (-targetFile parameter)");
			System.exit(1);
		}

		String polyOrder = params.get("-order");
		int order = 0;
		if (polyOrder == null || "".equals(polyOrder.trim())) {
			log.error("No polynomial order (-order parameter) given. Not continuing.");
			System.exit(1);
		}
		order = Integer.parseInt(polyOrder);

		String transformClass = params.get("-transformClass");
		if (polyOrder == null || "".equals(polyOrder.trim())) {
			log.error("No transformation class (-transformClass parameter) given. Not continuing.");
			System.exit(1);
		}

		String coordSep = params.get("-coordSep");
		if (coordSep == null || "".equals(coordSep)) {
			log.info(
					"No coordinates separator given (-coordSep parameter), therefore using ' ' (a space) as separator");
			coordSep = " ";
		}

		log.info("Trying to convert coordinates from: " + sourceCRS + " to: " + targetCRS);

		try {
			ICRS source = CRSManager.lookup(sourceCRS);
			ICRS target = CRSManager.lookup(targetCRS);
			new PolynomialParameterCreator(new File(sourceFile), new File(targetFile), coordSep, source, target,
					transformClass, order);

		}
		catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		catch (UnknownCRSException e) {
			log.error(e.getMessage(), e);
		}

	}

	private static void outputHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"The PolynomialParamter program can be used to create the polynomial variables to approximate a given\n");
		sb.append("function, which is defined by two list of coordinates.\n");
		sb.append("Following parameters are supported:\n");
		sb.append("-sourceFile the /path/of/the_source_crs_reference_points -file\n");
		sb.append("-srcCRS the name of the source crs, e.g. EPSG:4326.\n");
		sb.append("-targetCRS the name of the target crs, e.g. EPSG:31467.\n");
		sb.append("-targetFile the /path/to/the_target_crs_reference_points.\n");
		sb.append("-order the polynomial order to calculate the values for.\n");
		sb.append(
				"-transformClass the simple name of the transformation polynomial at the moment following values are supported:\n");
		sb.append("\t - leastsquares\n");
		sb.append(
				"[-coordSep] separator of between the coords in the file(s), e.g. ; or ' ', if omitted a space is assumed.\n");
		sb.append("-?|-h output this text\n");
		System.out.println(sb.toString());
		System.exit(1);
	}

}
