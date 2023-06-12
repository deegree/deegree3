/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2011-2022 by:
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.commons.json;

public class JSONParsingException extends RuntimeException {

	private String message = "org.deegree.json.JSONParsingException";

	private String stackTrace = "<< is empty >>";

	/**
	 * Creates a new instance of <code>JSONParsingException</code> without detail message.
	 */
	protected JSONParsingException() {
		// nothing to do
	}

	/**
	 * Constructs an instance of <code>JSONParsingException</code> with the specified
	 * detail message.
	 * @param msg the detail message.
	 */
	public JSONParsingException(String msg) {
		super();
		message = msg;

	}

	/**
	 * Constructs an instance of <code>JSONParsingException</code> with the specified
	 * cause.
	 * @param cause the Throwable that caused this XMLParsingException
	 *
	 */
	public JSONParsingException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an instance of <code>JSONParsingException</code> with the specified
	 * detail message.
	 * @param msg the detail message.
	 * @param e
	 */
	public JSONParsingException(String msg, Throwable e) {
		this(msg);
		if (e != null) {
			StackTraceElement[] se = e.getStackTrace();
			StringBuffer sb = new StringBuffer(1000);
			for (int i = 0; i < se.length; i++) {
				sb.append(se[i].getClassName() + " ");
				sb.append(se[i].getFileName() + " ");
				sb.append(se[i].getMethodName() + "(");
				sb.append(se[i].getLineNumber() + ")\n");
			}
			stackTrace = e.getMessage() + sb.toString();
		}
	}

	@Override
	public String toString() {
		return this.getClass() + ": " + getMessage() + "\n" + stackTrace;
	}

	/**
	 *
	 */
	@Override
	public String getMessage() {
		return message;
	}

}
