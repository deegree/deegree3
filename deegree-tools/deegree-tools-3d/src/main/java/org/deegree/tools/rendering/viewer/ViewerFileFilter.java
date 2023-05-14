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
package org.deegree.tools.rendering.viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * The <code>CustomFileFilter</code> class adds functionality to the filefilter mechanism
 * of the JFileChooser.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class ViewerFileFilter extends FileFilter {

	private List<String> acceptedExtensions;

	private String desc;

	/**
	 * @param acceptedExtensions list of extensions this filter accepts.
	 * @param description to show
	 */
	public ViewerFileFilter(List<String> acceptedExtensions, String description) {
		this.acceptedExtensions = new ArrayList<String>(acceptedExtensions.size());
		StringBuilder sb = new StringBuilder();
		if (acceptedExtensions.size() > 0) {

			sb.append("(");
			int i = 0;
			for (String ext : acceptedExtensions) {
				if (ext.startsWith(".")) {
					ext = ext.substring(1);
				}
				else if (ext.startsWith("*.")) {
					ext = ext.substring(2);
				}
				else if (ext.startsWith("*")) {
					ext = ext.substring(1);
				}

				this.acceptedExtensions.add(ext.trim().toUpperCase());
				sb.append("*.");
				sb.append(ext);
				if (++i < acceptedExtensions.size()) {
					sb.append(", ");
				}
			}
			sb.append(")");
		}
		sb.append(description);
		desc = sb.toString();
	}

	/**
	 * @param extension
	 * @return true if the extension is accepted
	 */
	public boolean accepts(String extension) {
		return extension != null && acceptedExtensions.contains(extension.toUpperCase());
	}

	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return true;
		}

		String extension = getExtension(pathname);
		if (extension != null) {
			if (acceptedExtensions.contains(extension.trim().toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param f
	 * @return the file extension (e.g. gml/shp/xml etc.)
	 */
	String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	@Override
	public String getDescription() {
		return desc;
	}

}
