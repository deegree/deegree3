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
package org.deegree.feature.persistence.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.sqldialect.SortCriterion;

/**
 * Defines the mapping between a {@link FeatureType} and tables in a relational database.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class FeatureTypeMapping {

	private final QName ftName;

	private final TableName table;

	private final FIDMapping fidMapping;

	private final List<SortCriterion> defaultSortCriteria;

	private final Map<QName, Mapping> propToMapping;

	private final List<Mapping> particles = new ArrayList<Mapping>();

	/**
	 * Creates a new {@link FeatureTypeMapping} instance.
	 * @param ftName name of the mapped feature type, must not be <code>null</code>
	 * @param table name of the database table that the feature type is mapped to, must
	 * not be <code>null</code>
	 * @param fidMapping mapping for the feature id, must not be <code>null</code>
	 * @param particleMappings particle mappings for the feature type, must not be
	 * <code>null</code>
	 * @param defaultSortCriteria default sort criterion for the feature type, must not be
	 * <code>null</code>
	 */
	public FeatureTypeMapping(QName ftName, TableName table, FIDMapping fidMapping, List<Mapping> particleMappings,
			List<SortCriterion> defaultSortCriteria) {
		this.ftName = ftName;
		this.table = table;
		this.fidMapping = fidMapping;
		this.defaultSortCriteria = defaultSortCriteria;
		this.propToMapping = new HashMap<QName, Mapping>();
		// TODO cope with non-QName XPaths as well
		for (Mapping mapping : particleMappings) {
			if (mapping != null && mapping.getPath().getAsQName() != null) {
				propToMapping.put(mapping.getPath().getAsQName(), mapping);
			}
		}
		for (Mapping mapping : particleMappings) {
			if (mapping != null) {
				this.particles.add(mapping);
			}
		}
	}

	/**
	 * Returns the name of the feature type.
	 * @return name of the feature type, never <code>null</code>
	 */
	public QName getFeatureType() {
		return ftName;
	}

	/**
	 * Returns the identifier of the table that the feature type is mapped to.
	 * @return identifier of the table, never <code>null</code>
	 */
	public TableName getFtTable() {
		return table;
	}

	/**
	 * Returns the feature id mapping.
	 * @return mapping for the feature id, never <code>null</code>
	 */
	public FIDMapping getFidMapping() {
		return fidMapping;
	}

	/**
	 * Returns the mapping parameters for the specified property.
	 * @param propName name of the property, must not be <code>null</code>
	 * @return mapping, may be <code>null</code> (if the property is not mapped)
	 */
	@Deprecated
	public Mapping getMapping(QName propName) {
		return propToMapping.get(propName);
	}

	/**
	 * Returns the {@link Mapping} particles.
	 * @return mapping particles, may be empty, but never <code>null</code>
	 */
	public List<Mapping> getMappings() {
		return particles;
	}

	/**
	 * Returns the default {@link SortCriterion}.
	 * @return sort criterions, may be empty, but never <code>null</code>
	 */
	public List<SortCriterion> getDefaultSortCriteria() {
		return defaultSortCriteria;
	}

	/**
	 * Returns the default (i.e. the first) {@link GeometryMapping}.
	 * @return default geometry mapping, may be <code>null</code> (no geometry mapping
	 * defined)
	 */
	public Pair<TableName, GeometryMapping> getDefaultGeometryMapping() {
		TableName table = getFtTable();
		for (Mapping particle : particles) {
			if (particle instanceof GeometryMapping) {
				List<TableJoin> joins = particle.getJoinedTable();
				if (joins != null && !joins.isEmpty()) {
					table = joins.get(joins.size() - 1).getToTable();
				}
				return new Pair<TableName, GeometryMapping>(table, (GeometryMapping) particle);
			}
		}
		for (Mapping particle : particles) {
			TableName propTable = table;
			if (particle instanceof CompoundMapping) {
				List<TableJoin> joins = particle.getJoinedTable();
				if (joins != null && !joins.isEmpty()) {
					propTable = joins.get(joins.size() - 1).getToTable();
				}
				Pair<TableName, GeometryMapping> gm = getDefaultGeometryMapping(propTable, (CompoundMapping) particle);
				if (gm != null) {
					return gm;
				}
			}
		}
		return null;
	}

	private Pair<TableName, GeometryMapping> getDefaultGeometryMapping(TableName table, CompoundMapping complex) {
		for (Mapping particle : complex.getParticles()) {
			if (particle instanceof GeometryMapping) {
				List<TableJoin> joins = particle.getJoinedTable();
				if (joins != null && !joins.isEmpty()) {
					table = joins.get(joins.size() - 1).getToTable();
				}
				return new Pair<TableName, GeometryMapping>(table, (GeometryMapping) particle);
			}
		}
		for (Mapping particle : complex.getParticles()) {
			TableName propTable = table;
			if (particle instanceof CompoundMapping) {
				List<TableJoin> joins = particle.getJoinedTable();
				if (joins != null && !joins.isEmpty()) {
					propTable = joins.get(joins.size() - 1).getToTable();
				}
				Pair<TableName, GeometryMapping> gm = getDefaultGeometryMapping(propTable, (CompoundMapping) particle);
				if (gm != null) {
					return gm;
				}
			}
		}
		return null;
	}

}