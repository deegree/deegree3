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

package org.deegree.framework.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The interface defines the access to a list of paramters that can be used as submitted parameters to methods that may
 * receive an variable list of parameters.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$ $Date$
 */
final public class ParameterList {

    private HashMap<String, Parameter> params = new HashMap<String, Parameter>();

    private ArrayList<String> keys = new ArrayList<String>( 100 );

    /**
     * returns the parameter that matches the submitted name. if no parameter can be found <tt>null</tt> will be
     * returned.
     *
     * @param name
     * @return name parameter
     */
    public Parameter getParameter( String name ) {
        return params.get( name );
    }

    /**
     * adds a new <tt>Parameter</tt> to the list
     *
     * @param name
     * @param value
     *
     */
    public void addParameter( String name, Object value ) {
        Parameter p = new Parameter( name, value );
        addParameter( p );
    }

    /**
     * adds a new <tt>Parameter</tt> to the list
     *
     * @param param
     */
    public void addParameter( Parameter param ) {
        params.put( param.getName(), param );
        keys.add( param.getName() );
    }

    /**
     * returns all <tt>Parameter</tt>s contained within the list as array. it is guarenteered that the arrays isn't
     * <tt>null</tt>
     *
     * @return returns an array with all Parameters from the list.
     */
    public Parameter[] getParameters() {
        Parameter[] p = new Parameter[keys.size()];
        int i = 0;
        for ( String key : keys ) {
            p[i++] = params.get( key );
        }
        return p;
    }

    /**
     * returns an array of all <tt>Parameter</tt>s names. it is guarenteered that the arrays isn't <tt>null</tt>
     *
     * @return parameter names
     */
    public String[] getParameterNames() {
        String[] s = new String[keys.size()];
        return keys.toArray( s );
    }

    /**
     * returns an array of all <tt>Parameter</tt>s values. it is guarenteered that the arrays isn't <tt>null</tt>
     *
     * @return parameter values
     */
    public Object[] getParameterValues() {
        Object[] o = new Object[keys.size()];
        for ( int i = 0; i < o.length; i++ ) {
            Parameter p = params.get( keys.get( i ) );
            o[i] = p.getValue();
        }
        return o;
    }

    /**
     * removes a parameter from the list
     *
     * @param name
     *            name of the parameter
     * @return nemd parameter
     */
    public Parameter removeParameter( String name ) {
        keys.remove( name );
        return params.remove( name );
    }

    /**
     * @return string representation
     */
    @Override
    public String toString() {
        String ret = null;
        ret = "params = " + params + "\n";
        return ret;
    }

}
