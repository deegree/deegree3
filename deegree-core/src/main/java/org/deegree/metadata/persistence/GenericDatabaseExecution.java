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
package org.deegree.metadata.persistence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.metadata.persistence.iso.parsing.ParsedProfileElement;

/**
 * Interface for different kinds of implementions of executing SELECT statements against a database.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface GenericDatabaseExecution {

    String getEncoding();

    /**
     * This method executes the statement for INSERT datasets
     * 
     * @param isDC
     *            true, if a Dublin Core record should be inserted <br>
     *            <div style="text-indent:38px;">false, if an ISO record should be inserted</div>
     * @param connection
     * @param parsedElement
     *            {@link ParsedProfileElement}
     * @return an integer that is the primarykey from the inserted record, or "" if there was no inserting possible
     *         (i.e. when inserting a record twice).
     * @throws IOException
     * @throws MetadataStoreException
     */
    String executeInsertStatement( boolean isDC, Connection connection, ParsedProfileElement parsedElement )
                            throws MetadataStoreException;

    int executeDeleteStatement( Connection connection, PostGISWhereBuilder builder )
                            throws MetadataStoreException;

    /**
     * This method executes the statement for updating the queryable- and returnable properties of one specific record.
     * 
     * @param connection
     * @param updatedIds
     * @param parsedElement
     *            {@link ParsedProfileElement}
     * @throws MetadataStoreException
     */
    int executeUpdateStatement( Connection connection, ParsedProfileElement parsedElement )
                            throws MetadataStoreException;

    PreparedStatement executeGetRecords( MetadataQuery recordStoreOptions, boolean setCount,
                                         PostGISWhereBuilder builder, Connection conn )
                            throws MetadataStoreException;

}
