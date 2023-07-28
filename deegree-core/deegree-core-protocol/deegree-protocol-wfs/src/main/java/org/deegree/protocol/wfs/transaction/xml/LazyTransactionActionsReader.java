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
package org.deegree.protocol.wfs.transaction.xml;

import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.protocol.wfs.transaction.TransactionAction;

/**
 * Parser for the actions contained in a WFS <code>Transaction</code> document.
 *
 * @see TransactionXmlReader
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
class LazyTransactionActionsReader implements Iterable<TransactionAction> {

	private final XMLStreamReader xmlStream;

	private final TransactionXmlReader transactionReader;

	private boolean createdIterator;

	/**
	 * Creates a new {@link LazyTransactionActionsReader} that provides sequential access
	 * to the given XML-encoded {@link TransactionAction}s.
	 * @param xmlStream
	 * @param transactionReader
	 */
	LazyTransactionActionsReader(XMLStreamReader xmlStream, TransactionXmlReader transactionReader) {
		this.xmlStream = xmlStream;
		this.transactionReader = transactionReader;
	}

	@Override
	public synchronized Iterator<TransactionAction> iterator() {
		if (createdIterator) {
			throw new RuntimeException("Iteration over the transaction actions can only be done once.");
		}
		createdIterator = true;
		return new Iterator<TransactionAction>() {

			boolean needsNextElement = false;

			@Override
			public boolean hasNext() {
				if (needsNextElement) {
					try {
						nextElement(xmlStream);
						needsNextElement = false;
					}
					catch (Exception e) {
						throw new XMLParsingException(xmlStream, "Error parsing transaction action: " + e.getMessage());
					}
				}
				return xmlStream.isStartElement();
			}

			@Override
			public TransactionAction next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				try {
					TransactionAction action = transactionReader.readAction(xmlStream);
					needsNextElement = true;
					return action;
				}
				catch (XMLStreamException e) {
					throw new XMLParsingException(xmlStream, "Error parsing transaction action: " + e.getMessage());
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
