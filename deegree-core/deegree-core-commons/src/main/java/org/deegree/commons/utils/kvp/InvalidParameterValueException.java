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

package org.deegree.commons.utils.kvp;

/**
 * Thrown to indicate that a parameter has an unsupported or unsuitable value.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class InvalidParameterValueException extends IllegalArgumentException {

	private static final long serialVersionUID = -6082873552693636502L;

	private String param;

	/**
	 * Constructs a new {@link InvalidParameterValueException} with null as its detail
	 * message.
	 */
	public InvalidParameterValueException() {
		// nothing to do
	}

	/**
	 * Constructs a new {@link InvalidParameterValueException} with the specified detail
	 * message.
	 * @param msg the detail message (which is saved for later retrieval by the
	 * <code>Throwable.getMessage()</code> method).
	 */
	public InvalidParameterValueException(String msg) {
		super(msg);
	}

	/**
	 * Constructs a new {@link InvalidParameterValueException} with the specified detail
	 * message.
	 * @param msg the detail message (which is saved for later retrieval by the
	 * <code>Throwable.getMessage()</code> method)
	 * @param param name of the missing parameter
	 */
	public InvalidParameterValueException(String msg, String param) {
		super(msg);
		this.param = param;
	}

	/**
	 * Constructs a new {@link InvalidParameterValueException} with the specified detail
	 * message and cause.
	 * @param msg the detail message (which is saved for later retrieval by the
	 * <code>Throwable.getMessage()</code> method).
	 * @param cause the cause (which is saved for later retrieval by the
	 * <code>Throwable.getCause()</code> method). (A <code>null</code> value is permitted,
	 * and indicates that the cause is nonexistent or unknown.)
	 */
	public InvalidParameterValueException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructs a new {@link InvalidParameterValueException} exception with the
	 * specified cause and a detail message of
	 * <code>(cause==null ? null : cause.toString())</code> (which typically contains the
	 * class and detail message of cause). This constructor is useful for runtime
	 * exceptions that are little more than wrappers for other throwables.
	 * @param cause the cause (which is saved for later retrieval by the
	 * <code>Throwable.getCause()</code> method). (A <code>null</code> value is permitted,
	 * and indicates that the cause is nonexistent or unknown.)
	 */
	public InvalidParameterValueException(Throwable cause) {
		super(cause);
	}

	/**
	 * Returns the name of the invalid parameter.
	 * @return the name of the invalid parameter
	 */
	public String getName() {
		return param;
	}

}
