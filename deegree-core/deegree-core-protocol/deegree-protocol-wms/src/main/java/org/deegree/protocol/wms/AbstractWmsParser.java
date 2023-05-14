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
package org.deegree.protocol.wms;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.stax.XMLStreamUtils;

/**
 * Contains common methods for WMS Parsers.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class AbstractWmsParser {

	private static final String VERSION_130 = "1.3.0";

	/**
	 * Skips to the start elment of the documents and checks the version attribute.
	 * @param request WMS request, never <code>null</code>
	 * @return the version from the document, if version is null the namespace is
	 * evaluated, never <code>null</code>
	 * @throws XMLStreamException if an exeption occured during parsing
	 * @throw {@link InvalidParameterValueException} if the version could not be parsed
	 */
	protected Version forwardToStartAndDetermineVersion(XMLStreamReader request) throws XMLStreamException {
		XMLStreamUtils.skipStartDocument(request);
		String versionAttributeValue = request.getAttributeValue(null, "version");
		if (CommonNamespaces.WMSNS.equals(request.getNamespaceURI())) {
			if (versionAttributeValue.isEmpty()) {
				versionAttributeValue = VERSION_130;
			}
		}
		return Version.parseVersion(versionAttributeValue);
	}

}