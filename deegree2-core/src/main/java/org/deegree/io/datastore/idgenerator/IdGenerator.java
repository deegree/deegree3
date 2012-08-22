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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeatureType;

/**
 * Abstract base class for generators that are used to create primary keys (especially {@link FeatureId}s).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class IdGenerator {

    /** Default generator type based on UUIDs. */
    public static String TYPE_UUID = "UUID";

    private static final String BUNDLE_NAME = "org.deegree.io.datastore.idgenerator.idgenerator";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

    /** Configuration properties. */
    protected Properties params;

    /** {@link MappedFeatureType} that this generator is bound to. */
    protected MappedFeatureType ft;

    /**
     * Creates a new <code>IdGenerator</code> instance.
     *
     * @param params
     *            configuration parameters
     */
    protected IdGenerator( Properties params ) {
        this.params = params;
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
    public abstract Object getNewId( DatastoreTransaction ta )
                            throws IdGenerationException;

    /**
     * Returns a new id for a feature of the given type.
     *
     * @param ft
     *            feature type
     * @param ta
     *            datastore transaction (context)
     * @return a new feature id.
     * @throws IdGenerationException
     */
    public abstract FeatureId getNewId( MappedFeatureType ft, DatastoreTransaction ta )
                            throws IdGenerationException;

    /**
     * Returns a concrete <code>IdGenerator</code> instance which is identified by the given type code.
     *
     * @param type
     *            type code
     * @param params
     *            initialization parameters for the IdGenerator
     * @return concrete IdGenerator instance
     */
    @SuppressWarnings("unchecked")
    public static final IdGenerator getInstance( String type, Properties params ) {
        IdGenerator generator = null;
        String className = null;
        try {
            className = RESOURCE_BUNDLE.getString( type );
            Class<IdGenerator> idGeneratorClass = (Class<IdGenerator>) Class.forName( className );

            // get constructor
            Class<?>[] parameterTypes = new Class[] { Properties.class };
            Constructor<IdGenerator> constructor = idGeneratorClass.getConstructor( parameterTypes );

            // call constructor
            Object arglist[] = new Object[] { params };
            generator = constructor.newInstance( arglist );
        } catch ( InvocationTargetException e ) {
            String msg = "Could not instantiate IdGenerator with type '" + type + "': "
                         + e.getTargetException().getMessage();
            throw new RuntimeException( msg );
        } catch ( ClassNotFoundException e ) {
            String msg = "IdGenerator class '" + className + "' not found: " + e.getMessage();
            throw new RuntimeException( msg );
        } catch ( Exception e ) {
            String msg = "Could not instantiate IdGenerator with type '" + type + "': " + e.getMessage();
            throw new RuntimeException( msg );
        }
        return generator;
    }
}
