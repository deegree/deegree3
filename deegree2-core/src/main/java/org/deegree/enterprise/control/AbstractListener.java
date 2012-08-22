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
package org.deegree.enterprise.control;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.deegree.datatypes.parameter.ParameterValueIm;
import org.deegree.framework.util.StringTools;

/**
 * The abstract listener allows the reuse of basic functionality.
 *
 * @author <a href="mailto:tfriebe@gmx.net">Torsten Friebe</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @version $Revision$
 */

public abstract class AbstractListener implements WebListener {

    private FormEvent event;

    private Object returnValue;

    private String alternativeDefaultTarget;

    private String alternativeNext;

    private String defaultTarget;

    private String next;

    private List<ParameterValueIm> params;

    /**
     *
     *
     * @param e
     */
    public abstract void actionPerformed( FormEvent e );

    /**
     *
     *
     * @param e
     */
    public final void handle( FormEvent e ) {
        this.event = e;
        this.getNextPageFormRequest();
        this.actionPerformed( e );
        getRequest().setAttribute( "returnValue", getReturnValue() );
        getRequest().setAttribute( "next", getNextPage() );
    }

    /**
     *
     *
     * @return the servlet request
     */
    public ServletRequest getRequest() {
        Object source = this.event.getSource();
        return (ServletRequest) source;
    }

    /**
     * @return the path from the servlet context
     */
    public String getHomePath() {
        String path2Dir = ( (HttpServletRequest) this.getRequest() ).getSession( true ).getServletContext().getRealPath(
                                                                                                                         "/" );
        if ( !path2Dir.startsWith( "/" ) ) {
            path2Dir = '/' + path2Dir;
        }
        return path2Dir;
    }

    /**
     *
     *
     * @param target
     */
    protected final void setDefaultNextPage( String target ) {
        this.defaultTarget = target;
    }

    /**
     *
     *
     * @param target
     */
    protected final void setDefaultAlternativeNextPage( String target ) {
        this.alternativeDefaultTarget = target;
    }

    /**
     * Sets the next page for this request.
     *
     * @param target the name of the next page
     */
    public void setNextPage( String target ) {
        this.next = target;
    }

    /**
     *
     *
     * @return the name of the next page, or the default target
     */
    public String getNextPage() {
        return ( ( this.next == null ) ? this.defaultTarget : this.next );
    }

    /**
     *
     *
     * @param target
     */
    public void setAlternativeNextPage( String target ) {
        this.alternativeNext = target;
    }

    /**
     *
     *
     * @return the name of the alternative next page, or the alternative default target
     */
    public String getAlternativeNextPage() {
        return ( ( this.alternativeNext == null ) ? this.alternativeDefaultTarget : this.alternativeNext );
    }

    /**
     * @return the return value
     *
     */
    public Object getReturnValue() {
        return this.returnValue;
    }

    /**
     * @param model
     *
     */
    public void setReturnValue( Object model ) {
        this.returnValue = model;
    }

    /**
     *
     */
    private void getNextPageFormRequest() {
        String target = null;
        if ( ( target = this.getRequest().getParameter( "nextPage" ) ) != null ) {
            this.setNextPage( target );
        }
    }

    /**
     * @param message the message for the error page
     */
    protected void gotoErrorPage( String message ) {
        getRequest().setAttribute( "SOURCE", "" + this.getClass().getName() );
        getRequest().setAttribute( "MESSAGE", message );
        setNextPage( "error.jsp" );
    }

    /**
     * sets the list of assigned initialization parameters
     *
     * @param params
     */
    void setInitParameterList( List<ParameterValueIm> params ) {
        this.params = params;
    }

    /**
     * @see #setInitParameterList(List)
     * @return the list of assigned initialization parameters
     */
    public List<ParameterValueIm> getInitParameterList() {
        return params;
    }

    /**
     * returns a named initi parameter or <code>null</code> if the parameter is not known
     *
     * @param name
     * @return a named initi parameter or <code>null</code> if the parameter is not known
     */
    public String getInitParameter( String name ) {
        for ( int i = 0; i < params.size(); i++ ) {
            ParameterValueIm param = params.get( i );
            if ( param.getDescriptor().getName().equals( name ) ) {
                return (String) param.getValue();
            }
        }
        return null;
    }

    /**
     * transforms the request to a set of name value pairs stored in a HashMap
     *
     * @return map of request parameters
     */
    protected HashMap<String, String> toModel() {
        HashMap<String, String> model = new HashMap<String, String>();
        ServletRequest req = getRequest();
        Enumeration iterator = req.getParameterNames();

        while ( iterator.hasMoreElements() ) {
            String name = (String) iterator.nextElement();
            String[] value = req.getParameterValues( name );

            int pos = name.indexOf( '@' ) + 1;

            if ( pos < 0 ) {
                pos = 0;
            }

            name = name.substring( pos, name.length() );
            model.put( name.toUpperCase(), StringTools.arrayToString( value, ',' ) );
        }

        return model;
    }
}
