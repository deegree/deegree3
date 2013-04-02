//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-datastores/deegree-featurestore/deegree-featurestore-sql/src/main/java/org/deegree/sqldialect/postgis/PostGISDialectProvider.java $
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
package org.deegree.sqldialect.postgis;

import static org.deegree.commons.jdbc.ConnectionManager.Type.PostgreSQL;
import static org.deegree.commons.utils.JDBCUtils.close;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.SQLDialectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SQLDialectProvider} for PostGIS-enabled PostgreSQL databases.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31034 $, $Date: 2011-06-09 16:47:31 +0200 (Do, 09. Jun 2011) $
 */
public class PostGISDialectProvider implements SQLDialectProvider {

    private static Logger LOG = LoggerFactory.getLogger( PostGISDialectProvider.class );

    @Override
    public Type getSupportedType() {
        return PostgreSQL;
    }

    @Override
    public SQLDialect create( String connId, DeegreeWorkspace ws )
                            throws ResourceInitException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean useLegacyPredicates = false;
        try {
            ConnectionManager mgr = ws.getSubsystemManager( ConnectionManager.class );
            conn = mgr.get( connId );
            if ( conn == null ) {
                throw new ResourceInitException( "JDBC connection " + connId + " is not available." );
            }
            useLegacyPredicates = JDBCUtils.useLegayPostGISPredicates( conn, LOG );
        } finally {
            close( rs, stmt, conn, LOG );
        }
        return new PostGISDialect( useLegacyPredicates );
    }

}
