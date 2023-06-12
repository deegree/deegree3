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

import static org.deegree.protocol.csw.CSWConstants.SDS_NS;
import static org.deegree.protocol.csw.CSWConstants.SDS_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.inspectors.RecordInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.FileIdentifierInspector;
import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;

/**
 * Inspects whether the fileIdentifier should be set when inserting a metadata or not and
 * what consequences should occur.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class FIInspector implements RecordInspector<ISORecord> {

	private static final Logger LOG = getLogger(FIInspector.class);

	private final FileIdentifierInspector config;

	private final NamespaceBindings nsContext = new NamespaceBindings();

	public FIInspector(FileIdentifierInspector inspector) {
		this.config = inspector;
		nsContext.addNamespace("srv", "http://www.isotc211.org/2005/srv");
		nsContext.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
		nsContext.addNamespace("gco", "http://www.isotc211.org/2005/gco");
		nsContext.addNamespace(SDS_PREFIX, SDS_NS);
	}

	/**
	 * @param fi the fileIdentifier that should be determined for one metadata, can be
	 * <Code>null</Code>.
	 * @param rsList the list of resourceIdentifier, not <Code>null</Code>.
	 * @param id the id-attribute, can be <Code>null<Code>.
	 *
	@param uuid
	 *            the uuid-attribure, can be <Code>null</Code>.
	 * @return the new fileIdentifier.
	 */
	private List<String> determineFileIdentifier(Connection conn, String[] fi, List<String> rsList, String id,
			String uuid, SQLDialect dialect) throws MetadataInspectorException {
		List<String> idList = new ArrayList<String>();
		if (fi.length != 0) {
			for (String f : fi) {
				LOG.debug(Messages.getMessage("INFO_FI_AVAILABLE", f.trim()));
				idList.add(f.trim());
			}
			return idList;
		}
		if (config != null && !config.isRejectEmpty()) {
			if (rsList.size() == 0 && id == null && uuid == null) {
				LOG.debug(Messages.getMessage("INFO_FI_GENERATE_NEW"));
				idList.add(new IdUtils(conn, dialect).generateUUID());
				LOG.debug(Messages.getMessage("INFO_FI_NEW", idList));
			}
			else {
				if (rsList.size() == 0 && id != null) {
					LOG.debug(Messages.getMessage("INFO_FI_DEFAULT_ID", id));
					idList.add(id);
				}
				else if (rsList.size() == 0 && uuid != null) {
					LOG.debug(Messages.getMessage("INFO_FI_DEFAULT_UUID", uuid));
					idList.add(uuid);
				}
				else {
					LOG.debug(Messages.getMessage("INFO_FI_DEFAULT_RSID", rsList.get(0)));
					idList.add(rsList.get(0));
				}
			}
			return idList;
		}
		if (rsList.size() == 0) {
			String msg = Messages.getMessage("ERROR_REJECT_FI");
			LOG.debug(msg);
			throw new MetadataInspectorException(msg);
		}
		LOG.debug(Messages.getMessage("INFO_FI_DEFAULT_RSID", rsList.get(0)));
		idList.add(rsList.get(0));
		return idList;

	}

	@Override
	public ISORecord inspect(ISORecord record, Connection conn, SQLDialect dialect) throws MetadataInspectorException {

		XMLAdapter a = new XMLAdapter(record.getAsOMElement());
		OMElement rootEl = record.getAsOMElement();

		String[] fileIdentifierString = a.getNodesAsStrings(rootEl,
				new XPath("./gmd:fileIdentifier/gco:CharacterString", nsContext));

		String identificationInfoXPathExpr = "./gmd:identificationInfo/srv:SV_ServiceIdentification | ./gmd:identificationInfo/gmd:MD_DataIdentification"
				+ " | ./gmd:identificationInfo/sds:SV_ServiceIdentification";
		OMElement identificationInfo = a.getElement(rootEl, new XPath(identificationInfoXPathExpr, nsContext));
		String dataIdentificationId = identificationInfo.getAttributeValue(new QName("id"));
		String dataIdentificationUuId = identificationInfo.getAttributeValue(new QName("uuid"));
		List<OMElement> identifier = a.getElements(identificationInfo,
				new XPath("./gmd:citation/gmd:CI_Citation/gmd:identifier", nsContext));
		List<String> resourceIdentifierList = new ArrayList<String>();
		for (OMElement resourceElement : identifier) {
			String resourceIdentifier = a.getNodeAsString(resourceElement, new XPath(
					"./gmd:MD_Identifier/gmd:code/gco:CharacterString | ./gmd:RS_Identifier/gmd:code/gco:CharacterString",
					nsContext), null);
			LOG.debug("resourceIdentifier: '" + resourceIdentifier + "' ");
			resourceIdentifierList.add(resourceIdentifier);

		}

		List<String> idList = determineFileIdentifier(conn, fileIdentifierString, resourceIdentifierList,
				dataIdentificationId, dataIdentificationUuId, dialect);
		if (!idList.isEmpty() && fileIdentifierString.length == 0) {
			for (String id : idList) {
				OMElement firstElement = rootEl.getFirstElement();
				firstElement.insertSiblingBefore(createFileIdentifierElement(id));
			}
		}
		return record;
	}

	private OMElement createFileIdentifierElement(String id) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace namespaceGMD = factory.createOMNamespace("http://www.isotc211.org/2005/gmd", "gmd");
		OMNamespace namespaceGCO = factory.createOMNamespace("http://www.isotc211.org/2005/gco", "gco");
		OMElement omFileIdentifier = factory.createOMElement("fileIdentifier", namespaceGMD);
		OMElement omFileCharacterString = factory.createOMElement("CharacterString", namespaceGCO);
		omFileIdentifier.addChild(omFileCharacterString);
		omFileCharacterString.setText(id);
		return omFileIdentifier;
	}

}