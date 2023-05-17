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
package org.deegree.metadata.iso.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.test.TestProperties;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.iso.persistence.parsing.ParseISOTest;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.transaction.DeleteOperation;
import org.deegree.metadata.persistence.transaction.MetadataProperty;
import org.deegree.metadata.persistence.transaction.UpdateOperation;
import org.deegree.protocol.csw.MetadataStoreException;
import org.jaxen.JaxenException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class ISOMetadatStoreTransactionTest extends AbstractISOTest {

	private static final Logger LOG = getLogger(ISOMetadatStoreTransactionTest.class);

	@Test
	public void testInsert() throws MetadataStoreException, FactoryConfigurationError, IOException,
			MetadataInspectorException, ResourceInitException, URISyntaxException {
		LOG.info("START Test: testInsert");

		initStore(TstConstants.configURL);
		Assume.assumeNotNull(store);

		String test_folder = TestProperties.getProperty("test_folder");

		Assume.assumeTrue(test_folder != null);

		File folder = new File(test_folder);
		File[] fileArray = folder.listFiles();

		if (fileArray == null) {
			LOG.error("test folder does not exist: " + test_folder);
			return;
		}

		URL[] urlArray = new URL[fileArray.length];
		int counter = 0;
		for (File f : fileArray) {
			urlArray[counter++] = new URL("file:" + f.getAbsolutePath());
		}

		TstUtils.insertMetadata(store, urlArray);

		MetadataQuery query = new MetadataQuery(null, null, null, null, 1, 10);
		resultSet = store.getRecords(query);
		int size = 0;
		while (resultSet.next()) {
			size++;
		}
	}

	/**
	 * Tests if 3 records will be inserted and 2 delete so the output should be 1 <br>
	 * The request-query tests after getAllRecords
	 * @throws MetadataStoreException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 * @throws MetadataInspectorException
	 * @throws ResourceInitException
	 */
	@Test
	public void testDelete() throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
			MetadataInspectorException, ResourceInitException {
		LOG.info("START Test: testDelete");

		initStore(TstConstants.configURL);
		Assume.assumeNotNull(store);

		List<String> ids = TstUtils.insertMetadata(store, TstConstants.tst_9, TstConstants.tst_10, TstConstants.tst_1);

		LOG.info("Inserted records with ids: " + ids + ". Now: delete them...");
		String fileString = TstConstants.propEqualToID.getFile();
		if (fileString == null) {
			LOG.warn("Skipping test (file with filterExpression not found).");
			return;
		}

		// test the deletion
		XMLStreamReader xmlStreamFilter = readXMLStream(fileString);
		Filter constraintDelete = Filter110XMLDecoder.parse(xmlStreamFilter);
		xmlStreamFilter.close();

		MetadataStoreTransaction taDel = store.acquireTransaction();
		DeleteOperation delete = new DeleteOperation("delete", null, constraintDelete);
		taDel.performDelete(delete);
		taDel.commit();
		// test query
		MetadataQuery query = new MetadataQuery(null, null, null, null, 1, 10);
		resultSet = store.getRecords(query);
		int size = 0;
		while (resultSet.next()) {
			size++;
		}

		Assert.assertEquals(1, size);

	}

	@Test
	public void testUpdateString() throws MetadataStoreException, MetadataInspectorException, FactoryConfigurationError,
			ResourceInitException {
		String idToUpdate = prepareUpdate();
		if (idToUpdate == null) {
			return;
		}

		// constraint
		Operator op = new PropertyIsEqualTo(new ValueReference("apiso:identifier", nsContext),
				new Literal<PrimitiveValue>(idToUpdate), true, null);
		Filter constraint = new OperatorFilter(op);

		// create recordProperty
		List<MetadataProperty> recordProperties = new ArrayList<MetadataProperty>();
		String xPath = "/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
		ValueReference name = new ValueReference(xPath, nsContext);
		String value = "UPDATED ORGANISATIONNAME";
		recordProperties.add(new MetadataProperty(name, value));

		// update!
		MetadataStoreTransaction mst = store.acquireTransaction();
		UpdateOperation update = new UpdateOperation(null, null, null, constraint, recordProperties);
		mst.performUpdate(update);
		mst.commit();

		// get record which should be updated
		MetadataQuery query = new MetadataQuery(null, null, constraint, null, 1, 10);
		resultSet = store.getRecords(query);
		assertNotNull(resultSet);
		assertTrue(resultSet.next());

		MetadataRecord m = resultSet.getRecord();
		assertNotNull(m);
		assertTrue(m instanceof ISORecord);
		String identifier = m.getIdentifier();

		// test if the updated was successfull
		if (identifier.equals(idToUpdate)) {
			String updatedString = ((ISORecord) m).getStringFromXPath(new XPath(xPath, nsContext));
			Assert.assertEquals(value, updatedString);
		}
	}

	@Test
	public void testUpdateStringWithCQP() throws MetadataStoreException, MetadataInspectorException,
			FactoryConfigurationError, ResourceInitException {
		String idToUpdate = prepareUpdate();
		if (idToUpdate == null) {
			return;
		}

		// constraint
		Operator op = new PropertyIsEqualTo(new ValueReference("apiso:identifier", nsContext),
				new Literal<PrimitiveValue>(idToUpdate), true, null);
		Filter constraint = new OperatorFilter(op);

		// create recordProperty
		List<MetadataProperty> recordProperties = new ArrayList<MetadataProperty>();
		String xPath = "/apiso:Modified";
		ValueReference name = new ValueReference(xPath, nsContext);
		String value = "3333-11-22";
		recordProperties.add(new MetadataProperty(name, value));

		// update!
		MetadataStoreTransaction mst = store.acquireTransaction();
		UpdateOperation update = new UpdateOperation(null, null, null, constraint, recordProperties);
		mst.performUpdate(update);
		mst.commit();

		// get record which should be updated
		MetadataQuery query = new MetadataQuery(null, null, constraint, null, 1, 10);
		resultSet = store.getRecords(query);
		assertNotNull(resultSet);
		assertTrue(resultSet.next());

		MetadataRecord m = resultSet.getRecord();
		assertNotNull(m);
		assertTrue(m instanceof ISORecord);
		String identifier = m.getIdentifier();

		// test if the updated was successfull
		if (identifier.equals(idToUpdate)) {
			String updatedString = ((ISORecord) m)
				.getStringFromXPath(new XPath("/gmd:MD_Metadata/gmd:dateStamp/gco:Date", nsContext));
			Assert.assertEquals(value, updatedString);
		}
	}

	@Test
	public void testUpdateOMElementReplace() throws FactoryConfigurationError, MetadataStoreException,
			MetadataInspectorException, JaxenException, ResourceInitException {
		String idToUpdate = prepareUpdate();
		if (idToUpdate == null) {
			return;
		}

		// constraint
		Operator op = new PropertyIsEqualTo(new ValueReference("apiso:identifier", nsContext),
				new Literal<PrimitiveValue>(idToUpdate), true, null);
		Filter constraint = new OperatorFilter(op);

		// create recordProperty
		List<MetadataProperty> recordProperties = new ArrayList<MetadataProperty>();
		String xPath = "/gmd:MD_Metadata/gmd:contact";
		ValueReference name = new ValueReference(xPath, nsContext);
		InputStream is = ParseISOTest.class.getResourceAsStream("../update/replace.xml");
		XMLAdapter a = new XMLAdapter(is);
		OMElement value = a.getRootElement();
		recordProperties.add(new MetadataProperty(name, value));

		// update!
		MetadataStoreTransaction mst = store.acquireTransaction();
		UpdateOperation update = new UpdateOperation(null, null, null, constraint, recordProperties);
		mst.performUpdate(update);
		mst.commit();

		// get record which should be updated
		MetadataQuery query = new MetadataQuery(null, null, constraint, null, 1, 10);
		resultSet = store.getRecords(query);
		assertNotNull(resultSet);
		assertTrue(resultSet.next());

		MetadataRecord m = resultSet.getRecord();
		assertNotNull(m);
		assertTrue(m instanceof ISORecord);
		String identifier = m.getIdentifier();

		// test if the updated was successfull
		if (identifier.equals(idToUpdate)) {
			String testXpath = xPath + "/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString";
			OMElement updatedNode = ((ISORecord) m).getNodeFromXPath(new XPath(testXpath, nsContext));

			AXIOMXPath p = new AXIOMXPath(testXpath);
			p.setNamespaceContext(nsContext);
			Object valueNode = p.selectSingleNode(value);
			Assert.assertEquals(((OMElement) valueNode).getText(), updatedNode.getText());
		}

	}

	@Test
	public void testUpdateOMElementRemove() throws FactoryConfigurationError, MetadataStoreException,
			MetadataInspectorException, ResourceInitException {

		String idToUpdate = prepareUpdate();
		if (idToUpdate == null) {
			return;
		}

		// constraint
		Operator op = new PropertyIsEqualTo(new ValueReference("apiso:identifier", nsContext),
				new Literal<PrimitiveValue>(idToUpdate), true, null);
		Filter constraint = new OperatorFilter(op);

		// create recordProperty
		List<MetadataProperty> recordProperties = new ArrayList<MetadataProperty>();
		String xPath = "/gmd:MD_Metadata/gmd:dataQualityInfo";
		ValueReference name = new ValueReference(xPath, nsContext);
		recordProperties.add(new MetadataProperty(name, null));

		// get record which should be updated
		MetadataQuery query = new MetadataQuery(null, null, constraint, null, 1, 10);
		resultSet = store.getRecords(query);
		assertNotNull(resultSet);
		assertTrue(resultSet.next());

		MetadataRecord m = resultSet.getRecord();
		assertNotNull(m);

		OMElement updatedNode = ((ISORecord) m).getNodeFromXPath(new XPath(xPath, nsContext));
		assertNotNull(updatedNode);

		// update!
		MetadataStoreTransaction mst = store.acquireTransaction();
		UpdateOperation update = new UpdateOperation(null, null, null, constraint, recordProperties);
		mst.performUpdate(update);
		mst.commit();

		resultSet.close();

		// get record which should be updated
		resultSet = store.getRecords(query);
		assertNotNull(resultSet);
		assertTrue(resultSet.next());

		m = resultSet.getRecord();
		assertNotNull(m);
		assertTrue(m instanceof ISORecord);
		String identifier = m.getIdentifier();

		// test if the updated was successfull
		if (identifier.equals(idToUpdate)) {
			updatedNode = ((ISORecord) m).getNodeFromXPath(new XPath(xPath, nsContext));
			assertNull(updatedNode);
		}
	}

	@Test
	public void updateCompleteWithoutConstraint()
			throws MetadataStoreException, MetadataInspectorException, JaxenException, ResourceInitException {
		String idToUpdate = prepareUpdate();
		if (idToUpdate == null) {
			return;
		}

		// md to update
		InputStream is = ParseISOTest.class.getResourceAsStream("../update/9update.xml");
		XMLAdapter a = new XMLAdapter(is);
		ISORecord value = new ISORecord(a.getRootElement());

		// update!
		MetadataStoreTransaction mst = store.acquireTransaction();
		UpdateOperation update = new UpdateOperation(null, value, null, null, null);
		int noOfUp = mst.performUpdate(update);
		assertEquals(1, noOfUp);
		mst.commit();

		// get record which should be updated
		Operator op = new PropertyIsEqualTo(new ValueReference("apiso:identifier", nsContext),
				new Literal<PrimitiveValue>(idToUpdate), true, null);
		MetadataQuery query = new MetadataQuery(null, null, new OperatorFilter(op), null, 1, 10);
		resultSet = store.getRecords(query);
		assertNotNull(resultSet);
		assertTrue(resultSet.next());

		MetadataRecord m = resultSet.getRecord();
		assertNotNull(m);
		assertTrue(m instanceof ISORecord);
		String identifier = m.getIdentifier();

		// test if the updated was successfull
		if (identifier.equals(idToUpdate)) {
			String testXpath = "/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
			OMElement updatedNode = ((ISORecord) m).getNodeFromXPath(new XPath(testXpath, nsContext));

			AXIOMXPath p = new AXIOMXPath(testXpath);
			p.setNamespaceContext(nsContext);
			Object valueNode = p.selectSingleNode(value.getAsOMElement());
			Assert.assertEquals(((OMElement) valueNode).getText(), updatedNode.getText());
		}
	}

	@Test
	public void updateCompleteWithConstraint()
			throws MetadataStoreException, MetadataInspectorException, JaxenException, ResourceInitException {
		String idToUpdate = prepareUpdate();
		if (idToUpdate == null) {
			return;
		}

		// constraint
		Operator op = new PropertyIsEqualTo(new ValueReference("apiso:identifier", nsContext),
				new Literal<PrimitiveValue>(idToUpdate), true, null);
		Filter constraint = new OperatorFilter(op);

		// md to update
		InputStream is = ParseISOTest.class.getResourceAsStream("../update/9update.xml");
		XMLAdapter a = new XMLAdapter(is);
		ISORecord value = new ISORecord(a.getRootElement());

		// update!
		MetadataStoreTransaction mst = store.acquireTransaction();
		UpdateOperation update = new UpdateOperation(null, value, null, constraint, null);
		int noOfUp = mst.performUpdate(update);
		assertEquals(1, noOfUp);
		mst.commit();

		// get record which should be updated
		MetadataQuery query = new MetadataQuery(null, null, constraint, null, 1, 10);
		resultSet = store.getRecords(query);
		assertNotNull(resultSet);
		assertTrue(resultSet.next());

		MetadataRecord m = resultSet.getRecord();
		assertNotNull(m);
		assertTrue(m instanceof ISORecord);
		String identifier = m.getIdentifier();

		// test if the updated was successfull
		if (identifier.equals(idToUpdate)) {
			String testXpath = "/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
			OMElement updatedNode = ((ISORecord) m).getNodeFromXPath(new XPath(testXpath, nsContext));

			AXIOMXPath p = new AXIOMXPath(testXpath);
			p.setNamespaceContext(nsContext);
			Object valueNode = p.selectSingleNode(value.getAsOMElement());
			Assert.assertEquals(((OMElement) valueNode).getText(), ((OMElement) updatedNode).getText());
		}
	}

	@Test
	public void updateNotExistingRecord()
			throws MetadataStoreException, MetadataInspectorException, JaxenException, ResourceInitException {
		String idToUpdate = prepareUpdate();
		if (idToUpdate == null) {
			return;
		}

		// constraint
		Operator op = new PropertyIsEqualTo(new ValueReference("apiso:identifier", nsContext),
				new Literal<PrimitiveValue>("dummyDoesNotExist"), true, null);
		Filter constraint = new OperatorFilter(op);

		// md to update
		InputStream is = ParseISOTest.class.getResourceAsStream("../update/9update.xml");
		XMLAdapter a = new XMLAdapter(is);
		ISORecord value = new ISORecord(a.getRootElement());

		// update!
		MetadataStoreTransaction mst = store.acquireTransaction();
		UpdateOperation update = new UpdateOperation(null, value, null, constraint, null);
		int noOfUp = mst.performUpdate(update);
		assertEquals(0, noOfUp);
		mst.commit();
	}

	private String prepareUpdate() throws MetadataStoreException, MetadataInspectorException, ResourceInitException {
		LOG.info("START Test: testUpdate");

		initStore(TstConstants.configURL);
		Assume.assumeNotNull(store);

		List<String> ids = TstUtils.insertMetadata(store, TstConstants.tst_9);
		LOG.info("Inserted records with ids: " + ids + ". Now: update " + ids);

		assertNotNull(ids);
		assertTrue(ids.size() > 0);

		return ids.get(0);
	}

	private static XMLStreamReader readXMLStream(String fileString)
			throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		XMLStreamReader xmlStream = XMLInputFactory.newInstance()
			.createXMLStreamReader(new FileInputStream(new File(fileString)));
		XMLStreamUtils.skipStartDocument(xmlStream);
		return xmlStream;
	}

}
