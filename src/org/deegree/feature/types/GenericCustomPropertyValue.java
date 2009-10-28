//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.feature.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

/**
 * Allows for the representation of generic custom properties. 
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GenericCustomPropertyValue {

    private QName name;

    private Map<QName, String> attributes = new LinkedHashMap<QName, String>();

    private List<Object> childNodes = new ArrayList<Object>();

    public GenericCustomPropertyValue( QName name ) {
        this.name = name;
    }

    public GenericCustomPropertyValue( String textNode ) {
        childNodes.add( textNode );
    }

    public QName getName() {
        return name;
    }

    public void setAttribute( QName name, String value ) {
        attributes.put( name, value );
    }

    public void addChild( Object child ) {
        childNodes.add( child );
    }

    public Map<QName, String> getAttributes() {
        return attributes;
    }

    /**
     * @return all textnodes in this {@link GenericCustomPropertyValue}.
     */
    public List<String> getTextNodes() {
        List<String> result = new ArrayList<String>();
        for ( Object o : childNodes ) {
            if ( o != null ) {
                if ( o instanceof String ) {
                    result.add( (String) o );
                }
            }
        }
        return result;
    }

    public List<Object> getChildNodesAll() {
        return childNodes;
    }

    /**
     * 
     * @param qName
     *            to match
     * @return the direct childnodes of this {@link GenericCustomPropertyValue}, which are
     *         {@link GenericCustomPropertyValue} as well.
     */
    public List<GenericCustomPropertyValue> getChildNodes( QName qName ) {
        List<GenericCustomPropertyValue> result = new ArrayList<GenericCustomPropertyValue>();
        for ( Object o : childNodes ) {
            if ( o != null ) {
                if ( o instanceof GenericCustomPropertyValue ) {
                    if ( qName.equals( ( (GenericCustomPropertyValue) o ).name ) ) {
                        result.add( (GenericCustomPropertyValue) o );
                    }
                }
            }
        }
        return result;
    }

    /**
     * 
     * @return the direct childnodes of this {@link GenericCustomPropertyValue}, which are
     *         {@link GenericCustomPropertyValue} as well.
     */
    public List<GenericCustomPropertyValue> getChildNodes() {
        List<GenericCustomPropertyValue> result = new ArrayList<GenericCustomPropertyValue>();
        for ( Object o : childNodes ) {
            if ( o != null ) {
                if ( o instanceof GenericCustomPropertyValue ) {
                    result.add( (GenericCustomPropertyValue) o );
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals (Object o ) {
        if (!(o instanceof GenericCustomPropertyValue)) {
            return false;
        }
        GenericCustomPropertyValue that = (GenericCustomPropertyValue) o;
        return this.toString().equals( that.toString() );
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for ( Object o : childNodes ) {
            if ( o instanceof String ) {
                sb.append( o );
            } else if ( o instanceof GenericCustomPropertyValue ) {
                GenericCustomPropertyValue childEl = (GenericCustomPropertyValue) o;
                sb.append( "<" + childEl.name );
                for ( Entry<QName, String> attr : childEl.attributes.entrySet() ) {
                    sb.append( " " + attr.getKey() + "=\"" + attr.getValue() + "\"" );
                }
                sb.append( ">" );
                sb.append( childEl );
                sb.append( "</" + childEl.name + ">" );
            }
        }
        return sb.toString();
    }
}
