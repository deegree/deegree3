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

import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang.StringUtils.trim;
import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.geometry.SFSProfiler;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.geometry.linearization.MaxErrorCriterion;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.wfs.GMLFormat;
import org.deegree.services.jaxb.wfs.GMLFormat.GetFeatureResponse;
import org.deegree.services.jaxb.wfs.GMLFormat.GetFeatureResponse.PrebindNamespace;
import org.deegree.services.jaxb.wfs.GeometryLinearization;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.format.Format;
import org.deegree.services.wfs.format.gml.request.GmlDescribeFeatureTypeHandler;
import org.deegree.services.wfs.format.gml.request.GmlGetFeatureHandler;
import org.deegree.services.wfs.format.gml.request.GmlGetGmlObjectHandler;
import org.deegree.services.wfs.format.gml.request.GmlGetPropertyValueHandler;
import org.deegree.workspace.ResourceInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link Format} implementation that can handle GML 2/3.0/3.1/3.2 and the
 * specific requirements for WFS 2.0 response <code>FeatureCollection</code>s (which are
 * not GML feature collections in a strict sense).
 * <p>
 * NOTE: For WFS 1.1.0, some schema communities decided to use a different feature
 * collection element than <code>wfs:FeatureCollection</code>, mostly because
 * <code>wfs:FeatureCollection</code> is bound to GML 3.1. This practice is supported by
 * this {@link Format} implementation for WFS 1.0.0 and WFS 1.1.0 output. However, for WFS
 * 2.0, there's hope that people will refrain from doing so (as WFS 2.0
 * <code>FeatureCollection</code> allows GML 3.2 output and is not bound to any specific
 * GML version). Therefore, it is currently not supported to use any different output
 * container for WFS 2.0.
 * </p>
 *
 * @author <a href="mailto:wanhoff@lat-lon.de">Jeronimo Wanhoff</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @since 3.3
 */
public class GmlFormat implements Format {

	private static final Logger LOG = LoggerFactory.getLogger(GmlFormat.class);

	private final WebFeatureService master;

	private final GmlFormatOptions options;

	private final GmlDescribeFeatureTypeHandler dftHandler;

	private final GmlGetFeatureHandler gfHandler;

	private final GmlGetPropertyValueHandler gpvHandler;

	private final GmlGetGmlObjectHandler ggoHandler;

	/**
	 * Creates a new {@link GmlFormat} instance with default behaviour for the specified
	 * GML version.
	 * @param master service that uses this format, must not be <code>null</code>
	 * @param gmlVersion GML version, must not be <code>null</code>
	 */
	public GmlFormat(WebFeatureService master, GMLVersion gmlVersion) {
		this.master = master;
		this.options = new GmlFormatOptions(gmlVersion, null, null, null, false, false, master.getQueryMaxFeatures(),
				master.getCheckAreaOfUse(), null, null, gmlVersion.getMimeType(), false, null, null,
				master.isEnableResponsePaging(), null);
		this.dftHandler = new GmlDescribeFeatureTypeHandler(this);
		this.gfHandler = new GmlGetFeatureHandler(this);
		this.gpvHandler = new GmlGetPropertyValueHandler(this);
		this.ggoHandler = new GmlGetGmlObjectHandler(this);
	}

	/**
	 * Creates a new {@link GmlFormat} instance with user-defined configuration options.
	 * @param master service that uses this format, must not be <code>null</code>
	 * @param formatDef JAXB configuration options, must not be <code>null</code>
	 * @throws ResourceInitException
	 */
	public GmlFormat(WebFeatureService master, org.deegree.services.jaxb.wfs.GMLFormat formatDef)
			throws ResourceInitException {
		this.master = master;
		final GMLVersion gmlVersion = GMLVersion.valueOf(formatDef.getGmlVersion().value());

		boolean generateBoundedByForFeatures = false, disableStreaming = false;
		if (formatDef.isGenerateBoundedByForFeatures() != null) {
			generateBoundedByForFeatures = formatDef.isGenerateBoundedByForFeatures();
		}

		QName responseContainerEl = null, responseFeatureMemberEl = null;
		String schemaLocation = null, appSchemaBaseURL = null;
		GMLSchemaInfoSet originalSchemaLocation = null;

		GetFeatureResponse responseConfig = formatDef.getGetFeatureResponse();
		boolean exportOriginalSchema = false;
		NamespaceBindings prebindNamespaces = null;
		if (responseConfig != null) {
			if (responseConfig.isDisableStreaming() != null) {
				disableStreaming = responseConfig.isDisableStreaming();
			}
			if (responseConfig.getContainerElement() != null) {
				responseContainerEl = responseConfig.getContainerElement();
			}
			if (responseConfig.getFeatureMemberElement() != null) {
				responseFeatureMemberEl = responseConfig.getFeatureMemberElement();
			}
			if (responseConfig.getAdditionalSchemaLocation() != null) {
				schemaLocation = responseConfig.getAdditionalSchemaLocation();
			}
			if (responseConfig.getDisableDynamicSchema() != null) {
				exportOriginalSchema = responseConfig.getDisableDynamicSchema().isValue();
				appSchemaBaseURL = responseConfig.getDisableDynamicSchema().getBaseURL();
				if (appSchemaBaseURL != null && appSchemaBaseURL.endsWith("/")) {
					appSchemaBaseURL = appSchemaBaseURL.substring(0, appSchemaBaseURL.length() - 1);
				}
				if (appSchemaBaseURL != null && appSchemaBaseURL.isEmpty()) {
					appSchemaBaseURL = null;
				}
			}
			if (responseConfig.getSchemaLocation() != null) {
				originalSchemaLocation = createGmlSchemaInfoSet(gmlVersion, responseConfig);
			}
			prebindNamespaces = getNamespaceBindings(responseConfig.getPrebindNamespace());
		}

		int queryMaxFeatures = master.getQueryMaxFeatures();
		boolean checkAreaOfUse = master.getCheckAreaOfUse();

		CoordinateFormatter formatter = null;
		try {
			JAXBElement<?> formatterEl = formatDef.getAbstractCoordinateFormatter();
			if (formatterEl != null) {
				Object formatterConf = formatterEl.getValue();
				if (formatterConf instanceof org.deegree.services.jaxb.wfs.DecimalCoordinateFormatter) {
					LOG.info("Setting up configured DecimalCoordinateFormatter.");
					org.deegree.services.jaxb.wfs.DecimalCoordinateFormatter decimalFormatterConf = (org.deegree.services.jaxb.wfs.DecimalCoordinateFormatter) formatterConf;
					formatter = new DecimalCoordinateFormatter(decimalFormatterConf.getPlaces().intValue());
				}
				else if (formatterConf instanceof org.deegree.services.jaxb.wfs.CustomCoordinateFormatter) {
					LOG.info("Setting up CustomCoordinateFormatter.");
					org.deegree.services.jaxb.wfs.CustomCoordinateFormatter customFormatterConf = (org.deegree.services.jaxb.wfs.CustomCoordinateFormatter) formatterConf;
					formatter = (CoordinateFormatter) Class.forName(customFormatterConf.getJavaClass()).newInstance();
				}
				else {
					LOG.warn("Unexpected JAXB type '" + formatterConf.getClass() + "'.");
				}
			}
		}
		catch (Exception e) {
			throw new ResourceInitException("Error initializing coordinate formatter: " + e.getMessage(), e);
		}

		final String mimeType = trim(formatDef.getMimeType().get(0));
		final SFSProfiler geometrySimplifier = getSfsProfiler(formatDef.getGeometryLinearization());
		this.options = new GmlFormatOptions(gmlVersion, responseContainerEl, responseFeatureMemberEl, schemaLocation,
				disableStreaming, generateBoundedByForFeatures, queryMaxFeatures, checkAreaOfUse, formatter,
				appSchemaBaseURL, mimeType, exportOriginalSchema, geometrySimplifier, prebindNamespaces,
				master.isEnableResponsePaging(), originalSchemaLocation);

		this.dftHandler = new GmlDescribeFeatureTypeHandler(this);
		this.gfHandler = new GmlGetFeatureHandler(this);
		this.gpvHandler = new GmlGetPropertyValueHandler(this);
		this.ggoHandler = new GmlGetGmlObjectHandler(this);
	}

	private NamespaceBindings getNamespaceBindings(final List<PrebindNamespace> prebindNamespaces) {
		if (prebindNamespaces == null) {
			return null;
		}
		final NamespaceBindings nsBindings = new NamespaceBindings();
		for (final PrebindNamespace prebindNamespace : prebindNamespaces) {
			nsBindings.addNamespace(prebindNamespace.getPrefix(), prebindNamespace.getUri());
		}
		return nsBindings;
	}

	private SFSProfiler getSfsProfiler(final GeometryLinearization geometryLinearization) {
		if (geometryLinearization == null) {
			return null;
		}
		final double maxError = geometryLinearization.getAccuracy();
		final MaxErrorCriterion crit = new MaxErrorCriterion(maxError, MAX_VALUE);
		return new SFSProfiler(crit);
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void doDescribeFeatureType(DescribeFeatureType request, HttpResponseBuffer response, boolean isSoap)
			throws OWSException, XMLStreamException, IOException {
		if (isSoap) {
			dftHandler.doDescribeFeatureTypeInSoap(request, response);
		}
		else {
			dftHandler.doDescribeFeatureType(request, response);
		}
	}

	@Override
	public void doGetFeature(GetFeature request, HttpResponseBuffer response) throws Exception {
		ResultType type = request.getPresentationParams().getResultType();
		if (type == RESULTS || type == null) {
			gfHandler.doGetFeatureResults(request, response);
		}
		else {
			gfHandler.doGetFeatureHits(request, response);
		}
	}

	@Override
	public void doGetGmlObject(GetGmlObject request, HttpResponseBuffer response) throws Exception {
		ggoHandler.doGetGmlObject(request, response);
	}

	@Override
	public void doGetPropertyValue(GetPropertyValue request, HttpResponseBuffer response) throws Exception {
		ResultType type = request.getPresentationParams().getResultType();
		if (type == RESULTS || type == null) {
			gpvHandler.doGetPropertyValueResult(request, response);
		}
		else {
			gpvHandler.doGetPropertyValueHits(request, response);
		}
	}

	/**
	 * Returns the service instance that uses this format.
	 * @return service that uses this format, must not be <code>null</code>
	 */
	public WebFeatureService getMaster() {
		return master;
	}

	/**
	 * Returns the configured GML output options.
	 * @return the configured options to control GML output, never <code>null</code>
	 */
	public GmlFormatOptions getGmlFormatOptions() {
		return options;
	}

	private GMLSchemaInfoSet createGmlSchemaInfoSet(GMLVersion gmlVersion, GetFeatureResponse responseConfig) {
		try {
			URL url = this.master.getMetadata().getLocation().resolveToUrl(responseConfig.getSchemaLocation());
			return new GMLSchemaInfoSet(gmlVersion, url.toString());
		}
		catch (Exception e) {
			throw new ResourceInitException("Error resolving of initializing schema location: " + e.getMessage(), e);
		}
	}

}
