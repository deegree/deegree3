/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.metadata.ebrim;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.slf4j.LoggerFactory.getLogger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.metadata.DCRecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.slf4j.Logger;

/**
 * Base type for all ebRIM {@link MetadataRecord}s.
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public class RegistryObject implements MetadataRecord {

	private static final Logger LOG = getLogger(RegistryObject.class);

	protected static final NamespaceBindings ns = CommonNamespaces.getNamespaceContext();

	public static final String RIM_NS = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";

	protected XMLAdapter adapter;

	static {
		ns.addNamespace("rim", RIM_NS);
		ns.addNamespace("wrs", "http://www.opengis.net/cat/wrs/1.0");
	}

	public RegistryObject(OMElement record) {
		this.adapter = new XMLAdapter(record);
	}

	public RegistryObject(XMLStreamReader xmlStream) {
		this.adapter = new XMLAdapter(xmlStream);
	}

	/**
	 * Returns the {@link RIMType}.
	 * @return {@link RIMType}, never <code>null</code>
	 */
	public RIMType getRIMType() {
		return RIMType.valueOf(adapter.getRootElement().getLocalName());
	}

	@Override
	public QName getName() {
		return null;
	}

	@Override
	public String getIdentifier() {
		return getId();
	}

	@Override
	public String[] getTitle() {
		return new String[] { getROName() };
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getRelation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getModified() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAbstract() {
		return new String[] { getDesc() };
	}

	@Override
	public Object[] getSpatial() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSubject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getRights() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPublisher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContributor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Envelope[] getBoundingBox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OMElement getAsOMElement() {
		return adapter.getRootElement();
	}

	@Override
	public DCRecord toDublinCore() {
		throw new UnsupportedOperationException("Conversion to DublinCore records is not implemented yet.");
	}

	@Override
	public boolean eval(Filter filter) throws FilterEvaluationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void serialize(XMLStreamWriter writer, ReturnableElement returnType) throws XMLStreamException {

		switch (returnType) {
			case brief:
				serializeBrief(writer);
				break;
			case summary:
				serializeSummary(writer);
				break;
			case full:
				XMLStreamReader xmlStream = adapter.getRootElement().getXMLStreamReader();
				XMLStreamUtils.skipStartDocument(xmlStream);
				XMLAdapter.writeElement(writer, xmlStream);
				break;
			default:
				throw new IllegalArgumentException("Unexpected return type '" + returnType + "'.");
		}
	}

	/**
	 * Writes out a brief representation of this {@link RegistryObject} to OGC 07-110r4,
	 * section 7.5.
	 * @param writer writer to write to, must not be <code>null</code>
	 * @throws XMLStreamException
	 */
	private void serializeBrief(XMLStreamWriter writer) throws XMLStreamException {

		XMLStreamReader inStream = adapter.getRootElement().getXMLStreamReader();
		XMLStreamUtils.skipStartDocument(inStream);
		if (inStream.getEventType() != XMLStreamConstants.START_ELEMENT) {
			throw new XMLStreamException("Input stream does not point to a START_ELEMENT event.");
		}

		if (inStream.getNamespaceURI() == ""
				&& (inStream.getPrefix() == DEFAULT_NS_PREFIX || inStream.getPrefix() == null)) {
			writer.writeStartElement(inStream.getLocalName());
		}
		else {
			if (inStream.getPrefix() != null && writer.getNamespaceContext().getPrefix(inStream.getPrefix()) == "") {
				// TODO handle special cases for prefix binding, see
				// http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html#getNamespaceURI(java.lang.String)
				writer.setPrefix(inStream.getPrefix(), inStream.getNamespaceURI());
			}
			writer.writeStartElement(inStream.getPrefix(), inStream.getLocalName(), inStream.getNamespaceURI());
		}

		// copy RIM namespace binding
		for (int i = 0; i < inStream.getNamespaceCount(); i++) {
			String nsPrefix = inStream.getNamespacePrefix(i);
			String nsURI = inStream.getNamespaceURI(i);
			if (RIM_NS.equals(nsURI)) {
				writer.writeNamespace(nsPrefix, nsURI);
			}
		}

		// copy attributes required for brief representation
		for (int i = 0; i < inStream.getAttributeCount(); i++) {
			String localName = inStream.getAttributeLocalName(i);
			String value = inStream.getAttributeValue(i);
			String nsURI = inStream.getAttributeNamespace(i);
			if (nsURI == null) {
				if ("id".equals(localName) || "lid".equals(localName) || "objectType".equals(localName)
						|| "status".equals(localName))
					writer.writeAttribute(localName, value);
			}
		}

		while (inStream.next() != END_ELEMENT) {
			if (inStream.isStartElement()) {
				QName elName = inStream.getName();
				if (RIM_NS.equals(elName.getNamespaceURI()) && "VersionInfo".equals(elName.getLocalPart())) {
					XMLAdapter.writeElement(writer, inStream);
				}
				else {
					XMLStreamUtils.skipElement(inStream);
				}
			}
		}

		writer.writeEndElement();
	}

	/**
	 * Writes out a summary representation of this {@link RegistryObject} to OGC 07-110r4,
	 * section 7.5.
	 * @param writer writer to write to, must not be <code>null</code>
	 * @throws XMLStreamException
	 */
	private void serializeSummary(XMLStreamWriter writer) throws XMLStreamException {

		XMLStreamReader inStream = adapter.getRootElement().getXMLStreamReader();
		XMLStreamUtils.skipStartDocument(inStream);
		if (inStream.getEventType() != XMLStreamConstants.START_ELEMENT) {
			throw new XMLStreamException("Input stream does not point to a START_ELEMENT event.");
		}

		if (inStream.getNamespaceURI() == ""
				&& (inStream.getPrefix() == DEFAULT_NS_PREFIX || inStream.getPrefix() == null)) {
			writer.writeStartElement(inStream.getLocalName());
		}
		else {
			if (inStream.getPrefix() != null && writer.getNamespaceContext().getPrefix(inStream.getPrefix()) == "") {
				// TODO handle special cases for prefix binding, see
				// http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html#getNamespaceURI(java.lang.String)
				writer.setPrefix(inStream.getPrefix(), inStream.getNamespaceURI());
			}
			writer.writeStartElement(inStream.getPrefix(), inStream.getLocalName(), inStream.getNamespaceURI());
		}

		// copy RIM namespace binding
		for (int i = 0; i < inStream.getNamespaceCount(); i++) {
			String nsPrefix = inStream.getNamespacePrefix(i);
			String nsURI = inStream.getNamespaceURI(i);
			if (RIM_NS.equals(nsURI)) {
				writer.writeNamespace(nsPrefix, nsURI);
			}
		}

		// copy attributes required for brief representation
		for (int i = 0; i < inStream.getAttributeCount(); i++) {
			String localName = inStream.getAttributeLocalName(i);
			String value = inStream.getAttributeValue(i);
			String nsURI = inStream.getAttributeNamespace(i);
			if (nsURI == null) {
				if ("id".equals(localName) || "lid".equals(localName) || "objectType".equals(localName)
						|| "status".equals(localName))
					writer.writeAttribute(localName, value);
			}
		}

		while (inStream.next() != END_ELEMENT) {
			if (inStream.isStartElement()) {
				QName elName = inStream.getName();
				if (RIM_NS.equals(elName.getNamespaceURI())) {
					if ("VersionInfo".equals(elName.getLocalPart()) || "Slot".equals(elName.getLocalPart())
							|| "Name".equals(elName.getLocalPart()) || "Description".equals(elName.getLocalPart())) {
						XMLAdapter.writeElement(writer, inStream);
					}
				}
				else {
					XMLStreamUtils.skipElement(inStream);
				}
			}
		}

		writer.writeEndElement();
	}

	@Override
	public void serialize(XMLStreamWriter writer, String[] elementNames) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(ValueReference propName, String replaceValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(ValueReference propName, OMElement replaceValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeNode(ValueReference propName) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return adapter.getRequiredNodeAsString(adapter.getRootElement(), new XPath("./@id", ns));
	}

	/**
	 * @return the name
	 */
	public String getROName() {
		return adapter.getNodeAsString(adapter.getRootElement(), new XPath("./rim:Name/rim:LocalizedString/@value", ns),
				null);
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return adapter.getNodeAsString(adapter.getRootElement(),
				new XPath("./rim:Description/rim:LocalizedString/@value", ns), null);
	}

	/**
	 * @return the extId
	 */
	public String getExtId() {
		return adapter.getNodeAsString(adapter.getRootElement(),
				new XPath("./rim:ExternalIdentifier/rim:Name/rim:LocalizedString/@value", ns), null);
	}

	/**
	 * @return the home
	 */
	public String getHome() {
		return adapter.getNodeAsString(adapter.getRootElement(), new XPath("./@home", ns), null);
	}

	/**
	 * @return the lid
	 */
	public String getLid() {
		return adapter.getNodeAsString(adapter.getRootElement(), new XPath("./@lid", ns), null);
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return adapter.getNodeAsString(adapter.getRootElement(), new XPath("./@status", ns), null);
	}

	/**
	 * @return the versionInfo
	 */
	public String getVersionInfo() {
		return adapter.getNodeAsString(adapter.getRootElement(), new XPath("./rim:versionInfo/@versionName", ns), null);
	}

	/**
	 * @return the objectType
	 */
	public String getObjectType() {
		return adapter.getNodeAsString(adapter.getRootElement(), new XPath("./@objectType", ns), null);
	}

	public Geometry getGeometrySlotValue(String slotName) {
		OMElement geomElem = adapter.getElement(adapter.getRootElement(),
				new XPath("./rim:Slot[@name='" + slotName + "']/wrs:ValueList/wrs:AnyValue[1]/*", ns));
		if (geomElem != null) {
			try {
				GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31,
						geomElem.getXMLStreamReader());
				return gmlReader.readGeometry();
			}
			catch (Exception e) {
				String msg = "Could not parse geometry " + geomElem;
				LOG.debug(msg, e);
				e.printStackTrace();
				throw new IllegalArgumentException(msg);
			}
		}
		return null;
	}

	public String[] getSlotValueList(String slotName) {
		return adapter.getNodesAsStrings(adapter.getRootElement(),
				new XPath("./rim:Slot[@name='" + slotName + "']/rim:ValueList/rim:Value", ns));
	}

	public String getSlotValue(String slotName) {
		return adapter.getNodeAsString(adapter.getRootElement(),
				new XPath("./rim:Slot[@name='" + slotName + "']/rim:ValueList/rim:Value[1]", ns), null);
	}

	public String[] getSlotNames() {
		return adapter.getNodesAsStrings(adapter.getRootElement(), new XPath("./rim:Slot/@name", ns));
	}

	/**
	 * @return the encapsulated XML
	 */
	public OMElement getElement() {
		return adapter.getRootElement();
	}

}
