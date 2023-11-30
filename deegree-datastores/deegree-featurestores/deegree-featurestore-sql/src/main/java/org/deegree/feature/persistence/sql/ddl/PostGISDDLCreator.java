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
package org.deegree.feature.persistence.sql.ddl;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Creates PostGIS-DDL (DataDefinitionLanguage) scripts from {@link MappedAppSchema}
 * instances.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class PostGISDDLCreator extends DDLCreator {

	private static Logger LOG = LoggerFactory.getLogger(PostGISDDLCreator.class);

	private final String undefinedSrid;

	private int spatialIndexIndex = 0;

	/**
	 * Creates a new {@link PostGISDDLCreator} instance for the given
	 * {@link MappedAppSchema}.
	 * @param schema mapped application schema, must not be <code>null</code>
	 * @param dialect SQL dialect, must not be <code>null</code>
	 */
	public PostGISDDLCreator(MappedAppSchema schema, SQLDialect dialect) {
		super(schema, dialect);
		this.undefinedSrid = dialect.getUndefinedSrid();
	}

	@Override
	protected List<String> getBLOBCreates() {
		String srid = detectSrid();
		List<String> ddl = new ArrayList<String>();

		// create feature_type table
		TableName ftTable = schema.getBBoxMapping().getTable();
		String ftTableSchema = ftTable.getSchema() == null ? "public" : ftTable.getSchema();
		ddl.add("CREATE TABLE " + ftTable + " (id smallint PRIMARY KEY, qname text NOT NULL)");
		ddl.add("COMMENT ON TABLE " + ftTable + " IS 'Ids and bboxes of concrete feature types'");
		ddl.add("SELECT ADDGEOMETRYCOLUMN('" + ftTableSchema.toLowerCase() + "', '" + ftTable.getTable().toLowerCase()
				+ "','bbox','" + srid + "','GEOMETRY',2)");

		// populate feature_type table
		for (short ftId = 0; ftId < schema.getFts(); ftId++) {
			QName ftName = schema.getFtName(ftId);
			ddl.add("INSERT INTO " + ftTable + "  (id,qname) VALUES (" + ftId + ",'" + ftName + "')");
		}

		// create gml_objects table
		TableName blobTable = schema.getBlobMapping().getTable();
		String blobTableSchema = blobTable.getSchema() == null ? "public" : blobTable.getSchema();
		ddl.add("CREATE TABLE " + blobTable + " (id serial PRIMARY KEY, "
				+ "gml_id text UNIQUE NOT NULL, ft_type smallint REFERENCES " + ftTable + " , binary_object bytea)");
		ddl.add("COMMENT ON TABLE " + blobTable + " IS 'All objects (features and geometries)'");
		ddl.add("SELECT ADDGEOMETRYCOLUMN('" + blobTableSchema.toLowerCase() + "', '"
				+ blobTable.getTable().toLowerCase() + "','gml_bounded_by','" + srid + "','GEOMETRY',2)");
		ddl.add("ALTER TABLE " + blobTable + " ADD CONSTRAINT gml_objects_geochk CHECK (ST_IsValid(gml_bounded_by))");
		ddl.add("CREATE INDEX gml_objects_sidx ON " + blobTable + "  USING GIST (gml_bounded_by)");
		// ddl.add( "CREATE TABLE gml_names (gml_object_id integer REFERENCES
		// gml_objects,"
		// + "name text NOT NULL,codespace text,prop_idx smallint NOT NULL)" );
		return ddl;
	}

	private String detectSrid() {
		String srid = schema.getGeometryParams().getSrid();
		if (srid != null)
			return srid;
		return undefinedSrid;
	}

	private List<StringBuffer> getGeometryCreate(GeometryMapping mapping, DBField dbField, TableName table) {
		List<StringBuffer> ddls = new ArrayList<StringBuffer>();
		StringBuffer sql = new StringBuffer();
		String schema = table.getSchema() == null ? "" : table.getSchema();
		String column = dbField.getColumn();
		String srid = mapping.getSrid();
		// TODO
		String geometryType = "GEOMETRY";
		int dim = 2;
		sql.append("SELECT ADDGEOMETRYCOLUMN('" + schema.toLowerCase() + "', '" + table.getTable().toLowerCase() + "','"
				+ column + "','" + srid + "','" + geometryType + "', " + dim + ")");
		ddls.add(sql);

		StringBuffer indexSql = new StringBuffer("CREATE INDEX ");
		String idxName = createIdxName(table.getTable(), column);
		indexSql.append(idxName);
		indexSql.append(" ON ").append(table.getTable().toLowerCase());
		indexSql.append(" USING GIST (").append(column).append(" ); ");
		ddls.add(indexSql);
		return ddls;
	}

	@Override
	protected void primitiveMappingSnippet(StringBuffer sql, PrimitiveMapping mapping) {
		MappingExpression me = mapping.getMapping();
		if (me instanceof DBField) {
			DBField dbField = (DBField) me;
			sql.append(",\n    ");
			sql.append(dbField.getColumn());
			sql.append(" ");
			sql.append(getDBType(mapping.getType().getBaseType()));
		}
	}

	@Override
	protected void geometryMappingSnippet(StringBuffer sql, GeometryMapping mapping, List<StringBuffer> ddls,
			TableName table) {
		MappingExpression me = mapping.getMapping();
		if (me instanceof DBField) {
			ddls.addAll(getGeometryCreate(mapping, (DBField) me, table));
		}
		else {
			LOG.info("Skipping geometry mapping -- not mapped to a db field. ");
		}
	}

	@Override
	protected void featureMappingSnippet(StringBuffer sql, FeatureMapping mapping) {
		SQLIdentifier col = mapping.getJoinedTable().get(mapping.getJoinedTable().size() - 1).getFromColumns().get(0);
		if (col != null) {
			sql.append(",\n    ");
			sql.append(col);
			sql.append(" text");
		}
		MappingExpression hrefMe = mapping.getHrefMapping();
		if (hrefMe instanceof DBField) {
			sql.append(",\n    ");
			sql.append(((DBField) hrefMe).getColumn());
			sql.append(" text");
		}
	}

	@Override
	protected StringBuffer createJoinedTable(TableName fromTable, TableJoin jc, List<StringBuffer> ddls,
			FIDMapping fidMapping) {
		StringBuffer sb = new StringBuffer("CREATE TABLE ");
		sb.append(jc.getToTable());
		sb.append(" (\n    ");
		sb.append("id serial PRIMARY KEY,\n    ");
		// TODO
		sb.append(jc.getToColumns().get(0));
		String primaryKeyType = retrieveTypeOfPrimaryKey(fromTable, jc.getFromColumns().get(0), fidMapping);
		sb.append(" ").append(primaryKeyType).append(" NOT NULL REFERENCES ");
		sb.append(fromTable);
		sb.append(" ON DELETE CASCADE");
		// TODO do this also for non-autogenerated schemas
		for (SQLIdentifier col : jc.getOrderColumns()) {
			sb.append(",\n    ").append(col).append(" integer not null");
		}
		return sb;
	}

	@Override
	protected String getDBType(BaseType type) {
		String postgresqlType = null;
		switch (type) {
			case BOOLEAN:
				postgresqlType = "boolean";
				break;
			case DATE:
				postgresqlType = "date";
				break;
			case DATE_TIME:
				postgresqlType = "timestamp";
				break;
			case DECIMAL:
				postgresqlType = "numeric";
				break;
			case DOUBLE:
				postgresqlType = "float";
				break;
			case INTEGER:
				postgresqlType = "integer";
				break;
			case STRING:
				postgresqlType = "text";
				break;
			case TIME:
				postgresqlType = "time";
				break;
			default:
				throw new RuntimeException("Internal error. Unhandled primitive type '" + type + "'.");
		}
		return postgresqlType;
	}

	private String retrieveTypeOfPrimaryKey(TableName fromTable, SQLIdentifier toColumn, FIDMapping fidMapping) {
		if (fidMapping != null) {
			for (Pair<SQLIdentifier, BaseType> column : fidMapping.getColumns()) {
				if (toColumn.equals(column.getFirst())) {
					return getDBType(column.getSecond());
				}
			}
		}
		// TODO implement this correctly
		// in joins not connected to the main feature type table 'integer' is used by
		// default
		if (!fromTable.equals(currentFtTable)) {
			return "integer";
		}
		return "text";
	}

	private String createIdxName(String tableName, String columnName) {
		String id = "spidx_" + tableName.toLowerCase() + "_" + columnName.toLowerCase();
		int maxColumnNameLength = dialect.getMaxColumnNameLength();
		if (id.length() >= maxColumnNameLength) {
			String idAsString = Integer.toString(this.spatialIndexIndex++);
			String suffix = "_" + idAsString;
			int delta = id.length() - maxColumnNameLength;
			int substringUntilPos = id.length() - delta - suffix.length();
			if (substringUntilPos >= 0) {
				String substring = id.substring(0, substringUntilPos);
				return substring + suffix;
			}
			else if (maxColumnNameLength == idAsString.length()) {
				return idAsString;
			}
			else {
				return UUID.randomUUID().toString().substring(0, maxColumnNameLength);
			}
		}
		return id;
	}

}