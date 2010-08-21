//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/src/main/java/org/deegree/feature/persistence/postgis/FeatureBuilder.java $
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
package org.deegree.feature.persistence.postgis;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreGMLIdResolver;
import org.deegree.feature.persistence.BlobCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds {@link Feature} instances from SQL result sets for the {@link PostGISFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 25480 $, $Date: 2010-07-22 19:36:56 +0200 (Do, 22. Jul 2010) $
 */
class FeatureBuilderBlob implements FeatureBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStore.class );

    private final PostGISFeatureStore fs;

    private final BlobCodec codec;

    FeatureBuilderBlob( PostGISFeatureStore fs, BlobCodec codec ) {
        this.fs = fs;
        this.codec = codec;
    }

    /**
     * Builds a {@link Feature} instance from the current row of the given {@link ResultSet}.
     * <p>
     * The first column must be the gml id, the second column must be the data BLOB.
     * </p>
     * 
     * @param rs
     *            PostGIS result set, must not be <code>null</code>
     * @return created {@link Feature} instance, never <code>null</code>
     * @throws SQLException
     */
    @Override
    public Feature buildFeature( ResultSet rs )
                            throws SQLException {

        Feature feature = null;
        try {
            String gmlId = rs.getString( 1 );
            feature = (Feature) fs.getCache().get( gmlId );
            if ( feature == null ) {
                LOG.debug( "Cache miss. Recreating object '" + gmlId + "' from blob." );
                feature = (Feature) codec.decode( rs.getBinaryStream( 2 ), fs.getSchema(), fs.getStorageSRS(),
                                                  new FeatureStoreGMLIdResolver( fs ) );
                fs.getCache().add( feature );
            } else {
                LOG.debug( "Cache hit." );
            }
        } catch ( Exception e ) {
            String msg = "Cannot recreate feature from result set: " + e.getMessage();
            throw new SQLException( msg, e );
        }
        return feature;
    }
}
