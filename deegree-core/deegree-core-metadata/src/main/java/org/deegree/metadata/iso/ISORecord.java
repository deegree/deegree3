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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.FilteringXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.metadata.DCRecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.iso.parsing.ParsedProfileElement;
import org.deegree.metadata.iso.parsing.RecordPropertyParser;
import org.deegree.metadata.iso.types.BoundingBox;
import org.deegree.metadata.iso.types.CRS;
import org.deegree.metadata.iso.types.Format;
import org.deegree.metadata.iso.types.Keyword;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an ISO 19115 {@link MetadataRecord}.
 * <p>
 * An ISO 19115 record can be either a data or a service metadata record. The root element
 * name for both types of records is {http://www.isotc211.org/2005/gmd}MD_Metadata.
 * <ul>
 * <li>Data Metadata: /gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset' (or
 * missing) or 'series' or 'application'</li>
 * <li>Service Metadata:
 * /gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='service'</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ISORecord implements MetadataRecord {

	private static Logger LOG = LoggerFactory.getLogger(ISORecord.class);

	/** Namespace for ISORecord elements */
	public static String ISO_RECORD_NS = CommonNamespaces.ISOAP10GMDNS;

	/** Schema URL for ISO Data and Service Metadata records **/
	public static final String SCHEMA_URL_GMD = "http://schemas.opengis.net/iso/19139/20060504/gmd/gmd.xsd";

	/** Additional schema URL for Service Metadata records **/
	public static final String SCHEMA_URL_SRV = "http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd";

	private OMElement root;

	private ParsedProfileElement pElem;

	private static final NamespaceBindings ns = CommonNamespaces.getNamespaceContext();

	static List<XPath> summaryFilterElementsXPath = new ArrayList<XPath>();

	static List<XPath> briefFilterElementsXPath = new ArrayList<XPath>();

	static {

		NamespaceBindings ns = new NamespaceBindings();
		ns.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
		ns.addNamespace("gco", "http://www.isotc211.org/2005/gco");

		briefFilterElementsXPath.add(new XPath("/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString", ns));
		briefFilterElementsXPath.add(new XPath("/gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode", ns));
		briefFilterElementsXPath
			.add(new XPath("/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode", ns));
		briefFilterElementsXPath.add(new XPath("/gmd:MD_Metadata/gmd:dateStamp/gco:DateTime", ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gmd:LanguageCode", ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:extentTypeCode/gco:Boolean",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal",
				ns));
		briefFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal",
				ns));

		summaryFilterElementsXPath.addAll(briefFilterElementsXPath);
		summaryFilterElementsXPath.add(new XPath("/gmd:MD_Metadata/gmd:language/gmd:LanguageCode", ns));
		summaryFilterElementsXPath.add(new XPath("/gmd:MD_Metadata/gmd:characterSet/gmd:MD_CharacterSetCode", ns));
		summaryFilterElementsXPath.add(new XPath("/gmd:MD_Metadata/gmd:metadataStandardName/gco:CharacterString", ns));
		summaryFilterElementsXPath
			.add(new XPath("/gmd:MD_Metadata/gmd:metadataStandardVersion/gco:CharacterString", ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:version/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions/gco:CharacterString",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:characterSet/gmd:MD_CharacterSetCode",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode",
				ns));
		summaryFilterElementsXPath.add(new XPath(
				"/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
				ns));
	}

	/**
	 * Creates a new {@link ISORecord} instance from the given XML stream.
	 * @param xmlStream xml stream, must not be <code>null</code> and point to the
	 * record's root element
	 */
	public ISORecord(XMLStreamReader xmlStream) {
		this.root = new XMLAdapter(xmlStream).getRootElement();
		root.declareDefaultNamespace("http://www.isotc211.org/2005/gmd");
	}

	public ISORecord(OMElement root) {
		this(root.getXMLStreamReader());
	}

	private synchronized ParsedProfileElement getParsedProfileElement() {
		if (pElem == null) {
			pElem = new RecordPropertyParser(root).parse();
		}
		return pElem;
	}

	@Override
	public QName getName() {
		return root.getQName();
	}

	@Override
	public boolean eval(Filter filter) throws FilterEvaluationException {
		return filter.evaluate(this, new ISORecordEvaluator());
	}

	@Override
	public String[] getAbstract() {

		List<String> l = getParsedProfileElement().getQueryableProperties().get_abstract();
		String[] s = new String[l.size()];
		int counter = 0;
		for (String st : l) {
			s[counter++] = st;
		}

		return s;
	}

	@Override
	public Envelope[] getBoundingBox() {

		List<BoundingBox> bboxList = getParsedProfileElement().getQueryableProperties().getBoundingBox();
		if (getParsedProfileElement().getQueryableProperties().getCrs().isEmpty()) {
			List<CRS> newCRSList = new LinkedList<CRS>();
			for (BoundingBox b : bboxList) {
				newCRSList.add(new CRS("4326", "EPSG", null));
			}

			getParsedProfileElement().getQueryableProperties().setCrs(newCRSList);
		}

		Envelope[] env = new Envelope[bboxList.size()];
		int counter = 0;
		for (BoundingBox box : bboxList) {
			CRS bboxCRS = getParsedProfileElement().getQueryableProperties().getCrs().get(counter);
			// convert to the deegree CRSCodeType - this is not nice!
			CRSCodeType crsCT;
			if (bboxCRS.getAuthority() != null)
				crsCT = new CRSCodeType(bboxCRS.getCrsId(), bboxCRS.getAuthority());
			else
				crsCT = new CRSCodeType(bboxCRS.getCrsId());
			ICRS crs = CRSManager.getCRSRef(crsCT.toString());
			env[counter++] = new GeometryFactory().createEnvelope(box.getWestBoundLongitude(),
					box.getSouthBoundLatitude(), box.getEastBoundLongitude(), box.getNorthBoundLatitude(), crs);
		}
		return env;
	}

	@Override
	public String[] getFormat() {
		List<Format> formats = getParsedProfileElement().getQueryableProperties().getFormat();
		String[] format = new String[formats.size()];
		int counter = 0;
		for (Format f : formats) {
			format[counter++] = f.getName();
		}
		return format;
	}

	@Override
	public String getIdentifier() {
		return getParsedProfileElement().getQueryableProperties().getIdentifier();
	}

	@Override
	public Date getModified() {
		return getParsedProfileElement().getQueryableProperties().getModified();
	}

	@Override
	public String[] getRelation() {
		List<String> l = getParsedProfileElement().getReturnableProperties().getRelation();
		String[] s = new String[l.size()];
		int counter = 0;
		for (String st : l) {
			s[counter++] = st;
		}

		return s;
	}

	@Override
	public Object[] getSpatial() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getTitle() {
		List<String> l = getParsedProfileElement().getQueryableProperties().getTitle();
		String[] s = new String[l.size()];
		int counter = 0;
		for (String st : l) {
			s[counter++] = st;
		}
		return s;
	}

	@Override
	public String getType() {

		return getParsedProfileElement().getQueryableProperties().getType();
	}

	@Override
	public String[] getSubject() {

		List<Keyword> keywords = getParsedProfileElement().getQueryableProperties().getKeywords();
		int keywordSizeCount = 0;
		for (Keyword k : keywords) {

			keywordSizeCount += k.getKeywords().size();

		}
		List<String> topicCategories = getParsedProfileElement().getQueryableProperties().getTopicCategory();

		String[] subjects = new String[keywordSizeCount + topicCategories.size()];
		int counter = 0;
		for (Keyword k : keywords) {
			for (String kName : k.getKeywords()) {
				subjects[counter++] = kName;
			}
		}
		for (String topics : topicCategories) {
			subjects[counter++] = topics;
		}

		return subjects;
	}

	/**
	 * @return the ISORecord as xmlStreamReader with skipped startDocument-preamble
	 * @throws XMLStreamException
	 */
	public XMLStreamReader getAsXMLStream() throws XMLStreamException {
		root.declareDefaultNamespace("http://www.isotc211.org/2005/gmd");
		XMLStreamReader xmlStream = root.getXMLStreamReader();
		XMLStreamUtils.skipStartDocument(xmlStream);
		return xmlStream;
	}

	@Override
	public OMElement getAsOMElement() {
		return root;
	}

	public byte[] getAsByteArray() throws FactoryConfigurationError {
		root.declareDefaultNamespace("http://www.isotc211.org/2005/gmd");
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(20000);
			root.serialize(out);
			out.close();
			return out.toByteArray();
		}
		catch (XMLStreamException e) {
			return root.toString().getBytes();
		}
		catch (IOException e) {
			return root.toString().getBytes();
		}

	}

	@Override
	public void serialize(XMLStreamWriter writer, ReturnableElement returnType) throws XMLStreamException {
		switch (returnType) {
			case brief:
				toISOBrief(writer);
				break;
			case summary:
				toISOSummary(writer);
				break;
			case full:
				root.serialize(writer);
				break;
			default:
				toISOSummary(writer);
				break;
		}
	}

	@Override
	public void serialize(XMLStreamWriter writer, String[] elementNames) throws XMLStreamException {
		List<XPath> xpathEN = new ArrayList<XPath>();
		for (String s : elementNames) {
			xpathEN.add(new XPath(s, CommonNamespaces.getNamespaceContext()));
		}

		if (!xpathEN.isEmpty()) {
			writer = new FilteringXMLStreamWriter(writer, xpathEN);
		}
		root.serialize(writer);
	}

	@Override
	public DCRecord toDublinCore() {
		return new DCRecord(this);

	}

	public boolean isHasSecurityConstraints() {

		return getParsedProfileElement().getQueryableProperties().isHasSecurityConstraints();
	}

	@Override
	public String getContributor() {

		return getParsedProfileElement().getReturnableProperties().getContributor();
	}

	@Override
	public String getPublisher() {

		return getParsedProfileElement().getReturnableProperties().getPublisher();
	}

	@Override
	public String[] getRights() {
		List<String> l = getParsedProfileElement().getReturnableProperties().getRights();
		String[] s = new String[l.size()];
		int counter = 0;
		for (String st : l) {
			s[counter++] = st;
		}
		return s;

	}

	@Override
	public String getSource() {
		return getParsedProfileElement().getReturnableProperties().getSource();
	}

	@Override
	public String getCreator() {

		return getParsedProfileElement().getReturnableProperties().getCreator();
	}

	@Override
	public String getLanguage() {
		return getParsedProfileElement().getQueryableProperties().getLanguage();
	}

	public String getParentIdentifier() {
		return getParsedProfileElement().getQueryableProperties().getParentIdentifier();

	}

	public ParsedProfileElement getParsedElement() {
		return getParsedProfileElement();
	}

	public String getStringFromXPath(XPath xpath) {
		return new XMLAdapter().getNodeAsString(root, xpath, null);
	}

	public OMElement getNodeFromXPath(XPath xpath) {
		return new XMLAdapter().getElement(root, xpath);
	}

	public String[] getStringsFromXPath(XPath xpath) {
		return new XMLAdapter().getNodesAsStrings(root, xpath);
	}

	private void toISOSummary(XMLStreamWriter writer) throws XMLStreamException {
		writer = new FilteringXMLStreamWriter(writer, summaryFilterElementsXPath);
		root.serialize(writer);
	}

	private void toISOBrief(XMLStreamWriter writer) throws XMLStreamException {
		writer = new FilteringXMLStreamWriter(writer, briefFilterElementsXPath);
		root.serialize(writer);
	}

	@Override
	public void update(ValueReference propName, String s) {
		AXIOMXPath path;
		Object node;
		try {
			path = getAsXPath(propName);
			node = path.selectSingleNode(root);
		}
		catch (JaxenException e) {
			String msg = "Could not propName as xPath and locate in in the record: " + propName;
			LOG.debug(msg, e);
			throw new InvalidParameterException(msg);
		}
		if (node == null) {
			String msg = "Could not find node with xPath: " + path;
			LOG.debug(msg);
			throw new InvalidParameterException(msg);
		}
		else if ((!(node instanceof OMElement))) {
			String msg = "Xpath + " + path + " does not adress a Node!";
			LOG.debug(msg);
			throw new InvalidParameterException(msg);
		}
		OMElement el = (OMElement) node;
		el.setText(s);
	}

	@Override
	public void update(ValueReference propName, OMElement newEl) {
		AXIOMXPath path;
		Object rootNode;
		try {
			path = getAsXPath(propName);
			rootNode = path.selectSingleNode(root);
		}
		catch (JaxenException e) {
			String msg = "Could not propName as xPath and locate in in the record: " + propName;
			LOG.debug(msg, e);
			throw new InvalidParameterException(msg);
		}
		if (rootNode == null) {
			String msg = "Could not find node with xPath: " + path;
			LOG.debug(msg);
			throw new InvalidParameterException(msg);
		}
		else if ((!(rootNode instanceof OMElement))) {
			String msg = "Xpath + " + path + " does not adress a Node!";
			LOG.debug(msg);
			throw new InvalidParameterException(msg);
		}

		OMElement rootEl = (OMElement) rootNode;
		OMNode prevSib = null;

		List<OMElement> toDetach = new ArrayList<OMElement>();

		// replace them
		Iterator<?> childs = rootEl.getChildrenWithName(newEl.getQName());
		while (childs.hasNext()) {
			Object next = childs.next();
			if (next instanceof OMElement) {
				prevSib = ((OMElement) next).getPreviousOMSibling();
				toDetach.add((OMElement) next);
			}
		}
		for (OMElement om : toDetach) {
			om.detach();
		}
		prevSib.insertSiblingAfter(newEl);
	}

	@Override
	public void removeNode(ValueReference propName) {
		AXIOMXPath path;
		Object rootNode;
		try {
			path = getAsXPath(propName);
			rootNode = path.selectSingleNode(root);
		}
		catch (JaxenException e) {
			String msg = "Could not propName as xPath and locate in in the record: " + propName;
			LOG.debug(msg, e);
			throw new InvalidParameterException(msg);
		}
		if (rootNode == null) {
			String msg = "Could not find node with xPath: " + path;
			LOG.debug(msg);
			throw new InvalidParameterException(msg);
		}
		else if ((!(rootNode instanceof OMElement))) {
			String msg = "Xpath + " + path + " does not adress a Node!";
			LOG.debug(msg);
			throw new InvalidParameterException(msg);
		}
		OMElement rootEl = (OMElement) rootNode;
		rootEl.detach();
	}

	private AXIOMXPath getAsXPath(ValueReference propName) throws JaxenException {
		AXIOMXPath path;
		XPath xPathFromCQP = ISOCQPMapping.getXPathFromCQP(propName.getAsQName(), getType());
		if (xPathFromCQP != null)
			path = new AXIOMXPath(xPathFromCQP.getXPath());
		else
			path = new AXIOMXPath(propName.getAsText());
		path.setNamespaceContext(ns);
		return path;
	}

	/**
	 * Returns whether the record is a service metadata record.
	 * <ul>
	 * <li>Data Metadata: /gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset'
	 * (or missing) or 'series' or 'application'</li>
	 * <li>Service Metadata:
	 * /gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='service'</li>
	 * </ul>
	 * @return <code>true</code>, if the record is a service metadata record,
	 * <code>false</code> otherwise (implies data metadata record)
	 */
	public boolean isServiceRecord() {
		XPath xpath = new XPath("gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue", ns);
		String scopeCode = getStringFromXPath(xpath);
		// TODO should this check be more fail safe?
		return scopeCode != null && scopeCode.equals("service");
	}

	@Override
	public String toString() {
		return getIdentifier();
	}

}