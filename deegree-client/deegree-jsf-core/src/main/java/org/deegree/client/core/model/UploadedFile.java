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
package org.deegree.client.core.model;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

import org.apache.commons.fileupload.FileItem;

/**
 * <code>UploadedFile</code> wraps the uploaded file item
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@Deprecated
public class UploadedFile implements Serializable {

	private static final long serialVersionUID = -8302793775220721969L;

	private FileItem fileItem;

	private URL url;

	private String absolutePath;

	public UploadedFile() {
	}

	/**
	 * Deletes the file
	 * @return
	 */
	public boolean delete() {
		File file = new File(absolutePath);
		return file.delete();
	}

	/**
	 * @return the absolute path of the uploaded file
	 */
	public String getAbsolutePath() {
		return absolutePath;
	}

	/**
	 * @return the name of the uploaded file
	 */
	public String getFileName() {
		return fileItem.getName();
	}

	/**
	 * @return the uploaded file item
	 */
	public FileItem getFileItem() {
		return fileItem;
	}

	/**
	 * @return the web accessible url of the uploaded file
	 */
	public URL getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return "" + fileItem;
	}

	public void setFileItem(FileItem fileItem) {
		this.fileItem = fileItem;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

}