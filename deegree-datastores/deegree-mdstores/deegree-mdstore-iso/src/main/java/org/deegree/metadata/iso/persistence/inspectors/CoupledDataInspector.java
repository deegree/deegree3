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
package org.deegree.metadata.iso.persistence.inspectors;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.iso.persistence.ISOPropertyNameMapper;
import org.deegree.metadata.iso.types.OperatesOnData;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.inspectors.RecordInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.CoupledResourceInspector;
import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;

/**
 * Inspects the coupling of data-metadata and service-metadata.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class CoupledDataInspector implements RecordInspector<ISORecord> {

	private static final Logger LOG = getLogger(CoupledDataInspector.class);

	private final CoupledResourceInspector config;

	private final NamespaceBindings nsContext;

	public CoupledDataInspector(CoupledResourceInspector config) {
		this.config = config;
		nsContext = new NamespaceBindings();
		nsContext.addNamespace("srv", "http://www.isotc211.org/2005/srv");
		nsContext.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
		nsContext.addNamespace("gco", "http://www.isotc211.org/2005/gco");
	}

	/**
	 * @param conn
	 * @param operatesOnStringUuIdAttribute
	 * @return true if there is a coupling with a data-metadata, otherwise false.
	 */
	private boolean determineCoupling(Connection conn, List<String> operatesOnStringUuIdAttribute, SQLDialect dialect)
			throws MetadataInspectorException {
		// consistencyCheck( operatesOnStringUuIdAttribute );
		boolean isCoupled = false;

		for (String a : operatesOnStringUuIdAttribute) {
			// isCoupled = getCoupledDataMetadatasets( a.trim(), connectionType );
			isCoupled = getCoupledDataMetadatasets(conn, a.trim(), dialect);
		}

		return isCoupled;
	}

	private boolean checkConsistency(List<String> o, List<String> i) {
		boolean isConsistent = true;

		while (!i.isEmpty()) {
			String id = i.get(0);
			for (String uuid : o) {
				if (!id.equals(uuid)) {
					isConsistent = false;
				}
				else {
					isConsistent = true;
					break;
				}
			}
			i.remove(0);
		}
		return isConsistent;
	}

	/**
	 * If there is a data metadata record available for the service metadata record.
	 *
	 */
	private boolean getCoupledDataMetadatasets(Connection conn, String resourceIdentifier, SQLDialect dialect)
			throws MetadataInspectorException {

		String resourceIdCol = ISOPropertyNameMapper.CommonColumnNames.resourceid.name();
		String mainTable = ISOPropertyNameMapper.DatabaseTables.idxtb_main.name();

		ResultSet rs = null;
		PreparedStatement stm = null;
		LOG.warn("Check table / column names.");
		String s = "SELECT " + resourceIdCol + " FROM " + mainTable + " WHERE " + resourceIdCol + " = ?";
		try {
			stm = conn.prepareStatement(s);
			stm.setString(1, resourceIdentifier);
			rs = stm.executeQuery();
			return rs.next();
		}
		catch (SQLException e) {
			String msg = Messages.getMessage("ERROR_SQL", s, e.getMessage());
			LOG.debug(msg);
			throw new MetadataInspectorException(msg);
		}
		finally {
			close(rs);
			close(stm);
		}
	}

	@Override
	public ISORecord inspect(ISORecord record, Connection conn, SQLDialect dialect) throws MetadataInspectorException {

		XMLAdapter a = new XMLAdapter(record.getAsOMElement());

		OMElement identificationInfo = a.getElement(a.getRootElement(),
				new XPath("./gmd:identificationInfo[1]", nsContext));

		List<OMElement> operatesOnElemList = a.getElements(identificationInfo,
				new XPath("./srv:SV_ServiceIdentification/srv:operatesOn", nsContext));
		List<String> operatesOnUuidList = new ArrayList<String>();
		List<String> resourceIDs = new ArrayList<String>();
		for (OMElement operatesOnElem : operatesOnElemList) {
			String uuid = operatesOnElem.getAttributeValue(new QName("uuidref"));
			if (uuid != null) {
				operatesOnUuidList.add(uuid.trim());
			}
		}

		List<OMElement> operatesOnCoupledResources = a.getElements(identificationInfo,
				new XPath("./srv:SV_ServiceIdentification/srv:coupledResource/srv:SV_CoupledResource", nsContext));
		List<OperatesOnData> operatesOnDataList = new ArrayList<OperatesOnData>();

		for (OMElement operatesOnCoupledResource : operatesOnCoupledResources) {
			String operatesOnIdentifier = a.getNodeAsString(operatesOnCoupledResource,
					new XPath("./srv:identifier/gco:CharacterString", nsContext), null);

			String operationName = a.getNodeAsString(operatesOnCoupledResource,
					new XPath("./srv:operationName/gco:CharacterString", nsContext), null);

			String scopedName = a.getNodeAsString(operatesOnCoupledResource, new XPath("./gco:ScopedName", nsContext),
					null);
			operatesOnDataList.add(new OperatesOnData(scopedName, operatesOnIdentifier, operationName));
			resourceIDs.add(operatesOnIdentifier.trim());

		}

		String couplingType = a.getNodeAsString(identificationInfo, new XPath(
				"./srv:SV_ServiceIdentification/srv:couplingType/srv:SV_CouplingType/@codeListValue", nsContext), null);

		/*---------------------------------------------------------------
		 * SV_ServiceIdentification
		 * Check for consistency in the coupling.
		 *
		 *---------------------------------------------------------------*/
		LOG.debug("checking consistency in coupling...");
		if (couplingType != null) {

			if (couplingType.equals("loose")) {
				// nothing to check
				LOG.debug("coupling: loose...");

			}
			else {
				LOG.debug("coupling: tight/mixed...");
				boolean throwException = determineCoupling(conn, operatesOnUuidList, dialect)
						&& !checkConsistency(operatesOnUuidList, resourceIDs);
				if (throwException && config.isThrowConsistencyError()) {
					String msg = Messages.getMessage("ERROR_COUPLING");
					LOG.debug(msg);
					throw new MetadataInspectorException(msg);
				}
			}
		}
		// TODO create new instance
		return record;
	}

	public CoupledResourceInspector getCi() {
		return config;
	}

}
