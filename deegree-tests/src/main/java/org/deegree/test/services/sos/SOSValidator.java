/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.test.services.sos;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.test.services.OGCValidator;
import org.deegree.test.services.util.HTTPResponseValidator;
import org.junit.BeforeClass;

/**
 * This class is the base class for all integration tests for the SOS OGC services.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class SOSValidator extends OGCValidator {

	/**
	 * the URL for the test service
	 */
	protected static final String serviceURL;

	/**
	 * the location of the schema
	 */
	protected static final String schemaLocation;

	static {
		serviceURL = getProperty("sos.service.url");
		schemaLocation = getProperty("sos.schema.url");
	}

	/**
	 * HTTPResonseValidator for OGC service expeptions (status 400; content-type:
	 * application/vnd.ogc.se_xml)
	 */
	protected static final HTTPResponseValidator SERVICE_EXCEPTION = new HTTPResponseValidator() {
		{
			responseCode = 400;
			contentType = "application/vnd.ogc.se_xml;charset=UTF-8";
		}
	};

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void init() throws Exception {
		NamespaceContext ctxt = CommonNamespaces.getNamespaceContext();
		ctxt.addNamespace("om", "http://www.opengis.net/om/1.0");
		ctxt.addNamespace("sos", "http://www.opengis.net/sos/1.0");
		ctxt.addNamespace("ows", "http://www.opengis.net/ows/1.1");
		ctxt.addNamespace("swe", "http://www.opengis.net/swe/1.0.1");
		ctxt.addNamespace("xlink", XLN_NS);
		ctxt.addNamespace("sml", "http://www.opengis.net/sensorML/1.0.1");
		setNSContext(ctxt);
		setSchemaDoc(schemaLocation);
	}

}
