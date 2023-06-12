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
package org.deegree.test.services;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.deegree.commons.xml.NamespaceContext;
import org.deegree.test.services.util.HTTPResponseValidator;
import org.deegree.test.services.util.HTTPTempFile;
import org.deegree.test.services.util.XPathAsserter;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base class for integration tests of OGC services.
 *
 * <p>
 * This class offers some convenience for the validation process of OGC service requests.
 * See {@link ValidatorTemplate} for an example. Subclasses should call the
 * {@link #setSchemaDoc(String)} and {@link #setNSContext(NamespaceContext)} methods
 * within an init-method (use junit's {@link BeforeClass} annotation).<br />
 *
 * The unit tests should then call {@link #getAsserterForValidDoc(String)} to send the
 * request and validate the response. This method returns an {@link XPathAsserter} that
 * can be used to check the content of the repsonse.
 * {@link #getAsserterForValidDoc(String)} will also check the HTTP response (code 200,
 * content-type text/xml). You can alter this behaviour with your own
 * {@link HTTPResponseValidator}.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public abstract class OGCValidator {

	private static final String OGCVALIDATOR_PROPERTIES = "ogcvalidator.properties";

	private static final Logger LOG = LoggerFactory.getLogger(OGCValidator.class);

	/**
	 * The XLink namespace
	 */
	protected static final String XLN_NS = "http://www.w3.org/1999/xlink";

	private static NamespaceContext ctxt;

	private static String schema;

	private static final Properties prop = new Properties();

	static {
		try {
			prop.load(OGCValidator.class.getResourceAsStream(OGCVALIDATOR_PROPERTIES));
		}
		catch (IOException e) {
			LOG.error("unable to load " + OGCVALIDATOR_PROPERTIES);
		}
	}

	/**
	 * Set the schema for this validator.
	 * @param schemaLocation a URL for the schema file
	 */
	protected static void setSchemaDoc(String schemaLocation) {
		schema = schemaLocation;
	}

	/**
	 * Set the namespace context for the {@link XPathAsserter}.
	 * @param nsContext
	 */
	protected static void setNSContext(NamespaceContext nsContext) {
		ctxt = nsContext;
	}

	/**
	 * get a property from the ogcvalidator.properties file
	 * @param key
	 * @return the property
	 */
	protected static String getProperty(String key) {
		return prop.getProperty(key);
	}

	/**
	 * Create a XPathAsserter for the given URL. This method further checks if the
	 * document is valid and if the HTTP response is OK (status=200) and text/xml.
	 * @param url the request URL
	 * @return an XPathAsserter
	 */
	protected XPathAsserter getAsserterForValidDoc(String url) {
		return getAsserterForValidDoc(url, new HTTPResponseValidator());
	}

	/**
	 * Create a XPathAsserter for the given URL. This method further checks if the
	 * document is valid and checks the HTTP response with the given validator.
	 * @param url the request URL
	 * @param check an {@link HTTPResponseValidator}
	 * @return an XPathAsserter
	 */
	protected XPathAsserter getAsserterForValidDoc(String url, HTTPResponseValidator check) {
		HTTPTempFile http = getHTTPTempFile(url);
		return getAsserterForValidDoc(url, http, check);
	}

	/**
	 * Create a XPathAsserter for the given URL and POST content. This method further
	 * checks if the document is valid and if the HTTP response is OK (status=200) and
	 * text/xml.
	 * @param url the request URL
	 * @param post a file with the post content
	 * @return an XPathAsserter
	 */
	protected XPathAsserter getAsserterForValidDoc(String url, File post) {
		return getAsserterForValidDoc(url, post, new HTTPResponseValidator());
	}

	/**
	 * Create a XPathAsserter for the given URL and POST content. This method further
	 * checks if the document is valid and checks the HTTP response with the given
	 * validator.
	 * @param url the request URL
	 * @param post a file with the post content
	 * @param check an {@link HTTPResponseValidator}
	 * @return an XPathAsserter
	 */
	protected XPathAsserter getAsserterForValidDoc(String url, File post, HTTPResponseValidator check) {
		HTTPTempFile http = getHTTPTempFile(url, post);
		return getAsserterForValidDoc(url, http, check);
	}

	/**
	 * Create a XPathAsserter for the given URL and POST content. This method further
	 * checks if the document is valid and checks the HTTP response with the given
	 * validator.
	 * @param url the request URL
	 * @param check an {@link HTTPResponseValidator}
	 * @return an XPathAsserter
	 */
	private XPathAsserter getAsserterForValidDoc(String url, HTTPTempFile http, HTTPResponseValidator check) {
		if (ctxt == null) {
			fail("validator is not initialized. please set the namespace context with setNSContext");
		}
		LOG.info("Start validating {}", url);
		check.validate(http);
		if (schema == null) {
			fail("validator is not initialized correctly. please set the schema with setSchemaDoc");
		}
		// XMLAssert.assertValidDocument( schema, new InputSource( http.getReader() ) );
		return new XPathAsserter(http.getStream(), url, ctxt);
	}

	private static HTTPTempFile getHTTPTempFile(String url) {
		HTTPTempFile httpTempFile;
		try {
			httpTempFile = new HTTPTempFile(url);
		}
		catch (IOException e) {
			fail("unable to load url " + url);
			return null;
		}
		return httpTempFile;
	}

	private static HTTPTempFile getHTTPTempFile(String url, File post) {
		HTTPTempFile httpTempFile;
		try {
			httpTempFile = new HTTPTempFile(url, post);
		}
		catch (IOException e) {
			fail("unable to load url with post request " + url + " (" + e.getMessage() + ")");
			return null;
		}
		return httpTempFile;
	}

	/**
	 * Get a file from the class resource.
	 * @param filename the local filename
	 * @param cls
	 * @return the file
	 */
	protected File getFile(String filename, Class<?> cls) {
		return new File(cls.getResource(filename).getFile());
	}

}
