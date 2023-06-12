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
package org.deegree.commons.mail;

/**
 * A UnknownMimetypeException is thrown if the MIME type is not supported.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</A>
 */
public class UnknownMimeTypeException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -3326395686226436933L;

	private String mimeType;

	/**
	 * Creates a exception with the given message and MIME type
	 * @param message
	 * @param mimeType
	 */
	public UnknownMimeTypeException(String message, String mimeType) {
		super(message + " : Unknown MIME Type :" + mimeType);
		this.mimeType = mimeType;
	}

	/**
	 * @return the name of the unknown mime type
	 *
	 */
	public String getMimeType() {
		return this.mimeType;
	}

}
