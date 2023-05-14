/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.services.wfs.util;

/**
 * Contains methods to calculate the start index for response paging.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public final class ResponsePagingUtils {

	private ResponsePagingUtils() {
	}

	/**
	 * Calculates the start index of the next url, if the last page is not reached.
	 * @param startIndex the start index of the request, must be >= 0
	 * @param count the number of features requested, must be > 0
	 * @param hits the number of features matched, must be >= 0
	 * @return a value > 0 if a next page is available; -1 if there is no next page
	 */
	public static int calculateNextStartIndex(int startIndex, int count, int hits) {
		int nextStartIndex = startIndex + count;
		if (nextStartIndex < hits) {
			return nextStartIndex;
		}
		return -1;
	}

	/**
	 * Calculates the start index of the previous url, if the first page is not reached.
	 * @param startIndex the start index of the request, must be >= 0
	 * @param count the number of features requested, must be > 0
	 * @return a value >= 0 if a previous page is available; -1 if there is no previous
	 * page
	 */
	public static int calculatePreviousStartIndex(int startIndex, int count) {
		int previousStartIndex = startIndex - count;
		if (previousStartIndex < 0 && startIndex > 0)
			return 0;
		if (previousStartIndex >= 0) {
			return previousStartIndex;
		}
		return -1;
	}

}