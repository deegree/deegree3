/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.services.config.actions;

import static org.deegree.services.config.actions.Utils.getWorkspaceAndPath;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.configuration.wkt.WKTParser;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.persistence.CRSStore;

/**
 * Lists CRS or detects if a CRS is available.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class Crs {

	private static final Pattern CODE = Pattern.compile("EPSG:[0-9]+");

	public static void listCrs(HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		List<ICRS> crss = new ArrayList<ICRS>();
		Collection<CRSStore> all = CRSManager.getAll();
		for (CRSStore crsStore : all) {
			crss.addAll(crsStore.getAvailableCRSs());
		}

		List<String> codes = new ArrayList<String>(crss.size());

		ServletOutputStream out = resp.getOutputStream();
		for (ICRS crs : crss) {
			for (CRSCodeType code : crs.getCodes()) {
				String s = code.toString().toUpperCase();
				if (CODE.matcher(s).matches()) {
					if (!codes.contains(s)) {
						codes.add(s);
					}
				}
			}
		}

		Collections.sort(codes);

		for (String code : codes) {
			IOUtils.write(code + "\n", out);
		}
	}

	public static void checkCrs(String path, HttpServletResponse resp) throws IOException {
		Pair<DeegreeWorkspace, String> p = getWorkspaceAndPath(path);
		try {
			CRSManager.lookup(p.second);
			IOUtils.write("true", resp.getOutputStream());
		}
		catch (UnknownCRSException e) {
			IOUtils.write("false", resp.getOutputStream());
		}
	}

	public static void getCodes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ServletOutputStream out = resp.getOutputStream();

		String s = req.getParameter("wkt");
		s = URLDecoder.decode(s, "UTF-8");
		ICRS crs = WKTParser.parse(s);
		Collection<CRSStore> all = CRSManager.getAll();
		for (CRSStore crsStore : all) {
			for (ICRS c : crsStore.getAvailableCRSs()) {
				if (c.equals(crs)) {
					for (CRSCodeType ct : c.getCodes()) {
						IOUtils.write(ct.toString(), out);
					}
				}
			}
		}
	}

}
