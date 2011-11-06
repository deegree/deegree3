//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql.insert;

import java.util.List;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.protocol.wfs.transaction.IDGenMode;

/**
 * A placeholder for the id of a {@link Feature} during an insert operation of a {@link FeatureStoreTransaction}.
 * <p>
 * The final value of the inserted id is usually not known during construction of the object (e.g. when
 * {@link IDGenMode#GENERATE_NEW} is used).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InsertFID {

    private final String originalId;

    private String newId;

    private FIDMapping fidMapping;

    /**
     * Creates a new {@link InsertFID} instance.
     * 
     * @param originalId
     *            original id of the feature (before id generation), must not be <code>null</code>
     */
    InsertFID( String originalId ) {
        this.originalId = originalId;
    }

    /**
     * Returns the original id of the {@link Feature}.
     * 
     * @return original id, can be <code>null</code> (anonymous)
     */
    public String getOriginalId() {
        return originalId;
    }

    /**
     * Returns the new id that's been assigned to the {@link Feature} by id generation.
     * 
     * @return new id, can be <code>null</code> (not assigned yet)
     */
    public String getNewId() {
        return newId;
    }

    void setFIDMapping( FIDMapping fidMapping ) {
        this.fidMapping = fidMapping;
    }
    
    void assign( InsertNode featureRow )
                            throws FeatureStoreException {
        newId = fidMapping.getPrefix();
        List<Pair<SQLIdentifier, BaseType>> fidColumns = fidMapping.getColumns();
        newId += checkFIDParticle( featureRow, fidColumns.get( 0 ).first );
        for ( int i = 1; i < fidColumns.size(); i++ ) {
            newId += fidMapping.getDelimiter() + checkFIDParticle( featureRow, fidColumns.get( i ).first );
        }
    }

    private Object checkFIDParticle( InsertNode featureRow, SQLIdentifier column )
                            throws FeatureStoreException {
        Object value = featureRow.get( column );
        if ( value == null ) {
            throw new FeatureStoreException( "FIDMapping error: No value for feature id column '" + column + "'." );
        }
        return value;
    }
}
