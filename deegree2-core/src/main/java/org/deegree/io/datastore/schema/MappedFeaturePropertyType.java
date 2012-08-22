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

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.schema.FeaturePropertyType;

/**
 * Representation of property types that contain features with mapping (persistence) information.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappedFeaturePropertyType extends FeaturePropertyType implements MappedPropertyType {

    private boolean isIdentityPart;

    private TableRelation[] tableRelations;

    private MappedFeatureTypeReference containedFT;

    private boolean isReferenceType;

    private boolean allowExternalLinks;

    /**
     * Constructs a new instance of <code>MappedFeaturePropertyType</code> from the given parameters.
     *
     * @param name
     * @param typeName
     * @param type
     * @param minOccurs
     * @param maxOccurs
     * @param isIdentityPart
     * @param tableRelations
     * @param containedFT
     * @param isReferenceType
     */
    public MappedFeaturePropertyType( QualifiedName name, QualifiedName typeName, int type, int minOccurs,
                                      int maxOccurs, boolean isIdentityPart, TableRelation[] tableRelations,
                                      MappedFeatureTypeReference containedFT, boolean isReferenceType ) {
        super( name, typeName, type, minOccurs, maxOccurs );
        this.isIdentityPart = isIdentityPart;
        this.tableRelations = tableRelations;
        this.containedFT = containedFT;
        this.isReferenceType = isReferenceType;
    }

    /**
     * @param name
     * @param typeName
     * @param type
     * @param minOccurs
     * @param maxOccurs
     * @param isIdentityPart
     * @param tableRelations
     * @param containedFT
     * @param isReferenceType
     * @param allowExternalLinks
     */
    public MappedFeaturePropertyType( QualifiedName name, QualifiedName typeName, int type, int minOccurs,
                                      int maxOccurs, boolean isIdentityPart, TableRelation[] tableRelations,
                                      MappedFeatureTypeReference containedFT, boolean isReferenceType,
                                      boolean allowExternalLinks ) {
        this( name, typeName, type, minOccurs, maxOccurs, isIdentityPart, tableRelations, containedFT, isReferenceType );
        this.allowExternalLinks = allowExternalLinks;
    }

    /**
     * Returns the {@link MappedFeatureTypeReference} to the feature type that is stored in this property.
     *
     * @return reference to the contained feature type
     */
    public MappedFeatureTypeReference getFeatureTypeReference() {
        return this.containedFT;
    }

    /**
     * Returns the path of {@link TableRelation}s that describe how to get to the table where the content is stored.
     *
     * @return path of TableRelations, may be null
     */
    public TableRelation[] getTableRelations() {
        return this.tableRelations;
    }

    /**
     * Returns whether this property has to be considered when two instances of the parent feature are checked for
     * equality.
     *
     * @return true, if this property is part of the feature's identity
     */
    public boolean isIdentityPart() {
        return this.isIdentityPart;
    }

    /**
     * Returns whether this property is of type "gml:ReferenceType" (in which case it's content must be specified by an
     * xlink:href attribute).
     *
     * @return true, if this property is of type "gml:ReferenceType", false otherwise
     */
    public boolean isReferenceType() {
        return this.isReferenceType;
    }

    /**
     * @return whether external links are allowed/stored
     */
    public boolean externalLinksAllowed() {
        return allowExternalLinks;
    }

}
