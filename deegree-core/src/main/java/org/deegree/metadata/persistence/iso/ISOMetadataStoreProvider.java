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
package org.deegree.metadata.persistence.iso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MetadataStoreProvider} for the {@link ISOMetadataStore}.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOMetadataStoreProvider implements MetadataStoreProvider {
    private static Logger LOG = LoggerFactory.getLogger( ISOMetadataStoreProvider.class );

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/metadata/iso19115";
    }

    @Override
    public URL getConfigSchema() {
        return ISOMetadataStoreProvider.class.getResource( "/META-INF/schemas/datasource/metadata/iso19115/0.6.0/iso19115.xsd" );
    }

    @Override
    public URL getConfigTemplate() {
        return ISOMetadataStoreProvider.class.getResource( "/META-INF/schemas/datasource/metadata/iso19115/0.6.0/example_iso19115.xml" );
    }

    @Override
    public String[] getCreateStatements( URL configURL )
                            throws UnsupportedEncodingException, IOException {
        List<String> creates = new ArrayList<String>();
        URL script = ISOMetadataStoreProvider.class.getResource( "postgis/create.sql" );
        creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        script = ISOMetadataStoreProvider.class.getResource( "postgis/create_inspire.sql" );
        creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        return creates.toArray( new String[creates.size()] );
    }

    @Override
    public String[] getDropStatements( URL configURL )
                            throws UnsupportedEncodingException, IOException {
        List<String> creates = new ArrayList<String>();
        URL script = ISOMetadataStoreProvider.class.getResource( "postgis/drop_inspire.sql" );
        creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        script = ISOMetadataStoreProvider.class.getResource( "postgis/drop.sql" );
        creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        return creates.toArray( new String[creates.size()] );
    }

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
    public MetadataStore getMetadataStore( URL configURL )
                            throws MetadataStoreException {
        ISOMetadataStoreConfig config = null;
        ISOMetadataStore store = null;
        if ( configURL == null ) {
            LOG.warn( "No metadata store configuration found!" );
        } else {
            try {
                JAXBContext jc = JAXBContext.newInstance( "org.deegree.metadata.persistence.iso19115.jaxb" );
                Unmarshaller u = jc.createUnmarshaller();
                config = (ISOMetadataStoreConfig) u.unmarshal( configURL );
                store = new ISOMetadataStore( config );
            } catch ( JAXBException e ) {
                String msg = "Error in metadata store configuration file '" + configURL + "': " + e.getMessage();
                LOG.error( msg );
                throw new MetadataStoreException( msg, e );
            }
        }
        return store;
    }
}