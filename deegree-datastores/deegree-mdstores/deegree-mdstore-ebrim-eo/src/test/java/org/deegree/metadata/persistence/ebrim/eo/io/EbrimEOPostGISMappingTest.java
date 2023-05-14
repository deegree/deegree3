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

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.sqldialect.filter.UnmappableException;
import org.junit.Test;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class EbrimEOPostGISMappingTest {

	private static final NamespaceBindings ns = CommonNamespaces.getNamespaceContext();

	static {
		ns.addNamespace("rim", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
		ns.addNamespace("wrs", "http://www.opengis.net/cat/wrs/1.0");
	}

	@Test
	public void testRegPack() throws FilterEvaluationException, UnmappableException {
		// PropertyName pn = new PropertyName( "/rim:RegistryPackage/@id", ns );
		// EbrimEOPostGISMapping mapping = new EbrimEOPostGISMapping();
		// PropertyNameMapping pnm = mapping.getMapping( pn, null );
		// assertNotNull( pnm );
		// assertNotNull( pnm.getTargetField() );
		// assertEquals( EOTYPE.PRODUCT.getTableName(), pnm.getTargetField().getTable() );
		// assertEquals( "regpackid", pnm.getTargetField().getColumn() );

	}

	@Test
	public void testEOProductID() throws FilterEvaluationException, UnmappableException {
		// PropertyName pn = new PropertyName(
		// "/rim:ExtrinsicObject/rim:Slot[@name=”urn:ogc:def:ebRIM-Slot:OGC-06-131:beginPosition”]/rim:ValueList/rim:Value[1]",
		// ns );
		// EbrimEOPostGISMapping mapping = new EbrimEOPostGISMapping();
		// PropertyNameMapping pnm = mapping.getMapping( pn, null );
		// assertEquals( "regPackId", pnm.getTargetField() );
	}

}
