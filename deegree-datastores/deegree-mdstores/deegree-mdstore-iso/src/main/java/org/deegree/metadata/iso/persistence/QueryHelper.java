package org.deegree.metadata.iso.persistence;

import java.sql.Connection;
import java.util.List;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.filter.UnmappableException;

/**
 * Executes statements that does the interaction with the underlying database. This is a PostGRES implementation.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31272 $, $Date: 2011-07-13 23:10:35 +0200 (Mi, 13. Jul 2011) $
 */
interface QueryHelper {

    /** Used to limit the fetch size for SELECT statements that potentially return a lot of rows. */
    public static final int DEFAULT_FETCH_SIZE = 100;

    public ISOMetadataResultSet execute( MetadataQuery query, Connection conn )
                            throws MetadataStoreException;

    public int executeCounting( MetadataQuery query, Connection conn )
                            throws MetadataStoreException, FilterEvaluationException, UnmappableException;

    public ISOMetadataResultSet executeGetRecordById( List<String> idList, Connection conn )
                            throws MetadataStoreException;

}