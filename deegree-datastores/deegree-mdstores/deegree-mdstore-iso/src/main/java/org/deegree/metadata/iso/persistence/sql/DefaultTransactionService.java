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
package org.deegree.metadata.iso.persistence.sql;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.jdbc.InsertRow;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.jdbc.TransactionRow;
import org.deegree.commons.jdbc.UpdateRow;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.CRSUtils;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometries;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.iso.parsing.QueryableProperties;
import org.deegree.metadata.iso.persistence.queryable.Queryable;
import org.deegree.metadata.iso.types.BoundingBox;
import org.deegree.metadata.iso.types.CRS;
import org.deegree.metadata.iso.types.Constraint;
import org.deegree.metadata.iso.types.Format;
import org.deegree.metadata.iso.types.Keyword;
import org.deegree.metadata.iso.types.OperatesOnData;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.slf4j.Logger;

/**
 * Here are all the queryable properties encapsulated which have to put into the backend.
 * Here is the functionality of the INSERT and UPDATE action.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class DefaultTransactionService extends AbstractSqlHelper implements TransactionService {

	private static final Logger LOG = getLogger(DefaultTransactionService.class);

	private AnyText anyTextConfig;

	public DefaultTransactionService(SQLDialect dialect, List<Queryable> queryables, AnyText anyTextConfig) {
		super(dialect, queryables);
		this.anyTextConfig = anyTextConfig;
	}

	@Override
	public synchronized int executeInsert(Connection conn, ISORecord rec)
			throws MetadataStoreException, XMLStreamException {
		int internalId = 0;
		InsertRow ir = new InsertRow(new TableName(mainTable), null);
		try {
			internalId = getLastDatasetId(conn, mainTable);
			internalId++;

			ir.addPreparedArgument(idColumn, internalId);
			ir.addPreparedArgument(recordColumn, rec.getAsByteArray());
			ir.addPreparedArgument("fileidentifier", rec.getIdentifier());
			ir.addPreparedArgument("version", null);
			ir.addPreparedArgument("status", null);

			appendValues(rec, ir);

			LOG.debug(ir.getSql());
			ir.performInsert(conn);

			QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
			insertNewValues(conn, internalId, qp);

		}
		catch (SQLException e) {
			String msg = Messages.getMessage("ERROR_SQL", ir.getSql(), e.getMessage());
			LOG.debug(msg);
			throw new MetadataStoreException(msg);
		}
		return internalId;
	}

	@Override
	public int executeDelete(Connection connection, AbstractWhereBuilder builder) throws MetadataStoreException {
		LOG.debug(Messages.getMessage("INFO_EXEC", "delete-statement"));
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Integer> deletableDatasets;
		int deleted = 0;
		try {
			StringBuilder header = getPreparedStatementDatasetIDs(builder);
			getPSBody(builder, header);
			stmt = connection.prepareStatement(header.toString());
			int i = 1;
			if (builder.getWhere() != null) {
				for (SQLArgument o : builder.getWhere().getArguments()) {
					o.setArgument(stmt, i++);
				}
			}
			if (builder.getOrderBy() != null) {
				for (SQLArgument o : builder.getOrderBy().getArguments()) {
					o.setArgument(stmt, i++);
				}
			}
			LOG.debug(Messages.getMessage("INFO_TA_DELETE_FIND", stmt.toString()));

			rs = stmt.executeQuery();

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("DELETE FROM ");
			stringBuilder.append(mainTable);
			stringBuilder.append(" WHERE ").append(idColumn);
			stringBuilder.append(" = ?");

			deletableDatasets = new ArrayList<Integer>();
			if (rs != null) {
				while (rs.next()) {
					deletableDatasets.add(rs.getInt(1));
				}
				rs.close();
				close(stmt);
				stmt = connection.prepareStatement(stringBuilder.toString());
				for (int d : deletableDatasets) {
					stmt.setInt(1, d);
					LOG.debug(Messages.getMessage("INFO_TA_DELETE_DEL", stmt.toString()));
					deleted = deleted + stmt.executeUpdate();
				}
			}
		}
		catch (SQLException e) {
			String msg = Messages.getMessage("ERROR_SQL", stmt.toString(), e.getMessage());
			LOG.debug(msg);
			throw new MetadataStoreException(msg);
		}
		finally {
			close(rs, stmt, null, LOG);
		}
		return deleted;
	}

	@Override
	public int executeUpdate(Connection conn, ISORecord rec, String fileIdentifier) throws MetadataStoreException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		StringWriter s = new StringWriter(150);
		int requestedId = -1;

		String idToUpdate = (fileIdentifier == null ? rec.getIdentifier() : fileIdentifier);
		try {
			s.append("SELECT ").append(idColumn);
			s.append(" FROM ").append(mainTable);
			s.append(" WHERE ").append(fileIdColumn).append(" = ?");
			LOG.debug(s.toString());

			stmt = conn.prepareStatement(s.toString());
			stmt.setObject(1, idToUpdate);
			rs = stmt.executeQuery();

			while (rs.next()) {
				requestedId = rs.getInt(1);
				LOG.debug("resultSet: " + rs.getInt(1));
			}

			if (requestedId > -1) {
				UpdateRow ur = new UpdateRow(new TableName(mainTable));
				ur.addPreparedArgument("version", null);
				ur.addPreparedArgument("status", null);
				ur.addPreparedArgument(recordColumn, rec.getAsByteArray());

				appendValues(rec, ur);

				ur.setWhereClause(idColumn + " = " + Integer.toString(requestedId));
				LOG.debug(stmt.toString());
				ur.performUpdate(conn);

				QueryableProperties qp = rec.getParsedElement().getQueryableProperties();

				deleteOldValues(conn, requestedId);
				insertNewValues(conn, requestedId, qp);
			}
		}
		catch (SQLException e) {
			String msg = Messages.getMessage("ERROR_SQL", s.toString(), e.getMessage());
			LOG.debug(msg);
			throw new MetadataStoreException(msg);
		}
		catch (FactoryConfigurationError e) {
			LOG.debug("error: " + e.getMessage(), e);
			throw new MetadataStoreException(e.getMessage());
		}
		finally {
			JDBCUtils.close(rs, stmt, null, LOG);
		}
		return requestedId;
	}

	private void insertNewValues(Connection conn, int requestedId, QueryableProperties qp)
			throws MetadataStoreException {
		LOG.debug("Insert values in referenced tables for dataset with id {}", requestedId);
		insertInCRSTable(conn, requestedId, qp);
		insertInKeywordTable(conn, requestedId, qp);
		insertInOperatesOnTable(conn, requestedId, qp);
		insertInConstraintTable(conn, requestedId, qp);
	}

	private void deleteOldValues(Connection conn, int requestedId) throws MetadataStoreException {
		LOG.debug("Delete existing values in referenced tables for dataset with id {}", requestedId);
		deleteExistingRows(conn, requestedId, crsTable);
		deleteExistingRows(conn, requestedId, keywordTable);
		deleteExistingRows(conn, requestedId, opOnTable);
		deleteExistingRows(conn, requestedId, constraintTable);
	}

	private void appendValues(ISORecord rec, TransactionRow tr) throws SQLException {
		tr.addPreparedArgument("abstract", concatenate(Arrays.asList(rec.getAbstract())));
		tr.addPreparedArgument("anytext", AnyTextHelper.getAnyText(rec, anyTextConfig));
		tr.addPreparedArgument("language", rec.getLanguage());
		Timestamp modified = null;
		if (rec.getModified() != null) {
			modified = new Timestamp(rec.getModified().getTimeInMilliseconds());
		}
		tr.addPreparedArgument("modified", modified);
		tr.addPreparedArgument("parentid", rec.getParentIdentifier());
		tr.addPreparedArgument("type", rec.getType());
		tr.addPreparedArgument("title", concatenate(Arrays.asList(rec.getTitle())));
		tr.addPreparedArgument("hassecurityconstraints", rec.isHasSecurityConstraints());

		QueryableProperties qp = rec.getParsedElement().getQueryableProperties();
		tr.addPreparedArgument("topiccategories", concatenate(qp.getTopicCategory()));
		tr.addPreparedArgument("alternateTitles", concatenate(qp.getAlternateTitle()));
		Timestamp revDate = null;
		if (qp.getRevisionDate() != null) {
			revDate = new Timestamp(qp.getRevisionDate().getTimeInMilliseconds());
		}
		tr.addPreparedArgument("revisiondate", revDate);
		Timestamp createDate = null;
		if (qp.getCreationDate() != null) {
			createDate = new Timestamp(qp.getCreationDate().getTimeInMilliseconds());
		}
		tr.addPreparedArgument("creationdate", createDate);
		Timestamp pubDate = null;
		if (qp.getPublicationDate() != null) {
			pubDate = new Timestamp(qp.getPublicationDate().getTimeInMilliseconds());
		}
		tr.addPreparedArgument("publicationdate", pubDate);
		tr.addPreparedArgument("organisationname", qp.getOrganisationName());
		tr.addPreparedArgument("resourceid", qp.getResourceIdentifier());
		tr.addPreparedArgument("resourcelanguage", qp.getResourceLanguage());
		tr.addPreparedArgument("geographicdescriptioncode", concatenate(qp.getGeographicDescriptionCode_service()));
		tr.addPreparedArgument("denominator", qp.getDenominator());
		tr.addPreparedArgument("distancevalue", qp.getDistanceValue());
		tr.addPreparedArgument("distanceuom", qp.getDistanceUOM());
		Timestamp begTmpExten = null;
		if (qp.getTemporalExtentBegin() != null) {
			begTmpExten = new Timestamp(qp.getTemporalExtentBegin().getTimeInMilliseconds());
		}
		tr.addPreparedArgument("tempextent_begin", begTmpExten);
		Timestamp endTmpExten = null;
		if (qp.getTemporalExtentEnd() != null) {
			endTmpExten = new Timestamp(qp.getTemporalExtentEnd().getTimeInMilliseconds());
		}
		tr.addPreparedArgument("tempextent_end", endTmpExten);
		tr.addPreparedArgument("servicetype", qp.getServiceType());
		tr.addPreparedArgument("servicetypeversion", concatenate(qp.getServiceTypeVersion()));
		tr.addPreparedArgument("couplingtype", qp.getCouplingType());
		tr.addPreparedArgument("formats", getFormats(qp.getFormat()));
		tr.addPreparedArgument("operations", concatenate(qp.getOperation()));
		tr.addPreparedArgument("degree", qp.isDegree());
		tr.addPreparedArgument("lineage", concatenate(qp.getLineages()));
		tr.addPreparedArgument("resppartyrole", qp.getRespPartyRole());
		tr.addPreparedArgument("spectitle", concatenate(qp.getSpecificationTitle()));
		Timestamp specDate = null;
		if (qp.getSpecificationDate() != null) {
			specDate = new Timestamp(qp.getSpecificationDate().getTimeInMilliseconds());
		}
		tr.addPreparedArgument("specdate", specDate);
		tr.addPreparedArgument("specdatetype", qp.getSpecificationDateType());

		Envelope env = calculateMainBBox(qp.getBoundingBox());
		Geometry geom = null;
		if (env != null) {
			geom = Geometries.getAsGeometry(env);
		}

		String bboxColumn = "bbox";
		String srid = null;
		// TODO: srid
		if (dialect.getClass().getSimpleName().equals("OracleDialect")) {
			srid = "4326";
		}
		GeometryParticleConverter converter = dialect.getGeometryConverter(bboxColumn, null, srid, true);

		tr.addPreparedArgument(bboxColumn, geom, converter);

		for (Queryable queryable : queryables) {
			String value;
			if (queryable.isMultiple()) {
				value = concatenate(queryable.getConvertedValues(rec));
			}
			else {
				value = queryable.getConvertedValue(rec);
			}
			tr.addPreparedArgument(queryable.getColumn(), value);
		}
	}

	private String getFormats(List<Format> list) {
		StringBuffer sb = new StringBuffer();
		if (list != null && list.size() > 0) {
			sb.append('\'');
			for (Format f : list) {
				sb.append('|').append(f.getName());
			}
			if (!list.isEmpty()) {
				sb.append("|");
			}
			sb.append("',");
		}
		return (sb.toString() != null && sb.length() > 0) ? sb.toString() : null;
	}

	private Envelope calculateMainBBox(List<BoundingBox> bbox) {
		if (bbox == null || bbox.isEmpty())
			return null;
		double west = bbox.get(0).getWestBoundLongitude();
		double east = bbox.get(0).getEastBoundLongitude();
		double south = bbox.get(0).getSouthBoundLatitude();
		double north = bbox.get(0).getNorthBoundLatitude();
		for (BoundingBox b : bbox) {
			west = Math.min(west, b.getWestBoundLongitude());
			east = Math.max(east, b.getEastBoundLongitude());
			south = Math.min(south, b.getSouthBoundLatitude());
			north = Math.max(north, b.getNorthBoundLatitude());
		}
		GeometryFactory gf = new GeometryFactory();
		return gf.createEnvelope(west, south, east, north, CRSUtils.EPSG_4326);
	}

	private void insertInConstraintTable(Connection conn, int operatesOnId, QueryableProperties qp)
			throws MetadataStoreException {
		List<Constraint> constraintss = qp.getConstraints();
		if (constraintss != null && constraintss.size() > 0) {
			final StringWriter sw = new StringWriter(300);
			sw.append("INSERT INTO ").append(constraintTable);
			sw.append('(')
				.append(idColumn)
				.append(',')
				.append(fk_main)
				.append(",conditionapptoacc,accessconstraints,otherconstraints,classification)");
			sw.append("VALUES( ?,?,?,?,?,? )");
			PreparedStatement stmt = null;

			try {
				stmt = conn.prepareStatement(sw.toString());
				stmt.setInt(2, operatesOnId);
				for (Constraint constraint : constraintss) {
					int localId = getNewIdentifier(conn, constraintTable);
					stmt.setInt(1, localId);
					stmt.setString(3, concatenate(constraint.getLimitations()));
					stmt.setString(4, concatenate(constraint.getAccessConstraints()));
					stmt.setString(5, concatenate(constraint.getOtherConstraints()));
					stmt.setString(6, constraint.getClassification());
					stmt.executeUpdate();
				}
			}
			catch (SQLException e) {
				String msg = Messages.getMessage("ERROR_SQL", sw, e.getMessage());
				LOG.debug(msg);
				throw new MetadataStoreException(msg);
			}
			finally {
				close(null, stmt, null, LOG);
			}
		}
	}

	private void insertInCRSTable(Connection conn, int operatesOnId, QueryableProperties qp)
			throws MetadataStoreException {
		List<CRS> crss = qp.getCrs();
		if (crss != null && crss.size() > 0) {
			for (CRS crs : crss) {
				InsertRow ir = new InsertRow(new TableName(crsTable), null);
				try {
					int localId = getNewIdentifier(conn, crsTable);
					ir.addPreparedArgument(idColumn, localId);
					ir.addPreparedArgument(fk_main, operatesOnId);
					ir.addPreparedArgument("authority", (crs.getAuthority() != null && crs.getAuthority().length() > 0)
							? crs.getAuthority() : null);
					ir.addPreparedArgument("crsid",
							(crs.getCrsId() != null && crs.getCrsId().length() > 0) ? crs.getCrsId() : null);
					ir.addPreparedArgument("version",
							(crs.getVersion() != null && crs.getVersion().length() > 0) ? crs.getVersion() : null);
					LOG.debug(ir.getSql());
					ir.performInsert(conn);
				}
				catch (SQLException e) {
					String msg = Messages.getMessage("ERROR_SQL", ir.getSql(), e.getMessage());
					LOG.debug(msg);
					throw new MetadataStoreException(msg);
				}
			}
		}
	}

	private void insertInKeywordTable(Connection conn, int operatesOnId, QueryableProperties qp)
			throws MetadataStoreException {
		List<Keyword> keywords = qp.getKeywords();
		if (keywords != null && keywords.size() > 0) {
			for (Keyword keyword : keywords) {
				InsertRow ir = new InsertRow(new TableName(keywordTable), null);
				try {
					int localId = getNewIdentifier(conn, keywordTable);
					ir.addPreparedArgument(idColumn, localId);
					ir.addPreparedArgument(fk_main, operatesOnId);
					ir.addPreparedArgument("keywordtype", keyword.getKeywordType());
					ir.addPreparedArgument("keywords", concatenate(keyword.getKeywords()));
					LOG.debug(ir.getSql());
					ir.performInsert(conn);
				}
				catch (SQLException e) {
					String msg = Messages.getMessage("ERROR_SQL", ir.getSql(), e.getMessage());
					LOG.debug(msg);
					throw new MetadataStoreException(msg);
				}
			}
		}
	}

	private void insertInOperatesOnTable(Connection conn, int operatesOnId, QueryableProperties qp)
			throws MetadataStoreException {
		List<OperatesOnData> opOns = qp.getOperatesOnData();
		if (opOns != null && opOns.size() > 0) {
			for (OperatesOnData opOn : opOns) {
				InsertRow ir = new InsertRow(new TableName(opOnTable), null);
				try {
					int localId = getNewIdentifier(conn, opOnTable);
					ir.addPreparedArgument(idColumn, localId);
					ir.addPreparedArgument(fk_main, operatesOnId);
					ir.addPreparedArgument("operateson", opOn.getOperatesOnId());
					ir.addPreparedArgument("operatesonid", opOn.getOperatesOnIdentifier());
					ir.addPreparedArgument("operatesonname", opOn.getOperatesOnName());
					LOG.debug(ir.getSql());
					ir.performInsert(conn);
				}
				catch (SQLException e) {
					String msg = Messages.getMessage("ERROR_SQL", ir.getSql(), e.getMessage());
					LOG.debug(msg);
					throw new MetadataStoreException(msg);
				}
			}
		}
	}

	private int getNewIdentifier(Connection connection, String databaseTable) throws MetadataStoreException {
		int localId = getLastDatasetId(connection, databaseTable);
		return ++localId;
	}

	private void deleteExistingRows(Connection connection, int operatesOnId, String databaseTable)
			throws MetadataStoreException {
		PreparedStatement stmt = null;
		StringWriter sqlStatement = new StringWriter();
		try {
			sqlStatement.append("DELETE FROM " + databaseTable + " WHERE " + fk_main + " = ?");
			stmt = connection.prepareStatement(sqlStatement.toString());
			stmt.setInt(1, operatesOnId);
			LOG.debug(stmt.toString());
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			String msg = Messages.getMessage("ERROR_SQL", sqlStatement.toString(), e.getMessage());
			LOG.debug(msg);
			throw new MetadataStoreException(msg);
		}
		finally {
			close(null, stmt, null, LOG);
		}
	}

	/**
	 * Provides the last known id in the databaseTable. So it is possible to insert new
	 * datasets into this table come from this id.
	 * @param conn
	 * @param databaseTable the databaseTable that is requested.
	 * @return the last Primary Key ID of the databaseTable.
	 * @throws MetadataStoreException
	 */
	private int getLastDatasetId(Connection conn, String databaseTable) throws MetadataStoreException {
		int result = 0;
		String selectIDRows = null;
		// TODO: use SQLDialect
		if (dialect instanceof PostGISDialect) {
			selectIDRows = "SELECT " + idColumn + " from " + databaseTable + " ORDER BY " + idColumn + " DESC LIMIT 1";
		}
		if (dialect.getClass().getSimpleName().equals("MSSQLDialect")) {
			selectIDRows = "SELECT TOP 1 " + idColumn + " from " + databaseTable + " ORDER BY " + idColumn + " DESC";
		}
		if (dialect.getClass().getSimpleName().equals("OracleDialect")) {
			String inner = "SELECT " + idColumn + " from " + databaseTable + " ORDER BY " + idColumn + " DESC";
			selectIDRows = "SELECT * FROM (" + inner + ") WHERE rownum = 1";
		}
		Statement stmt = null;
		ResultSet rsBrief = null;
		try {
			stmt = conn.createStatement();
			rsBrief = stmt.executeQuery(selectIDRows);
			while (rsBrief.next()) {
				result = rsBrief.getInt(1);
			}
		}
		catch (SQLException e) {
			String msg = Messages.getMessage("ERROR_SQL", selectIDRows, e.getMessage());
			LOG.debug(msg);
			throw new MetadataStoreException(msg);
		}
		finally {
			close(rsBrief, stmt, null, LOG);
		}

		return result;

	}

	private String concatenate(List<String> values) {
		if (values == null || values.isEmpty())
			return null;
		String s = "";
		for (String value : values) {
			if (value != null) {
				s = s + '|' + value.replace("\'", "\'\'");
			}
		}
		if (!values.isEmpty())
			s = s + '|';
		return s;
	}

}