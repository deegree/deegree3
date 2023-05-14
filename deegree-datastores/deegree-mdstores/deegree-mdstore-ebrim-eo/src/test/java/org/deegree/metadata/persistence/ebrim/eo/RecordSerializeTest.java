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
package org.deegree.metadata.persistence.ebrim.eo;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.junit.Test;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public class RecordSerializeTest {

	protected static final NamespaceBindings ns = CommonNamespaces.getNamespaceContext();

	static {
		ns.addNamespace("rim", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
	}

	@Test
	public void testBrief() throws Exception {
		InputStream is = EbrimEOTransactionTest.class.getResourceAsStream("io/ebrimRecord2.xml");
		RegistryPackage rec = new RegistryPackage(new XMLAdapter(is).getRootElement());
		XMLOutputFactory of = XMLOutputFactory.newInstance();
		StringWriter sw = new StringWriter();
		XMLStreamWriter writer = of.createXMLStreamWriter(sw);
		rec.serialize(writer, ReturnableElement.brief);
		writer.close();
		System.out.println(sw.toString());
		XMLInputFactory inf = XMLInputFactory.newInstance();
		StringReader input = new StringReader(sw.toString());
		XMLStreamReader reader = inf.createXMLStreamReader(input);

		XMLAdapter adapter = new XMLAdapter(reader);
		String[] slots = adapter.getNodesAsStrings(adapter.getRootElement(), new XPath("//rim:Slot", ns));
		Assert.assertEquals(0, slots.length);
	}

}
