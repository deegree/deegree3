/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wfs.transaction.xml;

import static org.deegree.commons.tom.ows.Version.parseVersion;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValue;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredAttributeValue;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.protocol.i18n.Messages;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class TransactionXmlReaderFactory {

	public TransactionXmlReader createReader(Version version) {
		if (VERSION_100.equals(version)) {
			return new TransactionXmlReader100();
		}
		else if (VERSION_110.equals(version)) {
			return new TransactionXmlReader110();
		}
		else if (VERSION_200.equals(version)) {
			return new TransactionXmlReader200();
		}
		String msg = Messages.get("UNSUPPORTED_VERSION", version,
				Version.getVersionsString(VERSION_100, VERSION_110, VERSION_200));
		throw new InvalidParameterValueException(msg);
	}

	public TransactionXmlReader createReader(XMLStreamReader xmlStream) {
		Version version = null;
		if (WFS_NS.equals(xmlStream.getNamespaceURI())) {
			String s = getAttributeValue(xmlStream, "version");
			if (s == null) {
				s = "1.1.0";
			}
			version = parseVersion(s);
		}
		else {
			version = parseVersion(getRequiredAttributeValue(xmlStream, "version"));
		}
		return createReader(version);
	}

}
