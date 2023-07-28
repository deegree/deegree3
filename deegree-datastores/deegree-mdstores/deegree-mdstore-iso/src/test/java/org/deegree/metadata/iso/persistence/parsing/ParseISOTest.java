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
package org.deegree.metadata.iso.persistence.parsing;

import java.util.List;

import javax.xml.stream.FactoryConfigurationError;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.geometry.Envelope;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.iso.persistence.AbstractISOTest;
import org.deegree.metadata.iso.persistence.ISOMetadataStore;
import org.deegree.metadata.iso.persistence.ISOMetadataStoreProvider;
import org.deegree.metadata.iso.persistence.TstConstants;
import org.deegree.metadata.iso.persistence.TstUtils;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.protocol.csw.MetadataStoreException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class ParseISOTest extends AbstractISOTest {

	private static Logger LOG = LoggerFactory.getLogger(ParseISOTest.class);

	protected static final NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();

	@Test
	public void testVariousElements() throws MetadataStoreException, FactoryConfigurationError,
			MetadataInspectorException, ResourceInitException {
		LOG.info("START Test: test various elements for one metadataRecord ");

		initStore(TstConstants.configURL);
		Assume.assumeNotNull(store);

		List<String> ids = TstUtils.insertMetadata(store, TstConstants.tst_10);
		if (ids != null) {
			// test query
			MetadataQuery query = new MetadataQuery(null, null, null, null, 1, 10);
			resultSet = store.getRecords(query);
			// identifier
			String identifier = null;
			String[] title = null;
			String type = null;
			String[] subject = null;
			String[] format = null;
			String[] _abstract = null;
			String[] rights = null;
			String source = null;
			Envelope[] bbox = null;
			while (resultSet.next()) {
				MetadataRecord m = resultSet.getRecord();
				identifier = m.getIdentifier();
				title = m.getTitle();
				type = m.getType();
				subject = m.getSubject();
				format = m.getFormat();
				_abstract = m.getAbstract();
				rights = m.getRights();
				source = m.getSource();
				bbox = m.getBoundingBox();
			}
			StringBuilder s_ident = new StringBuilder();
			s_ident.append(identifier);
			StringBuilder s_title = new StringBuilder();
			for (String t : title) {
				s_title.append(t);
			}
			StringBuilder s_sub = new StringBuilder();
			for (String sub : subject) {
				s_sub.append(sub).append(' ');
			}
			StringBuilder s_form = new StringBuilder();
			for (String f : format) {
				s_form.append(f).append(' ');
			}
			StringBuilder s_ab = new StringBuilder();
			for (String a : _abstract) {
				s_ab.append(a);
			}
			StringBuilder s_ri = new StringBuilder();
			for (String r : rights) {
				s_ri.append(r).append(' ');
			}
			StringBuilder s_b = new StringBuilder();
			for (Envelope e : bbox) {
				s_b.append(e.getMin().get0()).append(' ').append(e.getMin().get1()).append(' ');
				s_b.append(e.getMax().get0()).append(' ').append(e.getMax().get1()).append(' ');
				s_b.append(e.getCoordinateSystem().getAlias());
				LOG.debug("boundingBox: " + s_b.toString());
			}

			Assert.assertEquals("identifier: ", "d0e5c36eec7f473b91b8b249da87d522", s_ident.toString());
			Assert.assertEquals("title: ", "SPOT 5 RAW 2007-01-23T10:25:14", s_title.toString());
			Assert.assertEquals("type: ", "dataset", type.toString());
			Assert.assertEquals("subjects: ", "SPOT 5 PATH 50 ROW 242 Orthoimagery imageryBaseMapsEarthCover ",
					s_sub.toString());
			Assert.assertEquals("formats: ", "RAW ECW ", s_form.toString());
			Assert.assertEquals("abstract: ", "Raw (source) image from CwRS campaigns.", s_ab.toString());
			Assert.assertEquals("rights: ", "otherRestrictions license ", s_ri.toString());
			Assert.assertEquals("source: ", "Raw (Source) image as delivered by image provider.", source.toString());
			Assert.assertEquals("bbox: ", "9.342556163 52.6984540464 10.4685111912 53.3646726483 epsg:4326",
					s_b.toString());
		}
		else {
			throw new MetadataStoreException("something went wrong in creation of the metadataRecord");
		}
	}

}