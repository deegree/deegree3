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

package org.deegree.tools.commons.utils;

import static org.deegree.commons.utils.EncodingGuesser.guess;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.deegree.commons.annotations.Tool;
import org.slf4j.Logger;

/**
 * <code>ScanEncoding</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
@Tool(value = "scans the argument files and tries to guess their encoding, treats dbf files specially")
public class ScanEncoding {

	private static final Logger LOG = getLogger(ScanEncoding.class);

	private static void printSummary(String s) throws IOException {
		boolean dbf = s.toLowerCase().endsWith(".dbf");

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(s));

		if (dbf) {
			if (in.skip(32) != 32) {
				LOG.warn("Could not skip 32 bytes, is the dbf broken?");
			}
			int b;
			while ((b = in.read()) != -1) {
				if (b == 13) {
					break;
				}
			}
		}

		LOG.info("Encoding for '" + s + "': " + guess(in));
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		for (String s : args) {
			printSummary(s);
		}
	}

}
