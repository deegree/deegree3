//$HeadURL$
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

import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.net.DURL;
import org.deegree.commons.xml.XMLAdapter;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JAXBUtils {

    private static final Logger LOG = getLogger( JAXBUtils.class );

    private final static SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );

    /**
     * Call this once you're done in the thread that uses jaxb (un-)marshalling.
     */
    public static void fixThreadLocalLeaks() {
          LOG.warn ("Not fixing JAXB classloader leaks. Code needs updating.");
//        try {
//            Field f = ClassFactory.class.getDeclaredField( "tls" );
//            f.setAccessible( true );
//            ( (ThreadLocal<?>) f.get( null ) ).set( null );
//            f = Coordinator.class.getDeclaredField( "activeTable" );
//            f.setAccessible( true );
//            ( (ThreadLocal<?>) f.get( null ) ).set( null );
//        } catch ( java.lang.SecurityException e ) {
//            LOG.error( "Failed to plug thread local leaks of jaxb." );
//            LOG.trace( "Stack trace:", e );
//        } catch ( NoSuchFieldException e ) {
//            LOG.error( "Failed to plug thread local leaks of jaxb." );
//            LOG.trace( "Stack trace:", e );
//        } catch ( IllegalArgumentException e ) {
//            LOG.error( "Failed to plug thread local leaks of jaxb." );
//            LOG.trace( "Stack trace:", e );
//        } catch ( IllegalAccessException e ) {
//            LOG.error( "Failed to plug thread local leaks of jaxb." );
//            LOG.trace( "Stack trace:", e );
//        }
    }

    public static Object unmarshall( String jaxbPackage, URL schemaLocation, URL url, DeegreeWorkspace workspace )
                            throws JAXBException {
        Object o = null;
        Unmarshaller u = getUnmarshaller( jaxbPackage, schemaLocation, workspace );
        try {
            o = u.unmarshal( url );
        } catch ( JAXBException e ) {
            LOG.error( "Error in configuration file: '{}'", url );
            // whyever they use the linked exception here...
            // http://www.jaxb.com/how/to/hide/important/information/from/the/user/of/the/api/unknown_xml_format.xml
            LOG.error( "Error: " + e.getLinkedException().getMessage() );
            LOG.error( "Hint: Try validating the file with an XML-schema aware editor." );
            throw e;
        } catch ( Throwable e ) {
            LOG.error( "Error in configuration file '{}': {}", url, e.getLocalizedMessage() );
            LOG.error( "Hint: Try validating the file with an XML-schema aware editor." );
        }
        return o;
    }

    /**
     * Use #unmarshall(String, URL, URL, DeegreeWorkspace) instead.
     * 
     */
    @Deprecated
    public static Object unmarshall( String jaxbPackage, String schemaLocation, XMLAdapter xmlAdapter,
                                     DeegreeWorkspace workspace )
                            throws JAXBException {
        XMLStreamReader xmlStream = xmlAdapter.getRootElement().getXMLStreamReaderWithoutCaching();
        Object o = null;
        URL schemaURL = JAXBUtils.class.getResource( schemaLocation );
        Unmarshaller u = getUnmarshaller( jaxbPackage, schemaURL, workspace );
        try {
            o = u.unmarshal( xmlStream );
        } catch ( JAXBException e ) {
            LOG.error( "Error in configuration file: '{}'", xmlAdapter.getSystemId() );
            // whyever they use the linked exception here...
            // http://www.jaxb.com/how/to/hide/important/information/from/the/user/of/the/api/unknown_xml_format.xml
            LOG.error( "Error: " + e.getLinkedException().getMessage(), e );
            LOG.error( "Hint: Try validating the file with an XML-schema aware editor." );
            throw e;
        }
        return o;
    }

    /**
     * Creates a JAXB {@link Unmarshaller} which is instantiated with the given classpath (as well as the common
     * configuration classpath). If the given schemalocation is not <code>null</code>, the unmarshaller will validate
     * against the schema file loaded from the given location.
     * 
     * @param jaxbPackage
     *            used for instantiating the unmarshaller
     * @param schemaLocation
     *            if not <code>null</code> this method will try to load the schema from location and set the validation
     *            in the unmarshaller. This location could be:
     *            "/META-INF/schemas/[SERVICE_NAME]/[VERSION]/[SERVICE_NAME]_service_configuration.xsd"
     * @return an unmarshaller which can be used to unmarshall a document with jaxb
     * @throws JAXBException
     *             if the {@link Unmarshaller} could not be created.
     */
    private static Unmarshaller getUnmarshaller( String jaxbPackage, URL schemaLocation, DeegreeWorkspace workspace )
                            throws JAXBException {

        JAXBContext jc = null;
        try {
            if ( workspace == null ) {
                jc = JAXBContext.newInstance( jaxbPackage );
            } else {
                jc = JAXBContext.newInstance( jaxbPackage, workspace.getModuleClassLoader() );
            }
        } catch ( JAXBException e ) {
            LOG.error( "Unable to instantiate JAXBContext for package '{}'", jaxbPackage );
            throw e;
        }

        Unmarshaller u = jc.createUnmarshaller();
        if ( schemaLocation != null ) {
            Schema configSchema = getSchemaForUrl( schemaLocation );
            if ( configSchema != null ) {
                u.setSchema( configSchema );
            } else {
                LOG.warn( "Not performing schema validation, because the schema could not be loaded from '{}'.",
                          schemaLocation );
            }
        }
        return u;
    }

    /**
     * Tries to load a schema file from the given location, which might be useful for the validation of configuration
     * files with JAXB.
     * 
     * @param schemaFile
     *            location like: "/META-INF/schemas/[SERVICE_NAME]/[VERSION]/[SERVICE_NAME]_service_configuration.xsd"
     * @return the schema for the given url or <code>null</code> if no schema could be loaded from the given url.
     */
    private static Schema getSchemaForUrl( URL schemaFile ) {
        Schema result = null;
        if ( schemaFile != null ) {
            try {
                StreamSource origSchema = new StreamSource( new DURL( schemaFile.toExternalForm() ).openStream(),
                                                            schemaFile.toExternalForm() );
                URL descUrl = JAXBUtils.class.getResource( "/META-INF/schemas/commons/description/3.1.0/description.xsd" );
                URL spatUrl = JAXBUtils.class.getResource( "/META-INF/schemas/commons/spatialmetadata/3.1.0/spatialmetadata.xsd" );
                StreamSource desc = new StreamSource( new DURL( descUrl.toExternalForm() ).openStream(),
                                                      descUrl.toExternalForm() );
                StreamSource spat = new StreamSource( new DURL( spatUrl.toExternalForm() ).openStream(),
                                                      spatUrl.toExternalForm() );
                result = sf.newSchema( new Source[] { origSchema, desc, spat } );
            } catch ( Throwable e ) {
                LOG.error( "No schema could be loaded from file: " + schemaFile + " because: "
                           + e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }
        return result;
    }
}
