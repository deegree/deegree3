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

package org.deegree.tools.rendering.dem.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Blob;
import java.sql.SQLException;

public class FileBlob implements Blob {

	private RandomAccessFile file;

	private FileChannel channel;

	public FileBlob(File blobFile) throws FileNotFoundException {
		file = new RandomAccessFile(blobFile, "rw");
		channel = file.getChannel();
	}

	public void free() throws SQLException {
		try {
			file.close();
			channel.close();
		}
		catch (IOException e) {
			throw new SQLException(e.getMessage(), e);
		}
	}

	public InputStream getBinaryStream() throws SQLException {
		return null;
	}

	public InputStream getBinaryStream(long pos, long length) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getBytes(long pos, int length) throws SQLException {
		byte[] bytes = new byte[length];
		try {
			file.seek(pos - 1);
			file.read(bytes);
		}
		catch (IOException e) {
			throw new SQLException(e.getMessage(), e);
		}
		return bytes;
	}

	public void readBytesViaChannel(ByteBuffer buffer, long pos) throws IOException {
		channel.read(buffer, pos);
	}

	public long length() throws SQLException {
		long length;
		try {
			length = file.length();
		}
		catch (IOException e) {
			throw new SQLException(e.getMessage(), e);
		}
		return length;
	}

	public long position(byte[] pattern, long start) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public long position(Blob pattern, long start) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public OutputStream setBinaryStream(long pos) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int setBytes(long pos, byte[] bytes) throws SQLException {
		try {
			file.seek(pos - 1);
			file.write(bytes);
		}
		catch (IOException e) {
			throw new SQLException(e.getMessage(), e);
		}
		return bytes.length;
	}

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		try {
			file.seek(pos - 1);
			file.write(bytes, offset, len);
		}
		catch (IOException e) {
			throw new SQLException(e.getMessage(), e);
		}
		return len;
	}

	public void truncate(long len) throws SQLException {
		// TODO Auto-generated method stub

	}

}
