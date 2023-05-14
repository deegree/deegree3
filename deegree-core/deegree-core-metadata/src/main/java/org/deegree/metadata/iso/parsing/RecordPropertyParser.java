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
package org.deegree.metadata.iso.parsing;

import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDate;
import static org.deegree.protocol.csw.CSWConstants.SDS_NS;
import static org.deegree.protocol.csw.CSWConstants.SDS_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.SRV_NS;
import static org.deegree.protocol.csw.CSWConstants.SRV_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.iso.types.CRS;
import org.deegree.metadata.iso.types.Format;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;

/**
 * Parsing regarding to ISO and DC application profile. Here the input XML document is
 * parsed into its parts. So this is the entry point to generate a record that fits with
 * the backend. The queryable and returnable properties are disentangled. This is needed
 * to put them into the queryable property tables in the backend and makes them queryable.
 * In this context they are feasible to build the Dublin Core record which has nearly
 * planar elements with no nested areas.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public final class RecordPropertyParser extends XMLAdapter {

	private static final Logger LOG = getLogger(RecordPropertyParser.class);

	private static NamespaceBindings nsContext = new NamespaceBindings(XMLAdapter.nsContext);

	// TODO Check if the distinction between QueryableProperties and ReturnableProperties
	// makes sense.
	private QueryableProperties qp;

	private ReturnableProperties rp;

	static {
		nsContext.addNamespace(SRV_PREFIX, SRV_NS);
		nsContext.addNamespace(SDS_PREFIX, SDS_NS);
	}

	public RecordPropertyParser(OMElement element) {
		setRootElement(element);
	}

	/**
	 * Parses the recordelement that should be inserted into the backend. Every
	 * elementknot is put into an OMElement and its atomic representation:
	 * <p>
	 * e.g. the "fileIdentifier" is put into an OMElement identifier and its
	 * identification-String is put into the {@link QueryableProperties}.
	 * @param element the XML element that has to be parsed to be able to generate needed
	 * database properties
	 * @return {@link ParsedProfileElement}
	 * @throws IOException
	 */
	public ParsedProfileElement parse() {

		OMFactory factory = OMAbstractFactory.getOMFactory();

		qp = new QueryableProperties();

		rp = new ReturnableProperties();

		// for ( String error : ca.getMv().validate( rootElement ) ) {
		// throw new MetadataStoreException( "VALIDATION-ERROR: " + error );
		// }

		qp.setAnyText(getAnyText());

		String language = getNodeAsString(rootElement,
				new XPath("./gmd:language/gco:CharacterString | ./gmd:language/gmd:LanguageCode/@codeListValue",
						nsContext),
				null);

		// Locale locale = new Locale(
		// getNodeAsString(
		// rootElement,
		// new XPath(
		// "./gmd:language/gco:CharacterString |
		// ./gmd:language/gmd:LanguageCode/@codeListValue",
		// nsContextISOParsing ), null ) );

		qp.setLanguage(language);
		// LOG.debug( getElement( rootElement, new XPath( "./gmd:language",
		// nsContextISOParsing ) ).toString() );

		String parentId = getNodeAsString(rootElement,
				new XPath("./gmd:parentIdentifier/gco:CharacterString", nsContext), null);
		if (parentId != null) {
			parentId = parentId.trim();
		}
		qp.setParentIdentifier(parentId);

		/**
		 * if provided data is a dataset: type = dataset (default)
		 * <p>
		 * if provided data is a datasetCollection: type = series
		 * <p>
		 * if provided data is an application: type = application
		 * <p>
		 * if provided data is a service: type = service
		 */
		String type = getNodeAsString(rootElement,
				new XPath("./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue", nsContext), "dataset");
		qp.setType(type);

		String[] dateString = getNodesAsStrings(rootElement,
				new XPath("./gmd:dateStamp/gco:Date | ./gmd:dateStamp/gco:DateTime", nsContext));
		try {
			if (dateString != null) {
				for (String ds : dateString) {
					if (ds != null && ds.length() > 0) {
						qp.setModified(parseDate(ds.trim()));
						break;
					}
				}
			}
		}
		catch (Exception e) {
			String msg = Messages.getMessage("ERROR_PARSING", dateString[0], e.getMessage());
			LOG.debug(msg);
		}

		List<OMElement> crsElements = getElements(rootElement, new XPath(
				"./gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier",
				nsContext));

		List<CRS> crsList = new LinkedList<CRS>();
		for (OMElement crsElement : crsElements) {
			String nilReasonCRS = getNodeAsString(crsElement, new XPath("./gmd:code/@gco:nilReason", nsContext), null);

			String nilReasonAuth = getNodeAsString(crsElement, new XPath("./gmd:codeSpace/@gco:nilReason", nsContext),
					null);

			if (nilReasonCRS == null && nilReasonAuth == null) {
				try {
					String crs = getRequiredNodeAsString(crsElement,
							new XPath("./gmd:code/gco:CharacterString", nsContext));
					String crsAuthority = getNodeAsString(crsElement,
							new XPath("./gmd:codeSpace/gco:CharacterString", nsContext), null);
					String crsVersion = getNodeAsString(crsElement,
							new XPath("./gmd:version/gco:CharacterString", nsContext), null);
					crsList.add(new CRS(crs, crsAuthority, crsVersion));
				}
				catch (Exception e) {
					String msg = Messages.getMessage("ERROR_PARSING", "" + "", e.getMessage());
					LOG.error(msg);
				}
			}
		}
		qp.setCrs(crsList);

		List<OMElement> identificationInfo = getElements(rootElement, new XPath("./gmd:identificationInfo", nsContext));

		ParseIdentificationInfo pI = new ParseIdentificationInfo(factory, nsContext);
		pI.parseIdentificationInfo(identificationInfo, qp, rp);

		String fileIdentifierString = getNodeAsString(rootElement,
				new XPath("./gmd:fileIdentifier/gco:CharacterString", nsContext), null);
		qp.setIdentifier(fileIdentifierString);

		parseDistributionInfo();

		parseDataQualityInfo();

		return new ParsedProfileElement(qp, rp, getRootElement());
	}

	private String getAnyText() {
		try {
			StringBuilder sb = new StringBuilder();
			XMLStreamReader xmlStream = rootElement.getXMLStreamReader();
			while (xmlStream.hasNext()) {
				xmlStream.next();
				if (xmlStream.getEventType() == XMLStreamConstants.CHARACTERS && !xmlStream.isWhiteSpace()) {
					sb.append(xmlStream.getText()).append(" ");
				}
			}
			return sb.toString();
		}
		catch (XMLStreamException e) {
			LOG.warn("Could not read any text: {}", e.getMessage());
		}
		return null;
	}

	/**
	 * DistributionInfo
	 */
	private void parseDistributionInfo() {

		List<OMElement> formats = new ArrayList<OMElement>();

		formats.addAll(getElements(rootElement, new XPath(
				"./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat/gmd:MD_Format",
				nsContext)));

		formats.addAll(getElements(rootElement, new XPath(
				"./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format", nsContext)));

		// String onlineResource = getNodeAsString(
		// rootElement,
		// new XPath(
		// "./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
		// nsContextISOParsing ), null );

		List<Format> listOfFormats = new ArrayList<Format>();
		for (OMElement md_format : formats) {

			String formatName = getNodeAsString(md_format, new XPath("./gmd:name/gco:CharacterString", nsContext),
					null);

			String formatVersion = getNodeAsString(md_format, new XPath("./gmd:version/gco:CharacterString", nsContext),
					null);

			Format formatClass = new Format(formatName, formatVersion);
			listOfFormats.add(formatClass);

		}

		qp.setFormat(listOfFormats);

	}

	/**
	 * DataQualityInfo
	 * @throws MetadataStoreException
	 */
	private void parseDataQualityInfo() throws IllegalArgumentException {
		List<OMElement> lineageElems = getElements(rootElement, new XPath(
				"./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement", nsContext));
		List<String> lineages = new ArrayList<String>();
		for (OMElement lineageElem : lineageElems) {
			String[] lineageList = getNodesAsStrings(lineageElem, new XPath("./gco:CharacterString", nsContext));

			lineages.addAll(Arrays.asList(lineageList));
			lineageList = getNodesAsStrings(lineageElem,
					new XPath("./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString", nsContext));
			lineages.addAll(Arrays.asList(lineageList));
		}

		qp.setLineages(lineages);
		rp.setSources(lineages);

		qp.setDegree(getNodeAsBoolean(rootElement, new XPath(
				"./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:pass/gco:Boolean",
				nsContext), false));

		List<OMElement> titleElems = getElements(rootElement, new XPath(
				"./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title",
				nsContext));

		List<String> titleStringList = new ArrayList<String>();
		for (OMElement titleElem : titleElems) {
			String title = getNodeAsString(titleElem, new XPath("./gco:CharacterString", nsContext), null);
			String[] titleList = getNodesAsStrings(titleElem,
					new XPath("./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString", nsContext));
			if (title != null) {
				if (!title.equals("")) {
					titleStringList.add(title);
				}
			}
			if (titleList.length > 0) {
				titleStringList.addAll(Arrays.asList(titleList));
			}
		}
		if (!titleStringList.isEmpty()) {
			qp.setSpecificationTitle(titleStringList);
		}
		String specDateType = getNodeAsString(rootElement, new XPath(
				"./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue",
				nsContext), null);
		if (specDateType != null) {
			qp.setSpecificationDateType(specDateType);
		}
		String specificationDateString = getNodeAsString(rootElement, new XPath(
				"./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date",
				nsContext), null);
		Date dateSpecificationDate = null;

		try {
			if (specificationDateString != null && !"".equals(specificationDateString)) {
				dateSpecificationDate = parseDate(specificationDateString);
			}
		}
		catch (Exception e) {
			String msg = Messages.getMessage("ERROR_PARSING", specificationDateString, e.getMessage());
			LOG.debug(msg);
			throw new IllegalArgumentException(msg);

		}
		if (dateSpecificationDate != null) {
			qp.setSpecificationDate(dateSpecificationDate);
		}
	}

}
