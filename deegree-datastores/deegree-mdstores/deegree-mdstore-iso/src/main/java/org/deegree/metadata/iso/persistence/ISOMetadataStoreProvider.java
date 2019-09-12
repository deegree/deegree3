//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/iso/persistence/ISOMetadataStoreProvider.java $
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
package org.deegree.metadata.iso.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * {@link MetadataStoreProvider} for the {@link ISOMetadataStore}.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: lbuesching $
 * 
 * @version $Revision: 30800 $, $Date: 2011-05-12 16:49:44 +0200 (Do, 12. Mai 2011) $
 */
public class ISOMetadataStoreProvider extends MetadataStoreProvider {

    @Override
    public String[] getCreateStatements( SQLDialect dbType )
                            throws UnsupportedEncodingException, IOException {
        List<String> creates = new ArrayList<String>();
        if ( dbType.getClass().getSimpleName().equals( "MSSQLDialect" ) ) {
            URL script = ISOMetadataStoreProvider.class.getResource( "mssql/create.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
            script = ISOMetadataStoreProvider.class.getResource( "mssql/create_inspire.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        } else if ( dbType instanceof PostGISDialect ) {
            URL script = ISOMetadataStoreProvider.class.getResource( "postgis/create.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
            script = ISOMetadataStoreProvider.class.getResource( "postgis/create_inspire.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        } else if ( dbType.getClass().getSimpleName().equals( "OracleDialect" ) ) {
            URL script = ISOMetadataStoreProvider.class.getResource( "oracle/create.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
            script = ISOMetadataStoreProvider.class.getResource( "oracle/create_inspire.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        }
        return creates.toArray( new String[creates.size()] );
    }

    @Override
    public String[] getDropStatements( SQLDialect dbType )
                            throws UnsupportedEncodingException, IOException {
        List<String> creates = new ArrayList<String>();
        if ( dbType.getClass().getSimpleName().equals( "MSSQLDialect" ) ) {
            URL script = ISOMetadataStoreProvider.class.getResource( "mssql/drop_inspire.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
            script = ISOMetadataStoreProvider.class.getResource( "mssql/drop.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        } else if ( dbType instanceof PostGISDialect ) {
            URL script = ISOMetadataStoreProvider.class.getResource( "postgis/drop_inspire.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
            script = ISOMetadataStoreProvider.class.getResource( "postgis/drop.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        } else if ( dbType.getClass().getSimpleName().equals( "OracleDialect" ) ) {
            URL script = ISOMetadataStoreProvider.class.getResource( "oracle/drop.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
            script = ISOMetadataStoreProvider.class.getResource( "oracle/drop.sql" );
            creates.addAll( readStatements( new BufferedReader( new InputStreamReader( script.openStream(), "UTF-8" ) ) ) );
        }
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
    public String getNamespace() {
        return "http://www.deegree.org/datasource/metadata/iso19115";
    }

    @Override
    public ResourceMetadata<MetadataStore<? extends MetadataRecord>> createFromLocation( Workspace workspace,
                                                                                         ResourceLocation<MetadataStore<? extends MetadataRecord>> location ) {
        return new IsoMetadataStoreMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return ISOMetadataStoreProvider.class.getResource( "/META-INF/schemas/datasource/metadata/iso19115/3.4.0/iso19115.xsd" );
    }
}