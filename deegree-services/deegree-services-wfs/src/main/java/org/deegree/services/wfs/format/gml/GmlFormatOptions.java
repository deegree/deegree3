/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wfs.format.gml;

import javax.xml.namespace.QName;

import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.SFSProfiler;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLSchemaInfoSet;

/**
 * Configuration options for {@link GmlFormat}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @since 3.3
 */
public class GmlFormatOptions {

	private final GMLVersion gmlVersion;

	private final QName responseContainerEl;

	private final QName responseFeatureMemberEl;

	private final String schemaLocation;

	private final boolean disableStreaming;

	private final boolean generateBoundedByForFeatures;

	private final int queryMaxFeatures;

	private final boolean checkAreaOfUse;

	private final CoordinateFormatter formatter;

	private final String appSchemaBaseURL;

	private final String mimeType;

	private final boolean exportOriginalSchema;

	private final SFSProfiler geometrySimplifier;

	private final NamespaceBindings prebindNamespaces;

	private final boolean enableResponsePaging;

	private final GMLSchemaInfoSet originalSchemaLocation;

	/**
	 * Creates a new {@link GmlFormatOptions} instance.
	 * @param gmlVersion GML version, must not be <code>null</code>
	 * @param responseContainerEl can be <code>null</code> (use default
	 * <code>wfs:FeatureCollection</code> element)
	 * @param responseFeatureMemberEl can be <code>null</code> (use default
	 * <code>wfs:featureMember/wfs:member</code> element)
	 * @param schemaLocation can be <code>null</code> ()
	 * @param disableStreaming
	 * @param generateBoundedByForFeatures
	 * @param queryMaxFeatures
	 * @param checkAreaOfUse
	 * @param formatter
	 * @param appSchemaBaseURL
	 * @param mimeType
	 * @param exportOriginalSchema
	 * @param geometrySimplifier simplifier to apply to exported geometries, can be
	 * <code>null</code> (no simplification performed)
	 * @param prebindNamespaces namespaces to bind in the root element of GetFeature
	 * responses, can be <code>null</code>
	 * @param enableResponsePaging is response paging enabled
	 */
	public GmlFormatOptions(final GMLVersion gmlVersion, final QName responseContainerEl,
			final QName responseFeatureMemberEl, final String schemaLocation, final boolean disableStreaming,
			final boolean generateBoundedByForFeatures, final int queryMaxFeatures, final boolean checkAreaOfUse,
			final CoordinateFormatter formatter, final String appSchemaBaseURL, final String mimeType,
			final boolean exportOriginalSchema, final SFSProfiler geometrySimplifier,
			final NamespaceBindings prebindNamespaces, final boolean enableResponsePaging,
			final GMLSchemaInfoSet originalSchemaLocation) {
		this.gmlVersion = gmlVersion;
		this.responseContainerEl = responseContainerEl;
		this.responseFeatureMemberEl = responseFeatureMemberEl;
		this.schemaLocation = schemaLocation;
		this.disableStreaming = disableStreaming;
		this.generateBoundedByForFeatures = generateBoundedByForFeatures;
		this.queryMaxFeatures = queryMaxFeatures;
		this.checkAreaOfUse = checkAreaOfUse;
		this.formatter = formatter;
		this.appSchemaBaseURL = appSchemaBaseURL;
		this.mimeType = mimeType;
		this.exportOriginalSchema = exportOriginalSchema;
		this.geometrySimplifier = geometrySimplifier;
		this.prebindNamespaces = prebindNamespaces;
		this.enableResponsePaging = enableResponsePaging;
		this.originalSchemaLocation = originalSchemaLocation;
	}

	/**
	 * @return the gmlVersion
	 */
	public GMLVersion getGmlVersion() {
		return gmlVersion;
	}

	/**
	 * @return the responseContainerEl
	 */
	public QName getResponseContainerEl() {
		return responseContainerEl;
	}

	/**
	 * @return the responseFeatureMemberEl
	 */
	public QName getResponseFeatureMemberEl() {
		return responseFeatureMemberEl;
	}

	/**
	 * @return the schemaLocation
	 */
	public String getSchemaLocation() {
		return schemaLocation;
	}

	/**
	 * @return the disableStreaming
	 */
	public boolean isDisableStreaming() {
		return disableStreaming;
	}

	/**
	 * @return the generateBoundedByForFeatures
	 */
	public boolean isGenerateBoundedByForFeatures() {
		return generateBoundedByForFeatures;
	}

	/**
	 * @return the queryMaxFeatures
	 */
	public int getQueryMaxFeatures() {
		return queryMaxFeatures;
	}

	/**
	 * @return the checkAreaOfUse
	 */
	public boolean isCheckAreaOfUse() {
		return checkAreaOfUse;
	}

	/**
	 * @return the formatter
	 */
	public CoordinateFormatter getFormatter() {
		return formatter;
	}

	/**
	 * @return the appSchemaBaseURL
	 */
	public String getAppSchemaBaseURL() {
		return appSchemaBaseURL;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @return the exportOriginalSchema
	 */
	public boolean isExportOriginalSchema() {
		return exportOriginalSchema;
	}

	/**
	 * Returns the {@link SFSProfiler} to apply to exported {@link Geometry} instances.
	 * @return simplifier, can be <code>null</code> (no simplification performed)
	 */
	public SFSProfiler getGeometrySimplifier() {
		return geometrySimplifier;
	}

	/**
	 * @return namespaces to bind in the root element of GetFeature responses, can be
	 * <code>null</code>
	 */
	public NamespaceBindings getPrebindNamespaces() {
		return prebindNamespaces;
	}

	/**
	 * @return is response paging enabled
	 */
	public boolean isEnableResponsePaging() {
		return enableResponsePaging;
	}

	public GMLSchemaInfoSet getOriginalSchemaLocation() {
		return originalSchemaLocation;
	}

}
