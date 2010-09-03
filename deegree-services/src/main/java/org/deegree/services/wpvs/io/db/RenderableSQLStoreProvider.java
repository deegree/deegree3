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
package org.deegree.services.wpvs.io.db;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.rendering.r3d.jaxb.renderable.RenderableSQLStoreConfig;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;
import org.deegree.rendering.r3d.persistence.RenderableStore;
import org.deegree.rendering.r3d.persistence.RenderableStoreProvider;
import org.deegree.services.wpvs.io.ModelBackend;
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
public class RenderableSQLStoreProvider implements RenderableStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( RenderableSQLStoreProvider.class );

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/renderable/sql";
    }

    @Override
    public RenderableStore build( URL configURL ) {
        RenderableStore rs = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.rendering.r3d.jaxb.renderable" );
            Unmarshaller u = jc.createUnmarshaller();
            RenderableSQLStoreConfig config = (RenderableSQLStoreConfig) u.unmarshal( configURL );

            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );
            String connId = config.getJDBCConnId();
            Connection connection = ConnectionManager.getConnection( connId );
            connection.close();
            
            
            
            rs = new PostgisBackend( connId, (config.isIsBillboard()?ModelBackend.Type.TREE:ModelBackend.Type.BUILDING));
                // instantiate the texture dir
                List<String> tDirs = config.getTextureDirectory();
                for ( String tDir : tDirs ) {
                    if ( tDir != null ) {
                        File tD = resolveFile( tDir, resolver, false, null );
                        TexturePool.addTexturesFromDirectory( tD );
                    }
                }


        } catch ( JAXBException e ) {
            String msg = "Error in RenderableStore configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new IllegalArgumentException( msg, e );
        } catch ( SQLException e ) {
            LOG.error( e.getLocalizedMessage() );
        }
        return rs;
    }


    private File resolveFile( String fileName, XMLAdapter resolver, boolean required, String msg ) {
        URI resolve = resolveURI( fileName, resolver );
        if ( resolve == null ) {
            if ( required ) {
                throw new IllegalArgumentException( msg );
            }
            return null;
        }
        return new File( resolve );
    }

    private URI resolveURI( String fileName, XMLAdapter resolver ) {
        URI resolve = null;
        try {
            URL url = resolver.resolve( fileName );
            resolve = url.toURI();
        } catch ( MalformedURLException e ) {
            LOG.warn( "Error while resolving url for file: " + fileName + "." );
        } catch ( URISyntaxException e ) {
            LOG.warn( "Error while resolving url for file: " + fileName + "." );
        }
        return resolve;
    }
}
