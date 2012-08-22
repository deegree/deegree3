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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedPropertyType;

/**
 * Represents a {@link MappedFeatureType} as a node in a {@link QueryTableTree}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class FeatureTypeNode {

    // associated MappedFeatureType instance (contains the table name)
    private MappedFeatureType ft;

    // alias (may be null)
    private String ftAlias;

    // unique alias for the corresponding table
    private String tableAlias;

    private Map<MappedPropertyType, List<PropertyNode>> propertyMap = new HashMap<MappedPropertyType, List<PropertyNode>>();

    /**
     * Creates a new <code>FeatureTypeNode</code> from the given parameters.
     *
     * @param ft
     * @param ftAlias
     * @param tableAlias
     */
    FeatureTypeNode( MappedFeatureType ft, String ftAlias, String tableAlias ) {
        this.ft = ft;
        this.ftAlias = ftAlias;
        this.tableAlias = tableAlias;
    }

    /**
     * Returns the associated {@link MappedFeatureType}.
     *
     * @return associated MappedFeatureType
     */
    public MappedFeatureType getFeatureType() {
        return this.ft;
    }

    /**
     * Returns the alias as specified in the corresponding query.
     *
     * @return the alias (may be null)
     */
    public String getFtAlias() {
        return this.ftAlias;
    }

    /**
     * Returns the name of the associated table.
     *
     * @return the name of the associated table
     */
    public String getTable() {
        return this.ft.getTable();
    }

    /**
     * Returns the alias that uniquely identifies the table (in an SQL query).
     *
     * @return the unique alias for the table
     */
    public String getTableAlias() {
        return this.tableAlias;
    }

    /**
     * Returns all child {@link PropertyNode}s.
     *
     * @return all child PropertyNodes
     */
    public PropertyNode[] getPropertyNodes() {
        List<PropertyNode> propertyNodeList = new ArrayList<PropertyNode>();
        Iterator<?> iter = this.propertyMap.values().iterator();
        while ( iter.hasNext() ) {
            Iterator<?> iter2 = ( (List) iter.next() ).iterator();
            while ( iter2.hasNext() ) {
                propertyNodeList.add( (PropertyNode) iter2.next() );
            }
        }
        return propertyNodeList.toArray( new PropertyNode[propertyNodeList.size()] );
    }

    /**
     * Returns the child {@link PropertyNode}s with the given type.
     *
     * @param type
     *            the property type to look up
     * @return the child PropertyNode for the given property, may be null
     */
    public PropertyNode getPropertyNode( MappedPropertyType type ) {
        PropertyNode propertyNode = null;
        List<?> propertyNodeList = this.propertyMap.get( type );
        if ( propertyNodeList != null ) {
            Iterator<?> propertyNodeIter = propertyNodeList.iterator();
            boolean found = false;
            while ( propertyNodeIter.hasNext() ) {
                propertyNode = (PropertyNode) propertyNodeIter.next();
                if ( propertyNode.getProperty() == type ) {
                    found = true;
                    break;
                }
            }
            if ( !found ) {
                propertyNode = null;
            }
        }
        return propertyNode;
    }

    /**
     * Adds the given property node as a child.
     *
     * @param propertyNode
     *            the child node to add
     */
    public void addPropertyNode( PropertyNode propertyNode ) {
        List<PropertyNode> propertyNodeList = this.propertyMap.get( propertyNode.getProperty() );
        if ( propertyNodeList == null ) {
            propertyNodeList = new ArrayList<PropertyNode>();
            this.propertyMap.put( propertyNode.getProperty(), propertyNodeList );
        }
        propertyNodeList.add( propertyNode );
    }

    /**
     * Returns an indented string representation of the object.
     *
     * @param indent
     *            current indentation (String consisting of spaces)
     * @return an indented string representation of the object
     */
    String toString( String indent ) {
        StringBuffer sb = new StringBuffer();
        sb.append( indent );
        sb.append( "- " );
        sb.append( this.ft.getName() );
        sb.append( " (FeatureTypeNode, alias: '" );
        sb.append( this.ftAlias == null ? '-' : this.ftAlias );
        sb.append( "', table: '" );
        sb.append( this.ft.getTable() );
        sb.append( "', tableAlias: '" );
        sb.append( this.tableAlias );
        sb.append( "')\n" );
        Iterator<?> iter = this.propertyMap.values().iterator();
        while ( iter.hasNext() ) {
            List<?> propertyNodeList = (List) iter.next();
            Iterator<?> iter2 = propertyNodeList.iterator();
            while ( iter2.hasNext() ) {
                PropertyNode propertyNode = (PropertyNode) iter2.next();
                sb.append( propertyNode.toString( indent + "  " ) );
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null || ( !( obj instanceof FeatureTypeNode ) ) ) {
            return false;
        }
        FeatureTypeNode that = (FeatureTypeNode) obj;
        if ( this.getTable().equals( that.getTable() ) && this.tableAlias.equals( that.tableAlias ) ) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.tableAlias.hashCode();
    }
}
