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
package org.deegree.protocol.wfs.transaction.action;

import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.protocol.wfs.transaction.TransactionActionType.UPDATE;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.Filter;
import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionActionType;
import org.deegree.protocol.wfs.transaction.xml.TransactionXmlReader;

/**
 * A WFS <code>Update</code> action (part of a {@link Transaction} request).
 *
 * @see Transaction
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Update extends AbstractTransactionAction {

	private final QName ftName;

	private final String inputFormat;

	private final String srsName;

	private final XMLStreamReader xmlStream;

	private final TransactionXmlReader transactionReader;

	private boolean createdIterator;

	/**
	 * Creates a new {@link Update} instance for a stream-based access strategy.
	 * @param handle identifier for the operation, may be <code>null</code>
	 * @param ftName name of the targeted feature type, must not be <code>null</code>
	 * @param inputFormat the format of encoded property values, may be <code>null</code>
	 * (unspecified)
	 * @param srsName the coordinate references system used for the geometries, may be
	 * <code>null</code> (unspecified)
	 * @param xmlStream provides access to the XML encoded replacement properties and the
	 * filter, must point at the <code>START_ELEMENT</code> event of the first
	 * <code>wfs:Property</code>
	 * @param transactionReader
	 */
	public Update(String handle, Version version, QName ftName, String inputFormat, String srsName,
			XMLStreamReader xmlStream, TransactionXmlReader transactionReader) {
		super(handle);
		this.ftName = ftName;
		this.inputFormat = inputFormat;
		this.srsName = srsName;
		this.xmlStream = xmlStream;
		this.transactionReader = transactionReader;
	}

	/**
	 * Always returns {@link TransactionActionType#UPDATE}.
	 * @return {@link TransactionActionType#UPDATE}
	 */
	@Override
	public TransactionActionType getType() {
		return UPDATE;
	}

	/**
	 * Returns the name of the targeted feature type.
	 * @return the name of the targeted feature type, never null
	 */
	public QName getTypeName() {
		return ftName;
	}

	/**
	 * Returns the format of the encoded property values.
	 * @return the format of the encoded property values, may be null (unspecified)
	 */
	public String getInputFormat() {
		return inputFormat;
	}

	/**
	 * Returns the specified coordinate reference system for geometries to be updated.
	 * @return the specified coordinate reference system, can be null (unspecified)
	 */
	public String getSRSName() {
		return srsName;
	}

	public Iterator<PropertyReplacement> getReplacementProps() {
		if (createdIterator) {
			throw new RuntimeException("Iteration over replacement properties can only be done once.");
		}
		createdIterator = true;
		return new Iterator<PropertyReplacement>() {

			@Override
			public boolean hasNext() {
				return xmlStream.isStartElement() && "Property".equals(xmlStream.getName().getLocalPart());
			}

			@Override
			public PropertyReplacement next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				PropertyReplacement replacement = null;
				try {
					replacement = transactionReader.readProperty(xmlStream);
				}
				catch (XMLStreamException e) {
					e.printStackTrace();
					throw new XMLParsingException(xmlStream, "Error parsing transaction operation: " + e.getMessage());
				}
				return replacement;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Returns the filter that selects the feature instances to be updated.
	 * <p>
	 * NOTE: Due to the streaming access strategy, there are some rules for using this
	 * method:
	 * <ul>
	 * <li>#getReplacementProps() must have been called before</li>
	 * <li>the client must have iterated over all returned properties</li>
	 * <li>the method must be called exactly once</li>
	 * </ul>
	 * </p>
	 * @return Filter that selects the feature instances to be updated, can be
	 * <code>null</code>
	 * @throws XMLStreamException
	 */
	public Filter getFilter() throws XMLStreamException {
		// optional: 'ogc:Filter' / 'fes:Filter'
		Filter filter = null;
		if (xmlStream.isStartElement()) {
			filter = transactionReader.readFilter(xmlStream);
			// xmlStream.require( END_ELEMENT, CommonNamespaces.OGCNS, "Filter" );
			// contract: skip to wfs:Update END_ELEMENT
			nextElement(xmlStream);
			// contract: skip to next operation START_ELEMENT
			// nextElement( xmlStream );
		}
		return filter;
	}

}
