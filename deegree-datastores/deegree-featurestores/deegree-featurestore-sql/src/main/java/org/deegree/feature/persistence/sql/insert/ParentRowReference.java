/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql.insert;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.feature.persistence.sql.id.KeyPropagation;

/**
 * A reference from an {@link InsertRow} to a parent {@link InsertRow}.
 * <p>
 * The parent provides values for foreign key columns.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
class ParentRowReference {

	private final InsertRow parent;

	private final KeyPropagation propagation;

	private final Map<InsertRow, SQLIdentifier> hrefingRows = new HashMap<InsertRow, SQLIdentifier>();

	ParentRowReference(InsertRow parent, KeyPropagation propagation) {
		this.parent = parent;
		this.propagation = propagation;
	}

	InsertRow getTarget() {
		return parent;
	}

	/**
	 * Returns the {@link KeyPropagation} from the parent {@link InsertRow}.
	 * @return key propagation, never <code>null</code>
	 */
	KeyPropagation getKeyPropagation() {
		return propagation;
	}

	void addHrefingRow(InsertRow row, SQLIdentifier hrefCol) {
		hrefingRows.put(row, hrefCol);
	}

	boolean isHrefed(InsertRow childInsertRow) {
		return hrefingRows.containsKey(childInsertRow);
	}

	SQLIdentifier getHrefColum(InsertRow childInsertRow) {
		return hrefingRows.get(childInsertRow);
	}

}
