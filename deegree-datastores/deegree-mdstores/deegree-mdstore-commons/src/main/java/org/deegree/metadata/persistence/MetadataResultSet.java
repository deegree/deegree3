/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.metadata.persistence;

import java.sql.ResultSet;

import org.deegree.metadata.MetadataRecord;
import org.deegree.protocol.csw.MetadataStoreException;

/**
 * Has a corresponding {@link MetadataResultType} as content.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public interface MetadataResultSet<T extends MetadataRecord> {

	/**
	 * Must be invoked after using to close underlying resources, e.g. SQL
	 * {@link ResultSet}s.
	 */
	public void close() throws MetadataStoreException;

	/**
	 * Moves the cursor down one row from its current position. A ResultSet cursor is
	 * initially positioned before the first row; the first call to the method next makes
	 * the first row the current row; the second call makes the second row the current
	 * row, and so on.
	 * @return true, if the cursor was moved down one row, false otherwise (no rows left)
	 */
	public boolean next() throws MetadataStoreException;

	public void skip(int rows) throws MetadataStoreException;

	/**
	 * @return
	 * @throws MetadataStoreException
	 */
	public int getRemaining() throws MetadataStoreException;

	/**
	 * Returns the {@link MetadataRecord} at the current cursor position.
	 * @return record at the current position, never <code>null</code>
	 */
	public T getRecord() throws MetadataStoreException;

}