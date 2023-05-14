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
package org.deegree.services.wfs.format.gml.request;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReference;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.gml.reference.GmlXlinkStrategy;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.services.wfs.format.gml.BufferableXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of additional (referenced) {@link GMLObject}s that have to be included in
 * {@link GetFeature}/ {@link GetPropertyValue} responses.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class WfsXlinkStrategy implements GmlXlinkStrategy {

	private static Logger LOG = LoggerFactory.getLogger(WfsXlinkStrategy.class);

	private LinkedHashMap<String, GMLReference<?>> uriToRef = new LinkedHashMap<String, GMLReference<?>>();

	private Map<GMLReference<?>, GmlXlinkOptions> refToResolveState = new HashMap<GMLReference<?>, GmlXlinkOptions>();

	private final BufferableXMLStreamWriter xmlStream;

	private final boolean localReferencesPossible;

	private final String remoteXlinkTemplate;

	private final GmlXlinkOptions resolveOptions;

	private final Set<String> exportedIds = new HashSet<String>();

	public WfsXlinkStrategy(BufferableXMLStreamWriter xmlStream, boolean localReferencesPossible, String xlinkTemplate,
			GmlXlinkOptions resolveOptions) {
		this.xmlStream = xmlStream;
		this.localReferencesPossible = localReferencesPossible;
		this.remoteXlinkTemplate = xlinkTemplate;
		this.resolveOptions = resolveOptions;
	}

	@Override
	public String requireObject(GMLReference<?> ref, GmlXlinkOptions resolveState) {
		String uri = ref.getURI();
		LOG.debug("Exporting forward reference to object {} which must be included in the output.", uri);
		uriToRef.put(uri, ref);
		refToResolveState.put(ref, resolveState);
		return uri;
	}

	@Override
	public String handleReference(GMLReference<?> ref) {

		String uri = ref.getURI();
		LOG.debug("Encountered reference to object {}.", uri);
		if (!isGmlIdBasedUri(uri)) {
			LOG.debug("Reference to object {} considered non-rewritable.", uri);
			return uri;
		}

		if (localReferencesPossible) {
			LOG.debug("Exporting potential forward reference to object {} which may or may not be exported later.",
					ref.getURI());
			try {
				xmlStream.activateBuffering();
			}
			catch (XMLStreamException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return "{" + ref.getId() + "}";
		}
		LOG.debug("Exporting reference to object {} as remote reference.", ref.getId());
		return remoteXlinkTemplate.replace("{}", ref.getId());
	}

	private boolean isGmlIdBasedUri(String uri) {
		return uri.startsWith("#");
	}

	public Collection<GMLReference<?>> getAdditionalRefs() {
		return uriToRef.values();
	}

	public Map<GMLReference<?>, GmlXlinkOptions> getResolveStates() {
		return refToResolveState;
	}

	public void clear() {
		uriToRef = new LinkedHashMap<String, GMLReference<?>>();
		refToResolveState = new HashMap<GMLReference<?>, GmlXlinkOptions>();
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
