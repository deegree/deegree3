package org.deegree.metadata.iso.persistence.sql;

import java.sql.Connection;
import java.util.List;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.metadata.iso.persistence.ISOMetadataResultSet;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.filter.UnmappableException;

/**
 * Interface describing read access to sql backend.
 * 
 * @author <a href="mailto:erben@lat-lon.de">Alexander Erben</a>
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public interface QueryHelper {

    ISOMetadataResultSet execute( MetadataQuery query, Connection conn )
                            throws MetadataStoreException;

    int executeCounting( MetadataQuery query, Connection conn )
                            throws MetadataStoreException, FilterEvaluationException, UnmappableException;

    ISOMetadataResultSet executeGetRecordById( List<String> idList, Connection conn )
                            throws MetadataStoreException;

}