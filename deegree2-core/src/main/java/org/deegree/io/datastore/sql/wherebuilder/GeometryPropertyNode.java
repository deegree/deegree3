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

import org.deegree.io.datastore.schema.MappedGeometryPropertyType;

/**
 * Represents a {@link MappedGeometryPropertyType} as a node in a {@link QueryTableTree}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GeometryPropertyNode extends AbstractPropertyNode {

    /**
     * Creates a new <code>GeometryPropertyNode</code> instance from the given parameters.
     *
     * @param property
     *            the property that this node represents in the query tree
     * @param parent
     *            the parent feature type node
     * @param tableAliases
     *            the aliases for the tables that lead from the parent feature type node's table to
     *            the table where the property's value is stored
     */
    GeometryPropertyNode( MappedGeometryPropertyType property, FeatureTypeNode parent, String[] tableAliases ) {
        super( property, parent, tableAliases );
    }

    @Override
    public String toString( String indent ) {
        StringBuffer sb = new StringBuffer();
        sb.append( indent );
        sb.append( "+ " );
        sb.append( this.getProperty().getName() );
        sb.append( " (GeometryPropertyNode" );
        if ( this.getTableAliases() != null ) {
            for ( int i = 0; i < this.getTableAliases().length; i++ ) {
                sb.append( " [" );
                sb.append( getPathFromParent()[i] );
                sb.append( " target alias: '" );
                sb.append( this.getTableAliases()[i] );
                sb.append( "'" );
                if ( i != this.getTableAliases().length - 1 ) {
                    sb.append( ", " );
                } else {
                    sb.append( "]" );
                }
            }
        }
        sb.append( ")\n" );
        return sb.toString();
    }
}
