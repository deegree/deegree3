/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.commons.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.utils.net.HttpUtils.Worker;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;

/**
 * Utility class to use with HttpUtils.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class XmlHttpUtils {

	private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	/**
	 * Returns streaming XMLAdapter.
	 */
	public static final Worker<XMLAdapter> XML = new Worker<XMLAdapter>() {
		@Override
		public XMLAdapter work(InputStream in) {
			return new XMLAdapter(in);
		}
	};

	/**
	 * Returns streaming XMLAdapter.
	 */
	public static final Worker<XMLStreamReaderWrapper> XML_STREAM = new Worker<XMLStreamReaderWrapper>() {
		@Override
		public XMLStreamReaderWrapper work(InputStream in) throws IOException {
			try {
				return new XMLStreamReaderWrapper(XmlHttpUtils.xmlInputFactory.createXMLStreamReader(in),
						"Post response");
			}
			catch (XMLStreamException e) {
				throw new IOException("Error creating XMLStreamReader for POST response: " + e.getMessage());
			}
		}
	};

}
