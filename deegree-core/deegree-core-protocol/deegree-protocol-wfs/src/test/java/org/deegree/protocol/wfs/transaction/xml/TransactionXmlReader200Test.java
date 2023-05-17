/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2012 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.protocol.wfs.transaction.xml;

import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.filter.Filter;
import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionAction;
import org.deegree.protocol.wfs.transaction.action.Delete;
import org.deegree.protocol.wfs.transaction.action.Insert;
import org.deegree.protocol.wfs.transaction.action.Native;
import org.deegree.protocol.wfs.transaction.action.PropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.Replace;
import org.deegree.protocol.wfs.transaction.action.Update;
import org.deegree.protocol.wfs.transaction.action.UpdateAction;
import org.junit.Test;

/**
 * Test cases for {@link TransactionXmlReader200}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class TransactionXmlReader200Test extends TestCase {

	private final String DELETE_ACTION1 = "wfs200/delete1.xml";

	private final String DELETE_ACTION2 = "wfs200/delete2.xml";

	private final String DELETE_ACTION3 = "wfs200/delete3.xml";

	private final String INSERT_ACTION1 = "wfs200/insert1.dontvalidate";

	private final String NATIVE_ACTION1 = "wfs200/native1.xml";

	private final String REPLACE_ACTION = "wfs200/replace1.dontvalidate";

	private final String UPDATE_ACTION1 = "wfs200/update1.xml";

	private final String UPDATE_ACTION2 = "wfs200/update2.xml";

	private final String UPDATE_ACTION3 = "wfs200/update3.xml";

	private final String MIXED_200 = "wfs200/transaction1.dontvalidate";

	private final TransactionXmlReader200 reader = new TransactionXmlReader200();

	@Test
	public void testReadDeleteWfs200SpecExample1() throws Exception {
		XMLStreamReader xmlStream = getXMLStreamReader(DELETE_ACTION1);
		Delete delete = reader.readDelete(xmlStream);
		xmlStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Delete");
		assertNull(delete.getHandle());
		assertEquals(new QName("InWaterA_1M"), delete.getTypeName());
		assertNotNull(delete.getFilter());
	}

	@Test
	public void testReadDeleteWfs200SpecExample2() throws Exception {
		XMLStreamReader xmlStream = getXMLStreamReader(DELETE_ACTION2);
		Delete delete = reader.readDelete(xmlStream);
		xmlStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Delete");
		assertNull(delete.getHandle());
		QName expectedTypeName = new QName("http://www.someserver.com/myns", "InWaterA_1M");
		assertEquals(expectedTypeName, delete.getTypeName());
		assertNotNull(delete.getFilter());
	}

	@Test
	public void testReadDeleteWfs200SpecExample3() throws Exception {
		XMLStreamReader xmlStream = getXMLStreamReader(DELETE_ACTION3);
		Delete delete = reader.readDelete(xmlStream);
		xmlStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Delete");
		assertNull(delete.getHandle());
		QName expectedTypeName = new QName("InWaterA_1M");
		assertEquals(expectedTypeName, delete.getTypeName());
		assertNotNull(delete.getFilter());
	}

	@Test
	public void testReadInsertWfs200SpecExample1() throws Exception {
		XMLStreamReader xmlStream = getXMLStreamReader(INSERT_ACTION1);
		Insert insert = reader.readInsert(xmlStream);
		assertNull(insert.getHandle());
		assertNull(insert.getInputFormat());
		assertNull(insert.getSrsName());
		xmlStream.require(XMLStreamReader.START_ELEMENT, null, "InWaterA_1M");
		assertNull(insert.getSrsName());
		assertEquals(xmlStream, insert.getFeatures());
		skipElement(xmlStream);
		nextElement(xmlStream);
		skipElement(xmlStream);
		nextElement(xmlStream);
		xmlStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Insert");
	}

	@Test
	public void testReadNativeWfs200SpecExample1() throws Exception {
		XMLStreamReader xmlStream = getXMLStreamReader(NATIVE_ACTION1);
		Native action = reader.readNative(xmlStream);
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Native");
		assertNull(action.getHandle());
		assertEquals("BigDbCorp", action.getVendorId());
		assertTrue(action.isSafeToIgnore());
	}

	@Test
	public void testReadReplaceWfs200SpecExample1() throws Exception {
		XMLStreamReader xmlStream = getXMLStreamReader(REPLACE_ACTION);
		Replace action = reader.readReplace(xmlStream);
		xmlStream.require(XMLStreamReader.START_ELEMENT, null, "BuiltUpA_1M");
		assertNull(action.getHandle());
		XMLStreamReader featureReader = action.getReplacementFeatureStream();
		skipElement(featureReader);
		Filter filter = action.getFilter();
		assertNotNull(filter);
		xmlStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Replace");
	}

	@Test
	public void testReadUpdateWfs200SpecExample1() throws Exception {
		XMLStreamReader xmlStream = getXMLStreamReader(UPDATE_ACTION1);
		Update action = reader.readUpdate(xmlStream);
		assertNull(action.getHandle());
		assertNull(action.getInputFormat());
		assertNull(action.getSRSName());
		Iterator<PropertyReplacement> replacementProps = action.getReplacementProps();

		PropertyReplacement replacement = replacementProps.next();
		assertNull(replacement.getUpdateAction());
		XMLStreamReader valueStream = replacement.getReplacementValue();
		valueStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Value");
		assertEquals(new QName("populationType"), replacement.getPropertyName().getAsQName());
		assertEquals("CITY", valueStream.getElementText());
		nextElement(valueStream);
		valueStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Property");

		assertFalse(replacementProps.hasNext());

		nextElement(xmlStream);
		Filter filter = action.getFilter();
		assertNotNull(filter);

		xmlStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Update");
	}

	@Test
	public void testReadUpdateWfs200SpecExample2() throws Exception {
		XMLStreamReader xmlStream = getXMLStreamReader(UPDATE_ACTION2);
		Update action = reader.readUpdate(xmlStream);
		assertNull(action.getHandle());
		assertNull(action.getInputFormat());
		assertNull(action.getSRSName());
		Iterator<PropertyReplacement> replacementProps = action.getReplacementProps();

		PropertyReplacement replacement = replacementProps.next();
		assertNull(replacement.getUpdateAction());
		assertEquals(new QName("http://www.someserver.com/myns", "name"), replacement.getPropertyName().getAsQName());
		XMLStreamReader valueStream = replacement.getReplacementValue();
		valueStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Value");
		assertEquals("somestring", valueStream.getElementText());
		nextElement(valueStream);
		valueStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Property");

		assertFalse(replacementProps.hasNext());

		nextElement(xmlStream);
		Filter filter = action.getFilter();
		assertNotNull(filter);

		xmlStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Update");
	}

	@Test
	public void testReadUpdateWfs200AllOptionalAttributes() throws Exception {
		XMLStreamReader xmlStream = getXMLStreamReader(UPDATE_ACTION3);
		Update action = reader.readUpdate(xmlStream);
		assertEquals("BLA", action.getHandle());
		assertEquals("application/gml+xml; version=3.2", action.getInputFormat());
		assertEquals("EPSG:4326", action.getSRSName());
		Iterator<PropertyReplacement> replacementProps = action.getReplacementProps();

		PropertyReplacement replacement = replacementProps.next();
		assertEquals(UpdateAction.REPLACE, replacement.getUpdateAction());
		assertEquals(new QName("http://www.someserver.com/myns", "treeType"),
				replacement.getPropertyName().getAsQName());
		XMLStreamReader valueStream = replacement.getReplacementValue();
		valueStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Value");
		assertEquals("CONIFEROUS", valueStream.getElementText());
		nextElement(valueStream);
		valueStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Property");
		nextElement(valueStream);

		assertTrue(replacementProps.hasNext());
		replacement = replacementProps.next();
		assertEquals(UpdateAction.INSERT_AFTER, replacement.getUpdateAction());
		assertEquals(new QName("http://www.someserver.com/myns", "treeType2"),
				replacement.getPropertyName().getAsQName());
		valueStream = replacement.getReplacementValue();
		valueStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Value");
		assertEquals("CONIFEROUS", valueStream.getElementText());
		nextElement(valueStream);
		valueStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Property");
		nextElement(valueStream);

		assertTrue(replacementProps.hasNext());
		replacement = replacementProps.next();
		assertEquals(UpdateAction.INSERT_BEFORE, replacement.getUpdateAction());
		assertEquals(new QName("http://www.someserver.com/myns", "treeType3"),
				replacement.getPropertyName().getAsQName());
		valueStream = replacement.getReplacementValue();
		valueStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Value");
		assertEquals("CONIFEROUS", valueStream.getElementText());
		nextElement(valueStream);
		valueStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Property");
		nextElement(valueStream);

		assertTrue(replacementProps.hasNext());
		replacement = replacementProps.next();
		assertEquals(UpdateAction.REMOVE, replacement.getUpdateAction());
		assertEquals(new QName("http://www.someserver.com/myns", "treeType3"),
				replacement.getPropertyName().getAsQName());
		valueStream = replacement.getReplacementValue();
		valueStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Value");
		assertEquals("CONIFEROUS", valueStream.getElementText());
		nextElement(valueStream);
		valueStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Property");
		nextElement(valueStream);

		assertFalse(replacementProps.hasNext());

		Filter filter = action.getFilter();
		assertNotNull(filter);

		xmlStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Update");
	}

	@Test
	public void testReadUpdateWfs200MixedTransaction() throws Exception {

		XMLStreamReader xmlStream = getXMLStreamReader(MIXED_200);
		Transaction ta = reader.read(xmlStream);
		Iterator<TransactionAction> taIter = ta.getActions().iterator();

		// 1. action: Native
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Native");
		Native nativeAction = (Native) taIter.next();
		skipElement(nativeAction.getVendorSpecificData());
		xmlStream.require(XMLStreamReader.END_ELEMENT, WFS_200_NS, "Native");

		// 2. action: Insert
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Insert");
		Insert insert = (Insert) taIter.next();
		XMLStreamReader features = insert.getFeatures();
		features.require(XMLStreamReader.START_ELEMENT, "http://www.someserver.com/myns", "ElevP_1M");
		skipElement(features);
		nextElement(features);

		// 3. action: Insert
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Insert");
		insert = (Insert) taIter.next();
		features = insert.getFeatures();
		features.require(XMLStreamReader.START_ELEMENT, "http://www.someserver.com/myns", "RoadL_1M");
		skipElement(features);
		nextElement(features);

		// 4. action: Update
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Update");
		Update update = (Update) taIter.next();
		Iterator<PropertyReplacement> propIter = update.getReplacementProps();
		assertTrue(propIter.hasNext());
		PropertyReplacement property = propIter.next();
		XMLStreamReader replacementValue = property.getReplacementValue();
		skipElement(replacementValue);
		nextElement(replacementValue);
		nextElement(replacementValue);
		assertFalse(propIter.hasNext());
		assertNotNull(update.getFilter());

		// 5. action: Insert
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Insert");
		insert = (Insert) taIter.next();
		features = insert.getFeatures();
		features.require(XMLStreamReader.START_ELEMENT, "http://www.someserver.com/myns", "BuiltUpA_1M");
		skipElement(features);
		nextElement(features);
		skipElement(features);
		nextElement(features);

		// 6. action: Update
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Update");
		update = (Update) taIter.next();
		propIter = update.getReplacementProps();
		assertTrue(propIter.hasNext());
		property = propIter.next();
		replacementValue = property.getReplacementValue();
		skipElement(replacementValue);
		nextElement(replacementValue);
		nextElement(replacementValue);
		assertFalse(propIter.hasNext());
		assertNotNull(update.getFilter());

		// 7. action: Update
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Update");
		update = (Update) taIter.next();
		propIter = update.getReplacementProps();
		assertTrue(propIter.hasNext());
		property = propIter.next();
		replacementValue = property.getReplacementValue();
		skipElement(replacementValue);
		nextElement(replacementValue);
		nextElement(replacementValue);
		assertFalse(propIter.hasNext());
		assertNotNull(update.getFilter());

		// 8. action: Delete
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Delete");
		Delete delete = (Delete) taIter.next();
		assertNotNull(delete);

		// 9. action: Delete
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Delete");
		delete = (Delete) taIter.next();
		assertNotNull(delete);

		// 10. action: Replace
		assertTrue(taIter.hasNext());
		xmlStream.require(XMLStreamReader.START_ELEMENT, WFS_200_NS, "Replace");
		Replace replace = (Replace) taIter.next();
		skipElement(replace.getReplacementFeatureStream());
		assertNotNull(replace.getFilter());

		assertFalse(taIter.hasNext());
	}

	private XMLStreamReader getXMLStreamReader(String resourceName) throws XMLStreamException, IOException {
		URL exampleURL = this.getClass().getResource(resourceName);
		XMLInputFactory inputFac = XMLInputFactory.newInstance();
		XMLStreamReader xmlStream = inputFac.createXMLStreamReader(exampleURL.toString(), exampleURL.openStream());
		XMLStreamUtils.skipStartDocument(xmlStream);
		return xmlStream;
	}

}
