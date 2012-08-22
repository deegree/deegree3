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
package org.deegree.io.datastore.schema;

import java.net.URI;

import org.deegree.datatypes.QualifiedName;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.idgenerator.IdGenerationException;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.schema.DefaultFeatureType;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * Represents a {@link FeatureType} with mapping (persistence) information.
 * <p>
 * The mapping information describe how the {@link FeatureType} is mapped in the database backend.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappedFeatureType extends DefaultFeatureType {

    private static final long serialVersionUID = -6091409034103779707L;

    private String table;

    private MappedGMLId gmlId;

    private MappedGMLSchema schema;

    private Datastore datastore;

    private boolean isVisible;

    private boolean isUpdatable;

    private boolean isDeletable;

    private boolean isInsertable;

    private boolean isPseudoFeatureType;

    private URI defaultSRS;

    private URI[] otherSRS;

    /**
     * Creates a new instance of <code>MappedFeatureType</code> from the given parameters.
     *
     * @param name
     * @param isAbstract
     * @param properties
     * @param table
     * @param gmlId
     * @param schema
     * @param isVisible
     * @param isUpdatable
     * @param isDeletable
     * @param isInsertable
     * @param isPseudoFeatureType
     * @param otherSRS
     * @param defaultSRS
     */
    MappedFeatureType( QualifiedName name, boolean isAbstract, PropertyType[] properties, String table,
                       MappedGMLId gmlId, MappedGMLSchema schema, boolean isVisible, boolean isUpdatable,
                       boolean isDeletable, boolean isInsertable, boolean isPseudoFeatureType, URI defaultSRS,
                       URI[] otherSRS ) {
        super( name, isAbstract, properties );
        this.table = table;
        this.gmlId = gmlId;
        this.schema = schema;
        this.datastore = schema.getDatastore();
        this.isVisible = isVisible;
        this.isUpdatable = isUpdatable;
        this.isDeletable = isDeletable;
        this.isInsertable = isInsertable;
        this.isPseudoFeatureType = isPseudoFeatureType;
        this.defaultSRS = defaultSRS;
        this.otherSRS = otherSRS;
    }

    /**
     * Returns the name of the (database) table where the feature type is stored.
     *
     * @return name of the associated table
     */
    public String getTable() {
        return this.table;
    }

    /**
     * Returns the mapping information for the "gml:Id" attribute.
     *
     * @return mapping information for the "gml:Id" attribute
     */
    public MappedGMLId getGMLId() {
        return this.gmlId;
    }

    /**
     * Generates a new and unique feature identifier.
     *
     * @param ta
     * @return a new and unique feature identifier.
     * @throws IdGenerationException
     */
    public FeatureId generateFid( DatastoreTransaction ta )
                            throws IdGenerationException {
        return this.gmlId.generateFid( this, ta );
    }

    /**
     * Returns the GML Application schema that defines this feature type.
     *
     * @return GML Application schema that defines this feature type
     */
    public MappedGMLSchema getGMLSchema() {
        return this.schema;
    }

    /**
     * Returns whether the persistent feature type is visible (e.g. queryable in the WFS).
     *
     * @return true, if the persistent feature type is visible.
     */
    public boolean isVisible() {
        return this.isVisible;
    }

    /**
     * Returns whether update operations may be performed on the persistent feature type.
     *
     * @return true, if update operations may be performed, false otherwise.
     */
    public boolean isUpdatable() {
        return this.isUpdatable;
    }

    /**
     * Returns whether delete operations may be performed on the persistent feature type.
     *
     * @return true, if delete operations may be performed, false otherwise.
     */
    public boolean isDeletable() {
        return this.isDeletable;
    }

    /**
     * Returns whether insert operations may be performed on the persistent feature type.
     *
     * @return true, if insert operations may be performed, false otherwise.
     */
    public boolean isInsertable() {
        return this.isInsertable;
    }

    /**
     * Returns whether this feature type definition is used to store complex xml data that is not a real gml feature.
     * <p>
     * If this is the case, the <code>gml:id</code> and <code>gml:boundedBy</code> elements are omitted in XML output.
     *
     * @return true, if this feature type declaration is a pseudo feature type, false otherwise
     */
    public boolean isPseudoFeatureType() {
        return this.isPseudoFeatureType;
    }

    /**
     * Returns the default SRS.
     *
     * @return the default SRS
     */
    public URI getDefaultSRS() {
        return defaultSRS;
    }

    /**
     * Returns alternative SRS that may be used to query the feature type.
     *
     * @return alternative SRS
     */
    public URI[] getOtherSRS() {
        return otherSRS;
    }

    /**
     * Performs the given {@link Query}. It must target solely this <code>MappedFeatureType</code> (joins are not
     * allowed).
     * <p>
     * All members of the resulting <code>FeatureCollection</code> have this <code>MappedFeatureType</code>.
     *
     * @param query
     *            Query to be performed
     * @return FeatureCollection with members that have this type
     * @throws DatastoreException
     * @throws UnknownCRSException
     */
    public FeatureCollection performQuery( Query query )
                            throws DatastoreException, UnknownCRSException {
        return this.datastore.performQuery( query, new MappedFeatureType[] { this } );
    }

    /**
     * Performs the given <code>Query</code> <i>inside</i> the given transaction context. It must target solely this
     * <code>MappedFeatureType</code> (joins are not allowed).
     * <p>
     * All members of the resulting <code>FeatureCollection</code> have this <code>MappedFeatureType</code>.
     *
     * @param query
     *            Query to be performed
     * @param context
     *            transaction context (used to specify the JDBCConnection, for example)
     * @return FeatureCollection with members that have this type
     * @throws DatastoreException
     * @throws UnknownCRSException
     */
    public FeatureCollection performQuery( Query query, DatastoreTransaction context )
                            throws DatastoreException, UnknownCRSException {
        return this.datastore.performQuery( query, new MappedFeatureType[] { this }, context );
    }

    /**
     * Retrieves a transaction object for this feature type.
     *
     * @return a transaction object for this feature type
     * @throws DatastoreException
     *             if transaction could not be acquired
     */
    public DatastoreTransaction acquireTransaction()
                            throws DatastoreException {
        return this.datastore.acquireTransaction();
    }

    /**
     * Returns all non-abstract feature types that may be used as substitutions for this feature type.
     *
     * @return all non-abstract feature types that may be used as substitutions
     */
    public MappedFeatureType[] getConcreteSubstitutions() {
        return this.schema.getSubstitutions( this );
    }

    /**
     * Return whether this feature type has more than one concrete substitution.
     * <p>
     * Read as: Is there only one concrete feature type that all instances of this type must have? Or are there several
     * possible concrete subtypes?
     *
     * @return true, if the feature type has more than once concrete implementations, false otherwise
     */
    public boolean hasSeveralImplementations() {
        return this.schema.hasSeveralImplementations( this );
    }
}
