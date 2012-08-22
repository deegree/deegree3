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
package org.deegree.portal.cataloguemanager.control;

import java.io.IOException;
import java.sql.Connection;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.io.DBConnectionPool;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TestDBConnectionListener extends AbstractListener {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler resp )
                            throws IOException {

        String url = (String) event.getParameter().get( "url" );
        String db = (String) event.getParameter().get( "db" );
        String user = (String) event.getParameter().get( "user" );
        String password = (String) event.getParameter().get( "pw" );
        String sid = (String) event.getParameter().get( "sid" );
        String driver = null;
        String database = null;
        if ( db.equalsIgnoreCase( "postgres" )) {
            driver = "org.postgresql.Driver";
            database = "jdbc:postgresql://" + url + '/' + sid;
        } else if ( db.equalsIgnoreCase( "oracle" )) {
            driver = "oracle.jdbc.OracleDriver";
            database = "jdbc:oracle:thin:@" + url + ':' + sid;
        } else {
            resp.writeAndClose( "ERROR: not supported database type" );
            return;
        }

        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = null;
        try {
            conn = pool.acquireConnection( driver, database, user, password );
        } catch ( Exception e ) {
            resp.writeAndClose( "ERROR: " + e.getMessage() );
            return;
        } finally {
            try {
                if ( conn != null ) {
                    pool.releaseConnection( conn, driver, url, user, password );
                }
            } catch ( Exception e ) {
                // do nothing
            }
        }
        if ( doesSchemaExist( conn ) ) {
            resp.writeAndClose( false, new Boolean( true ) );
        } else {
            resp.writeAndClose( false, new Boolean( false ) );
        }
    }

    /**
     * @return
     */
    private boolean doesSchemaExist( Connection conn ) {
        return false;
    }

}
