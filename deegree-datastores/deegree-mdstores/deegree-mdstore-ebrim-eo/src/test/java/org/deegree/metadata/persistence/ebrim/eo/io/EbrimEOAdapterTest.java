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
package org.deegree.metadata.persistence.ebrim.eo.io;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.SLOTURN;

import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.metadata.ebrim.Association;
import org.deegree.metadata.ebrim.Classification;
import org.deegree.metadata.ebrim.ClassificationNode;
import org.deegree.metadata.ebrim.ExtrinsicObject;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.EOTYPE;
import org.junit.Test;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class EbrimEOAdapterTest {

	@Test
	public void testParsingExtrinsicObject() throws ParseException {
		InputStream is = EbrimEOAdapterTest.class.getResourceAsStream("ebrimRecord1.xml");
		RegistryPackage rp = new RegistryPackage(new XMLAdapter(is).getRootElement());

		assertNotNull(rp);

		List<ExtrinsicObject> eops = rp.getExtrinsicObjects(EOTYPE.PRODUCT.getType());
		List<ExtrinsicObject> acqp = rp.getExtrinsicObjects(EOTYPE.ACQUPLATFORM.getType());

		assertNotNull(eops);
		assertEquals(1, eops.size());
		ExtrinsicObject eop = eops.get(0);
		assertEquals("DESCENDING", eop.getSlotValue(SLOTURN + "orbitDirection"));
		assertEquals("2010-06-06", eop.getSlotValue(SLOTURN + "beginPosition"));
		assertEquals("11.55", eop.getSlotValue(SLOTURN + "acrossTrackIncidenceAngle"));

		assertNotNull(acqp);
		assertEquals(1, acqp.size());
		assertEquals("6.5", acqp.get(0).getSlotValue(SLOTURN + "sensorResolution"));

		assertNull(eop.getSlotValue(SLOTURN + "centerOf"));

		Geometry multiExtentOf = (Geometry) eop.getGeometrySlotValue(SLOTURN + "multiExtentOf");
		assertNotNull(multiExtentOf);
		assertTrue(multiExtentOf instanceof MultiSurface<?>);

		// Classification
		List<Classification> classifications = rp.getClassifications();
		assertNotNull(classifications);
		assertEquals(1, classifications.size());
		Classification classification = classifications.get(0);
		assertNotNull(classification);
		assertEquals("urn:x-ogc:specification:csw-ebrim:EO:EOProductTypes:OPT", classification.getClassificationNode());
		assertEquals("urn:x-ogc:specification:cswebrim:EO:EOProductTypes", classification.getClassificationScheme());
		assertEquals("urn:ogc:def:EOP:RE00:IMG_MSI_3A:5230420:class", classification.getId());

		List<Association> associations = rp.getAssociations();
		assertNotNull(associations);
		assertEquals(4, associations.size());

		List<ClassificationNode> classificationNodes = rp.getClassificationNodes();
		assertEquals(0, classificationNodes.size());
	}

	@Test
	public void testParsingEoProfile() {
		InputStream is = EbrimEOAdapterTest.class.getResourceAsStream("eo_profile_extension_package.xml");
		RegistryPackage rp = new RegistryPackage(new XMLAdapter(is).getRootElement());
		assertNotNull(rp);

		assertEquals(6, rp.getAssociations().size());
		assertEquals(0, rp.getClassifications().size());
		assertEquals(0, rp.getExtrinsicObjects(EOTYPE.PRODUCT.getType()).size());

		List<ClassificationNode> classificationNodes = rp.getClassificationNodes();
		assertEquals(18, classificationNodes.size());
	}

}
