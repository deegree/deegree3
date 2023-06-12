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
package org.deegree.commons.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Writes back all bytes that are read into an output stream.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class LoggingInputStream extends InputStream {

	private InputStream in;

	private OutputStream out;

	/**
	 * @param in
	 * @param out
	 */
	public LoggingInputStream(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public int read() throws IOException {
		int read = in.read();
		if (read > 0) {
			out.write(read);
		}
		return read;
	}

	@Override
	public int read(byte[] bs) throws IOException {
		int read = in.read(bs);
		if (read > 0) {
			out.write(bs, 0, read);
		}
		return read;
	}

	@Override
	public int read(byte[] bs, int off, int len) throws IOException {
		int read = in.read(bs, off, len);
		if (read > 0) {
			out.write(bs, off, off + read);
		}
		return read;
	}

	@Override
	public void close() throws IOException {
		in.close();
		out.close();
	}

}
