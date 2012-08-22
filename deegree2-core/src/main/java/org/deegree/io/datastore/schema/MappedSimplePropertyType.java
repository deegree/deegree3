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
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.model.feature.schema.SimplePropertyType;

/**
 * Representation of property types that contain primitive data types with mapping (persistence)
 * information.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappedSimplePropertyType extends SimplePropertyType implements MappedPropertyType {

    private boolean isIdentityPart;

    private TableRelation[] tableRelations;

    private SimpleContent content;

    /**
     * Constructs a new instance of <code>MappedSimplePropertyType</code> from the given
     * parameters.
     *
     * @param name
     * @param type
     * @param minOccurs
     * @param maxOccurs
     * @param isIdentityPart
     * @param tableRelations
     * @param content
     */
    public MappedSimplePropertyType( QualifiedName name, int type, int minOccurs, int maxOccurs,
                                    boolean isIdentityPart, TableRelation[] tableRelations,
                                    SimpleContent content ) {
        super( name, type, minOccurs, maxOccurs );
        this.isIdentityPart = isIdentityPart;
        this.tableRelations = tableRelations;
        this.content = content;
    }

    /**
     * Returns whether this property has to be considered when two instances of the parent feature
     * are checked for equality.
     *
     * @return true, if this property is part of the feature's identity
     */
    public boolean isIdentityPart() {
        return this.isIdentityPart;
    }

    /**
     * Returns the path of {@link TableRelation}s that describe how to get to the table
     * where the content is stored.
     *
     * @return path of TableRelations, may be null
     */
    public TableRelation[] getTableRelations() {
        return this.tableRelations;
    }

    /**
     * Returns the {@link SimpleContent} of the property type.
     *
     * @return the SimpleContent of the property type
     */
    public SimpleContent getContent() {
        return this.content;
    }
}
