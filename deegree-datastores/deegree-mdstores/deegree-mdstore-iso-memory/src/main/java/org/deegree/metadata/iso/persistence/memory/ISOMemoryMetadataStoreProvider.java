//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/iso/persistence/ISOMetadataStoreProvider.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.metadata.iso.persistence.memory;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.iso.persistence.memory.jaxb.ISOMemoryMetadataStoreConfig;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;

/**
 * {@link MetadataStoreProvider} for the {@link ISOMemoryMetadataStore}.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: 30800 $, $Date: 2011-05-12 16:49:44 +0200 (Do, 12. Mai 2011) $
 */
public class ISOMemoryMetadataStoreProvider implements MetadataStoreProvider {

    private static final Logger LOG = getLogger( ISOMemoryMetadataStoreProvider.class );

    private DeegreeWorkspace deegreeWorkspace;

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.metadata.iso.persistence.memory.jaxb";

    private final static String CONFIG_NAMESPACE = "http://www.deegree.org/datasource/metadata/iso19139/memory";

    private final static URL CONFIG_SCHEMA = ISOMemoryMetadataStore.class.getResource( "/META-INF/schemas/datasource/metadata/iso19139/memory/3.2.0/memory.xsd" );

    @Override
    public MetadataStore<ISORecord> create( URL configURL )
                            throws ResourceInitException {
        List<File> recordDirectories = new ArrayList<File>();
        File insertDirectory = null;
        try {
            ISOMemoryMetadataStoreConfig config = (ISOMemoryMetadataStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE,
                                                                                                       CONFIG_SCHEMA,
                                                                                                       configURL,
                                                                                                       deegreeWorkspace );
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );
            List<String> isoRecordDirectories = config.getISORecordDirectory();
            for ( String isoRecordDirectory : isoRecordDirectories ) {
                recordDirectories.add( new File( resolver.resolve( isoRecordDirectory ).toURI() ) );
            }
            if ( config.getInsertDirectory() != null ) {
                insertDirectory = new File( resolver.resolve( config.getInsertDirectory() ).toURI() );
            } else {
                insertDirectory = recordDirectories.get( 0 );
            }

            return new ISOMemoryMetadataStore( recordDirectories, insertDirectory );
        } catch ( Exception e ) {
            String msg = "Error setting up iso memory meatadata store from configuration: " + e.getMessage();
            LOG.error( msg, e );
            throw new ResourceInitException( msg, e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public void init( DeegreeWorkspace deegreeWorkspace ) {
        this.deegreeWorkspace = deegreeWorkspace;

    }

    @Override
    public String getConfigNamespace() {
        return CONFIG_NAMESPACE;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public String[] getCreateStatements( SQLDialect dbType )
                            throws IOException {
        return new String[0];
    }

    @Override
    public String[] getDropStatements( SQLDialect dbType )
                            throws IOException {
        return new String[0];
    }

}
