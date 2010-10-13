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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FillProperties {

    private static Logger LOG = LoggerFactory.getLogger( FillProperties.class );

    private static final String fk_datasets = PostGISMappingsISODC.CommonColumnNames.fk_datasets.name();

    private static final StringBuilder s = new StringBuilder().append( "SELECT " ).append( "?" ).append( " FROM " ).append(
                                                                                                                            "?" ).append(
                                                                                                                                          " WHERE " ).append(
                                                                                                                                                              fk_datasets ).append(
                                                                                                                                                                                    '=' ).append(
                                                                                                                                                                                                  "?" );

    private final Connection conn;

    private final int id;

    public FillProperties( Connection conn, int id ) {
        this.conn = conn;
        this.id = id;
    }

    String[] getTitle()
                            throws MetadataStoreException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> l = new ArrayList<String>();

        try {
            ps = conn.prepareStatement( s.toString() );
            ps.setString( 0, "title" );
            ps.setString( 1, "isoqp_title" );
            ps.setInt( 2, id );

            rs = ps.executeQuery();
            while ( rs.next() ) {
                l.add( rs.getString( 1 ) );
            }
        } catch ( SQLException e ) {
            throw new MetadataStoreException( e.getMessage() );
        } finally {
            JDBCUtils.close( ps );
            JDBCUtils.close( rs );
        }

        String[] s = new String[l.size()];
        int counter = 0;
        for ( String st : l ) {
            s[counter++] = st;
        }
        return s;
    }

}
