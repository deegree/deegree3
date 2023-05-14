/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.ows.exception;

import java.util.List;

import org.deegree.commons.ows.exception.OWSException;

/**
 * Encapsulates a number of {@link OWSException}s.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class OWSExceptionReport extends Exception {

	private static final long serialVersionUID = 3640316306096580505L;

	private final String version;

	private final String lang;

	private final List<OWSException> exceptions;

	public OWSExceptionReport(List<OWSException> exceptions, String lang, String version) {
		super(buildMessage(exceptions));
		this.version = version;
		this.lang = lang;
		this.exceptions = exceptions;
	}

	private static String buildMessage(List<OWSException> exceptions) {
		StringBuilder sb = new StringBuilder();
		for (OWSException exception : exceptions) {
			sb.append(exception.getMessage());
		}
		return sb.toString();
	}

	public String getVersion() {
		return version;
	}

	public String getLang() {
		return lang;
	}

	public List<OWSException> getExceptions() {
		return exceptions;
	}

}
