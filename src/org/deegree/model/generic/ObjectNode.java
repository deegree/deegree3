//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.generic;

import java.util.List;

import javax.xml.xpath.XPath;

import org.deegree.model.generic.schema.ObjectNodeType;

/**
 * Base interface for all nodes that may occur in a {@link StructuredObject} hierarchy.
 * <p>
 * Each {@link ObjectNode} has:
 * <ul>
 * <li>type information (think: schema)</li>
 * <li>a parent {@link StructuredObject} (may be null)</li>
 * </ul>
 * <p>
 * There are three different types of nodes:
 * <ul>
 * <li>{@link StructuredObject}: has a name, attributes, and contents (this is a mixed sequence of
 * {@link StructuredObject} and {@link ValueNode} objects)</li>
 * <li>{@link AttributeNode}: an attribute of a {@link StructuredObject}</li>
 * <li>{@link ValueNode}: (partial) contents of a {@link StructuredObject}</li>
 * </ul>
 * 
 * @see StructuredObject
 * @see AttributeNode
 * @see ValueNode
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface ObjectNode {

    /**
     * Returns the type information (think: schema) of the <code>ObjectNode</code>.
     * 
     * @return the type information
     */
    public ObjectNodeType getType();

    /**
     * Returns the parent <code>StructuredObject</code> of the hierarchy (null if there is no parent).
     * 
     * @return the parent or null if there is none
     */
    public StructuredObject getParent();

    /**
     * Evaluates the given {@link XPath} against this <code>ObjectNode</code> and returns all matching nodes.
     * 
     * @param xpath
     *            expression to be matched
     * @return matching nodes
     */
    public List<ObjectNode> evaluateXPath( XPath xpath );
}
