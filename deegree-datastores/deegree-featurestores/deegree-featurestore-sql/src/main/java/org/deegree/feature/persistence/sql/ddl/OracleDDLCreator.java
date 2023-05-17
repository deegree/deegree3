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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

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

/**
 * Creates Oracle-DDL (DataDefinitionLanguage) scripts from {@link MappedAppSchema}
 * instances.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class OracleDDLCreator extends DDLCreator {

	private static Logger LOG = LoggerFactory.getLogger(OracleDDLCreator.class);

	/**
	 * Creates a new {@link OracleDDLCreator} instance for the given
	 * {@link MappedAppSchema}.
	 * @param schema mapped application schema, must not be <code>null</code>
	 * @param dialect SQL dialect, must not be <code>null</code>
	 */
	public OracleDDLCreator(MappedAppSchema schema, SQLDialect dialect) {
		super(schema, dialect);
	}

	@Override
	protected List<String> getBLOBCreates() {

		List<String> ddl = new ArrayList<String>();

		// create feature_type table
		TableName ftTable = schema.getBBoxMapping().getTable();
		ddl.add("CREATE TABLE " + ftTable
				+ " (id integer PRIMARY KEY, qname varchar2(4000) NOT NULL, bbox sdo_geometry)");

		// populate feature_type table
		for (short ftId = 0; ftId < schema.getFts(); ftId++) {
			QName ftName = schema.getFtName(ftId);
			ddl.add("INSERT INTO " + ftTable + "  (id,qname) VALUES (" + ftId + ",'" + ftName + "')");
		}

		// create gml_objects table
		TableName blobTable = schema.getBlobMapping().getTable();
		ddl.add("CREATE TABLE " + blobTable + " (id integer not null, "
				+ "gml_id varchar2(4000) NOT NULL, ft_type integer REFERENCES " + ftTable
				+ " , binary_object blob, gml_bounded_by sdo_GEOMETRY, constraint gml_objects_id_pk primary key(id))");

		ddl.add("create sequence " + blobTable + "_id_seq start with 1 increment by 1 nomaxvalue");

		ddl.add("create or replace trigger " + blobTable + "_id_trigger before insert on " + blobTable
				+ " for each row begin select " + blobTable + "_id_seq.nextval into :new.id from dual; end;");

		double[] dom = schema.getBlobMapping().getCRS().getValidDomain();

		ddl.add("INSERT INTO user_sdo_geom_metadata(TABLE_NAME,COLUMN_NAME,DIMINFO,SRID) VALUES (" + "'" + blobTable
				+ "','gml_bounded_by',SDO_DIM_ARRAY(" + "SDO_DIM_ELEMENT('X', " + dom[0] + ", " + dom[2]
				+ ", 0.00000005), SDO_DIM_ELEMENT('Y', " + dom[1] + ", " + dom[3] + ", 0.00000005)), null)");

		// TODO validity check, how?
		// ddl.add( "ALTER TABLE " + blobTable
		// + " ADD CONSTRAINT gml_objects_geochk CHECK (" + blobTable +
		// ".gml_bounded_by.st_isvalid()=1)" );

		ddl.add("CREATE INDEX gml_objects_sidx ON " + blobTable + "(gml_bounded_by) INDEXTYPE IS MDSYS.SPATIAL_INDEX");
		// ddl.add( "CREATE INDEX gml_objects_sidx ON " + blobTable + " USING GIST
		// (gml_bounded_by GIST_GEOMETRY_OPS)"
		// );
		// ddl.add( "CREATE TABLE gml_names (gml_object_id integer REFERENCES
		// gml_objects,"
		// + "name text NOT NULL,codespace text,prop_idx smallint NOT NULL)" );
		return ddl;
	}

	private List<StringBuffer> getGeometryCreate(GeometryMapping mapping, DBField dbField, TableName table) {
		List<StringBuffer> ddls = new ArrayList<StringBuffer>();
		String schema = table.getSchema() == null ? "" : table.getSchema();
		String column = dbField.getColumn();
		String srid = mapping.getSrid();

		double[] dom = mapping.getCRS().getValidDomain();
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO user_sdo_geom_metadata(TABLE_NAME,COLUMN_NAME,DIMINFO,SRID) VALUES (" + "'"
				+ table.toString().toUpperCase() + "','" + column.toUpperCase() + "',SDO_DIM_ARRAY("
				+ "SDO_DIM_ELEMENT('X', " + dom[0] + ", " + dom[2] + ", 0.00000005), SDO_DIM_ELEMENT('Y', " + dom[1]
				+ ", " + dom[3] + ", 0.00000005)), null)");
		ddls.add(sql);

		sql = new StringBuffer();
		sql.append("CREATE INDEX " + table.getTable().toUpperCase() + "_" + column.toUpperCase() + " ON "
				+ table.toString().toUpperCase() + "(" + column.toUpperCase() + ") INDEXTYPE IS MDSYS.SPATIAL_INDEX");
		ddls.add(sql);
		return ddls;
	}

	@Override
	protected StringBuffer createJoinedTable(TableName fromTable, TableJoin jc, List<StringBuffer> ddls,
			FIDMapping fidMapping) {
		StringBuffer sb = new StringBuffer("CREATE TABLE ");
		sb.append(jc.getToTable());
		sb.append(" (\n    ");
		sb.append("id integer not null,\n    constraint " + jc.getToTable() + "_id_pk primary key(id),\n    ");
		// TODO
		sb.append(jc.getToColumns().get(0));
		String primaryKeyType = retrieveTypeOfPrimaryKey(fromTable, jc.getFromColumns().get(0), fidMapping);
		sb.append(" ").append(primaryKeyType).append(" NOT NULL REFERENCES ");
		sb.append(fromTable);
		// TODO do this also for non-autogenerated schemas
		for (SQLIdentifier col : jc.getOrderColumns()) {
			sb.append(",\n    ").append(col).append(" integer not null");
		}

		ddls.add(sb);

		ddls.add(new StringBuffer("create sequence ").append(jc.getToTable())
			.append("_id_seq start with 1 increment by 1 nomaxvalue"));
		ddls.add(new StringBuffer("create or replace trigger ").append(jc.getToTable())
			.append("_id_trigger before insert on ")
			.append(jc.getToTable())
			.append(" for each row begin select ")
			.append(jc.getToTable())
			.append("_id_seq.nextval into :new.id from dual; end;"));

		return sb;
	}

	@Override
	protected String getDBType(BaseType type) {
		String postgresqlType = null;
		switch (type) {
			case BOOLEAN:
				postgresqlType = "char";
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
				postgresqlType = "varchar2(2000)";
				break;
			case TIME:
				postgresqlType = "time";
				break;
			default:
				throw new RuntimeException("Internal error. Unhandled primitive type '" + type + "'.");
		}
		return postgresqlType;
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
			DBField dbField = (DBField) me;
			sql.append(",\n    ");
			sql.append(dbField.getColumn());
			sql.append(" sdo_geometry");
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
			sql.append(" varchar(2000)");
		}
		MappingExpression hrefMe = mapping.getHrefMapping();
		if (hrefMe instanceof DBField) {
			sql.append(",\n    ");
			sql.append(((DBField) hrefMe).getColumn());
			sql.append(" varchar(2000)");
		}
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
		return "varchar(2000)";
	}

}