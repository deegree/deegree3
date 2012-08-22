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
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.MappingGeometryField;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.schema.GeometryPropertyType;

/**
 * Representation of property types that contain spatial data with mapping (persistence)
 * information.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappedGeometryPropertyType extends GeometryPropertyType implements MappedPropertyType {

    private boolean isIdentityPart;

    private URI srs;

    private CoordinateSystem cs;

    private TableRelation[] tableRelations;

    private MappingGeometryField mappingField;

    /**
     * Constructs a new instance of <code>MappedGeometryPropertyType</code> from the given
     * parameters.
     *
     * @param name
     * @param typeName
     * @param type
     * @param minOccurs
     * @param maxOccurs
     * @param isIdentityPart
     * @param srs
     * @param tableRelations
     * @param mappingField
     * @throws UnknownCRSException
     */
    public MappedGeometryPropertyType( QualifiedName name, QualifiedName typeName, int type,
                                      int minOccurs, int maxOccurs, boolean isIdentityPart,
                                      URI srs, TableRelation[] tableRelations,
                                      MappingGeometryField mappingField ) throws UnknownCRSException {
        super( name, typeName, type, minOccurs, maxOccurs );
        this.isIdentityPart = isIdentityPart;
        this.srs = srs;
        this.srs = srs;
        // TODO always check if this worked as expected
        this.cs = CRSFactory.create( srs.toString() );
        this.tableRelations = tableRelations;
        this.mappingField = mappingField;
    }

    /**
     * Returns whether this property has to be considered when two instances of the parent feature
     * are checked for equality.
     *
     * @return true, if this property is part of the feature's identity, false otherwise
     */
    public boolean isIdentityPart() {
        return this.isIdentityPart;
    }

    /**
     * Returns the SRS of the property's geometry content.
     *
     * @return the SRS of the property's geometry content
     */
    public URI getSRS() {
        return this.srs;
    }

    /**
     * Returns the {@link CoordinateSystem} of the property's geometry content.
     *
     * @return the coordinate system of the property's geometry content
     */
    public CoordinateSystem getCS() {
        return this.cs;
    }

    /**
     * Returns the path of <code>TableRelation</code>s that describe how to get to the table
     * where the content is stored.
     *
     * @return path of TableRelations, may be null
     */
    public TableRelation[] getTableRelations() {
        return this.tableRelations;
    }

    /**
     * Returns the {@link MappingField} that stores the geometry information.
     *
     * @return the MappingField that stores the geometry information
     */
    public MappingGeometryField getMappingField() {
        return this.mappingField;
    }
}
