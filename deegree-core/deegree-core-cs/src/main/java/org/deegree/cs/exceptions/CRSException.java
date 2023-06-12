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

package org.deegree.cs.exceptions;

/**
 * A <code>CRSException</code> will inform the user that an error occurred while
 * transforming, projecting or creating a crs. The exporting process has been halted.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */

public class CRSException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -8120907445764439063L;

	/**
	 * @param string
	 */
	public CRSException(String string) {
		super(string);
	}

	/**
	 * @param message another message.
	 * @param e which caused this crsException
	 */
	public CRSException(String message, Exception e) {
		super(message, e);
	}

	/**
	 * @param e cause of this CRSException
	 */
	public CRSException(Exception e) {
		super(e);
	}

}
