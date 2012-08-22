//$$HeadURL$$
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.deegree.io.datastore.schema.MappedFeaturePropertyType;

/**
 * Represents a {@link MappedFeaturePropertyType} as a node in a {@link QueryTableTree}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class FeaturePropertyNode extends AbstractPropertyNode {

    private Collection<FeatureTypeNode> children = new ArrayList<FeatureTypeNode> ();

    /**
     * Creates a new <code>FeaturePropertyNode</code> instance from the given parameters.
     *
     * @param property
     *            the PropertyType that this FeaturePropertyNode represents
     * @param parent
     * @param tableAliases
     *            the aliases for the tables that lead from the parent feature type node's table to
     *            the table where the property's value is stored
     */
    FeaturePropertyNode( MappedFeaturePropertyType property, FeatureTypeNode parent,
                        String[] tableAliases ) {
        super( property, parent, tableAliases );
    }

    /**
     * Returns the children of this <code>FeaturePropertyNode</code>.
     *
     * @return the children of this FeaturePropertyNode.
     */
    public FeatureTypeNode[] getFeatureTypeNodes() {
        return this.children.toArray( new FeatureTypeNode[this.children.size()] );
    }


    /**
     * Adds a new child to this <code>FeaturePropertyNode</code>.
     *
     * @param newFeatureTypeNode child node to add
     */
    public void addFeatureTypeNode( FeatureTypeNode newFeatureTypeNode ) {
        this.children.add(newFeatureTypeNode);
    }

    @Override
    public String toString( String indent ) {

        StringBuffer sb = new StringBuffer();
        sb.append( indent );
        sb.append( "+ " );
        sb.append( this.getProperty().getName() );
        sb.append( " (FeaturePropertyNode" );
        if ( this.getTableAliases() != null ) {
            for (int i = 0; i < this.getTableAliases().length; i++) {
                sb.append( " [" );
                sb.append( getPathFromParent()[i] );
                sb.append( " target alias: '" );
                sb.append( getTableAliases()[i] );
                sb.append( "'" );
                if ( i != this.getTableAliases().length - 1 ) {
                    sb.append( ", " );
                } else {
                    sb.append( "]" );
                }
            }
        }
        sb.append( ")\n" );
        Iterator<FeatureTypeNode> iter = this.children.iterator ();
        while (iter.hasNext()) {
            FeatureTypeNode child = iter.next ();
            sb.append( child.toString( indent + "  " ) );
        }
        return sb.toString();
    }
}
