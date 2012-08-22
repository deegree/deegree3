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

// $Id$
package org.deegree.enterprise.control.ajax;

// JDK 1.3
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.EventObject;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.framework.util.KVP2Map;

/**
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * 
 * @version $Revision$
 */
public class WebEvent extends EventObject {
    /**
     * 
     */
    private static final long serialVersionUID = 706936738051288390L;

    private ServletContext servletContext;
    
    protected String bean;
    
    private Map<String,String> param;

    /**
     * Creates a new WebEvent object.
     * 
     * @param servletContext
     * @param request
     * @param bean
     */
    WebEvent( ServletContext servletContext, HttpServletRequest request, String bean ) {
        super( request );
        this.bean = bean;
        this.servletContext = servletContext;
        param = KVP2Map.toMap( this._getRequest() );
    }

    /**
     * @return the parameters
     */
    @SuppressWarnings("unchecked")
    public Map getParameter() {
        return param;
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

    /**
     * 
     * @return requests character encoding
     */
    public String getCharacterEncoding() {
        return _getRequest().getCharacterEncoding();
    }

    /**
     * 
     * @return session assigned to event
     */
    public HttpSession getSession() {
        return _getRequest().getSession();
    }

    /**
     * 
     * @param relativePath
     * @return absolute path based on servlet context
     */
    public String getAbsolutePath( String relativePath ) {
        return servletContext.getRealPath( relativePath );
    }

    /**
     * 
     * @return request parameter mapped to a bean class
     */
    public Object getAsBean() throws Exception {        
        Map<String,String> param = KVP2Map.toMap( this._getRequest() );
        Class<?> clzz = Class.forName( bean );
        Object bean = clzz.newInstance();
        Method[] methods = clzz.getMethods();           
        for ( Method method : methods ) {
            if ( method.getName().startsWith( "set" ) ) {        
                String var = method.getName().substring( 3, method.getName().length() );
                Object val = param.get( var.toUpperCase() );
                Type type = method.getGenericParameterTypes()[0];                
                method.invoke( bean, ((Class<?>)type).cast( val ) );
            }
        }
        return bean;
    }
    
    
    @Override
    public String toString() {
        return this.getClass().getName().concat( " [ " ).concat( getDocumentPath() ).concat( " ]" );
    }

    private RequestUser _getRequestUser( HttpServletRequest request ) {
        return new RequestUser( request );
    }

    private HttpServletRequest _getRequest() {
        return (HttpServletRequest) getSource();
    }

}