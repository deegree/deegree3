/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Checks the parsing of the association object
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class AssociationTest {

	private static Association association;

	@BeforeClass
	public static void initAssociation() throws XMLStreamException, FactoryConfigurationError {
		XMLStreamReader associationAsXml = readAssociationFromXml();
		association = new Association(associationAsXml);
	}

	@Test
	public void testGetTargetObjectShouldBeParsedFromXml() {
		String actualTargetObject = association.getTargetObject();
		assertEquals("urn:ogc:def:EOP:RE00:MSI_IMG_3A:5397721:eoap", actualTargetObject);
	}

	@Test
	public void testGetSourceObjectShouldBeParsedFromXml() {
		String actualSourceObject = association.getSourceObject();
		assertEquals("urn:ogc:def:EOP:RE00:MSI_IMG_3A:5397721:eo", actualSourceObject);
	}

	@Test
	public void testGetAssociationTypeShouldBeParsedFromXml() {
		String actualAssociationType = association.getAssociationType();
		assertEquals("urn:x-ogc:specification:csw-ebrim:AssociationType:EO:AcquiredBy", actualAssociationType);
	}

	private static XMLStreamReader readAssociationFromXml() throws XMLStreamException, FactoryConfigurationError {
		InputStream associationAsStream = AssociationTest.class.getResourceAsStream("association.xml");
		return XMLInputFactory.newInstance().createXMLStreamReader(associationAsStream);
	}

}