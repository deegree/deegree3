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

package org.deegree.metadata.persistence.ebrim.eo;

import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;
import static org.deegree.db.ConnectionProviderUtils.executeQuery;
import static org.deegree.metadata.ebrim.RIMType.AdhocQuery;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;
import org.deegree.commons.jdbc.InsertRow;
import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.datetime.ISO8601Converter;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.MetadataRecordFactory;
import org.deegree.metadata.ebrim.AdhocQuery;
import org.deegree.metadata.ebrim.AliasedRIMType;
import org.deegree.metadata.ebrim.RegistryObject;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.XMLMetadataResultSet;
import org.deegree.metadata.persistence.ebrim.eo.mapping.EOPropertyNameMapper;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.Table;
import org.deegree.metadata.persistence.transaction.DeleteOperation;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.Join;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.sqldialect.postgis.PostGISWhereBuilder;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * {@link MetadataStore} implementation for accessing {@link EbrimEOMDRecord}s stored in
 * spatial SQL databases (currently only PostgreSQL / PostGIS is supported).
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class EbrimEOMDStore implements MetadataStore<RegistryObject> {

	/**
	 * Used to limit the fetch size for SELECT statements that potentially return a lot of
	 * rows.
	 */
	public static final int DEFAULT_FETCH_SIZE = 100;

	private static final Logger LOG = getLogger(EbrimEOMDStore.class);

	private final String connId;

	private boolean useLegacyPredicates;

	private Map<String, AdhocQuery> idToQuery = new HashMap<String, AdhocQuery>();

	private final long queryTimeout;

	private RegistryPackage profile;

	private Date lastModified;

	private static final NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();

	private ResourceMetadata<MetadataStore<? extends MetadataRecord>> metadata;

	private Workspace workspace;

	static {
		nsContext.addNamespace("rim", RegistryObject.RIM_NS);
	}

	/**
	 * Creates a new {@link EbrimEOMDStore} instance.
	 * @param connId id of the JDBC connection to use, must not be <code>null</code>
	 * @param queriesDir directory containing individual AdhocQuery files (*.xml), can be
	 * <code>null</code>
	 * @param profile RegistryPackage containing the profile informations, can be
	 * <code>null</code>
	 * @param queryTimeout number of milliseconds to allow for queries, or <code>0</code>
	 * (unlimited)
	 * @throws ResourceInitException
	 */
	public EbrimEOMDStore(String connId, File queriesDir, RegistryPackage profile, Date lastModified, long queryTimeout,
			ResourceMetadata<MetadataStore<? extends MetadataRecord>> metadata, Workspace workspace)
			throws ResourceInitException {
		this.connId = connId;
		this.profile = profile;
		this.lastModified = lastModified;
		this.queryTimeout = queryTimeout;
		this.metadata = metadata;
		this.workspace = workspace;
		if (queriesDir != null) {
			File[] listFiles = queriesDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File arg0, String arg1) {
					return arg1.endsWith(".xml");
				}
			});

			if (listFiles != null) {
				for (File file : listFiles) {
					FileInputStream is = null;
					try {
						is = FileUtils.openInputStream(file);
						XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
						AdhocQuery query = new AdhocQuery(xmlStream);
						idToQuery.put(query.getId(), query);
						LOG.info("Found adhocQuery " + file + " with id " + query.getId());
					}
					catch (Throwable t) {
						LOG.error(t.getMessage(), t);
						throw new ResourceInitException(t.getMessage());
					}
					finally {
						try {
							if (is != null) {
								is.close();
							}
						}
						catch (IOException e) {
							LOG.info("Could not close InputStream");
						}
					}
				}
			}
		}
	}

	@Override
	public void init() {
		Connection conn = null;
		try {
			conn = getConnection(true);
			useLegacyPredicates = JDBCUtils.useLegayPostGISPredicates(conn, LOG);
		}
		catch (MetadataStoreException e) {
			throw new ResourceInitException(e.getMessage(), e);
		}
		finally {
			JDBCUtils.close(conn);
		}
		if (profile != null) {
			PreparedStatement stmt = null;
			ResultSet result = null;
			try {
				conn = getConnection(false);
				String sql = "Select value FROM management WHERE key = 'LAST_INSERTED'";
				stmt = conn.prepareStatement(sql);
				result = stmt.executeQuery();
				Date lastInserted = null;
				if (result.next()) {
					try {
						String date = result.getString(1);
						if (date != null) {
							lastInserted = parseDateTime(date).getDate();
						}
					}
					catch (IllegalArgumentException e) {
						LOG.info("Could not parse lastInserted Date. Handle as never insertd!");
					}
				}
				sql = "Select value FROM management WHERE key = 'REGISTRYPACKAGE_ID'";
				stmt = conn.prepareStatement(sql);
				result = stmt.executeQuery();
				String regPackId = null;
				if (result.next()) {
					regPackId = result.getString(1);
				}
				LOG.debug("Profile in database: " + regPackId + " from " + lastInserted);
				String insertedId = updateProfile(regPackId, lastInserted, profile, lastModified);
				if (insertedId != null) {
					sql = "DELETE FROM management where key = 'REGISTRYPACKAGE_ID' or key = 'LAST_INSERTED'";
					stmt = conn.prepareStatement(sql);
					stmt.execute();

					InsertRow ir = new InsertRow(new TableName("management"), null);
					ir.addPreparedArgument(new SQLIdentifier("key"), "REGISTRYPACKAGE_ID");
					ir.addPreparedArgument(new SQLIdentifier("value"), insertedId);
					ir.performInsert(conn);

					ir = new InsertRow(new TableName("management"), null);
					ir.addPreparedArgument(new SQLIdentifier("key"), "LAST_INSERTED");
					ir.addPreparedArgument(new SQLIdentifier("value"), ISO8601Converter.formatDateTime(new Date()));
					ir.performInsert(conn);

					conn.commit();
				}
				else {
					LOG.info("Did not insert profile!");
				}
			}
			catch (MetadataStoreException e) {
				String msg = "Could not insert profile: " + e.getMessage();
				LOG.debug(msg);
				throw new ResourceInitException(msg, e);
			}
			catch (SQLException e) {
				String msg = "Could not insert profile: " + e.getMessage();
				LOG.debug(msg);
				throw new ResourceInitException(msg, e);
			}
			finally {
				JDBCUtils.close(result, stmt, conn, LOG);
			}
		}
	}

	private String updateProfile(String regPackId, Date lastInserted, RegistryPackage profile, Date lastModified)
			throws ResourceInitException {
		MetadataStoreTransaction trans;
		try {
			trans = acquireTransaction();
		}
		catch (MetadataStoreException e) {
			String msg = "Could not aquireTranacation to update the profile: " + e.getMessage();
			LOG.debug(msg);
			throw new ResourceInitException(msg, e);
		}

		try {
			if (profile != null && lastModified != null
					&& ((lastInserted != null && lastInserted.before(lastModified)) || lastInserted == null)) {
				if (regPackId != null) {
					LOG.info("profile has changed: Delete old profile with id " + regPackId + ", last inserted: "
							+ lastInserted);
					ValueReference propertyName = new ValueReference("rim:RegistryPackage/@id", nsContext);
					Literal<PrimitiveValue> lit = new Literal<PrimitiveValue>(
							new PrimitiveValue(regPackId, new PrimitiveType(BaseType.STRING)), null);
					Filter constraint = new OperatorFilter(new PropertyIsEqualTo(propertyName, lit, true, null));
					trans.performDelete(new DeleteOperation(null, null, constraint));
				}
				List<String> performInsert = trans
					.performInsert(new InsertOperation(Collections.singletonList(profile), null, null));
				trans.commit();
				if (!performInsert.isEmpty()) {
					LOG.info("Inserted profile with id " + performInsert.get(0));
					return performInsert.get(0);
				}
			}
			return null;
		}
		catch (MetadataStoreException e) {
			String msg = "Could not update the profile: " + e.getMessage();
			LOG.debug(msg);
			try {
				trans.rollback();
			}
			catch (MetadataStoreException e1) {
				LOG.error("Rollback failed: ", e1);
			}
			throw new ResourceInitException(msg, e);
		}
		catch (MetadataInspectorException e) {
			String msg = "Could not update the profile: " + e.getMessage();
			LOG.debug(msg);
			try {
				trans.rollback();
			}
			catch (MetadataStoreException e1) {
				LOG.error("Rollback failed: ", e1);
			}
			throw new ResourceInitException(msg, e);
		}
	}

	@Override
	public void destroy() {
		// nothing to do yet
	}

	@Override
	public MetadataResultSet<RegistryObject> getRecords(MetadataQuery query) throws MetadataStoreException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, connId);
		Connection conn = getConnection(true);
		try {
			EOPropertyNameMapper propMapper = new EOPropertyNameMapper(query.getQueryTypeNames(), useLegacyPredicates);
			if (query.getFilter() instanceof IdFilter) {
				throw new MetadataStoreException("ID filters are currently not supported.");
			}

			AbstractWhereBuilder wb = new PostGISWhereBuilder(null, propMapper, (OperatorFilter) query.getFilter(),
					query.getSorting(), null, false, useLegacyPredicates);
			AliasedRIMType returnType = propMapper.getReturnType(query.getReturnTypeNames());
			StringBuilder idSelect = new StringBuilder("SELECT DISTINCT(");
			idSelect.append(propMapper.getTableAlias(returnType));
			idSelect.append(".internalId) FROM ");
			idSelect.append(propMapper.getTable(returnType));
			idSelect.append(' ');
			idSelect.append(propMapper.getTableAlias(returnType));
			boolean first = true;
			for (AliasedRIMType queryType : propMapper.getQueryTypes()) {
				if (queryType != returnType) {
					if (first) {
						idSelect.append(" LEFT OUTER JOIN ");
					}
					else {
						idSelect.append(" FULL OUTER JOIN ");
					}
					idSelect.append(propMapper.getTable(queryType).name());
					idSelect.append(' ');
					idSelect.append(propMapper.getTableAlias(queryType));
					idSelect.append(" ON TRUE");
					first = false;
				}
			}
			// cope with rim:RegistryPackage -> rim:RegistryObjectList/* join
			for (Join additionalJoin : propMapper.getAdditionalJoins()) {
				if (first) {
					idSelect.append(" LEFT OUTER JOIN ");
				}
				else {
					idSelect.append(" FULL OUTER JOIN ");
				}
				idSelect.append(additionalJoin.getToTable());
				idSelect.append(' ');
				idSelect.append(additionalJoin.getToTableAlias());
				idSelect.append(" ON ");
				idSelect.append(additionalJoin.getSQLJoinCondition());
				first = false;
			}

			if (wb.getWhere() != null) {
				idSelect.append(" WHERE ").append(wb.getWhere().getSQL());
			}
			if (wb.getOrderBy() != null) {
				idSelect.append(" ORDER BY ");
				idSelect.append(wb.getOrderBy().getSQL());
			}
			if (query != null && query.getStartPosition() != 1) {
				idSelect.append(" OFFSET ").append(Integer.toString(query.getStartPosition() - 1));
			}
			if (query != null) {
				idSelect.append(" LIMIT ").append(query.getMaxRecords());
			}

			StringBuilder blobSelect = new StringBuilder("SELECT data FROM ");
			blobSelect.append(propMapper.getTable(returnType));
			blobSelect.append(" WHERE internalId IN (");
			blobSelect.append(idSelect);
			blobSelect.append(")");

			stmt = conn.prepareStatement(blobSelect.toString());
			stmt.setFetchSize(DEFAULT_FETCH_SIZE);

			int i = 1;
			if (wb.getWhere() != null) {
				for (SQLArgument argument : wb.getWhere().getArguments()) {
					argument.setArgument(stmt, i++);
				}
			}

			if (wb.getOrderBy() != null) {
				for (SQLArgument argument : wb.getOrderBy().getArguments()) {
					argument.setArgument(stmt, i++);
				}
			}

			LOG.debug("Execute: " + stmt.toString());
			rs = executeQuery(stmt, prov, queryTimeout);
			return new EbrimEOMDResultSet(rs, conn, stmt);
		}
		catch (Throwable t) {

			JDBCUtils.close(rs, stmt, conn, LOG);
			LOG.debug(t.getMessage(), t);
			throw new MetadataStoreException(t.getMessage(), t);
		}
	}

	@Override
	public int getRecordCount(MetadataQuery query) throws MetadataStoreException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, connId);
		Connection conn = getConnection(true);
		try {
			EOPropertyNameMapper propMapper = new EOPropertyNameMapper(query.getQueryTypeNames(), useLegacyPredicates);
			if (query.getFilter() instanceof IdFilter) {
				throw new MetadataStoreException("ID filters are currently not supported.");
			}

			AbstractWhereBuilder wb = new PostGISWhereBuilder(null, propMapper, (OperatorFilter) query.getFilter(),
					query.getSorting(), null, false, useLegacyPredicates);
			AliasedRIMType returnType = propMapper.getReturnType(query.getReturnTypeNames());
			StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT(");
			sql.append(propMapper.getTableAlias(returnType));
			sql.append(".internalId)) FROM ");
			sql.append(propMapper.getTable(returnType));
			sql.append(' ');
			sql.append(propMapper.getTableAlias(returnType));
			boolean first = true;
			for (AliasedRIMType queryType : propMapper.getQueryTypes()) {
				if (queryType != returnType) {
					if (first) {
						sql.append(" LEFT OUTER JOIN ");
					}
					else {
						sql.append(" FULL OUTER JOIN ");
					}
					sql.append(propMapper.getTable(queryType).name());
					sql.append(' ');
					sql.append(propMapper.getTableAlias(queryType));
					sql.append(" ON TRUE");
					first = false;
				}
			}
			// cope with rim:RegistryPackage -> rim:RegistryObjectList/* join
			for (Join additionalJoin : propMapper.getAdditionalJoins()) {
				if (first) {
					sql.append(" LEFT OUTER JOIN ");
				}
				else {
					sql.append(" FULL OUTER JOIN ");
				}
				sql.append(additionalJoin.getToTable());
				sql.append(' ');
				sql.append(additionalJoin.getToTableAlias());
				sql.append(" ON ");
				sql.append(additionalJoin.getSQLJoinCondition());
				first = false;
			}
			if (wb.getWhere() != null) {
				sql.append(" WHERE ").append(wb.getWhere().getSQL());
			}

			stmt = conn.prepareStatement(sql.toString());

			int i = 1;
			if (wb.getWhere() != null) {
				for (SQLArgument argument : wb.getWhere().getArguments()) {
					argument.setArgument(stmt, i++);
				}
			}

			LOG.debug("Execute: " + stmt.toString());
			rs = executeQuery(stmt, prov, queryTimeout);
			rs.next();
			return rs.getInt(1);
		}
		catch (Throwable t) {
			JDBCUtils.close(rs, stmt, conn, LOG);
			LOG.debug(t.getMessage(), t);
			throw new MetadataStoreException(t.getMessage(), t);
		}
		finally {
			JDBCUtils.close(rs, stmt, conn, LOG);
		}
	}

	@Override
	public MetadataResultSet<RegistryObject> getRecordById(List<String> idList, QName[] recordTypeNames)
			throws MetadataStoreException {

		Table table = Table.idxtb_registrypackage;
		if (recordTypeNames != null && recordTypeNames.length > 0) {
			if (recordTypeNames.length > 1) {
				String msg = "Record by id queries with multiple specified type names are not supported.";
				throw new MetadataStoreException(msg);
			}

			try {
				List<AliasedRIMType> aliasedTypes = AliasedRIMType.valueOf(recordTypeNames[0]);
				if (aliasedTypes.get(0).getType() == AdhocQuery) {
					return getAdhocQueries(idList);
				}
				table = SlotMapper.getTable(aliasedTypes.get(0).getType());
			}
			catch (Throwable t) {
				String msg = "Specified type name '" + recordTypeNames[0]
						+ "' is not a known ebRIM 3.0 registry object type.";
				throw new MetadataStoreException(msg);
			}
			if (table == null) {
				String msg = "Queries on registry object type '" + recordTypeNames[0] + "' are not supported.";
				throw new MetadataStoreException(msg);
			}
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, connId);
		Connection conn = getConnection(true);

		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT data");
			sql.append(" FROM ");
			sql.append(table.name());
			sql.append(" WHERE id IN (");
			sql.append("?");
			for (int i = 1; i < idList.size(); i++) {
				sql.append(",?");
			}
			sql.append(")");

			stmt = conn.prepareStatement(sql.toString());
			stmt.setFetchSize(DEFAULT_FETCH_SIZE);

			int i = 1;
			for (String identifier : idList) {
				stmt.setString(i, identifier);
				i++;
			}

			LOG.debug("Execute: " + stmt.toString());
			rs = executeQuery(stmt, prov, queryTimeout);
			return new EbrimEOMDResultSet(rs, conn, stmt);
		}
		catch (Throwable t) {
			JDBCUtils.close(rs, stmt, conn, LOG);
			LOG.debug(t.getMessage(), t);
			throw new MetadataStoreException(t.getMessage(), t);
		}
	}

	// TODO integrate storage of AdhocQueries with other RegistryObjects
	private MetadataResultSet<RegistryObject> getAdhocQueries(final List<String> idList) {
		List<AdhocQuery> records = new ArrayList<AdhocQuery>();
		for (String id : idList) {
			AdhocQuery query = idToQuery.get(id);
			if (query != null) {
				records.add(query);
			}
		}
		final Iterator<AdhocQuery> iter = records.iterator();
		return new MetadataResultSet<RegistryObject>() {

			private AdhocQuery current = null;

			@Override
			public boolean next() throws MetadataStoreException {
				try {
					current = iter.next();
				}
				catch (NoSuchElementException e) {
					return false;
				}
				return true;
			}

			@Override
			public RegistryObject getRecord() throws MetadataStoreException {
				return current;
			}

			@Override
			public void close() throws MetadataStoreException {
				// nothing to do
			}

			@Override
			public int getRemaining() throws MetadataStoreException {
				return 0;
			}

			@Override
			public void skip(int rows) throws MetadataStoreException {
				// TODO Auto-generated method stub
			}
		};
	}

	@Override
	public MetadataStoreTransaction acquireTransaction() throws MetadataStoreException {
		return new EbrimEOMDStoreTransaction(getConnection(false), useLegacyPredicates);
	}

	/**
	 * NOTE: Although autoCommit=true disables cursor-based access (streaming), it is
	 * currently set for very good reasons: It was found that it can ruin the performance
	 * of INNER SELECTs. Ask Andreas P. / Markus before changing this!
	 * @param autoCommit
	 * @return
	 * @throws MetadataStoreException
	 */
	private Connection getConnection(boolean autoCommit) throws MetadataStoreException {
		ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, connId);
		try {
			Connection conn = prov.getConnection();
			conn.setAutoCommit(autoCommit);
			return conn;
		}
		catch (SQLException e) {
			throw new MetadataStoreException("Could not get Connection with ID " + connId + " " + e.getMessage());
		}
	}

	private class EbrimEOMDResultSet extends XMLMetadataResultSet<RegistryObject> {

		EbrimEOMDResultSet(ResultSet rs, Connection conn, PreparedStatement stmt) {
			super(rs, conn, stmt);
		}

		@Override
		protected RegistryObject getRecord(XMLStreamReader xmlReader) {
			XMLAdapter adapter = new XMLAdapter(xmlReader);
			return (RegistryObject) MetadataRecordFactory.create(adapter.getRootElement());
		}

	}

	@Override
	public String getConnId() {
		return connId;
	}

	@Override
	public String getType() {
		return "ebrimeo";
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

}
