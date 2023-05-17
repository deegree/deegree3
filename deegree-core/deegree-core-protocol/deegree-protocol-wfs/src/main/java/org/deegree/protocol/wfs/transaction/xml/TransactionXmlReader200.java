/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.protocol.wfs.transaction.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValue;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredAttributeValueAsQName;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.INSERT_AFTER;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.INSERT_BEFORE;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.REMOVE;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.REPLACE;

import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPathUtils;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.xml.Filter200XMLDecoder;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.transaction.ReleaseAction;
import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionAction;
import org.deegree.protocol.wfs.transaction.action.Delete;
import org.deegree.protocol.wfs.transaction.action.Insert;
import org.deegree.protocol.wfs.transaction.action.Native;
import org.deegree.protocol.wfs.transaction.action.PropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.Replace;
import org.deegree.protocol.wfs.transaction.action.Update;
import org.deegree.protocol.wfs.transaction.action.UpdateAction;

/**
 * {@link TransactionXmlReader} for WFS 2.0.0.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
class TransactionXmlReader200 extends AbstractTransactionXmlReader {

	@Override
	public Transaction read(XMLStreamReader xmlStream) throws XMLStreamException {

		xmlStream.require(START_ELEMENT, WFS_200_NS, "Transaction");

		// <xsd:attribute name="handle" type="xsd:string"/>
		String handle = getAttributeValue(xmlStream, "handle");

		// <xsd:attribute name="lockId" type="xsd:string"/>
		String lockId = getAttributeValue(xmlStream, "lockId");

		// <xsd:attribute name="releaseAction" type="wfs:AllSomeType" default="ALL"/>
		String releaseActionString = getAttributeValue(xmlStream, "releaseAction");
		ReleaseAction releaseAction = parseReleaseAction(releaseActionString);

		// <xsd:attribute name="srsName" type="xsd:anyURI"/>
		String srsName = getAttributeValue(xmlStream, "srsName");

		nextElement(xmlStream);
		LazyTransactionActionsReader iterable = new LazyTransactionActionsReader(xmlStream, this);
		return new Transaction(VERSION_200, handle, lockId, releaseAction, iterable, srsName);
	}

	@Override
	public TransactionAction readAction(XMLStreamReader xmlStream) throws XMLStreamException, XMLParsingException {
		if (!WFS_200_NS.equals(xmlStream.getNamespaceURI())) {
			String msg = "Unexpected element: " + xmlStream.getName()
					+ "' is not a WFS 2.0.0 transaction action element. Not in the WFS 2.0.0 namespace.";
			throw new XMLParsingException(xmlStream, msg);
		}

		TransactionAction operation = null;
		String localName = xmlStream.getLocalName();
		if ("Delete".equals(localName)) {
			operation = readDelete(xmlStream);
		}
		else if ("Insert".equals(localName)) {
			operation = readInsert(xmlStream);
		}
		else if ("Native".equals(localName)) {
			operation = readNative(xmlStream);
		}
		else if ("Replace".equals(localName)) {
			operation = readReplace(xmlStream);
		}
		else if ("Update".equals(localName)) {
			operation = readUpdate(xmlStream);
		}
		else {
			throw new XMLParsingException(xmlStream, "Unexpected operation element " + localName + ".");
		}
		return operation;
	}

	/**
	 * Returns the object representation for the given <code>wfs:Delete</code> element.
	 * Consumes all events corresponding to the <code>wfs:Delete</code> element from the
	 * given <code>XMLStream</code>.
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wfs:Delete&gt;), points at the corresponding <code>END_ELEMENT</code> event
	 * (&lt;/wfs:Delete&gt;) afterwards
	 * @return corresponding {@link Delete} object, never <code>null</code>
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 */
	Delete readDelete(XMLStreamReader xmlStream) throws XMLStreamException {

		// <xsd:attribute name="handle" type="xsd:string"/>
		String handle = xmlStream.getAttributeValue(null, "handle");

		// <xsd:attribute name="typeName" type="xsd:QName" use="required"/>
		QName typeName = getRequiredAttributeValueAsQName(xmlStream, null, "typeName");

		// required: 'fes:Filter'
		nextElement(xmlStream);
		try {
			xmlStream.require(START_ELEMENT, FES_20_NS, "Filter");
		}
		catch (XMLStreamException e) {
			throw new MissingParameterException("Mandatory 'fes:Filter' element is missing in Delete.");
		}
		Filter filter = readFilter(xmlStream);
		nextElement(xmlStream);
		xmlStream.require(END_ELEMENT, WFS_200_NS, "Delete");
		return new Delete(handle, typeName, filter);
	}

	/**
	 * Returns the object representation for the given <code>wfs:Insert</code> element.
	 * <p>
	 * NOTE: In order to allow stream-oriented processing, this method does *not* consume
	 * all events corresponding to the <code>wfs:Insert</code> elemetn from the given
	 * <code>XMLStream</code>. After a call to this method, the XML stream points at the
	 * <code>START_ELEMENT</code> of the insert payload.
	 * </p>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wfs:Insert&gt;)
	 * @return corresponding {@link Insert} object, never <code>null</code>
	 * @throws NoSuchElementException
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 */
	Insert readInsert(XMLStreamReader xmlStream) throws NoSuchElementException, XMLStreamException {

		// <xsd:attribute name="handle" type="xsd:string"/>
		String handle = xmlStream.getAttributeValue(null, "handle");

		// <xsd:attribute name="inputFormat" type="xsd:string"
		// default="application/gml+xml; version=3.2"/>
		String inputFormat = xmlStream.getAttributeValue(null, "inputFormat");

		// <xsd:attribute name="srsName" type="xsd:anyURI"/>
		String srsName = xmlStream.getAttributeValue(null, "srsName");

		nextElement(xmlStream);
		if (!xmlStream.isStartElement()) {
			throw new XMLParsingException(xmlStream, Messages.get("WFS_INSERT_MISSING_FEATURE_ELEMENT"));
		}

		return new Insert(handle, null, inputFormat, srsName, xmlStream);
	}

	/**
	 * Returns the object representation for the given <code>wfs:Native</code> element.
	 * <p>
	 * NOTE: In order to allow stream-oriented processing, this method does *not* consume
	 * all events corresponding to the <code>wfs:Native</code> element from the given
	 * <code>XMLStream</code>. After a call to this method, the XML stream still points at
	 * the <code>START_ELEMENT</code> of the <code>wfs:Native</code> element.
	 * </p>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wfs:Native&gt;)
	 * @return corresponding {@link Native} object, never <code>null</code>
	 * @throws NoSuchElementException
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 */
	Native readNative(XMLStreamReader xmlStream) {

		// <xsd:attribute name="handle" type="xsd:string"/>
		String handle = xmlStream.getAttributeValue(null, "handle");

		// <xsd:attribute name="vendorId" type="xsd:string" use="required"/>
		String vendorId = XMLStreamUtils.getRequiredAttributeValue(xmlStream, "vendorId");

		// <xsd:attribute name="safeToIgnore" type="xsd:boolean" use="required"/>
		boolean safeToIgnore = XMLStreamUtils.getRequiredAttributeValueAsBoolean(xmlStream, null, "safeToIgnore");

		return new Native(handle, vendorId, safeToIgnore, xmlStream);
	}

	/**
	 * Returns the object representation for the given <code>wfs:Replace</code> element.
	 * <p>
	 * NOTE: In order to allow stream-oriented processing, this method does *not* consume
	 * all events corresponding to the <code>wfs:Replace</code> element from the given
	 * <code>XMLStream</code>. After a call to this method, the XML stream points at the
	 * <code>START_ELEMENT</code> of the replacement feature. The replacement feature is
	 * followed by a <code>fes:Filter</code> element.
	 * </p>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wfs:Replace&gt;)
	 * @return corresponding {@link Replace} object, never <code>null</code>
	 * @throws NoSuchElementException
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 */
	Replace readReplace(XMLStreamReader xmlStream) throws NoSuchElementException, XMLStreamException {

		// <xsd:attribute name="handle" type="xsd:string"/>
		String handle = xmlStream.getAttributeValue(null, "handle");

		nextElement(xmlStream);
		if (!xmlStream.isStartElement()) {
			throw new XMLParsingException(xmlStream, Messages.get("WFS_REPLACE_MISSING_FEATURE_ELEMENT"));
		}

		return new Replace(handle, xmlStream);
	}

	/**
	 * Returns the object representation for the given <code>wfs:Update</code> element.
	 * <p>
	 * NOTE: In order to allow stream-oriented processing, this method does *not* consume
	 * all events corresponding to the <code>wfs:Update</code> element from the given
	 * <code>XMLStream</code>. After a call to this method, the XML stream points at the
	 * <code>START_ELEMENT</code> event of the first <code>wfs:Property</code> element.
	 * </p>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event
	 * (&lt;wfs:Update&gt;)
	 * @return corresponding {@link Update} object, never <code>null</code>
	 * @throws NoSuchElementException
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 */
	Update readUpdate(XMLStreamReader xmlStream) throws NoSuchElementException, XMLStreamException {

		// <xsd:attribute name="handle" type="xsd:string"/>
		String handle = xmlStream.getAttributeValue(null, "handle");

		// <xsd:attribute name="inputFormat" type="xsd:string"
		// default="application/gml+xml; version=3.2"/>
		String inputFormat = xmlStream.getAttributeValue(null, "inputFormat");

		// <xsd:attribute name="srsName" type="xsd:anyURI"/>
		String srsName = xmlStream.getAttributeValue(null, "srsName");

		// <xsd:attribute name="typeName" type="xsd:QName" use="required"/>
		QName typeName = getRequiredAttributeValueAsQName(xmlStream, null, "typeName");

		// skip to first "wfs:Property" element
		nextElement(xmlStream);
		xmlStream.require(START_ELEMENT, WFS_200_NS, "Property");

		return new Update(handle, null, typeName, inputFormat, srsName, xmlStream, this);
	}

	@Override
	public Filter readFilter(XMLStreamReader xmlStream) throws XMLParsingException, XMLStreamException {
		return Filter200XMLDecoder.parse(xmlStream);
	}

	@Override
	public PropertyReplacement readProperty(XMLStreamReader xmlStream) throws XMLStreamException {

		xmlStream.require(START_ELEMENT, WFS_200_NS, "Property");
		nextElement(xmlStream);
		xmlStream.require(START_ELEMENT, WFS_200_NS, "ValueReference");
		UpdateAction updateAction = parseUpdateAction(xmlStream.getAttributeValue(null, "action"));

		String propName = xmlStream.getElementText();
		Set<String> prefixes = XPathUtils.extractPrefixes(propName);
		ValueReference propertyName = new ValueReference(propName,
				new NamespaceBindings(xmlStream.getNamespaceContext(), prefixes));

		nextElement(xmlStream);

		PropertyReplacement replacement = null;
		if (new QName(WFS_200_NS, "Value").equals(xmlStream.getName())) {
			replacement = new PropertyReplacement(propertyName, xmlStream, updateAction);
		}
		else {
			xmlStream.require(END_ELEMENT, WFS_200_NS, "Property");
			replacement = new PropertyReplacement(propertyName, null, updateAction);
			nextElement(xmlStream);
		}
		return replacement;
	}

	private UpdateAction parseUpdateAction(String action) {
		if ("replace".equals(action)) {
			return REPLACE;
		}
		else if ("insertBefore".equals(action)) {
			return INSERT_BEFORE;
		}
		else if ("insertAfter".equals(action)) {
			return INSERT_AFTER;
		}
		else if ("remove".equals(action)) {
			return REMOVE;
		}
		return null;
	}

}
