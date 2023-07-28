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
package org.deegree.services.wms.controller.exceptions;

import java.util.Map;

import javax.servlet.ServletException;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wms.controller.WMSController;

/**
 * Serializes an exception as xml document.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class XmlExceptionSerializer implements ExceptionsSerializer {

	private WMSController controller;

	/**
	 * @param controller never <code>null</code>
	 */
	public XmlExceptionSerializer(WMSController controller) {
		this.controller = controller;
	}

	@Override
	public void serializeException(HttpResponseBuffer response, OWSException ex,
			XMLExceptionSerializer exceptionSerializer, Map<String, String> map) throws SerializingException {
		try {
			controller.sendException(null, exceptionSerializer, ex, response);
		}
		catch (ServletException e) {
			throw new SerializingException(e);
		}
	}

}