//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.model.generic.implementation.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.model.generic.Attribute;
import org.deegree.model.generic.Node;
import org.deegree.model.generic.implementation.GenericDeegreeObject;
import org.deegree.model.generic.schema.AttributeType;
import org.deegree.model.generic.schema.ContentModel;
import org.deegree.model.generic.schema.ObjectType;
import org.deegree.model.generic.schema.Occurrence;
import org.deegree.model.generic.schema.Sequence;
import org.deegree.model.generic.schema.TextType;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GenericObjectType implements ObjectType {

    private QName name;

    private List<AttributeType> attributes;

    private ContentModel contents;

    private boolean isAbstract;

    private ObjectType parentType;

    public GenericObjectType( QName name, List<AttributeType> attributes, ContentModel contents, boolean isAbstract,
                              ObjectType parentType ) {
        this.name = name;
        this.attributes = attributes;
        this.contents = contents;
        this.isAbstract = isAbstract;
        this.parentType = parentType;
    }

    public GenericObjectType( QName name, TextType textContents) {
        this.name = name;
        this.attributes = new ArrayList<AttributeType>();

        List<Occurrence<?>> occurences = new ArrayList<Occurrence<?>>();
        occurences.add( new Occurrence<TextType>(textContents));
        this.contents = new Sequence(occurences);
    }    

    public GenericObjectType( QName name, ObjectType child) {
        this.name = name;
        this.attributes = new ArrayList<AttributeType>();

        List<Occurrence<?>> occurences = new ArrayList<Occurrence<?>>();
        occurences.add( new Occurrence<ObjectType>(child));
        this.contents = new Sequence(occurences);
    }    
    
    public QName getName() {
        return name;
    }

    public List<AttributeType> getAttributes() {
        return attributes;
    }

    public ContentModel getContents() {
        return contents;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public ObjectType getParentType() {
        return parentType;
    }

    @Override
    public String toString() {
        return toString( "" );
    }

    public GenericDeegreeObject createInstance() {
        return new GenericDeegreeObject(this);
    }    
    
    public GenericDeegreeObject createInstance(List<Attribute> attributes, List<Node> contents ) {
        return new GenericDeegreeObject(this, attributes, contents);
    }    
    
    public String toString( String indent ) {
        String s = indent + "- object type: " + name.toString() + ", abstract: " + isAbstract + ", parent type: ";
        if (parentType != null) {
            s += "'" + parentType.getName() + "'\n";
        } else {
            s += "-\n";
        }

        for ( AttributeType attribute : attributes ) {
            s += indent + "  - " + attribute + "\n";
        }
        if ( contents != null ) {
            s += contents.toString( indent + "  " );
        }
        return s;
    }
}
