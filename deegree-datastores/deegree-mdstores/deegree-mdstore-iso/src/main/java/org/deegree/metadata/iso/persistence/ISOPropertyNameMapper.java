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
package org.deegree.metadata.iso.persistence;

import static org.deegree.commons.tom.primitive.BaseType.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.DATE_TIME;
import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;
import static org.deegree.commons.tom.primitive.BaseType.INTEGER;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.xml.CommonNamespaces.OWS_NS;
import static org.deegree.cs.CRSUtils.EPSG_4326;
import static org.deegree.protocol.csw.CSWConstants.APISO_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.DCT_NS;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.persistence.queryable.Queryable;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.Join;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.PropertyNameMapping;
import org.deegree.sqldialect.filter.TableAliasManager;
import org.deegree.sqldialect.filter.UnmappableException;
import org.slf4j.Logger;

/**
 * Implementation of the {@link PropertyNameMapper}. It's the base class for access to the
 * backend. Is there any change in the database schema for the {@link ISOMetadataStore}
 * then in this class should be changed the binding, as well.
 * <p>
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class ISOPropertyNameMapper implements PropertyNameMapper {

	private static final Logger LOG = getLogger(ISOPropertyNameMapper.class);

	private static Map<QName, Triple<Pair<String, String>, Boolean, BaseType>> propToTableAndCol = new HashMap<QName, Triple<Pair<String, String>, Boolean, BaseType>>();

	/**
	 * XML element name in the representation of the response
	 */
	public final static String RECORD = "Record";

	static {

		// ----------------------------------------------------------------------------------------
		// ----------------------<common queryable
		// properties>-------------------------------------

		addStringProp(APISO_NS, "title", DatabaseTables.idxtb_main, "title", true);
		addStringProp(APISO_NS, "Title", DatabaseTables.idxtb_main, "title", true);
		addStringProp("", "Title", DatabaseTables.idxtb_main, "title", true);
		addStringProp(DC_NS, "Title", DatabaseTables.idxtb_main, "title", true);
		addStringProp(CSW_202_NS, "Title", DatabaseTables.idxtb_main, "title", true);
		addStringProp(APISO_NS, "abstract", DatabaseTables.idxtb_main, "abstract", true);
		addStringProp(APISO_NS, "Abstract", DatabaseTables.idxtb_main, "abstract", true);
		addStringProp(DCT_NS, "Abstract", DatabaseTables.idxtb_main, "abstract", true);
		addStringProp("", "Abstract", DatabaseTables.idxtb_main, "abstract", true);
		addStringProp(CSW_202_NS, "Abstract", DatabaseTables.idxtb_main, "abstract", true);
		addGeometryProp(APISO_NS, "BoundingBox", DatabaseTables.idxtb_main, "bbox", false);
		addGeometryProp(DC_NS, "coverage", DatabaseTables.idxtb_main, "bbox", false);
		addGeometryProp(OWS_NS, "BoundingBox", DatabaseTables.idxtb_main, "bbox", false);
		addGeometryProp(OWS_NS, "boundingBox", DatabaseTables.idxtb_main, "bbox", false);
		addGeometryProp("", "boundingBox", DatabaseTables.idxtb_main, "bbox", false);
		addGeometryProp(CSW_202_NS, "BoundingBox", DatabaseTables.idxtb_main, "bbox", false);
		addStringProp(APISO_NS, "type", DatabaseTables.idxtb_main, "type", false);
		addStringProp(APISO_NS, "Type", DatabaseTables.idxtb_main, "type", false);
		addStringProp(DC_NS, "Type", DatabaseTables.idxtb_main, "type", false);
		addStringProp("", "Type", DatabaseTables.idxtb_main, "type", false);
		addStringProp(CSW_202_NS, "Type", DatabaseTables.idxtb_main, "type", false);
		addStringProp(APISO_NS, "format", DatabaseTables.idxtb_main, "formats", true);
		addStringProp(APISO_NS, "Format", DatabaseTables.idxtb_main, "formats", true);
		addStringProp(DC_NS, "Format", DatabaseTables.idxtb_main, "formats", true);
		addStringProp("", "Format", DatabaseTables.idxtb_main, "formats", true);
		addStringProp(CSW_202_NS, "Format", DatabaseTables.idxtb_main, "formats", true);
		addStringProp(APISO_NS, "Subject", DatabaseTables.idxtb_keyword, "keywords", true);
		addStringProp(APISO_NS, "subject", DatabaseTables.idxtb_keyword, "keywords", true);
		addStringProp(DC_NS, "Subject", DatabaseTables.idxtb_keyword, "keywords", true);
		addStringProp("", "Subject", DatabaseTables.idxtb_keyword, "keywords", true);
		addStringProp(CSW_202_NS, "Subject", DatabaseTables.idxtb_keyword, "keywords", true);
		addStringProp(APISO_NS, "AnyText", DatabaseTables.idxtb_main, "anytext", false);
		addStringProp(APISO_NS, "anyText", DatabaseTables.idxtb_main, "anytext", false);
		addStringProp(CSW_202_NS, "AnyText", DatabaseTables.idxtb_main, "anytext", false);
		addStringProp("", "AnyText", DatabaseTables.idxtb_main, "anytext", false);
		addStringProp(APISO_NS, "identifier", DatabaseTables.idxtb_main, "fileidentifier", false);
		addStringProp(APISO_NS, "Identifier", DatabaseTables.idxtb_main, "fileidentifier", false);
		addStringProp(DC_NS, "Identifier", DatabaseTables.idxtb_main, "fileidentifier", false);
		addStringProp("", "Identifier", DatabaseTables.idxtb_main, "fileidentifier", false);
		addStringProp(CSW_202_NS, "Identifier", DatabaseTables.idxtb_main, "fileidentifier", false);
		addDateProp(APISO_NS, "modified", DatabaseTables.idxtb_main, "modified");
		addDateProp(APISO_NS, "Modified", DatabaseTables.idxtb_main, "modified");
		addDateProp(DCT_NS, "Modified", DatabaseTables.idxtb_main, "modified");
		addDateProp("", "Modified", DatabaseTables.idxtb_main, "modified");
		addDateProp(CSW_202_NS, "Modified", DatabaseTables.idxtb_main, "modified");
		addStringProp(APISO_NS, "CRS", DatabaseTables.idxtb_crs, "crsid", false);
		addStringProp(DC_NS, "CRS", DatabaseTables.idxtb_crs, "crsid", false);
		addStringProp("", "CRS", DatabaseTables.idxtb_crs, "crsid", false);

		// ----------------------</common queryable
		// properties>------------------------------------
		// ----------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------
		// ----------------------<additional common queryable
		// properties>--------------------------
		addStringProp(APISO_NS, "Language", DatabaseTables.idxtb_main, "language", false);
		addStringProp(APISO_NS, "language", DatabaseTables.idxtb_main, "language", false);
		addDateProp(APISO_NS, "RevisionDate", DatabaseTables.idxtb_main, "revisiondate");
		addDateProp(APISO_NS, "CreationDate", DatabaseTables.idxtb_main, "creationdate");
		addStringProp(APISO_NS, "AlternateTitle", DatabaseTables.idxtb_main, "alternatetitles", true);
		addDateProp(APISO_NS, "PublicationDate", DatabaseTables.idxtb_main, "publicationdate");
		addStringProp(APISO_NS, "OrganisationName", DatabaseTables.idxtb_main, "organisationname", false);
		addBooleanProp(APISO_NS, "HasSecurityConstraints", DatabaseTables.idxtb_main, "hassecurityconstraint");
		addStringProp(APISO_NS, "ResourceIdentifier", DatabaseTables.idxtb_main, "resourceid", false);
		addStringProp(APISO_NS, "ParentIdentifier", DatabaseTables.idxtb_main, "parentid", false);
		addStringProp(APISO_NS, "KeywordType", DatabaseTables.idxtb_keyword, "keywordtype", false);
		addStringProp(APISO_NS, "TopicCategory", DatabaseTables.idxtb_main, "topicCategories", true);
		addStringProp(APISO_NS, "ResourceLanguage", DatabaseTables.idxtb_main, "resourcelanguage", false);
		addStringProp(APISO_NS, "GeographicDescriptionCode", DatabaseTables.idxtb_main, "geographicdescriptioncode",
				true);
		addIntProp(APISO_NS, "Denominator", DatabaseTables.idxtb_main, "denominator");
		addDecimalProp(APISO_NS, "DistanceValue", DatabaseTables.idxtb_main, "distancevalue");
		addStringProp(APISO_NS, "DistanceUOM", DatabaseTables.idxtb_main, "distanceuom", false);
		addDateProp(APISO_NS, "TempExtent_begin", DatabaseTables.idxtb_main, "tempextent_begin");
		addDateProp(APISO_NS, "TempExtent_end", DatabaseTables.idxtb_main, "tempextent_end");
		addStringProp(APISO_NS, "ServiceType", DatabaseTables.idxtb_main, "servicetype", false);
		addStringProp(APISO_NS, "ServiceTypeVersion", DatabaseTables.idxtb_main, "servicetypeversion", true);

		addStringProp(APISO_NS, "Operation", DatabaseTables.idxtb_main, "operations", true);
		addStringProp(APISO_NS, "OperatesOn", DatabaseTables.idxtb_operatesondata, "operateson", false);
		addStringProp(APISO_NS, "OperatesOnIdentifier", DatabaseTables.idxtb_operatesondata, "operatesonid", false);
		addStringProp(APISO_NS, "OperatesOnName", DatabaseTables.idxtb_operatesondata, "operatesonname", false);
		addStringProp(APISO_NS, "CouplingType", DatabaseTables.idxtb_main, "couplingtype", false);

		// ----------------------</additional common queryable
		// properties>-------------------------
		// ----------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------
		// ----------------------<additional queryable properties for
		// INSPIRE>--------------------------

		addBooleanProp(APISO_NS, "Degree", DatabaseTables.idxtb_main, "degree");
		addStringProp(APISO_NS, "AccessConstraints", DatabaseTables.idxtb_constraint, "accessconstraints", true);
		addStringProp(APISO_NS, "OtherConstraints", DatabaseTables.idxtb_constraint, "otherconstraints", true);
		addStringProp(APISO_NS, "Classification", DatabaseTables.idxtb_constraint, "classification", false);
		addStringProp(APISO_NS, "ConditionApplyingToAccessAndUse", DatabaseTables.idxtb_constraint, "conditionapptoacc",
				true);
		addStringProp(APISO_NS, "Lineage", DatabaseTables.idxtb_main, "lineage", true);
		addStringProp(APISO_NS, "SpecificationTitle", DatabaseTables.idxtb_main, "spectitle", true);
		addStringProp(APISO_NS, "SpecificationDateType", DatabaseTables.idxtb_main, "specdatetype", false);
		addDateProp(APISO_NS, "SpecificationDate", DatabaseTables.idxtb_main, "specdate");
		addStringProp(APISO_NS, "ResponsiblePartyRole", DatabaseTables.idxtb_main, "resppartyrole", false);

		// ----------------------</additional queryable properties for
		// INSPIRE>-------------------------
		// ----------------------------------------------------------------------------------------

	}

	/**
	 *
	 * The common column names that are used in the backend for each databasetable.
	 *
	 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
	 */
	public enum CommonColumnNames {

		/**
		 * the primarykey of a databasetable
		 */
		id,

		/**
		 * the identifier of the record
		 */
		fileidentifier,

		/**
		 * the resourceIdentifier of the record
		 */
		resourceid,

		/**
		 * the BLOB data for the reecord
		 */
		recordfull,

		/**
		 * the foreign key from sub idx to idx_main
		 */
		fk_main

	}

	/**
	 * The names of the databasetables that are used in the backend.
	 *
	 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
	 */
	public enum DatabaseTables {

		/**
		 * main databasetable, all of the other tables derive from this table
		 */
		idxtb_main, idxtb_constraint, idxtb_crs, idxtb_keyword, idxtb_operatesondata

	}

	// private final boolean useLegacyPredicates;
	//
	// private final Type connectionType;

	private final SQLDialect dialect;

	private final List<Queryable> queryables;

	public ISOPropertyNameMapper(SQLDialect dialect, List<Queryable> queryables) {
		this.dialect = dialect;
		this.queryables = queryables;
	}

	@Override
	public PropertyNameMapping getMapping(ValueReference propName, TableAliasManager aliasManager)
			throws FilterEvaluationException {

		PropertyNameMapping mapping = null;
		String tableAlias = aliasManager.getRootTableAlias();

		QName qName = propName.getAsQName();
		if (qName == null) {
			String msg = Messages.getMessage("WARN_PROPNAME_MAPPING", propName);
			LOG.debug(msg);
			throw new FilterEvaluationException(msg);
		}
		else {
			Triple<Pair<String, String>, Boolean, BaseType> tableColumn = propToTableAndCol.get(qName);
			if (tableColumn != null) {
				String mainTable = DatabaseTables.idxtb_main.name();
				String id = CommonColumnNames.id.name();
				String fk_main = CommonColumnNames.fk_main.name();
				List<Join> joins = new ArrayList<Join>();
				if (!tableColumn.first.first.equals(mainTable)) {
					String fromTable = mainTable;
					String fromTableAlias = aliasManager.getRootTableAlias();
					String fromColumn = id;
					String toTable = tableColumn.first.first;
					String toTableAlias = aliasManager.generateNew();
					String toColumn = fk_main;
					joins.add(new Join(fromTable, fromTableAlias, fromColumn, toTable, toTableAlias, toColumn));
					tableAlias = toTableAlias;
				}
				ParticleConverter<?> converter = null;
				if (tableColumn.third == null) {
					String srid = dialect.getUndefinedSrid();
					// TODO: srid
					if (dialect.getClass().getSimpleName().equals("OracleDialect")) {
						srid = "4326";
					}
					converter = dialect.getGeometryConverter(tableColumn.first.second, EPSG_4326, srid, true);
				}
				else {
					converter = new DefaultPrimitiveConverter(new PrimitiveType(tableColumn.third),
							tableColumn.first.second, tableColumn.second);
				}
				mapping = new PropertyNameMapping(converter, joins, tableColumn.first.second, tableAlias);
			}
			else {
				Queryable queryable = getQueryable(qName);
				if (queryable != null) {
					DefaultPrimitiveConverter converter = new DefaultPrimitiveConverter(new PrimitiveType(STRING),
							queryable.getColumn(), queryable.isMultiple());
					mapping = new PropertyNameMapping(converter, new ArrayList<Join>(), queryable.getColumn(),
							tableAlias);
				}
				else {
					String msg = Messages.getMessage("ERROR_PROPNAME_MAPPING", qName);
					LOG.debug(msg);
					throw new FilterEvaluationException(msg);
				}
			}
		}
		return mapping;
	}

	private Queryable getQueryable(QName qName) {
		for (Queryable q : queryables) {
			if (q.getNames().contains(qName)) {
				return q;
			}
		}
		return null;
	}

	private static void addBooleanProp(String propNs, String propName, DatabaseTables table, String column) {
		QName qName = new QName(propNs, propName);
		Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
				new Pair<String, String>(table.name(), column), false, BOOLEAN);
		propToTableAndCol.put(qName, mapping);

	}

	private static void addDateProp(String propNs, String propName, DatabaseTables table, String column) {
		QName qName = new QName(propNs, propName);
		Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
				new Pair<String, String>(table.name(), column), false, DATE_TIME);
		propToTableAndCol.put(qName, mapping);

	}

	private static void addStringProp(String propNs, String propName, DatabaseTables table, String column,
			boolean concatenated) {
		QName qName = new QName(propNs, propName);
		Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
				new Pair<String, String>(table.name(), column), concatenated, STRING);
		propToTableAndCol.put(qName, mapping);
	}

	private static void addGeometryProp(String propNs, String propName, DatabaseTables table, String column,
			boolean concatenated) {
		QName qName = new QName(propNs, propName);
		Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
				new Pair<String, String>(table.name(), column), concatenated, null);
		propToTableAndCol.put(qName, mapping);
	}

	private static void addIntProp(String propNs, String propName, DatabaseTables table, String column) {
		QName qName = new QName(propNs, propName);
		Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
				new Pair<String, String>(table.name(), column), false, INTEGER);
		propToTableAndCol.put(qName, mapping);
	}

	private static void addDecimalProp(String propNs, String propName, DatabaseTables table, String column) {
		QName qName = new QName(propNs, propName);
		Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
				new Pair<String, String>(table.name(), column), false, DECIMAL);
		propToTableAndCol.put(qName, mapping);
	}

	/**
	 * @return a map&lang;QName, PropertyNameMapping&rang; can not be <Code>null</Code>
	 */
	public Map<QName, Triple<Pair<String, String>, Boolean, BaseType>> getPropToTableAndCol() {
		return propToTableAndCol;
	}

	@Override
	public PropertyNameMapping getSpatialMapping(ValueReference propName, TableAliasManager aliasManager)
			throws FilterEvaluationException, UnmappableException {
		return getMapping(propName, aliasManager);
	}

}
