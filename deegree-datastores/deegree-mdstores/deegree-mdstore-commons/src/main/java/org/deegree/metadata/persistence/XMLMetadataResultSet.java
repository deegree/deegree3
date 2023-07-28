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
package org.deegree.metadata.persistence;

import java.io.BufferedInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.metadata.MetadataRecord;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * abstract base class for {@link MetadataResultSet}s. The reults set must contain the XML
 * representation of the metadata record as binary at the first position!
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public abstract class XMLMetadataResultSet<T extends MetadataRecord> implements MetadataResultSet<T> {

	private static Logger LOG = LoggerFactory.getLogger(XMLMetadataResultSet.class);

	private final ResultSet rs;

	private final Connection conn;

	private final PreparedStatement stmt;

	public XMLMetadataResultSet(ResultSet rs, Connection conn, PreparedStatement stmt) {
		this.rs = rs;
		this.conn = conn;
		this.stmt = stmt;
	}

	@Override
	public void close() throws MetadataStoreException {
		JDBCUtils.close(rs, stmt, conn, LOG);
	}

	@Override
	public T getRecord() throws MetadataStoreException {
		try {
			BufferedInputStream bais = new BufferedInputStream(rs.getBinaryStream(1));
			XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(bais);
			return getRecord(xmlReader);
		}
		catch (Exception e) {
			throw new MetadataStoreException("Error re-creating MetadataRecord from result set: " + e.getMessage());
		}
	}

	@Override
	public void skip(int rows) throws MetadataStoreException {
		try {
			for (int i = 0; i < rows; i++) {
				rs.next();
			}
		}
		catch (SQLException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
	}

	@Override
	public int getRemaining() throws MetadataStoreException {
		int i = 0;
		try {
			while (rs.next()) {
				i++;
			}
		}
		catch (SQLException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
		return i;
	}

	@Override
	public boolean next() throws MetadataStoreException {
		try {
			return rs.next();
		}
		catch (SQLException e) {
			throw new MetadataStoreException(e.getMessage(), e);
		}
	}

	protected abstract T getRecord(XMLStreamReader xmlReader);

}