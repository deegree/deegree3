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
package org.deegree.protocol.wfs.storedquery.xml;

import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wfs.query.StoredQuery;
import org.deegree.protocol.wfs.storedquery.Parameter;
import org.deegree.protocol.wfs.storedquery.QueryExpressionText;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;

/**
 * Defines the template for a {@link StoredQuery}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class StoredQueryDefinitionXMLAdapter extends XMLAdapter {

	/** Namespace context with predefined bindings "wfs200" */
	protected static final NamespaceBindings nsContext;

	/** Namespace binding for WFS 2.0.0 constructs */
	protected final static String WFS_200_PREFIX = "wfs200";

	static {
		nsContext = new NamespaceBindings(XMLAdapter.nsContext);
		nsContext.addNamespace(WFS_200_PREFIX, WFS_200_NS);
		nsContext.addNamespace("ows", OWS_11_NS);
	}

	public StoredQueryDefinition parse() {

		// <xsd:attribute name="id" type="xsd:anyURI" use="required"/>
		String id = getRequiredNodeAsString(rootElement, new XPath("@id", nsContext));

		// <xsd:element ref="wfs:Title" minOccurs="0" maxOccurs="unbounded"/>
		List<OMElement> titleEls = getElements(rootElement, new XPath("wfs200:Title", nsContext));
		List<LanguageString> titles = new ArrayList<LanguageString>(titleEls.size());
		for (OMElement titleEl : titleEls) {
			String lang = getNodeAsString(titleEl, new XPath("@xml:lang", nsContext), null);
			String value = titleEl.getText();
			titles.add(new LanguageString(value, lang));
		}

		// <xsd:element ref="wfs:Abstract" minOccurs="0" maxOccurs="unbounded"/>
		List<OMElement> abstractEls = getElements(rootElement, new XPath("wfs200:Abstract", nsContext));
		List<LanguageString> abstracts = new ArrayList<LanguageString>(abstractEls.size());
		for (OMElement abstractEl : abstractEls) {
			String lang = getNodeAsString(abstractEl, new XPath("@xml:lang", nsContext), null);
			String value = abstractEl.getText();
			abstracts.add(new LanguageString(value, lang));
		}

		// <xsd:element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded"/>
		List<OMElement> metadataEls = getElements(rootElement, new XPath("ows:Metadata", nsContext));

		// <xsd:element name="Parameter" type="wfs:ParameterExpressionType" minOccurs="0"
		// maxOccurs="unbounded"/>
		List<OMElement> parameterEls = getElements(rootElement, new XPath("wfs200:Parameter", nsContext));
		List<Parameter> parameters = new ArrayList<Parameter>(parameterEls.size());
		for (OMElement parameterEl : parameterEls) {
			parameters.add(parseParameter(parameterEl));
		}

		// <xsd:element name="QueryExpressionText" type="wfs:QueryExpressionTextType"
		// minOccurs="1"
		// maxOccurs="unbounded"/>
		List<OMElement> queryExprEls = getRequiredElements(rootElement,
				new XPath("wfs200:QueryExpressionText", nsContext));
		List<QueryExpressionText> queryExpressionTexts = new ArrayList<QueryExpressionText>(queryExprEls.size());
		for (OMElement queryExprEl : queryExprEls) {
			queryExpressionTexts.add(parseQueryExpressionText(queryExprEl));
		}

		return new StoredQueryDefinition(id, titles, abstracts, metadataEls, parameters, queryExpressionTexts);
	}

	private Parameter parseParameter(OMElement el) {

		// <xsd:attribute name="name" type="xsd:string" use="required"/>
		String name = getRequiredNodeAsString(el, new XPath("@name", nsContext));

		// <xsd:attribute name="type" type="xsd:QName" use="required"/>
		QName type = getRequiredNodeAsQName(el, new XPath("@type", nsContext));

		// <xsd:element ref="wfs:Title" minOccurs="0" maxOccurs="unbounded"/>
		List<OMElement> titleEls = getElements(el, new XPath("wfs200:Title", nsContext));
		List<LanguageString> titles = new ArrayList<LanguageString>(titleEls.size());
		for (OMElement titleEl : titleEls) {
			String lang = getNodeAsString(titleEl, new XPath("@xml:lang", nsContext), null);
			String value = titleEl.getText();
			titles.add(new LanguageString(value, lang));
		}

		// <xsd:element ref="wfs:Abstract" minOccurs="0" maxOccurs="unbounded"/>
		List<OMElement> abstractEls = getElements(el, new XPath("wfs200:Abstract", nsContext));
		List<LanguageString> abstracts = new ArrayList<LanguageString>(abstractEls.size());
		for (OMElement abstractEl : abstractEls) {
			String lang = getNodeAsString(abstractEl, new XPath("@xml:lang", nsContext), null);
			String value = abstractEl.getText();
			abstracts.add(new LanguageString(value, lang));
		}

		// <xsd:element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded"/>
		List<OMElement> metadataEls = getElements(el, new XPath("ows:Metadata", nsContext));

		return new Parameter(name, type, titles, abstracts, metadataEls);
	}

	private QueryExpressionText parseQueryExpressionText(OMElement el) {

		// <xsd:attribute name="returnFeatureTypes" type="wfs:ReturnFeatureTypesListType"
		// use="required"/>
		List<QName> returnFtNames = parseFeatureTypes(el);

		// <xsd:attribute name="language" type="xsd:anyURI" use="required"/>
		String language = getRequiredNodeAsString(el, new XPath("@language", nsContext));

		// <xsd:attribute name="isPrivate" type="xsd:boolean" default="false"/>
		boolean isPrivate = getNodeAsBoolean(el, new XPath("@isPrivate", nsContext), false);

		// <xsd:any namespace="##other" processContents="skip" minOccurs="0"
		// maxOccurs="unbounded"/>
		// <xsd:any namespace="##targetNamespace" processContents="skip" minOccurs="0"
		// maxOccurs="unbounded"/>
		List<OMElement> childEls = getElements(el, new XPath("*", nsContext));

		return new QueryExpressionText(returnFtNames, language, isPrivate, childEls);
	}

	private List<QName> parseFeatureTypes(OMElement el) {
		String returnFtsStr = getRequiredNodeAsString(el, new XPath("@returnFeatureTypes", nsContext)).trim();
		if ("".equals(returnFtsStr))
			return new ArrayList<QName>();
		String[] tokens = StringUtils.split(returnFtsStr, " ");
		if (tokens.length == 1 && "${deegreewfs:ServedFeatureTypes}".equals(tokens[0]))
			return new ArrayList<QName>();

		List<QName> returnFtNames = new ArrayList<QName>(tokens.length);
		for (String token : tokens) {
			returnFtNames.add(parseQName(token, el));
		}
		return returnFtNames;
	}

}
