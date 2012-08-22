//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2006 by: M.O.S.S. Computer Grafik Systeme GmbH
 Hohenbrunner Weg 13
 D-82024 Taufkirchen
 http://www.moss.de/

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.datastore.sde;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.sdeapi.SDEConnection;
import org.deegree.model.filterencoding.Filter;

import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeState;

/**
 * Handler for <code>Delete</code> operations contained in <code>Transaction</code> requests.
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SDEDeleteHandler extends AbstractSDERequestHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( SDEDeleteHandler.class );

    /**
     * Creates a new <code>DeleteHandler</code> from the given parameters.
     * 
     * @param dsTa
     * @param aliasGenerator
     * @param conn
     */
    SDEDeleteHandler( SDETransaction dsTa, TableAliasGenerator aliasGenerator, SDEConnection conn ) {
        super( dsTa.getDatastore(), aliasGenerator, conn );
    }

    /**
     * Deletes the features from the datastore that are matched by the given filter and type.
     * 
     * @param mappedFeatureType
     * @param filter
     * @return number of deleted feature instances
     * @throws DatastoreException
     */
    int performDelete( MappedFeatureType mappedFeatureType, Filter filter )
                            throws DatastoreException {

        FeatureId[] fids = determineAffectedFIDs( mappedFeatureType, filter );
        try {
            for ( int i = 0; i < fids.length; i++ ) {
                SeDelete deleter = deleteFeature( mappedFeatureType, fids[i] );
                deleter.execute();
            }
        } catch ( Exception e ) {
            LOG.logDebug( "delete error occured", e );
            throw new DatastoreException( "delete error occured", e );
        }
        // return count of featureids deleted.
        return fids.length;
    }

    /**
     * Deletes the feature with the given feature id.
     * 
     * @param mappedFeatureType
     * @param fid
     * @throws DatastoreException
     */
    private SeDelete deleteFeature( MappedFeatureType mappedFeatureType, FeatureId fid )
                            throws Exception {

        LOG.logDebug( "Deleting feature with id '" + fid + "' and type '" + mappedFeatureType.getName() + "'..." );

        // delete feature type table
        SeDelete deleter = new SeDelete( conn.getConnection() );
        String table = mappedFeatureType.getTable();
        StringBuffer where = buildFIDWhereClause( fid );
        deleter.fromTable( table, where.toString() );
        deleter.setState( conn.getState().getId(), new SeObjectId( SeState.SE_NULL_STATE_ID ),
                          SeState.SE_STATE_DIFF_NOCHECK );
        return deleter;

    }

    /**
     * @param fid
     */
    private StringBuffer buildFIDWhereClause( FeatureId fid ) {
        StringBuffer where = new StringBuffer();
        MappingField[] fidFields = fid.getFidDefinition().getIdFields();
        for ( int i = 0; i < fidFields.length; i++ ) {
            if ( 0 != i ) {
                where.append( " AND " );
            }
            where.append( fidFields[i].getField() );
            where.append( "='" );
            where.append( fid.getValue( i ).toString() );
            where.append( "'" );
        }
        return where;
    }
}