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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.Filter;
import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionAction;
import org.deegree.protocol.wfs.transaction.action.PropertyReplacement;

/**
 * Reader for XML encoded WFS <code>Transaction</code> elements.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface TransactionXmlReader {

	/**
	 * Returns the object representation of the given <code>Transaction</code> element.
	 * <p>
	 * NOTE: In order to allow streaming processing, this method will not necessarily
	 * consume all corresponding events from the given <code>XMLStream</code> .
	 * </p>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event, must
	 * not be <code>null</code>
	 * @return corresponding {@link Transaction} object, never <code>null</code>
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 */
	Transaction read(XMLStreamReader xmlStream) throws XMLStreamException, XMLParsingException;

	/**
	 * Returns the object representation of the given transaction action element.
	 * <p>
	 * NOTE: In order to allow streaming processing, this method will not necessarily
	 * consume all corresponding events from the given <code>XMLStream</code> .
	 * </p>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event, must
	 * not be <code>null</code>
	 * @return corresponding {@link TransactionAction} object, never <code>null</code>
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 */
	TransactionAction readAction(XMLStreamReader xmlStream) throws XMLStreamException, XMLParsingException;

	Filter readFilter(XMLStreamReader xmlStream) throws XMLParsingException, XMLStreamException;

	PropertyReplacement readProperty(XMLStreamReader xmlStream) throws XMLStreamException;

}
