//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.metadata.persistence.ebrim.eo;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.metadata.ebrim.RegistryObject;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.metadata.persistence.ebrim.eo.jaxb.EbrimEOMDStoreConfig;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.slf4j.Logger;

/**
 * {@link MetadataStoreProvider} for {@link EbrimEOMDStore}s
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class EbrimEOMDStoreProvider implements MetadataStoreProvider {

    private static final Logger LOG = getLogger( MetadataStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/metadata/ebrim/eo";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.metadata.persistence.ebrim.eo.jaxb";

    private static final String CONFIG_SCHEMA = "/META-INF/schemas/datasource/metadata/ebrim/eo/3.1.0/ebrim-eo.xsd";

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public MetadataStore<RegistryObject> create( URL configUrl )
                            throws ResourceInitException {
        EbrimEOMDStoreConfig storeConfig;
        try {
            storeConfig = (EbrimEOMDStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, getConfigSchema(),
                                                                       configUrl, workspace );
        } catch ( JAXBException e ) {
            String msg = Messages.getMessage( "ERROR_IN_CONFIG_FILE", configUrl, e.getMessage() );
            LOG.error( msg );
            throw new ResourceInitException( msg, e );
        }

        XMLAdapter a = new XMLAdapter( configUrl );
        File queriesDir = null;
        String dir = null;
        try {
            dir = storeConfig.getAdhocQueriesDirectory();
            if ( dir != null ) {
                URL resolved = a.resolve( dir );
                queriesDir = new File( resolved.toURI() );
            }
        } catch ( MalformedURLException e ) {
            String msg = "Could not resolve path to the queries directory: " + dir;
            LOG.error( msg );
            throw new ResourceInitException( msg );
        } catch ( URISyntaxException e ) {
            String msg = "Could not resolve path to the queries directory: " + dir;
            LOG.error( msg );
            throw new ResourceInitException( msg );
        }

        String profile = null;
        RegistryPackage rp = null;
        profile = storeConfig.getExtensionPackage();
        Date lastModified = null;
        try {
            if ( profile != null ) {
                URL resolved = a.resolve( profile );
                File f = new File( resolved.toURI() );
                lastModified = new Date( f.lastModified() );
                XMLInputFactory inf = XMLInputFactory.newInstance();
                XMLStreamReader reader = inf.createXMLStreamReader( resolved.openStream() );
                rp = new RegistryPackage( reader );
            }
        } catch ( MalformedURLException e ) {
            String msg = "Could not resolve path to the profile: " + profile;
            LOG.error( msg );
            throw new ResourceInitException( msg );
        } catch ( XMLStreamException e ) {
            String msg = "Could not resolve profile: " + profile;
            LOG.error( msg );
            throw new ResourceInitException( msg );
        } catch ( IOException e ) {
            String msg = "Could not resolve profile: " + profile;
            LOG.error( msg );
            throw new ResourceInitException( msg );
        } catch ( URISyntaxException e ) {
            String msg = "Could not resolve path to the profile: " + profile;
            LOG.error( msg );
            throw new ResourceInitException( msg );
        }
        long queryTimeout = storeConfig.getQueryTimeout() == null ? 0 : storeConfig.getQueryTimeout().intValue();

        EbrimEOMDStore store = new EbrimEOMDStore( storeConfig.getJDBCConnId(), queriesDir, rp, lastModified,
                                                   queryTimeout );
        store.init( workspace );
        return store;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    @Override
    public URL getConfigSchema() {
        return EbrimEOMDStoreProvider.class.getResource( CONFIG_SCHEMA );
    }

    // TODO : don't copy
    private List<String> readStatements( BufferedReader reader )
                            throws IOException {
        List<String> stmts = new ArrayList<String>();
        String currentStmt = "";
        String line = null;
        while ( ( line = reader.readLine() ) != null ) {
            if ( line.startsWith( "--" ) || line.trim().isEmpty() ) {
                // skip
            } else if ( line.contains( ";" ) ) {
                currentStmt += line.substring( 0, line.indexOf( ';' ) );
                stmts.add( currentStmt );
                currentStmt = "";
            } else {
                currentStmt += line + "\n";
            }
        }
        return stmts;
    }

    @Override
    public String[] getCreateStatements( SQLDialect dbType )
                            throws UnsupportedEncodingException, IOException {
        List<String> creates = new ArrayList<String>();
        if ( dbType instanceof PostGISDialect ) {
            URL script = EbrimEOMDStoreProvider.class.getResource( "postgis/create.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        }
        return creates.toArray( new String[creates.size()] );
    }

    @Override
    public String[] getDropStatements( SQLDialect dbType )
                            throws UnsupportedEncodingException, IOException {
        List<String> creates = new ArrayList<String>();
        if ( dbType instanceof PostGISDialect ) {
            URL script = EbrimEOMDStoreProvider.class.getResource( "postgis/drop.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        }
        return creates.toArray( new String[creates.size()] );
    }

}
