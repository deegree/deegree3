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
package org.deegree.tools.alkis;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReference;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.gml.reference.GmlXlinkStrategy;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of {@link GMLObject} references that have to be included in a
 * {@link GetFeature} response.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
class XlinkedObjectsHandler implements GmlXlinkStrategy {

	private static Logger LOG = LoggerFactory.getLogger(XlinkedObjectsHandler.class);

	private LinkedHashMap<String, GMLReference<?>> objectIdToRef = new LinkedHashMap<String, GMLReference<?>>();

	private final boolean localReferencesPossible;

	private final String remoteXlinkTemplate;

	private final GmlXlinkOptions resolveOptions;

	private final Set<String> exportedIds = new HashSet<String>();

	XlinkedObjectsHandler(boolean localReferencesPossible, String xlinkTemplate, GmlXlinkOptions resolveOptions) {
		this.localReferencesPossible = localReferencesPossible;
		this.remoteXlinkTemplate = xlinkTemplate;
		this.resolveOptions = resolveOptions;
	}

	@Override
	public String requireObject(GMLReference<?> ref, GmlXlinkOptions resolveState) {
		LOG.debug("Exporting forward reference to object {} which must be included in the output.", ref.getId());
		objectIdToRef.put(ref.getId(), ref);
		return "#" + ref.getId();
	}

	@Override
	public String handleReference(GMLReference<?> ref) {
		if (localReferencesPossible) {
			LOG.debug("Exporting potential forward reference to object {} which may or may not be exported later.",
					ref.getId());
			// try {
			// xmlStream.activateBuffering();
			// } catch ( XMLStreamException e ) {
			// throw new RuntimeException( e.getMessage(), e );
			// }
			return "{" + ref.getId() + "}";
		}
		LOG.debug("Exporting reference to object {} as remote reference.", ref.getId());
		return remoteXlinkTemplate.replace("{}", ref.getId());
	}

	Collection<GMLReference<?>> getAdditionalRefs() {
		return objectIdToRef.values();
	}

	void clear() {
		objectIdToRef = new LinkedHashMap<String, GMLReference<?>>();
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
