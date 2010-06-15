//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.client.mdeditor.model.mapping;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.xml.NamespaceContext;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MappingInformation {

    private String id;

    private String name;

    private String version;

    private String describtion;

    private List<MappingElement> mappingElements = new ArrayList<MappingElement>();

    private String schema;

    private NamespaceContext nsContext = new NamespaceContext();;

    public MappingInformation( String id, String name, String version, String describtion, String schema ) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.describtion = describtion;
        this.schema = schema;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion( String version ) {
        this.version = version;
    }

    public String getDescribtion() {
        return describtion;
    }

    public void setDescribtion( String describtion ) {
        this.describtion = describtion;
    }

    public void setSchema( String schema ) {
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    public void addMappingElement( MappingElement mappingElement ) {
        mappingElements.add( mappingElement );
    }

    public List<MappingElement> getMappingElements() {
        return mappingElements;
    }

    @Override
    public String toString() {
        return "ID: " + id + ( ( name != null ) ? "; Name: " + name : "" )
               + ( ( version != null ) ? "; Version: " + version : "" ) + "; Schema: " + schema;
    }

    public String getLabel() {
        String v = version != null ? "(" + version + ")" : "";
        if ( name == null ) {
            return id + v;
        }
        return name + v;
    }

    public void setNsContext( NamespaceContext nsContext ) {
        this.nsContext = nsContext;
    }

    public NamespaceContext getNsContext() {
        return nsContext;
    }

}
