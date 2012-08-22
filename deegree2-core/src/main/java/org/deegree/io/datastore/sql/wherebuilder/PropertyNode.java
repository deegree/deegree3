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
package org.deegree.io.datastore.sql.wherebuilder;

import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.TableRelation;

/**
 * Represents a {@link MappedPropertyType} as a node in a {@link QueryTableTree}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface PropertyNode {

    /**
     * Returns the {@link MappedPropertyType} that this node represents.
     *
     * @return the MappedPropertyType that this node represents
     */
    MappedPropertyType getProperty();

    /**
     * Returns the parent feature type node.
     *
     * @return the parent feature type node
     */
    FeatureTypeNode getParent();

    /**
     * Returns the table relations that lead from the parent feature type node's table to the table
     * associated with this property.
     *
     * @return the table relations that lead from the parent feature type node's table
     */
    TableRelation[] getPathFromParent();

    /**
     * Returns the aliases for the target tables in the table relations.
     *
     * @return the aliases for the target tables
     */
    String[] getTableAliases();

    /**
     * Returns an indented string representation of the object.
     *
     * @param indent
     *            current indentation (contains spaces to be prepended)
     * @return an indented string representation of the object
     */
    String toString( String indent );
}
