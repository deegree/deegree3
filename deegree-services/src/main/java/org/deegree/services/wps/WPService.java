//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.services.exception.ServiceInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WPService {

    private static final Logger LOG = LoggerFactory.getLogger( WPService.class );

    private static ServiceLoader<ProcessManagerProvider> providerLoader = ServiceLoader.load( ProcessManagerProvider.class );

    private static Map<String, ProcessManagerProvider> nsToProvider = null;

    private List<ProcessManager> managers = new ArrayList<ProcessManager>();

    /**
     * Creates a new {@link WPService} instance with the given configuration.
     * 
     * @param processesDir
     *            directory to be scanned for process provider configuration documents, never <code>null</code>
     * @throws ServiceInitException
     */
    public WPService( File processesDir ) throws ServiceInitException {
        File[] fsConfigFiles = processesDir.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.toLowerCase().endsWith( ".xml" );
            }
        } );
        for ( File fsConfigFile : fsConfigFiles ) {
            String fileName = fsConfigFile.getName();
            LOG.info( "Setting up process manager from file '" + fileName + "'..." + "" );
            try {
                ProcessManager manager = create( fsConfigFile.toURI().toURL() );
                manager.init();
                managers.add( manager );
            } catch ( Exception e ) {
                LOG.error( "Error creating process manager: " + e.getMessage(), e );
            }
        }
    }

    /**
     * Returns all available {@link ProcessManagerProvider} instances.
     * 
     * @return all available providers, keys: config namespace, value: provider instance
     */
    static synchronized Map<String, ProcessManagerProvider> getProviders() {
        if ( nsToProvider == null ) {
            nsToProvider = new HashMap<String, ProcessManagerProvider>();
            try {
                for ( ProcessManagerProvider provider : providerLoader ) {
                    LOG.debug( "Process manager provider: " + provider + ", namespace: "
                               + provider.getConfigNamespace() );
                    if ( nsToProvider.containsKey( provider.getConfigNamespace() ) ) {
                        LOG.error( "Multiple manager providers for config namespace: '" + provider.getConfigNamespace()
                                   + "' on classpath -- omitting provider '" + provider.getClass().getName() + "'." );
                        continue;
                    }
                    nsToProvider.put( provider.getConfigNamespace(), provider );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }

        }
        return nsToProvider;
    }

    /**
     * Returns an uninitialized {@link ProcessManager} instance that's created from the specified process manager
     * configuration document.
     * 
     * @param configURL
     *            URL of the configuration document, must not be <code>null</code>
     * @return corresponding {@link ProcessManager} instance, not yet initialized, never <code>null</code>
     * @throws ServiceInitException
     */
    public static synchronized ProcessManager create( URL configURL )
                            throws ServiceInitException {

        String namespace = null;
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( configURL.openStream() );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
        } catch ( Exception e ) {
            String msg = "Error determining configuration namespace for file '" + configURL + "'.";
            LOG.error( msg );
            throw new ServiceInitException( msg );
        }
        LOG.debug( "Config namespace: '" + namespace + "'" );
        ProcessManagerProvider provider = getProviders().get( namespace );
        if ( provider == null ) {
            String msg = "No process manager provider for namespace '" + namespace + "' (file: '" + configURL
                         + "') registered. Skipping it.";
            LOG.error( msg );
            throw new ServiceInitException( msg );
        }
        ProcessManager manager = provider.createManager( configURL );
        return manager;
    }

    public void destroy() {
        for ( ProcessManager manager : managers ) {
            manager.destroy();
        }
    }

    public Map<CodeType, WPSProcess> getProcesses() {
        Map<CodeType, WPSProcess> processes = new HashMap<CodeType, WPSProcess>();
        for ( ProcessManager manager : managers ) {
            Map<CodeType, WPSProcess> managerProcesses = manager.getProcesses();
            if ( managerProcesses != null ) {
                processes.putAll( manager.getProcesses() );
            }
        }
        return processes;
    }

    public WPSProcess getProcess( CodeType id ) {
        WPSProcess process = null;
        for ( ProcessManager manager : managers ) {
            process = manager.getProcess( id );
            if ( process != null ) {
                break;
            }
        }
        return process;
    }
}
