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
package org.deegree.io.datastore.idgenerator;

import java.util.Properties;

import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeatureType;

/**
 * {@link IdGenerator} that takes the {@link FeatureId} of the parent feature.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ParentIDGenerator extends IdGenerator {

    /**
     * Creates a new <code>ParentIDGenerator</code> instance.
     *
     * @param params
     *            configuration parameters
     */
    public ParentIDGenerator (Properties params) {
        super (params);
    }

    /**
     * Returns a new primary key.
     *
     * @param ta
     *            datastore transaction (context)
     * @return a new primary key.
     * @throws IdGenerationException
     *             if the generation of the id could not be performed
     */
    @Override
    public String getNewId( DatastoreTransaction ta ) throws IdGenerationException {
        throw new UnsupportedOperationException(
            "ParentIDGenerator cannot be used to generate primary keys (that are no feature ids)." );
    }

    /**
     * Returns a new id for a feature of the given type.
     *
     * @param ft
     *            (mapped) feature type (irrelevant for this generator)
     * @param ta
     *            datastore transaction (context)
     * @return a new feature id.
     * @throws IdGenerationException
     */
    @Override
    public FeatureId getNewId(  MappedFeatureType ft, DatastoreTransaction ta ) throws IdGenerationException {
        throw new UnsupportedOperationException(
            "ParentIDGenerator cannot be used to generate feature ids (without information on the parent feature." );
    }
}
