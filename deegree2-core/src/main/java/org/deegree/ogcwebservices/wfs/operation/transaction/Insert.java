//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.ogcwebservices.wfs.operation.transaction;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;

/**
 * Represents an <code>Insert</code> operation as a part of a {@link Transaction} request.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Insert extends TransactionOperation {

    /**
     * Generation strategies for feature ids.
     */
    public static enum ID_GEN {

        /** Use provided feature ids. */
        USE_EXISTING,

        /** Use provided feature ids, generate new id if feature with same id already exists. */
        REPLACE_DUPLICATE,

        /** Always generate new feature ids. */
        GENERATE_NEW
    }

    /** Always generate new feature ids. */
    public static final String ID_GEN_GENERATE_NEW_STRING = "GenerateNew";

    /** Use provided feature ids. */
    public static final String ID_GEN_USE_EXISTING_STRING = "UseExisting";

    /** Use provided feature ids, generate new id if feature with same id already exists. */
    public static final String ID_GEN_REPLACE_DUPLICATE_STRING = "ReplaceDuplicate";

    private ID_GEN idGenMode;

    private URI srsName;

    private FeatureCollection fc;

    /**
     * Creates a new <code>Insert</code> instance.
     *
     * @param handle
     *            optional identifier for the operation (for error messsages)
     * @param idGenMode
     *            mode for feature id generation
     * @param srsName
     *            name of the spatial reference system
     * @param fc
     *            feature instances to be inserted, wrapped in a <code>FeatureCollection</code>
     */
    public Insert( String handle, ID_GEN idGenMode, URI srsName, FeatureCollection fc ) {
        super( handle );
        this.idGenMode = idGenMode;
        this.srsName = srsName;
        this.fc = fc;
    }

    /**
     * Returns the mode for id generation.
     * <p>
     * Must be one of the following:
     * <ul>
     * <li>ID_GEN.USE_EXISTING</li>
     * <li>ID_GEN.REPLACE_DUPLICATE</li>
     * <li>ID_GEN.GENERATE_NEW</li>
     * </ul>
     *
     * @return the mode for id generation
     */
    public ID_GEN getIdGen() {
        return this.idGenMode;
    }

    /**
     * Returns the asserted SRS of the features to be inserted.
     *
     * @return the asserted SRS of the features
     */
    public URI getSRSName() {
        return this.srsName;
    }

    /**
     * Returns the feature instances to be inserted.
     *
     * @return the feature instances to be inserted
     */
    public FeatureCollection getFeatures() {
        return this.fc;
    }

    /**
     * Sets the feature instances to be inserted.
     *
     * @param fc
     *            feature instances to be inserted
     */
    public void setFeatureCollection( FeatureCollection fc ) {
        this.fc = fc;
    }

    /**
     * Returns the names of the feature types that are affected by the operation.
     *
     * @return the names of the affected feature types
     */
    @Override
    public List<QualifiedName> getAffectedFeatureTypes() {
        Set<QualifiedName> featureTypeSet = new HashSet<QualifiedName>();
        for ( int i = 0; i < this.fc.size(); i++ ) {
            Feature feature = this.fc.getFeature( i );
            featureTypeSet.add( feature.getName() );
        }
        List<QualifiedName> featureTypes = new ArrayList<QualifiedName>( featureTypeSet );
        return featureTypes;
    }
}
