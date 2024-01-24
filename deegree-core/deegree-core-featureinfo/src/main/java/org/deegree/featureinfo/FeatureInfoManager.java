/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.featureinfo;

import org.deegree.featureinfo.serializing.FeatureInfoGmlWriter;
import org.deegree.featureinfo.serializing.FeatureInfoSerializer;
import org.deegree.featureinfo.serializing.GeoJsonFeatureInfoSerializer;
import org.deegree.featureinfo.serializing.PlainTextFeatureInfoSerializer;
import org.deegree.featureinfo.serializing.TemplateFeatureInfoSerializer;
import org.deegree.featureinfo.serializing.XsltFeatureInfoSerializer;
import org.deegree.gml.GMLVersion;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for managing feature info output formats and their serializers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 */
public class FeatureInfoManager {

	private static final Logger LOG = getLogger(FeatureInfoManager.class);

	private final Map<String, FeatureInfoSerializer> featureInfoSerializers = new LinkedHashMap<String, FeatureInfoSerializer>();

	public FeatureInfoManager(boolean addDefaultFormats) {
		if (addDefaultFormats) {
			LOG.debug("Adding default feature info formats");

			final FeatureInfoGmlWriter gmlWriter = new FeatureInfoGmlWriter();

			featureInfoSerializers.put("application/vnd.ogc.gml", gmlWriter);
			featureInfoSerializers.put("text/xml", gmlWriter);

			featureInfoSerializers.put("text/plain", new PlainTextFeatureInfoSerializer());
			featureInfoSerializers.put("text/html", new TemplateFeatureInfoSerializer());

			for (final String version : new String[] { "2.1", "3.0", "3.1", "3.2" }) {
				featureInfoSerializers.put("application/gml+xml; version=" + version, gmlWriter);
			}

			for (final String version : new String[] { "2.1.2", "3.0.1", "3.1.1", "3.2.1" }) {
				featureInfoSerializers.put("text/xml; subtype=gml/" + version, gmlWriter);
			}
		}
	}

	public void addOrReplaceCustomFormat(String format, FeatureInfoSerializer serializer) {
		LOG.debug("Adding custom feature info format");

		featureInfoSerializers.put(format, serializer);
	}

	public void addOrReplaceFormat(String format, String file) {
		LOG.debug("Adding template feature info format");

		featureInfoSerializers.put(format, new TemplateFeatureInfoSerializer(file));
	}

	public void addOrReplaceXsltFormat(String format, URL xsltUrl, GMLVersion version, Workspace workspace) {
		LOG.debug("Adding xslt feature info format");

		XsltFeatureInfoSerializer xslt = new XsltFeatureInfoSerializer(version, xsltUrl, workspace);
		featureInfoSerializers.put(format, xslt);
	}

	public void addOrReplaceGeoJsonFormat(String format, boolean allowOtherCrsThanWGS84,
			boolean allowExportOfGeometries) {
		LOG.debug("Adding GeoJson feature info format");
		GeoJsonFeatureInfoSerializer geoJsonSerializer = new GeoJsonFeatureInfoSerializer(allowOtherCrsThanWGS84,
				allowExportOfGeometries);
		featureInfoSerializers.put(format, geoJsonSerializer);
	}

	public Set<String> getSupportedFormats() {
		return featureInfoSerializers.keySet();
	}

	public void serializeFeatureInfo(FeatureInfoParams params, FeatureInfoContext context)
			throws IOException, XMLStreamException {

		String format = params.getFormat();

		LOG.debug("Generating feature info output for format: {}", format);

		FeatureInfoSerializer serializer = featureInfoSerializers.get(format.toLowerCase());
		if (serializer != null) {
			serializer.serialize(params, context);
		}
		else {
			throw new IOException("FeatureInfo format '" + format + "' is unknown.");
		}
	}

}
