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
package org.deegree.client.mdeditor.model;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormFieldPath implements Iterator<String> {

    private Stack<String> path = new Stack<String>();

    private int count = 0;

    public FormFieldPath( String... id ) {
        for ( int i = 0; i < id.length; i++ ) {
            path.add( id[i] );
        }
    }

    public List<String> getPath() {
        return path;
    }

    public void addStep( String step ) {
        path.add( step );
    }

    public void removeLastStep() {
        path.pop();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for ( String p : path ) {
            sb.append( p );
            if ( path.indexOf( p ) < path.size() - 1 ) {
                sb.append( '/' );
            }
        }
        return sb.toString();
    }

    @Override
    public boolean hasNext() {
        if ( count < path.size() )
            return true;
        return false;
    }

    @Override
    public String next() {
        if ( count == path.size() )
            throw new NoSuchElementException();
        return path.get( count++ );
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void resetIterator() {
        count = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if ( obj == null || !( obj instanceof FormFieldPath ) ) {
            return false;
        }
        FormFieldPath other = (FormFieldPath) obj;
        if ( other.path.size() != path.size() ) {
            return false;
        }
        for ( int i = 0; i < path.size(); i++ ) {
            if ( !path.get( i ).equals( other.path.get( i ) ) ) {
                return false;
            }
        }
        return true;
    }

}
