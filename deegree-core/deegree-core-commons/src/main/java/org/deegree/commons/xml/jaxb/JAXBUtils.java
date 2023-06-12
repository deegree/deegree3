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
package org.deegree.commons.xml.jaxb;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.deegree.commons.utils.net.DURL;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class JAXBUtils {

	private static final Logger LOG = getLogger(JAXBUtils.class);

	private final static SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

	public static Object unmarshall(String jaxbPackage, URL schemaLocation, InputStream input, Workspace workspace)
			throws JAXBException {
		Object o = null;
		Unmarshaller u = getUnmarshaller(jaxbPackage, schemaLocation, workspace);
		try {
			o = u.unmarshal(input);
		}
		catch (JAXBException e) {
			LOG.error("Error in configuration file: " + e.getLocalizedMessage());
			// whyever they use the linked exception here...
			// http://www.jaxb.com/how/to/hide/important/information/from/the/user/of/the/api/unknown_xml_format.xml
			if (e.getLinkedException() != null) {
				LOG.error("Error: " + e.getLinkedException().getLocalizedMessage());
			}
			LOG.error("Hint: Try validating the file with an XML-schema aware editor.");
			throw e;
		}
		catch (Exception e) {
			LOG.error("Error in configuration file: {}", e.getLocalizedMessage());
			LOG.error("Hint: Try validating the file with an XML-schema aware editor.");
		}
		return o;
	}

	/**
	 * Creates a JAXB {@link Unmarshaller} which is instantiated with the given classpath
	 * (as well as the common configuration classpath). If the given schemalocation is not
	 * <code>null</code>, the unmarshaller will validate against the schema file loaded
	 * from the given location.
	 * @param jaxbPackage used for instantiating the unmarshaller
	 * @param schemaLocation if not <code>null</code> this method will try to load the
	 * schema from location and set the validation in the unmarshaller. This location
	 * could be:
	 * "/META-INF/schemas/[SERVICE_NAME]/[VERSION]/[SERVICE_NAME]_service_configuration.xsd"
	 * @return an unmarshaller which can be used to unmarshall a document with jaxb
	 * @throws JAXBException if the {@link Unmarshaller} could not be created.
	 */
	private static Unmarshaller getUnmarshaller(String jaxbPackage, URL schemaLocation, Workspace workspace)
			throws JAXBException {

		JAXBContext jc = null;
		try {
			if (workspace == null) {
				jc = JAXBContext.newInstance(jaxbPackage);
			}
			else {
				jc = JAXBContext.newInstance(jaxbPackage, workspace.getModuleClassLoader());
			}
		}
		catch (JAXBException e) {
			LOG.error("Unable to instantiate JAXBContext for package '{}'", jaxbPackage);
			throw e;
		}

		Unmarshaller u = jc.createUnmarshaller();
		if (schemaLocation != null) {
			Schema configSchema = getSchemaForUrl(schemaLocation);
			if (configSchema != null) {
				u.setSchema(configSchema);
			}
			else {
				LOG.warn("Not performing schema validation, because the schema could not be loaded from '{}'.",
						schemaLocation);
			}
		}
		return u;
	}

	/**
	 * Tries to load a schema file from the given location, which might be useful for the
	 * validation of configuration files with JAXB.
	 * @param schemaFile location like:
	 * "/META-INF/schemas/[SERVICE_NAME]/[VERSION]/[SERVICE_NAME]_service_configuration.xsd"
	 * @return the schema for the given url or <code>null</code> if no schema could be
	 * loaded from the given url.
	 */
	private static Schema getSchemaForUrl(URL schemaFile) {
		Schema result = null;
		if (schemaFile != null) {
			try {
				StreamSource origSchema = new StreamSource(new DURL(schemaFile.toExternalForm()).openStream(),
						schemaFile.toExternalForm());
				URL descUrl = JAXBUtils.class.getResource("/META-INF/schemas/commons/description/description.xsd");
				URL spatUrl = JAXBUtils.class
					.getResource("/META-INF/schemas/commons/spatialmetadata/spatialmetadata.xsd");
				URL layUrl = JAXBUtils.class.getResource("/META-INF/schemas/layers/base/base.xsd");
				StreamSource desc = new StreamSource(new DURL(descUrl.toExternalForm()).openStream(),
						descUrl.toExternalForm());
				List<Source> list = new ArrayList<Source>();
				list.add(desc);
				if (spatUrl != null) {
					StreamSource spat = new StreamSource(new DURL(spatUrl.toExternalForm()).openStream(),
							spatUrl.toExternalForm());
					list.add(spat);
				}
				if (layUrl != null) {
					StreamSource lay = new StreamSource(new DURL(layUrl.toExternalForm()).openStream(),
							layUrl.toExternalForm());
					list.add(lay);
				}
				list.add(origSchema);
				result = sf.newSchema(list.toArray(new Source[list.size()]));
			}
			catch (Exception e) {
				LOG.error(
						"No schema could be loaded from file: " + schemaFile + " because: " + e.getLocalizedMessage());
				LOG.trace("Stack trace:", e);
			}
		}
		return result;
	}

}
