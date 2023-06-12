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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.sql.FeatureBuilder;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds {@link Feature} instances from SQL result set rows (BLOB/hybrid mode).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class FeatureBuilderBlob implements FeatureBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(FeatureBuilderBlob.class);

	private final SQLFeatureStore fs;

	private final BlobMapping blobMapping;

	private final BlobCodec codec;

	private final ICRS crs;

	private TypeName[] typeNames;

	/**
	 * Creates a new {@link FeatureBuilderBlob} instance.
	 * @param fs feature store, must not be <code>null</code>
	 * @param blobMapping blob mapping parameters, must not be <code>null</code>
	 */
	public FeatureBuilderBlob(SQLFeatureStore fs, BlobMapping blobMapping) {
		this(fs, blobMapping, null);
	}

	/**
	 * Creates a new {@link FeatureBuilderBlob} instance.
	 * @param fs feature store, must not be <code>null</code>
	 * @param blobMapping blob mapping parameters, must not be <code>null</code>
	 * @param typeNames list of requested type names, if {@link #buildFeature(ResultSet)}
	 * is invoked and a feature is not in this list an exception is thrown
	 */
	public FeatureBuilderBlob(SQLFeatureStore fs, BlobMapping blobMapping, TypeName[] typeNames) {
		this.fs = fs;
		this.blobMapping = blobMapping;
		this.codec = blobMapping.getCodec();
		this.crs = blobMapping.getCRS();
		this.typeNames = typeNames;
	}

	@Override
	public List<String> getInitialSelectList() {
		List<String> columns = new ArrayList<String>();
		columns.add(blobMapping.getGMLIdColumn());
		columns.add(blobMapping.getDataColumn());
		return columns;
	}

	@Override
	public Feature buildFeature(ResultSet rs) throws SQLException {
		Feature feature = null;
		try {
			String gmlId = rs.getString(1);
			if (fs.getCache() != null) {
				feature = (Feature) fs.getCache().get(gmlId);
			}
			if (feature == null) {
				LOG.debug("Recreating object '" + gmlId + "' from db (BLOB/hybrid mode).");
				feature = (Feature) codec.decode(rs.getBinaryStream(2), fs.getNamespaceContext(), fs.getSchema(), crs,
						fs.getResolver());
				if (fs.getCache() != null) {
					fs.getCache().add(feature);
				}
			}
			else {
				LOG.debug("Cache hit.");
			}
			fs.checkIfFeatureTypIsRequested(typeNames, feature.getType());
		}
		catch (Exception e) {
			String msg = "Cannot recreate feature from result set: " + e.getMessage();
			throw new SQLException(msg, e);
		}
		return feature;
	}

}