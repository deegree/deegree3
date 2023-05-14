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
package org.deegree.test.services.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * This class downloads a given URL and stores the result in a temporary file for repeated
 * access.
 *
 * <p>
 * You can get multiple reader ({@link HTTPTempFile#getReader()}) without downloading the
 * URL again. It also gives you access to the status and response headers.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class HTTPTempFile {

	private final HttpMethod method;

	private File tmpFile;

	private int status;

	/**
	 * Create a new HTTPTempFile
	 * @param url
	 * @throws IOException
	 */
	public HTTPTempFile(String url) throws IOException {
		this.method = new GetMethod(url);
		doRequest();
	}

	/**
	 * Create a new HTTPTempFile for POST request
	 * @param url
	 * @param post the content for the POST request
	 * @throws IOException
	 */
	public HTTPTempFile(String url, File post) throws IOException {
		PostMethod method = new PostMethod(url);
		method.setRequestEntity(new FileRequestEntity(post, "text/xml"));
		this.method = method;
		doRequest();
	}

	private void doRequest() throws IOException {
		tmpFile = File.createTempFile("http_tmp_file_", ".bin");
		tmpFile.deleteOnExit();
		try {
			HttpClient client = new HttpClient();
			status = client.executeMethod(method);
			writeStreamToFile(method.getResponseBodyAsStream(), tmpFile);
		}
		catch (HttpException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private void writeStreamToFile(InputStream stream, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		byte[] buf = new byte[1024];
		int count;
		while ((count = stream.read(buf)) > 0) {
			fos.write(buf, 0, count);
		}
		fos.close();
	}

	/**
	 * @return a new reader for the HTTP response
	 */
	public FileReader getReader() {
		try {
			return new FileReader(tmpFile);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return a new InputStream for the HTTP response
	 */
	public InputStream getStream() {
		try {
			return new FileInputStream(tmpFile);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the HTTP response code
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param key the response header name
	 * @return the value of the header, or <code>null</code>
	 */
	public String getHeader(String key) {
		Header header = method.getResponseHeader(key);
		if (header == null) {
			return null;
		}
		return header.getValue();
	}

}
