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

// $Id$
package org.deegree.enterprise.control;

// JDK 1.3
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author <a href="mailto:tfriebe@gmx.net">Torsten Friebe</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @version $Revision$
 */
public class WebEvent extends EventObject implements FormEvent {
    /**
     *
     */
    private static final long serialVersionUID = 706936738051288390L;

    /**
     * Creates a new WebEvent object.
     *
     * @param request
     */
    public WebEvent( HttpServletRequest request ) {
        super( request );
    }

    /**
     * @return the parameters
     */
    public Properties getParameter() {
        return this._getParameters( this._getRequest() );
    }

    /**
     * @return the document path
     */
    public String getDocumentPath() {
        return this._getRequest().getRequestURI();
    }

    /**
     * @return the request user
     */
    public RequestUser getRequestUser() {
        return this._getRequestUser( this._getRequest() );
    }

    @Override
    public String toString() {
        return this.getClass().getName().concat( " [ " ).concat( getDocumentPath() ).concat( " ]" );
    }

    /**
     * Returns a list of Properties with key value pairs created out of the incoming POST paramteres.
     */
    private Properties _getParameters( HttpServletRequest request ) {
        Properties p = new Properties();

        for ( Enumeration<?> e = request.getParameterNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            p.setProperty( key, request.getParameter( key ) );
        }

        return p;
    }

    private RequestUser _getRequestUser( HttpServletRequest request ) {
        return new RequestUser( request );
    }

    private HttpServletRequest _getRequest() {
        return (HttpServletRequest) getSource();
    }

    /** @link dependency */

    /* #RequestUser lnkRequestUser; */
}
