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
import static org.deegree.protocol.csw.CSWConstants.VERSION_100;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

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
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.csw.exporthandling.CapabilitiesHandler;
import org.deegree.services.csw.exporthandling.EbrimGetCapabilitiesHandler;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public class EbrimProfile implements ServiceProfile {

	private static List<String> versions = new ArrayList<String>();

	// the CSW-ebRim needs two service names because of contradictoons between OGC ebRim
	// profile for EO
	// products and its base specification.
	public static final String SERVICENAME_CSW = "CSW";

	public static final String SERVICENAME_CSW_EBRIM = "CSW-ebRIM";

	public static final String SERVICENAME_WRS = "WRS";

	public static final String RIM_NS = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";

	private static final String RIM_SCHEMA = "http://docs.oasis-open.org/regrep/v3.0/schema/rim.xsd";

	private static final String RIM_AP_NS = "http://www.opengis.net/cat/wrs/1.0";

	private static final String RIM_AP_SCHEMA = "http://schemas.opengis.net/csw/2.0.2/profiles/ebrim/1.0/csw-ebrim.xsd";

	static {
		versions.add(VERSION_100.toString());
		versions.add(VERSION_202.toString());
	}

	protected static final ImplementationMetadata<CSWRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<CSWRequestType>() {
		{
			supportedVersions = new Version[] { VERSION_100, VERSION_202 };
			handledNamespaces = new String[] { CSW_202_NS, "http://www.opengis.net/cat/csw" };
			handledRequests = CSWRequestType.class;
			serviceName = new String[] { SERVICENAME_CSW, SERVICENAME_CSW_EBRIM, SERVICENAME_WRS };
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
		return new String[] { "CSW", "CSW-ebRIM", "WRS" };
	}

	@Override
	public String getAcceptFormat(GetCapabilities getCapabilitiesRequest) throws OWSException {
		String acceptFormat;
		Set<String> af = getCapabilitiesRequest.getAcceptFormats();
		String text = "text/xml";
		if (af.isEmpty()) {
			acceptFormat = text;
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
		return new EbrimGetCapabilitiesHandler(writer, sections, identification, provider, version,
				isTransactionEnabled, extendedCapabilities);
	}

	@Override
	public QName[] getDefaultTypeNames() {
		return new QName[] { new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "RegistryPackage", "rim") };
	}

	@Override
	public URL getSchema(QName typeName) {
		return null;
	}

	@Override
	public List<URL> getSchemaReferences(QName typeName) {
		return Collections.singletonList(
				EbrimProfile.class.getResource("/org/deegree/services/csw/exporthandling/rim_schema_ref.xml"));
	}

	@Override
	public Version checkVersion(Version version) {
		if (VERSION_100.equals(version))
			return VERSION_202;
		return version;
	}

	@Override
	public boolean supportsOperation(CSWRequestType type) {
		return true;
	}

	@Override
	public String getGetRecordByIdSchemaLocation(Version version) {
		return CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA + " " + RIM_AP_NS + " " + RIM_AP_SCHEMA + " " + RIM_NS + " "
				+ RIM_SCHEMA;
	}

	@Override
	public boolean isStrict() {
		return false;
	}

	@Override
	public boolean returnAsDC(URI outputSchema) throws MetadataStoreException {
		return false;
	}

}
