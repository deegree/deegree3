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
package org.deegree.services.wms.controller.capabilities.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serializes capabilities documents (XML).
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public interface CapabilitiesSerializer {

	/**
	 * @param capabilitiesXmlStream the stream containing the capabilities document, never
	 * <code>null</code>
	 * @param responseStream stream to write the document in, never <code>null</code>
	 * @throws IOException if an error occurred
	 */
	void serialize(InputStream capabilitiesXmlStream, OutputStream responseStream) throws IOException;

}