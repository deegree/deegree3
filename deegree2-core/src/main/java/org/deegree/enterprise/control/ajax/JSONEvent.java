//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.enterprise.control.ajax;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.stringtree.json.JSONReader;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JSONEvent extends WebEvent {

    private static final long serialVersionUID = 6459849162427895987L;

    private static final ILogger LOG = LoggerFactory.getLogger( JSONEvent.class );

    private Map<String, ?> json;
    
    

    /**
     * 
     * @param servletContext
     * @param request
     * @throws ServletException
     */
    @SuppressWarnings("unchecked")
    JSONEvent( ServletContext servletContext, HttpServletRequest request ) throws ServletException {
        super( servletContext, request, null );
        JSONReader reader = new JSONReader();
        String string = null;
        try {            
            LOG.logDebug( "request character encoding: " + request.getCharacterEncoding() );
            
            InputStreamReader isr = null;
            if ( request.getCharacterEncoding() != null ) {
                isr = new InputStreamReader( request.getInputStream(), request.getCharacterEncoding() );
            } else {
                // always use UTF-8 because XMLHttpRequest normally uses this encoding
                isr = new InputStreamReader( request.getInputStream(), "UTF-8" );
            }
            string = FileUtils.readTextFile( isr ).toString();
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new ServletException( "can not parse json: " + json, e );
        }
        json = (Map<String, ?>) reader.read( string );
        LOG.logDebug( "request parameter: " +  json );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map getParameter() {
        return json;
    }
    
    /**
     * 
     * @param bean
     */
    void setBean( String bean ) {
        this.bean = bean;
    }

    @Override
    public Object getAsBean()
                            throws Exception {
        Class<?> clzz = Class.forName( bean );
        Object bean = clzz.newInstance();
        Method[] methods = clzz.getMethods();           
        for ( Method method : methods ) {
            if ( method.getName().startsWith( "set" ) ) {        
                String var = method.getName().substring( 4, method.getName().length() );
                var = method.getName().substring( 3, 4 ).toLowerCase() + var;
                Object val = json.get( var );
                Type type = method.getGenericParameterTypes()[0];                
                if ( val != null ) {
                    method.invoke( bean, ((Class<?>)type).cast( val ) );
                }
            }
        }
        return bean;
    }

}
