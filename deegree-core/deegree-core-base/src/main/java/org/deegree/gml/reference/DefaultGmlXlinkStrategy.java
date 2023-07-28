/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.gml.reference;

import java.util.HashSet;
import java.util.Set;

import org.deegree.commons.tom.gml.GMLReference;

public class DefaultGmlXlinkStrategy implements GmlXlinkStrategy {

	private final String remoteXlinkTemplate;

	private final GmlXlinkOptions resolveOptions;

	private final Set<String> exportedIds = new HashSet<String>();

	public DefaultGmlXlinkStrategy() {
		this.remoteXlinkTemplate = "#{}";
		this.resolveOptions = new GmlXlinkOptions();
	}

	/**
	 * @param remoteXlinkTemplate template used to create references to document-remote
	 * objects, e.g.
	 * <code>http://localhost:8080/d3_wfs_lab/services?SERVICE=WFS&REQUEST=GetGmlObject&VERSION=1.1.0&TRAVERSEXLINKDEPTH=1&GMLOBJECTID={}</code>
	 * , the substring <code>{}</code> is replaced by the object id, must not be
	 * <code>null</code>
	 * @param resolveOptions
	 */
	public DefaultGmlXlinkStrategy(String remoteXlinkTemplate, GmlXlinkOptions resolveOptions) {
		this.remoteXlinkTemplate = remoteXlinkTemplate;
		this.resolveOptions = resolveOptions;
	}

	@Override
	public String requireObject(GMLReference<?> ref, GmlXlinkOptions resolveState) {
		if (ref.isLocal()) {
			return remoteXlinkTemplate.replace("{}", ref.getId());
		}
		return ref.getURI();
	}

	@Override
	public String handleReference(GMLReference<?> ref) {
		if (ref.isLocal()) {
			return remoteXlinkTemplate.replace("{}", ref.getId());
		}
		return ref.getURI();
	}

	@Override
	public GmlXlinkOptions getResolveOptions() {
		return resolveOptions;
	}

	@Override
	public void addExportedId(String gmlId) {
		exportedIds.add(gmlId);
	}

	@Override
	public boolean isObjectExported(String gmlId) {
		return exportedIds.contains(gmlId);
	}

}
