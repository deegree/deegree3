/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.metadata.iso.persistence.memory;

import java.util.Iterator;
import java.util.List;

import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.protocol.csw.MetadataStoreException;

/**
 * Implementation of an {@link MetadataResultSet} encapsulating a list of
 * {@link ISORecord}s.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class ListMetadataResultSet implements MetadataResultSet<ISORecord> {

	private int requestedRecords = 0;

	private Iterator<ISORecord> iterator;

	private List<ISORecord> foundRecords;

	ListMetadataResultSet(List<ISORecord> foundRecords) {
		this.foundRecords = foundRecords;
		iterator = foundRecords.iterator();
	}

	@Override
	public void close() throws MetadataStoreException {
		// nothing to do
	}

	@Override
	public boolean next() throws MetadataStoreException {
		return iterator.hasNext();
	}

	@Override
	public void skip(int rows) throws MetadataStoreException {
		throw new UnsupportedOperationException("skip is not implemented yet");
	}

	@Override
	public synchronized int getRemaining() throws MetadataStoreException {
		return foundRecords.size() - requestedRecords;
	}

	@Override
	public synchronized ISORecord getRecord() throws MetadataStoreException {
		requestedRecords++;
		return iterator.next();
	}

}
