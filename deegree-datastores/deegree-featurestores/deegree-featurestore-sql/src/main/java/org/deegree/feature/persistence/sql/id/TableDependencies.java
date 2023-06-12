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
package org.deegree.feature.persistence.sql.id;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the {@link KeyPropagation}s defined by a {@link MappedAppSchema}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class TableDependencies {

	private static final Logger LOG = LoggerFactory.getLogger(TableDependencies.class);

	private final Map<TableName, LinkedHashSet<SQLIdentifier>> tableToGenerators = new HashMap<TableName, LinkedHashSet<SQLIdentifier>>();

	private final Map<TableName, LinkedHashSet<SQLIdentifier>> tableToKeyColumns = new HashMap<TableName, LinkedHashSet<SQLIdentifier>>();

	private final Map<TableName, LinkedHashSet<KeyPropagation>> tableToParents = new HashMap<TableName, LinkedHashSet<KeyPropagation>>();

	private final Map<TableName, LinkedHashSet<KeyPropagation>> tableToChildren = new HashMap<TableName, LinkedHashSet<KeyPropagation>>();

	private final boolean deleteCascadingByDB;

	/**
	 * Creates a new {@link TableDependencies} instance.
	 * @param ftMappings mapped feature types, can be <code>null</code>
	 * @param deleteCascadingByDB set to <code>true</code>, if the database cascades
	 * deletes, <code>false</code> otherwise (manual deletion required)
	 */
	public TableDependencies(FeatureTypeMapping[] ftMappings, boolean deleteCascadingByDB) {
		if (ftMappings != null) {
			for (FeatureTypeMapping ftMapping : ftMappings) {
				scanFeatureTypeMapping(ftMapping);
			}
		}
		this.deleteCascadingByDB = deleteCascadingByDB;
	}

	private void scanFeatureTypeMapping(FeatureTypeMapping ftMapping) {
		scanFidGenerator(ftMapping);
		TableName ftTable = ftMapping.getFtTable();
		for (Mapping particle : ftMapping.getMappings()) {
			scanParticle(particle, ftTable);
		}
	}

	private void scanFidGenerator(FeatureTypeMapping ftMapping) {
		FIDMapping fidMapping = ftMapping.getFidMapping();
		for (Pair<SQLIdentifier, BaseType> columnAndType : fidMapping.getColumns()) {
			SQLIdentifier fidColumn = columnAndType.first;
			addAutoColumn(ftMapping.getFtTable(), fidColumn);
			addKeyColumn(ftMapping.getFtTable(), fidColumn);
		}
	}

	private void scanParticle(Mapping particle, TableName currentTable) {
		List<TableJoin> joins = particle.getJoinedTable();
		if (joins != null && !joins.isEmpty()) {
			if (particle instanceof FeatureMapping) {
				if (joins.size() != 1) {
					String msg = "Feature type joins with more than one table are not supported yet.";
					throw new UnsupportedOperationException(msg);
				}
				// feature type joins are "special" (as features are independent objects),
				// so
				// don't treat such a join as a dependency
				return;
			}
			for (TableJoin join : joins) {
				TableName joinTable = scanJoin(currentTable, join);
				currentTable = joinTable;
			}
		}
		if (particle instanceof CompoundMapping) {
			for (Mapping child : ((CompoundMapping) particle).getParticles()) {
				scanParticle(child, currentTable);
			}
		}
	}

	private TableName scanJoin(TableName currentTable, TableJoin join) {

		boolean found = false;
		TableName joinTable = join.getToTable();

		// check for propagations from current table to joined table
		List<SQLIdentifier> fromColumns = join.getFromColumns();
		List<SQLIdentifier> toColumns = join.getToColumns();
		Set<SQLIdentifier> generatedColumns = tableToGenerators.get(currentTable);
		if (generatedColumns != null && generatedColumns.containsAll(fromColumns)) {
			KeyPropagation prop = new KeyPropagation(join.getFromTable(), fromColumns, joinTable, toColumns);
			LOG.debug("Found key propagation (to join table): " + prop);
			addChild(currentTable, prop);
			addParent(joinTable, prop);
			found = true;
		}

		// add generated columns and check for propagations from joined table to current
		// table
		Set<SQLIdentifier> generatedColumnsJoinTable = join.getKeyColumnToGenerator().keySet();
		if (generatedColumnsJoinTable != null) {
			for (SQLIdentifier generatedColumnJoinTable : generatedColumnsJoinTable) {
				addAutoColumn(joinTable, generatedColumnJoinTable);
				addKeyColumn(joinTable, generatedColumnJoinTable);
			}
			if (generatedColumnsJoinTable.containsAll(toColumns)) {
				KeyPropagation prop = new KeyPropagation(joinTable, toColumns, join.getFromTable(), fromColumns);
				LOG.debug("Found key propagation (from join table): " + prop);
				addChild(joinTable, prop);
				addParent(currentTable, prop);
				found = true;
			}
		}

		if (!found) {
			assumeParentToJoinTablePropagation(currentTable, join);
		}
		return joinTable;
	}

	private void assumeParentToJoinTablePropagation(TableName currentTable, TableJoin join) {
		List<SQLIdentifier> fromColumns = join.getFromColumns();
		List<SQLIdentifier> toColumns = join.getToColumns();
		TableName joinTable = join.getToTable();
		KeyPropagation prop = new KeyPropagation(join.getFromTable(), fromColumns, joinTable, toColumns);
		LOG.debug("Found key propagation (to join table): " + prop);
		addChild(currentTable, prop);
		addParent(joinTable, prop);
		for (SQLIdentifier fromColumn : fromColumns) {
			addKeyColumn(currentTable, fromColumn);
		}
		String msg = "Join " + join + " does not contain key generator on either side. "
				+ "Assuming propagation from parent to joined table: " + prop;
		LOG.warn(msg);
	}

	private void addAutoColumn(TableName table, SQLIdentifier autoColumn) {
		LinkedHashSet<SQLIdentifier> autoColumns = tableToGenerators.get(table);
		if (autoColumns == null) {
			autoColumns = new LinkedHashSet<SQLIdentifier>();
			tableToGenerators.put(table, autoColumns);
		}
		autoColumns.add(autoColumn);
	}

	private void addKeyColumn(TableName table, SQLIdentifier keyColumn) {
		LinkedHashSet<SQLIdentifier> keyColumns = tableToKeyColumns.get(table);
		if (keyColumns == null) {
			keyColumns = new LinkedHashSet<SQLIdentifier>();
			tableToKeyColumns.put(table, keyColumns);
		}
		keyColumns.add(keyColumn);
	}

	private void addParent(TableName table, KeyPropagation propagation) {
		LinkedHashSet<KeyPropagation> parents = tableToParents.get(table);
		if (parents == null) {
			parents = new LinkedHashSet<KeyPropagation>();
			tableToParents.put(table, parents);
		}
		parents.add(propagation);
	}

	private void addChild(TableName table, KeyPropagation propagation) {
		LinkedHashSet<KeyPropagation> children = tableToChildren.get(table);
		if (children == null) {
			children = new LinkedHashSet<KeyPropagation>();
			tableToChildren.put(table, children);
		}
		children.add(propagation);
	}

	/**
	 * Returns the columns that are auto-generated on insert for the given table.
	 * @param table name of the table, must not be <code>null</code>
	 * @return columns that are auto-generated on insert, can be <code>null</code> (no
	 * autogenerated columns)
	 */
	public Set<SQLIdentifier> getGeneratedColumns(TableName table) {
		return tableToGenerators.get(table);
	}

	/**
	 * Returns the columns that are used as keys in the given table.
	 * @param table name of the table, must not be <code>null</code>
	 * @return columns that are used as keys, can be <code>null</code> (no autogenerated
	 * columns)
	 */
	public Set<SQLIdentifier> getKeyColumns(TableName table) {
		return tableToKeyColumns.get(table);
	}

	/**
	 * Performs a lookup for the specified key propagation.
	 * @param table1 first table, must not be <code>null</code>
	 * @param table1KeyColumns key columns (of first table), must not be <code>null</code>
	 * and contain at least one entry
	 * @param table2 second table, must not be <code>null</code>
	 * @param table2KeyColumns key columns (of second table), must not be
	 * <code>null</code> and contain at least one entry
	 * @return key propagation, can be <code>null</code> (no such propagation defined)
	 */
	public KeyPropagation findKeyPropagation(TableName table1, List<SQLIdentifier> table1KeyColumns, TableName table2,
			List<SQLIdentifier> table2KeyColumns) {
		KeyPropagation fromToTo = new KeyPropagation(table1, table1KeyColumns, table2, table2KeyColumns);
		KeyPropagation toToFrom = new KeyPropagation(table2, table2KeyColumns, table1, table1KeyColumns);

		Set<KeyPropagation> candidates = new HashSet<KeyPropagation>();
		if (tableToChildren.get(table1) != null) {
			candidates.addAll(tableToChildren.get(table1));
		}
		if (tableToChildren.get(table2) != null) {
			candidates.addAll(tableToChildren.get(table2));
		}
		for (KeyPropagation candidate : candidates) {
			if (candidate.equals(fromToTo) || candidate.equals(toToFrom)) {
				return candidate;
			}
		}
		return null;
	}

	public boolean getDeleteCascadingByDB() {
		return deleteCascadingByDB;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<TableName> tables = new TreeSet<TableName>();
		tables.addAll(tableToGenerators.keySet());
		tables.addAll(tableToParents.keySet());
		tables.addAll(tableToChildren.keySet());
		for (TableName table : tables) {
			sb.append("\n\nTable: " + table);
			sb.append("\n -Generated key columns:");
			if (tableToGenerators.get(table) != null) {
				for (SQLIdentifier autoColumn : tableToGenerators.get(table)) {
					sb.append("\n  -" + autoColumn);
				}
			}
			sb.append("\n -Parents:");
			if (tableToParents.get(table) != null) {
				for (KeyPropagation parent : tableToParents.get(table)) {
					sb.append("\n  -" + parent);
				}
			}
			sb.append("\n -Children:");
			if (tableToChildren.get(table) != null) {
				for (KeyPropagation child : tableToChildren.get(table)) {
					sb.append("\n  -" + child);
				}
			}
		}
		return sb.toString();
	}

}
