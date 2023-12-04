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
package org.deegree.coverage.tools;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;

/**
 * This class contains some static convenience methods for raster commandline tools.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public class RasterCommandUtils {

	/**
	 * Returns an InterpolationType for a string shortcut.
	 * @param type bl or nn
	 * @return the interpolation type
	 * @throws ParseException if the type is not supported
	 */
	public static InterpolationType getInterpolationType(String type) throws ParseException {
		if (type == null) {
			return InterpolationType.NEAREST_NEIGHBOR; // default
		}
		if (type.equalsIgnoreCase("bl")) {
			return InterpolationType.BILINEAR;
		}
		if (type.equalsIgnoreCase("nn")) {
			return InterpolationType.NEAREST_NEIGHBOR;
		}
		throw new ParseException("interpolation type " + type + " is not supported");
	}

	/**
	 * Retruns an envelope for given bbox string.
	 * @param bbox comma separated bbox
	 * @return the parsed envelope
	 * @throws ParseException if the bbox is invalid
	 */
	public static Envelope parseBBOX(String bbox) throws ParseException {
		String[] parts = bbox.split(",");
		float[] coords = new float[4];
		try {
			for (int i = 0; i < 4; i++) {
				coords[i] = Float.parseFloat(parts[i]);
			}
		}
		catch (IndexOutOfBoundsException ex) {
			throw new ParseException("invalid bbox '" + bbox + "' doesn't contain four values");
		}
		catch (NumberFormatException ex) {
			throw new ParseException("invalid bbox '" + bbox + "' doesn't contain four float values");
		}

		GeometryFactory geomFactory = new GeometryFactory();

		return geomFactory.createEnvelope(new double[] { coords[0], coords[1] }, new double[] { coords[2], coords[3] },
				null);
	}

	/**
	 * Parse a background value which is either a 0xdecafbad hex value or a float value.
	 * Float values will be returned as a 4-byte array.
	 * @param background
	 * @return a byte array with the parsed value
	 * @throws ParseException
	 */
	public static byte[] parseBackgroundValue(String background) throws ParseException {

		if (background.toLowerCase().startsWith("0x")) {
			byte[] result = null;
			String hexValues = background.substring(2);
			try {
				if (hexValues.length() == 3 || hexValues.length() == 4) {
					result = new byte[hexValues.length()];
					for (int i = 0; i < result.length; i++) {
						result[i] = parseHexToByte("" + hexValues.charAt(i) + hexValues.charAt(i));
					}
				}
				else if (hexValues.length() == 6 || hexValues.length() == 8) {
					result = new byte[hexValues.length() / 2];
					for (int i = 0; i < result.length; i++) {
						result[i] = parseHexToByte("" + hexValues.charAt(i * 2) + hexValues.charAt(i * 2 + 1));
					}
				}
				else {
					throw new ParseException("not a valid hex color value (like 0xff0, or 0x0ab4f8)");
				}
			}
			catch (NumberFormatException ex) {
				throw new ParseException("not a valid hex color value (like 0xff0, or 0x0ab4f8)");
			}

			return result;
		}
		ByteBuffer tmp = ByteBuffer.allocate(4);
		tmp.putFloat(Float.parseFloat(background));
		return tmp.array();
	}

	private static byte parseHexToByte(String hex) {
		return (byte) (0xff & Integer.parseInt(hex, 16));
	}

	/**
	 * Get all files from the given location(s).
	 * @param args array with file and directory names.
	 * @param recurse recurse into all directories
	 * @param fileExt only get files with the given file suffix
	 * @return a list with all given files and all files in the given directories.
	 */
	public static List<File> getAllFiles(String[] args, boolean recurse, final String fileExt) {
		List<File> result = new LinkedList<File>();
		for (String filename : args) {
			result.addAll(getFiles(new File(filename), recurse, new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(fileExt);
				}
			}));
		}
		return result;
	}

	/**
	 * Get all files from the given location(s).
	 * @param args array with file and directory names.
	 * @param recurse recurse into all directories
	 * @return a list with all given files and all files in the given directories.
	 */
	public static List<File> getAllFiles(String[] args, boolean recurse) {
		return getAllFiles(args, recurse, "");
	}

	private static List<File> getFiles(File root, boolean recurse, FileFilter fileFilter) {
		List<File> result = new LinkedList<File>();
		if (root.isDirectory()) {
			for (File file : root.listFiles()) {
				if (file.isDirectory() && recurse) {
					result.addAll(getFiles(file, recurse, fileFilter));
				}
				else if (file.isFile() && fileFilter.accept(file)) {
					result.add(file);
				}
			}
		}
		else {
			result.add(root);
		}
		return result;
	}

}
