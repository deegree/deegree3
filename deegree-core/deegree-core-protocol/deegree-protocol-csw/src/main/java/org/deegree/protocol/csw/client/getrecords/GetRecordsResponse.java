/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.protocol.csw.client.getrecords;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.MetadataRecordFactory;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpResponse;

/**
 * Represents a <code>GetRecords</code> response of a CSW.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetRecordsResponse {

	private final OwsHttpResponse response;

	private int numberOfRecordsReturned;

	private int numberOfRecordsMatched;

	private int nextRecord;

	private XMLStreamReader xmlStream;

	protected String recordElementName;

	public GetRecordsResponse(OwsHttpResponse response)
			throws XMLProcessingException, OWSExceptionReport, XMLStreamException {
		this.response = response;
		xmlStream = response.getAsXMLStream();
		XMLStreamUtils.skipStartDocument(xmlStream);
		XMLStreamUtils.moveReaderToFirstMatch(xmlStream, new QName(CSW_202_NS, "SearchResults"));
		String noOfRecM = XMLStreamUtils.getAttributeValue(xmlStream, "numberOfRecordsMatched");
		numberOfRecordsMatched = noOfRecM != null ? Integer.parseInt(noOfRecM) : 0;

		String noOfRecR = XMLStreamUtils.getAttributeValue(xmlStream, "numberOfRecordsReturned");
		numberOfRecordsReturned = noOfRecR != null ? Integer.parseInt(noOfRecR) : 0;

		String nextRec = XMLStreamUtils.getAttributeValue(xmlStream, "nextRecord");
		nextRecord = nextRec != null ? Integer.parseInt(nextRec) : 0;

		xmlStream.next();
		while (xmlStream.getEventType() != END_DOCUMENT && !xmlStream.isStartElement() && !xmlStream.isEndElement()) {
			xmlStream.next();
		}
		if (xmlStream.getEventType() != END_DOCUMENT) {
			recordElementName = xmlStream.getLocalName();
		}
	}

	public OwsHttpResponse getResponse() {
		return response;
	}

	public Iterator<MetadataRecord> getRecords() {
		return new Iterator<MetadataRecord>() {

			@Override
			public boolean hasNext() {
				return recordElementName != null && xmlStream.isStartElement()
						&& recordElementName.equals(xmlStream.getLocalName());
			}

			@Override
			public MetadataRecord next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				try {
					return MetadataRecordFactory.create(xmlStream);
				}
				finally {
					try {
						nextElement(xmlStream);
					}
					catch (XMLStreamException e) {
						throw new XMLParsingException(xmlStream, e.getMessage());
					}
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public int getNumberOfRecordsMatched() {
		return numberOfRecordsMatched;
	}

	public int getNumberOfRecordsReturned() {
		return numberOfRecordsReturned;
	}

	public int getNextRecord() {
		return nextRecord;
	}

	public void close() throws IOException {
		response.close();
	}

}
