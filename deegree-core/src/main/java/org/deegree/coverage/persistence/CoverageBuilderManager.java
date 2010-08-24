//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.coverage.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.coverage.AbstractCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CoverageBuilderManager {

    private static final Logger LOG = LoggerFactory.getLogger( CoverageBuilderManager.class );

    private static ServiceLoader<CoverageBuilder> covBuilderLoader = ServiceLoader.load( CoverageBuilder.class );

    private static Map<String, CoverageBuilder> nsToBuilder = new ConcurrentHashMap<String, CoverageBuilder>();

    private Map<String, AbstractCoverage> idToCov = Collections.synchronizedMap( new HashMap<String, AbstractCoverage>() );

    private File coverageConfigLocation;

    /**
     * Load all available {@link CoverageBuilder}s
     * 
     */
    static {
        try {
            for ( CoverageBuilder builder : covBuilderLoader ) {
                if ( builder != null ) {
                    LOG.debug( "Service loader found CoverageBuilder: " + builder + ", namespace: "
                               + builder.getConfigNamespace() );
                    if ( nsToBuilder.containsKey( builder.getConfigNamespace() ) ) {
                        LOG.error( "Multiple coverage builders for config namespace: '" + builder.getConfigNamespace()
                                   + "' on classpath -- omitting provider '" + builder.getClass().getName() + "'." );
                        continue;
                    }
                    nsToBuilder.put( builder.getConfigNamespace(), builder );
                }
            }
        } catch ( Exception e ) {
            LOG.error( e.getMessage(), e );
        }
    }

    /**
     * @param coverageConfigLocation
     */
    public CoverageBuilderManager( File coverageConfigLocation ) {
        this.coverageConfigLocation = coverageConfigLocation;
    }

    /**
     * Initializes the {@link CoverageBuilderManager} by loading all coverage configurations from the given directory
     * (given while constructing).
     * 
     */
    public void init() {
        if ( coverageConfigLocation == null || !coverageConfigLocation.exists() ) {
            LOG.info( "No 'datasources/coverage' directory -- skipping initialization of coverage stores." );
            return;
        }
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up coverages from '{}'", coverageConfigLocation );
        LOG.info( "--------------------------------------------------------------------------------" );
        if ( coverageConfigLocation == null ) {
            LOG.warn( "The coverage config location may not be null." );
            return;
        }
        if ( !coverageConfigLocation.exists() ) {
            LOG.warn( "The given coverage config location does not exist: " + coverageConfigLocation.getAbsolutePath() );
            return;
        }
        File[] csConfigFiles = null;
        if ( coverageConfigLocation.isFile() ) {
            String ext = FileUtils.getFileExtension( coverageConfigLocation );
            if ( "xml".equalsIgnoreCase( ext ) ) {
                csConfigFiles = new File[] { coverageConfigLocation };
            }
        } else {
            csConfigFiles = coverageConfigLocation.listFiles( new FilenameFilter() {
                @Override
                public boolean accept( File dir, String name ) {
                    return name.toLowerCase().endsWith( ".xml" );
                }
            } );
        }
        if ( csConfigFiles == null ) {
            LOG.warn( "Did not find any coverage configuration files in directory: "
                      + coverageConfigLocation.getAbsolutePath() + " no global coverage will be available." );
            return;
        }
        for ( File csConfigFile : csConfigFiles ) {
            if ( csConfigFile != null ) {
                String csId = FileUtils.getFilename( csConfigFile );
                LOG.info( "Setting up coverage '" + csId + "' from file '" + csConfigFile.getName() + "'..." + "" );
                if ( idToCov.containsKey( csId ) ) {
                    String msg = "Duplicate definition of feature store with id '" + csId + "'.";
                    LOG.warn( msg );
                } else {
                    try {
                        AbstractCoverage cs = create( csConfigFile.toURI().toURL() );
                        if ( cs != null ) {
                            LOG.info( "Registering global coverage with id '" + csId + "', type: '"
                                      + cs.getClass().getCanonicalName() + "'" );
                            idToCov.put( csId, cs );
                        } else {
                            LOG.info( "Coverage with id '" + csId + "', could not be loaded (null)." );
                        }
                    } catch ( Exception e ) {
                        LOG.error( "Error initializing coverage builder: " + e.getMessage(), e );
                    }
                }
            }
        }
        LOG.info( "" );
    }

    /**
     * Returns the {@link AbstractCoverage} instance with the specified identifier.
     * 
     * @param id
     *            identifier of the coverage instance
     * @return the corresponding {@link AbstractCoverage} instance or null if no such instance has been created
     */
    public AbstractCoverage get( String id ) {
        return idToCov.get( id );
    }

    /**
     * Returns all active {@link AbstractCoverage}s.
     * 
     * @return the {@link AbstractCoverage}s instance, may be empty but never <code>null</code>
     */
    public Collection<AbstractCoverage> getAll() {
        return idToCov.values();
    }

    /**
     * Returns an uninitialized {@link AbstractCoverage} instance from the 'coverage' configuration document.
     * <p>
     * If the configuration specifies an identifier, the instance is also registered as global {@link AbstractCoverage}.
     * </p>
     * 
     * @param configURL
     *            URL of the configuration document, must not be <code>null</code>
     * @return corresponding {@link AbstractCoverage} instance, initialized and ready to be used
     * @throws IOException
     *             if the building of the coverage failed.
     * @throws CoverageBuilderException
     *             if the creation fails, e.g. due to a configuration error
     */
    private static synchronized AbstractCoverage create( URL configURL )
                            throws CoverageBuilderException, IOException {

        String namespace = null;
        try {
            InputStream is = configURL.openStream();
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( is );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
            is.close();
        } catch ( Exception e ) {
            String msg = "Error determining configuration namespace for file '" + configURL + "'";
            LOG.error( msg );
            throw new CoverageBuilderException( msg );
        }
        LOG.debug( "Config namespace: '" + namespace + "'" );
        CoverageBuilder builder = nsToBuilder.get( namespace );
        if ( builder == null ) {
            String msg = "No coverage builder for namespace '" + namespace + "' (file: '" + configURL
                         + "') registered. Skipping it.";
            LOG.error( msg );
            throw new CoverageBuilderException( msg );
        }
        return builder.buildCoverage( configURL );
    }

    /**
     * 
     */
    public void destroy () {
        idToCov.clear();
    }

}
