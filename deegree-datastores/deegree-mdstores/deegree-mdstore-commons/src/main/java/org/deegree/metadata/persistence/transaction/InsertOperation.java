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
package org.deegree.metadata.persistence.transaction;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.metadata.MetadataRecord;
import org.deegree.protocol.csw.CSWConstants.TransactionType;

/**
 * Represents a CSW <code>Insert</code> action (part of a Transaction request).
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class InsertOperation extends TransactionOperation {

	private List<? extends MetadataRecord> records;

	private QName typeName;

	/**
	 * @param records
	 * @param typeName
	 * @param handle
	 */
	public InsertOperation(List<? extends MetadataRecord> records, QName typeName, String handle) {
		super(handle);
		this.records = records;
		this.typeName = typeName;

	}

	@Override
	public TransactionType getType() {
		return TransactionType.INSERT;
	}

	public List<? extends MetadataRecord> getRecords() {
		return records;
	}

	/**
	 * @return the typeName
	 */
	public QName getTypeName() {
		return typeName;
	}

}