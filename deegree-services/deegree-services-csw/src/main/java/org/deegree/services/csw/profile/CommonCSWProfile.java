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
package org.deegree.services.csw.profile;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.GMD_NS;
import static org.deegree.protocol.csw.CSWConstants.GMD_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.OutputSchema;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.csw.exporthandling.CapabilitiesHandler;
import org.deegree.services.csw.exporthandling.GetCapabilitiesHandler;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public class CommonCSWProfile implements ServiceProfile {

	private static final Logger LOG = getLogger(CommonCSWProfile.class);

	private static List<String> versions = new ArrayList<String>();

	static {
		versions.add(VERSION_202.toString());
	}

	protected static final ImplementationMetadata<CSWRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<CSWRequestType>() {
		{
			supportedVersions = new Version[] { VERSION_202 };
			handledNamespaces = new String[] { CSW_202_NS };
			handledRequests = CSWRequestType.class;
			serviceName = new String[] { "CSW" };
		}
	};

	@Override
	public List<String> getSupportedVersions() {
		return versions;
	}

	@Override
	public ImplementationMetadata<CSWRequestType> getImplementationMetadata() {
		return IMPLEMENTATION_METADATA;
	}

	@Override
	public String[] getSupportedServiceNames() {
		return new String[] { "CSW" };
	}

	@Override
	public String getAcceptFormat(GetCapabilities getCapabilitiesRequest) throws OWSException {
		String acceptFormat;
		Set<String> af = getCapabilitiesRequest.getAcceptFormats();
		String application = "application/xml";
		String text = "text/xml";
		if (af.isEmpty()) {
			acceptFormat = text;
		}
		else if (af.contains(application)) {
			acceptFormat = application;
		}
		else if (af.contains(text)) {
			acceptFormat = text;
		}
		else {
			throw new OWSException("Format determination failed. Requested format is not supported by this CSW.",
					OWSException.INVALID_FORMAT);
		}
		return acceptFormat;
	}

	@Override
	public CapabilitiesHandler getCapabilitiesHandler(XMLStreamWriter writer,
			DeegreeServicesMetadataType mainControllerConf, DeegreeServiceControllerType mainConf,
			Set<Sections> sections, ServiceIdentificationType identification, Version version,
			boolean isTransactionEnabled, boolean isEnabledInspireExtension, ServiceProviderType provider,
			URL extendedCapabilities) {
		return new GetCapabilitiesHandler(writer, mainControllerConf, mainConf, sections, identification, version,
				isTransactionEnabled, isEnabledInspireExtension, extendedCapabilities);
	}

	@Override
	public QName[] getDefaultTypeNames() {
		return new QName[] { new QName(CSW_202_NS, "DublinCore", CSW_PREFIX),
				new QName(GMD_NS, "MD_Metadata", GMD_PREFIX) };
	}

	@Override
	public URL getSchema(QName typeName) {
		if (OutputSchema.determineByTypeName(typeName) == OutputSchema.DC) {
			try {
				return new URL(CSWConstants.CSW_202_RECORD);
			}
			catch (MalformedURLException e) {
				LOG.info("Could not resolve URL " + CSWConstants.CSW_202_RECORD);
			}
		}
		return null;
	}

	@Override
	public List<URL> getSchemaReferences(QName typeName) {
		if (OutputSchema.determineByTypeName(typeName) == OutputSchema.DC) {
			return Collections.singletonList(
					CommonCSWProfile.class.getResource("/org/deegree/services/csw/exporthandling/dublinCore.xml"));
		}
		else if (OutputSchema.determineByTypeName(typeName) == OutputSchema.ISO_19115) {
			List<URL> schemas = new ArrayList<URL>();
			schemas.add(CommonCSWProfile.class.getResource("/org/deegree/services/csw/exporthandling/iso_data.xml"));
			schemas.add(CommonCSWProfile.class.getResource("/org/deegree/services/csw/exporthandling/iso_service.xml"));
			return schemas;
		}
		return null;
	}

	@Override
	public Version checkVersion(Version version) {
		return version;
	}

	@Override
	public boolean supportsOperation(CSWRequestType type) {
		return CSWRequestType.GetRepositoryItem.equals(type) ? false : true;
	}

	@Override
	public String getGetRecordByIdSchemaLocation(Version version) {
		if (VERSION_202.equals(version)) {
			return CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA;
		}
		return null;
	}

	@Override
	public boolean isStrict() {
		return true;
	}

	@Override
	public boolean returnAsDC(URI outputSchema) throws MetadataStoreException {
		if (outputSchema != null && outputSchema.equals(OutputSchema.determineOutputSchema(OutputSchema.ISO_19115))) {
			return false;
		}
		return true;
	}

}
