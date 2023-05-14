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
package org.deegree.client.core.filter;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class InputFileWrapper extends HttpServletRequestWrapper {

	private Map<String, String[]> formParameters;

	@SuppressWarnings("unchecked")
	public InputFileWrapper(HttpServletRequest request) throws ServletException {
		super(request);
		try {
			ServletFileUpload upload = new ServletFileUpload();
			DiskFileItemFactory factory = new DiskFileItemFactory();
			upload.setFileItemFactory(factory);
			String encoding = request.getCharacterEncoding();
			List<FileItem> fileItems = upload.parseRequest(request);
			formParameters = new HashMap<String, String[]>();
			for (int i = 0; i < fileItems.size(); i++) {
				FileItem item = fileItems.get(i);
				if (item.isFormField()) {
					String[] values;
					String v;
					if (encoding != null) {
						v = item.getString(encoding);
					}
					else {
						v = item.getString();
					}
					if (formParameters.containsKey(item.getFieldName())) {
						String[] strings = formParameters.get(item.getFieldName());
						values = new String[strings.length + 1];
						for (int j = 0; j < strings.length; j++) {
							values[j] = strings[j];
						}
						values[strings.length] = v;
					}
					else {
						values = new String[] { v };
					}
					formParameters.put(item.getFieldName(), values);
				}
				else if (item.getName() != null && item.getName().length() > 0 && item.getSize() > 0) {
					request.setAttribute(item.getFieldName(), item);
				}
			}
		}
		catch (FileUploadException fe) {
			ServletException servletEx = new ServletException();
			servletEx.initCause(fe);
			throw servletEx;
		}
		catch (UnsupportedEncodingException e) {
			ServletException servletEx = new ServletException();
			servletEx.initCause(e);
			throw servletEx;
		}
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return formParameters;
	}

	@Override
	public String[] getParameterValues(String name) {
		return (String[]) formParameters.get(name);
	}

	@Override
	public String getParameter(String name) {
		String[] params = getParameterValues(name);
		if (params == null)
			return null;
		return params[0];
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(formParameters.keySet());
	}

}
