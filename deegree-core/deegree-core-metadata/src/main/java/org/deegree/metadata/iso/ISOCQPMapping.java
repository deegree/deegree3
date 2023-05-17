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
package org.deegree.metadata.iso;

import static org.deegree.protocol.csw.CSWConstants.APISO_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XPath;

/**
 * handles mapping of the CSW Queryable Properties to a ISO Record
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ISOCQPMapping {

	private static final String IDENTIFIER_XPATH = "/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";

	private static final String MODIFIED_XPATH = "/gmd:MD_Metadata/gmd:dateStamp/gco:Date";

	private static final String FORMAT_XPATH = "/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString";

	private static final String TYPE_XPATH = "/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue";

	private static final String TITLE_DATA_XPATH = "/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";

	private static final String TITLE_SERVICE_XPATH = "/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";

	private static final String ABSTRACT_DATA_XPATH = "/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString";

	private static final String ABSTRACT_SERVICE_XPATH = "/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_ServiceIdentification/gmd:abstract/gco:CharacterString";

	private static final List<Pair<List<QName>, XPath>> cqpToISO = new ArrayList<Pair<List<QName>, XPath>>();

	private static final List<Pair<List<QName>, XPath>> cqpToISO_Data = new ArrayList<Pair<List<QName>, XPath>>();

	private static final List<Pair<List<QName>, XPath>> cqpToISO_Service = new ArrayList<Pair<List<QName>, XPath>>();

	protected static final NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();

	static {
		cqpToISO_Data.add(new Pair<List<QName>, XPath>(createQNameList("title", "Title"),
				new XPath(TITLE_DATA_XPATH, nsContext)));
		cqpToISO_Service.add(new Pair<List<QName>, XPath>(createQNameList("title", "Title"),
				new XPath(TITLE_SERVICE_XPATH, nsContext)));

		cqpToISO_Data.add(new Pair<List<QName>, XPath>(createQNameList("abstract", "Abstract"),
				new XPath(ABSTRACT_DATA_XPATH, nsContext)));
		cqpToISO_Service.add(new Pair<List<QName>, XPath>(createQNameList("abstract", "Abstract"),
				new XPath(ABSTRACT_SERVICE_XPATH, nsContext)));

		cqpToISO.add(new Pair<List<QName>, XPath>(createQNameList("identifier", "Identifier"),
				new XPath(IDENTIFIER_XPATH, nsContext)));
		cqpToISO.add(new Pair<List<QName>, XPath>(createQNameList("modified", "Modified"),
				new XPath(MODIFIED_XPATH, nsContext)));

		cqpToISO_Data.add(new Pair<List<QName>, XPath>(createQNameList("abstract", "Abstract"),
				new XPath(ABSTRACT_DATA_XPATH, nsContext)));
		cqpToISO_Service.add(new Pair<List<QName>, XPath>(createQNameList("abstract", "Abstract"),
				new XPath(ABSTRACT_SERVICE_XPATH, nsContext)));

		cqpToISO
			.add(new Pair<List<QName>, XPath>(createQNameList("format", "Format"), new XPath(FORMAT_XPATH, nsContext)));
		cqpToISO.add(new Pair<List<QName>, XPath>(createQNameList("type", "Type"), new XPath(TYPE_XPATH, nsContext)));
	}

	public static XPath getXPathFromCQP(QName qname, String type) {
		XPath xPath = getFromList(cqpToISO, qname);
		if (xPath != null)
			return xPath;
		if ("service".equals(type))
			return getFromList(cqpToISO_Service, qname);
		return getFromList(cqpToISO_Data, qname);
	}

	private static XPath getFromList(List<Pair<List<QName>, XPath>> list, QName qname) {
		for (Pair<List<QName>, XPath> pairs : list) {
			if (pairs.first.contains(qname)) {
				return pairs.second;
			}
		}
		return null;
	}

	private static List<QName> createQNameList(String... strings) {
		List<QName> list = new ArrayList<QName>();
		for (int i = 0; i < strings.length; i++) {
			list.add(new QName(APISO_NS, strings[i]));
			list.add(new QName(CSW_202_NS, strings[i]));
		}
		return list;
	}

}
