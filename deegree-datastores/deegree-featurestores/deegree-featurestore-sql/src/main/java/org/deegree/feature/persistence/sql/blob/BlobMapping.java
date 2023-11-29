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
package org.deegree.feature.persistence.sql.blob;

import org.deegree.commons.jdbc.TableName;
import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;

/**
 * Encapsulates the BLOB mapping parameters of a {@link MappedAppSchema}.
 *
 * @see MappedAppSchema
 * @see FeatureTypeMapping
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class BlobMapping {

	private final TableName table;

	private final ICRS storageCRS;

	private final BlobCodec codec;

	/**
	 * Creates a new {@link BlobMapping} instance.
	 * @param table the name of the table that stores the BLOBs, must not be
	 * <code>null</code>
	 * @param storageCRS crs used for storing geometries / envelopes, must not be
	 * <code>null</code>
	 * @param codec the decoder / encoder used for the BLOBs, must not be
	 * <code>null</code>
	 */
	public BlobMapping(String table, ICRS storageCRS, BlobCodec codec) {
		this.table = new TableName(table);
		this.storageCRS = storageCRS;
		this.codec = codec;
	}

	/**
	 * Returns the table that stores the BLOBs.
	 * @return the table that stores the BLOBs, never <code>null</code>
	 */
	public TableName getTable() {
		return table;
	}

	/**
	 * Returns the {@link CRS} used for storing the geometries / envelopes.
	 * @return the crs, never <code>null</code>
	 */
	public ICRS getCRS() {
		return storageCRS;
	}

	/**
	 * Returns the {@link BlobCodec} for encoding and decoding features / geometries.
	 * @return the codec, never <code>null</code>
	 */
	public BlobCodec getCodec() {
		return codec;
	}

	/**
	 * Returns the name of the column that stores the gml ids.
	 * @return the name of the column, never <code>null</code>
	 */
	public String getGMLIdColumn() {
		return "gml_id";
	}

	/**
	 * @return
	 */
	public String getDataColumn() {
		return "binary_object";
	}

	/**
	 * @return
	 */
	public String getBBoxColumn() {
		return "gml_bounded_by";
	}

	/**
	 * @return
	 */
	public String getTypeColumn() {
		return "ft_type";
	}

	public String getInternalIdColumn() {
		return "id";
	}

	public String getXPlanInternalIdColumn() {
		return "internal_id";
	}

	public String getXPlanIdColumn() {
		return "plan_id";
	}

	public String getXPlanNameColumn() {
		return "plan_name";
	}

	public String getXPlanRechtsstandColumn() {
		return "rechtsstand";
	}

}