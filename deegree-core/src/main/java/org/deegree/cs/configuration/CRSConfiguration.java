//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.cs.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.transformations.TransformationFactory;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CRSConfiguration</code> creates, instantiates and supplies a configured CRS-Provider. Because only one
 * crs-configuration is needed inside the JVM, this implementation uses a singleton pattern.
 * <p>
 * The configuration will try to read the file: crs_providers.properties. It uses the following strategy to load this
 * file, first the root directory (e.g. '/' or WEB-INF/classes ) will be searched. If no file was found there, it will
 * try to load from the package. The properties file must denote a property with name 'CRS_PROVIDER' followed by a '='
 * and a fully qualified name denoting the class (an instance of CRSProvider) which should be available in the
 * classpath. This class must have an empty constructor.
 * </p>
 * 
 * The class is also a command line tool for transferring CRSs between providers. Please see the main method or call the
 * class with -h. The last feature introduced was the possibility of removing CRSs from the database backend (by using
 * the -remove command-line parameter).
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "Get information on the instantiation on the appropriate providers, and the configured properties.")
public class CRSConfiguration {
    private static Logger LOG = LoggerFactory.getLogger( CRSConfiguration.class );

    private CRSProvider provider;

    private TransformationFactory transformationFactory;

    /**
     * 
     */
    public static final Map<String, CRSConfiguration> DEFINED_CONFIGURATIONS = new HashMap<String, CRSConfiguration>();

    // Load from XML file
    private static final String XML_PROVIDER = "org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider";

    // Load from DB
    // private static final String DEFAULT_PROVIDER_CLASS =
    // "org.deegree.cs.configuration.deegree.db.DatabaseCRSProvider";

    private static String CONFIGURED_DEFAULT_PROVIDER_CLASS = XML_PROVIDER;

    private final static String PROVIDER_CONFIG = "crs_providers.properties";

    private static Properties configuredProperties = null;

    static {
        configuredProperties = new Properties();
        LOG.debug( "Trying to load configured CRS provider from configuration (/crs_providers.properties)." );
        InputStream is = CRSConfiguration.class.getResourceAsStream( "/" + PROVIDER_CONFIG );
        if ( is == null ) {
            LOG.debug( "Trying to load configured CRS provider from configuration (org.deegree.cs.configuration.crs_providers.properties)." );
            is = CRSConfiguration.class.getResourceAsStream( PROVIDER_CONFIG );
        }
        if ( is == null ) {
            LOG.warn( Messages.getMessage( "CRS_CONFIG_NO_PROVIDER_DEFS_FOUND", PROVIDER_CONFIG ) );
        } else {
            try {
                configuredProperties.load( is );
                CONFIGURED_DEFAULT_PROVIDER_CLASS = configuredProperties.getProperty( "CRS_PROVIDER" );
                String crs_configuration = System.getProperty( "crs.configuration" );
                if ( crs_configuration != null && !"".equals( crs_configuration ) ) {
                    LOG.info( "Using the supplied crs.configuration property for the crs_configuration location." );
                    configuredProperties.put( "crs.configuration", crs_configuration );
                }
                configuredProperties.put( "crs.default.configuration", "deegree-crs-configuration.xml" );
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            } finally {
                try {
                    is.close();
                } catch ( IOException e ) {
                    // no output if the stream can't be closed, just leave it as it is.
                }
            }
        }
    }

    // private static CRSConfiguration CONFIG = null;

    /**
     * @param provider
     *            to get the CRS's from.
     */
    private CRSConfiguration( CRSProvider provider ) {
        this.provider = provider;
        this.transformationFactory = new TransformationFactory( provider,
                                                                DSTransform.fromProperties( configuredProperties ) );
    }

    /**
     * Creates or returns an instance of the CRSConfiguration by trying to instantiate the given provider class. If the
     * name is null or "" the Provider configured in the 'crs_providers.properties' will be returned. If the
     * instantiation of this class fails a {@link org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider} will be
     * returned.
     * 
     * @param providerName
     *            the canonical name of the class, e.g. org.deegree.cs.MyProvider
     * @return an instance of a CRS-Configuration with the configured CRSProvider.
     * @throws CRSConfigurationException
     *             if --anything-- went wrong while instantiating the CRSProvider.
     */
    public synchronized static CRSConfiguration getInstance( String providerName ) {
        String provName = null;
        if ( providerName == null || "".equals( providerName.trim() ) ) {
            provName = CONFIGURED_DEFAULT_PROVIDER_CLASS;
        } else {
            provName = providerName.trim();
        }
        LOG.debug( "Trying to find a provider for class: " + provName );
        if ( DEFINED_CONFIGURATIONS.containsKey( provName ) && DEFINED_CONFIGURATIONS.get( provName ) != null ) {
            LOG.debug( "Found a cached provider for class: " + provName );
            return DEFINED_CONFIGURATIONS.get( provName );
        }
        CRSProvider provider = null;

        if ( XML_PROVIDER.equals( provName )
             || ( CONFIGURED_DEFAULT_PROVIDER_CLASS.equals( provName ) && CONFIGURED_DEFAULT_PROVIDER_CLASS.equals( XML_PROVIDER ) ) ) {
            provider = DeegreeCRSProvider.getInstance( new Properties( configuredProperties ) );
            // provider = new DatabaseCRSProvider( configuredProperties );
        } else {
            try {
                // use reflection to instantiate the configured provider.
                Class<?> t = Class.forName( provName );
                t.asSubclass( CRSProvider.class );
                LOG.debug( "Trying to load configured CRS provider from classname: " + provName );
                Constructor<?> constructor = t.getConstructor( Properties.class );
                if ( constructor != null ) {
                    LOG.debug( "Invoking constructor: " + constructor );
                    provider = (CRSProvider) constructor.newInstance( new Properties( configuredProperties ) );
                    // provider = (CRSProvider) constructor.newInstance( configuredProperties );
                }
            } catch ( InstantiationException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", provName, e.getMessage() ), e );
            } catch ( IllegalAccessException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", provName, e.getMessage() ), e );
            } catch ( ClassNotFoundException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", provName, e.getMessage() ), e );
            } catch ( SecurityException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", provName, e.getMessage() ), e );
            } catch ( NoSuchMethodException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", provName, e.getMessage() ), e );
            } catch ( IllegalArgumentException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", provName, e.getMessage() ), e );
            } catch ( InvocationTargetException e ) {
                LOG.error( e.getCause().getMessage(), e.getCause() );
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", provName, e.getMessage() ), e );
            } catch ( Throwable t ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", provName, t.getMessage() ), t );
            } finally {
                if ( provider == null ) {
                    LOG.info( "The configured class: " + provName
                              + " was not created. Trying to create an xml based deegree-crs-provider" );
                    provider = DeegreeCRSProvider.getInstance( new Properties( configuredProperties ) );
                    // provider = new DatabaseCRSProvider( configuredProperties );
                }
            }
        }
        CRSConfiguration config = new CRSConfiguration( provider );
        DEFINED_CONFIGURATIONS.put( provName, config );
        LOG.debug( "Instantiated a new CRSConfiguration :" + config );
        return config;
    }

    /**
     * Creates or returns an instance of the CRSConfiguration by reading the DEFAULT property configured in the
     * 'crs_providers.properties'. If no key is given (or no string could be loaded), the {@link DeegreeCRSProvider}
     * will be used.
     * 
     * @return an instance of a CRS-Configuration with the configured CRSProvider.
     * @throws CRSConfigurationException
     *             if --anything-- went wrong while instantiating the CRSProvider.
     */
    public synchronized static CRSConfiguration getInstance()
                            throws CRSConfigurationException {
        return getInstance( null );
    }

    /**
     * Overwrites the crs.configuration property with the given value.
     * 
     * @param fileName
     *            to set the crs.configuration property to.
     * 
     * @return the old crs.configuration propert (if any)
     * @throws CRSConfigurationException
     *             if --anything-- went wrong while instantiating the CRSProvider.
     */
    public synchronized static String setDefaultFileProperty( String fileName ) {
        return (String) configuredProperties.setProperty( "crs.configuration", fileName );
    }

    /**
     * @return the crs provider.
     */
    public final CRSProvider getProvider() {
        return provider;
    }

    /**
     * @return a transformation factory instantiated with the provider;
     */
    public final TransformationFactory getTransformationFactory() {
        return transformationFactory;
    }

    /**
     * @return a text that specifies whether a crs is provided or not (and the canonical name of the provider, if that
     *         is the case)
     */
    @Override
    public String toString() {
        return "CRSConfiguration is using "
               + ( ( provider == null ) ? "no crs provider, this is strange."
                                       : "crs provider: " + provider.getClass().getCanonicalName() );
    }
}
