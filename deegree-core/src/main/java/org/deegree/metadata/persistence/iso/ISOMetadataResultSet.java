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

import java.io.BufferedInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MetadataResultSet} for the ISO Application Profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOMetadataResultSet implements MetadataResultSet {

    private static Logger LOG = LoggerFactory.getLogger( ISOMetadataResultSet.class );

    private final ResultSet rs;

    private final AnyText anyText;

    private final Connection conn;

    private final PreparedStatement stmt;

    public ISOMetadataResultSet( ResultSet rs, Connection conn, PreparedStatement stmt, AnyText anyText ) {
        this.rs = rs;
        this.conn = conn;
        this.stmt = stmt;
        this.anyText = anyText;
    }

    @Override
    public void close()
                            throws MetadataStoreException {
        JDBCUtils.close( rs, stmt, conn, LOG );
    }

    @Override
    public MetadataRecord getRecord()
                            throws MetadataStoreException {

        MetadataRecord record = null;
        try {
            BufferedInputStream bais = new BufferedInputStream( rs.getBinaryStream( 1 ) );
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( bais );
            record = new ISORecord( xmlReader, anyText );
        } catch ( Exception e ) {
            throw new MetadataStoreException( "Error re-creating MetadataRecord from result set: " + e.getMessage() );
        }
        return record;
    }

    @Override
    public boolean next()
                            throws MetadataStoreException {
        try {
            return rs.next();
        } catch ( SQLException e ) {
            throw new MetadataStoreException( e.getMessage(), e );
        }
    }

}