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

import javax.xml.namespace.QName;

import org.deegree.model.generic.schema.StructuredObjectType;

/**
 * Interface for all structured data objects in deegree.
 * <p>
 * Some properties of the concept of a <code>StructuredObject</code>:
 * <ul>
 * <li>Designed to match the concept of an XML element, but leaves out the cumbersome complexities (many different node
 * types, namespace bindings, etc.).</li>
 * <li>Has a {@link QName}.</li>
 * <li>May have attributes (represented as {@link AttributeNode}s.)</li>
 * <li>Has a sequence of contained {@link ObjectNode}s: {@link StructuredObject}s and/or {@link ValueNode}s.</li>
 * <li>Is the base for more specialized (and more efficient) data models (e.g. the {@link Feature} model).</li>
 * </ul>
 * 
 * @see Feature
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface StructuredObject extends ObjectNode {

    /**
     * Returns the type information (think: schema) of the <code>StructuredObject</code>.
     * 
     * @return the type information
     */
    public StructuredObjectType getType();

    /**
     * Returns the name of the <code>StructuredObject</code>.
     * 
     * @return the name
     */
    public QName getName();

    /**
     * Returns the attributes of the <code>StructuredObject</code>.
     * 
     * @return the attributes
     */
    public List<AttributeNode> getAttributes();

    public void addAttribute(AttributeNode attribute);
    
    /**
     * Returns the contained nodes (each entry is either a {@link ValueNode} or a {@link StructuredObject}).
     * <p>
     * NOTE: Attribute nodes are not considered to be contents and are not returned in the list.
     * 
     * @return the contained nodes
     */
    public List<ObjectNode> getContents();

    public void addContent (StructuredObject content);

    public void addContent (ValueNode content);
}
