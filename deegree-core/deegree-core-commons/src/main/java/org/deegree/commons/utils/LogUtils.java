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
package org.deegree.commons.utils;

/**
 * This class contains static utility methods for writing files when a log is set to
 * debug.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class LogUtils {

	/**
	 * Create a string as follows: ${message} + took: ... {[ms]}{[second|minute][s]} the
	 * current time millis will be used as the end time.
	 * @param message to start with
	 * @param startTime
	 * @param roundToMinutes if true the second| minutes syntax will be used, if falls
	 * only ms will be appended.
	 * @return a string as follows: ${message} + took: ... [second|minute][s]
	 */
	public static String createDurationTimeString(String message, long startTime, boolean roundToMinutes) {
		return createTimeString(message, startTime, System.currentTimeMillis(), roundToMinutes);
	}

	/**
	 * Create a string as follows: ${message} + took: ... {[ms]}{[second|minute][s]}
	 * @param message to start with
	 * @param startTime
	 * @param endTime
	 * @param roundToMinutes if true the second| minutes syntax will be used, if falls
	 * only ms will be appended.
	 * @return a string as follows: ${message} + took: ... [second|minute][s]
	 */
	public static String createTimeString(String message, long startTime, long endTime, boolean roundToMinutes) {
		StringBuilder sb = new StringBuilder(message);

		long totalTime = endTime - startTime;
		sb.append(" took: ");
		if (roundToMinutes) {
			double rPT = Math.round((Math.round((totalTime) / 10d) / 100d));
			double min = Math.round(rPT / 60d);
			if (rPT > 60) {
				sb.append(min);
				sb.append(" minute");
				if (min > 1) {
					sb.append("s");
				}
			}
			else {
				sb.append(rPT);
				sb.append(" second");
				if (rPT != 1.0) {
					sb.append("s");
				}
			}
		}
		else {
			sb.append(totalTime);
			sb.append(" ms");
		}
		sb.append(".");
		return sb.toString();
	}

}
