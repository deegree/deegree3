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

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XPath;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.iso.persistence.inspectors.CoupledDataInspector;
import org.deegree.metadata.iso.persistence.inspectors.FIInspector;
import org.deegree.metadata.iso.persistence.inspectors.HierarchyLevelInspector;
import org.deegree.metadata.iso.persistence.inspectors.InspireComplianceInspector;
import org.deegree.metadata.iso.persistence.inspectors.NamespaceNormalizationInspector;
import org.deegree.metadata.iso.persistence.queryable.Queryable;
import org.deegree.metadata.iso.persistence.queryable.QueryableConverter;
import org.deegree.metadata.iso.persistence.sql.QueryService;
import org.deegree.metadata.iso.persistence.sql.ServiceManager;
import org.deegree.metadata.iso.persistence.sql.ServiceManagerProvider;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.inspectors.MetadataSchemaValidationInspector;
import org.deegree.metadata.persistence.inspectors.RecordInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.CoupledResourceInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.FileIdentifierInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.Inspectors;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.QueryableProperties;
import org.deegree.metadata.persistence.iso19115.jaxb.InspireInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.NamespaceNormalizer;
import org.deegree.metadata.persistence.iso19115.jaxb.QueryableProperty;
import org.deegree.metadata.persistence.iso19115.jaxb.QueryableProperty.Name;
import org.deegree.metadata.persistence.iso19115.jaxb.SchemaValidator;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * {@link MetadataStore} implementation for accessing ISO 19115 records stored in spatial
 * SQL databases (currently only supports PostgreSQL / PostGIS).
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class ISOMetadataStore implements MetadataStore<ISORecord> {

	private static final Logger LOG = getLogger(ISOMetadataStore.class);

	private final String connectionId;

	private final ISOMetadataStoreConfig config;

	private final List<RecordInspector<ISORecord>> inspectorChain = new ArrayList<RecordInspector<ISORecord>>();

	/**
	 * Used to limit the fetch size for SELECT statements that potentially return a lot of
	 * rows.
	 */
	public static final int DEFAULT_FETCH_SIZE = 100;

	private final SQLDialect dialect;

	private final List<Queryable> queryables = new ArrayList<Queryable>();

	private Workspace workspace;

	private ResourceMetadata<MetadataStore<? extends MetadataRecord>> metadata;

	/**
	 * Creates a new {@link ISOMetadataStore} instance from the given JAXB configuration
	 * object.
	 * @param config
	 * @param dialect
	 * @throws ResourceInitException
	 */
	public ISOMetadataStore(ISOMetadataStoreConfig config, SQLDialect dialect,
			ResourceMetadata<MetadataStore<? extends MetadataRecord>> metadata, Workspace workspace)
			throws ResourceInitException {
		this.dialect = dialect;
		this.metadata = metadata;
		this.workspace = workspace;
		this.connectionId = config.getJDBCConnId();
		this.config = config;

		// this.varToValue = new HashMap<String, String>();
		// String systemStartDate = "2010-11-16";
		// varToValue.put( "${SYSTEM_START_DATE}", systemStartDate );
		// build inspector chain
		Inspectors inspectors = config.getInspectors();
		if (inspectors != null) {
			FileIdentifierInspector fi = inspectors.getFileIdentifierInspector();
			InspireInspector ii = inspectors.getInspireInspector();
			CoupledResourceInspector cri = inspectors.getCoupledResourceInspector();
			SchemaValidator sv = inspectors.getSchemaValidator();
			NamespaceNormalizer nn = inspectors.getNamespaceNormalizer();
			if (fi != null) {
				inspectorChain.add(new FIInspector(fi));
			}
			if (ii != null) {
				inspectorChain.add(new InspireComplianceInspector(ii));
			}
			if (cri != null) {
				inspectorChain.add(new CoupledDataInspector(cri));
			}
			if (sv != null) {
				inspectorChain.add(new MetadataSchemaValidationInspector<ISORecord>());
			}
			if (nn != null) {
				inspectorChain.add(new NamespaceNormalizationInspector(nn));
			}
		}
		// hard coded because there is no configuration planned
		inspectorChain.add(new HierarchyLevelInspector());

		QueryableProperties queryableProperties = config.getQueryableProperties();
		if (queryableProperties != null) {
			for (QueryableProperty qp : queryableProperties.getQueryableProperty()) {
				QueryableConverter converter = null;
				if (qp.getConverterClass() != null) {
					String converterClass = qp.getConverterClass();
					Object cc;
					try {
						cc = Class.forName(converterClass).newInstance();
						if (!(cc instanceof QueryableConverter)) {
							throw new ResourceInitException("QuerableConverter class " + converterClass
									+ " is not a subtype of " + QueryableConverter.class.getCanonicalName());
						}
						converter = (QueryableConverter) cc;
					}
					catch (Exception e) {
						throw new ResourceInitException("Could not create QueraybleConverter class " + converterClass,
								e);
					}
				}

				// TODO: namespace bindings configured by the user!?
				NamespaceBindings namespaceContext = CommonNamespaces.getNamespaceContext();
				namespaceContext.addNamespace(CSWConstants.SRV_PREFIX, CSWConstants.SRV_NS);
				namespaceContext.addNamespace(CSWConstants.SDS_PREFIX, CSWConstants.SDS_NS);
				XPath xpath = new XPath(qp.getXpath(), namespaceContext);
				List<Name> name = qp.getName();
				List<QName> names = new ArrayList<QName>();
				for (Name n : name) {
					names.add(new QName(n.getNamespace(), n.getValue()));
				}
				getQueryables().add(new Queryable(xpath, names, qp.isIsMultiple(), qp.getColumn(), converter));
			}
		}
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	/**
	 * Returns the JDBC connection id.
	 * @return the JDBC connection id, never <code>null</code>
	 */
	@Override
	public String getConnId() {
		return connectionId;
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public MetadataResultSet<ISORecord> getRecords(final MetadataQuery query) throws MetadataStoreException {
		final String operationName = "getRecords";
		LOG.debug(Messages.getMessage("INFO_EXEC", operationName));
		QueryService queryService = getReadOnlySqlService();
		return queryService.execute(query, getConnection());
	}

	/**
	 * The mandatory "resultType" attribute in the GetRecords operation is set to "hits".
	 * @throws MetadataStoreException
	 */
	public int getRecordCount(final MetadataQuery query) throws MetadataStoreException {
		final String resultTypeName = "hits";
		LOG.debug(Messages.getMessage("INFO_EXEC", "do " + resultTypeName + " on getRecords"));
		try {
			QueryService queryService = getReadOnlySqlService();
			return queryService.executeCounting(query, getConnection());
		}
		catch (Throwable t) {
			LOG.debug(t.getMessage(), t);
			String msg = Messages.getMessage("ERROR_REQUEST_TYPE", ResultType.results.name(), t.getMessage());
			LOG.debug(msg);
			throw new MetadataStoreException(msg);
		}
		finally {
			// Don't close the ResultSet or PreparedStatement if no error occurs, the
			// ResultSet is needed in the
			// ISOMetadataResultSet and both will be closed by
			// org.deegree.metadata.persistence.XMLMetadataResultSet#close().
		}
	}

	@Override
	public MetadataResultSet<ISORecord> getRecordById(final List<String> idList, final QName[] recordTypeNames)
			throws MetadataStoreException {
		LOG.debug(Messages.getMessage("INFO_EXEC", "getRecordsById"));
		QueryService qh = getReadOnlySqlService();
		return qh.executeGetRecordById(idList, getConnection());
	}

	@Override
	public MetadataStoreTransaction acquireTransaction() throws MetadataStoreException {
		ISOMetadataStoreTransaction ta = null;
		try {
			Connection conn = getConnection();
			ta = new ISOMetadataStoreTransaction(conn, dialect, inspectorChain, getQueryables(), config.getAnyText());
		}
		catch (SQLException e) {
			LOG.error("error " + e.getMessage(), e);
			throw new MetadataStoreException(e.getMessage(), e);
		}
		return ta;
	}

	/**
	 * Acquires a new JDBC connection from the configured connection pool.
	 * <p>
	 * The returned connection has auto commit off to make it suitable for dealing with
	 * very large result sets (cursor-based results). If auto commit is not disabled, some
	 * JDBC drivers (e.g. PostGIS) will always fetch all rows at once (which will lead to
	 * out of memory errors). See
	 * <a href="http://jdbc.postgresql.org/documentation/81/query.html"/> for more
	 * information. Note that it may still be necessary to set the fetch size and to close
	 * the connection to return it to the pool.
	 * </p>
	 * @return connection with auto commit set to off, never <code>null</code>
	 * @throws MetadataStoreException
	 */
	private Connection getConnection() throws MetadataStoreException {
		Connection conn = null;
		try {
			ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, connectionId);
			conn = prov.getConnection();
			conn.setAutoCommit(false);
		}
		catch (Throwable e) {
			e.printStackTrace();
			throw new MetadataStoreException(e.getMessage());
		}
		return conn;
	}

	@Override
	public String getType() {
		return "iso";
	}

	public List<Queryable> getQueryables() {
		return queryables;
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

	private QueryService getReadOnlySqlService() throws MetadataStoreException {
		ServiceManager serviceManager = ServiceManagerProvider.getInstance().getServiceManager();
		return serviceManager.getQueryService(dialect, queryables);
	}

}