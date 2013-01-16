package org.deegree.metadata.iso.persistence;

import java.sql.Connection;
import java.util.List;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.filter.UnmappableException;

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