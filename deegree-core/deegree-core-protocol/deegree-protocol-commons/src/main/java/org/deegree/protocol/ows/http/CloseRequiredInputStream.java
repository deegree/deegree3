/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.ows.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} that needs to be closed after reading so the underlying resource
 * (e.g. an HTTP connection) will be closed.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class CloseRequiredInputStream extends InputStream {

	private final Closeable cleanupResource;

	private final InputStream is;

	/**
	 * Creates a new {@link CloseRequiredInputStream} instance.
	 * @param cleanupResource resource that needs to be closed, can be <code>null</code>
	 * @param is input stream, must not be <code>null</code>
	 */
	public CloseRequiredInputStream(Closeable cleanupResource, InputStream is) {
		this.cleanupResource = cleanupResource;
		this.is = is;
	}

	@Override
	public int available() throws IOException {
		return is.available();
	}

	@Override
	public void close() throws IOException {
		is.close();
		if (cleanupResource != null) {
			cleanupResource.close();
		}
	}

	@Override
	public void mark(int readLimit) {
		is.mark(readLimit);
	}

	@Override
	public boolean markSupported() {
		return is.markSupported();
	}

	@Override
	public int read() throws IOException {
		return is.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return is.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return is.read(b, off, len);
	}

	@Override
	public void reset() throws IOException {
		is.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return is.skip(n);
	}

}
