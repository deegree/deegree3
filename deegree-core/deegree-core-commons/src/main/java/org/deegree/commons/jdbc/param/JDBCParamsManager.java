//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.commons.jdbc.param;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.DefaultResourceManagerMetadata;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;

/**
 * Manages the {@link JDBCParams} resources in a {@link DeegreeWorkspace}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@SuppressWarnings("unchecked")
public class JDBCParamsManager extends AbstractResourceManager<JDBCParams> {

    private JDBCParamsManagerMetadata metadata;

    @Override
    public void initMetadata( DeegreeWorkspace workspace ) {
        metadata = new JDBCParamsManagerMetadata( workspace );
    }

    @Override
    public ResourceManagerMetadata<JDBCParams> getMetadata() {
        return metadata;
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[0];
    }

    @Override
    protected void add( JDBCParams params )
                            throws ResourceInitException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection( params.getUrl(), params.getUser(), params.getPassword() );
        } catch ( SQLException e ) {
            throw new ResourceInitException( e.getMessage() );
        } finally {
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    // nothing to do
                }
            }
        }
    }

    static class JDBCParamsManagerMetadata extends DefaultResourceManagerMetadata<JDBCParams> {
        JDBCParamsManagerMetadata( DeegreeWorkspace workspace ) {
            super( "jdbc params", "jdbc/", JDBCParamsProvider.class, workspace );
        }
    }
}
