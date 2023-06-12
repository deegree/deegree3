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

import java.net.URI;
import java.net.URL;
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
import org.deegree.services.csw.describerecord.DescribeRecord;
import org.deegree.services.csw.exporthandling.CapabilitiesHandler;
import org.deegree.services.csw.exporthandling.GetCapabilitiesHandler;
import org.deegree.services.csw.getrecordbyid.GetRecordById;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;

/**
 * Bundles the differences of different CSW implemantations.
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public interface ServiceProfile {

	/**
	 * @return the {@link ImplementationMetadata} instance of the service
	 */
	ImplementationMetadata<CSWRequestType> getImplementationMetadata();

	/**
	 * @return all versions supported of the service
	 */
	List<String> getSupportedVersions();

	/**
	 * @return all service names supported of the service
	 */
	String[] getSupportedServiceNames();

	/**
	 * @param getCapabilitiesRequest
	 * @return the format accepted of the service dependent of the GetCapabilities
	 * request, never <code>null</code>. If the format os not accepted a
	 * {@link OWSException} is thrown.
	 * @throws OWSException if the format is not accepted
	 */
	String getAcceptFormat(GetCapabilities getCapabilitiesRequest) throws OWSException;

	/**
	 * TODO
	 * @param writer
	 * @param mainControllerConf
	 * @param mainConf
	 * @param sections
	 * @param identification
	 * @param version
	 * @param isTransactionEnabled
	 * @param isEnabledInspireExtension
	 * @param extendedCapabilities
	 * @return an instance of a {@link GetCapabilitiesHandler} to hanlde the a
	 * GetCapabilities request
	 */
	CapabilitiesHandler getCapabilitiesHandler(XMLStreamWriter writer, DeegreeServicesMetadataType mainControllerConf,
			DeegreeServiceControllerType mainConf, Set<Sections> sections, ServiceIdentificationType identification,
			Version version, boolean isTransactionEnabled, boolean isEnabledInspireExtension,
			ServiceProviderType provider, URL extendedCapabilities);

	/**
	 * @return the defaultTypeNames supported for a {@link DescribeRecord} request
	 */
	QName[] getDefaultTypeNames();

	/**
	 * @param typeName
	 * @return the URL of the schema assigned to the type name, <code>null</code> if no
	 * schema can be found
	 */
	URL getSchema(QName typeName);

	/**
	 * @param typeName a list of schema references assigned to the type name,
	 * <code>null</code> if no schema reference can be found
	 * @return
	 */
	List<URL> getSchemaReferences(QName typeName);

	/**
	 * Checks is the version is supported and must be handled as another version. This can
	 * be required if two version should be handled equal.
	 * @param version the version to check
	 * @return the version to use instead (often the same as the given one)
	 */
	Version checkVersion(Version version);

	/**
	 * Checks if the given {@link CSWRequestType} is supported by this service
	 * @param type
	 * @return true, if the request is supported, false otherwise
	 */
	boolean supportsOperation(CSWRequestType type);

	/**
	 * @param version
	 * @return the schemalocation for the a {@link GetRecordById} request
	 */
	String getGetRecordByIdSchemaLocation(Version version);

	/**
	 * @return true if the service handles unknown ids requestes by a
	 * {@link GetRecordById} strict, false otherwise
	 */
	boolean isStrict();

	/**
	 * Determine if the given outputSchema should be handled as DublinCore.
	 * @param outputSchema the outputsschema, can be <code>null</code>
	 * @return true, if dublinCore should be forced, false otherwise
	 * @throws MetadataStoreException
	 */
	boolean returnAsDC(URI outputSchema) throws MetadataStoreException;

}
